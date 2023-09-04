package com.mvv.gui.words

import com.mvv.gui.dictionary.Dictionary
import com.mvv.gui.dictionary.extractExamples
import com.mvv.gui.javafx.UpdateSet
import com.mvv.gui.javafx.updateSetProperty
import com.mvv.gui.util.trimToNull
import javafx.collections.ObservableList
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.input.DataFormat
import java.io.FileReader
import java.nio.file.Files
import java.nio.file.Path
import java.util.Optional
import kotlin.io.path.isRegularFile
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
        .filter { !it.ignoreNoBaseWordInSet && it.noBaseWordInSet }
        .toSortedSet(cardWordEntryComparator)

    val baseWordsToAddMap: Map<CardWordEntry, List<CardWordEntry>> = withoutBaseWord
        .asSequence()
        .map { card -> Pair(card, englishBaseWords(card.from, dictionary)) }
        //.filterNotNullPairValue()
        .filter { it.second.isNotEmpty() }
        //.map { Pair(it.first, CardWordEntry(it.second, "")) }
        .associate { it }

    // Need to do it manually due to JavaFX bug (if a table has rows with different height)
    // This JavaFX bug appears if rows have different height.
    // See my comments in TableView.setViewPortAbsoluteOffsetImpl()
    //currentWordsList.runWithScrollKeeping { // restoreScrollPosition ->

        // Ideally this approach should keep less/more scrolling (without any hacks) but...
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


        //if (baseWordsToAddMap.size == 1) {
        //
        //    // Need to do it manually due to JavaFX bug (if a table has rows with different height)
        //    // !!! both call currentWordsList.viewPortAbsoluteOffset are needed !!!
        //    // restoreScrollPosition(SetViewPortAbsoluteOffsetMode.Immediately)
        //
        //    val newBaseWordCard = baseWordsToAddMap.values.asSequence().flatten().first()
        //
        //    // select new base word to edit it immediately
        //    if (currentWordsList.selectionModel.selectedItems.size <= 1) {
        //        currentWordsList.selectionModel.clearSelection()
        //        currentWordsList.selectionModel.select(newBaseWordCard)
        //    }
        //}
    //}

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
        log.warn("Error of getting transcription for [${word}].", ex)
        Optional.empty() }


internal fun Dictionary.translateWord(word: String): CardWordEntry =
    CardWordEntry(word, "").also { this.translateWord(it) }


internal fun Dictionary.translateWord(card: CardWordEntry) {
    val translation = this.find(card.from.trim())
    card.to = translation.translations.joinToString("\n")
    card.transcription = translation.transcription ?: ""
    card.examples = extractExamples(translation)
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
    //if (currentWordsList.isEditing) return

    it.forEach {
        it.from = it.from.lowercase()
        it.to = it.to.lowercase()
    }
    //currentWordsList.sort()
    //currentWordsList.refresh()
}


internal val Iterable<CardWordEntry>.isOneOfSelectedWordsHasNoBaseWord: Boolean get() =
    this.any { !it.ignoreNoBaseWordInSet && it.noBaseWordInSet }


internal fun ignoreNoBaseWordInSet(cards: Iterable<CardWordEntry>) =
    cards.forEach {
        updateSetProperty(it.wordCardStatusesProperty, WordCardStatus.BaseWordDoesNotExist, UpdateSet.Set) }


internal fun extractWordsFromText(content: CharSequence, ignoredWords: Collection<String>): List<CardWordEntry> =
    TextParser()
        .parse(content)
        .asSequence()
        .filter { !ignoredWords.contains(it) }
        .map { CardWordEntry(it, "") }
        .toList()


internal fun extractWordsFromFile(filePath: Path, ignoredWords: Collection<String>): List<CardWordEntry> =
    FileReader(filePath.toFile(), Charsets.UTF_8)
        .use { r -> extractWordsFromText(r.readText(), ignoredWords) } // T O D O: would be better to pass lazy CharSequence instead of loading full text as String


internal fun extractWordsFromClipboard(clipboard: Clipboard, ignoredWords: Collection<String>): List<CardWordEntry> {

    val content = clipboard.getContent(DataFormat.PLAIN_TEXT)

    log.info("clipboard content: [${content}]")
    if (content == null) return emptyList()

    val words = extractWordsFromText(content.toString(), ignoredWords)

    log.info("clipboard content as words: $words")
    return words
}



internal fun loadWordsFromAllExistentDictionaries(skipFiles: Collection<Path> = emptyList()): List<String> {

    if (dictDirectory.notExists()) return emptyList()

    val allWordsFilesExceptIgnored = Files.list(dictDirectory)
        .asSequence()
        .filter { it.isRegularFile() }
        .filter { it !in skipFiles && it != ignoredWordsFile }
        .filter { it.isInternalCsvFormat || it.isMemoWordFile }
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

    val skipFiles: Collection<Path> = currentWordsFile?.let { listOf(
        it,
        it.useFilenameSuffix(memoWordFileExt),
        it.useFilenameSuffix(internalWordCardsFileExt),
        it.useFilenameSuffix(plainWordsFileExt),
    ) }
        ?: emptyList()

    val toRemove = loadWordsFromAllExistentDictionaries(skipFiles)
    val toRemoveAsSet = toRemove.asSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toSortedSet(String.CASE_INSENSITIVE_ORDER)

    val currentToRemove = currentWords.filter { it.from.trim() in toRemoveAsSet }

    // perform removing as ONE operation to minimize change events
    currentWords.removeAll(currentToRemove)
}
