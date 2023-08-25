package com.mvv.gui

import com.mvv.gui.WordCardStatus.BaseWordDoesNotExist
import com.mvv.gui.WordCardStatus.NoBaseWordInSet
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.paint.Color


class CardWordEntry {
    val fromProperty = SimpleStringProperty(this, "from", "")
    val toProperty = SimpleStringProperty(this, "to", "")
    val transcriptionProperty = SimpleStringProperty(this, "transcription", "")
    val translationCountProperty = AroundReadOnlyIntegerProperty<String>(this, "translationCount", toProperty) {
        to -> to?.translationCount ?: 0 }
    val examplesProperty = SimpleStringProperty(this, "examples", "")
    val wordCardStatusesProperty = SimpleObjectProperty<Set<WordCardStatus>>(this, "wordCardStatuses", emptySet())

    var from: String
        get() = fromProperty.get()
        set(value) = fromProperty.set(value)
    var to: String
        get() = toProperty.get()
        set(value) = toProperty.set(value)
    var transcription: String
        get() = transcriptionProperty.get()
        set(value) = transcriptionProperty.set(value)
    var examples: String
        get() = examplesProperty.get()
        set(value) = examplesProperty.set(value)

    @Suppress("MemberVisibilityCanBePrivate")
    var translationCount: Int = 0
        private set

    var wordCardStatuses: Set<WordCardStatus>
        get() = wordCardStatusesProperty.get()
        set(value) {
            wordCardStatusesProperty.set(value)
        }

    constructor(from: String, to: String) {
        toProperty.addListener { _, _, newValue -> translationCount = newValue?.translationCount ?: 0 }

        this.from = from
        this.to = to
    }

    override fun toString(): String {
        return "CardWordEntry(from='$from', to='${to.take(20)}...', wordCardStatuses=$wordCardStatuses, translationCount=$translationCount, transcription='$transcription', examples='${examples.take(10)}...')"
    }
}


val cardWordEntryComparator: Comparator<CardWordEntry> = Comparator.comparing({ it.from }, String.CASE_INSENSITIVE_ORDER)



enum class TranslationCountStatus(val color: Color) {
    Ok(Color.TRANSPARENT),
    NotBad(Color.valueOf("#fffac5")),
    Warn(Color.valueOf("#ffdcc0")),
    ToMany(Color.valueOf("#ffbbbb")),
    ;

    val cssClass: String get() = "TranslationCountStatus-${this.name}"

    companion object {
        val allCssClasses = TranslationCountStatus.values().map { it.cssClass }
    }
}

val Int.toTranslationCountStatus: TranslationCountStatus get() = when (this) {
    in 0..3 -> TranslationCountStatus.Ok
    in 4..5 -> TranslationCountStatus.NotBad
    in 6..7 -> TranslationCountStatus.Warn
    else -> TranslationCountStatus.ToMany
}


enum class WordCardStatus (
    val toolTipF: (CardWordEntry)->String,
    ) {

    Ok({""}),

    /**
     * If current word has ending/suffix 'ed', 'ing', 'es', 's' does not have
     * but the whole set does not have base word without such ending/suffix.
     *
     * It is not comfortable to learn such word if you do not know base word.
     */
    NoBaseWordInSet({
        "Words set does not have base word ${possibleEnglishBaseWords(it.from).joinToString("|")}.\n" +
        "It is advised to add card for this word." }),

    /**
     * Marker to stop validation on NoBaseWordInSet.
     */
    BaseWordDoesNotExist({""}),
    ;

    val cssClass: String get() = "WordCardStatus-${this.name}"

    companion object {
        val allCssClasses = WordCardStatus.values().map { it.cssClass }
    }
}


val CardWordEntry.noBaseWordInSet: Boolean get() = this.wordCardStatuses.contains(NoBaseWordInSet)
val CardWordEntry.ignoreNoBaseWordInSet: Boolean get() = this.wordCardStatuses.contains(BaseWordDoesNotExist)


fun analyzeWordCards(allWordCards: Iterable<CardWordEntry>) = analyzeWordCards(allWordCards, allWordCards)

fun analyzeWordCards(wordCardsToVerify: Iterable<CardWordEntry>, allWordCards: Iterable<CardWordEntry>) {

    println("### analyzeWordCards") // TODO: use logger

    val allWordCardsMap: Map<String, CardWordEntry> = allWordCards.associateBy { it.from.trim().lowercase() }

    wordCardsToVerify.forEach { card ->
        val englishWord = card.from.trim().lowercase()

        if (!card.ignoreNoBaseWordInSet && englishWord.mayBeDerivedWord) {

            val baseWords = possibleEnglishBaseWords(englishWord)
            val cardsSetContainsBaseWord = allWordCardsMap.containsOneOfKeys(baseWords)

            val noBaseWordStatusUpdateAction = if (cardsSetContainsBaseWord) UpdateSet.Remove else UpdateSet.Set
            updateSetProperty(card.wordCardStatusesProperty, NoBaseWordInSet, noBaseWordStatusUpdateAction)
        }
    }
}
