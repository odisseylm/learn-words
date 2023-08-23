package com.mvv.gui



private val derivedWordSuffixes = listOf("ed", "ing", "es", "s", "er")

private val String.hasDoubledLastChar: Boolean get() =
    this.length >= 3 && this[this.length - 2] == this[this.length - 1]


val String.mayBeDerivedWord: Boolean get() = this.endsWithOneOf(derivedWordSuffixes)


/** Returns empty list if word is already base word. */
fun possibleEnglishBaseWords(word: String): List<String> {
    val englishWord = word.trim().lowercase()

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
        .flatMap { w -> if (w.endsWith('e')) listOf(w) else listOf(w, w + 'e') }
}
