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
import org.jsoup.nodes.Document
import java.net.*
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.*


private val log = mu.KotlinLogging.logger {}


class MemoWordSession : AutoCloseable {

    private val cookieStore = PersistentCookieStore(getProjectDirectory().resolve("temp/.memoCookies.json"))
        .also { it.load() }
    private val cookieManager = CookieManager(cookieStore, CookiePolicy.ACCEPT_ORIGINAL_SERVER)

    private val client = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .cookieHandler(cookieManager)
        .build()

    private val memoSettings: MemoSettings by lazy {
        requireNotNull(settings.memoSettings) { "MemoWord settings are not set properly." } }

    override fun close() { cookieStore.save() }

    fun connect() {
        client.sendGet("https://memoword.online/en/")
        loginIfNeeded(true)
    }

    private fun loginIfNeeded(logState: Boolean) {
        val loginPageResponse = client.sendGet("https://memowordapp.com/Account/Login?lng=en")

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

    fun uploadCardSet(cardSetName: String, csvCardSetFile: Path, rewrite: Boolean) {

        log.info { "Uploading card set '$cardSetName' from $csvCardSetFile" }

        loginIfNeeded(false)

        val memoLanguageProfileId = memoSettings.languageProfileId
        val memoLanguageProfileName = memoSettings.languageProfileName
        val author = "Cheburan"
        val creationDateStr = SimpleDateFormat("dd.MM.yyyy").format(Date())

        client.sendGet("https://memowordapp.com/panel/lists/index/$memoLanguageProfileId?lng=en")

        val uploadCardSetFormPageResponse = client.sendGet(
            "https://memowordapp.com/Panel/Import/Index/$memoLanguageProfileId?lng=en")

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
            .filePart("File", csvCardSetFile, MediaType.parse("text/csv"))
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
            "An error occured processing your request", // 'occured' - now HTML contains mistake :-)
            "An error occurred processing your request",
            "An error occured",
            "An error occurred",
        )

        val uploadCardSetResponseHtml = uploadCardSetResponse.body().parseAsHtml()
        val fullCardSetName = "$cardSetName - $memoLanguageProfileName"

        val containsCardSetTextParts = uploadCardSetResponse.body().containsOneOf(
            "Sets ($fullCardSetName)",
            ">$fullCardSetName<",
            )
        val containsCardSetHRef =
            uploadCardSetResponseHtml.containsHRef(id = "aMemoListFullName", innerText = fullCardSetName, ignoreCase = true) ||
            uploadCardSetResponseHtml.containsHRef(id =  "MemoListFullName", innerText = fullCardSetName, ignoreCase = true)
        val containsCardSetInput =
            uploadCardSetResponseHtml.containsInput(name =  "MemoListFullName", value = fullCardSetName, ignoreCase = true) ||
            uploadCardSetResponseHtml.containsInput(name = "aMemoListFullName", value = fullCardSetName, ignoreCase = true)

        val seemsSuccess = containsCardSetTextParts || containsCardSetHRef || containsCardSetInput

        /*
        val seemsSuccess = uploadCardSetResponse.body().containsOneOf(
            "Sets ($fullCardSetName)",
            ">$fullCardSetName<",
            """<a href="#" id="aMemoListFullName" data-type="text" class="editable editable-click" style="display: inline;">$fullCardSetName</a>""",
            """<input id="MemoListFullName" name="MemoListFullName" type="hidden" value="$fullCardSetName" />""",
            // Ideal HTML should be parsed and <option/> found and verified.
            // <option selected="selected" value="8b1f872c-001d-4f6a-a2c6-0b01ee732572">army3 - Ru-En</option>
        )
        */

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
            uploadCardSet(cardSetName, csvCardSetFile, rewrite = false)
        }
    }

    private fun deleteCardSet(cardSetName: String) {

        log.info { "Removing card set '$cardSetName'" }

        val cardSet = findCardSetId(cardSetName)
            ?: throw IllegalStateException("Card Set '$cardSetName' is not found.")

        deleteCardSet(cardSet)
    }

    fun deleteExistentCardSets(cardSetNames: Iterable<String>) {

        log.info { "Removing existent card sets of '${cardSetNames}'" }

        val cardSetNamesAndFullNamesToDelete = cardSetNames.toSet() +
                cardSetNames.map { "$it - ${memoSettings.languageProfileName}" }

        val allCardSets = getCardSets()

        val existent = allCardSets.filter { cardSetNamesAndFullNamesToDelete.contains(it.FullName) }

        existent.forEach {
            deleteCardSet(it)
        }
    }

    private fun deleteCardSet(cardSet: MemoCardSetInfo) {
        require(cardSet.CanDelete) { "Card set '${cardSet.FullName}' / ${cardSet.MemoListId} is not deletable." }

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
            throw IllegalStateException("Error of deleting MemoWord card set '${cardSet.FullName}' / ${cardSet.MemoListId}.")
    }

    private fun findCardSetId(cardSetName: String): MemoCardSetInfo? {
        val cardSets = getCardSets()

        val cardSet = cardSets.find {
            ( it.FullName == cardSetName || it.FullName == "$cardSetName - ${memoSettings.languageProfileName}" ) }

        return cardSet
    }

    private fun getCardSets(): List<MemoCardSetInfo> {
        // https://memowordapp.com/panel/lists/GetMemoLists/665ebd51-66cb-43d7-9ad0-ee3f0b489710
        // https://memowordapp.com/panel/lists/GetMemoLists/665ebd51-66cb-43d7-9ad0-ee3f0b489710?sort=UpdateDate&order=desc&offset=0&limit=100

        val languageProfileId = memoSettings.languageProfileId

        val objectMapper = createDefaultObjectMapper().also {
            it.registerModule(KotlinModule.Builder().build())
            it.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }

        val cardSetsResponse: HttpResponse<List<MemoCardSetInfo>> = client.sendJsonGet(
            "https://memowordapp.com/panel/lists/GetMemoLists/$languageProfileId?lng=en",
            objectMapper
        )

        val allCardSets = cardSetsResponse.body()
        val cardSets = allCardSets.filter { it.LanguageProfileId == languageProfileId }
        return cardSets
    }

    private fun loginToMemoWord() {

        val loginPageResponse = client.sendGet("https://memowordapp.com/Account/Login?lng=en")

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
    val formPageDoc: Document = this.body().parseAsHtml()

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
    MemoWordSession().use {
        it.connect()
        it.uploadCardSet(
            "army3",
            Path.of("/home/vmelnykov/english/words/grouped/army-RuEn-MemoWord.csv"),
            rewrite = true
        )
    }
}


