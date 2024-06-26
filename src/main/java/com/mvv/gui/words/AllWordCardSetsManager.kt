package com.mvv.gui.words

import com.mvv.gui.task.TaskManager
import com.mvv.gui.task.addTask
import com.mvv.gui.util.*
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
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


interface AllCardWordEntry : CardWordEntry {
    val fileProperty: ObjectProperty<Path>
}
val AllCardWordEntry.file: Path? get() = fileProperty.get()

fun CardWordEntry.toAllEntry(file: Path): AllCardWordEntry = AllCardWordEntryImpl(this, file)

private class AllCardWordEntryImpl(
    private val card: CardWordEntry,
    file: Path)
    : CardWordEntry by card, AllCardWordEntry {

    override val fileProperty = SimpleObjectProperty(this, "file", file)
    override fun toString(): String = "set: ${file?.name} $from => $to"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AllCardWordEntryImpl

        if (card != other.card) return false
        if (fileProperty.value != other.fileProperty.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = card.hashCode()
        result = 31 * result + fileProperty.value.hashCode()
        return result
    }
}


class AllWordCardSetsManager : AutoCloseable {

    @Volatile
    var ignoredFile: Path? = null

    @Volatile
    private var sets: Map<Path, WordsData> = mapOf()
    @Volatile
    private var searchWordEntries: Map<String, List<AllCardWordEntry>> = mutableMapOf()

    private val updatePeriod = Duration.ofSeconds(30)

    private val updateTimer = Timer("AllSetsManager", true).also {
        it.schedule(timerTask { reloadAllSetsAsync() }, updatePeriod.toMillis(), updatePeriod.toMillis()) }

    private val updatesExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "AllWordCardSetsManager updater").also { it.isDaemon = true }
    } }

    fun isEmpty(): Boolean = searchWordEntries.isEmpty()

    fun reloadAllSetsAsync() { updatesExecutor.submit { reloadAllSets() } }
    fun reloadAllSetsAsync(taskManager: TaskManager) {
        taskManager.addTask("Reloading all word card sets", updatesExecutor) {
            reloadAllSets()
    } }

    private fun reloadAllSets() {
        val sw = startStopWatch()

        val ignoredFileBaseName = this.ignoredFile?.baseWordsFilename
        val allExistentSetFiles = getAllExistentSetFiles(toIgnoreBaseWordsFilename = ignoredFileBaseName).toSet()

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
                    WordsData(f, loadWordCardsFromInternalCsv(f))
                } catch (ex: Exception) {
                    log.warn(ex) { "Error of loading set [$f]." }; null
                }
            }
            .associateBy { it.file }

        val currentSearchWordEntries: Map<String, List<AllCardWordEntry>> = currentExistentSets.entries
            .flatMap { fileAndData ->
                fileAndData.value.searchWordEntries.entries.flatMap { searchEntry ->
                    searchEntry.value.map { card -> Pair(searchEntry.key, card.toAllEntry(fileAndData.key)) }
                }
            }
            .groupBy({ it.first }, { it.second })

        log.info { "All sets are updated (files(${toUpdatedSetsFromFiles.size}): $toUpdatedSetsFromFiles) (took ${sw.time}ms)" }

        this.sets = currentExistentSets
        this.searchWordEntries = currentSearchWordEntries
    }

    // Try to return read-only cards.
    fun findBy(wordOrPhrase: String, matchMode: MatchMode): List<AllCardWordEntry> {

        if (wordOrPhrase.isBlank()) return emptyList()

        val toSearch = wordOrPhrase.lowercase().trim()

        return when (matchMode) {
            MatchMode.Exact    -> findByExactMatch(toSearch)
            MatchMode.ByPrefix -> findByPrefix(toSearch)
            MatchMode.Contains -> findByContains(toSearch)
        }
    }

    private fun findByExactMatch(toSearch: String): List<AllCardWordEntry> {

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

    private fun findByPrefix(wordOrPhrase: String): List<AllCardWordEntry> {

        // Actually it is needed only for possible verbs, and we can avoid it (for some cases) when we sure it is not verb.
        val toSearchPrefix = if (wordOrPhrase.startsWith("to ")) listOf(wordOrPhrase) else listOf(wordOrPhrase, "to $wordOrPhrase", "to be $wordOrPhrase")

        // to keep the same ref for all searches
        val searchWordEntriesRef = searchWordEntries

        val matchedKeys = searchWordEntriesRef.keys
            .filter { it.startsWithOneOf(toSearchPrefix) }

        val found: List<AllCardWordEntry> = matchedKeys
            .mapNotNull { searchWordEntriesRef[it] }
            .flatten()
            .distinct()

        return found.ignoreEntriesFromFile(this.ignoredFile)
    }

    private fun findByContains(wordOrPhrase: String): List<AllCardWordEntry> {

        // to keep the same ref for all searches
        val searchWordEntriesRef = searchWordEntries

        val matchedKeys = searchWordEntriesRef.keys
            .filter { keyAsWord -> keyAsWord.contains(wordOrPhrase) }

        val found: List<AllCardWordEntry> = matchedKeys
            .mapNotNull { searchWordEntriesRef[it] }
            .flatten()
            .distinct()

        return found.ignoreEntriesFromFile(this.ignoredFile)
    }

    private fun List<AllCardWordEntry>.ignoreEntriesFromFile(ignoredFile: Path?): List<AllCardWordEntry> =
        this.filter { it.file != ignoredFile }

    val allCardSets: List<Path> get() = this.sets.keys.toList()

    override fun close() = updateTimer.cancel()
}


