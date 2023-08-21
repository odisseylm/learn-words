package com.mvv.gui.dictionary

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.streams.asSequence


interface DictionariesLoader {
    fun load(): List<Dictionary>
}


typealias DictionaryLoader = (dictionariesRootDirectory: Path, dictionaryFile: Path)->Dictionary?


@Suppress("unused")
class AutoDictionariesLoader : DictionariesLoader {

    private val loaders: List<DictionaryLoader> = listOf(
        { dir, file -> tryToDo<Dictionary> { DictDictionary(DictDictionarySource(dir, file)) } },
        { _, file   -> tryToDo<Dictionary> { MidDictionary(MidDictionarySource(file)) } },
        { _, file   -> tryToDo<Dictionary> { SlovnykDictionary(SlovnykDictionarySource(file)) } },
    )

    override fun load(): List<Dictionary> {
        val projectDir = getProjectDirectory(this.javaClass)
        return loadFromDirectory(projectDir.resolve("dicts")) + loadFromDirectory(projectDir.resolve("dictionaries"))
    }

    private fun loadFromDirectory(dictionariesRootDir: Path): List<Dictionary> =
        if (dictionariesRootDir.exists())
            Files.walk(dictionariesRootDir)
                .use { filesStream ->
                    filesStream
                        .asSequence()
                        .sorted()
                        .map { tryToLoadDictionary(dictionariesRootDir, it) }
                        .filterNotNull()
                        .toList()
                }
        else emptyList()

    private fun tryToLoadDictionary(dictionariesRootDirectory: Path, dictionaryFile: Path): Dictionary? =
        loaders
            .asSequence()
            .map { it(dictionariesRootDirectory, dictionaryFile) }
            .find { it != null}
}



private fun <T> tryToDo(action: ()->T): T? = try { action() } catch (ignore: Exception) { null }



@Suppress("unused")
class HardcodedDictionariesLoader : DictionariesLoader {
    override fun load(): List<Dictionary> {
        val projectDirectory = getProjectDirectory(this.javaClass)

        return listOf(
            DictDictionary(DictDictionarySource("mueller-base",
                projectDirectory.resolve("dicts/mueller-dict-3.1.1/dict"))),
            DictDictionary(DictDictionarySource("mueller-dict",
                projectDirectory.resolve("dicts/mueller-dict-3.1.1/dict"))),
            DictDictionary(DictDictionarySource("mueller-abbrev",
                projectDirectory.resolve("dicts/mueller-dict-3.1.1/dict"))),
            DictDictionary(DictDictionarySource("mueller-names",
                projectDirectory.resolve("dicts/mueller-dict-3.1.1/dict"))),
            DictDictionary(DictDictionarySource("mueller-geo",
                projectDirectory.resolve("dicts/mueller-dict-3.1.1/dict"))),

            SlovnykDictionary(SlovnykDictionarySource(
                projectDirectory.resolve("dicts/slovnyk/slovnyk_en-gb_ru-ru.csv.gz"))),
            SlovnykDictionary(SlovnykDictionarySource(
                projectDirectory.resolve("dicts/slovnyk/slovnyk_en-us_ru-ru.csv.gz"))),

            MidDictionary(MidDictionarySource(
                projectDirectory.resolve("dicts/DictionaryForMIDs_EngRus_Mueller.jar"))),
            MidDictionary(MidDictionarySource(
                projectDirectory.resolve("dicts/DfM_OmegaWiki_EngRus_3.5.9.jar"))),
            MidDictionary(MidDictionarySource(
                projectDirectory.resolve("dicts/DfM_OmegaWiki_Eng_3.5.9.jar"))),
        )
    }
}
