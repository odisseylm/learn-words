package com.mvv.gui

import com.opencsv.*
import java.io.BufferedReader
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.text.Charsets.UTF_8


const val cardWordsFileExt = ".csv"
const val memoWorldCardWordsFileExt = "-RuEn-MemoWord.csv" //
const val plainWordsFileExt = ".txt"
const val ignoredWordsFilename = "ignored-words${plainWordsFileExt}"

val Path.isMemoWordFile: Boolean get() = this.name.lowercase().endsWith(memoWorldCardWordsFileExt.lowercase())

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


enum class CsvFormat { Standard, MemoWorld }


// -----------------------------------------------------------------------------
fun loadWords(file: Path): List<String> {
    val ext = file.extension.lowercase()
    return when {
        file.isMemoWordFile -> loadFromWordsFromMemoWordsCsv(file)
        ext == "csv" -> loadFromWordsFromInternalCsv(file)
        ext == "txt" -> loadWordsFromTxtFile(file)
        else -> throw IllegalArgumentException("Unsupported file [$file].")
    }
}

fun loadWordEntries(file: Path): List<CardWordEntry> {
    val ext = file.extension.lowercase()
    return when {
        file.isMemoWordFile -> loadWordEntriesFromMemoWorldsCsv(file)
        ext == "csv" -> loadWordEntriesFromInternalCsv(file)
        ext == "txt" -> loadWordsFromTxtFile(file).map { CardWordEntry(it, "") }
        else -> throw IllegalArgumentException("Unsupported file [$file].")
    }
}


// -----------------------------------------------------------------------------
fun loadWordsFromTxtFile(file: Path): List<String> =
    BufferedReader(FileReader(file.toFile()))
        .use {
            val words = it.lineSequence()
                .map { word -> word.trim() }
                .filter { word -> word.isNotEmpty() }
                .toList()
            words
        }

fun saveWordsToTxtFile(file: Path, words: Iterable<String>) {

    if (!words.iterator().hasNext()) throw IllegalArgumentException("Nothing to save to [$file].")

    Files.createDirectories(file.parent)

    FileWriter(file.toFile())
        .use { f -> words.forEach { f.write(it); f.write("\n") } }
}


// -----------------------------------------------------------------------------
private fun internalFormatCsvReader(fileReader: FileReader): CSVReader = CSVReader(fileReader)

fun loadFromWordsFromInternalCsv(file: Path): List<String> =
    FileReader(file.toFile(), UTF_8)
        .use { fileReader ->
            internalFormatCsvReader(fileReader)
                .map { it.getOrEmpty(0) }
                .filter { it.isNotBlank() }
                .toList()
        }

fun loadWordEntriesFromInternalCsv(file: Path): List<CardWordEntry> =
    FileReader(file.toFile(), UTF_8)
        .use { fileReader ->
            internalFormatCsvReader(fileReader)
                .filter { it.isNotEmpty() && it[0].isNotBlank() }
                .map {
                    // Fields order: from, to, transcription, examples, wordCardStatuses
                    val card = CardWordEntry("", "")
                    var index = 0
                    card.from = it[index++]
                    card.to = it.getOrEmpty(index++)
                    card.transcription = it.getOrEmpty(index++)
                    card.examples = it.getOrEmpty(index++)
                    card.wordCardStatuses = wordCardStatusesFromString(it.getOrEmpty(index))
                    card
                }
                .toList()
    }


fun saveWordEntriesWithMemoWordFormat(file: Path, words: Iterable<CardWordEntry>) {
    if (!words.iterator().hasNext()) throw IllegalArgumentException("Nothing to save to [$file].")
    Files.createDirectories(file.parent)

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
                        "", // part of speech
                        formatWordOrPhraseToMemoWorldFormat(it.transcription + '\n' + it.examples),
                    ))
                }
        }
}


// -----------------------------------------------------------------------------
private fun memoWordCsvReader(fileReader: FileReader): CSVReader =
    CSVReaderBuilder(fileReader)
        .withCSVParser(
            CSVParserBuilder()
                .withSeparator(';')
                .withEscapeChar(ICSVWriter.NO_ESCAPE_CHARACTER)
                .withQuoteChar(ICSVWriter.NO_QUOTE_CHARACTER)
                .withIgnoreQuotations(true)
                .withStrictQuotes(false)
                .build()
        )
        .build()

fun loadFromWordsFromMemoWordsCsv(file: Path): List<String> =
    FileReader(file.toFile(), UTF_8)
        .use { fileReader ->
            memoWordCsvReader(fileReader)
                .map { it.getOrEmpty(1) }
                .filter { it.isNotBlank() }
                .toList()
        }


fun loadWordEntriesFromMemoWorldsCsv(file: Path): List<CardWordEntry> =
    FileReader(file.toFile(), UTF_8)
        .use { fileReader ->
            memoWordCsvReader(fileReader)
                .filter { it.size >= 2 && it[0].isNotBlank() }
                .map {
                    // Fields order: russian/to, english/from, transcription, examples, wordCardStatuses
                    val card = CardWordEntry("", "")
                    var index = 0
                    card.to = it.getOrEmpty(index++)
                    card.from = it.getOrEmpty(index++)
                    index++ // part of speech
                    val transcriptionAndExamples = it.getOrEmpty(index)
                    card.transcription = "" // T O D O: extract transcription from transcriptionAndExamples
                    card.examples = transcriptionAndExamples
                    card
                }
                .filter { it.from.isNotBlank() }
                .toList()
    }


fun saveWordEntriesWithStandardFormat(file: Path, words: Iterable<CardWordEntry>) {
    if (!words.iterator().hasNext()) throw IllegalArgumentException("Nothing to save to [$file].")
    Files.createDirectories(file.parent)

    FileWriter(file.toFile(), UTF_8)
        .use { fw ->
            val csvWriter = CSVWriter(fw) // CSVWriterBuilder(fw).build()
            words.forEach {
                csvWriter.writeNext(arrayOf(it.from, it.to, it.transcription, it.examples,
                    wordCardStatusesToString(it.wordCardStatuses)))
            }
        }
}


// -----------------------------------------------------------------------------

fun saveWordEntries(file: Path, format: CsvFormat, words: Iterable<CardWordEntry>) =
    when (format) {
        CsvFormat.Standard  -> saveWordEntriesWithStandardFormat(file, words)
        CsvFormat.MemoWorld -> saveWordEntriesWithMemoWordFormat(file, words)
    }


private fun wordCardStatusesToString(statuses: Iterable<WordCardStatus>) =
    statuses.asSequence().map { it.name }.joinToString(",")

private fun wordCardStatusesFromString(statuses: String): Set<WordCardStatus> =
    statuses.splitToSequence(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        // TODO: improve or log or... (remember that this is used to load)
        // TODO: do not use it for loadAllWords
        .map { try { WordCardStatus.valueOf(it) } catch (ignore: Exception) { null } }
        .filterNotNull()
        .toSet()



fun formatWordOrPhraseToMemoWorldFormat(wordOrPhrase: String): String =
    wordOrPhrase
        .replace('"', '\'')
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


// -----------------------------------------------------------------------------

// It would be nice to optimize it to avoid unneeded string conversions.
val String.translationCount: Int get() =
    formatWordOrPhraseToMemoWorldFormat(this)
        .split(",")
        .filter { it.isNotBlank() }.size


// -----------------------------------------------------------------------------

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
