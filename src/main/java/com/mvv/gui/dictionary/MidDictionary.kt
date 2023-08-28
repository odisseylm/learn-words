package com.mvv.gui.dictionary

import com.mvv.gui.startsWithOneOf
import org.apache.commons.lang3.StringUtils
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.Comparator
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.streams.asSequence


data class MidDictionarySource (
    val jarDictionaryFile: Path,   // Example: dictionaries/DictionaryForMIDs_EngRus_Mueller.jar
) {

    init {
        val dictExt = "jar"

        require(jarDictionaryFile.extension.lowercase() == dictExt) {
            "File [$jarDictionaryFile] does not have expected extension [.$dictExt]." }
        require(jarDictionaryFile.exists()) {
            "File [$jarDictionaryFile] does not exist." }

        // Mandatory files
        // dictionary/searchlistEng.csv
        // dictionary/directory1.csv
        //
        require(
            jarContains(jarDictionaryFile, Path.of("dictionary/searchlistEng.csv"))) {
            "JAR file [$jarDictionaryFile] does not contains [dictionary/searchlistEng.csv]" }
        require(
            jarContains(jarDictionaryFile, Path.of("dictionary/directory1.csv"))) {
            "JAR file [$jarDictionaryFile] does not contains [dictionary/directory1.csv]" }
    }

}


class MidDictionary(val source: MidDictionarySource) : Dictionary {

    private val searchListEntries: List<SearchListEntry> = loadSearchListEntries()
    private val indexesMap: ConcurrentMap<Int, IndexFile> = ConcurrentHashMap()

    override fun toString(): String = "${javaClass.simpleName} { ${source.jarDictionaryFile} }"

    override fun find(word: String): DictionaryEntry = findImpl(word.trim().lowercase())

    private fun findImpl(word: String): DictionaryEntry {

        val searchIndexComparator =
            Comparator.comparing(SearchListEntry::startingFileWord, java.lang.String.CASE_INSENSITIVE_ORDER)
        val i = Collections.binarySearch(searchListEntries, SearchListEntry(word, -1), searchIndexComparator)

        val fileIndex = if (i >= 0) {
             searchListEntries[i].index
        }
        else {
            // if result is negative  =>  result = (-insertionPoint - 1)  =>  insertionPoint = -i - 1
            val insertionPoint = -i - 1
            val indexRange = insertionPoint - 1

            if (indexRange == -1) -1 else searchListEntries[indexRange].index
        }

        if (fileIndex == -1) return DictionaryEntry.empty(word)

        val indexFile: IndexFile = indexesMap.computeIfAbsent(fileIndex) { loadIndexFile(fileIndex) }

        val translations: List<DictionaryEntry> = indexFile.getIndex(word)
            .stream().asSequence()
            .flatMap { it.exactTranslations }
            .map { loadTranslation(it) }
            .toList()

        return mergeDictionaryEntries(word, translations)
    }

    private fun loadTranslation(index: TranslationFileIndex): DictionaryEntry {

        val bytesStream = getFileStream(Path.of("dictionary/directory${index.dictionaryFileNumber}.csv"))
        return bytesStream.use {
            it.skip(index.bytesOffset)
            val r = BufferedReader(InputStreamReader(bytesStream, Charsets.UTF_8))
            val translationLine = r.readLine()

            parseDictionaryEntry(translationLine)
        }
    }


    private fun loadSearchListEntries(): List<SearchListEntry> {
        val bytes = loadFileBytes(Path.of("dictionary/searchlistEng.csv"))
        val r = InputStreamReader(ByteArrayInputStream(bytes), Charsets.UTF_8)

        return r.readLines()
            .map { parseSearchListEntry(it) }
            .toList()
    }


    private fun loadIndexFile(indexFileNumber: Int): IndexFile {
        val indexFileEntries = loadFileLines(Path.of("dictionary/indexEng${indexFileNumber}.csv"))
            .filter { it.isNotBlank() }
            .map { parseIndexFileEntry(it) }
            .toList()
        return IndexFile(indexFileEntries)
    }


    private fun getFileStream(path: Path): InputStream = getFileStream(source.jarDictionaryFile, path)


