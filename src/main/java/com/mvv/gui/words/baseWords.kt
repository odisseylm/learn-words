package com.mvv.gui.words

import com.mvv.gui.dictionary.Dictionary
import com.mvv.gui.util.endsWithOneOf
import com.mvv.gui.util.startsWithOneOf


private interface BaseWordRule {
    fun possibleBaseWords(word: String): List<String>
}


private const val minBaseWordLength = 3

// actually make sense to put there only words with length >= 3
val excludeBaseWords = setOf(
    "a", "an", "the",
    "adv", "alt",
    "to", "out", "off", "like", "for", "via", "with", "near", "with",
    "red", "black",
    "self",
)


private class SuffixBaseWordRule (suffix: String, excludeSuffixes: List<String>, replaceSuffixBy: List<String>) : BaseWordRule {

    val suffix: String = suffix.removePrefix("-")
    val excludeSuffixes: List<String> = excludeSuffixes.map { it.removePrefix("-") }
    val replaceSuffixBy: List<String> = replaceSuffixBy.map { it.removePrefix("-") }

    constructor(suffix: String, replaceSuffixBy: List<String>)
        : this(suffix, emptyList(), replaceSuffixBy)

    override fun possibleBaseWords(word: String): List<String> {
        val suitableWord = word.length >= suffix.length + minBaseWordLength - 1
                && word.endsWith(suffix) && !word.endsWithOneOf(excludeSuffixes)

        return if (suitableWord) {
            val wordWithoutSuffix = word.removeSuffix(suffix)

            val wordsWithoutSuffix: List<String> = if (wordWithoutSuffix.hasDoubledLastChar)
                listOf(wordWithoutSuffix, wordWithoutSuffix.substring(0, wordWithoutSuffix.length - 1))
                else listOf(wordWithoutSuffix)

            replaceSuffixBy
                .asSequence()
                .flatMap { newSuffix -> wordsWithoutSuffix.map { it + newSuffix } }
                .map { it.removePrefix("-").removeSuffix("-") }
                .filter { it.length >= minBaseWordLength }
                .filter { it !in excludeBaseWords }
                .distinct()
                .toList()
        }
        else emptyList()
    }
}

private class PrefixBaseWordRule (prefix: String, excludePrefixes: List<String>, replacePrefixBy: List<String>) : BaseWordRule {

    val prefix: String = prefix.removeSuffix("-")
    val excludePrefixes: List<String> = excludePrefixes.map { it.removeSuffix("-") }
    val replacePrefixBy: List<String> = replacePrefixBy.map { it.removeSuffix("-") }

    constructor(prefix: String, replacePrefixBy: List<String>)
        : this(prefix, emptyList(), replacePrefixBy)

    override fun possibleBaseWords(word: String): List<String> {
        val suitableWord = word.length > prefix.length + minBaseWordLength
                && word.startsWith(prefix) && !word.startsWithOneOf(excludePrefixes)

        return if (suitableWord) {
            val wordWithoutPrefix = word.removePrefix(prefix)
            replacePrefixBy
                .asSequence()
                .map { newPrefix -> newPrefix + wordWithoutPrefix }
                .map { it.removePrefix("-").removeSuffix("-") }
                .filter { it.length >= minBaseWordLength }
                .filter { it !in excludeBaseWords }
                .distinct()
                .toList()
        }
        else emptyList()
    }
}


