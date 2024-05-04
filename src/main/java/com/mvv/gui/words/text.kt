package com.mvv.gui.words

import com.mvv.gui.cardeditor.actions.mergeDuplicates
import com.mvv.gui.util.predictFileEncoding
import java.io.FileReader
import java.io.Reader
import java.io.StringReader
import java.nio.file.Path


data class Part (
    val content: CharSequence,
    val inBrackets: Boolean,
    // content
    val from: Int,
    /** Exclusive */
    val to: Int,
    // bracket indices
    val openBracketIndex: Int,
    val closingBracketIndex: Int,
) {
    fun asSubContent(): CharSequence = if (inBrackets) content.subSequence(openBracketIndex, closingBracketIndex + 1)
    else content.subSequence(from, to)
    fun asTextOnly(): CharSequence = content.subSequence(from, to)

    companion object {
        fun inBrackets(
            content: CharSequence,
            // bracket indices
            openBracketIndex: Int,
            closingBracketIndex: Int,
        ) = Part(content, true, openBracketIndex + 1, closingBracketIndex, openBracketIndex, closingBracketIndex)
        fun withoutBrackets(
            content: CharSequence,
            from: Int,
            /** Exclusive */
            to: Int,
        ) = Part(content, false, from, to, -1, -1)
    }
}


val Part.withoutBrackets: Boolean get() = !this.inBrackets


fun CharSequence.splitByBrackets(): List<Part> {

    val parts = mutableListOf<Part>()
    var bracketLevel = 0
    var bracketPartStart = -1
    var withoutBracketPartStart = 0

    for (i in this.indices) {
        val ch = this[i]

        if (ch == '(') {
            bracketLevel++

            if (bracketLevel == 1) {
                if (i > withoutBracketPartStart) {
                    parts.add(Part.withoutBrackets(this, withoutBracketPartStart, i))
                    withoutBracketPartStart = -1
                }

                bracketPartStart = i
            }
        }

        if (ch == ')') {
            bracketLevel--

            if (bracketLevel == 0) {
                parts.add(Part.inBrackets(this, bracketPartStart, i))
                bracketPartStart = -1
                withoutBracketPartStart = i + 1
            }
        }
    }

    if (withoutBracketPartStart != -1 && withoutBracketPartStart < this.lastIndex) {
        parts.add(Part.withoutBrackets(this, withoutBracketPartStart, this.length))
    }

    return parts
}


@Suppress("unused", "FunctionName")
internal fun extractWordsFromText_Old(content: CharSequence, ignoredWords: Collection<String>): List<CardWordEntry> =
    TextParser()
        .parse(content)
        .asSequence()
        .filter { !ignoredWords.contains(it) }
        .map { CardWordEntry(it, "") }
        .toList()


internal fun extractWordsFromText_New(content: CharSequence, sentenceEndRule: SentenceEndRule, ignoredWords: Set<String>): List<CardWordEntry> =
    TextParserEx(sentenceEndRule)
        .parse(content)
        .asSequence()
        .flatMap { extractNeededWords(it) }
        .flatMap { it.toCardWordEntries() }
        .filter  { it.from !in ignoredWords }
        .toList()


private data class WordInternal (
    val word: WordEntry,
    val wordWithPreposition: WordEntry?,
) {
    fun toCardWordEntries(): List<CardWordEntry> =
        if (wordWithPreposition == null)
            listOf(fillCardFrom(word))
        else
            listOf(fillCardFrom(word), fillCardFrom(wordWithPreposition))
                .onEach { it.fromWithPreposition = wordWithPreposition.word }
}

private fun fillCardFrom(word: WordEntry): CardWordEntry =
    CardWordEntry(word.word, "").also {
        it.sourcePositions = listOf(word.position)
        it.sourceSentences = word.sentence.text.toString().replace('\n', ' ').replace("  ", " ") // TODO: make sure that the same String instance is used
    }

fun WordEntry.lowercase(): WordEntry =
    WordEntry(this.word.lowercase(), this.position, this.sentence)

private fun extractNeededWords(sentence: Sentence): List<WordInternal> {

    val result = mutableListOf<WordInternal>()

    sentence.allTokens.forEachIndexed { index, wordEntry ->
        if (wordEntry is WordEntry && wordEntry.word.isTranslatable) {
            val preposition: String? = extractPreposition(sentence.allTokens, index + 1)

            result.add(WordInternal(
                wordEntry,
                preposition?.let { WordEntry(wordEntry.word.lowercase() + " " + preposition, wordEntry.position, wordEntry.sentence) }
            ))
        }
    }

    return result
}

fun extractPreposition(words: List<Token>, tokenSequenceIndex: Int): String? {
    val possiblePrepositions: List<WordSequence> = prepositions
    val preposition: WordSequence? = possiblePrepositions.find { words.containsWordSequence(tokenSequenceIndex, it) }
    return preposition?.asText
}

private fun List<Token>.containsWordSequence(tokenSequenceIndex: Int, wordSequence: WordSequence): Boolean {

    if (this.lastIndex < tokenSequenceIndex + wordSequence.size) return false

    var j = tokenSequenceIndex
    for (i in wordSequence.indices) {

        // skip 'space' tokens
        while (j < this.size && this[j].text.isBlank()) j++

        if (j >= this.size || this[j].text != wordSequence[i]) return false
        j++
    }

    return true
}


internal fun extractWordsFromFile(filePath: Path, sentenceEndRule: SentenceEndRule, ignoredWords: Set<String>, preProcessor: (Reader)-> Reader): List<CardWordEntry> =
    preProcessor(FileReader(filePath.toFile(), predictFileEncoding(filePath)))
        .use { r -> extractWordsFromText_New(r.readText(), sentenceEndRule, ignoredWords) }


internal fun extractWordsFromFileAndMerge(filePath: Path, sentenceEndRule: SentenceEndRule, toIgnoreWords: Set<String>, preProcessor: (Reader)->Reader = { it }): List<CardWordEntry> =
    mergeDuplicates(extractWordsFromFile(filePath, sentenceEndRule, toIgnoreWords, preProcessor))

internal fun extractWordsFromSrtFileAndMerge(filePath: Path, toIgnoreWords: Set<String>): List<CardWordEntry> =
    extractWordsFromFileAndMerge(filePath, SentenceEndRule.ByEndingDot, toIgnoreWords) { StringReader(loadOnlyTextFromSrt(it)) }