class WordsData (
    val file: Path,
    private val wordEntries: List<CardWordEntry>,
    val loadedAt: Instant = Instant.now(),
    ) {
    val fileLastUpdatedAt: Instant? get() = try { Files.getLastModifiedTime(file).toInstant() } catch (_: Exception) { null }

    val searchWordEntries: Map<String, List<CardWordEntry>> = splitByAllWords()

    private fun String.removeOptionalTrailingPronoun(): String {
        val s1 = englishOptionalTrailingPronounsFinder.removeMatchedSubSequence(this, SubSequenceFinderOptions(false))
        val s2 = russianOptionalTrailingPronounsFinder.removeMatchedSubSequence(this, SubSequenceFinderOptions(false))

        return if (s1.length < s2.length) s1 else s2
    }

    private fun splitByAllWords(): Map<String, List<CardWordEntry>> {
        return wordEntries
            .flatMap { card -> card.getAllSearchableWords().flatMap { listOf(it, it.removeOptionalTrailingPronoun()) }.distinct().map { Pair(it, card) } }
            .groupBy( { it.first }, { it.second } )
    }
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
        .flatMap { it.splitTranslationToIndexed() }
        .flatMap { getFromSubWords(it.trim().lowercase()) }
        .distinct()
    translations.forEach { allSearchWords.add(it) }

    return allSearchWords
}


private val Part.isVerb: Boolean get() = this.asTextOnly().isVerb

private val Part.isOneWordVerb: Boolean get() = this.asTextOnly().wordCount == 1 && this.isVerb

private val Part.isOptionalVerbEnd: Boolean get() = this.asTextOnly().isOptionalVerbEnd


internal fun CharSequence.splitTranslationToIndexed(): List<String> =
    this.splitRussianTranslationToIndexed()

private fun CharSequence.splitRussianTranslationToIndexed(): List<String> {

    val parts0: List<Part> = this
        .trim()
        .removeRepeatableSpaces(SpaceCharPolicy.UseSpaceOnly)
        .splitByBrackets()
        .filterOutUnneededInBracketParts()

    val trimmed = parts0
        .joinToString("") { it.asSubContent() }
        .trim().removeRepeatableSpaces(SpaceCharPolicy.UseSpaceOnly)
    val trimmedWithoutBrackets = parts0
        .filter { it.withoutBrackets }
        .joinToString(" ") { it.asTextOnly() }
        .trim()
        .removeRepeatableSpaces()

    val wordCount = trimmedWithoutBrackets.wordCount

    if (trimmed.endsWith('!')) {
        val result = mutableListOf<String>()

        result.addAll(
            trimmed, trimmed.removeSuffix("!"),
            trimmedWithoutBrackets, trimmedWithoutBrackets.removeSuffix("!"))

        if (trimmed.endsWith("айте!"))
            result.addAll(
                trimmed.replaceSuffix("айте!", "ай!"),
                trimmed.replaceSuffix("айте!", "ай"),
                trimmedWithoutBrackets.replaceSuffix("айте!", "ай!"),
                trimmedWithoutBrackets.replaceSuffix("айте!", "ай"),
            )

        return result.distinct()
    }

    val withoutOptionalEnding = trimmed.removeOptionalTranslationEnding()

    val t1 = trimmed.splitRussianTranslationImpl(true)
    val t2 = if (withoutOptionalEnding.length < trimmed.length)
             withoutOptionalEnding.splitRussianTranslationImpl(true)
             else emptyList()

    return (t1 + t2)
        .map { it.trim().removeRepeatableSpaces(SpaceCharPolicy.UseSpaceOnly) }
        .distinct()
        .flatMap { listOf(it, it.removeOptionalTranslationEnding()) }
        .filter { wordCount == 1 || it !in veryBaseVerbs }
        .distinct()
}


