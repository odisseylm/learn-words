package com.mvv.gui.words

import com.mvv.gui.util.logTime
import com.mvv.gui.util.removeSuffixesRepeatably


private val log = mu.KotlinLogging.logger {}


/**
 * Classes WordEntry and Sentence are deeply coupled.
 * I think in this case we can afford it and for that reason they have
 * closed/hidden constructors (and for this reason they are not data classes).
 */

@Suppress("EqualsOrHashCode")
class WordEntry internal constructor (
    val word: String,
    /** It is word index in the WHOLE text (not char index!). */
    val position: Int,
    val sentence: Sentence,
) {

    override fun toString(): String = "Word([$word]:$position )"

    // !! Only for testing !! Do not rely on it on production code!
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WordEntry

        if (word != other.word) return false
        if (position != other.position) return false
        if (sentence.text != other.sentence.text) return false

        return true
    }
}

@Suppress("EqualsOrHashCode")
class Sentence internal constructor(text: CharSequence, words: Iterable<WordEntry>) {

    val text: CharSequence
    val allWords: List<WordEntry>
    val translatableWords: List<WordEntry>

    init {
        this.text = text
        this.allWords = words.map { WordEntry(it.word, it.position, this) }
        this.translatableWords = this.allWords.filter { it.word.isTranslatable }
    }


    override fun toString(): String =
        "Sentence([$text], allWords=$allWords, translatableWords=$translatableWords)"

    // !! Only for testing !! Do not rely on it on production code!
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Sentence

        if (text != other.text) return false
        if (allWords != other.allWords) return false

        return true
    }
}


interface WordSequence {
    val words: List<String>
    val asText: String

    operator fun get(index: Int) = words[index]
    val size: Int get() = words.size

    val indices: IntRange get() = 0 until size

}

class Preposition (override val words: List<String>) : WordSequence {
    constructor(str: String) : this(str.split(" "))

    override val asText: String = words.joinToString(" ")
}

/*
private val verbPrepositions: List<Preposition> = sequenceOf(
        "about", "at", "from", "for", "in", "of", "on", "to", "with",
    )
    .map { it.lowercase() }
    .distinct()
    .map { Preposition(it) }
    .toList()
*/

val prepositions: List<Preposition> = sequenceOf(
        "at", "in", "about", "against", "before",
        // "concerning", "including", "depending", "granted",
        // "alongside", "outside", "within", "wherewith",
        // "because of", "instead of", "by virtue of", "for the sake of", "with regard to",
        "above", "across from", "around", "behind", "below", "between", "by", "beside", "near", "under",
        "close to", "in", "in front of", "next to", "on", "opposite", "over", "past",
        "across", "along", "away from", "back to", "down", "from", "into", "off", "onto", "out of", "over",
        "past", "round", "around", "through", "to", "towards", "under", "up",
        "after", "at", "before", "by", "during", "for", "from", "in", "on", "past", "since",
        "through", "till", "until", "within",
        "by", "with", "without", "on",
        "out off",
        "because of", "in accordance with", "thanks to", "on account of", "due to", "", "", "", "", "", "",
        "among", "as", "beneath", "beside", "beyond", "despite", "except", "inside",
        "ahead of", "apart from", "as for", "as well as", "except for", "in addition to", "in place of",
        "in spite of",
        // verb prepositions
        "about", "at", "from", "for", "in", "of", "on", "to", "with",
    )
    .map { it.lowercase() }
    .distinct()
    .map { Preposition(it) }
    .sortedBy { -it.size }
    .toList()


class TextParser {

    private val delimiters = arrayOf(
        " ", "\n", "\r", "\t",
        ".", ",", ";", ":", "!", "?",
        "=", "—",
        "!", "@", "$", "%", "^", "&", "*",
        "+", "*", "/",
        "<", ">",
        "\"", "\'", "/", "|",
        "~", "`", "“",
        "(", ")", "[", "]", "{", "}",
        "\\", "/",
    )

    fun parse(text: CharSequence): List<String> =
        text.splitToSequence(delimiters = delimiters)
            .filter { it.isNotEmpty() }
            .filter { it.length > 1 }
            .map { it.removePrefix("-") }
            .filter { !it.isNumber }
            .distinctBy { it.lowercase() }
            //.filter { !ignoredWordsSorted.contains(it) }
            //.distinct()
            .toList()
}


internal class AbbreviationRule (val text: String, val indices: Array<Int>) {

