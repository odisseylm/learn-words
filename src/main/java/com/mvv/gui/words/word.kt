package com.mvv.gui.words

import com.mvv.gui.cardeditor.isGoodLearnCardCandidate
import com.mvv.gui.javafx.CacheableObservableValue
import com.mvv.gui.javafx.mapCached
import com.mvv.gui.cardeditor.parseToCard
import com.mvv.gui.util.containsEnglishLetters
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.scene.paint.Color
import java.nio.file.Path


//private val log = mu.KotlinLogging.logger {}


interface BaseWordExtractor {
    fun extractBaseWord(phrase: String): String
}


class CardWordEntry (from: String, to: String, /*val features: Set<CardWordEntryFeatures> = emptySet(),*/ private val baseWordExtractor: BaseWordExtractor? = null) {
    val fromProperty = SimpleStringProperty(this, "from", "")
    val fromWithPrepositionProperty = SimpleStringProperty(this, "fromWithPreposition", "")

    val baseWordOfFromProperty: CacheableObservableValue<String> = fromProperty.let { fromProp ->
        fromProp.mapCached { baseWordExtractor?.extractBaseWord(it) ?: it }
    }

    // Synthetic property for sorting because there is no way to pass Comparator<Card> for specific table column (we can use only Comparator<String>).
    val baseWordAndFromProperty: CacheableObservableValue<BaseAndFrom> = baseWordOfFromProperty.mapCached { baseOfFrom ->
        BaseAndFrom(baseOfFrom, fromProperty.value)
    }

    val fromWordCountProperty = fromProperty.mapCached { it.trim().split(" ", "\t", "\n").size }
    val toProperty = SimpleStringProperty(this, "to", "")
    val transcriptionProperty = SimpleStringProperty(this, "transcription", "")
    val translationCountProperty: ObservableValue<Int> = toProperty.mapCached { it?.translationCount ?: 0 }

    val examplesProperty = SimpleStringProperty(this, "examples", "")
    val exampleCountProperty = examplesProperty.mapCached { it.examplesCount }
    val exampleNewCardCandidateCountProperty = examplesProperty.mapCached { it.exampleNewCardCandidateCount }

    val statusesProperty        = SimpleObjectProperty<Set<WordCardStatus>>(this, "statuses", emptySet())
    val predefinedSetsProperty  = SimpleObjectProperty<Set<PredefinedSet>>(this, "predefinedSets", emptySet())
    val sourcePositionsProperty = SimpleObjectProperty<List<Int>>(this, "sourcePositions", emptyList())
    val sourceSentencesProperty = SimpleStringProperty(this, "sourceSentences", "")

    @Transient // optional, used only in search results
    val fileProperty = SimpleObjectProperty<Path>(this, "file", null)

    var from: String
        get()      = fromProperty.valueSafe
        set(value) = fromProperty.set(value)
    var fromWithPreposition: String
        get()      = fromWithPrepositionProperty.valueSafe
        set(value) = fromWithPrepositionProperty.set(value)
    val fromWordCount: Int
        get() = fromWordCountProperty.value
    val baseWordOfFrom: String
        get() = baseWordOfFromProperty.value ?: ""
    var to: String
        get()      = toProperty.get()
        set(value) = toProperty.set(value)
    var transcription: String
        get()      = transcriptionProperty.get()
        set(value) = transcriptionProperty.set(value)
    var examples: String
        get()      = examplesProperty.get()
        set(value) = examplesProperty.set(value)
    //val exampleCount: Int get() = exampleCountProperty.value
    val exampleNewCardCandidateCount: Int get() = exampleNewCardCandidateCountProperty.value

    val translationCount: Int
        get() = translationCountProperty.value

    var statuses: Set<WordCardStatus>
        get()      = statusesProperty.get()
        set(value) = statusesProperty.set(value)

    var predefinedSets: Set<PredefinedSet>
        get()      = predefinedSetsProperty.get()
        set(value) = predefinedSetsProperty.set(value)

    var sourcePositions: List<Int>
        get()      = sourcePositionsProperty.get()
        set(value) = sourcePositionsProperty.set(value)

    var sourceSentences: String
        get()      = sourceSentencesProperty.get()
        set(value) = sourceSentencesProperty.set(value)

    //@Transient
    var file: Path?
        get()      = fileProperty.get()
        set(value) = fileProperty.set(value)

    // for showing in tooltip. It is filled during word cards analysis.
    @Transient
    var missedBaseWords: List<String> = emptyList()

    //constructor(from: String, to: String, features: Set<CardWordEntryFeatures> = emptySet()) {
    init {
        this.from = from
        this.to   = to
    }

    override fun toString(): String =
        "CardWordEntry(from='$from', to='${to.take(20)}...', statuses=$statuses," +
                " translationCount=$translationCount, transcription='$transcription', examples='${examples.take(10)}...')"