// We cannot use there HashSet because in general CharSequence may not have hashCode()/equals() or compareTo().
private val inBracketPartsToSkip: Set<CharSequence> = TreeSet(CharSequenceComparator()).also { it.addAll(
    "(разг.)", "(разг)", "(ам)", "(ам.)", "(уст.)", "(уст)", "(шутл.)", "(шутл)",
) }


private val veryBaseVerbs = setOf("быть", "идти", "стоять")

private fun List<Part>.filterOutUnneededInBracketParts(): List<Part> =
    this.filterNot { inBracketPartsToSkip.contains(it.asSubContent()) }


private fun CharSequence.splitRussianTranslationImpl(processChildren: Boolean): List<CharSequence> {

    val result = mutableListOf(this)

    val splitByBrackets = this.splitByBrackets().filter { it.asTextOnly().isNotBlank() }
    val splitByBrackets2 = splitByBrackets.filterIndexed { i, it -> i < 2 || it.withoutBrackets }

    result.add(
        splitByBrackets.filter { it.withoutBrackets && it.asTextOnly().isNotBlank() }.joinToString(" ") { it.asTextOnly().trim() }
    )

    if (splitByBrackets2.size == 2) {

        val part1 = splitByBrackets2[0]
        val part1Text = part1.asTextOnly()

        val part2 = splitByBrackets2[1]
        val part2Text = part2.asTextOnly()

        val part1Words = part1Text.splitToWords()
        val part2Words = part2Text.splitToWords()

        val part1WordCount = part1Words.size
        val part2WordCount = part2Words.size

        if (splitByBrackets.size == 2) {
            // case "рассчитывать на (надеяться на)"
            if (part1.withoutBrackets && part1WordCount == 2
                && part2.inBrackets && part2WordCount == 2) {

                if (part1Words[0].isVerb && part2Words[0].isVerb && part1Words[1] == part2Words[1]) {
                    result.add(part1Text)
                    result.add(part2Text)
                }
            }
        }


        // case: "страшный (ужасный)"
        if (part1.withoutBrackets && part2.inBrackets
            && part1WordCount == 1 && part2WordCount == 1
            && ((part1Text.isVerb && part2Text.isVerb) || (part1Text.isAdjective && part2Text.isAdjective)))
            result.add(part2Text)

        // case: "доверять(ся) (полагать(ся))"
        else if (part1.withoutBrackets && part2.inBrackets
            && part1Text.isOneWordVerb && part2Text.isOptionalVerbEnd
        ) {
            result.add(part1Text.toString())

            val part1Trimmed = part1Text.trim()
            result.add("${part1Trimmed}${part2Text}")
            result.add("${part1Trimmed}(${part2Text})")
        }

        // case: " страшный (ужасный жуткий) "
        else if (part1.withoutBrackets && part2.inBrackets
            && part1WordCount == 1 && part2WordCount > 1
            && (part1Text.isAdjective && part2Text.isAdjective)) {

            val subItems = part2Text.toString().splitRussianTranslationImpl(false).flatMap { it.splitToWords() }
            // case: " страшный (ужасный жуткий) "
            if (subItems.all { it.isAdjective })
                result.addAll(subItems)
        }

        // case: " доверяться (полагаться опираться) "
        else if (part1.withoutBrackets && part2.inBrackets
            && part1Text.isOneWordVerb && part2WordCount > 1) {

            val subItems = part2Text.toString().splitRussianTranslationImpl(false).flatMap { it.splitToWords() }

            // case: " доверяться (полагаться опираться) "
            if (subItems.isNotEmpty() && subItems.all { it.isVerb })
                result.addAll(subItems)

            // case: "бить (сильно ударять)"
            if (part2WordCount == 2 && part2Words[0].isProbablyAdverbForVerb && part2Words[1].isVerb) {
                result.add(part2Words[1])
                result.add(part2Text)
            }
            // case: "бить (ударять сильно)"
            if (part2WordCount == 2 && part2Words[0].isVerb && part2Words[1].isProbablyAdverbForVerb) {
                result.add(part2Words[0])
                result.add(part2Text)
            }
        }

        if (splitByBrackets.size == 2) {

            // case "выдерживать (держаться до конца)"
            if (part1.withoutBrackets && part1WordCount == 1 && part1Text.isVerb &&
                part2.inBrackets && part2WordCount > 1 && part2Words[0].isVerb && !part2Words[1].isVerb
                ) {
                result.addAll(part2Text.splitRussianTranslationImpl(false))
            }
        }

    }


    if (splitByBrackets.size == 3) {

        val part1 = splitByBrackets[0]
        val part1Text = part1.asTextOnly()

        val part2 = splitByBrackets[1]
        val part2Text = part2.asTextOnly()

        val part3 = splitByBrackets[2]
        val part3Text = part3.asTextOnly()

        if (part2Text == "или") {
            result.add(part1Text)
            result.add(part3Text)
        }

        if (part1.withoutBrackets && part2.inBrackets && part3.inBrackets
            && part1Text.isOneWordVerb && part2Text.isOptionalVerbEnd
        ) {
            result.add("${part1Text}${part2Text}")
            result.add("${part1Text}(${part2Text})")

            if (processChildren) {
                val part3TextStr = part3Text.toString()
                val part3Parts: List<Part> = part3TextStr.splitByBrackets().filter { it.asTextOnly().isNotBlank() }

                // case: "доверять(ся) (полагать(ся))"
                if ((part3Parts.size == 1 && part3Parts[0].isOneWordVerb)
                    || (part3Parts.size == 2 && part3Parts[0].isOneWordVerb && part3Parts[1].isOptionalVerbEnd))
                    result.addAll(part3TextStr.splitRussianTranslationImpl(false))

                // case: " доверять(ся) (полагать(ся) опирать(ся)) "
                else if (part3Parts.isNotEmpty() && part3Parts.all { it.isVerb || it.isOptionalVerbEnd }) {

                    val allWords = part3Parts.flatMap { it.asTextOnly().splitToWords() }
                    var lastVerb: String? = null

                    for (w in allWords) {
                        if (w.isOptionalVerbEnd) {
                            if (lastVerb != null) {
                                result.add(lastVerb + w)
                                result.add("$lastVerb($w)")
                            }
                        } else {
                            result.add(w)
                            lastVerb = w
                        }
                    }
                }
            }
        }

        // case "грубый (немузыкальный) слух"
        // It is not reliable and probably should be removed.
        if (part1.withoutBrackets && part1Text.wordCount == 1 && part1Text.isAdjective &&
            part2.inBrackets      && part2Text.wordCount == 1 && part2Text.isAdjective &&
            part3.withoutBrackets && part3Text.wordCount == 1 && !(part3Text.isAdjective || part3Text.isVerb)
            ) {
            result.add("$part1Text $part3Text")
            result.add("$part2Text $part3Text")
        }
    }

    val allWordsWithoutBrackets: List<String> = splitByBrackets.filter { it.withoutBrackets }.flatMap { it.asTextOnly().splitToWords() }

    // "бык или корова" "бык или корова (откормленные на убой)"
    if (allWordsWithoutBrackets.size == 3 && allWordsWithoutBrackets[1] == "или") {
        result.add(allWordsWithoutBrackets[0])
        result.add(allWordsWithoutBrackets[2])
    }

    // case "беспрестанно бранить"
    if (allWordsWithoutBrackets.size == 2 && allWordsWithoutBrackets[0].isProbablyAdverbForVerb && allWordsWithoutBrackets[1].isVerb) {
        result.add(allWordsWithoutBrackets[1])
    }

    // case "бранить беспрестанно"
    if (allWordsWithoutBrackets.size == 2 && allWordsWithoutBrackets[1].isProbablyAdverbForVerb && allWordsWithoutBrackets[0].isVerb) {
        result.add(allWordsWithoutBrackets[0])
    }

    // to avoid removing too much
    if (allWordsWithoutBrackets.size > 1) {
        result.removeAll(veryBaseVerbs)
    }

    return result.distinct()
}


private val russianOptionalTrailingFinderOptions = SubSequenceFinderOptions(true)
private val russianOptionalTrailingFinder = russianOptionalTrailingFinder()


private fun String.removeOptionalTranslationEnding(): String =
    russianOptionalTrailingFinder.removeMatchedSubSequence(this, russianOptionalTrailingFinderOptions)


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
