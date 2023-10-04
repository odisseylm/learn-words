package com.mvv.gui.words

import com.mvv.gui.isGoodLearnCardCandidate
import com.mvv.gui.javafx.mapCached
import com.mvv.gui.parseToCard
import com.mvv.gui.util.containsEnglishLetters
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.scene.paint.Color


//private val log = mu.KotlinLogging.logger {}


class CardWordEntry {
    val fromProperty = SimpleStringProperty(this, "from", "")
    val fromWithPrepositionProperty = SimpleStringProperty(this, "fromWithPreposition", "")
    val fromWordCountProperty = fromProperty.mapCached { it.trim().split(" ", "\t", "\n").size }
    val toProperty = SimpleStringProperty(this, "to", "")
    val transcriptionProperty = SimpleStringProperty(this, "transcription", "")
    val translationCountProperty: ObservableValue<Int> = toProperty.mapCached { it?.translationCount ?: 0 }

    val examplesProperty = SimpleStringProperty(this, "examples", "")
    val exampleCountProperty = examplesProperty.mapCached { it.examplesCount }
    val exampleNewCardCandidateCountProperty = examplesProperty.mapCached { it.exampleNewCardCandidateCount }

    val wordCardStatusesProperty = SimpleObjectProperty<Set<WordCardStatus>>(this, "wordCardStatuses", emptySet())
    val predefinedSetsProperty = SimpleObjectProperty<Set<PredefinedSet>>(this, "predefinedSets", emptySet())
    val sourcePositionsProperty = SimpleObjectProperty<List<Int>>(this, "sourcePositions", emptyList())
    val sourceSentencesProperty = SimpleStringProperty(this, "sourceSentences", "")

    var from: String
        get() = fromProperty.get()
        set(value) = fromProperty.set(value)
    var fromWithPreposition: String
        get() = fromWithPrepositionProperty.get()
        set(value) = fromWithPrepositionProperty.set(value)
    val fromWordCount: Int
        get() = fromWordCountProperty.value
    var to: String
        get() = toProperty.get()
        set(value) = toProperty.set(value)
    var transcription: String
        get() = transcriptionProperty.get()
        set(value) = transcriptionProperty.set(value)
    var examples: String
        get() = examplesProperty.get()
        set(value) = examplesProperty.set(value)
    //val exampleCount: Int get() = exampleCountProperty.value
    val exampleNewCardCandidateCount: Int get() = exampleNewCardCandidateCountProperty.value

    val translationCount: Int
        get() = translationCountProperty.value

    var wordCardStatuses: Set<WordCardStatus>
        get() = wordCardStatusesProperty.get()
        set(value) = wordCardStatusesProperty.set(value)

    var predefinedSets: Set<PredefinedSet>
        get() = predefinedSetsProperty.get()
        set(value) = predefinedSetsProperty.set(value)

    var sourcePositions: List<Int>
        get() = sourcePositionsProperty.get()
        set(value) = sourcePositionsProperty.set(value)

    var sourceSentences: String
        get() = sourceSentencesProperty.get()
        set(value) = sourceSentencesProperty.set(value)

    // for showing in tooltip. It is filled during word cards analysis.
    @Transient
    var missedBaseWords: List<String> = emptyList()

    constructor(from: String, to: String) {
        //toProperty.addListener { _, _, newValue -> translationCount = newValue?.translationCount ?: 0 }

        this.from = from
        this.to = to
    }

    override fun toString(): String =
        "CardWordEntry(from='$from', to='${to.take(20)}...', wordCardStatuses=$wordCardStatuses," +
                " translationCount=$translationCount, transcription='$transcription', examples='${examples.take(10)}...')"

    fun copy(): CardWordEntry = CardWordEntry(this.from, this.to).also {
        it.fromWithPreposition = this.fromWithPreposition
        it.transcription = this.transcription
        it.examples = this.examples
        it.wordCardStatuses = this.wordCardStatuses
        it.predefinedSets = this.predefinedSets
        it.sourcePositions = this.sourcePositions
        it.sourceSentences = this.sourceSentences
        it.missedBaseWords = this.missedBaseWords
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


private val String.examplesCount: Int get() {
    if (this.isBlank()) return 0

    return this
        .splitToSequence("\n")
        .filter { it.isNotBlank() }
        .filter { it.containsEnglishLetters() }
        .count()
}


private val String.exampleNewCardCandidateCount: Int get() {
    if (this.isBlank()) return 0

    return this
        .splitToSequence("\n")
        .filter { it.isNotBlank() }
        .filter { it.containsEnglishLetters() }
        .filter { it.parseToCard()?.isGoodLearnCardCandidate() ?: false }
        .count()
}


enum class WordCardStatus (
    val isWarning: Boolean,
    val toolTipF: (CardWordEntry)->String,
    ) {

    // *************************** Low priority warnings ***************************************************************
    //
    /**
     * If current word has ending/suffix 'ed', 'ing', 'es', 's' does not have
     * but the whole set does not have base word without such ending/suffix.
     *
     * It is not comfortable to learn such word if you do not know base word.
     */
    NoBaseWordInSet(true, {
        "Words set does not have base word(s) '${it.missedBaseWords.joinToString("|")}'.\n" +
        "It is advised to add these base word(s) to the set." }),

    // T O D O: do not save warnings to file since they are recalculated, only save 'ignore' flags
    TooManyExampleNewCardCandidates(true, {
        "There are too many examples similar to learning cards for '${it.from}' (${it.exampleNewCardCandidateCount})." +
                " Please convert them to separate cards."}),


    // *************************** High priority warnings **************************************************************
    //
    TranslationIsNotPrepared(true, {"The translation for '${it.from}' is not prepared for learning. " +
            "Please remove unneeded symbols (like [, 1., 2., 1), 2) so on)."}),

    Duplicates(true, { "Duplicate. Please remove duplicates." }),

    NoTranslation(true, {"No translation for '${it.from}'."}),


    // *************************** Ignore flags ************************************************************************
    //
    /**
     * Marker to stop validation on NoBaseWordInSet.
     */
    BaseWordDoesNotExist(false, {""}),

    IgnoreExampleCardCandidates(false, {""}),
    ;

    val cssClass: String get() = "WordCardStatus-${this.name}"

    companion object {
        val allCssClasses = WordCardStatus.values().map { it.cssClass }
    }
}


enum class PredefinedSet (val humanName: String) {
    DifficultToListen("Difficult To Listen"),
    DifficultSense("Difficult Sense"),
}
