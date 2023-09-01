package com.mvv.gui.words

import com.mvv.gui.dictionary.Dictionary
import com.mvv.gui.util.endsWithOneOf
import com.mvv.gui.util.startsWithOneOf


private interface BaseWordRule {
    fun possibleBaseWords(word: String): List<String>
}


private class SuffixBaseWordRule (val suffix: String, val excludeSuffixes: List<String>, val replaceSuffixBy: List<String>) : BaseWordRule {

    constructor(suffix: String, replaceSuffixBy: List<String>)
        : this(suffix, emptyList(), replaceSuffixBy)

    constructor(suffix: String, replaceSuffixBy: String)
        : this(suffix, listOf(replaceSuffixBy))

    override fun possibleBaseWords(word: String): List<String> {
        val suitableWord = word.length > suffix.length && word.endsWith(suffix) && !word.endsWithOneOf(excludeSuffixes)

        return if (suitableWord) {
            val wordWithoutSuffix = word.removeSuffix(suffix)

            val wordsWithoutSuffix: List<String> = if (wordWithoutSuffix.hasDoubledLastChar)
                listOf(wordWithoutSuffix, wordWithoutSuffix.substring(0, wordWithoutSuffix.length - 1))
                else listOf(wordWithoutSuffix)

            replaceSuffixBy.flatMap { newSuffix -> wordsWithoutSuffix.map { it + newSuffix } }
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
        val suitableWord = word.length > prefix.length && word.startsWith(prefix) && !word.startsWithOneOf(excludePrefixes)

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

    SuffixBaseWordRule("ly", listOf("", "e")),

    SuffixBaseWordRule("ing", listOf("", "e", "y")),
    SuffixBaseWordRule("ion", listOf("", "e", "y")),

    SuffixBaseWordRule("able", listOf("", "e", "l", "le")),
    SuffixBaseWordRule("ible", listOf("", "io")),

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



//private val derivedWordSuffixes = listOf("ed", "ing", "es", "s", "er")

private val String.hasDoubledLastChar: Boolean get() =
    this.length >= 3 && this[this.length - 2] == this[this.length - 1]


val String.mayBeDerivedWord: Boolean get() =
    //derivedWordSuffixes.any { suffix -> this.length > suffix.length + 2 && this.endsWith(suffix) }
    baseWordRules.map { this }.any()


/** Returns empty list if word is already base word. */
fun possibleEnglishBaseWords(word: String): List<String> {
    val englishWord = word.trim().lowercase()

    return baseWordRules
        .asSequence()
        .flatMap { it.possibleBaseWords(englishWord) }
        .distinct()
        .sorted()
        .toList()

    /*
    val baseWords = mutableListOf<String>()

    val baseWord = derivedWordSuffixes
        .find { suffix -> englishWord.endsWith(suffix) }
        ?.let { suffix -> englishWord.removeSuffix(suffix) }
        ?.trimToNull()
        ?: return emptyList()

    baseWords.add(baseWord)
    if (baseWord.hasDoubledLastChar) {
        baseWords.add(baseWord.substring(0, baseWord.length - 1))
    }

    return baseWords
        //.flatMap { w -> if (w.endsWith('e')) listOf(w) else listOf(w, w + 'e') }
        .flatMap { w -> if (w.endsWith("ee")) listOf(w) else listOf(w, w + 'e') }
    */
}


fun possibleBestEnglishBaseWord(word: String): String? =
    possibleEnglishBaseWords(word).minOfOrNull { it }


fun englishBaseWords(word: String, dictionary: Dictionary): List<CardWordEntry> {
    val foundWords: List<CardWordEntry> = possibleEnglishBaseWords(word)
        .asSequence()
        .map { baseWord ->
            try { dictionary.translateWord(baseWord) }
            catch (ignore: Exception) { CardWordEntry(baseWord, "") } }
        .filter { it.to.isNotBlank() }
        .toList()

    //val baseWords: List<CardWordEntry> = foundWords.ifEmpty {
    //    possibleBestEnglishBaseWord(word)?.let { listOf(CardWordEntry(it, "")) } ?: emptyList() }
    //
    //return baseWords.sortedBy { it.from.lowercase() }
    return foundWords
}
