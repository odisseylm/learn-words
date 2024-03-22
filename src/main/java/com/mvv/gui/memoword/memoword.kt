package com.mvv.gui.memoword

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.mizosoft.methanol.MediaType
import com.github.mizosoft.methanol.MultipartBodyPublisher
import com.mvv.gui.cardeditor.MemoSettings
import com.mvv.gui.cardeditor.memoSettings
import com.mvv.gui.cardeditor.settings
import com.mvv.gui.dictionary.getProjectDirectory
import com.mvv.gui.util.containsOneOf
import com.mvv.gui.util.isOneOf
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.*
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.*


private val log = mu.KotlinLogging.logger {}


class MemoSession : AutoCloseable {

    private val cookieStore = PersistentCookieStore(getProjectDirectory().resolve("temp/.memoCookies.json"))
        .also { it.load() }
    private val cookieManager = CookieManager(cookieStore, CookiePolicy.ACCEPT_ORIGINAL_SERVER)

    private val client = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .cookieHandler(cookieManager)
        .build()

    private val memoSettings: MemoSettings by lazy { requireNotNull(settings.memoSettings) { "MemoWord settings are not set properly." } }

    override fun close() { cookieStore.save() }

    fun connect() {
        client.send(
            HttpRequest.newBuilder(URI("https://memoword.online/en/")).GET().build(),
            HttpResponse.BodyHandlers.ofString(),
        )

        loginIfNeeded(true)
    }

    private fun loginIfNeeded(logState: Boolean) {
        val loginPageResponse: HttpResponse<String> = client.send(
            HttpRequest.newBuilder(URI("https://memowordapp.com/Account/Login?lng=en")).GET().build(),
            HttpResponse.BodyHandlers.ofString(),
        )

        val needToLogin = loginPageResponse.body().containsOneOf(
            "To log in, enter your email address",
            " type=\"password\""
        )

        if (needToLogin) {
            if (logState) log.info("Need to login to MemoWord site.")
            loginToMemoWord()
        }
        else
            if (logState) log.info("User is already logged in MemoWord site.")
    }

    fun uploadCardSet(csvCardSet: Path, cardSetName: String, rewrite: Boolean) {

        log.info { "Uploading card set '$cardSetName' from $csvCardSet" }

        loginIfNeeded(false)

        val memoLanguageProfileId = memoSettings.languageProfileId
        val memoLanguageProfileName = memoSettings.languageProfileName
        val author = "Cheburan"
        val creationDateStr = SimpleDateFormat("dd.MM.yyyy").format(Date())

        client.send(
            HttpRequest.newBuilder(URI("https://memowordapp.com/panel/lists/index/$memoLanguageProfileId?lng=en")).GET().build(),
            HttpResponse.BodyHandlers.ofString(),
        )

        val uploadCardSetFormPageResponse: HttpResponse<String> = client.send(
            HttpRequest.newBuilder(URI("https://memowordapp.com/Panel/Import/Index/$memoLanguageProfileId?lng=en")).GET().build(),
            HttpResponse.BodyHandlers.ofString(),
        )

        val formRequestVerificationToken = uploadCardSetFormPageResponse.extractFormRequestVerificationToken()

        // https://mizosoft.github.io/methanol/multipart_and_forms/#multipart-bodies
        val multipartBodyPublisher = MultipartBodyPublisher.newBuilder()
            .textPart("__RequestVerificationToken", formRequestVerificationToken)
            .textPart("LanguageProfileId", memoLanguageProfileId)
            .textPart("LanguageProfileFullName", memoLanguageProfileName)
            .textPart("MemoListFullName", cardSetName)
            .textPart("MemoListCreateUser", author)
            .textPart("UserFullName", author)
            .textPart("MemoListCreateDate", creationDateStr)
            .textPart("MemoListSourceType", "Imported Excel")
            .textPart("Note", "")
            .textPart("AdditionalUrl", "")
            .textPart("current_lang", "en-US")
            .filePart("File", csvCardSet, MediaType.parse("text/csv"))
            .build()

        val uploadCardSetResponse: HttpResponse<String> = client.send(
            HttpRequest.newBuilder(URI("https://memowordapp.com/Panel/Import/Index/$memoLanguageProfileId?lng=en"))
                .header("Content-Type", multipartBodyPublisher.mediaType().toString())
                .header("Origin", "https://memowordapp.com")
                .header("Referer", "https://memowordapp.com/Panel/Import/Index/$memoLanguageProfileId")
                .POST(multipartBodyPublisher)
                .build(),
            HttpResponse.BodyHandlers.ofString(),
        )

        val alreadyExists = uploadCardSetResponse.body().containsOneOf("The list with this name already exists")
        val isError = uploadCardSetResponse.body().containsOneOf(
            "An error occured processing your request",
            "An error occurred processing your request",
            "An error occured",
            "An error occurred",
        )
        val seemsSuccess = uploadCardSetResponse.body().containsOneOf(
            "Sets ($cardSetName - $memoLanguageProfileName)",
            ">$cardSetName - $memoLanguageProfileName<",
            """<a href="#" id="aMemoListFullName" data-type="text" class="editable editable-click" style="display: inline;">$cardSetName - $memoLanguageProfileName</a>""",
            """<input id="MemoListFullName" name="MemoListFullName" type="hidden" value="$cardSetName - $memoLanguageProfileName" />""",
            // Ideal HTML should be parsed and <option/> found and verified.
            // <option selected="selected" value="8b1f872c-001d-4f6a-a2c6-0b01ee732572">army3 - Ru-En</option>
        )

        if (isError) {
            log.info { "Error" }
            throw IllegalStateException("Error of upload CSV.")
        }
        else if (alreadyExists)
            log.info { "Card set '$cardSetName' already exists." }
        else if (seemsSuccess)
            log.info { "Card set '$cardSetName' is uploaded." }
        else
            throw IllegalStateException("Unknown upload status.")

        if (alreadyExists && rewrite) {
            deleteCardSet(cardSetName)
            uploadCardSet(csvCardSet, cardSetName, rewrite = false)
        }
    }

