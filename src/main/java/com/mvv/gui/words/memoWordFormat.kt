package com.mvv.gui.words

import com.mvv.gui.util.getOrEmpty
import com.mvv.gui.util.removeCharSuffixesRepeatably
import com.mvv.gui.util.safeSubstring
import com.opencsv.*
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.usermodel.Workbook.MAX_SENSITIVE_SHEET_NAME_LEN
import org.apache.poi.util.Units
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.min
import kotlin.math.round


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
                        formatWordOrPhraseToCsvMemoWordFormat(minimizeTo(it.to)),
                        formatWordOrPhraseToCsvMemoWordFormat(fixFrom(it.from)),
                        "", // part of speech
                        formatWordOrPhraseToCsvMemoWordFormat(it.transcription + '\n' + it.examples),
                    ))
                }
        }
}

fun saveWordCardsIntoMemoWordXlsx(file: Path, words: Iterable<CardWordEntry>) {
    if (!words.iterator().hasNext()) throw IllegalArgumentException("Nothing to save to [$file].")
    Files.createDirectories(file.parent)

    // xlsx - application/vnd.openxmlformats-officedocument.spreadsheetml.sheet

    val sortedWords = words.toList()
        .filter { it.from.isNotBlank() }
        .sortedBy { it.baseWordAndFromProperty.value }

    val workbook = XSSFWorkbook()
    val sheet = workbook.createSheet(file.baseWordsFilename.safeSubstring(0, MAX_SENSITIVE_SHEET_NAME_LEN))

    val toCellIndex = 0
    val fromCellIndex = 1 // in reversed order -> ru-en
    val speechPartCellIndex = 2
    val remarksCellIndex = 3

    val defaultWideColumnWidthInChars = 40
    val defaultFromColumnWidthInChars = 25 // really 34 chars ??
    val defaultSpeechPartColumnWidthInChars = 10

    val defaultWideColumnWidth = calculateExcelColumnWidth(defaultWideColumnWidthInChars)
    val defaultFromColumnWidth = calculateExcelColumnWidth(defaultFromColumnWidthInChars)
    val defaultSpeechPartColumnWidth = calculateExcelColumnWidth(defaultSpeechPartColumnWidthInChars)

    sheet.setColumnWidth(toCellIndex, defaultWideColumnWidth)
    sheet.setColumnWidth(fromCellIndex, defaultFromColumnWidth)
    sheet.setColumnWidth(speechPartCellIndex, defaultSpeechPartColumnWidth)
    sheet.setColumnWidth(remarksCellIndex, defaultWideColumnWidth)

    val bigCellStyle = workbook.createCellStyle()
    bigCellStyle.wrapText = true
    bigCellStyle.verticalAlignment = VerticalAlignment.TOP
    bigCellStyle.alignment = HorizontalAlignment.LEFT

    val shortCellStyle = workbook.createCellStyle()
    shortCellStyle.wrapText = true
    shortCellStyle.verticalAlignment = VerticalAlignment.CENTER
    shortCellStyle.alignment = HorizontalAlignment.LEFT

    var index = 0

    /*
    val header = sheet.createRow(index++)
    header.createCell(toCellIndex).also { it.setCellValue("ru") }
    header.createCell(fromCellIndex).also { it.setCellValue("en") }
    header.createCell(speechPartCellIndex).also { it.setCellValue("part") }
    header.createCell(remarksCellIndex).also { it.setCellValue("remarks") }
    sheet.createRow(index++) // empty splitting row
    */

    sortedWords
        .forEach { card ->
            val row = sheet.createRow(index++)

            val from = fixFrom(card.from)
            val to = adoptWordOrPhraseToXlsxMemoWordFormat(minimizeTo(card.to))
            val remarks = adoptWordOrPhraseToXlsxMemoWordFormat(card.transcription + '\n' + card.examples)

            row.createCell(toCellIndex)        .also { it.setCellValue(to);      it.cellStyle = bigCellStyle   }
            row.createCell(fromCellIndex)      .also { it.setCellValue(from);    it.cellStyle = shortCellStyle }
            row.createCell(speechPartCellIndex).also { it.setCellValue("");      it.cellStyle = shortCellStyle }
            row.createCell(remarksCellIndex)   .also { it.setCellValue(remarks); it.cellStyle = bigCellStyle   }

            val cellHeightInPoints = listOf(
                sheet.calculateCellHeightInPoints(from, defaultFromColumnWidthInChars),
                sheet.calculateCellHeightInPoints(to, defaultWideColumnWidthInChars),
                sheet.calculateCellHeightInPoints(remarks, defaultWideColumnWidthInChars),
            )

            cellHeightInPoints
                .filterNotNull()
                .maxOrNull()
                ?.also { row.heightInPoints = it }
        }

    // should be called AFTER filling data
    sheet.autoSizeColumn(fromCellIndex)
    sheet.autoSizeColumn(toCellIndex)
    //sheet.autoSizeColumn(speechPartCellIndex)
    sheet.autoSizeColumn(remarksCellIndex)

    if (sheet.getColumnWidth(fromCellIndex) > defaultFromColumnWidth)
        sheet.setColumnWidth(fromCellIndex, defaultFromColumnWidth)
    if (sheet.getColumnWidth(toCellIndex) > defaultWideColumnWidth)
        sheet.setColumnWidth(toCellIndex, defaultWideColumnWidth)
    if (sheet.getColumnWidth(speechPartCellIndex) > defaultSpeechPartColumnWidth)
        sheet.setColumnWidth(speechPartCellIndex, defaultSpeechPartColumnWidth)
    if (sheet.getColumnWidth(remarksCellIndex) > defaultWideColumnWidth)
        sheet.setColumnWidth(remarksCellIndex, defaultWideColumnWidth)

    FileOutputStream(file.toFile()).use { workbook.write(it) }
}


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
        listOf("т. п." to "тп", "т.п." to "тп")


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
// TODO: rewrite it using for-each and remove all unneeded chars in effective way
fun formatWordOrPhraseToCsvMemoWordFormat(wordOrPhrase: String): String =
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


