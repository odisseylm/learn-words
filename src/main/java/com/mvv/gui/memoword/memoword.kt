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


// https://memowordapp.com/Panel
// https://memowordapp.com/Account


// User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:124.0) Gecko/20100101 Firefox/124.0


// Load (with redirection) https://memoword.online/  ()
//
// Response
//  Cookie: _ga_GPTPQ09Z5W=GS1.1.1711056654.1.1.1711057316.0.0.0; _ga=GA1.1.665749401.1711056654
//

// Load https://memowordapp.com/Account/Login?lng=en
//
// Response:
//  set-cookie: ASP.NET_SessionId=cgprwf4osexryulg0zagaiar; path=/; HttpOnly; SameSite=Lax
//  set-cookie: ASP.NET_SessionId=cgprwf4osexryulg0zagaiar; path=/; HttpOnly; SameSite=Lax
//  set-cookie: __RequestVerificationToken=cl8qiiAvyVz61Ef5h0OT6BLaT2u_0QkcS_aBghRAeveQn0Qcw5Ujqapuf4bbEkPvB-mQUEWco3g8gpej9VbsQZAo1vo43IMCzzisxUQHKVc1; path=/; HttpOnly
//
// Page
//  Login:
//    To log in, enter your email address and registration code that you received when registering your email in the application.
//    <input class="form-control" data-val="true" data-val-email="The Логин field is not a valid e-mail address." data-val-required="Введите логин (e-mail)" id="Login" name="Login" placeholder="example@address.com" type="text" value="">
//    <input class="form-control" data-val="true" data-val-required="Введите пароль" id="Password" name="Password" placeholder="12345" type="password">


