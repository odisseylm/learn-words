package com.mvv.gui.words

import com.mvv.gui.javafx.CacheableObservableValue
import com.mvv.gui.javafx.mapCached
import com.mvv.gui.util.safeSubstring
import com.mvv.gui.util.toTextPosition
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ObservableValue
import javafx.scene.paint.Color
import java.time.ZonedDateTime


//private val log = mu.KotlinLogging.logger {}


enum class Language {
    English,
    Russian,
}


interface BaseWordExtractor {
    fun extractBaseWord(phrase: String): String
}


interface CardWordEntry {
    val baseWordExtractor: BaseWordExtractor?

    val createdAtProperty: ObjectProperty<ZonedDateTime>
    val updatedAtProperty: ObjectProperty<ZonedDateTime>

    val fromProperty: StringProperty
    val fromWithPrepositionProperty: StringProperty
    val baseWordOfFromProperty: CacheableObservableValue<String>
    val baseWordAndFromProperty: CacheableObservableValue<BaseAndFrom>
    val fromWordCountProperty: ObservableValue<Int>

    val toProperty: StringProperty
    val transcriptionProperty: StringProperty
    val translationCountProperty: ObservableValue<Int>

    val partsOfSpeechProperty: ObjectProperty<Set<PartOfSpeech>>
    val predictedPartOfSpeechProperty: ObservableValue<PartOfSpeech>

    val examplesProperty: StringProperty
    val exampleCountProperty: ObservableValue<Int>
    val exampleNewCardCandidateCountProperty: ObservableValue<Int>

    val statusesProperty: ObjectProperty<Set<WordCardStatus>>
    val predefinedSetsProperty:  ObjectProperty<Set<PredefinedSet>>
    val sourcePositionsProperty: ObjectProperty<List<Int>>
    val sourceSentencesProperty: StringProperty

    // for showing in tooltip. It is filled during word cards analysis.
    //@Transient
    val missedBaseWordsProperty: ObjectProperty<List<String>>

    fun copy(): CardWordEntry
    // TODO: why do I need it?? Try to remove this param.
    fun copy(baseWordExtractor: BaseWordExtractor?): CardWordEntry
}


// It is my mistake that I didn't add createdAt/updatedAt from the outset.
// but now I cannot replace missed timestamps with 'now' since it will break
// real 'latest card' verification.
// Let's use date of project beginning.
val defaultEpochDate: ZonedDateTime = ZonedDateTime.parse("2022-01-01T11:11:11+02:00")

//fun ZonedDateTime?.toNullIfEpochDefault(): ZonedDateTime? =
//    if (this != defaultEpochDate) null else this
//@Suppress("NOTHING_TO_INLINE")
//inline fun ZonedDateTime?.toNullIfUnset(): ZonedDateTime? = this.toNullIfEpochDefault()
fun ZonedDateTime?.isUnset(): Boolean =
    this == null || this == defaultEpochDate

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

inline fun cardWordEntry(init: CardWordEntry.()->Unit) = CardWordEntry("", "").apply(init)


// Currently I keep there only mostly required for learning,
// otherwise it overloads UI (combo-box) for choosing  'part of speech'
enum class PartOfSpeech (vararg shorts: String) {
    Noun("n.", "(n.)", "_n."),
    Adjective("adj.", "(adj.)", "_adj."),      // Прилагательное

    Verb("v.", "(v.)", "_v."),
    // AuxiliaryVerb("av.", "(av.)", "_av."),
    // ModalVerb("mv.", "(mv.)", "_mv."),
    // PhrasalVerb("phrv.", "(phrv.)", "_phrv."),

    // PluralPreposition("pi.", "(pi.)", "_pi."),

    //Singular("sg.", "(sg.)", "_sg."),
    //Plural("pl.", "(pl.)", "_pl."),

    //Pronoun("pron.", "(.pron)", ".pron_"),     // Местоимение
    Adverb("adv.", "(adv.)", "_adv."),         // Наречие
    // Union("un.", "(un.)", "_un."),             // Союз     // Is 'un.' ok?
    Numeral("num.", "(num.)", "_num.", "n-ord.", "(n-ord.)", "_n-ord.", "n-card.", "(n-card.)", "_n-card."),
    // Article("art.", "(art.)", "_art."),
    // Particle("part.", "(part.)", "_part."),    // Частица  // Is 'part.' ok?
    // Preposition("prep.", "(prep.)", "_prep."), // Предлог
    // Interjection("interj.", "(interj.)", "_interj."), // Междометие
    // Conjunction("cj.", "(cj.)", "_cj."),
    // Determiner("det.", "(det.)", "_det."),            // Is 'det.' ok?
    Exclamation("exclam.", "(exclam.)", "_exclam."),  // Is 'excl.' ok?

