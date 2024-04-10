package com.mvv.gui.memoword

import com.fasterxml.jackson.annotation.JsonProperty
import com.mvv.gui.util.isOneOf
import com.mvv.gui.util.removeCharPrefixesRepeatably
import com.mvv.gui.util.removeCharSuffixesRepeatably
import com.mvv.gui.words.*
import java.time.Instant
import java.time.ZonedDateTime


/*
{
  "MemoCardId": "219ebaee-e036-49d7-832d-761dcb33c720",
  "LanguageFromId": 68,
  "LanguageFrom": "русский",
  "LanguageToId": 4,
  "LanguageTo": "английский",
  "PartOfSpeechId": 7,
  "PartOfSpeech": "Фр",
  "SourceTypeId": 552,
  "SourceType": "Imported Excel",
  "TranslationServiceId": 601,
  "TranslationService": "Google Translation API",
  "TextFrom": "_посл. лучше хоть что-нибудь, чем ничего",
  "TextTo": "half a loaf is better than no bread",
  "Note": "",
  "IsActive": true,
  "InsertDate": "\/Date(1711155077894)\/",
  "UpdateDate": null,
  "OthersLists":[
    {"Id":"685e3f2b-00f8-4484-ab97-a216c589e5ed","Name":"I know"},
    {"Id":"f5a057f9-6239-488c-82dc-418d76c460b8","Name":"Repeat"},
    {"Id":"358ab8d6-9732-4dd8-9d23-ca4583c14a2a","Name":"Equiod p01_01 - Ru-En"}
    ],
  "OrderNumber": 1
}
*/
@Suppress("PropertyName")
data class MemoCard (
    val MemoCardId: String = "", // "219ebaee-e036-49d7-832d-761dcb33c720",
    val LanguageFromId: Long?, // 68,
    val LanguageFrom: String?, // "русский",
    val LanguageToId: Long?,   // 4,
    val LanguageTo: String?,   // "английский",
    val PartOfSpeechId: Int?,  // 7,
    val PartOfSpeech: String?, // "Фр",
    val SourceTypeId: Long?,   // 552,
    val SourceType: String?,   // "Imported Excel",
    val TranslationServiceId: Long?, // 601,
    val TranslationService: String?, // "Google Translation API",
    val TextFrom: String?,     // "(посл.) лучше хоть что-нибудь, чем ничего",
    val TextTo: String?,       // "half a loaf is better than no bread",
    val Note: String?,         // "",
    val IsActive: Boolean?,    // true,
    val InsertDate: String?,   // "\/Date(1711155077894)\/",
    val UpdateDate: String?,   // null,
    val OthersLists: List<MemoListEntry>?, //[{ "Id": "b7760ca3-56e9-44e7-8047-6427fc22b4ac", "Name": "All cards" }],
    val OrderNumber: Int?      // 1
)

@Suppress("PropertyName")
data class MemoListEntry (
    val Id: String,
    val Name: String?,
)

@Suppress("NOTHING_TO_INLINE")
inline fun MemoCard.belongsToMemoList(memoList: MemoList): Boolean =
    this.belongsToMemoListById(memoList.id)
fun MemoCard.belongsToMemoListById(memoListId: String): Boolean =
    this.otherLists.any { it.id == memoListId }

fun List<MemoListEntry>?.addMemoList(memoList: MemoList): List<MemoListEntry> =
    ((this ?: emptyList()) + memoList.asMemoListEntry).distinctBy { it.id }

val MemoList.asMemoListEntry: MemoListEntry get() =
    MemoListEntry(this.id, this.FullName)


