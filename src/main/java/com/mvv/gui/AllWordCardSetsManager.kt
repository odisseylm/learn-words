package com.mvv.gui

import com.mvv.gui.util.startStopWatch
import com.mvv.gui.util.timerTask
import com.mvv.gui.words.*
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.io.path.name


private val log = mu.KotlinLogging.logger {}


class AllWordCardSetsManager : AutoCloseable {

    @Volatile
    var ignoredFile: Path? = null

    @Volatile
    private var sets: Map<Path, WordsData> = mutableMapOf()
    @Volatile
    private var searchWordEntries: Map<String, List<SearchEntry>> = mutableMapOf()

    private val updatePeriod = Duration.ofSeconds(30)

    private val updateTimer = Timer("AllSetsManager", true).also {
        it.schedule(timerTask { reloadAllSets() }, updatePeriod.toMillis(), updatePeriod.toMillis()) }

    fun reloadAllSets() {
        val sw = startStopWatch()

        val ignoredFileBaseName = this.ignoredFile?.baseWordsFilename
        val allExistentSetFiles =
            getAllExistentSetFiles(includeMemoWordFile = false, toIgnoreBaseWordsFilename = ignoredFileBaseName).toSet()

        val prevSets = this.sets

        val removedSetFiles = prevSets.keys - allExistentSetFiles - ignoredFile
        val addedSetFiles = allExistentSetFiles - prevSets.keys - ignoredFile

        val toUpdatedSetsFromFiles = allExistentSetFiles
            .mapNotNull {
                val prevFileData = prevSets[it]
                val fileLastUpdatedAt = prevFileData?.fileLastUpdatedAt

                if (prevFileData == null || fileLastUpdatedAt == null || fileLastUpdatedAt > prevFileData.loadedAt)
                    it else null
            }
            .filter { it != ignoredFile }
            .toSet()

        log.debug { "Verification to reload all dictionaries" +
                " removedSetFiles: $removedSetFiles, addedSetFiles: $addedSetFiles," +
                " toUpdatedSetsFromFiles: $toUpdatedSetsFromFiles " }

        if (removedSetFiles.isEmpty() && addedSetFiles.isEmpty() && toUpdatedSetsFromFiles.isEmpty()) return

        val currentExistentSets = allExistentSetFiles
            .mapNotNull { f ->
                val prevFileData = prevSets[f]
                val fileLastUpdatedAt = prevFileData?.fileLastUpdatedAt

                if (prevFileData != null && fileLastUpdatedAt != null && fileLastUpdatedAt < prevFileData.loadedAt)
                    prevFileData
                else try {
                    WordsData(f, loadWordCardsFromInternalCsv(f).onEach { it.file = f  })
                } catch (ex: Exception) {
                    log.warn(ex) { "Error of loading set [$f]." }; null
                }
            }
            .associateBy { it.file }

        val currentSearchWordEntries: Map<String, List<SearchEntry>> = currentExistentSets.entries
            .flatMap { fileAndData ->
                fileAndData.value.searchWordEntries.entries.flatMap { searchEntry ->
                    searchEntry.value.map { card -> Pair(searchEntry.key, SearchEntry(fileAndData.key, card)) }
                }
            }
            .groupBy({ it.first }, { it.second })

        log.info { "All sets are updated (files: $toUpdatedSetsFromFiles) (took ${sw.time}ms)" }

        this.sets = currentExistentSets
        this.searchWordEntries = currentSearchWordEntries
    }

    // TODO: try to return read-only cards
    fun findBy(wordOrPhrase: String): List<SearchEntry> {

        val toSearch = wordOrPhrase.lowercase().trim()

        val byExactMatch = searchWordEntries[toSearch] ?: emptyList()
        if (byExactMatch.size >= 5) return byExactMatch.ignoreEntriesFromFile(this.ignoredFile)

        val matched2 = if (toSearch.startsWith("to be "))
            searchWordEntries[toSearch.removePrefix("to be ")] ?: emptyList() else emptyList()
        val matched3 = if (toSearch.startsWith("to "))
            searchWordEntries[toSearch.removePrefix("to ")] ?: emptyList() else emptyList()

        return (byExactMatch + matched2 + matched3).ignoreEntriesFromFile(this.ignoredFile)
    }

    private fun List<SearchEntry>.ignoreEntriesFromFile(ignoredFile: Path?): List<SearchEntry> =
        this.filter { it.file != ignoredFile }

    override fun close() = updateTimer.cancel()
}


class WordsData (
    val file: Path,
    private val wordEntries: List<CardWordEntry>,
    val loadedAt: Instant = Instant.now(),
    ) {
    val fileLastUpdatedAt: Instant? get() = try { Files.getLastModifiedTime(file).toInstant() } catch (_: Exception) { null }

    val searchWordEntries: Map<String, List<CardWordEntry>> = splitByAllWords()

    /*
    fun findBy(wordOrPhrase: String): List<SearchEntry> {

        val toSearch = wordOrPhrase.lowercase().trim()

        val byExactMatch = searchWordEntries.getOrDefault(toSearch, emptyList())
        if (byExactMatch.size >= 5) return byExactMatch.map { SearchEntry(file, it) }

        val matched2 = if (toSearch.startsWith("to be "))
            searchWordEntries[toSearch.removePrefix("to be ")] ?: emptyList() else emptyList()
        val matched3 = if (toSearch.startsWith("to "))
            searchWordEntries[toSearch.removePrefix("to ")] ?: emptyList() else emptyList()

        return (byExactMatch + matched2 + matched3).map { SearchEntry(file, it) }
    }
    */

    private fun splitByAllWords(): Map<String, List<CardWordEntry>> {
        return wordEntries
            .flatMap { card -> card.getAllSearchableWords().map { Pair(it, card) } }
            .groupBy( { it.first }, { it.second } )
    }
}


class SearchEntry (
    val file: Path,
    val card: CardWordEntry,
) {
    override fun toString(): String = "set: ${file.name} ${card.from} => ${card.to}"
}


private fun CardWordEntry.getAllSearchableWords(): List<String> {
    val allSearchWords = mutableListOf<String>()

    val fromKey1 = this.from.lowercase()
    allSearchWords.add(fromKey1)

    if (fromKey1.startsWith("to be ")) {
        allSearchWords.add(fromKey1.removePrefix("to be "))
    }

    if (fromKey1.startsWith("to ")) {
        allSearchWords.add(fromKey1.removePrefix("to "))
    }

    val translations = this.to.splitToToTranslations()
        .flatMap { getFromSubWords(it.trim().lowercase()) }
        .distinct()
    translations.forEach { allSearchWords.add(it) }

    return allSearchWords
}


private fun getFromSubWords(wordOrPhrase: String): List<String> {
    val words = mutableListOf(wordOrPhrase)

    if (wordOrPhrase.endsWith("(ся)")) {
        words.add(wordOrPhrase.removeSuffix("(ся)"))
    }
    else if (wordOrPhrase.endsWith("ся")) {
        words.add(wordOrPhrase.removeSuffix("ся"))
    }

    return words
}