    // v.t.

    // Article,      // the, an, a
    // Conjunction,  // and... but... or... while... because
    // Interjection, // Oh!... Wow!... Oops!

    /*
    v.t.
    _emph. сами;
    _emph. сам, сама
    _attr. молодёжный;
    _sup      best   1. _a. (_sup. от good 1)
    _predic.  kaput    _нем. _a. _predic. _разг. уничтоженный; разорённый; потерпевший
    _pass     passive voice
    _press-p
    _demonstr.  it 1. _pron.  1) _pers. (_obj. без измен.) он, она, оно (о предметах и животных);  ... это; 2) _demonstr. это;

    _subj.

    _p-p       cracked   1. _p-p. от crack
    _p         crept     _p. и _p-p. от creep 1

    _pres-p    yearning  1. _pres-p. от yearn
    _poss.

    abbrev    abbreviation or acronym
    av        auxiliary verb
    Am Eng    American English
    Br Eng    British English
    conj      determiner
    ?? conj det  conjunction determiner
    ?? det    determiner
    exclam    exclamation

    mv        modal verb
    n         noun

    ???
    phrv      phrasal verb
    pi
    prep       plural preposition
    prep phr   prepositional phrase

    pron       pronoun
    sing       singular
    v          verb

    ?? v. a.
    ?? v. a. & n.
    ?? v. n.
    */

    //AmericanEnglish("Am Eng"),
    //BritishEnglish("Br Eng"),

    // Enough good type, but it is not supported by memo and for that reason now it is disabled.
    // Abbreviation or acronym
    // Abbreviation("abbrev.", "(abbrev.)", "_abbrev."), // Is 'abbrev.' ok?

    // prepositional phrase
    // PrepPhrase("prep phr."),

    Word,
    Phrase("phr.", "(phr.)", "_phr."),     // Is 'phr.' ok?

    SetExpression("se.", "(se.)", "_se."), // Is 'se.' ok?
    ;

    val all: List<String> = (shorts.toList() + shorts.map { "($it)" } + this.name.lowercase() + this.name).sortedBy { it.length }

    companion object {
        val allCssClasses = PartOfSpeech.values().map { it.name }
    }
}

/*
Groups:
_разг.
_архит. _мор. _стр. _жив. _охот. _тех. _бот. _ж-д.

_перен.
_a. _predic.
_pl. _собир.
*/

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
    to.createdAt       = this.createdAt
    to.updatedAt       = this.updatedAt
    to.partsOfSpeech   = this.partsOfSpeech
}


private class CardWordEntryImpl (
    // val features: Set<CardWordEntryFeatures> = emptySet(),
    override val baseWordExtractor: BaseWordExtractor? = null,
    ) : CardWordEntry {

    override val createdAtProperty = SimpleObjectProperty(this, "createdAt", defaultEpochDate)
    override val updatedAtProperty = SimpleObjectProperty(this, "updatedAt", defaultEpochDate)

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

    override val partsOfSpeechProperty: ObjectProperty<Set<PartOfSpeech>> = SimpleObjectProperty(this, "partsOfSpeech", emptySet())
    override val predictedPartOfSpeechProperty: CacheableObservableValue<PartOfSpeech> = fromProperty.mapCached(
        { guessPartOfSpeech(it, toProperty.value) }, toProperty)

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
    TranslationIsNotPrepared(true, {
        val s = StringBuilder("The from/translation for '${it.from}' is not prepared for learning. ")
        val status = validateFromOrToPreparedStatus(it)

        if (!status.isOk) {
            for (problem in status.problems) {
                when (problem) {
                    FormatProblem.IsBlank ->
                        s.append("\nIt is blank.")
                    FormatProblem.ForbiddenChars ->
                        s.append("\nPlease remove unneeded symbols (like [, 1., 2., 1), 2) so on.")
                    FormatProblem.UnpairedBrackets ->
                        s.append("\nPlease fix unpaired brackets '()'.")
                }
            }

            val problemIndex = status.problemIndex
            s.append("\nSee at position $problemIndex")

            val to = it.to
            if (problemIndex != -1 && problemIndex < to.length) {
                val textPos = to.toTextPosition(problemIndex)
                s.append(" [${textPos.row + 1},${textPos.column + 1}]")

                val textPart = to.safeSubstring(problemIndex, problemIndex + 20)
                if (textPart.isNotBlank())
                    s.append(" (").append(textPart).append("...)")
            }
            s.append(".")
        }
        s.toString()
    }),

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
