package com.mvv.gui.words

import com.mvv.gui.util.*
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.notExists


enum class CsvFormat { Internal, MemoWord }

// TODO: rename and move to Settings
val dictDirectory: Path = userHome.resolve("english/words")

const val internalWordCardsFileExt = ".csv"
private const val memoWordFileEnding = " - RuEn-MemoWord.csv"
private val memoWordFilePossibleEndingsLowerCase = listOf(memoWordFileEnding.lowercase(),
    " - RuEn-MemoWord.csv", "- RuEn-MemoWord.csv", "RuEn-MemoWord", "-MemoWord.csv", " MemoWord.csv",
    " - RuEn-MemoWord.xlsx", "- RuEn-MemoWord.xlsx", "RuEn-MemoWord.xlsx", "-MemoWord.xlsx", " MemoWord.xlsx",
    ).map { it.lowercase() }
const val plainWordsFileExt = ".txt"
const val ignoredWordsFilename = "ignored-words${plainWordsFileExt}"
val ignoredWordsFile: Path = dictDirectory.resolve(ignoredWordsFilename)

const val maxMemoCardWordCount = 300

val CsvFormat.fileNameEnding: String get() = when (this) {
    CsvFormat.Internal -> internalWordCardsFileExt
    CsvFormat.MemoWord -> memoWordFileEnding
}

val Path.isMemoWordFile: Boolean get() = this.name.lowercase().endsWithOneOf(memoWordFilePossibleEndingsLowerCase)
val Path.isInternalCsvFormat: Boolean get() {
    val lowerFilename = this.name.lowercase()
    return lowerFilename.endsWith(internalWordCardsFileExt) && !this.isMemoWordFile
}


// Function name uses 'suffix' because it can be any suffix (not only file extension)
val Path.baseWordsFilename: String get() =
    this.name
        .removeSuffixCaseInsensitive(memoWordFilePossibleEndingsLowerCase)
        .removeSuffixCaseInsensitive(internalWordCardsFileExt)
        .removeSuffixCaseInsensitive(plainWordsFileExt)


// Function name uses 'suffix' because it can be any suffix (not only file extension)
fun Path.useFilenameSuffix(filenameSuffix: String): Path =
    this.parent.resolve(this.baseWordsFilename + filenameSuffix)

fun Path.useMemoWordFilenameSuffix(): Path = this.parent.resolve(this.baseWordsFilename + memoWordFileEnding)
fun Path.useInternalFilenameSuffix(): Path = this.parent.resolve(this.baseWordsFilename + internalWordCardsFileExt)


// -----------------------------------------------------------------------------
fun loadWords(file: Path): List<String> {
    val ext = file.extension.lowercase()
    return when {
        file.isMemoWordFile      -> loadWordsFromMemoWordCsv(file)
        file.isInternalCsvFormat -> loadWordsFromInternalCsv(file)
        ext == "txt"             -> loadWordsFromTxtFile(file)
        else -> throw IllegalArgumentException("Unsupported file [$file].")
    }
}


fun loadWordCards(file: Path): List<CardWordEntry> {
    val ext = file.extension.lowercase()
    return when {
        file.isMemoWordFile      -> loadWordCardsFromMemoWordCsv(file)
        file.isInternalCsvFormat -> loadWordCardsFromInternalCsv(file)
        ext == "txt"             -> loadWordsFromTxtFile(file).map { CardWordEntry(it, "") }
        else -> throw IllegalArgumentException("Unsupported file [$file].")
    }
}


fun saveWordCards(file: Path, format: CsvFormat, words: Iterable<CardWordEntry>) =
    when (format) {
        CsvFormat.Internal -> saveWordCardsIntoInternalCsv(file, words)
        // CsvFormat.MemoWord -> saveWordCardsIntoMemoWordCsv(file, words)
        CsvFormat.MemoWord -> {
            saveWordCardsIntoMemoWordCsv(file.replaceExt(".csv"), words)
            saveWordCardsIntoMemoWordXlsx(file.replaceExt(".xlsx"), words)
        }
    }


@Deprecated("It is may provoke performance degradation (in case if we have too many dictionaries)." +
        " Use some cached mechanism, for example AllWordCardSetsManager")
internal fun loadWordsFromAllExistentDictionaries(toIgnoreBaseWordsFilename: String?): List<String> {

    val allWordsFilesExceptIgnored = getAllExistentSetFiles(toIgnoreBaseWordsFilename = toIgnoreBaseWordsFilename)

    return allWordsFilesExceptIgnored
        .asSequence()
        .map { loadWordCards(it) }
        .flatMap { it }
        .map { it.from }
        .distinctBy { it.lowercase() }
        .toList()
}

// -----------------------------------------------------------------------------

fun saveSplitWordCards(file: Path, words: Iterable<CardWordEntry>, directory: Path, portionSize: Int/*, fileFormat: CsvFormat*/): List<Path> =
    words
        .asSequence()
        .windowed(portionSize, portionSize, true)
        .flatMapIndexed { i, cardWordEntries ->
            val baseWordsFilename = file.baseWordsFilename
            //val folder = file.parent.resolve("split")
            val numberSuffix = "%02d".format(i + 1)

            val intFile = directory.resolve("$baseWordsFilename (${numberSuffix})").useInternalFilenameSuffix()
            saveWordCards(intFile, CsvFormat.Internal, cardWordEntries)

            val memoFile = directory.resolve("$baseWordsFilename (${numberSuffix})").useMemoWordFilenameSuffix()
            val memoFileXlsx = memoFile.replaceExt("xlsx")
            // it also stores in xlsx format
            saveWordCards(memoFile, CsvFormat.MemoWord, cardWordEntries)

            listOf(intFile, memoFile, memoFileXlsx)
        }
        .toList()


fun getAllExistentSetFiles(toIgnoreBaseWordsFilename: String?): List<Path> =
    getAllExistentSetFiles(dictDirectory, toIgnoreBaseWordsFilename)

fun getAllExistentSetFiles(dir: Path, toIgnoreBaseWordsFilename: String?): List<Path> {
    if (dir.notExists()) return emptyList()

    return dir.toFile().walkTopDown()
        .asSequence()
        .map { it.toPath() }
        .filter { it.isRegularFile() }
        .filter { it != ignoredWordsFile }
        //.filter { it.isInternalCsvFormat || (includeMemoWordFile && it.isMemoWordFile) }
        .filterNot { it.containsOneOf("_MemoWord", ".MemoWord") }
        .filter { it.isInternalCsvFormat }
        .filter { toIgnoreBaseWordsFilename.isNullOrBlank() || !it.name.contains(toIgnoreBaseWordsFilename) }
        .toList()
}


enum class CardsGroup (val groupName: String, subDir: String) {
    Root("Root", ""),
    Topic("Topic", "topic"),
    Grouped("Grouped", "grouped"),
    Synonyms("Synonyms", "synonyms"),
    Homophones("Homophones", "homophones"),
    Films("Films", "films"),
    BaseVerbs("Base Verbs", "base-verbs"),
    ;

    val directory: Path = if (subDir.isEmpty()) dictDirectory else dictDirectory.resolve(subDir)
    val fileBelongsToGroup: (Path)->Boolean = { it.parent == this.directory }
}
