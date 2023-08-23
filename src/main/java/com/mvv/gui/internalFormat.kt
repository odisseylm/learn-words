package com.mvv.gui

import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import com.opencsv.ICSVWriter
import java.io.BufferedReader
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Path



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
private fun internalFormatCsvWriter(fileWriter: FileWriter): ICSVWriter = CSVWriter(fileWriter)

fun loadWordsFromInternalCsv(file: Path): List<String> =
    FileReader(file.toFile(), Charsets.UTF_8)
        .use { fileReader ->
            internalFormatCsvReader(fileReader)
                .map { it.getOrEmpty(0) }
                .filter { it.isNotBlank() }
                .toList()
        }

fun loadWordCardsFromInternalCsv(file: Path): List<CardWordEntry> =
    FileReader(file.toFile(), Charsets.UTF_8)
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


fun saveWordCardsIntoInternalCsv(file: Path, words: Iterable<CardWordEntry>) {
    if (!words.iterator().hasNext()) throw IllegalArgumentException("Nothing to save to [$file].")
    Files.createDirectories(file.parent)

    FileWriter(file.toFile(), Charsets.UTF_8)
        .use { fw ->
            val csvWriter = internalFormatCsvWriter(fw)
            words.forEach {
                csvWriter.writeNext(arrayOf(it.from, it.to, it.transcription, it.examples,
                    wordCardStatusesToString(it.wordCardStatuses)))
            }
        }
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
