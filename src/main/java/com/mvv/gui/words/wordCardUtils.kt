package com.mvv.gui.words

import com.mvv.gui.cardeditor.actions.isGoodLearnCardCandidate
import com.mvv.gui.cardeditor.actions.parseToCard
import com.mvv.gui.util.containsEnglishLetters
import com.mvv.gui.util.isOneOf
import com.mvv.gui.util.splitToWords
import com.mvv.gui.util.startsWithOneOf
import java.util.TreeSet


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

        when {
            ch == '(' ->
                bracketLevel++

            ch == ')' && bracketLevel > 0 -> {
                bracketLevel--
                if (bracketLevel == 0) lastNonSpaceIndex = i
            }

            else ->
                if (bracketLevel == 0) {
                    if (ch == '\n' || ch == ',' || ch == ';') {
                        if (firstNonSpaceIndex != -1) translations.add(Range(firstNonSpaceIndex, lastNonSpaceIndex))
                        firstNonSpaceIndex = -1
                        lastNonSpaceIndex = -1
                    } else if (!ch.isWhitespace()) {
                        if (firstNonSpaceIndex == -1) firstNonSpaceIndex = i
                        lastNonSpaceIndex = i
                    }
                }
        }
    }

    if (bracketLevel == 0 && firstNonSpaceIndex != -1) translations.add(Range(firstNonSpaceIndex, lastNonSpaceIndex))

    return translations.map { this.subSequence(it.first, it.last + 1) }
}


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


fun guessPartOfSpeech(card: CardWordEntry): PartOfSpeech = guessPartOfSpeech(card.from, card.to)

fun guessPartOfSpeech(from: String, to: String): PartOfSpeech {
    val guessedByFrom = guessPartOfSpeech(from)
    return if (guessedByFrom.isOneOf(PartOfSpeech.Word, PartOfSpeech.Phrase) && to.areAllVerbs)
                PartOfSpeech.Verb
           else guessedByFrom
}

fun guessPartOfSpeech(wordOrPhrase: String): PartOfSpeech {
    val allWords = wordOrPhrase.splitToWords()
    val mainWords = allWords.filterNot { it in ignorableWords }
    val mainWordCount = mainWords.size

    return when {
        allWords.size <= 1 -> PartOfSpeech.Word

        wordOrPhrase.startsWithOneOf("an ", "a ", "the ", ignoreCase = true) && mainWordCount <= 1
            -> PartOfSpeech.Noun

        wordOrPhrase.startsWithOneOf("to ") && mainWordCount <= 1
            -> PartOfSpeech.Verb

        allWords.size > 2 -> PartOfSpeech.Phrase

        else -> PartOfSpeech.Word
    }
}


private val ignorableWords: Set<String> by lazy {
    (listOf(
            "to",
            "somebody", "smb.", "smb",
            "something", "smth.", "smth", "smt.", "smt",
        ) +
        prepositions.flatMap { it.words }
    )
    .toCollection(TreeSet<String>(String.CASE_INSENSITIVE_ORDER))
}
