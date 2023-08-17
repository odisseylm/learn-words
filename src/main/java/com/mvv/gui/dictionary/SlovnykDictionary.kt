package com.mvv.gui.dictionary

import com.opencsv.CSVReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.file.Path
import java.util.zip.GZIPInputStream
import kotlin.text.Charsets.UTF_8


data class SlovnykDictionarySource(
    val path: Path,
)


// Example (slovnyk):
//  en-gb,ru-ru,"Aboard ship","Берег"
//  en-gb,ru-ru,"Aboard ship","Борт"
//
class SlovnykDictionary(source: SlovnykDictionarySource) : Dictionary {

    private val translations: Map<String, List<Translation>>

    init {
        val fileInputStream = FileInputStream(source.path.toFile())
        fileInputStream.use {
            val csvReader = CSVReader(InputStreamReader(GZIPInputStream(fileInputStream), UTF_8))

            translations = csvReader
                .map { Translation(it[2].lowercase(), it[2], it[3]) }
                .flatMap { splitToImplicits(it) }
                .groupBy { it.fromLowercase }
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
