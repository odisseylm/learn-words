package com.mvv.gui.words

import com.mvv.gui.dictionary.Dictionary
import com.mvv.gui.dictionary.extractExamples
import com.mvv.gui.util.*
import com.mvv.gui.words.WordCardStatus.NoBaseWordInSet
import javafx.collections.ObservableList
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.input.DataFormat
import java.io.FileReader
import java.io.Reader
import java.io.StringReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.util.Optional
import java.util.TreeMap
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.notExists
import kotlin.math.min


private val log = mu.KotlinLogging.logger {}


internal fun addBaseWordsInSet(wordCardsToProcess: Iterable<CardWordEntry>,
                               warnAboutMissedBaseWordsMode: WarnAboutMissedBaseWordsMode,
                               allWordCards: ObservableList<CardWordEntry>,
                               dictionary: Dictionary): Map<CardWordEntry, List<CardWordEntry>> {

    val allWordCardsMap: Map<String, CardWordEntry> = allWordCards.associateBy { it.from.trim().lowercase() }

    val withoutBaseWord = wordCardsToProcess
        .asSequence()
        .filter { NoBaseWordInSet in it.statuses }
        .toSortedSet(cardWordEntryComparator)

    val baseWordsToAddMap: Map<CardWordEntry, List<CardWordEntry>> = withoutBaseWord
        .asSequence()
        .map { card -> Pair(card, englishBaseWords(card.from, dictionary, card)) }
        .filter { it.second.isNotEmpty() }
        .associate { it }

    baseWordsToAddMap.forEach { (currentWordCard, baseWordCards) ->
        // T O D O: optimize this n*n
        val index = allWordCards.indexOf(currentWordCard)
        baseWordCards
            .filterNot { allWordCardsMap.containsKey(it.from) }
            .forEachIndexed { i, baseWordCard ->
                allWordCards.add(index + i, baseWordCard)
        }
    }
    analyzeWordCards(withoutBaseWord, warnAboutMissedBaseWordsMode, allWordCards, dictionary)

    return baseWordsToAddMap
}


/** Returns true if any transcription is added. */
internal fun addTranscriptions(wordCards: MutableList<CardWordEntry>, dictionary: Dictionary): Boolean {
    val cardsWithoutTranscription = wordCards.filter { it.transcription.isBlank() }

    cardsWithoutTranscription.forEach { card ->
        getTranscription(card.from, dictionary)
            .ifPresent { card.transcription = it }
    }

    return cardsWithoutTranscription.any { it.transcription.isNotBlank() }
}


private fun getTranscription(word: String, dictionary: Dictionary): Optional<String> =
    try { Optional.ofNullable(
        dictionary.translateWord(word).transcription.trimToNull()) }
    catch (ex: Exception) {
        log.warn(ex) { "Error of getting transcription for [${word}]." }
        Optional.empty() }


internal fun Dictionary.translateWord(word: String): CardWordEntry =
    CardWordEntry(word, "").also { this.translateWord(it) }


internal fun Dictionary.translateWord(card: CardWordEntry) {
    val translation = this.find(card.from.trim())

    card.to            = translation.translations.joinToString("\n")
    card.transcription = translation.transcription ?: ""
    card.examples      = extractExamples(translation)
}


internal fun Dictionary.translateWords(words: Iterable<CardWordEntry>) =
    words
        .filter  { it.from.isNotBlank() }
        .forEach {
            if (it.to.isBlank())
                this.translateWord(it)
        }


internal fun copyWordsToClipboard(words: Iterable<CardWordEntry>) {

    val wordCardsAsString = words
        .joinToString("\n") { "${it.from}  ${it.to}" }

    val clipboardContent = ClipboardContent()
    clipboardContent.putString(wordCardsAsString)
    Clipboard.getSystemClipboard().setContent(clipboardContent)
}


internal fun wordCardsToLowerCaseRow(wordCards: Iterable<CardWordEntry>) {
    val it = wordCards.iterator()

    it.forEach {
        it.from = it.from.lowercase()
        it.to   = it.to.lowercase()
    }
}



@Suppress("unused")
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


fun mergeDuplicates(cards: Iterable<CardWordEntry>): List<CardWordEntry> {
    val grouped = cards.groupByTo(TreeMap(String.CASE_INSENSITIVE_ORDER)) { it.from }
    return grouped.entries.asSequence().map { mergeCards(it.value) }.toList()
}

fun mergeCards(cards: List<CardWordEntry>): CardWordEntry {

    if (cards.size == 1) return cards.first()

    fun Iterable<CardWordEntry>.merge(delimiter: String, getter: (CardWordEntry)->String): String =
        this.map(getter).distinct().filter { it.isNotBlank() }.joinToString(delimiter)

    fun <T> Iterable<CardWordEntry>.mergeList(getter: (CardWordEntry)->List<T>): List<T> =
        this.flatMap(getter).distinct().toList()

    fun <T> Iterable<CardWordEntry>.mergeSet(getter: (CardWordEntry)->Set<T>): Set<T> =
        this.flatMap(getter).toSet()


    val card = CardWordEntry(
        mergeFrom(cards.map { it.from }),
        cards.merge("\n") { it.to },
    )

    card.fromWithPreposition = cards.merge("  ") { it.fromWithPreposition }
    card.transcription    = cards.merge(" ")  { it.transcription }
    card.examples         = cards.flatMap { it.examples.splitExamples() }.filterNotBlank()
                                 .distinct()
                                 .joinToString("\n\n")
                                 .ifNotBlank { it + "\n\n" }
    card.statuses         = cards.mergeSet    { it.statuses }
    card.predefinedSets   = cards.mergeSet    { it.predefinedSets }
    card.sourcePositions  = cards.mergeList   { it.sourcePositions }
    card.sourceSentences  = cards.merge("\n") { it.sourceSentences }
    card.missedBaseWords  = cards.mergeList   { it.missedBaseWords }
    card.file  = cards.map { it.file }.firstOrNull()

    return card
}

