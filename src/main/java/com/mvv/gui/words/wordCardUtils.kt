package com.mvv.gui.words

import com.mvv.gui.cardeditor.isGoodLearnCardCandidate
import com.mvv.gui.cardeditor.parseToCard
import com.mvv.gui.util.containsEnglishLetters


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
internal val CharSequence.examplesCount: Int get() {
    if (this.isBlank()) return 0

    return this
        .splitToSequence("\n")
        .filter { it.isNotBlank() }
        .filter { it.containsEnglishLetters() }
        .count()
}


internal val CharSequence.exampleNewCardCandidateCount: Int get() {
    if (this.isBlank()) return 0

    return this
        .splitToSequence("\n")
        .filter { it.isNotBlank() }
        .filter { it.containsEnglishLetters() }
        .filter { it.parseToCard()?.isGoodLearnCardCandidate() ?: false }
        .count()
}