    private fun deleteCardSet(cardSetName: String) {

        log.info { "Removing card set '$cardSetName'" }

        val cardSet = findCardSetId(cardSetName)
            ?: throw IllegalStateException("Card Set '$cardSetName' is not found.")

        require(cardSet.CanDelete) { "Card set '$cardSetName' / ${cardSet.MemoListId} is not deletable." }

        val deleteCardSetResponse: HttpResponse<String> = client.send(
            HttpRequest.newBuilder()
                .uri(URI("https://memowordapp.com/Panel/Lists/RemoveList"))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Origin", "https://memowordapp.com")
                .header("Referer", "https://memowordapp.com/panel/lists/index/${memoSettings.languageProfileId}")
                .POST(HttpRequest.BodyPublishers.ofString(""" {"id":"${cardSet.MemoListId}"} """))
                .build(),
            HttpResponse.BodyHandlers.ofString(),
        )

        val isDeleted = deleteCardSetResponse.statusCode().isOneOf(200, 201)
        if (!isDeleted)
            throw IllegalStateException("Error of deleting MemoWord card set '$cardSetName' / ${cardSet.MemoListId}")
    }

    private fun findCardSetId(cardSetName: String): MemoCardSetInfo? {

        // https://memowordapp.com/panel/lists/GetMemoLists/665ebd51-66cb-43d7-9ad0-ee3f0b489710
        // https://memowordapp.com/panel/lists/GetMemoLists/665ebd51-66cb-43d7-9ad0-ee3f0b489710?sort=UpdateDate&order=desc&offset=0&limit=100

        val languageProfileId = memoSettings.languageProfileId

        val objectMapper = createDefaultObjectMapper().also {
            it.registerModule(KotlinModule.Builder().build())
            it.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }

        val cardSetsResponse: HttpResponse<List<MemoCardSetInfo>> = client.send(
            HttpRequest.newBuilder(URI("https://memowordapp.com/panel/lists/GetMemoLists/$languageProfileId?lng=en")).GET().build(),
            jsonBodyHandler<List<MemoCardSetInfo>>(objectMapper)
        )

        val cardSet = cardSetsResponse.body().find {
            it.LanguageProfileId == languageProfileId
                && ( it.FullName == cardSetName || it.FullName == "$cardSetName - ${memoSettings.languageProfileName}" )
        }

        return cardSet
    }

