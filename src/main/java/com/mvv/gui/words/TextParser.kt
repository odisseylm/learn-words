package com.mvv.gui.words

import com.mvv.gui.util.*


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


private const val spaceChars = " \n\t"
private const val possibleSentenceEndChars = ".?!"

// We do not use there universal "'\""' because they are not 'paired'
private const val openCloseQuotesChars = "\"'"
private const val openQuotesChars = "“‘«‚"
private const val closingQuotesChars = "”’»’"

private fun isSentenceEnd(text: CharSequence, position: Int, sentenceEndRule: SentenceEndRule): Boolean {
    val char = text[position]
    if (char == 'C')
        Math.random()

    if (sentenceEndRule.byLineBreak && char == '\n') return true

    if (sentenceEndRule.byDot) {
        if (char in closingQuotesChars && position > 0 && text.prevChar(position) in possibleSentenceEndChars)
            return true

        if (!possibleSentenceEndChars.contains(char)) return false

        if (isPartOfAbbreviation(text, position)) return false
        if (isPartOfNumber(text, position)) return false

        return nextCharsAreSentenceBeginning(text, position, sentenceEndRule)
    }
    return false
}


private fun CharSequence.hasPrevChar(currentPosition: Int): Boolean = currentPosition > 0 && currentPosition < this.length
private fun CharSequence.prevChar(currentPosition: Int): Char = this[currentPosition - 1]

private fun CharSequence.hasNextChar(currentPosition: Int): Boolean = currentPosition < this.length - 1
private fun CharSequence.nextChar(currentPosition: Int): Char = this[currentPosition + 1]


/** 'byDot' means also '!' or '?'. */
enum class SentenceEndRule (val byDot: Boolean, val byLineBreak: Boolean) {
    ByEndingDot(true, false),
    ByLineBreak(false, true),
    ByEndingDotOrLineBreak(true, true),
}

