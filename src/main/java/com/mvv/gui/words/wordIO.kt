package com.mvv.gui.words

import com.mvv.gui.util.removeSuffixCaseInsensitive
import com.mvv.gui.util.userHome
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.notExists


enum class CsvFormat { Internal, MemoWord }

// TODO: rename and move to Settings
val dictDirectory: Path = userHome.resolve("english/words")

const val internalWordCardsFileExt = ".csv"
const val memoWordFileExt = "-RuEn-MemoWord.csv" //
const val plainWordsFileExt = ".txt"
const val ignoredWordsFilename = "ignored-words${plainWordsFileExt}"
val ignoredWordsFile: Path = dictDirectory.resolve(ignoredWordsFilename)

const val maxMemoCardWordCount = 300

val CsvFormat.fileNameEnding: String get() = when (this) {
    CsvFormat.Internal -> internalWordCardsFileExt
    CsvFormat.MemoWord -> memoWordFileExt
}

val Path.isMemoWordFile: Boolean get() = this.name.lowercase().endsWith(memoWordFileExt.lowercase())
val Path.isInternalCsvFormat: Boolean get() {
    val lowerFilename = this.name.lowercase()
    return lowerFilename.endsWith(internalWordCardsFileExt) && !lowerFilename.endsWith(memoWordFileExt.lowercase())
}


// Function name uses 'suffix' because it can be any suffix (not only file extension)
val Path.baseWordsFilename: String get() =
    this.name
        .removeSuffixCaseInsensitive(memoWordFileExt)
        .removeSuffixCaseInsensitive(internalWordCardsFileExt)
        .removeSuffixCaseInsensitive(plainWordsFileExt)


// Function name uses 'suffix' because it can be any suffix (not only file extension)
fun Path.useFilenameSuffix(filenameSuffix: String): Path =
    this.parent.resolve(this.baseWordsFilename + filenameSuffix)


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
        CsvFormat.MemoWord -> saveWordCardsIntoMemoWordCsv(file, words)
    }


@Deprecated("It is may provoke performance degradation (in case if we have too many dictionaries)." +
        " Use some cached mechanism, for example AllWordCardSetsManager")
internal fun loadWordsFromAllExistentDictionaries(toIgnoreBaseWordsFilename: String?): List<String> {

    val allWordsFilesExceptIgnored = getAllExistentSetFiles(includeMemoWordFile = true, toIgnoreBaseWordsFilename = toIgnoreBaseWordsFilename)

    return allWordsFilesExceptIgnored
        .asSequence()
        .map { loadWordCards(it) }
        .flatMap { it }
        .map { it.from }
        .distinctBy { it.lowercase() }
        .toList()
}

// -----------------------------------------------------------------------------

fun saveSplitWordCards(file: Path, words: Iterable<CardWordEntry>, directory: Path, portionSize: Int, fileFormat: CsvFormat) =
    words
        .asSequence()
        .windowed(portionSize, portionSize, true)
        .forEachIndexed { i, cardWordEntries ->
            val baseWordsFilename = file.baseWordsFilename
            //val folder = file.parent.resolve("split")
            val numberSuffix = "%02d".format(i + 1)

            saveWordCards(directory.resolve("${baseWordsFilename}_${numberSuffix}${fileFormat.fileNameEnding}"), fileFormat, cardWordEntries)
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