private val baseWordRules: List<BaseWordRule> = listOf(
    rule("-ied", "y"),
    rule("-ed"),

    rule("-ingly"),
    rule("-ly"),

    rule("-ing", "y"),
    rule("-ion", "y"),
    rule("-ism", "y"),
    rule("-ist", "y"),
    rule("-ish", "y"),
    rule("-ity", "y"),
    rule("-ty", "y"),

    rule("-ian", "y"),

    rule("-ment", "y"),
    rule("-ness", "y"),
    rule("-ship", "y"),
    rule("-hood", "y"),
    rule("-tion", "y"),
    rule("-cion", "y"),
    rule("-sion", "y"),

    rule("-age"),

    rule("-able", "l", "le"),
    rule("-ible", "io"),

    rule("-ent"),
    rule("-ant"),

    rule("-ence", "ent", "ant"),
    rule("-ance", "ent", "ant"),
    rule("-ency", "ent", "ant"),
    rule("-ancy", "ent", "ant"),

    rule("-ieves", "ief", "iev"),
    rule("-ves", "f", "v"),

    rule("-ress"),
    rule("-ous"),

    rule("-ies", "y"),
    rule("-ees", "ee"),
    rule("-es"),
    SuffixBaseWordRule("-s", listOf("ss", "ass", "ious", "ous", "ess", "ess"), listOf("", "e")),

    rule("-ular", "ule", "le"),
    SuffixBaseWordRule("-ar", listOf("ular"), listOf("", "e")),
    rule("-er"),
    rule("-or", "o"),
    rule("-eer"),

    rule("-ard"),
    rule("-art"),

    rule("-ary"),
    rule("-ory", "o"),
    rule("-ery"),
    rule("-ry", "o"),
    rule("-ure", "o"),

    rule("-est"),

    rule("-dom"),

    rule("-ee"),
    rule("-ese"),
    rule("-ess"),

    rule("-ful"),
    rule("-full"),
    rule("-like"),
    rule("-ic"),
    rule("-ical"),
    rule("-less"),
    rule("-ern"),

    rule("-ate"),
    rule("-en"),
    rule("-ify"),
    rule("-efy"),
    rule("-ise"),
    rule("-ize"),
    rule("-ish"),
    rule("-yse"),
    rule("-yze"),

    rule("-wise"),
    rule("-way"),
    rule("-ways"),
    rule("-ward"),
    rule("-wards"),

    rule("-logy"),
    rule("-man"),

    // By pPrefix
    //
    rule("a-"),
    rule("re-"),
    rule("un-"),
    rule("dis-"),
    rule("mis-"),
    rule("il-"),
    rule("im-"),
    rule("in-"),
    rule("ir-"),
    rule("de-"),
    rule("di-"),
    rule("re-"),
    rule("over-"),
    rule("under-"),
    rule("extra-"),
    rule("post-"),
    rule("pre-"),
    rule("anti-"),
    rule("ex-"),
    rule("out-"),
    rule("sub-"),
    rule("co-"),
    rule("mid-"),
    rule("hyper-"),
    rule("hypo-"),
    rule("inter-"),
    rule("auto-"),
    rule("para-"),
    rule("multi-"),
    rule("trans-"),
    rule("super-"),
    rule("uni-"),
    rule("bi-"),
    rule("tri-"),
    rule("tetra-"),
    rule("fore-"),
    rule("neo-"),
    rule("peri-"),
    rule("tele-"),
    rule("contra-"),
    rule("mis-"),
    rule("non-"),
    rule("uni-"),

    rule("con-"),
    rule("com-"),
    rule("col-"),
    rule("cor-"),
    rule("down-"),
    rule("for-"),
    rule("fore-"),
    rule("forth-"),
    rule("male-"),
    rule("off-"),
    rule("on-"),
    rule("out-"),
    rule("over-"),
)


private fun rule(prefixOrSuffix: String, vararg replaceBy: String): BaseWordRule {
    val replaceByList: List<String> = (replaceBy.toList() + "" + "e").distinct()
    return when {
        prefixOrSuffix.startsWith('-') -> SuffixBaseWordRule(prefixOrSuffix.removeSuffix("-"), replaceByList)
        prefixOrSuffix.endsWith('-') -> PrefixBaseWordRule(prefixOrSuffix.removeSuffix("-"), replaceByList)
        else -> throw IllegalArgumentException("Unexpected prefix/suffix '$prefixOrSuffix'.")
    }
    
}


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