@Suppress("PropertyName")
internal data class MemoWordInsertUpdateCardRequest (
    @get:JsonProperty("MemoCardId")
    val MemoCardId: String,        // "0c78e519-fc46-4477-bca1-f1fa0d6bc638",
    @get:JsonProperty("MemoListId")
    val MemoListId: String,        // "b7d6bed0-e1dd-47d0-8a72-180d9b9ff349",
    @get:JsonProperty("MemoListIds")
    val MemoListIds: List<String>, // ["b7d6bed0-e1dd-47d0-8a72-180d9b9ff349","b7760ca3-56e9-44e7-8047-6427fc22b4ac"],
    @get:JsonProperty("MemoCardPartOfSpeechId")
    val MemoCardPartOfSpeechId: String, // "7",
    @get:JsonProperty("Note")
    val Note: String,
    @get:JsonProperty("TextFrom")
    val TextFrom: String,          // "привет 55",
    @get:JsonProperty("TextTo")
    val TextTo: String,            // "hello",
    @get:JsonProperty("SelectedMemoList")
    val SelectedMemoList: String?, //"771d766d-96db-46d0-9db8-048330e054c6"
)


@Suppress("PropertyName")
internal data class CardsForMemoListRequest (
    @get:JsonProperty("MemoListId")
    val MemoListId: String,
    @get:JsonProperty("MemoCardIds")
    val MemoCardIds: List<String>,
)


inline val MemoCard.id: String get() = this.MemoCardId
inline val MemoCard.otherLists: List<MemoListEntry> get() = this.OthersLists ?: emptyList()
inline val MemoList.id: String get() = this.MemoListId
inline val MemoListEntry.id: String get() = this.Id

val MemoCard.shortDescr: String get() = "${this.id} [${this.textOrNull(Language.English)}]"
val MemoList.shortDescr: String get() = "${this.id} [${this.FullName}]"

// T O D O: refactor to remove duplicating
fun MemoCard.text(language: Language): String =
    textOrNull(language) ?: throw IllegalStateException("MemoCard '${this.shortDescr}' has no relation to $language.")

enum class MemoLanguage (val id: Long, val names: List<String>) {
    English(4, listOf("английский", "english")),
    Russian(68, listOf("русский", "russian")),
}

fun Language.toMemo(): MemoLanguage = when (this) {
    Language.English -> MemoLanguage.English
    Language.Russian -> MemoLanguage.Russian
}

fun MemoCard.textOrNull(language: Language): String? {
    val memoLang = language.toMemo()
    return when {
        this.LanguageToId   == memoLang.id -> this.TextTo   ?: ""
        this.LanguageFromId == memoLang.id -> this.TextFrom ?: ""
        this.LanguageTo  .isOneOf(memoLang.names, ignoreCase = true) -> this.TextTo   ?: ""
        this.LanguageFrom.isOneOf(memoLang.names, ignoreCase = true) -> this.TextFrom ?: ""
        else -> null
    }
}

fun MemoCard.withText(language: Language, newValue: String): MemoCard {
    val memoLang = language.toMemo()
    return when {
        this.LanguageToId   == memoLang.id -> this.copy(TextTo = newValue)
        this.LanguageFromId == memoLang.id -> this.copy(TextFrom = newValue)
        this.LanguageTo  .isOneOf(memoLang.names, ignoreCase = true) -> this.copy(TextTo = newValue)
        this.LanguageFrom.isOneOf(memoLang.names, ignoreCase = true) -> this.copy(TextFrom = newValue)
        else -> throw IllegalStateException("MemoCard '${this.shortDescr}' has no relation to $language.")
    }
}

internal fun MemoWordInsertUpdateCardRequest.withText(memoList: MemoList, language: Language, newValue: String): MemoWordInsertUpdateCardRequest {
    val memoLang = language.toMemo()
    return when {
        memoList.LanguageToId   == memoLang.id -> this.copy(TextTo = newValue)
        memoList.LanguageFromId == memoLang.id -> this.copy(TextFrom = newValue)
        memoList.LanguageTo  .isOneOf(memoLang.names, ignoreCase = true) -> this.copy(TextTo = newValue)
        memoList.LanguageFrom.isOneOf(memoLang.names, ignoreCase = true) -> this.copy(TextFrom = newValue)
        else -> throw IllegalStateException("MemoCard '${memoList.shortDescr}' has no relation to $language.")
    }
}

