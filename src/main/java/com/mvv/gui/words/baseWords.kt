package com.mvv.gui.words

import com.mvv.gui.dictionary.Dictionary
import com.mvv.gui.util.endsWithOneOf
import com.mvv.gui.util.startsWithOneOf


private interface BaseWordRule {
    fun possibleBaseWords(word: String): List<String>
}


private const val minBaseWordLength = 3

private class SuffixBaseWordRule (val suffix: String, val excludeSuffixes: List<String>, val replaceSuffixBy: List<String>) : BaseWordRule {

    constructor(suffix: String, replaceSuffixBy: List<String>)
        : this(suffix, emptyList(), replaceSuffixBy)

    constructor(suffix: String, replaceSuffixBy: String)
        : this(suffix, listOf(replaceSuffixBy))

    override fun possibleBaseWords(word: String): List<String> {
        val suitableWord = word.length > suffix.length + minBaseWordLength - 2
                && word.endsWith(suffix) && !word.endsWithOneOf(excludeSuffixes)

        return if (suitableWord) {
            val wordWithoutSuffix = word.removeSuffix(suffix)

            val wordsWithoutSuffix: List<String> = if (wordWithoutSuffix.hasDoubledLastChar)
                listOf(wordWithoutSuffix, wordWithoutSuffix.substring(0, wordWithoutSuffix.length - 1))
                else listOf(wordWithoutSuffix)

            replaceSuffixBy.flatMap { newSuffix -> wordsWithoutSuffix.map { it + newSuffix } }.filter { it.length >= minBaseWordLength }
        }
        else emptyList()
    }
}

private class PrefixBaseWordRule (val prefix: String, val excludePrefixes: List<String>, val replacePrefixBy: List<String>) : BaseWordRule {

    constructor(prefix: String, replacePrefixBy: List<String>)
        : this(prefix, emptyList(), replacePrefixBy)

    constructor(prefix: String, replacePrefixBy: String)
        : this(prefix, listOf(replacePrefixBy))

    override fun possibleBaseWords(word: String): List<String> {
        val suitableWord = word.length > prefix.length + minBaseWordLength
                && word.startsWith(prefix) && !word.startsWithOneOf(excludePrefixes)

        return if (suitableWord) {
            val wordWithoutPrefix = word.removePrefix(prefix)
            replacePrefixBy.map { newPrefix -> newPrefix + wordWithoutPrefix }
        }
        else emptyList()
    }
}


private val baseWordRules: List<BaseWordRule> = listOf(
    SuffixBaseWordRule("ied", "y"),
    SuffixBaseWordRule("ed", listOf("", "e")),

    SuffixBaseWordRule("ingly", listOf("", "e")),
    SuffixBaseWordRule("ly", listOf("", "e")),

    SuffixBaseWordRule("ing", listOf("", "e", "y")),
    SuffixBaseWordRule("ion", listOf("", "e", "y")),

    SuffixBaseWordRule("able", listOf("", "e", "l", "le")),
    SuffixBaseWordRule("ible", listOf("", "io", "e")),

    SuffixBaseWordRule("ence", listOf("", "e", "ent")),

    SuffixBaseWordRule("ieves", listOf("ief", "iev")),
    SuffixBaseWordRule("ves", listOf("f", "v")),

    SuffixBaseWordRule("ress", listOf("")),
    SuffixBaseWordRule("ous", listOf("", "e")),

    SuffixBaseWordRule("ies", listOf("y")),
    SuffixBaseWordRule("ees", listOf("ee")),
    SuffixBaseWordRule("es", listOf("", "e")),
    SuffixBaseWordRule("s", listOf("ss", "ass", "ious", "ous", "ess", "ess"), listOf("", "e")),

    SuffixBaseWordRule("ular", listOf("", "e", "ule", "le")),
    SuffixBaseWordRule("ar", listOf("ular"), listOf("", "e")),
    SuffixBaseWordRule("er", listOf("", "e")),
    SuffixBaseWordRule("or", listOf("", "e", "o")),

    PrefixBaseWordRule("a", ""),
    PrefixBaseWordRule("re", ""),
)


private val String.hasDoubledLastChar: Boolean get() =
    this.length >= 3 && this[this.length - 2] == this[this.length - 1]


/** Returns empty list if word is already base word. */
private fun possibleEnglishBaseWords(word: String): List<String> {
    val englishWord = word.trim().lowercase()

    return baseWordRules
        .asSequence()
        .flatMap { it.possibleBaseWords(englishWord) }
        .distinct()
        .sorted()
        .toList()
}


fun englishBaseWords(word: String, dictionary: Dictionary): List<CardWordEntry> {
    val foundWords: List<CardWordEntry> = possibleEnglishBaseWords(word)
        .asSequence()
        .map { baseWord ->
            try { dictionary.translateWord(baseWord) }
            catch (ignore: Exception) { CardWordEntry(baseWord, "") } }
        .filter { it.to.isNotBlank() }
        .toList()

    return foundWords
}
