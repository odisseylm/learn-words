package com.mvv.gui

import com.mvv.gui.util.*
import com.mvv.gui.words.*
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.io.path.name


private val log = mu.KotlinLogging.logger {}


enum class MatchMode {
    Exact,
    ByPrefix,
    Contains,
    //ByMask,
}


class AllWordCardSetsManager : AutoCloseable {

    @Volatile
    var ignoredFile: Path? = null

    @Volatile
    private var sets: Map<Path, WordsData> = mapOf()
    @Volatile
    private var searchWordEntries: Map<String, List<SearchEntry>> = mutableMapOf()

    private val updatePeriod = Duration.ofSeconds(30)

    private val updateTimer = Timer("AllSetsManager", true).also {
        it.schedule(timerTask { reloadAllSetsAsync() }, updatePeriod.toMillis(), updatePeriod.toMillis()) }

    private val updatesExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "AllWordCardSetsManager updater").also { it.isDaemon = true }
    } }

    fun reloadAllSetsAsync() { updatesExecutor.submit { reloadAllSets() } }

    private fun reloadAllSets() {
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
                " removedSetFiles: $removedSetFiles, addedSetFiles(${addedSetFiles.size}): $addedSetFiles," +
                " toUpdatedSetsFromFiles(${toUpdatedSetsFromFiles.size}): $toUpdatedSetsFromFiles " }

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

        log.info { "All sets are updated (files(${toUpdatedSetsFromFiles.size}): $toUpdatedSetsFromFiles) (took ${sw.time}ms)" }

        this.sets = currentExistentSets
        this.searchWordEntries = currentSearchWordEntries
    }

    // TODO: try to return read-only cards
    fun findBy(wordOrPhrase: String, matchMode: MatchMode): List<SearchEntry> {

        if (wordOrPhrase.isBlank()) return emptyList()

        val toSearch = wordOrPhrase.lowercase().trim()

        return when (matchMode) {
            MatchMode.Exact    -> findByExactMatch(toSearch)
            MatchMode.ByPrefix -> findByPrefix(toSearch)
            MatchMode.Contains -> findByContains(toSearch)
        }
    }

    private fun findByExactMatch(toSearch: String): List<SearchEntry> {

        // to keep the same ref for all searches
        val searchWordEntriesRef = searchWordEntries

        val byExactMatch = searchWordEntriesRef[toSearch] ?: emptyList()
        if (byExactMatch.size >= 5) return byExactMatch.ignoreEntriesFromFile(this.ignoredFile)

        val matched2 = if (toSearch.startsWith("to be "))
            searchWordEntriesRef[toSearch.removePrefix("to be ")] ?: emptyList() else emptyList()
        val matched3 = if (toSearch.startsWith("to "))
            searchWordEntriesRef[toSearch.removePrefix("to ")] ?: emptyList() else emptyList()

        return (byExactMatch + matched2 + matched3).distinct().ignoreEntriesFromFile(this.ignoredFile)
    }

    private fun findByPrefix(wordOrPhrase: String): List<SearchEntry> {

        // Actually it is needed only for possible verbs, and we can avoid it (for some cases) when we sure it is not verb.
        val toSearchPrefix = if (wordOrPhrase.startsWith("to ")) listOf(wordOrPhrase) else listOf(wordOrPhrase, "to $wordOrPhrase", "to be $wordOrPhrase")

        // to keep the same ref for all searches
        val searchWordEntriesRef = searchWordEntries

        val matchedKeys = searchWordEntriesRef.keys
            .filter { it.startsWithOneOf(toSearchPrefix) }

        val found: List<SearchEntry> = matchedKeys
            .mapNotNull { searchWordEntriesRef[it] }
            .flatten()
            .distinct()

        return found.ignoreEntriesFromFile(this.ignoredFile)
    }

    private fun findByContains(wordOrPhrase: String): List<SearchEntry> {

        // to keep the same ref for all searches
        val searchWordEntriesRef = searchWordEntries

        val matchedKeys = searchWordEntriesRef.keys
            .filter { keyAsWord -> keyAsWord.contains(wordOrPhrase) }

        val found: List<SearchEntry> = matchedKeys
            .mapNotNull { searchWordEntriesRef[it] }
            .flatten()
            .distinct()

        return found.ignoreEntriesFromFile(this.ignoredFile)
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


data class SearchEntry (
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

    val translations = this.to.lowercase()
        .splitToToTranslations()
        .flatMap { it.splitTranslation() }
        .flatMap { getFromSubWords(it.trim().lowercase()) }
        .distinct()
    translations.forEach { allSearchWords.add(it) }

    return allSearchWords
}


private val CharSequence.wordCount: Int get() = this.trim().split(" ", "\t", "\n").size

private val CharSequence.isVerb: Boolean get() {
    val fixed = this.trimEnd()
    return fixed.endsWithOneOf("ть", "ться")
}

private val CharSequence.isOneWordVerb: Boolean get() = this.wordCount == 1 && this.isVerb

private val CharSequence.isVerbEnd: Boolean get() {
    val fixed = this.trimEnd()
    return fixed.endsWith("ся")
}

private val CharSequence.isAdjective: Boolean get() {
    val fixed = this.trimEnd()//.lowercase()
    return fixed.isNotEmpty() && (fixed[fixed.length - 1] == 'й') &&
           fixed.endsWithOneOf("ый", "ий", "ой", "ин", "ын", "ов", "ей", "от")
}

internal fun String.splitTranslation(): List<String> =
    this.splitTranslationImpl(true)
        .map { it.trim() }
        .distinct()

private fun String.splitTranslationImpl(processChildren: Boolean): List<String> {

    val result = mutableListOf(this)

    val splitByBrackets = this.splitByBrackets()

    result.add(
        splitByBrackets.filter { it.withoutBrackets && it.asTextOnly().isNotBlank() }.joinToString(" ") { it.asTextOnly().trim() }
    )

    if (splitByBrackets.size == 2) {

        val part1 = splitByBrackets[0]
        val part1Text = part1.asTextOnly()

        val part2 = splitByBrackets[1]
        val part2Text = part2.asTextOnly()

        if (part1.withoutBrackets && part2.inBrackets
            && part1Text.wordCount == 1 && part2Text.wordCount == 1
            && ((part1Text.isVerb && part2Text.isVerb) || (part1Text.isAdjective && part2Text.isAdjective)))
            result.add(part2Text.toString())

        else if (part1.withoutBrackets && part2.inBrackets
            && part1Text.isOneWordVerb && part2Text.isVerbEnd
        ) {
            result.add(part1Text.toString())
            val part1Trimmed = part1Text.trim()
            result.add("${part1Trimmed}${part2Text}")
            result.add("${part1Trimmed}${part2.asSubContent()}") // with verb end in brackets
        }
    }

    val splitByBrackets2 =  splitByBrackets.filter { it.asTextOnly().isNotBlank() }
    if (splitByBrackets2.size == 3) {

        val part1 = splitByBrackets2[0]
        val part1Text = part1.asTextOnly()

        val part2 = splitByBrackets2[1]
        val part2Text = part2.asTextOnly()

        val part3 = splitByBrackets2[2]
        val part3Text = part3.asTextOnly()

        if (part1.withoutBrackets && part2.inBrackets && part3.inBrackets
            && part1Text.isOneWordVerb && part2Text.isVerbEnd
        ) {
            result.add("${part1Text}${part2.asSubContent()}")
            result.add("${part1Text}${part2Text}")

            if (processChildren) {
                val part3TextStr = part3Text.toString()
                val part3Parts: List<Part> = part3TextStr.splitByBrackets()
                if ((part3Parts.size == 1 && part3Parts[0].asTextOnly().isOneWordVerb)
                    || (part3Parts.size == 2 && part3Parts[0].asTextOnly().isOneWordVerb && part3Parts[1].asTextOnly().isVerbEnd)
                ) {
                    result.addAll(part3TextStr.splitTranslationImpl(false))
                }
            }
        }
    }

    return result.distinct()
}

data class Part (
    val content: String,
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
            content: String,
            // bracket indices
            openBracketIndex: Int,
            closingBracketIndex: Int,
        ) = Part(content, true, openBracketIndex + 1, closingBracketIndex, openBracketIndex, closingBracketIndex)
        fun withoutBrackets(
            content: String,
            from: Int,
            /** Exclusive */
            to: Int,
        ) = Part(content, false, from, to, -1, -1)
    }
}

val Part.withoutBrackets: Boolean get() = !this.inBrackets

internal fun String.splitByBrackets(): List<Part> {

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