val MemoCard.lastUpdatedAt: Instant? get() =
    (this.UpdateDate ?: this.InsertDate) ?.parseMemoDate()


// T O D O: I'm not sure that ID/name are constants !!
enum class MemoListType (val id: Long, val fullName: String) {
    AllCards(751, "All cards"),
    // All cards:
    //     CardTypeId = 751, LearnTypeId = 701 (Учу), SourceTypeId = 555 (Служебные сеты)
    // I know:
    //     CardTypeId = 752 (Уже знаю)
    // Difficult:
    //     CardTypeId = 753 (Сложно)
    // Repeat:
    //     CardTypeId = 754, LearnTypeId = 701 (Учу), SourceTypeId = 555 (Служебные сеты)
    //
    // My words ???
    //     ... CardTypeId = null
}



/*
  'All cards' MemoList
  {
    "MemoListId": "b7760ca3-56e9-44e7-8047-6427fc22b4ac",
    "LanguageProfileId": "665ebd51-66cb-43d7-9ad0-ee3f0b489710",
    "LanguageProfile": "Ru-En",
    "LanguageFromId": 68,
    "LanguageFrom": "русский",
    "LanguageToId": 4,
    "LanguageTo": "английский",
    "CardTypeId": 751,
    "CardType": "Все карточки",
    "LearnTypeId": 701,
    "LearnType": "Учу",
    "SourceTypeId": 555,
    "SourceType": "Служебные сеты",
    "ProductId": null,
    "IsPaid": false,
    "FullName": "All cards",
    "Note": "Cards from all sets. Use the search box",
    "IsPublic": false,
    "IsDefault": false,
    "IsActive": true,
    "InsertDate": "\/Date(1691696098982)\/",
    "UpdateDate": "\/Date(1711744488489)\/",
    "Qty": 5425,
    "AccessEmails": null,
    "Author": "Cheburan",
    "ListType": "Служебный",
    "OriginalListId": null,
    "OriginalListProductId": null,
    "AuthorId": 422205,
    "PartnerId": null,
    "CanEdit": false,
    "Courses": "",
    "CanDelete": false
  }
*/
@Suppress("PropertyName")
data class MemoList (
    val MemoListId: String,        // "57e26534-68d7-4498-9a27-026997b5da79"
    val LanguageProfileId: String, // "665ebd51-66cb-43d7-9ad0-ee3f0b489710"
    val LanguageProfile: String?,  // "Ru-En"

    val FullName: String?,  // "My words"    "army2 - Ru-En"
    val Note: String?,      // "Your first set for the cards you created"
    val Author: String?,    // "Cheburan"
    val ListType: String?,  // "Служебный"

    val IsActive: Boolean,     // true
    val CanDelete: Boolean,
    val CanEdit: Boolean,      // false

    val LanguageFromId: Long?, // 68
    val LanguageFrom: String?, // "русский"
    val LanguageToId: Long?,   // 4
    val LanguageTo: String?,   // "английский"

    val CardTypeId: Long?,     // null
    val CardType: String?,     // null
    val LearnTypeId: Long?,    // 701
    val LearnType: String?,    // "Учу"
    val SourceTypeId: Long?,   // 555
    val SourceType: String?,   // "Служебные сеты"
    //val ProductId: Any?,     // null
    //val IsPaid: Boolean?,    // false
    //val IsPublic: Boolean?,  // false
    //val IsDefault: Boolean?, // true
    //val InsertDate: String?, // "/Date(1691696098994)/"
    //val UpdateDate: String?, // "/Date(1694104076757)/"
    val Qty: Long?,            // 3
    //val AccessEmails: Any?,    // null
    //val OriginalListId: Any?,  // null
    //val OriginalListProductId: Any?, // null
    //val AuthorId: Long?,       // 422205
    //val PartnerId: Any?,       // null
    //val Courses: String?,      // ""
) {
    companion object {
        /*
        // Temporary, only for debugging/testing
        @Suppress("BooleanLiteralArgument")
        @Deprecated("Temporary, only for debugging/testing", replaceWith = ReplaceWith(""))
        fun byId(id: String, name: String): MemoList = MemoList(
            MemoListId = id,
            settings.memoSettings!!.languageProfileId, null, name, null, null, null,
            true, true, true, null, null, null,
            null, null, null, null, null, null,
            null, null,
        )
        */
    }
}


