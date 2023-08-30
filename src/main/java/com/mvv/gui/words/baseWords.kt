package com.mvv.gui.words

import com.mvv.gui.dictionary.Dictionary
import com.mvv.gui.util.trimToNull


private val derivedWordSuffixes = listOf("ed", "ing", "es", "s", "er")

private val String.hasDoubledLastChar: Boolean get() =
    this.length >= 3 && this[this.length - 2] == this[this.length - 1]


val String.mayBeDerivedWord: Boolean get() =
    derivedWordSuffixes.any { suffix -> this.length > suffix.length + 2 && this.endsWith(suffix) }


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

    val baseWords: List<CardWordEntry> = foundWords.ifEmpty {
        possibleBestEnglishBaseWord(word)?.let { listOf(CardWordEntry(it, "")) } ?: emptyList() }

    return baseWords.sortedBy { it.from.lowercase() }
}