    private fun loadFileBytes(path: Path): ByteArray =
        getFileStream(path).use { it.readAllBytes() }


    private fun loadFileLines(path: Path): Sequence<String> =
        BufferedReader(InputStreamReader(getFileStream(path))).lineSequence()


    companion object {
        internal data class SearchListEntry(val startingFileWord: String, val index: Int)

        internal data class IndexFileEntry(
            val word: String,
            val exactTranslations: List<TranslationFileIndex>,
            // ? References is probably bad naming ?
            val references: List<TranslationFileIndex>,
        )

        enum class DefinitionType {
            /** It is marked by letter B in index file. */
            Exact,
            // ? References is probably bad naming ?
            /** It is marked by letter S in index file. */
            Reference,
        }

        // Format in file: 5-3887-S,20-22027-B,82-13346-S,200-0-S
        internal data class TranslationFileIndex(val definitionType: DefinitionType, val dictionaryFileNumber: Int, val bytesOffset: Long)


        /**
         * @param indexes - should be sorted (it is already sorted in file)
         */
        private class IndexFile (private val indexes: List<IndexFileEntry>) {

            fun getIndex(wordOrPhrase: String): Optional<IndexFileEntry> {
                val wordComparator: Comparator<IndexFileEntry> = Comparator.comparing(IndexFileEntry::word)

                val wordOrPhraseLowerCase = wordOrPhrase.lowercase()
                val wordIndex: Int = Collections.binarySearch(indexes, IndexFileEntry(wordOrPhraseLowerCase, emptyList(), emptyList()), wordComparator)

                // TODO: use it BUT should it be it.word.compareTo(wordOrPhraseLowerCase) or opposite
                //val wordIndex: Int = indexes.binarySearch { it.word.compareTo(wordOrPhraseLowerCase) }

                return if (wordIndex < 0) Optional.empty() else Optional.of(indexes[wordIndex])
            }
        }


        private val spaceChars: CharArray = " \t".toCharArray()

        internal fun parseSearchListEntry(line: String): SearchListEntry {
            @Suppress("NAME_SHADOWING")
            val line = line.trim()

            val indexBeforeFileNumber = line.lastIndexOfAny(spaceChars)

            val fileIndexNumber = line.substring(indexBeforeFileNumber).trim().toInt()
            val startingWord = line.substring(0, indexBeforeFileNumber).trim()

            return SearchListEntry(startingWord, fileIndexNumber)
        }

        // Examples:
        //    'two bits	212-36328-B'
        //    'type	147-14230-S,204-2622-S,212-40166-B'
        internal fun parseIndexFileEntry(line: String): IndexFileEntry {

            @Suppress("NAME_SHADOWING")
            val line = line.trim()

            val indexOfIndexes = line.lastIndexOfAny(spaceChars)

            val wordOrPhrase = line.substring(0, indexOfIndexes).trimEnd()
            val rawIndexes = line.substring(indexOfIndexes + 1)
            val allIndexes: List<TranslationFileIndex> = rawIndexes
                .splitToSequence(",")
                .map { parseIndexToDict(it) }
                .toList()

            val exactTranslations = allIndexes.filter { it.definitionType == DefinitionType.Exact }
            val referencesTranslations = allIndexes.filter { it.definitionType == DefinitionType.Exact }

            return IndexFileEntry(wordOrPhrase, exactTranslations, referencesTranslations)
        }

        // Examples: 212-36328-B, 147-14230-S, 204-2622-S, 212-40166-B
        private fun parseIndexToDict(strIndexToDict: String): TranslationFileIndex {
            val parts = strIndexToDict.split('-')

            val definitionType: DefinitionType = when (val strDefType = parts[2]) {
                "B" -> DefinitionType.Exact
                "S" -> DefinitionType.Reference
                else  -> throw IllegalArgumentException("Unexpected DefinitionType [${strDefType}] (expected B or S).")
            }

            return TranslationFileIndex(definitionType, parts[0].toInt(), parts[1].toLong())
        }


        // Examples (with different format):
        //  infallible	[01(ınˈfæləbl)]\n [02 (a.)] \n1. безошибочный, непогрешимый; none of us is infallible всем нам свойственно ошибаться \n2. надёжный, верный; infallible success безусловный (успех)
        //
        //  apple[01\nThe wood of the apple tree.]	яблоня
        //
        //  [02noun] apple[01\nThe popular, crisp, round fruit of the apple tree, usually with red, yellow or green skin, light-coloured flesh and pips inside.]	яблоко
        //
        private fun parseDictionaryEntry(translationLine: String): DictionaryEntry {

            // TODO: refactor this piece of shit

            // [02noun] apple[01\nThe popular, crisp, round fruit of the apple tree, usually with red, yellow or green skin, light-coloured flesh and pips inside.]	яблоко
            val isAltFormat1 = translationLine.startsWith("[0")
            if (isAltFormat1) {

                // [02noun] [02verb] [02adjective] [02adverb] [02name]
                val items = mutableListOf<String>()

                val i1_2 = translationLine.indexOf(']')
                val s1 = translationLine.substring(0, i1_2 + 1)
                items.add(s1.trim().fixEscapedSpaceChars())

                var word = ""

                var remaining = translationLine.substring(i1_2 + 1)
                if (remaining.contains("[0")) {
                    val i2_1 = remaining.indexOf("[0")
                    val i2_2 = remaining.indexOf("]", i2_1)

                    word = remaining.substring(0, i2_1).trim().fixEscapedSpaceChars()

                    if (i2_2 != -1) {
                        val s2 = remaining.substring(i2_1, i2_2 + 1)
                        items.add(s2.trim().fixEscapedSpaceChars())

                        remaining = remaining.substring(i2_2 + 1)
                    }
                }

                remaining.splitToSequence("\\t", "\\n", "\t", "\n")
                    .map { it.trim().fixEscapedSpaceChars() }
                    .filter { it.isNotEmpty() }
                    .forEach { items.add(it) }

                return DictionaryEntry(word, null, items)
            }

            val transcriptionStart = translationLine.indexOf('[')
            val transcriptionEnd = translationLine.indexOf(']', transcriptionStart)

            val word = translationLine.substring(0, transcriptionStart)
            val transcriptionOrDescription = translationLine.substring(transcriptionStart, transcriptionEnd + 1).fixEscapedSpaceChars()
            val definition = translationLine.substring(transcriptionEnd + 1)
                .trimStart().removePrefix("\\n").trimStart()

            val translations = definition.splitToSequence("\\n")
                .map { it.trim().fixEscapedSpaceChars() }
                .filter { it.isNotEmpty() }
                .flatMap { splitTranslationToMainAndRemark(it) }
                .filter { it.isNotEmpty() }
                .toMutableList()

            val hasTranscription = transcriptionOrDescription.endsWith(")]")
                    && !transcriptionOrDescription.startsWithOneOf(
                        "[0 ", "[0\n", "[01 ", "[01\n", "[02 ", "[02\n", "[03 ", "[03\n", )
            val transcription = if (hasTranscription) transcriptionOrDescription else null

            if (!hasTranscription) translations.add(0, transcriptionOrDescription)

            return DictionaryEntry(word, transcription, translations)
        }

        private fun splitTranslationToMainAndRemark(definition: String): List<String> {
            val remarksIndex = definition.indexOf(';')
            return if (remarksIndex == -1) listOf(definition)
                   else listOf(
                          definition.substring(0, remarksIndex).trim(),
                          definition.substring(remarksIndex + 1).trim()
                   )
        }

        private fun String.fixEscapedSpaceChars(): String {
            val s1 = StringUtils.replace(this, "\\n", " ") // ? or we can use '\n'
            return StringUtils.replace(s1, "\\t", " ") // ? or we can use '\t'
        }
    }

}


private fun getFileStream(jarFile: Path, pathInArchive: Path): InputStream =
    //if (jarFile.exists()) FileInputStream(jarFile.toFile())
    //else URL("jar:file:${jarFile}!/${pathInArchive}").openStream()
    URL("jar:file:${jarFile}!/${pathInArchive}").openStream()


private fun jarContains(jarFile: Path, pathInArchive: Path): Boolean =
    jarFile.exists() && getFileStream(jarFile, pathInArchive).use { it.read() != -1 }
