package com.mvv.gui

import com.opencsv.*
import java.io.BufferedReader
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.text.Charsets.UTF_8


const val cardWordsFileExt = ".csv"
const val memoWorldCardWordsFileExt = "-RuEn-MemoWord.csv" //
const val plainWordsFileExt = ".txt"
const val ignoredWordsFilename = "ignored-words${plainWordsFileExt}"

val dictDirectory: Path = Path.of(System.getProperty("user.home") + "/english/words")


// Function name uses 'suffix' because it can be any suffix (not only file extension)
val Path.baseWordsFilename: String get() =
    this.name
        .removeSuffixCaseInsensitive(memoWorldCardWordsFileExt)
        .removeSuffixCaseInsensitive(cardWordsFileExt)
        .removeSuffixCaseInsensitive(plainWordsFileExt)


// Function name uses 'suffix' because it can be any suffix (not only file extension)
fun Path.useFilenameSuffix(filenameSuffix: String): Path =
    this.parent.resolve(this.baseWordsFilename + filenameSuffix)



//private fun Path.withMemoWorldSuffix(): Path {
//    val filename = this.name
//        .removeSuffixCaseInsensitive(memoWorldCardWordsFileExt)
//        .removeSuffixCaseInsensitive(cardWordsFileExt)
//        .plus(memoWorldCardWordsFileExt)
//
//    return this.parent.resolve(filename)
//}
//
//
//private fun Path.withStandardSuffix(): Path {
//    val filename = this.name
//        .removeSuffixCaseInsensitive(memoWorldCardWordsFileExt)
//        .removeSuffixCaseInsensitive(cardWordsFileExt)
//        .plus(cardWordsFileExt)
//
//    return this.parent.resolve(filename)
//}


fun loadWords(file: Path): List<String> =
    BufferedReader(FileReader(file.toFile()))
        .use {
            val words = it.lineSequence()
                .map { word -> word.trim() }
                .filter { word -> word.isNotEmpty() }
                .toList()
            words
    }


fun loadWordEntries(file: Path): List<CardWordEntry> {

    val isMemoWorldCsvFormat = file.name.lowercase().endsWith(memoWorldCardWordsFileExt)

    return FileReader(file.toFile(), UTF_8)
        .use { fileReader ->
            val csvReader =
                if (isMemoWorldCsvFormat)
                    CSVReaderBuilder(fileReader)
                        .withCSVParser(CSVParserBuilder()
                            .withSeparator(';')
                            .withEscapeChar(ICSVWriter.NO_ESCAPE_CHARACTER)
                            .withQuoteChar(ICSVWriter.NO_QUOTE_CHARACTER)
                            .build())
                        .build()
                else CSVReader(fileReader)

            csvReader
                .filter { it.size > 1 && it[0].isNotBlank() }
                .map { CardWordEntry(it[0].trim(), readTranslations(it) ) }
                .toList()
    }
}


private fun readTranslations(csvLine: Array<String>): String =
    csvLine
        .asSequence()
        .drop(1)
        .filter { it.isNotBlank() }
        .joinToString("\n")


fun saveWords(file: Path, words: Iterable<String>) {

    if (!words.iterator().hasNext()) {
        throw IllegalArgumentException("Nothing to save to [$file].")
    }

    Files.createDirectories(file.parent)

    FileWriter(file.toFile())
        .use { f -> words.forEach { f.write(it); f.write("\n") } }
}


enum class CsvFormat { Standard, MemoWorld }



fun saveWordEntries(file: Path, format: CsvFormat, words: Iterable<CardWordEntry>) {

    if (!words.iterator().hasNext()) throw IllegalArgumentException("Nothing to save to [$file].")

    Files.createDirectories(file.parent)

    when (format) {
        CsvFormat.Standard  -> saveWordEntriesWithStandardFormatImpl(file, words)
        CsvFormat.MemoWorld -> saveWordEntriesWithMemoWorldFormatImpl(file, words)
    }
}


private fun saveWordEntriesWithStandardFormatImpl(file: Path, words: Iterable<CardWordEntry>) {
    FileWriter(file.toFile(), UTF_8)
        .use { fw ->
            val csvWriter = CSVWriter(fw) // CSVWriterBuilder(fw).build()
                words.forEach {
                    csvWriter.writeNext(arrayOf(it.from, it.to))
        }
    }
}


private fun saveWordEntriesWithMemoWorldFormatImpl(file: Path, words: Iterable<CardWordEntry>) {
    FileWriter(file.toFile(), UTF_8)
        .use { fw ->
            val csvWriter = CSVWriterBuilder(fw)
                .withSeparator(';')
                .withEscapeChar(ICSVWriter.NO_ESCAPE_CHARACTER)
                .withQuoteChar(ICSVWriter.NO_QUOTE_CHARACTER)
                .build()

            words
                .filter { it.from.isNotBlank() }
                .forEach {
                    csvWriter.writeNext(arrayOf(
                        // in reversed order -> ru-en
                        formatWordOrPhraseToMemoWorldFormat(it.to),
                        formatWordOrPhraseToMemoWorldFormat(it.from),
                    ))
            }
        }
}


fun formatWordOrPhraseToMemoWorldFormat(wordOrPhrase: String): String =
    wordOrPhrase
        .trim()
        .replace("\n ", ", ")
        .replace("\n", ", ")
        .replace("; ", ", ")
        .replace(";", ", ")
        .replace(", , ", ", ")
        .replace(", ,", ", ")
        .replace(",, ", ", ")
        .replace(", ", ", ")
        //.replace("\"", "'")
        .removePrefix(",")
        .removePrefix(";")
        .removeSuffix(";")
        .removeSuffix(",")
        .trim()


// It would be nice to optimize it to avoid unneeded string conversions.
val String.translationCount: Int get() =
    formatWordOrPhraseToMemoWorldFormat(this).split(",").filter { it.isNotBlank() }.size



fun saveSplitWords(file: Path, words: Iterable<CardWordEntry>, portionSize: Int) =
    words
        .asSequence()
        .windowed(portionSize, portionSize, true)
        .forEachIndexed { i, cardWordEntries ->
            val baseWordsFilename = file.baseWordsFilename
            val folder = file.parent.resolve("split")
            val numberSuffix = "%02d".format(i + 1)
            saveWordEntries(folder.resolve("${baseWordsFilename}_${numberSuffix}${cardWordsFileExt}"), CsvFormat.Standard, cardWordEntries)
            saveWordEntries(folder.resolve("${baseWordsFilename}_${numberSuffix}${memoWorldCardWordsFileExt}"), CsvFormat.MemoWorld, cardWordEntries)
        }