fun adoptWordOrPhraseToXlsxMemoWordFormat(wordOrPhrase: String): String {

    val lineCount = wordOrPhrase.lineCount()

    return if (lineCount < 5) wordOrPhrase
        else wordOrPhrase
            .replace("\n ", ", ")
            .replace("\n", ", ")
            .replace(", , ", ", ")
            .replace(", ,", ", ")
            .replace(",, ", ", ")
            .replace(", ", ", ")
            .removeCharSuffixesRepeatably(",;")
            .trim()
    }

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


// Seems it does not work as I expect... What is char width there?
private fun calculateExcelColumnWidth(widthInChars: Int): Int =
    when {
        widthInChars > 254  -> 65280 // Maximum allowed column width.
        widthInChars > 1    -> {
            //val floor = floor(widthInChars / 5.0)
            //val factor = 30.0 * floor
            //val value = 450 + factor + ((widthInChars - 1) * 250)
            //value.toInt()
            round((widthInChars * Units.DEFAULT_CHARACTER_WIDTH + 5.0) / Units.DEFAULT_CHARACTER_WIDTH * 256.0).toInt()
        }
        else -> 450 // default to column size 1 if zero, one or negative number
    }


private fun Sheet.calculateCellHeightInPoints(value: String, cellWidthInChars: Int): Float? {
    var lineCount = min(value.trim().lineCount(), 20)
    if (lineCount <= 1)
        lineCount = round(value.length / cellWidthInChars.toFloat()).toInt()

    return when {
        lineCount <= 1 -> null
        lineCount < 5  -> (lineCount + 0.5f) * defaultRowHeightInPoints
        else           -> lineCount * defaultRowHeightInPoints * 0.6f
    }
}


private fun CharSequence.lineCount(): Int {
    var lineCount = 0
    for (ch in this)
        if (ch == '\n') lineCount++
    return lineCount
}