    init {
        require(text.isNotBlank())
        require(indices.isNotEmpty()) { "There are no '.' in abbreviation [$text]." }
    }

    internal fun thisDotIsPartOfTheAbbreviation(text: CharSequence, textDotPosition: Int): Boolean =
        this.indices.any { thisDotIsPartOfTheAbbreviation(it, text, textDotPosition) }


    private fun thisDotIsPartOfTheAbbreviation(abbrevDotPosition: Int, text: CharSequence, textDotPosition: Int): Boolean {
        for (i in abbrevDotPosition downTo 0) {
            val textPos = textDotPosition - (abbrevDotPosition - i)
            if (textPos < 0 || textPos >= text.length || text[textPos] != this.text[i]) return false
        }

        for (i in abbrevDotPosition until this.text.length) {
            val textPos = textDotPosition + i - abbrevDotPosition
            if (textPos < 0 || textPos >= text.length || text[textPos] != this.text[i]) return false
        }

        return true
    }


    companion object {
        operator fun invoke(text: String): AbbreviationRule {
            val indices = text
                .mapIndexedNotNull { index, char -> if (char == '.') index else null }
                .toTypedArray()
            return AbbreviationRule(text, indices)
        }
    }
}


private val abbreviations: List<AbbreviationRule> = sequenceOf(
    "Mr.", "Mrs.", "Ms.", "Dr.", "Jr.", "Sr.", "Smth.", "Smb.",
    "vs.",
    "etc.", "e.g.", "i.e.",
    "n.b", "et.al",
    "V.I.P.",
    "n.", "v.", "adj.", "adv.", "prep.", "p.", "pp.", "par.", "ex.",
    "pl.", "sing.", "P.S.", "P.P.S.", "Re.", "Rf.", "Edu.", "Appx.",
    // "w/o", "w/", "&",
    // "mph", "kph", "lb", "X-mas",
    "in.", "sec.", "gm.", "cm.", "qt.", "mph.", "kph.", "ft.", "lb.", "oz.", "pt.",
    "yr.", "Jan.", "Feb.", "Mar.", "Apr.", "Jun.", "Jul.", "Aug.", "Sep.", "Oct.", "Nov.", "Dec.", "May.", "X-mas.",
    "Mon.", "Wed.", "Thu.", "Fri.", "Sat.", "Sun.",
    //"TGIF",
    //"sis", "doc", "telly", "ph", "sngl", "dbl", "gent",
    "div.",
    )
    .filter { it.isNotBlank() }
    .filter { it.contains('.').also {
        containsDots -> if (!containsDots) log.warn { "Abbreviation [$it] does not contain '.' and ignored." } } }
    .map { AbbreviationRule(it) }
    .toList()


private const val possibleSentenceEndChars = ".?!"

private fun isSentenceEnd(text: CharSequence, position: Int): Boolean {
    val char = text[position]
    if (char == '.')
        Math.random()
    if (!possibleSentenceEndChars.contains(char)) return false

    if (isPartOfAbbreviation(text, position)) return false
    if (isPartOfNumber(text, position)) return false

    return nextCharsAreSentenceBeginning(text, position)
}


private fun CharSequence.hasPrevChar(currentPosition: Int): Boolean = currentPosition > 0 && currentPosition < this.length
private fun CharSequence.prevChar(currentPosition: Int): Char = this[currentPosition - 1]

private fun CharSequence.hasNextChar(currentPosition: Int): Boolean = currentPosition < this.length - 1
private fun CharSequence.nextChar(currentPosition: Int): Char = this[currentPosition + 1]

fun nextCharsAreSentenceBeginning(text: CharSequence, position: Int): Boolean {

    if (text.hasNextChar(position) && text.nextChar(position) in possibleSentenceEndChars) return false

    for (charIndex in position + 1 until text.length) {
        val ch = text[charIndex]
        when {
            ch == '\n' -> return true // TODO: make it configured to use '\n' or not
            Character.isSpaceChar(ch) -> continue
            ch.isDigit() -> return true
            ch.isUpperCase() -> return true
        }
    }

    return false
}

