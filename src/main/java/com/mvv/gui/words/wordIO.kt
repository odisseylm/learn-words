package com.mvv.gui.words

import com.mvv.gui.util.removeSuffixCaseInsensitive
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.name


enum class CsvFormat { Internal, MemoWord }


const val internalWordCardsFileExt = ".csv"
const val memoWordFileExt = "-RuEn-MemoWord.csv" //
const val plainWordsFileExt = ".txt"
const val ignoredWordsFilename = "ignored-words${plainWordsFileExt}"

val Path.isMemoWordFile: Boolean get() = this.name.lowercase().endsWith(memoWordFileExt.lowercase())
val Path.isInternalCsvFormat: Boolean get() {
    val lowerFilename = this.name.lowercase()
    return lowerFilename.endsWith(internalWordCardsFileExt) && !lowerFilename.endsWith(memoWordFileExt.lowercase())
}

val dictDirectory: Path = Path.of(System.getProperty("user.home") + "/english/words")


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



// -----------------------------------------------------------------------------

fun saveSplitWordCards(file: Path, words: Iterable<CardWordEntry>, directory: Path, portionSize: Int) =
    words
        .asSequence()
        .windowed(portionSize, portionSize, true)
        .forEachIndexed { i, cardWordEntries ->
            val baseWordsFilename = file.baseWordsFilename
            //val folder = file.parent.resolve("split")
            val numberSuffix = "%02d".format(i + 1)

            saveWordCards(directory.resolve("${baseWordsFilename}_${numberSuffix}${internalWordCardsFileExt}"), CsvFormat.Internal, cardWordEntries)
            saveWordCards(directory.resolve("${baseWordsFilename}_${numberSuffix}${memoWordFileExt}"), CsvFormat.MemoWord, cardWordEntries)
        }
