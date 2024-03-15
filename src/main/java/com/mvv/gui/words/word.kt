package com.mvv.gui.words

import com.mvv.gui.javafx.CacheableObservableValue
import com.mvv.gui.javafx.mapCached
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ObservableValue
import javafx.scene.paint.Color


//private val log = mu.KotlinLogging.logger {}


interface BaseWordExtractor {
    fun extractBaseWord(phrase: String): String
}


interface CardWordEntry {
    val baseWordExtractor: BaseWordExtractor?

    val fromProperty: StringProperty
    val fromWithPrepositionProperty: StringProperty
    val baseWordOfFromProperty: CacheableObservableValue<String>
    val baseWordAndFromProperty: CacheableObservableValue<BaseAndFrom>
    val fromWordCountProperty: ObservableValue<Int>

    val toProperty: StringProperty
    val transcriptionProperty: StringProperty
    val translationCountProperty: ObservableValue<Int>

    val examplesProperty: StringProperty
    val exampleCountProperty: ObservableValue<Int>
    val exampleNewCardCandidateCountProperty: ObservableValue<Int>

    val statusesProperty: ObjectProperty<Set<WordCardStatus>>
    val predefinedSetsProperty:  ObjectProperty<Set<PredefinedSet>>
    val sourcePositionsProperty: ObjectProperty<List<Int>>
    val sourceSentencesProperty: StringProperty

    // for showing in tooltip. It is filled during word cards analysis.
    //@Transient
    //var missedBaseWords: List<String> = emptyList()
    val missedBaseWordsProperty: ObjectProperty<List<String>>

    fun copy(): CardWordEntry
    // TODO: why do I need it?? Try to remove this param.
    fun copy(baseWordExtractor: BaseWordExtractor?): CardWordEntry
}

fun CardWordEntry(
    from: String, to: String,
    // val features: Set<CardWordEntryFeatures> = emptySet(),
    baseWordExtractor: BaseWordExtractor? = null,
): CardWordEntry {
    return CardWordEntryImpl(baseWordExtractor).also {
        it.from = from
        it.to = to
    }
}


fun CardWordEntry.copyBasePropsTo(to: CardWordEntry) {
    to.from = this.from
    to.to   = this.to
    to.fromWithPreposition = this.fromWithPreposition
    to.transcription   = this.transcription
    to.examples        = this.examples
    to.statuses        = this.statuses
    to.predefinedSets  = this.predefinedSets
    to.sourcePositions = this.sourcePositions
    to.sourceSentences = this.sourceSentences
    to.missedBaseWords = this.missedBaseWords
}


private class CardWordEntryImpl (
    // val features: Set<CardWordEntryFeatures> = emptySet(),
    override val baseWordExtractor: BaseWordExtractor? = null,
    ) : CardWordEntry {

    override val fromProperty = SimpleStringProperty(this, "from", "")
    override val fromWithPrepositionProperty = SimpleStringProperty(this, "fromWithPreposition", "")

    override val baseWordOfFromProperty: CacheableObservableValue<String> = fromProperty.let { fromProp ->
        fromProp.mapCached { baseWordExtractor?.extractBaseWord(it) ?: it }
    }

    // Synthetic property for sorting because there is no way to pass Comparator<Card> for specific table column (we can use only Comparator<String>).
    override val baseWordAndFromProperty: CacheableObservableValue<BaseAndFrom> = baseWordOfFromProperty.mapCached { baseOfFrom ->
        BaseAndFrom(baseOfFrom, fromProperty.value)
    }

    override val fromWordCountProperty = fromProperty.mapCached { it.trim().split(" ", "\t", "\n").size }
    override val toProperty = SimpleStringProperty(this, "to", "")
    override val transcriptionProperty = SimpleStringProperty(this, "transcription", "")
    override val translationCountProperty: ObservableValue<Int> = toProperty.mapCached { it?.translationCount ?: 0 }

    override val examplesProperty = SimpleStringProperty(this, "examples", "")
    override val exampleCountProperty = examplesProperty.mapCached { it.examplesCount }
    override val exampleNewCardCandidateCountProperty = examplesProperty.mapCached { it.exampleNewCardCandidateCount }

    override val statusesProperty        = SimpleObjectProperty<Set<WordCardStatus>>(this, "statuses", emptySet())
    override val predefinedSetsProperty  = SimpleObjectProperty<Set<PredefinedSet>>(this, "predefinedSets", emptySet())
    override val sourcePositionsProperty = SimpleObjectProperty<List<Int>>(this, "sourcePositions", emptyList())
    override val sourceSentencesProperty = SimpleStringProperty(this, "sourceSentences", "")

    // for showing in tooltip. It is filled during word cards analysis.
    @Transient
    override val missedBaseWordsProperty = SimpleObjectProperty<List<String>>(this, "missedBaseWords", emptyList())

    override fun toString(): String =
        "CardWordEntry(from='$from', to='${to.take(20)}...', statuses=$statuses," +
                " translationCount=$translationCount, transcription='$transcription', examples='${examples.take(10)}...')"

    override fun copy(): CardWordEntry = copy(null)
    override fun copy(baseWordExtractor: BaseWordExtractor?): CardWordEntry =
        CardWordEntryImpl(baseWordExtractor ?: this.baseWordExtractor)
            .also { this.copyBasePropsTo(it) }

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