    fun copy(baseWordExtractor: BaseWordExtractor? = null): CardWordEntry =
        CardWordEntry(this.from, this.to, baseWordExtractor ?: this.baseWordExtractor)
            .also {
                it.fromWithPreposition = this.fromWithPreposition
                it.transcription   = this.transcription
                it.examples        = this.examples
                it.statuses        = this.statuses
                it.predefinedSets  = this.predefinedSets
                it.sourcePositions = this.sourcePositions
                it.sourceSentences = this.sourceSentences
                it.missedBaseWords = this.missedBaseWords
            }

}


val cardWordEntryComparator: Comparator<CardWordEntry> = Comparator.comparing({ it.from }, String.CASE_INSENSITIVE_ORDER)


class BaseAndFrom (val base: String, val from: String) : Comparable<BaseAndFrom> {
    override fun compareTo(other: BaseAndFrom): Int {
        val baseComparing = this.base.compareTo(other.base)
        if (baseComparing == 0) {
            return when (base) {
                this.from  -> -1  // to show pure word before other ('wear' before 'in wear')
                other.from ->  1  // to show pure word before other ('wear' before 'in wear')
                else       -> this.from.compareTo(other.from)
            }
        }

        return baseComparing
    }
    //override fun compareTo(other: BaseAndFrom): Int =
    //    this.base.compareTo(other.base)
    //        .thenCompare { this.from.compareTo(other.from) }
    //override fun compareTo(other: BaseAndFrom): Int =
    //    compare(this, other) { it.base }
    //       .thenCompare(this, other) { it.from }
}


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


val CharSequence.translationCount: Int get() {

    var count = 0
    var bracketLevel = 0
    var wasTranslationChars = false

    for (i in this.indices) {
        val ch = this[i]

        if (ch == '(') bracketLevel++
        if (ch == ')' && bracketLevel > 0) bracketLevel--

        if (bracketLevel == 0) {
            if (ch == '\n' || ch == ',' || ch == ';') {
                if (wasTranslationChars) count++
                wasTranslationChars = false
            }
            else if (!ch.isWhitespace())
                wasTranslationChars = true
        }
    }

    if (bracketLevel == 0 && wasTranslationChars) count++

    return count
}

fun CharSequence.splitToToTranslations(): List<CharSequence> {

    data class Range (val first: Int, val last: Int) // or we can use Pair, but I preferred to use normal class

    var bracketLevel = 0

    var firstNonSpaceIndex = -1
    var lastNonSpaceIndex = -1
    val translations = mutableListOf<Range>()

    for (i in this.indices) {
        val ch = this[i]

        if (ch == '(') bracketLevel++
        if (ch == ')' && bracketLevel > 0) bracketLevel--

        if (bracketLevel == 0) {
            if (ch == '\n' || ch == ',' || ch == ';') {
                if (firstNonSpaceIndex != -1) translations.add(Range(firstNonSpaceIndex, lastNonSpaceIndex))
                firstNonSpaceIndex = -1
                lastNonSpaceIndex = -1
            }
            else if (!ch.isWhitespace()) {
                if (firstNonSpaceIndex == -1) firstNonSpaceIndex = i
                lastNonSpaceIndex = i
            }
        }
    }

    if (bracketLevel == 0 && firstNonSpaceIndex != -1) translations.add(Range(firstNonSpaceIndex, lastNonSpaceIndex))

    return translations.map { this.subSequence(it.first, it.last + 1) }
}

val String.translationCount_Old: Int get() = splitToToTranslations_Old().size

fun String.splitToToTranslations_Old() =
    formatWordOrPhraseToMemoWordFormat(this)
        .split(",")
        .filter { it.isNotBlank() }


val Int.toTranslationCountStatus: TranslationCountStatus get() = when (this) {
    in 0..3 -> TranslationCountStatus.Ok
    in 4..5 -> TranslationCountStatus.NotBad
    in 6..7 -> TranslationCountStatus.Warn
    else -> TranslationCountStatus.ToMany
}


// TODO: improve examples splitting logic
//       1) We should process auto inserted examples from dictionaries (like 1) bla-bla 2) bla-bla ...   it is in one long line )
//       2) We should process auto inserted by Ctrl+E.
//          Such example can have several lines (2nd, 3rd line start with space chars) and all lines should be considered as ONE example.
//
private val CharSequence.examplesCount: Int get() {
    if (this.isBlank()) return 0

    return this
        .splitToSequence("\n")
        .filter { it.isNotBlank() }
        .filter { it.containsEnglishLetters() }
        .count()
}


private val CharSequence.exampleNewCardCandidateCount: Int get() {
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
    TranslationIsNotPrepared(true, {"The from/translation for '${it.from}' is not prepared for learning. " +
            "Please remove unneeded symbols (like [, 1., 2., 1), 2) so on) and unpaired brackets '()'."}),

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