@Suppress("unused")
enum class MemoWordPartOfSpeech (val id: Int) {
    Noun(1),          // <option value="1">Существительное</option>
    Adjective(2),     // <option value="2">Прилагательное</option>
    Verb(3),          // <option value="3">Глагол</option>
    Pronoun(4),       // <option value="4">Местоимение</option>
    Adverb(5),        // <option value="5">Наречие</option>
    Union(6),         // <option value="6">Союз</option>
    Phrase(7),        // <option selected="selected" value="7">Фраза</option>
    Word(8),          // <option value="8">Слово</option>
    Numeral(9),       // ??? <option value="9">Числительное</option>
    Particle(10),     // <option value="10">Частица</option>
    Preposition(11),  // <option value="11">Предлог</option>
    Interjection(12), // <option value="12">Междометие</option>
    PhrasalVerb(13),  // <option value="13">Фразовый глагол</option>

    AdditionalPartOfSpeech1(14), // <option value="14">Доп.часть речи 1</option>
    AdditionalPartOfSpeech2(15), // <option value="15">Доп.часть речи 2</option>
    AdditionalPartOfSpeech3(16), // <option value="16">Доп.часть речи 3</option>

    MasculineGender(17), // <option value="17">Мужской род</option>
    FeminineGender(18),  // <option value="18">Женский род</option>
    NeuterGender(19),    // <option value="19">Средний род</option>

    SetExpression(20),   // <option value="20">Уст.словосочетание</option>
}

val PartOfSpeech.asMemo: MemoWordPartOfSpeech get() = when (this) {
    PartOfSpeech.Noun          -> MemoWordPartOfSpeech.Noun
    PartOfSpeech.Adjective     -> MemoWordPartOfSpeech.Adjective

    PartOfSpeech.Verb          -> MemoWordPartOfSpeech.Verb
    PartOfSpeech.AuxiliaryVerb -> MemoWordPartOfSpeech.Verb
    PartOfSpeech.ModalVerb     -> MemoWordPartOfSpeech.Verb
    PartOfSpeech.PhrasalVerb   -> MemoWordPartOfSpeech.Verb

    PartOfSpeech.Pronoun       -> MemoWordPartOfSpeech.Pronoun
    PartOfSpeech.Adverb        -> MemoWordPartOfSpeech.Adverb
    PartOfSpeech.Union         -> MemoWordPartOfSpeech.Union
    PartOfSpeech.Numeral       -> MemoWordPartOfSpeech.Numeral
    PartOfSpeech.Article       -> MemoWordPartOfSpeech.Noun
    PartOfSpeech.Particle      -> MemoWordPartOfSpeech.Particle
    PartOfSpeech.Preposition   -> MemoWordPartOfSpeech.Preposition
    PartOfSpeech.PluralPreposition -> MemoWordPartOfSpeech.Word
    PartOfSpeech.Interjection  -> MemoWordPartOfSpeech.Interjection

    PartOfSpeech.Word          -> MemoWordPartOfSpeech.Word
    PartOfSpeech.Phrase        -> MemoWordPartOfSpeech.Phrase
    PartOfSpeech.PrepPhrase    -> MemoWordPartOfSpeech.Phrase

    PartOfSpeech.Exclamation   -> MemoWordPartOfSpeech.Phrase
    PartOfSpeech.SetExpression -> MemoWordPartOfSpeech.SetExpression

    //PartOfSpeech.Singular    -> MemoWordPartOfSpeech.Word
    //PartOfSpeech.Plural      -> MemoWordPartOfSpeech.Word

    PartOfSpeech.Conjunction   -> MemoWordPartOfSpeech.Word
    PartOfSpeech.Determiner    -> MemoWordPartOfSpeech.Word
    PartOfSpeech.Abbreviation  -> MemoWordPartOfSpeech.Word
}