internal fun String.splitExamples(): List<String> {
    if (this.isBlank()) return emptyList()

    val lines = this.split("\n")
    if (lines.isEmpty()) return emptyList()

    val separatedExamples = mutableListOf<String>()
    var currentExample = ""

    for (line in lines) {

        val lineIsBlank = line.isBlank()
        val continueOfPreviousExample = !lineIsBlank && line.startsWith(' ')

        if (lineIsBlank) { // end of previous example
            if (currentExample.isNotBlank())
                separatedExamples.add(currentExample.trim())
            currentExample = ""
        }

        else if (continueOfPreviousExample) { // next line of previous comment
            currentExample += "\n"
            currentExample += line.trimEnd()
        }

        else { // just next example
            if (currentExample.isNotBlank())
                separatedExamples.add(currentExample.trim())
            currentExample = line.trim()
        }
    }

    if (currentExample.isNotBlank())
        separatedExamples.add(currentExample.trim())

    return separatedExamples
}

private fun mergeFrom(froms: List<String>): String {

    val suffixes = listOf("s", "d", "'s", "es", "ed", "ing")

    var merged = froms.map { it.lowercase() }.filterNotEmpty().distinct().joinToString(" ")

    if (froms.size == 2) {
        val first  = froms[0]
        val second = froms[1]

        if (first == second) return first

        // T O D O: add support of 2nd/3rd form of irregular verbs

        for (suffix in suffixes) {
            if ((first + suffix).equals(second, ignoreCase = true)) merged = first
            if (first.equals(second + suffix, ignoreCase = true)) merged = second
        }
    }

    return merged
}

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
        .use { r -> extractWordsFromText_New(r.readText(), sentenceEndRule, ignoredWords) } // T O D O: would be better to pass lazy CharSequence instead of loading full text as String

private fun predictFileEncoding(filePath: Path): Charset {
    val bytes = filePath.readBytes(min(10U, Files.size(filePath).toUInt()))

    val feByte = 0xFE.toByte()
    val ffByte = 0xFF.toByte()

    return if (bytes.size >= 2 && (
                   (bytes[0] == feByte && bytes[1] == ffByte)
                || (bytes[1] == feByte && bytes[0] == ffByte)))
           Charsets.UTF_16
           else Charsets.UTF_8
}

internal fun extractWordsFromFileAndMerge(filePath: Path, sentenceEndRule: SentenceEndRule, toIgnoreWords: Set<String>, preProcessor: (Reader)->Reader = { it }): List<CardWordEntry> =
    mergeDuplicates(extractWordsFromFile(filePath, sentenceEndRule, toIgnoreWords, preProcessor))

internal fun extractWordsFromSrtFileAndMerge(filePath: Path, toIgnoreWords: Set<String>): List<CardWordEntry> =
    extractWordsFromFileAndMerge(filePath, SentenceEndRule.ByEndingDot, toIgnoreWords) { StringReader(loadOnlyTextFromSrt(it)) }



internal fun extractWordsFromClipboard(clipboard: Clipboard, sentenceEndRule: SentenceEndRule, ignoredWords: Set<String>): List<CardWordEntry> {

    val content = clipboard.getContent(DataFormat.PLAIN_TEXT)

    log.info("clipboard content: [${content}]")
    if (content == null) return emptyList()

    //val words = extractWordsFromText(content.toString(), ignoredWords)
    val words = mergeDuplicates(
        extractWordsFromText_New(content.toString(), sentenceEndRule, ignoredWords))

    log.info("clipboard content as words: $words")
    return words
}




fun getAllExistentSetFiles(includeMemoWordFile: Boolean, toIgnoreBaseWordsFilename: String?): List<Path> {
    if (dictDirectory.notExists()) return emptyList()

    return dictDirectory.toFile().walkTopDown()
        .asSequence()
        .map { it.toPath() }
        .filter { it.isRegularFile() }
        .filter { it != ignoredWordsFile }
        .filter { it.isInternalCsvFormat || (includeMemoWordFile && it.isMemoWordFile) }
        .filter { toIgnoreBaseWordsFilename.isNullOrBlank() || !it.name.contains(toIgnoreBaseWordsFilename) }
        .toList()
}


internal fun loadWordsFromAllExistentDictionaries(baseWordsFilename: String?): List<String> {

    val allWordsFilesExceptIgnored = getAllExistentSetFiles(includeMemoWordFile = true, toIgnoreBaseWordsFilename = baseWordsFilename)

    return allWordsFilesExceptIgnored
        .asSequence()
        .map { loadWordCards(it) }
        .flatMap { it }
        .map { it.from }
        .distinctBy { it.lowercase() }
        .toList()
}


fun removeWordsFromOtherSetsFromCurrentWords(currentWords: MutableList<CardWordEntry>, currentWordsFile: Path?): List<CardWordEntry> {

    val toRemove = loadWordsFromAllExistentDictionaries(currentWordsFile?.baseWordsFilename)
    val toRemoveAsSet = toRemove.asSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toSortedSet(String.CASE_INSENSITIVE_ORDER)

    val currentToRemove = currentWords.filter { it.from.trim() in toRemoveAsSet }

    // perform removing as ONE operation to minimize change events
    currentWords.removeAll(currentToRemove)

    return currentToRemove
}
