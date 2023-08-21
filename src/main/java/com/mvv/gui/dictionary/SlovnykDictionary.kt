package com.mvv.gui.dictionary

import com.opencsv.CSVReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.file.Path
import java.util.zip.GZIPInputStream
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.text.Charsets.UTF_8


data class SlovnykDictionarySource(val path: Path) {
    init {
        // Example: dictionaries/slovnyk/slovnyk_en-gb_ru-ru.csv.gz

        val fileNameLowercase = path.name.lowercase()

        require(fileNameLowercase.endsWith(".csv") || fileNameLowercase.endsWith(".csv.gz")) {
            "Path [$path] should have extension .csv or .csv.gz." }

        require(
            // one of subdirectory should contain 'slovnyk'
            path.any { it.name.lowercase().contains("slovnyk") }
            // or filename should contain 'slovnyk'
            || fileNameLowercase.contains("slovnyk")
        ) { "Path [$path] should contain 'slovnyk'." }
    }
}


// Example (slovnyk):
//  en-gb,ru-ru,"Aboard ship","Берег"
//  en-gb,ru-ru,"Aboard ship","Борт"
//
class SlovnykDictionary(private val source: SlovnykDictionarySource) : Dictionary {

    private val translations: Map<String, List<Translation>> = loadData()

    override fun toString(): String = "${javaClass.simpleName} { ${source.path} }"

    private fun loadData(): Map<String, List<Translation>> {
        val fileInputStream = FileInputStream(source.path.toFile())
        return fileInputStream.use {
            val isGzipFile = source.path.extension.lowercase() == "gz"
            val inStream = if (isGzipFile) GZIPInputStream(fileInputStream) else fileInputStream
            val csvReader = CSVReader(InputStreamReader(inStream, UTF_8))

            val translations = csvReader
                .map { Translation(it[2].lowercase(), it[2], it[3]) }
                .flatMap { splitToImplicits(it) }
                .groupBy { it.fromLowercase }

            translations
        }
    }

    override fun find(word: String): DictionaryEntry {
        val translations: List<Translation>? = translations[word]
        return if (translations == null) DictionaryEntry(word, null, emptyList())
               else DictionaryEntry(translations.first().from, null, translations.map { it.to }.distinct())
    }


    companion object {

        data class Translation (
            val fromLowercase: String,
            val from: String,
            val to: String,
        )

        private val possibleImplicitPrefixes = listOf("a ", "an ", "the ", "to ", "be ")

        private fun splitToImplicits(translation: Translation): List<Translation> {
            for (possibleImplicitPrefix in possibleImplicitPrefixes) {
                if (translation.fromLowercase.startsWith(possibleImplicitPrefix)) {
                    return listOf(translation, translation.copy(
                        fromLowercase = translation.fromLowercase.removePrefix(possibleImplicitPrefix)))
                }
            }
            return listOf(translation)
        }
    }
}
