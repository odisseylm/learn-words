package com.mvv.gui.memoword

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.github.mizosoft.methanol.MediaType
import com.github.mizosoft.methanol.MoreBodyPublishers
import com.github.mizosoft.methanol.MultipartBodyPublisher
import com.mvv.gui.cardeditor.MemoSettings
import com.mvv.gui.cardeditor.actions.CompareSenseResult
import com.mvv.gui.cardeditor.actions.isSenseBetter
import com.mvv.gui.cardeditor.memoSettings
import com.mvv.gui.cardeditor.settings
import com.mvv.gui.dictionary.getProjectDirectory
import com.mvv.gui.util.*
import com.mvv.gui.words.*
import org.jsoup.nodes.Document
import java.net.*
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.*
import java.util.Collections.synchronizedList
import kotlin.io.path.extension


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

    private val tempMemoLists: MutableList<MemoList> = synchronizedList(mutableListOf())

    private val systemMemoLists: List<MemoList> by lazy {
        loginIfNeeded()

        /*
          {
            "FullName": "All cards",
            "CardTypeId": 751,
            "CardType": "Все карточки",
            "ListType": "Служебный",
            "SourceTypeId": 555,
            "SourceType": "Служебные сеты",
            "IsDefault": false,
            "IsActive": true,
            "CanEdit": false,
            "CanDelete": false
          },
          {
            "FullName": "I know",
            "CardTypeId": 752,
            "CardType": "Уже знаю",
            "ListType": "Служебный",
            "SourceTypeId": 555,
            "SourceType": "Служебные сеты",
            "IsDefault": false,
            "IsActive": true,
            "CanEdit": false,
            "CanDelete": false
          },
          {
            "FullName": "Difficult",
            "CardTypeId": 753,
            "CardType": "Сложно",
            "ListType": "Служебный",
            "SourceTypeId": 555,
            "SourceType": "Служебные сеты",
            "IsPublic": false,
            "IsDefault": false,
            "IsActive": true,
            "CanEdit": false,
            "CanDelete": false
          },
          {
            "FullName": "Repeat",
            "CardTypeId": 754,
            "CardType": "Повторить",
            "ListType": "Служебный",
            "SourceTypeId": 555,
            "SourceType": "Служебные сеты",
            "IsDefault": false,
            "IsActive": true,
            "CanEdit": false,
            "CanDelete": false
          },
          {
            "FullName": "My words",
            "CardTypeId": null,
            "CardType": null,
            "ListType": "Служебный",
            "SourceTypeId": 555,
            "SourceType": "Служебные сеты",
            "IsDefault": true,
            "IsActive": true,
            "CanEdit": false,
            "CanDelete": false
          },
        */

        downloadMemoLists()
            .filter {
                it.ListType == "Служебный" ||
                //it.CardTypeId.isOneOf(751, 752, 753, 754) ||
                it.CardTypeId != null ||
                (!it.CanEdit && !it.CanDelete)
                //it.CardType == "Все карточки" ||
                //it.SourceTypeId == 555L ||
                //it.SourceType == "Служебные сеты" ||
                //it.FullName.isOneOf("All cards", "I know", "Difficult", "Repeat", ignoreCase = true)
            }
    }

    private val allCardMemoList: MemoListEntry by lazy {
        var allCardsMemoList: MemoList? = systemMemoLists.find {
            it.CardTypeId == MemoListType.AllCards.id || it.FullName == MemoListType.AllCards.fullName }

        if (allCardsMemoList == null)
            allCardsMemoList = systemMemoLists.maxByOrNull { it.Qty ?: 0 }

        requireNotNull(allCardsMemoList) { "Error of finding ${MemoListType.AllCards.fullName}." }
        allCardsMemoList.asMemoListEntry
    }


    override fun close() { cookieStore.save() }

    fun connect() {
        client.sendGet("https://memoword.online/en/")
        loginIfNeeded(true)
    }

    private fun loginIfNeeded(logState: Boolean = false) {
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

    @Suppress("unused")
    fun uploadMemoList(memoListName: String, xlsxOrCsvMemoListFile: Path, rewrite: Boolean) =
        uploadMemoList(memoListName, FileSource(xlsxOrCsvMemoListFile), rewrite)

    @Suppress("SameParameterValue")
    private fun uploadMemoList(memoListName: String, cards: List<CardWordEntry>, rewrite: Boolean): MemoList {
        log.info { "uploadMemoList ${cards.debugStr()}" }

        val validCards = filterInsertingCards(cards)
        if (validCards.isEmpty()) {
            log.info { "No valid cards to upload." }
            return findMemoList(memoListName) ?: createMemoList(memoListName)
        }

        val memoWordXlsxAsBytes = wordCardsIntoMemoWordXlsx(memoListName, cards)
        return uploadMemoList(memoListName, BytesSource(memoWordXlsxAsBytes, XlsxMediaType), rewrite)
    }

    private fun uploadMemoList(memoListName: String, xlsxOrCsvMemoListFile: DataSource, rewrite: Boolean): MemoList {

        log.info { "Uploading card set '$memoListName' from $xlsxOrCsvMemoListFile" }

        loginIfNeeded(false)

        val memoLanguageProfileId   = memoSettings.languageProfileId
        val memoLanguageProfileName = memoSettings.languageProfileName
        val author                  = memoSettings.author
        val creationDateStr = SimpleDateFormat("dd.MM.yyyy").format(Date())

        client.sendGet("https://memowordapp.com/panel/lists/index/$memoLanguageProfileId?lng=en")

        val uploadMemoListFormPageResponse = client.sendGet(
            "https://memowordapp.com/Panel/Import/Index/$memoLanguageProfileId?lng=en")

        val formRequestVerificationToken = uploadMemoListFormPageResponse.extractFormRequestVerificationToken()

        // https://mizosoft.github.io/methanol/multipart_and_forms/#multipart-bodies
        val multipartBodyPublisherB = MultipartBodyPublisher.newBuilder()
            .textPart("__RequestVerificationToken", formRequestVerificationToken)
            .textPart("LanguageProfileId", memoLanguageProfileId)
            .textPart("LanguageProfileFullName", memoLanguageProfileName)
            .textPart("MemoListFullName", memoListName)
            .textPart("MemoListCreateUser", author)
            .textPart("UserFullName", author)
            .textPart("MemoListCreateDate", creationDateStr)
            .textPart("MemoListSourceType", "Imported Excel")
            .textPart("Note", "")
            .textPart("AdditionalUrl", "")
            .textPart("current_lang", "en-US")

        when (xlsxOrCsvMemoListFile) {
            is FileSource  -> multipartBodyPublisherB.filePart("File", xlsxOrCsvMemoListFile.file, xlsxOrCsvMemoListFile.mediaType)
            is BytesSource -> multipartBodyPublisherB.filePart("File", xlsxOrCsvMemoListFile.bytes, "${memoListName}.xlsx", xlsxOrCsvMemoListFile.mediaType)
        }

        val multipartBodyPublisher = multipartBodyPublisherB.build()

        val uploadMemoListResponse: HttpResponse<String> = client.send(
            HttpRequest.newBuilder(URI("https://memowordapp.com/Panel/Import/Index/$memoLanguageProfileId?lng=en"))
                .header("Content-Type", multipartBodyPublisher.mediaType().toString())
                .header("Origin",  "https://memowordapp.com")
                .header("Referer", "https://memowordapp.com/Panel/Import/Index/$memoLanguageProfileId")
                .POST(multipartBodyPublisher)
                .build(),
            HttpResponse.BodyHandlers.ofString(),
        )

        val alreadyExists = uploadMemoListResponse.body().containsOneOf("The list with this name already exists")
        val isError = uploadMemoListResponse.body().containsOneOf(
            "An error occured processing your request", // 'occured' - now HTML contains mistake :-)
            "An error occurred processing your request",
            "An error occured",
            "An error occurred",
        )

        val uploadMemoListResponseHtml = uploadMemoListResponse.body().parseAsHtml()
        val fullMemoListName = "$memoListName - $memoLanguageProfileName"

        val containsMemoListTextParts = uploadMemoListResponse.body().containsOneOf(
            "Sets ($fullMemoListName)",
            ">$fullMemoListName<",
            )
        val containsMemoListHRef =
            uploadMemoListResponseHtml.containsHRef(id = "aMemoListFullName", innerText = fullMemoListName, ignoreCase = true) ||
            uploadMemoListResponseHtml.containsHRef(id =  "MemoListFullName", innerText = fullMemoListName, ignoreCase = true)
        val containsMemoListInput =
            uploadMemoListResponseHtml.containsInput(name =  "MemoListFullName", value = fullMemoListName, ignoreCase = true) ||
            uploadMemoListResponseHtml.containsInput(name = "aMemoListFullName", value = fullMemoListName, ignoreCase = true)

        val seemsSuccess = containsMemoListTextParts || containsMemoListHRef || containsMemoListInput

        /*
        val seemsSuccess = uploadMemoListResponse.body().containsOneOf(
            "Sets ($fullMemoListName)",
            ">$fullMemoListName<",
            """<a href="#" id="aMemoListFullName" data-type="text" class="editable editable-click" style="display: inline;">$fullMemoListName</a>""",
            """<input id="MemoListFullName" name="MemoListFullName" type="hidden" value="$fullMemoListName" />""",
            // Ideal HTML should be parsed and <option/> found and verified.
            // <option selected="selected" value="8b1f872c-001d-4f6a-a2c6-0b01ee732572">army3 - Ru-En</option>
        )
        */

        if (isError) {
            log.info { "Error" }
            throw IllegalStateException("Error of upload CSV.")
        }
        else if (alreadyExists)
            log.info { "Card set '$memoListName' already exists." }
        else if (seemsSuccess)
            log.info { "Card set '$memoListName' is uploaded." }
        else {
            Files.writeString(getProjectDirectory().resolve(".~temp-upload-cards-${System.currentTimeMillis()}.html"), uploadMemoListResponse.body())
            throw IllegalStateException("Unknown upload status.")
        }

        if (alreadyExists && rewrite) {
            deleteMemoList(memoListName)
            return uploadMemoList(memoListName, xlsxOrCsvMemoListFile, rewrite = false)
        }

        val memoListId =
            if (alreadyExists) findMemoList(memoListName)?.id
            else
                uploadMemoListResponse.uri().toString()
                    .substringAfterLast("/words/index/", "", ignoreCase = true)
                    .substringBefore('?')

        if (memoListId.isNullOrBlank())
            throw IllegalStateException("MemoList '$memoListName' is not found.")

        val memoList = downloadMemoLists().find { it.id == memoListId }
            ?: throw IllegalStateException("MemoList with ID '$memoListId' is not found.")

        return memoList
    }

    private fun deleteMemoList(memoListName: String) {

        log.info { "Removing card set '$memoListName'" }

        val memoList = findMemoListId(memoListName)
            ?: throw IllegalStateException("MemoList '$memoListName' is not found.")

        deleteMemoList(memoList)
    }

    fun deleteExistentMemoLists(memoListNamesOrIds: Iterable<String>) {

        log.info { "Removing existent MemoLists of '${memoListNamesOrIds}'" }

        val memoListNamesAndFullNamesToDelete = memoListNamesOrIds.toSet() +
                memoListNamesOrIds.map { "$it - ${memoSettings.languageProfileName}" }

        val allMemoLists = downloadMemoLists()

        val existent = allMemoLists.filter { memoListNamesAndFullNamesToDelete.containsOneOf(it.FullName, it.id) }
        log.info { "Existent MemoLists to remove ${existent.map { it.FullName + " / " + it.id }}" }

        existent.forEach {
            deleteMemoList(it)
        }
    }

    // There is no PUT, DELETE and PATCH because we do not need it now.
    enum class Method { GET, POST }

    private inline fun <reified R> doJsonRequest(
        uri: URI,
        method: Method = Method.GET,
        request: Any? = null,
        additionalHeaders: Map<String, String>? = null,
        sendNulls: Boolean = false,
        verifyLogin: Boolean = false,
    ): R = doJsonRequestImpl(
        uri, method, request, null,
        object : TypeReference<R>() {},
        additionalHeaders, sendNulls, verifyLogin,
    )

    private fun <R> doJsonRequestImpl(
        uri: URI,
        method: Method,
        request: Any?,
        @Suppress("SameParameterValue")
        responseType: Class<R>?,
        responseTypeRef: TypeReference<R>?,
        additionalHeaders: Map<String, String>? = null,
        sendNulls: Boolean = false,
        verifyLogin: Boolean = false,
        ): R {

        if (method == Method.GET)  require(request == null) { "GET cannot have request." }
        if (method == Method.POST) require(request != null) { "POST must have request."  }

        if (verifyLogin) loginIfNeeded()

        val requestObjectMapper = createDefaultObjectMapper().also {
            if (!sendNulls) it.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }
        val responseObjectMapper = createDefaultObjectMapper().also {
            it.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }

        val requestString = if (request != null) requestObjectMapper.writeValueAsString(request) else null

        log.info { "Requesting $uri" }

        val httpRequest = HttpRequest.newBuilder()
            .uri(uri)
            .header("Content-Type", "application/json; charset=utf-8")
            .header("Origin",  "https://memowordapp.com")
            .header("Referer", "https://memowordapp.com/panel/lists/index/${memoSettings.languageProfileId}")

        additionalHeaders?.forEach { (k, v) -> httpRequest.header(k, v) }

        if (method == Method.POST) {
            log.info { "Sending request $requestString" }
            httpRequest.POST(HttpRequest.BodyPublishers.ofString(requestString))
        }

        val response: HttpResponse<String> = client.send(
            httpRequest.build(),
            HttpResponse.BodyHandlers.ofString(),
        )

        val success = response.statusCode().isOneOf(200, 201)
        if (!success) {
            log.error { "Request failed with ${response.statusCode()}" }
            throw IllegalStateException("Error of executing $uri (response status ${response.statusCode()}).")
        }

        val responseStr = response.body()

        @Suppress("UNCHECKED_CAST")
        return when {
            responseType == String::class.java || responseTypeRef?.type == String::class.java
                -> responseStr as R
            responseType == Unit::class.java || responseTypeRef?.type == Unit::class.java
                -> Unit as R

            responseType != null
                -> responseObjectMapper.readValue(responseStr, responseType)
            responseTypeRef != null
                -> responseObjectMapper.readValue(responseStr, responseTypeRef)

            else -> throw IllegalStateException("responseType/responseTypeRef is missed.")
        }
    }

    private fun deleteMemoList(memoList: MemoList) {
        require(memoList.CanDelete) { "MemoList '${memoList.FullName}' / ${memoList.id} is not deletable." }

        doJsonRequest<String>(
            uri     = URI("https://memowordapp.com/Panel/Lists/RemoveList"),
            method  = Method.POST,
            request = mapOf("id" to memoList.id),
        )
    }

    private fun findMemoListId(memoListName: String): MemoList? {
        val extMemoListName = "$memoListName - ${memoSettings.languageProfileName}"
        val memoLists = downloadMemoLists()

        val memoList = memoLists.find { it.FullName.isOneOf(memoListName, extMemoListName) }
        return memoList
    }

    private fun downloadMemoLists(): List<MemoList> {
        val languageProfileId = memoSettings.languageProfileId

        val allMemoLists = doJsonRequest<List<MemoList>>(
            URI("https://memowordapp.com/panel/lists/GetMemoLists/$languageProfileId?lng=en"))
        val memoLists = allMemoLists.filter { it.LanguageProfileId == languageProfileId } // filter to make sure
        return memoLists
    }

    private fun findMemoList(memoListIdOrName: String): MemoList? {
        val memoLists = downloadMemoLists()

        val fullMemoListName = "$memoListIdOrName - ${memoSettings.languageProfileName}"

        val memoList = memoLists.find { it.id == memoListIdOrName }
            ?: memoLists.find { it.FullName == memoListIdOrName }
            ?: memoLists.find { it.FullName == fullMemoListName }

        return memoList
    }

    fun CardWordEntry.isCardValid() = this.from.isNotBlank() && this.to.isNotBlank()

    private fun filterInsertingCards(cards: List<CardWordEntry>): List<CardWordEntry> {

        val invalidCards = cards.filterNot { it.isCardValid() }
        val validCards   = cards.filter    { it.isCardValid() }

        if (invalidCards.isNotEmpty())
            log.warn { "Some inserting cards are invalid ${invalidCards.debugStr()}. They will be skipped." }

        return validCards
    }

    private fun filterUpdatingCards(entries: List<Pair<MemoCard, CardWordEntry>>): List<Pair<MemoCard, CardWordEntry>> {

        val invalidCards = entries.filterNot { it.second.isCardValid() }
        val validCards   = entries.filter    { it.second.isCardValid() }

        if (invalidCards.isNotEmpty())
            log.warn { "Some inserting cards are invalid ${invalidCards.map { it.second }.debugStr()}. They will be skipped." }

        return validCards
    }

    private fun insertNewCards(cards: List<CardWordEntry>, memoList: MemoList) {
        val validCards = filterInsertingCards(cards)
        validCards.forEach { insertNewCard(it, memoList) }
    }

    private fun insertNewCard(card: CardWordEntry, memoList: MemoList) {

        // https://memowordapp.com/Panel/Card/Create/2530c8c0-e032-45bc-9368-9bf8093c4713
        // https://memowordapp.com/Panel/Card/Save
        // content-type: application/json; charset=utf-8
        // {
        //  "MemoCardId":"",
        //  "MemoListId":"2530c8c0-e032-45bc-9368-9bf8093c4713",
        //  "MemoListIds":["2530c8c0-e032-45bc-9368-9bf8093c4713"],
        //  "TextFrom":"привет 88",
        //  "TextTo":"Hello 55",
        //  "Note":"",
        //  "MemoCardPartOfSpeechId":"20",
        //  "SelectedMemoList":"57e26534-68d7-4498-9a27-026997b5da79"
        // }
        //
        // Response
        // {"Redirect":"/Panel/Words/Index/2530c8c0-e032-45bc-9368-9bf8093c4713"}

        val request = MemoWordInsertUpdateCardRequest(
                MemoCardId  = "",
                MemoListId  = memoList.id,
                MemoListIds = listOf(memoList.id),
                TextFrom    = "",
                TextTo      = "",
                Note        = card.memoCardNote,
                MemoCardPartOfSpeechId = (card.partOfSpeech ?: guessPartOfSpeech(card.from)).asMemo.toString(),
                SelectedMemoList = null,
            )
            .withText(memoList, Language.English, card.fromInMemoWordFormat)
            .withText(memoList, Language.Russian, card.toInMemoWordFormat)

        doJsonRequest<Map<String, String>>(
            uri     = URI("https://memowordapp.com/Panel/Card/Save"),
            method  = Method.POST,
            request = request,
        )
    }

    private fun moveCardsToRecycleMemoList(cards: List<MemoCard>, fromList: MemoList): MemoList {
        val tempMemoList: MemoList = createMemoList("temp-${System.currentTimeMillis()}")
        moveCardsToMemoList(cards, fromList, tempMemoList)
        return tempMemoList
    }

    private fun createMemoList(memoListName: String): MemoList {

        // Form
        // multipart/form-data; boundary=---------------------------40482128124172102556501603362
        // "SourceType"         WebSite
        // "LanguageProfileId"  665ebd51-66cb-43d7-9ad0-ee3f0b489710
        // "LanguageFrom"       68
        // "LanguageTo"         4
        // "FullName"           test 04
        // "Author"             Cheburan
        // "Description"        notes
        //  Initial page https://memowordapp.com/Panel/Lists/Create/665ebd51-66cb-43d7-9ad0-ee3f0b489710

        log.info { "Creating card set '$memoListName'" }

        //loginIfNeeded()

        val memoLanguageProfileId = memoSettings.languageProfileId

        //val uploadMemoListFormPageResponse = client.sendGet(
        //    "https://memowordapp.com/Panel/Import/Index/$memoLanguageProfileId?lng=en")
        //
        //val formRequestVerificationToken = uploadMemoListFormPageResponse.extractFormRequestVerificationToken()

        // https://mizosoft.github.io/methanol/multipart_and_forms/#multipart-bodies
        val multipartBodyPublisher = MultipartBodyPublisher.newBuilder()
            //.textPart("__RequestVerificationToken", formRequestVerificationToken)
            .textPart("SourceType", "WebSite")
            .textPart("LanguageProfileId", memoLanguageProfileId)
            .textPart("LanguageFrom", MemoLanguage.English.id)
            .textPart("LanguageTo", MemoLanguage.Russian.id)
            .textPart("FullName", memoListName)
            .textPart("Author", memoSettings.author)
            .textPart("Description", "")
            .build()

        val createMemoListResponse: HttpResponse<String> = client.send(
            HttpRequest.newBuilder(URI("https://memowordapp.com/Panel/Lists/Create/$memoLanguageProfileId"))
                .header("Content-Type", multipartBodyPublisher.mediaType().toString())
                .header("Origin",  "https://memowordapp.com")
                .header("Referer", "https://memowordapp.com/Panel/Lists/Create/$memoLanguageProfileId")
                .POST(multipartBodyPublisher)
                .build(),
            HttpResponse.BodyHandlers.ofString(),
        )

        // !!! Upload fails with 'already exists' but you can create duplicated empty MemoList
        val alreadyExists = createMemoListResponse.body().containsOneOf("The list with this name already exists")
        // And seems error never happens
        val isError = createMemoListResponse.body().containsOneOf(
            "An error occured processing your request", // 'occured' - now HTML contains mistake :-)
            "An error occurred processing your request",
            "An error occured",
            "An error occurred",
        )

        //val seemsSuccess = containsMemoListTextParts || containsMemoListHRef || containsMemoListInput

        // ?? https://memowordapp.com/panel/words/index/66b1e2db-4c87-4fa6-8a9b-5b1b3a500642
        // ??
        // https://memowordapp.com/Panel/Lists/Create/665ebd51-66cb-43d7-9ad0-ee3f0b489710
        // https://memowordapp.com/Panel/Words/index/2530c8c0-e032-45bc-9368-9bf8093c4713

        val memoListId = createMemoListResponse.uri().toString()
            .substringAfterLast("/words/index/", "", ignoreCase = true)
            .substringBefore('?')

        if (isError) {
            log.info { "Error" }
            throw IllegalStateException("Error of upload CSV.")
        }
        else if (alreadyExists)
            log.info { "Card set '$memoListName' already exists." }
        else if (memoListId.isBlank())
            throw IllegalStateException("Error of extracting created MemoListId.")
        //else if (seemsSuccess)
        //    log.info { "Card set '$memoListName' is uploaded." }
        //else
        //    throw IllegalStateException("Unknown upload status.")

        val memoList = downloadMemoLists().find { it.id == memoListId }
            ?: throw IllegalStateException("MemoList with ID '$memoListId'.")

        return memoList
    }

    private fun removeCardsFromMemoList(cards: List<MemoCard>, memoList: MemoList) =
        cards.doIfNotEmpty {
            // https://memowordapp.com/Panel/Words/RemoveWordsFromList
            // {"MemoListId":"b7d6bed0-e1dd-47d0-8a72-180d9b9ff349","MemoCardIds":["0c78e519-fc46-4477-bca1-f1fa0d6bc638"]}
            doJsonRequest<String>(
                uri     = URI("https://memowordapp.com/Panel/Words/RemoveWordsFromList"),
                method  = Method.POST,
                request = CardsForMemoListRequest(MemoListId = memoList.id, MemoCardIds = cards.map { it.id })
            )
        }

    private fun moveCardsToMemoList(cards: List<MemoCard>, fromMemoList: MemoList, toMemoList: MemoList) =
        cards.doIfNotEmpty {
            addCardsToMemoList(cards, toMemoList)
            removeCardsFromMemoList(cards, fromMemoList)
        }

    private fun addCardsToMemoList(cards: List<MemoCard>, memoList: MemoList) = cards.doIfNotEmpty {
        // {"MemoListId":"1614e343-21ae-4379-a0a1-3c103b530e7e","MemoCardIds":["5a05c72f-fc57-4497-9812-02ea32181e6b"]}
        doJsonRequest<String>(
            uri     = URI("https://memowordapp.com/Panel/Words/MoveWordsToList"),
            method  = Method.POST,
            request = CardsForMemoListRequest(MemoListId = memoList.id, MemoCardIds = cards.map { it.id }.distinct())
        )
    }

    internal fun saveMemoList(memoListName: String, file: Path) {
        val cards = loadWordCards(file)
        saveMemoList(memoListName, cards)
    }

    private fun saveMemoList(memoListName: String, cards: List<CardWordEntry>) {

        log.info { "saveMemoList '$memoListName' (${cards.size})" }

        val allPossibleFromsOfSavingCards = cards
            .flatMap { card -> card.possibleMemoFroms() }
            .toSet()

        val existentMemoList = findMemoList(memoListName)

        val allMemoCards: List<MemoCard> = downloadAllMemoCards()
        log.info { "allMemoCards: ${allMemoCards.size}" }

        val currentMemoCards = if (existentMemoList == null) emptyList()
                               else downloadMemoCards(existentMemoList.asMemoListEntry)
        log.info { "currentMemoCards: ${currentMemoCards.debugMStr()}" }

        val currentMemoCardsMap = currentMemoCards.associateBy { it.text(Language.English) }

        val toRemoveFromMemoList: List<MemoCard> = currentMemoCards.filterNot { it.text(Language.English) in allPossibleFromsOfSavingCards }
        log.info { "toRemoveFromMemoList: ${toRemoveFromMemoList.debugMStr()}" }

        val memoWordFroms = allMemoCards.map { it.text(Language.English) }.toSet()
        val toInsertNewToMemoList: List<CardWordEntry> = cards
            .filterNot { currentMemoCardsMap.containsOneOfKeys(it.possibleMemoFroms())  }
            .filterNot {
                // T O D O: avoid calling possibleMemoFroms(), try to reuse cardsMap
                memoWordFroms.containsOneOf(it.possibleMemoFroms())
            }
            .distinctBy { it.from }
        log.info { "toInsertNewToMemoList: ${toInsertNewToMemoList.debugStr()}" }

        val existentMemoCardEntries = findExistentMemoCardsFor(cards, allMemoCards)
        log.info { "existentMemoCardEntries: ${existentMemoCardEntries.debugPStr()}" }

        val toAddExistentToMemoList: List<MemoCard> = allMemoCards
            .filter { it.text(Language.English) in allPossibleFromsOfSavingCards }
            .distinctBy { it.text(Language.English) }
            .filter { existentMemoList == null || !it.belongsToMemoList(existentMemoList) }
        log.info { "toAddExistentToMemoList: ${toAddExistentToMemoList.debugMStr()}" }

        var toUpdate = toUpdateChangedCards(existentMemoCardEntries)
        log.info { "toUpdate: ${toUpdate.debugPStr()}" }

        var tempListId: MemoList? = null

        val memoList: MemoList = if (existentMemoList == null) {
            if (toInsertNewToMemoList.isNotEmpty())
                uploadMemoList(memoListName, toInsertNewToMemoList, rewrite = false)
            else
                createMemoList(memoListName)

            findMemoList(memoListName)
                ?: throw IllegalStateException("Created MemoList [$memoListName] is not found.")
        }
        else {
            if (toRemoveFromMemoList.isNotEmpty())
                tempListId = moveCardsToRecycleMemoList(toRemoveFromMemoList, existentMemoList)

            insertNewCards(toInsertNewToMemoList, existentMemoList)
            existentMemoList
        }

        addCardsToMemoList(toAddExistentToMemoList, memoList)

        val addedCardIds = toAddExistentToMemoList.map { it.id }.toSet()

        toUpdate = toUpdate.map { p ->
            val memoCard = p.first
            val intCard = p.second

            if (memoCard.id in addedCardIds)
                Pair(memoCard.copy(OthersLists = memoCard.otherLists.addMemoList(memoList)), intCard)
            else
                p
        }


        doUpdateMemoCards(toUpdate)

        if (tempListId != null)
            tempMemoLists.add(tempListId)
    }

    fun deleteTempMemoLists() {
        val memoLists = tempMemoLists.toTypedArray()
        deleteExistentMemoLists(memoLists.map { it.id })
        this.tempMemoLists.removeAll(memoLists.toList())
    }

    private fun findExistentMemoCardsFor(
        cards: List<CardWordEntry>,
        allMemoCards: List<MemoCard>,
        ): List<Pair<MemoCard, CardWordEntry>> {

        val duplicatedFrom = cards.groupBy { it.from }
            .entries.filter { it.value.size > 1 }
            .map { it.key }

        if (duplicatedFrom.isNotEmpty())
            throw IllegalArgumentException(
                "Impossible to synchronize/update MemoWord cards. There duplicates $duplicatedFrom. Please remove/merge them.")

        val cardsMap = cards
            .flatMap { card -> card.possibleMemoFroms().map { Pair(it, card) } }
            .associate { it }

        val allMemoCardsMap: Map<String, MemoCard> = allMemoCards.associateBy { it.text(Language.English) }

        val existentMemoCards = cardsMap
            .map { (from, card) ->
                val existentMemoCard = allMemoCardsMap[from]
                if (existentMemoCard == null) null else Pair(existentMemoCard, card)
            }
            .filterNotNull()

        val notExistent = (cards - existentMemoCards.map { it.second }.toSet()).sortedBy { it.from }
        log.info { "notExistent  ${notExistent.debugStr()}" }

        return existentMemoCards
    }

    // return updated ones
    private fun toUpdateChangedCards(cards: List<Pair<MemoCard, CardWordEntry>>) = cards
        .mapNotNull { entry ->
            val (memoCard, card) = entry

            val memoCardIsUpdatedAt = memoCard.lastUpdatedAt
            requireNotNull(memoCardIsUpdatedAt) { "MemoCard ${memoCard.id} has no updatedAt info." }

            val cardIsUpdatedAt = card.lastUpdatedAt
            val toUpdate: Boolean =
                if (cardIsUpdatedAt != null)
                    card.from.isNotBlank() && (cardIsUpdatedAt.toInstant() > memoCardIsUpdatedAt)
                else
                    card.isBetterThan(memoCard)

            if (toUpdate) entry else null
        }

    private fun doUpdateMemoCards(memoCards: List<Pair<MemoCard, CardWordEntry>>) {

        val validCards = filterUpdatingCards(memoCards)

        val changedMemoCards = validCards.map { (memoCard, intCard) ->
            memoCard.updateFrom(intCard)
        }

        loginIfNeeded()

        changedMemoCards.forEach {
            doUpdateMemoCard(it)
        }
    }


    private fun doUpdateMemoCard(card: MemoCard) {

        val updateRequestObj = MemoWordInsertUpdateCardRequest(
            MemoCardId  = card.id,
            MemoListId  = card.otherLists.first().id,
            MemoListIds = card.otherLists.map { it.id },
            TextFrom    = card.TextFrom!!,
            TextTo      = card.TextTo!!,
            Note        = card.Note!!,
            MemoCardPartOfSpeechId = card.PartOfSpeechId!!.toString(),
            SelectedMemoList = null,
        )

        log.info { "doUpdateMemoCard request: $updateRequestObj" }

        // Possible response: {"Redirect":"/Panel/Words/Index/b7d6bed0-e1dd-47d0-8a72-180d9b9ff349"}

        val resp = doJsonRequest<Map<String, Any>>(
            uri     = URI("https://memowordapp.com/Panel/Card/Save"),
            method  = Method.POST,
            request = updateRequestObj,
            additionalHeaders = mapOf("X-Requested-With" to "XMLHttpRequest")
        )

        println("### Response: $resp")
    }

    private fun downloadAllMemoCards(): List<MemoCard> {
        loginIfNeeded()
        return downloadMemoCards(allCardMemoList)
    }

    /*
    private fun downloadMemoCards(memoListIdOrName: String): List<MemoCard> {
        val memoList = findMemoList(memoListIdOrName)
            ?: throw IllegalStateException("MemoList [$memoListIdOrName] is not found.")
        return downloadMemoCardsByMemoListId(memoList.asMemoListEntry)
    }
    */

    private fun downloadMemoCards(memoList: MemoListEntry): List<MemoCard> {
        loginIfNeeded()

        // !!! offset & limit do NOT work at all !!!
        // https://memowordapp.com/panel/words/GetMemoWords/b7760ca3-56e9-44e7-8047-6427fc22b4ac

        val allCards = doJsonRequest<List<MemoCard>>(
            URI("https://memowordapp.com/panel/words/GetMemoWords/${memoList.id}"))

        return allCards.map { it.copy(OthersLists = it.otherLists + memoList) }
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
                .header("Origin",     "https://memowordapp.com")
                .header("Referer",    "https://memowordapp.com/Account/Login?lng=en")
                .header("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:124.0) Gecko/20100101 Firefox/124.0")
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


// TODO: write tests
private fun CardWordEntry.isBetterThan(memoCard: MemoCard): Boolean {
    val translation1 = this.to
    val translation2 = memoCard.text(Language.Russian)

    return when {
        translation1.isNotBlank() && translation2.isBlank()    -> true
        translation1.isBlank()    && translation2.isNotBlank() -> false
        else -> this.isTranslationSenseOrFormatBetter(memoCard)
    }
}

// TODO: write tests
internal fun CardWordEntry.isTranslationSenseOrFormatBetter(memoCard: MemoCard): Boolean {
    val origTranslation1 = this.to
    val origTranslation2 = memoCard.text(Language.Russian)
    if (origTranslation1.isSenseBetter(origTranslation2) == CompareSenseResult.Better)
        return true

    val optimizedTranslation1 = optimizeToForMemoWord(origTranslation1)
    val lineCount1 = optimizedTranslation1.nonEmptyLineCount()
    val lineCount2 = origTranslation2.nonEmptyLineCount()

    when {
        lineCount1 > lineCount2 -> CompareSenseResult.Better
        lineCount1 < lineCount2 -> CompareSenseResult.Worse
        else -> CompareSenseResult.AlmostSame
    }

    return lineCount1 > lineCount2
}

// TODO: write tests
private fun CardWordEntry.possibleMemoFroms(): Collection<String> {
    val from = this.from
    val memoWordFrom = optimizeFromForMemoWord(from)
    val csvMemoWordFrom = formatWordOrPhraseToCsvMemoWordFormat(memoWordFrom)

    return aFewValues(from, memoWordFrom, csvMemoWordFrom)
}

// small optimization
private fun aFewValues(v1: String, v2: String, v3: String): Collection<String> = when {
    v1 == v2 && v2 == v3 -> Collections.singleton(v1)
    v2 == v3 -> listOf(v1, v2)
    else -> setOf(v1, v2, v3)
}

sealed interface DataSource {
    val mediaType: MediaType
}

private val CsvMediaType = MediaType.parse("text/csv")
private val XlsxMediaType = MediaType.parse("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")

class FileSource(val file: Path, private val fileMediaType: MediaType? = null) : DataSource {
    override fun toString(): String = "DataSource { $file }"
    override val mediaType: MediaType get() = fileMediaType ?: when (file.extension) {
        "csv"  -> CsvMediaType
        "xlsx" -> XlsxMediaType
        else   -> throw IllegalArgumentException("Error of determine MediaType for $file.")
    }
}
class BytesSource(val bytes: ByteArray, override val mediaType: MediaType) : DataSource {
    override fun toString(): String = "DataSource { ${bytes.size}, $mediaType }"
}


fun MultipartBodyPublisher.Builder.filePart(name: String, bytes: ByteArray, filename: String, mediaType: MediaType): MultipartBodyPublisher.Builder {
    val publisher = MoreBodyPublishers.ofMediaType(HttpRequest.BodyPublishers.ofByteArray(bytes), mediaType)
    return this.formPart(name, filename, publisher)
}


//private fun String.debugStr(): String = this.safeSubstring(0, 15)
private fun String.debugStr(): String = this
private fun <T> List<T>.safeSubList(fromIndex: Int, toIndex: Int) =
    if (toIndex < this.size) this.subList(fromIndex, toIndex) else this

private fun List<CardWordEntry>.debugStr(): String {
    val s = StringBuilder()
    s.append(this.size).append("\n")
    //this.forEach { s.append("    ").append(it.from.debugStr()).append("\n") }
    this.safeSubList(0, 2).forEach { s.append("    ").append(it.from.debugStr()).append("\n") }
    s.append("    ...\n")
    return s.toString()
}
private fun List<MemoCard>.debugMStr(): String {
    val s = StringBuilder()
    s.append(this.size).append("\n")
    //this.forEach { s.append("    ").append(it.text(Language.English).debugStr()).append("\n") }
    this.safeSubList(0, 2).forEach { s.append("    ").append(it.text(Language.English).debugStr()).append("\n") }
    s.append("    ...\n")
    return s.toString()
}
private fun List<Pair<MemoCard, CardWordEntry>>.debugPStr(): String =
    this.map { it.second }.debugStr()


fun main() {
    MemoWordSession().use {
        it.connect()
        //it.uploadMemoList(
        //    "army3",
        //    Path.of("/home/vmelnykov/english/words/grouped/army-RuEn-MemoWord.csv"),
        //    rewrite = true
        //)
        //it.updateExistentCards(listOf(
        //    cardWordEntry {
        //        from = "hello"
        //        to = "привет пока"
        //        updatedAt = ZonedDateTime.now()
        //    }
        //))

        //it.saveMemoList(
        //    "army3",
        //    loadWordCards(Path.of("/home/vmelnykov/english/words/grouped/army.csv")),
        //)

        //it.deleteMemoList(MemoList.byId("temp", "temp"))
        it.deleteExistentMemoLists(listOf("temp"))

        //it.uploadMemoList(
        //    "army3",
        //    Path.of("/home/vmelnykov/english/words/grouped/_MemoWord/army/army - RuEn-MemoWord.xlsx"),
        //    rewrite = true
        //)
        //it.uploadMemoList(
        //    "army3",
        //    BytesSource(
        //        Files.readAllBytes(Path.of("/home/vmelnykov/english/words/grouped/_MemoWord/army/army - RuEn-MemoWord.xlsx")),
        //        XlsxMediaType),
        //    rewrite = true
        //)
        //it.uploadMemoList(
        //    "army3",
        //    listOf(
        //        cardWordEntry {
        //            from = "hello"
        //            to = "привет пока"
        //            updatedAt = ZonedDateTime.now()
        //        }
        //    ),
        //    rewrite = true
        //)
        //it.uploadMemoList(
        //    "army3",
        //    loadWordCards(Path.of("/home/vmelnykov/english/words/grouped/army.csv")),
        //    rewrite = true
        //)
    }
}
