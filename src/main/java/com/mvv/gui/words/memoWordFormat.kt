package com.mvv.gui.words

import com.mvv.gui.util.getOrEmpty
import com.opencsv.*
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Path


//private val log = mu.KotlinLogging.logger {}


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
                    // Fields order: russian/to, english/from, transcription, examples, statuses
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

    val sortedWords = words.toList().sortedBy { it.baseWordAndFromProperty.value }

    FileWriter(file.toFile(), Charsets.UTF_8)
        .use { fw ->
            val csvWriter = memoWordCsvWriter(fw)
            sortedWords
                .filter { it.from.isNotBlank() }
                .forEach {
                    csvWriter.writeNext(arrayOf(
                        // in reversed order -> ru-en
                        formatWordOrPhraseToMemoWordFormat(minimizeTo(it.to)),
                        formatWordOrPhraseToMemoWordFormat(fixFrom(it.from)),
                        "", // part of speech
                        formatWordOrPhraseToMemoWordFormat(it.transcription + '\n' + it.examples),
                    ))
                }
        }
}

// TODO: it should be dynamic and exclude some verbs/prepositions/etc if they are present in card's set (in base form)
// TODO: PrefixFinder should not be global
private val temporaryGlobalPF = PrefixFinder()
fun String.calculateBaseOfFromForSorting(): String = temporaryGlobalPF.calculateBaseOfFromForSorting(this)


private val minimizableToConversions: List<Pair<String, String>> = listOf(
        listOf("что-либо", "чего-либо", "чему-либо", "чем-либо", "чём-либо") to "ч-л",
        listOf("что-л", "чего-л", "чему-л", "чем-л", "чём-л") to "ч-л",
        listOf("кто-либо", "кого-либо", "кому-либо", "кем-либо", "ком-либо") to "к-л",
        listOf("кто-л", "кого-л", "кому-л", "кем-л", "ком-л") to "к-л",
        listOf("какой-л", "какого-л", "каком-л", "какому-л", "каким-л") to "к-л",
    )
    .flatMap { p -> p.first.flatMap { listOf("$it." to p.second, it to p.second) }  } +
        listOf("ч-л." to "ч-л") +
        listOf("к-л." to "к-л") +
        listOf("(разг.)" to "(рз.)", "(_разг.)" to "(рз.)", "_разг." to "(рз.)", "_разг" to "(рз.)") +
        listOf("т. п." to "тп", "т.п." to "тп",)


// We use full form of 'somebody'/'something' since TTS (text-to-speech) does not support good pronunciation of smb/smth.
private val fixedFromConversions: List<Pair<String, String>> = listOf(
        listOf("smb.'s", "smb's.") to "somebody's",
        listOf("smb.", "smb") to "somebody",

        listOf("smth.'s", "smth's.", "smt.'s", "smt's.") to "something's",
        listOf("smth.", "smth", "smt.", "smt") to "something",
    )
    .flatMap { p -> p.first.map { it to p.second }  }

fun minimizeTo(to: String): String {
    var s = to
    minimizableToConversions.forEach { s = s.replace(it.first, it.second) }
    return s
}

fun fixFrom(from: String): String {
    var s = from
    fixedFromConversions.forEach { s = s.replace(it.first, it.second) }
    return s
}


// Not optimized but very-very simple approach :-)
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