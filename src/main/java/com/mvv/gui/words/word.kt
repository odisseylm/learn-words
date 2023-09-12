package com.mvv.gui.words

import com.mvv.gui.javafx.AroundReadOnlyIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.paint.Color



//private val log = mu.KotlinLogging.logger {}


class CardWordEntry {
    val fromProperty = SimpleStringProperty(this, "from", "")
    val fromWithPrepositionProperty = SimpleStringProperty(this, "fromWithPreposition", "")
    val toProperty = SimpleStringProperty(this, "to", "")
    val transcriptionProperty = SimpleStringProperty(this, "transcription", "")
    val translationCountProperty = AroundReadOnlyIntegerProperty<String>(this, "translationCount", toProperty) {
        to -> to?.translationCount ?: 0 }
    val examplesProperty = SimpleStringProperty(this, "examples", "")
    val wordCardStatusesProperty = SimpleObjectProperty<Set<WordCardStatus>>(this, "wordCardStatuses", emptySet())
    val predefinedSetsProperty = SimpleObjectProperty<Set<PredefinedSet>>(this, "predefinedSets", emptySet())
    val sourcePositionsProperty = SimpleObjectProperty<List<Int>>(this, "sourcePositions", emptyList())
    val sourceSentencesProperty = SimpleObjectProperty<List<String>>(this, "sourceSentences", emptyList())

    var from: String
        get() = fromProperty.get()
        set(value) = fromProperty.set(value)
    var fromWithPreposition: String
        get() = fromWithPrepositionProperty.get()
        set(value) = fromWithPrepositionProperty.set(value)
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

    var predefinedSets: Set<PredefinedSet>
        get() = predefinedSetsProperty.get()
        set(value) = predefinedSetsProperty.set(value)

    var sourcePositions: List<Int>
        get() = sourcePositionsProperty.get()
        set(value) = sourcePositionsProperty.set(value)

    var sourceSentences: List<String>
        get() = sourceSentencesProperty.get()
        set(value) = sourceSentencesProperty.set(value)

    // for showing in tooltip. It is filled during word cards analysis.
    @Transient
    var missedBaseWords: List<String> = emptyList()

    constructor(from: String, to: String) {
        toProperty.addListener { _, _, newValue -> translationCount = newValue?.translationCount ?: 0 }

        this.from = from
        this.to = to
    }

    override fun toString(): String =
        "CardWordEntry(from='$from', to='${to.take(20)}...', wordCardStatuses=$wordCardStatuses," +
                " translationCount=$translationCount, transcription='$transcription', examples='${examples.take(10)}...')"

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


// It would be nice to optimize it to avoid unneeded string conversions.
val String.translationCount: Int get() =
    formatWordOrPhraseToMemoWordFormat(this)
        .split(",")
        .filter { it.isNotBlank() }.size


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
        "Words set does not have base word(s) '${it.missedBaseWords.joinToString("|")}'.\n" +
        "It is advised to add these base word(s) to the set." }),

    /**
     * Marker to stop validation on NoBaseWordInSet.
     */
    BaseWordDoesNotExist({""}),

    NoTranslation({"No translation for '${it.from}'."}),

    TranslationIsNotPrepared({"The translation for '${it.from}' is not prepared for learning. " +
            "Please remove unneeded symbols (like [, 1., 2., 1), 2) so on)."}),
    ;

    val cssClass: String get() = "WordCardStatus-${this.name}"

    companion object {
        val allCssClasses = WordCardStatus.values().map { it.cssClass }
    }
}


enum class PredefinedSet {
    DifficultToListen,
    DifficultSense,
}