/*

Get Card Sets (Memo Lists)
https://memowordapp.com/panel/lists/GetMemoLists/665ebd51-66cb-43d7-9ad0-ee3f0b489710?sort=UpdateDate&order=desc&offset=0&limit=100
https://memowordapp.com/panel/lists/GetMemoLists/665ebd51-66cb-43d7-9ad0-ee3f0b489710

Get  ALL cards
https://memowordapp.com/panel/words/GetMemoWords/b7760ca3-56e9-44e7-8047-6427fc22b4ac?order=asc&offset=0&limit=100
https://memowordapp.com/panel/words/GetMemoWords/b7760ca3-56e9-44e7-8047-6427fc22b4ac

Already learned (I know)
https://memowordapp.com/panel/words/GetMemoWords/685e3f2b-00f8-4484-ab97-a216c589e5ed?order=asc&offset=0&limit=100
https://memowordapp.com/panel/words/GetMemoWords/685e3f2b-00f8-4484-ab97-a216c589e5ed

Download CardSet in Excel format
https://memowordapp.com/Panel/words/Download?listId=b7d6bed0-e1dd-47d0-8a72-180d9b9ff349


Modify card
https://memowordapp.com/Panel/Card/Save ??

<form action="/Panel/Card/Save" id="cardForm" method="post">
<input id="MemoListId" name="MemoListId" type="hidden" value="b7d6bed0-e1dd-47d0-8a72-180d9b9ff349" />
<input id="MemoCardId" name="MemoCardId" type="hidden" value="c107b2df-3abf-412e-804b-bc1ef49f3731" />
<textarea id="textFrom" name="TextFrom" />
<textarea id="textTo" name="TextTo" />
<select class="form-control" id="memoLists" name="SelectedMemoList" style="width: 300px;">
...
</select>
<input type="submit" id="saveCard" class="btn btn-default" value="Сохранить" />

?? It is ent as json ??
Headers
  "name": "X-Requested-With",  "value": "XMLHttpRequest"

"postData": {
"mimeType": "application/json; charset=utf-8",
"params": [],
"text": '{
  "MemoCardId":"0c78e519-fc46-4477-bca1-f1fa0d6bc638",
  "MemoListId":"b7d6bed0-e1dd-47d0-8a72-180d9b9ff349",
  "MemoListIds":["b7d6bed0-e1dd-47d0-8a72-180d9b9ff349","b7760ca3-56e9-44e7-8047-6427fc22b4ac"],
  "MemoCardPartOfSpeechId":"7",
  "Note":"",
  "TextFrom":"привет 55",
  "TextTo":"hello",
  "SelectedMemoList":"771d766d-96db-46d0-9db8-048330e054c6"
  }'
}
Answer
"content": {
"size": 70,
"text": '{"Redirect":"/Panel/Words/Index/b7d6bed0-e1dd-47d0-8a72-180d9b9ff349"}'
},

Insert card JSON
https://memowordapp.com/Panel/Card/Save
{
"MemoCardId":"",
"MemoListId":"b7d6bed0-e1dd-47d0-8a72-180d9b9ff349",
"MemoListIds":["b7d6bed0-e1dd-47d0-8a72-180d9b9ff349","57e26534-68d7-4498-9a27-026997b5da79"],
"MemoCardPartOfSpeechId":"7",
"Note":"",
"TextFrom":"привет",
"TextTo":"hello",
"SelectedMemoList":"771d766d-96db-46d0-9db8-048330e054c6"
}

https://memowordapp.com/panel/words/GetMemoWords/b7d6bed0-e1dd-47d0-8a72-180d9b9ff349?order=asc&offset=0&limit=100


Get CardSets for specific card
https://memowordapp.com/panel/card/GetMemoCardLists/c107b2df-3abf-412e-804b-bc1ef49f3731?order=asc


RemoveWordsFromList
https://memowordapp.com/Panel/Words/RemoveWordsFromList
{"MemoListId":"b7d6bed0-e1dd-47d0-8a72-180d9b9ff349","MemoCardIds":["0c78e519-fc46-4477-bca1-f1fa0d6bc638"]}


MoveWordsToList (really it does NOT move, it adds)
https://memowordapp.com/Panel/Words/MoveWordsToList
{"MemoListId":"1614e343-21ae-4379-a0a1-3c103b530e7e","MemoCardIds":["5a05c72f-fc57-4497-9812-02ea32181e6b"]}



Card
{
  "MemoCardId":"51f6268b-a397-46f9-b38b-aa63d9237e9d",
  "LanguageFromId":68,
  "LanguageFrom":"русский",
  "LanguageToId":4,
  "LanguageTo":"английский",
  "PartOfSpeechId":7,
  "PartOfSpeech":"Фр",
  "SourceTypeId":552,
  "SourceType":"Imported Excel",
  "TranslationServiceId":601,
  "TranslationService":"Google Translation API",
  "TextFrom":"дело, дела, занятия, афера, вещь",
  "TextTo":"affair",
  "Note":"[əˈfɛə]",
  "IsActive":true,
  "InsertDate":"\/Date(1693856014434)\/",
  "UpdateDate":null,
  "OthersLists":[
    {"Id":"685e3f2b-00f8-4484-ab97-a216c589e5ed","Name":"I know"},
    {"Id":"f5a057f9-6239-488c-82dc-418d76c460b8","Name":"Repeat"},
    {"Id":"358ab8d6-9732-4dd8-9d23-ca4583c14a2a","Name":"Equiod p01_01 - Ru-En"}
    ],
    "OrderNumber":1
}

*/
