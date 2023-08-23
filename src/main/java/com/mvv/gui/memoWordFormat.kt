package com.mvv.gui

import com.opencsv.*
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Path


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


private fun memoWordCsvWriter(fileWriter: FileWriter): ICSVWriter =
    CSVWriterBuilder(fileWriter)
        .withSeparator(';')
        .withEscapeChar(ICSVWriter.NO_ESCAPE_CHARACTER)
        .withQuoteChar(ICSVWriter.NO_QUOTE_CHARACTER)
        .build()


fun loadWordsFromMemoWordCsv(file: Path): List<String> =
    FileReader(file.toFile(), Charsets.UTF_8)
        .use { fileReader ->
            memoWordCsvReader(fileReader)
                .map { it.getOrEmpty(1) }
                .filter { it.isNotBlank() }
                .toList()
        }


fun loadWordCardsFromMemoWordCsv(file: Path): List<CardWordEntry> =
    FileReader(file.toFile(), Charsets.UTF_8)
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
                    val transcriptionAndExamples = splitTransactionAndExamples(it.getOrEmpty(index))
                    card.transcription = transcriptionAndExamples.first
                    card.examples = transcriptionAndExamples.second
                    card
                }
                .filter { it.from.isNotBlank() }
                .toList()
        }


fun saveWordCardsIntoMemoWordCsv(file: Path, words: Iterable<CardWordEntry>) {
    if (!words.iterator().hasNext()) throw IllegalArgumentException("Nothing to save to [$file].")
    Files.createDirectories(file.parent)

    FileWriter(file.toFile(), Charsets.UTF_8)
        .use { fw ->
            val csvWriter = memoWordCsvWriter(fw)
            words
                .filter { it.from.isNotBlank() }
                .forEach {
                    csvWriter.writeNext(arrayOf(
                        // in reversed order -> ru-en
                        formatWordOrPhraseToMemoWordFormat(it.to),
                        formatWordOrPhraseToMemoWordFormat(it.from),
                        "", // part of speech
                        formatWordOrPhraseToMemoWordFormat(it.transcription + '\n' + it.examples),
                    ))
                }
        }
}


fun formatWordOrPhraseToMemoWordFormat(wordOrPhrase: String): String =
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


private fun splitTransactionAndExamples(memoWordTransactionAndExamples: String): Pair<String, String> {

    val s = memoWordTransactionAndExamples.trim()

    if (!s.startsWith("[0") && s.startsWith("[")) {
        val possiblyEndOfTranscription = s.indexOf(']')
        if (possiblyEndOfTranscription == -1) {
            return Pair("", s)
        }

        val possiblyTranscription = s.substring(0, possiblyEndOfTranscription + 1)
        val examples = s.substring(possiblyEndOfTranscription + 1)
            .trim().removePrefix(",").trim()
        return Pair(possiblyTranscription, examples)
    }

    return Pair("", s)
}