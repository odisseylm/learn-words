package com.mvv.gui.dictionary

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class DictionaryCompositionTest {

    companion object {
        private val projectDirectory = getProjectDirectory(this::class.java)

        private val allDictionaries: List<Dictionary> = listOf(
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

        private val dictionaryComposition = DictionaryComposition(allDictionaries)
    }


    @Test
    fun find() {
        val dictEntry = dictionaryComposition.find("apple", false)

        assertThat(dictEntry).isNotNull
        assertThat(dictEntry.word).isEqualTo("apple")
        assertThat(dictEntry.transcription).isIn("[ˈæpl][01(ˈæpl)]", "[01(ˈæpl)][ˈæpl]") // TODO: improve transcription comparing
        assertThat(dictEntry.translations).containsExactly(
            "1) яблоко",
            "2) яблоня",
            "*) apple of discord яблоко раздора; apple of one's eye а) зрачок;\n" +
                    "    б) зеница ока; the rotten apple injures its neighbours _посл. паршивая\n" +
                    "    овца всё стадо портит",
            "Нью-Йорк",
            "[02 (n.)]",
            "apple of discord яблоко раздора; apple of one's eye а> зрачок; б> зеница ока; the rotten apple injures its neighbours [02 (посл.)] паршивая овца всё стадо портит",
            "[01 The popular, crisp, round fruit of the apple tree, usually with red, yellow or green skin, light-coloured flesh and pips inside.]",
            "[01 The wood of the apple tree.]",
        )
    }


    @Test
    fun find2() {
        val dictEntry = dictionaryComposition.find("chemical formula", false)

        assertThat(dictEntry).isNotNull
        assertThat(dictEntry.word).isEqualTo("chemical formula")
        assertThat(dictEntry.transcription).isNull()
        assertThat(dictEntry.translations).containsExactly(
            "Химическая формула",
            "[01 A way of expressing information about the atoms that constitute a particular chemical compound, and how the relationship between those atoms changes in chemical reactions.]",
        )
    }


    @Test
    fun findFor_illumination() {
        val dictEntry = dictionaryComposition.find("illumination", false)

        assertThat(dictEntry).isNotNull
        assertThat(dictEntry.word).isEqualTo("illumination")
        assertThat(dictEntry.transcription).isEqualTo("[ɪˌlju:mɪˈneɪʃən][01(ıˌljuːmıˈneıʃən)]")
        assertThat(dictEntry.translations).containsExactly(
            "1) освещение",
            "2) _эл. освещённость",
            "3) яркость",
            "4) (обыкн. _pl.) иллюминация",
            "5) _pl. украшения и рисунки в рукописи; раскраска",
            "6) вдохновение",
            "7) _attr. осветительный; illumination engineering осветительная\n    техника",
            "Иллюминация",
            "Освещение в фотографии",
            "[02 (n.)]",
            "2. [02 (эл.)] освещённость",
            "4. (обыкн. [02 (pl.)]) иллюминация",
            "5. [02 (pl.)] украшения и рисунки в рукописи",
            "раскраска",
            "7. [02 (attr.)] осветительный",
            "illumination engineering осветительная техника",
            "[01The act of illuminating, or supplying with light; the state of being illuminated.]",
        )
    }
}
