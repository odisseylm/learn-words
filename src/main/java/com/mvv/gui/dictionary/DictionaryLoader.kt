package com.mvv.gui.dictionary

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.streams.asSequence


interface DictionariesLoader {
    fun load(): List<Dictionary>
}



@Suppress("unused")
class AutoDictionariesLoader : DictionariesLoader {

    private val loaders: List<SpecificDictionaryLoader> = listOf(
        dictLoader(
            { dir, file -> DictDictionarySource(dir, file) },
            { dir, file -> DictDictionary(DictDictionarySource(dir, file)) }),
        dictLoader(
            { _, file -> MidDictionarySource(file) },
            { _, file -> MidDictionary(MidDictionarySource(file)) }),
        dictLoader(
            { _, file -> SlovnykDictionarySource(file) },
            { _, file -> SlovnykDictionary(SlovnykDictionarySource(file)) }),
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
            .find { it.isValid(dictionariesRootDirectory, dictionaryFile) }
            ?.load(dictionariesRootDirectory, dictionaryFile)
}



// TODO: try to avoid this interface
internal interface SpecificDictionaryLoader {
    fun isValid(dictionariesRootDirectory: Path, dictionaryFile: Path): Boolean
    fun load(dictionariesRootDirectory: Path, dictionaryFile: Path): Dictionary
}


private fun <DictSource> dictLoader(dictSourceCreator: (Path,Path)->DictSource, dictCreator: (Path,Path)->Dictionary) =
  object : SpecificDictionaryLoader {
    override fun isValid(dictionariesRootDirectory: Path, dictionaryFile: Path): Boolean =
        try { dictSourceCreator(dictionariesRootDirectory, dictionaryFile); true }
        catch (ignore: Exception) { false }

    override fun load(dictionariesRootDirectory: Path, dictionaryFile: Path): Dictionary =
        dictCreator(dictionariesRootDirectory, dictionaryFile)
}



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
