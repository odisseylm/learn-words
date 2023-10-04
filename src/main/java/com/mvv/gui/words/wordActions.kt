package com.mvv.gui.words

import com.mvv.gui.dictionary.Dictionary
import com.mvv.gui.dictionary.extractExamples
import com.mvv.gui.util.trimToNull
import com.mvv.gui.words.WordCardStatus.NoBaseWordInSet
import javafx.collections.ObservableList
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.input.DataFormat
import java.io.FileReader
import java.io.Reader
import java.nio.file.Files
import java.nio.file.Path
import java.util.Optional
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.notExists
import kotlin.streams.asSequence


private val log = mu.KotlinLogging.logger {}


internal fun addBaseWordsInSet(wordCardsToProcess: Iterable<CardWordEntry>,
                               warnAboutMissedBaseWordsMode: WarnAboutMissedBaseWordsMode,
                               allWordCards: ObservableList<CardWordEntry>,
                               dictionary: Dictionary): Map<CardWordEntry, List<CardWordEntry>> {

    val allWordCardsMap: Map<String, CardWordEntry> = allWordCards.associateBy { it.from.trim().lowercase() }

    val withoutBaseWord = wordCardsToProcess
        .asSequence()
        .filter { NoBaseWordInSet in it.wordCardStatuses }
        .toSortedSet(cardWordEntryComparator)

    val baseWordsToAddMap: Map<CardWordEntry, List<CardWordEntry>> = withoutBaseWord
        .asSequence()
        .map { card -> Pair(card, englishBaseWords(card.from, dictionary)) }
        .filter { it.second.isNotEmpty() }
        .associate { it }

    baseWordsToAddMap.forEach { (currentWordCard, baseWordCards) ->
        // TODO: optimize this n*n
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


internal fun extractWordsFromText_New(content: CharSequence, sentenceEndRule: SentenceEndRule, ignoredWords: Collection<String>): List<CardWordEntry> =
    TextParserEx(sentenceEndRule)
        .parse(content)
        .asSequence()
        .flatMap { extractNeededWords(it) }
        .flatMap { it.toCardWordEntries() }
        .filter  { !ignoredWords.contains(it.from) }
        .toList()


fun mergeDuplicates(cards: Iterable<CardWordEntry>): List<CardWordEntry> {
    val grouped = cards.groupByTo(LinkedHashMap()) { it.from }
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
        cards.first().from,
        cards.merge("\n") { it.to },
    )

    card.fromWithPreposition = cards.merge("\n") { it.fromWithPreposition }
    card.transcription    = cards.merge(" ")  { it.transcription }
    card.examples         = cards.merge("\n") { it.examples }
    card.wordCardStatuses = cards.mergeSet    { it.wordCardStatuses }
    card.predefinedSets   = cards.mergeSet    { it.predefinedSets }
    card.sourcePositions  = cards.mergeList   { it.sourcePositions }
    card.sourceSentences  = cards.merge("\n") { it.sourceSentences }
    card.missedBaseWords  = cards.mergeList   { it.missedBaseWords }

    return card
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


private fun extractNeededWords(sentence: Sentence): List<WordInternal> {

    val result = mutableListOf<WordInternal>()

    sentence.allWords.forEachIndexed { index, wordEntry ->
        if (wordEntry.word.isTranslatable) {
            val preposition: String? = extractPreposition(sentence.allWords, index + 1)

            result.add(WordInternal(
                wordEntry,
                preposition?.let { WordEntry(wordEntry.word + " " + preposition, wordEntry.position, wordEntry.sentence) }
            ))
        }
    }

    return result
}

fun extractPreposition(words: List<WordEntry>, index: Int): String? {
    val possiblePrepositions: List<WordSequence> = prepositions
    val preposition: WordSequence? = possiblePrepositions.find { words.containsWordSequence(index, it) }
    return preposition?.asText
}

private fun List<WordEntry>.containsWordSequence(sequenceIndex: Int, wordSequence: WordSequence): Boolean {

    if (this.lastIndex < sequenceIndex + wordSequence.size) return false

    for (i in wordSequence.indices) {
        if (this[sequenceIndex + i].word != wordSequence[i]) return false
    }

    return true
}


internal fun extractWordsFromFile(filePath: Path, sentenceEndRule: SentenceEndRule, ignoredWords: Collection<String>, preProcessor: (Reader)-> Reader): List<CardWordEntry> =
    preProcessor(FileReader(filePath.toFile(), Charsets.UTF_8))
        .use { r -> extractWordsFromText_New(r.readText(), sentenceEndRule, ignoredWords) } // T O D O: would be better to pass lazy CharSequence instead of loading full text as String


internal fun extractWordsFromClipboard(clipboard: Clipboard, sentenceEndRule: SentenceEndRule, ignoredWords: Collection<String>): List<CardWordEntry> {

    val content = clipboard.getContent(DataFormat.PLAIN_TEXT)

    log.info("clipboard content: [${content}]")
    if (content == null) return emptyList()

    //val words = extractWordsFromText(content.toString(), ignoredWords)
    val words = mergeDuplicates(
        extractWordsFromText_New(content.toString(), sentenceEndRule, ignoredWords))

    log.info("clipboard content as words: $words")
    return words
}



internal fun loadWordsFromAllExistentDictionaries(baseWordsFilename: String?): List<String> {

    if (dictDirectory.notExists()) return emptyList()

    val allWordsFilesExceptIgnored = Files.list(dictDirectory)
        .asSequence()
        .filter { it.isRegularFile() }
        .filter { it != ignoredWordsFile }
        .filter { it.isInternalCsvFormat || it.isMemoWordFile }
        .filter { baseWordsFilename.isNullOrBlank() || !it.name.contains(baseWordsFilename) }
        .sorted()
        .toList()

    return allWordsFilesExceptIgnored
        .asSequence()
        .map { loadWordCards(it) }
        .flatMap { it }
        .map { it.from }
        .distinctBy { it.lowercase() }
        .toList()
}


fun removeWordsFromOtherSetsFromCurrentWords(currentWords: MutableList<CardWordEntry>, currentWordsFile: Path?) {

    val toRemove = loadWordsFromAllExistentDictionaries(currentWordsFile?.baseWordsFilename)
    val toRemoveAsSet = toRemove.asSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toSortedSet(String.CASE_INSENSITIVE_ORDER)

    val currentToRemove = currentWords.filter { it.from.trim() in toRemoveAsSet }

    // perform removing as ONE operation to minimize change events
    currentWords.removeAll(currentToRemove)
}