fun nextCharsAreSentenceBeginning(text: CharSequence, position: Int, sentenceEndRule: SentenceEndRule): Boolean {

    if (text.hasNextChar(position) && text.nextChar(position) in possibleSentenceEndChars) return false

    for (charIndex in position + 1 until text.length) {
        val ch = text[charIndex]
        when {
            ch == '\n' && sentenceEndRule.byLineBreak -> return true
            Character.isSpaceChar(ch) -> continue
            ch.isDigit()     -> return true
            ch.isLetter() && ch.isUpperCase() -> return true
            ch.isLetter() && ch.isLowerCase() -> return false
            ch in closingQuotesChars -> return false
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


class TextParserEx (private val sentenceEndRule: SentenceEndRule = SentenceEndRule.ByEndingDot) {

    fun parse(text: CharSequence): List<Sentence> = logTime(log, "TextParserEx.parse($text)") {
        var wordsCount = 0
        parseSentences(text, sentenceEndRule)
            .map { logTime(log, "TextParserEx.parseSentence([$it])") {  parseSentence(it, wordsCount) } }
            .onEach { wordsCount += it.allWords.size }
            .toList()
    }

    private fun parseSentences(text: CharSequence, sentenceEndRule: SentenceEndRule): Sequence<CharSequence> {
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
            if (isSentenceEnd(text, index, sentenceEndRule)) {
                val currentSentence = text.subSequence(prevSentenceEnd + 1, index + 1).trim()
                prevSentenceEnd = index
                addSentence(currentSentence, sentences)
            }
        }

        if (prevSentenceEnd != text.length - 1) {
            val lastSentence = text.subSequence(prevSentenceEnd + 1, text.length).trim()
            addSentence(lastSentence, sentences)
        }

        return sentences.asSequence()
    }

    private fun addSentence(sentence: CharSequence, sentences: MutableList<CharSequence>) {
        var fixed = sentence.trim()

        fixed = removeUnpairedStartingQuote(fixed)
        fixed = removeUnpairedEndingQuote(fixed)

        if (fixed.isNotBlank()) {
            fixed = fixed
                .removeCharPrefixesRepeatably(spaceChars) // It is hack :-( need to fix finding proper sentence end.
                .removeCharSuffixesRepeatably(spaceChars) // It is hack :-( need to fix finding proper sentence end.

            fixed = addMissedEndingQuote(fixed)

            if (fixed.isNotEmpty())
                sentences.add(fixed)
        }
    }

}


internal fun removeUnpairedStartingQuote(sentence: CharSequence): CharSequence {
    var fixed = sentence
    if (fixed.startsWithOneOfChars(openQuotesChars)) {
        val indexOfClosingQuote = fixed.indexOfOneOfChars(closingQuotesChars, 1)
        val indexOfNextOpenQuote = fixed.indexOfOneOfChars(openQuotesChars, 1)

        val openQuoteHasNoClosingQuote = (indexOfClosingQuote == -1) && (indexOfClosingQuote < indexOfNextOpenQuote || indexOfNextOpenQuote == -1)

        if (openQuoteHasNoClosingQuote)
            fixed = fixed.substring(1).removeCharPrefixesRepeatably(spaceChars)
    }
    else {
        for (openCloseQuotesChar in openCloseQuotesChars) {
            if (fixed.startsWith(openCloseQuotesChar)) {
                val openCloseQuotesCharCount = fixed.count { it == openCloseQuotesChar }

                if (openCloseQuotesCharCount %2 == 1) {
                    fixed = fixed.substring(1).removeCharPrefixesRepeatably(spaceChars)
                }
                break
            }
        }
    }

    return fixed
}


internal fun removeUnpairedEndingQuote(sentence: CharSequence): CharSequence {
    var fixed = sentence
    if (fixed.endsWithOneOfChars(closingQuotesChars)) {
        val lastIndexOfOpenQuote = fixed.lastIndexOfOneOfChars(openQuotesChars)
        val lastIndexOfPrevCloseQuote = fixed.lastIndexOfOneOfChars(closingQuotesChars, 0, fixed.length - 2)

        val closeQuoteHasNoOpenQuote = (lastIndexOfOpenQuote == -1) && (lastIndexOfOpenQuote < lastIndexOfPrevCloseQuote || lastIndexOfPrevCloseQuote == -1)

        if (closeQuoteHasNoOpenQuote)
            fixed = fixed.substring(0, fixed.length - 1)
            fixed = fixed.removeCharSuffixesRepeatably(spaceChars)
    }
    else {
        for (openCloseQuotesChar in openCloseQuotesChars) {
            if (fixed.endsWith(openCloseQuotesChar)) {
                val openCloseQuotesCharCount = fixed.count { it == openCloseQuotesChar }

                if (openCloseQuotesCharCount %2 == 1) {
                    fixed = fixed.substring(0, fixed.length - 1).removeCharPrefixesRepeatably(spaceChars)
                }
                break
            }
        }
    }

    return fixed
}


private fun addMissedEndingQuote(sentence: CharSequence): CharSequence {
    var fixed = sentence

    val lastIndexOfOpenQuote = fixed.lastIndexOfOneOfChars(openQuotesChars)
    val lastIndexOfCloseQuote = fixed.lastIndexOfOneOfChars(closingQuotesChars)

    val lastOpenQuoteDoesNotHavePairedClosingQuote =
        (lastIndexOfOpenQuote != -1) && (lastIndexOfOpenQuote > lastIndexOfCloseQuote || lastIndexOfCloseQuote == -1)

    if (lastOpenQuoteDoesNotHavePairedClosingQuote)
        fixed = "$fixed”"

    return fixed
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
    val unneeded = "-—\"!?“”'"
    return sentence
        .removeCharSuffixesRepeatably(".!?")
        .splitToSequence(delimiters = wordDelimiters)
        .filter { it.isNotBlank() }
        //.filter { it.length > 1 }
        .map { it.removeCharPrefixesRepeatably(unneeded) }
        .map { it.removeCharSuffixesRepeatably(unneeded) }
        .filter { it.isNotBlank() }
        .mapIndexed { index, s -> WordEntry(s.toString().lowercase(), prevWordCount + index, tempSentenceStuff) }
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

// T O D O: probably can be removed or moved to other file (to CardWordEntry file)
val String.isTranslatable: Boolean get() = !this.isNumber