// load https://memowordapp.com/Panel with enabled redirection
// if it contains login form, we need to login
//
// Login
//  Content-Type: application/x-www-form-urlencoded
//  Cookie: ASP.NET_SessionId=llku50bga5imlb3eaz1wzu2h; __RequestVerificationToken=WFpbdLyOGvKzJYTJcuA_EK76jPOPCcGi9kch_xfa4jyae2nujPz4uaudnrKicp6h6eVbu26pSI8yzAmMIGM_cLSeBpLFiKaOlt3eMUDlMyM1
//  Origin: https://memowordapp.com
//  Referer: https://memowordapp.com/Account/Login?lng=ru
//  User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:124.0) Gecko/20100101 Firefox/124.0
//  Content:
//    __RequestVerificationToken=Nz1DADK-8_DMdDSp7Gx_ns0QEN3FLNfKUhFc3NVCu3LkHh27b2Nt1DQCbzEukjBC4Dz4qqzLMhnkZFEW82YslYJKOl4ywkaMBDwUMNUt_nc1
//    &Login=vasya.pupkin%40gmail.com
//    &Password=1111
//    &Agreement=true
//    &Agreement=false


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

        val memoLanguageProfileId = memoSettings.languageProfileId
        val memoLanguageProfileName = memoSettings.languageProfileName

        loginIfNeeded(false)

        val author = "Cheburan"

        val creationDateStr = SimpleDateFormat("dd.MM.yyyy").format(Date())

        /*val profileSetsPageResponse: HttpResponse<String> =*/ client.send(
            HttpRequest.newBuilder(URI("https://memowordapp.com/panel/lists/index/$memoLanguageProfileId?lng=en")).GET().build(),
            HttpResponse.BodyHandlers.ofString(),
        )

        val uploadCardSetFormPageResponse: HttpResponse<String> = client.send(
            HttpRequest.newBuilder(URI("https://memowordapp.com/Panel/Import/Index/$memoLanguageProfileId?lng=en")).GET().build(),
            HttpResponse.BodyHandlers.ofString(),
        )

        val formRequestVerificationToken = uploadCardSetFormPageResponse.extractFormRequestVerificationToken()

        //val tempCardSetId = findCardSetId("army")
        //log.info { "### tempCardSetId: $tempCardSetId" }

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
            """<input id="MemoListFullName" name="MemoListFullName" type="hidden" value="$cardSetName - $memoLanguageProfileName" />""",
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

        //log.info { "### uploadCardSetResponse\n${uploadCardSetResponse.body()}" }

        if (alreadyExists && rewrite) {
            deleteCardSet(cardSetName)
            uploadCardSet(csvCardSet, cardSetName, rewrite = false)
        }

        // if success
        // Cards of set "army2 - Ru-En"
        //
        // Key information
        //   Set  army2 - Ru-En
        //   <a href="#" id="aMemoListFullName" data-type="text" class="editable editable-click" style="display: inline;">army3 - Ru-En</a>
        //   <input id="MemoListFullName" name="MemoListFullName" type="hidden" value="army3 - Ru-En" />
        //
        // Parameters of set
        //
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

        // https://memowordapp.com/api/mobile/v10/
        //

        // https://memowordapp.com/panel/lists/index/665ebd51-66cb-43d7-9ad0-ee3f0b489710?lng=en

        // POST
        // https://memowordapp.com/Panel/Lists/RemoveList
        // Headers
        //   Content-Type: application/json; charset=utf-8
        //   Origin: https://memowordapp.com
        //   Referer: https://memowordapp.com/panel/lists/index/665ebd51-66cb-43d7-9ad0-ee3f0b489710?lng=en
        // Content/body
        //   {"id":"d53792ab-1a5d-43a7-91a9-6a7940e43ba5"}
        // Response
        //   content-type: application/json; charset=utf-8
        //
    }

    private fun findCardSetId(cardSetName: String): MemoCardSetInfo? {

        // https://memowordapp.com/panel/lists/GetMemoLists/665ebd51-66cb-43d7-9ad0-ee3f0b489710?sort=UpdateDate&order=desc&offset=0&limit=100
        // https://memowordapp.com/panel/lists/GetMemoLists/665ebd51-66cb-43d7-9ad0-ee3f0b489710

        val languageProfileId = memoSettings.languageProfileId

        val objectMapper = createDefaultObjectMapper().also {
            it.registerModule(KotlinModule.Builder().build())
            it.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }

        val cardSetsResponse: HttpResponse<List<MemoCardSetInfo>> = client.send(
            HttpRequest.newBuilder(URI("https://memowordapp.com/panel/lists/GetMemoLists/$languageProfileId?lng=en")).GET().build(),
            jsonBodyHandler<List<MemoCardSetInfo>>(objectMapper)
        )

        //  - Ru-En
        val cardSet = cardSetsResponse.body().find {
            it.LanguageProfileId == languageProfileId
                && ( it.FullName == cardSetName || it.FullName == "$cardSetName - ${memoSettings.languageProfileName}" )
        }

        return cardSet
    }

    private fun loginToMemoWord() {
        /*
        val cookieHandler = client.cookieHandler().get()

        val cookieDomains = listOf(
            "https://memoword.online/", "https://memowordapp.com",
            "http://memoword.online/", "http://memowordapp.com",
            "memoword.online/", "memowordapp.com",
        )

        val cookies: List<String> = cookieDomains
            .flatMap { cookieHandler.get(URI(it), mapOf()).values.flatten() }
            .distinct()
        val cookieTokenPair = cookies.find { it.startsWith("__RequestVerificationToken=") }
        */

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


/*
https://memowordapp.com/panel/lists/index/665ebd51-66cb-43d7-9ad0-ee3f0b489710
https://memowordapp.com/Panel/Import/Index/665ebd51-66cb-43d7-9ad0-ee3f0b489710

<form action="/Panel/Import/Index/665ebd51-66cb-43d7-9ad0-ee3f0b489710" enctype="multipart/form-data" id="frmImport" method="post">
  <input name="__RequestVerificationToken" type="hidden" value="9taMygSKUG8tOBw3IBL-kzkTOxTsOucDMd1ju9m5Q-abclqHK8B5FRSii-NO_FA2h7_V-juq8Vcfi4xVYRtHZYkm9XkJJzzbk-m49zYJbjn-iuovHoGb2kbJpZMmw-Q9yxTbc1pODGyDBRxn9kpZgw2" />
  <input data-val="true" data-val-required="The LanguageProfileId field is required." id="LanguageProfileId" name="LanguageProfileId" type="hidden" value="665ebd51-66cb-43d7-9ad0-ee3f0b489710" />

  <input data-val="true" data-val-required="The Наименование списка field is required." id="MemoListFullName" name="MemoListFullName" placeholder="set name" type="text" value="New set" />
  <input id="MemoListCreateUser" name="MemoListCreateUser" placeholder="will be used as Author when creating sets" type="text" value="Cheburan" />
  <textarea cols="20" id="Note" name="Note" placeholder="short description" rows="2"></textarea>
  <input id="AdditionalUrl" name="AdditionalUrl" placeholder="Link to additional materials" type="text" value="" />

  <input id="UserFullName" name="UserFullName" type="hidden" value="Cheburan" />
  <input id="LanguageProfileFullName" name="LanguageProfileFullName" type="hidden" value="Ru-En" />

  <input id="MemoListCreateDate" name="MemoListCreateDate" type="hidden" value="22.03.2024" />
  <input id="MemoListSourceType" name="MemoListSourceType" type="hidden" value="Imported Excel" />

  <input accept=".csv, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" class="btn btn-primary" id="File" name="File" style="width: 300px" type="file" value="" />

  <input type="hidden" id="current_lang" value="en-US" />

</form>
*/

/*

fetch('https://memowordapp.com/api/mobile/v10/app/lastVersion',{method: 'POST', headers: {'test': 'TestPost'} })
  .then(response => response.json())
  .then(json => console.log(json))

fetch('https://memowordapp.com/api/mobile/v10/app/lastVersion',{method: 'POST'})
  .then(response => response.json())
  .then(json => console.log(json))


++
fetch('https://memowordapp.com/api/app/ping',{
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({})
    })
  .then(response => response.json())
  .then(json => console.log(json))

-- null
fetch('https://memowordapp.com/api/app/lastVersion',{
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({})
    })
  .then(response => response.json())
  .then(json => console.log(json))

-- error
fetch('https://memowordapp.com/api/app/versionInfo',{
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({})
    })
  .then(response => response.json())
  .then(json => console.log(json))


-- error "Отсутствует параметр X-Auth-AppId"
fetch('https://memowordapp.com/api/languagesProfiles/list',{
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({})
    })
  .then(response => response.json())
  .then(json => console.log(json))


-- error "Отсутствует параметр X-Auth-AppId"
fetch('https://memowordapp.com/api/memoCards/list',{
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-Auth-AppId': 'ru.mobsolutions.memoword',
      'X-Auth-Method': '' // XAuthMethod = "sha512mob";
      },
    body: JSON.stringify({})
    })
  .then(response => response.json())
  .then(json => console.log(json))


    @POST("/api/app/lastVersion")
    Observable<Response<AppVersionResponseModel>> getLastVersion(@HeaderMap Map<String, String> map, @Body RequestBody requestBody);

    @POST("/api/app/ping")
    Observable<Response<AppPingModel>> getPingUnsigned(@HeaderMap Map<String, String> map);

    @POST("/api/app/versionInfo")
    Observable<Response<AppVersionResponseModel>> getVersionInfo(@HeaderMap Map<String, String> map, @Body RequestBody requestBody);

    @POST("/api/languagesProfiles/list")
    Observable<Response<List<LangProfileModel>>> languageProfilesGet(@HeaderMap Map<String, String> map, @Body RequestBody requestBody);

    @POST("/api/languages/list")
    Observable<Response<List<LanguageModel>>> languagesList(@HeaderMap Map<String, String> map, @Body RequestBody requestBody);

    @POST("/api/memoCards/list")
    Observable<Response<List<MemoCardModel>>> memoCardsGet(@HeaderMap Map<String, String> map, @Body RequestBody requestBody);

    @POST("/api/memoCardsLists/list")
    Observable<Response<List<CardToListModel>>> memoCardsToListsGet(@HeaderMap Map<String, String> map, @Body RequestBody requestBody);

    @POST("/api/memoLists/list")
    Observable<Response<List<MemoListModel>>> memoListsGet(@HeaderMap Map<String, String> map, @Body RequestBody requestBody);

    @POST("/api/memoLists/requestSets")
    Observable<Response<Object>> sendInfos(@HeaderMap Map<String, String> map, @Body RequestBody requestBody);
*/


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