// Format example: "\/Date(1711155077894)\/"
internal fun String.parseMemoDate(): Instant? {
    val strTimeStamp = this
        .removeCharPrefixesRepeatably("/\\")
        .removeCharSuffixesRepeatably("/\\")
        .trim()
        .removePrefix("Date")
        .trim()
        .removePrefix("(").removeSuffix(")")
        .trim()

    return Instant.ofEpochMilli(strTimeStamp.toString().toLong())
}

internal fun Instant.toMemoDate(): String =
    "/Date(${this.toEpochMilli()})/"
internal fun ZonedDateTime.toMemoDate(): String = this.toInstant().toMemoDate()

fun CardWordEntry.asSinglePartOfSpeech(): PartOfSpeech {
    val card = this
    val partsOfSpeech = card.partsOfSpeech

    val partOfSpeech: PartOfSpeech = when {
        partsOfSpeech == null ->
            guessPartOfSpeech(card)
        partsOfSpeech.size == 1 ->
            partsOfSpeech.first()
        card.fromWordCount == 1 ->
            PartOfSpeech.Word
        else ->
            PartOfSpeech.Phrase
    }
    return partOfSpeech
}

fun MemoCard.updateFrom(card: CardWordEntry) = this.update(
        Language.English, card.fromInMemoWordFormat,
        Language.Russian, card.toInMemoWordFormat,
        card.memoCardNote,
        card.updatedAt?.toInstant(),
        card.asSinglePartOfSpeech(),
    )

internal fun MemoCard.update(
    fromLanguage: Language, from: String,
    toLanguage: Language, to: String,
    note: String,
    updatedAt: Instant?,
    partOfSpeech: PartOfSpeech?,
    ): MemoCard =
    this.withText(fromLanguage, from)
        .withText(toLanguage, to)
        .copy(
            PartOfSpeechId = partOfSpeech?.asMemo?.id,
            PartOfSpeech =   partOfSpeech?.asMemo?.toString(),
            Note = note,
            UpdateDate = updatedAt?.toMemoDate(),
        )


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


Get MemoLists for specific card
https://memowordapp.com/panel/card/GetMemoCardLists/c107b2df-3abf-412e-804b-bc1ef49f3731?order=asc


RemoveWordsFromList
https://memowordapp.com/Panel/Words/RemoveWordsFromList
{"MemoListId":"b7d6bed0-e1dd-47d0-8a72-180d9b9ff349","MemoCardIds":["0c78e519-fc46-4477-bca1-f1fa0d6bc638"]}


MoveWordsToList (really it does NOT move, it adds)
https://memowordapp.com/Panel/Words/MoveWordsToList
{"MemoListId":"1614e343-21ae-4379-a0a1-3c103b530e7e","MemoCardIds":["5a05c72f-fc57-4497-9812-02ea32181e6b"]}


Save request
{
"MemoCardId":"2f733280-5499-4826-ba48-380ffa762c52",
"MemoListId":"4d47a30c-ed43-4566-8ce3-fa0d2c62ac31",
"MemoListIds": [
  "72051d52-e048-4dc1-9895-2ae0a436048c",
  "4d47a30c-ed43-4566-8ce3-fa0d2c62ac31",
  "b7760ca3-56e9-44e7-8047-6427fc22b4ac"
  ],
"MemoCardPartOfSpeechId":"7",
"Note":"",
"TextFrom":"?@825B 457",
"TextTo":"hello",
"SelectedMemoList":"57e26534-68d7-4498-9a27-026997b5da79"  ???
}
*/