fun isPartOfNumber(text: CharSequence, position: Int): Boolean {
    // T O D O: try to find more general rules
    val currentChar = text[position]
    if (currentChar == '.') {
        if (text.hasPrevChar(position)) {
            val prevChar = text.prevChar(position)
            if (prevChar == '.' || prevChar == 'e' || prevChar == 'E') return false
        }
        if (text.hasNextChar(position)) {
            val nextChar = text.nextChar(position)
            if (nextChar == '.') return false
        }
    }
    if (currentChar == '-') {
        if (text.hasPrevChar(position)) {
            val prevChar = text.prevChar(position)
            if (prevChar == '-') return false
        }
        if (text.hasNextChar(position)) {
            val nextChar = text.nextChar(position)
            if (nextChar == '-') return false
        }
    }
    if (currentChar == 'e') {
        if (text.hasPrevChar(position)) {
            val prevChar = text.prevChar(position)
            if (prevChar.isLetter()) return false
        }
        if (text.hasNextChar(position)) {
            val nextChar = text.nextChar(position)
            if (nextChar.isLetter()) return false
        }
    }

    if (position >= 1 && text[position - 1].isPartOfNumber) return true
    if (position < text.length - 1 && text[position + 1].isPartOfNumber) return true

    return false
}

private fun isPartOfAbbreviation(text: CharSequence, position: Int) =
    abbreviations.any { abbr -> abbr.thisDotIsPartOfTheAbbreviation(text, position) }


//fun main() { logTime("TextParserEx.main()") { TextParserEx().parse("Mama washed rama.") } }


class TextParserEx {

    fun parse(text: CharSequence): List<Sentence> = logTime(log, "TextParserEx.parse($text)") {
        var wordsCount = 0
        parseSentences(text)
            .map { logTime(log, "TextParserEx.parseSentence([$it])") {  parseSentence(it, wordsCount) } }
            .onEach { wordsCount += it.allWords.size }
            .toList()
    }

    private fun parseSentences(text: CharSequence): Sequence<CharSequence> {
        var prevSentenceEnd = -1

        // We can optimize it to avoid using list
        // and implement really 'lazy' functional approach to return next sentence only by request,
        // but it will do the code more complicated, and we will not have real advantage
        // (since CharSequence to my surprise is not really lazy sequence!!, it is just array with access to any char by index!!).
        //
        // We can do such implementation only just as exercise :-)
        //
        val sentences = mutableListOf<CharSequence>()

        text.forEachIndexed { index, _ ->
            if (isSentenceEnd(text, index)) {
                val currentSentence = text.subSequence(prevSentenceEnd + 1, index + 1).trim()
                prevSentenceEnd = index
                sentences.add(currentSentence)
            }
        }

        if (prevSentenceEnd != text.length - 1) {
            val lastSentence = text.subSequence(prevSentenceEnd + 1, text.length).trim()
            sentences.add(lastSentence)
        }

        return sentences.asSequence()
    }

}


private val wordDelimiters = arrayOf(
    " ", "\n", "\r", "\t",
    ",", ";", ":", "!", "?",
    // ".",
    // "\'",
    "=", "—",
    "!", "@", "$", "%", "^", "&", "*",
    "+", "*", "/",
    "<", ">",
    "\"", "/", "|",
    "~", "`", "“",
    "(", ")", "[", "]", "{", "}",
    "\\", "/",
)


private fun parseSentence(sentence: CharSequence, prevWordCount: Int): Sentence {
    val tempSentenceStuff = Sentence(sentence, emptyList())
    return sentence
        .removeSuffixesRepeatably(".", "!", "?")
        .splitToSequence(delimiters = wordDelimiters)
        .filter { it.isNotBlank() }
        //.filter { it.length > 1 }
        .map { it.removePrefix("\"").removeSuffix("\"") }
        .map { it.removePrefix("-").removeSuffix("-") }
        .filter { it.isNotBlank() }
        .mapIndexed { index, s -> WordEntry(s.lowercase(), prevWordCount + index, tempSentenceStuff) }
        .toList()
        .let { Sentence(sentence, it) }

}


private const val numberChars = "1234567890-_.Ee"

private val Char.isPartOfNumber: Boolean get() = numberChars.contains(this)
private val Int.isPartOfNumber: Boolean get() = numberChars.contains(this.toChar())

private val String.isNumber: Boolean get() {
    return try { this.toInt(); true }
    catch (ignore: NumberFormatException) {
        this.codePoints().allMatch { ch -> ch.isPartOfNumber }
    }
}

// TODO: probably can be removed or moved to CardWordEntry file
val String.isTranslatable: Boolean get() = !this.isNumber