    private fun loginToMemoWord() {

        val loginPageResponse: HttpResponse<String> = client.send(
            HttpRequest.newBuilder(URI("https://memowordapp.com/Account/Login?lng=en")).GET().build(),
            HttpResponse.BodyHandlers.ofString(),
        )

        val formRequestVerificationToken: String = loginPageResponse.extractFormRequestVerificationToken()

        val email = settings.memoLogin
        val psw = settings.memoPassword
        require(email != null && psw != null) { "Memo login mame/psw is not set." }

        val loginResponse: HttpResponse<String> = client.send(
            HttpRequest.newBuilder()
                .uri(URI("https://memowordapp.com/Account/Login"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Origin", "https://memowordapp.com")
                .header("Referer", "https://memowordapp.com/Account/Login?lng=en")
                .header("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:124.0) Gecko/20100101 Firefox/124.0")
                //.header("Sec-Fetch-Site", "same-origin")
                .POST(ofFormData(mapOf(
                    "__RequestVerificationToken" to formRequestVerificationToken,
                    "Login" to email,
                    "Password" to psw,
                    "Agreement" to "true",
                )))
                .build(),
            HttpResponse.BodyHandlers.ofString(),
        )

        val loggedInSuccessfully = loginResponse.body().containsOneOf(
            "MemoWord - MemoWord - Personal cabinet",
            "/Account/LogOut",
        )
        if (!loggedInSuccessfully)
            throw IllegalStateException("Error of logging into MemoWord site.")

        log.info { "Successful logging into MemoWord site." }
    }

}


private fun HttpResponse<String>.extractFormRequestVerificationToken(): String {
    val formPageDoc: Document = Jsoup.parse(this.body())

    return formPageDoc.forms()
        .flatMap { it.select("input") }
        .find { it.attributeValue("name") == "__RequestVerificationToken" }
        ?.attributeValue("value")
        ?: throw IllegalStateException(
            "Error of logging into MemoWord site.",
            IllegalStateException("No form __RequestVerificationToken")
        )
}


@Suppress("PropertyName")
internal data class MemoCardSetInfo (
    val MemoListId: String,        // "57e26534-68d7-4498-9a27-026997b5da79"
    val LanguageProfileId: String, // "665ebd51-66cb-43d7-9ad0-ee3f0b489710"
    val LanguageProfile: String?,  // "Ru-En"

    val FullName: String?,  // "My words"    "army2 - Ru-En"
    val Note: String?,      // "Your first set for the cards you created"
    val Author: String?,    // "Cheburan"
    val ListType: String?,  // "Служебный"

    val IsActive: Boolean?, // true
    val CanDelete: Boolean,

    //val LanguageFromId: Long?, // 68
    //val LanguageFrom: String?, // "русский"
    //val LanguageToId: Long?,   // 4
    //val LanguageTo: String?,   // "английский"

    //val CardTypeId: Any?,      // null
    //val CardType: Any?,        // null
    //val LearnTypeId: Long?,    // 701
    //val LearnType: String?,    // "Учу"
    //val SourceTypeId: Long?,   // 555
    //val SourceType: String?,   // "Служебные сеты"
    //val ProductId: Any?,       // null
    //val IsPaid: Boolean?,      // false
    //val IsPublic: Boolean?,    // false
    //val IsDefault: Boolean?,   // true
    //val InsertDate: String?,   // "/Date(1691696098994)/"
    //val UpdateDate: String?,   // "/Date(1694104076757)/"
    //val Qty: Long?,            // 3
    //val AccessEmails: Any?,    // null
    //val OriginalListId: Any?,  // null
    //val OriginalListProductId: Any?, // null
    //val AuthorId: Long?,       // 422205
    //val PartnerId: Any?,       // null
    //val CanEdit: Boolean,      // false
    //val Courses: String?,      // ""
    //val CanDelete: Boolean?,   // false
)


fun main() {
    MemoSession().use {
        it.connect()
        it.uploadCardSet(Path.of("/home/vmelnykov/english/words/grouped/army-RuEn-MemoWord.csv"), "army3", rewrite = true)
    }
}
