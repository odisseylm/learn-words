package com.mvv.gui.dictionary

import com.mvv.gui.replaceFilenamesSuffix
import org.dict.kernel.DictEngine
import org.dict.kernel.IAnswer
import org.dict.kernel.IDatabase
import org.dict.server.DatabaseFactory
import java.nio.charset.Charset
import java.nio.file.Path
import java.util.*
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.name


/**
 * This class has prefix 'dict' because they work with 'DICT' dictionary format.
 */
data class DictDictionarySource (
    val id: String,
    val dictRootDirectory: Path,
    val dataFile: Path,
    val indexFile: Path,
    val dataEncoding: Charset = Charsets.UTF_8,
) {
    fun toProperties(): Properties {
        val props = Properties()

        props.setProperty("${id}.data", dataFile.absolutePathString())
        props.setProperty("${id}.index", indexFile.absolutePathString())
        props.setProperty("${id}.encoding", dataEncoding.name())

        return props
    }

    companion object {

        operator fun invoke(dictionaryId: String, dictRootDirectory: Path, dictionaryBaseFilename: String,
                            dataEncoding: Charset = Charsets.UTF_8): DictDictionarySource =
            DictDictionarySource(
                dictionaryId,
                dictRootDirectory,
                dictRootDirectory.resolve("${dictionaryBaseFilename}.dict.dz"),
                dictRootDirectory.resolve("${dictionaryBaseFilename}.index"),
                dataEncoding,
            )

        operator fun invoke(dictionaryIdAndBaseFilename: String, dictRootDirectory: Path,
                            dataEncoding: Charset = Charsets.UTF_8): DictDictionarySource =
            invoke(dictionaryIdAndBaseFilename, dictRootDirectory, dictionaryIdAndBaseFilename, dataEncoding)

        operator fun invoke(dictionariesRootDirectory: Path, dictionaryFile: Path, dataEncoding: Charset = Charsets.UTF_8): DictDictionarySource {

            val dictExt = "dict.dz"
            val dictIndexExt = "index"

            require(dictionaryFile.name.lowercase().endsWith(".$dictExt")) {
                "File [$dictionaryFile] does not have expected extension [.$dictExt]." }
            require(dictionaryFile.startsWith(dictionariesRootDirectory)) {
                "File [$dictionaryFile] is not located in dictionaries root directory [$dictionariesRootDirectory]." }
            require(dictionaryFile.exists()) {
                "File [$dictionaryFile] does not exist." }

            val indexFile = dictionaryFile.replaceFilenamesSuffix(".$dictExt", ".$dictIndexExt")
            require(indexFile.exists()) { "Index file [$dictionaryFile] does not exist." }

            val dictionaryId = dictionaryFile.subpath(dictionariesRootDirectory.nameCount, dictionaryFile.nameCount)
                .toString()
                .replace("/", "--")
                .replace("\\", "--")

            return DictDictionarySource(
                dictionaryId, dictionaryFile.parent,
                dictionaryFile, indexFile,
                dataEncoding,
            )
        }
    }
}


/**
 * This class has prefix 'dict' because they work with 'DICT' dictionary format.
 */
class DictDictionary(val source: DictDictionarySource) : Dictionary {

    private val database: IDatabase
    private val fEngine = DictEngine()

    init {
        database = DatabaseFactory.createDatabase(source.id, source.dictRootDirectory.toFile(), source.toProperties())
        fEngine.databases = arrayOf(database)
    }

    override fun toString(): String = "${javaClass.simpleName} { ${source.dataFile} }"

    override fun find(word: String): DictionaryEntry {

        var answers: Array<IAnswer> = fEngine.defineMatch(
            source.id, word, null, true, IDatabase.STRATEGY_EXACT)

        if (!hasValidAnswer(answers)) {
            answers = fEngine.defineMatch(source.id, word, null, true, IDatabase.STRATEGY_NONE)
        }

        if (!hasValidAnswer(answers)) {
            answers = fEngine.define(source.id, word)
        }

        if (!hasValidAnswer(answers)) {
            return DictionaryEntry(word, "", emptyList())
        }

        val asDictEntries = answers
            .filter { !it.definition.isNullOrBlank() }
            .map { parseDictDictionaryDefinition(word, it.definition) }

        return mergeDictionaryEntries(word, asDictEntries)
    }

    private fun hasValidAnswer(answers: Array<IAnswer>): Boolean =
        answers.any { !it.definition.isNullOrBlank() }
}


internal fun parseDictDictionaryDefinition(word: String, definition: String): DictionaryEntry {

    val translItems = definition.splitToSequence("\n\n")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .filter { it != word }
        .toList()

    if (translItems.isEmpty()) return DictionaryEntry(word, "", emptyList())

    val firstItem = translItems.first()
    val isFirstItemIsTranscription = firstItem.startsWith("[") && firstItem.contains("]")

    val translations: List<String>
    val transcription: String?

    if (isFirstItemIsTranscription) {
        transcription = firstItem.removeSuffix("_n.").removeSuffix("_n").trimEnd()
        translations = translItems.subList(1, translItems.size)
    } else {
        transcription = null
        translations = translItems
    }

    return DictionaryEntry(word, transcription, translations)
}
