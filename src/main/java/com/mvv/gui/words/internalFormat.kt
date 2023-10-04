package com.mvv.gui.words

import com.mvv.gui.util.getOrEmpty
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import com.opencsv.ICSVWriter
import mu.KotlinLogging
import org.apache.commons.lang3.EnumUtils
import java.io.BufferedReader
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Path
import java.util.EnumSet


private val log = KotlinLogging.logger {}


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
                    // Fields order: from, to, transcription, examples, statuses
                    val card = CardWordEntry("", "")
                    var index = 0
                    card.from = it[index++]
                    card.to = it.getOrEmpty(index++)
                    card.transcription = it.getOrEmpty(index++)
                    card.examples = it.getOrEmpty(index++)
                    card.statuses = stringToEnums(it.getOrEmpty(index++))
                    card.fromWithPreposition = it.getOrEmpty(index++)
                    card.predefinedSets = stringToEnums(it.getOrEmpty(index++))
                    card.sourcePositions = stringToInts(it.getOrEmpty(index++))
                    card.sourceSentences = it.getOrEmpty(index)
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
            words.forEach { card ->
                csvWriter.writeNext(arrayOf(
                    card.from,
                    card.to,
                    card.transcription,
                    card.examples,
                    card.statuses.joinToString(",") { it.name },
                    card.fromWithPreposition,
                    card.predefinedSets.joinToString(",") { it.name },
                    card.sourcePositions.joinToString(",") { it.toString() },
                    card.sourceSentences, // TODO: optimize it in some way to avoid having huge files
                ))
            }
        }
}


private fun stringToInts(string: String): List<Int> =
    stringToCollectionImpl(string, mutableListOf<Int>()) { it.toInt() }

private inline fun <reified T: Enum<T>> stringToEnums(string: String): Set<T> =
    stringToCollectionImpl(string, EnumSet.noneOf(T::class.java)) { EnumUtils.getEnum(T::class.java, it) }


private fun <T, C: MutableCollection<T>> stringToCollectionImpl(string: String, collection: C, converter: (String)->T): C {
    val parsed = string
        .splitToSequence(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map {
            try {
                converter(it)
            } catch (ex: Exception) {
                log.warn("Error of parsing [{}].", it); null
            }
        }
        .filterNotNull()
        .toSet()

    collection.addAll(parsed)
    return collection
}
