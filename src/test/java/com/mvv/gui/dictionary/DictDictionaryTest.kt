package com.mvv.gui.dictionary

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


class DictDictionaryTest {

    @Test
    @DisplayName("parseDictDictionaryDefinition full definition")
    fun test_parseDictDictionaryDefinition_fullDefinition() {

        val appleMuellerDefinition =
            "apple\n" +
            "\n" +
            "  [ˈæpl] _n.\n" +
            "\n" +
            "    1) яблоко\n" +
            "\n" +
            "    2) яблоня\n" +
            "\n" +
            "    *) apple of discord яблоко раздора; apple of one's eye а) зрачок;\n" +
            "    б) зеница ока; the rotten apple injures its neighbours _посл. паршивая\n" +
            "    овца всё стадо портит\n" +
            "\n"


        val dictEntry = parseDictDictionaryDefinition("apple", appleMuellerDefinition)

        assertThat(dictEntry).isNotNull
        assertThat(dictEntry.word).isEqualTo("apple")
        assertThat(dictEntry.transcription).isEqualTo("[ˈæpl]")
        assertThat(dictEntry.translations).containsExactly(
            "1) яблоко",
            "2) яблоня",
            "*) apple of discord яблоко раздора; apple of one's eye а) зрачок;\n" +
                "    б) зеница ока; the rotten apple injures its neighbours _посл. паршивая\n" +
                "    овца всё стадо портит",
        )
    }

    @Test
    @DisplayName("parseDictDictionaryDefinition without word at the beginning")
    fun test_parseDictDictionaryDefinition_withoutWordAtTheBeginning() {

        val appleMuellerDefinition =
            "  [ˈæpl] _n.\n" +
            "\n" +
            "    1) яблоко\n" +
            "\n" +
            "    2) яблоня\n" +
            "\n" +
            "    *) apple of discord яблоко раздора; apple of one's eye а) зрачок;\n" +
            "    б) зеница ока; the rotten apple injures its neighbours _посл. паршивая\n" +
            "    овца всё стадо портит\n" +
            "\n"


        val dictEntry = parseDictDictionaryDefinition("apple", appleMuellerDefinition)

        assertThat(dictEntry).isNotNull
        assertThat(dictEntry.word).isEqualTo("apple")
        assertThat(dictEntry.transcription).isEqualTo("[ˈæpl]")
        assertThat(dictEntry.translations).containsExactly(
            "1) яблоко",
            "2) яблоня",
            "*) apple of discord яблоко раздора; apple of one's eye а) зрачок;\n" +
                "    б) зеница ока; the rotten apple injures its neighbours _посл. паршивая\n" +
                "    овца всё стадо портит",
        )
    }

    @Test
    @DisplayName("parseDictDictionaryDefinition without transcription")
    fun test_parseDictDictionaryDefinition_withoutTranscription() {

        val appleMuellerDefinition =
            "apple\n" +
            "\n" +
            "    1) яблоко\n" +
            "\n" +
            "    2) яблоня\n" +
            "\n" +
            "    *) apple of discord яблоко раздора; apple of one's eye а) зрачок;\n" +
            "    б) зеница ока; the rotten apple injures its neighbours _посл. паршивая\n" +
            "    овца всё стадо портит\n" +
            "\n"


        val dictEntry = parseDictDictionaryDefinition("apple", appleMuellerDefinition)

        assertThat(dictEntry).isNotNull
        assertThat(dictEntry.word).isEqualTo("apple")
        assertThat(dictEntry.transcription).isNull()
        assertThat(dictEntry.translations).containsExactly(
            "1) яблоко",
            "2) яблоня",
            "*) apple of discord яблоко раздора; apple of one's eye а) зрачок;\n" +
                "    б) зеница ока; the rotten apple injures its neighbours _посл. паршивая\n" +
                "    овца всё стадо портит",
        )
    }

    @Test
    @DisplayName("parseDictDictionaryDefinition without word at the beginning and transcription")
    fun test_parseDictDictionaryDefinition_withoutWordAtTheBeginningAndTranscription() {

        val appleMuellerDefinition =
            "    1) яблоко\n" +
            "\n" +
            "    2) яблоня\n" +
            "\n" +
            "    *) apple of discord яблоко раздора; apple of one's eye а) зрачок;\n" +
            "    б) зеница ока; the rotten apple injures its neighbours _посл. паршивая\n" +
            "    овца всё стадо портит\n" +
            "\n"


        val dictEntry = parseDictDictionaryDefinition("apple", appleMuellerDefinition)

        assertThat(dictEntry).isNotNull
        assertThat(dictEntry.word).isEqualTo("apple")
        assertThat(dictEntry.transcription).isNull()
        assertThat(dictEntry.translations).containsExactly(
            "1) яблоко",
            "2) яблоня",
            "*) apple of discord яблоко раздора; apple of one's eye а) зрачок;\n" +
                "    б) зеница ока; the rotten apple injures its neighbours _посл. паршивая\n" +
                "    овца всё стадо портит",
        )
    }


    @Test
    fun findInDictDictionary() {

        val dictionaryRootDir = getProjectDir().resolve("dicts/mueller-dict-3.1.1/dict")
        val dict = DictDictionary(DictDictionarySource(
            "mueller-base",
            dictionaryRootDir,
            dictionaryRootDir.resolve("mueller-base.dict.dz"),
            dictionaryRootDir.resolve("mueller-base.index"),
        ))

        val dictEntry = dict.find("apple")

        assertThat(dictEntry).isNotNull
        assertThat(dictEntry.word).isEqualTo("apple")
        assertThat(dictEntry.transcription).isEqualTo("[ˈæpl]")
        assertThat(dictEntry.translations).containsExactly(
            "1) яблоко",
            "2) яблоня",
            "*) apple of discord яблоко раздора; apple of one's eye а) зрачок;\n" +
                    "    б) зеница ока; the rotten apple injures its neighbours _посл. паршивая\n" +
                    "    овца всё стадо портит",
            )

    }


    @Test
    fun findInDictDictionary_forIllumination() {

        val dictionaryRootDir = getProjectDir().resolve("dicts/mueller-dict-3.1.1/dict")
        val dict = DictDictionary(DictDictionarySource(
            "mueller-base",
            dictionaryRootDir,
            dictionaryRootDir.resolve("mueller-base.dict.dz"),
            dictionaryRootDir.resolve("mueller-base.index"),
        ))

        val dictEntry = dict.find("illumination")

        assertThat(dictEntry).isNotNull
        assertThat(dictEntry.word).isEqualTo("illumination")
        assertThat(dictEntry.transcription).isEqualTo("[ıˌlju:mıˈneıʃən]")
        assertThat(dictEntry.translations).containsExactly(
            "1) освещение",
            "2) _эл. освещённость",
            "3) яркость",
            "4) (обыкн. _pl.) иллюминация",
            "5) _pl. украшения и рисунки в рукописи; раскраска",
            "6) вдохновение",
            // It would be nice to fix this unneeded '\n    '
            "7) _attr. осветительный; illumination engineering осветительная\n    техника",
            )

    }


    @Test
    fun findInDictDictionary_forEnjoyable() {

        val dictionaryRootDir = getProjectDir().resolve("dicts/mueller-dict-3.1.1/dict")
        val dict = DictDictionary(DictDictionarySource(
            "mueller-base",
            dictionaryRootDir,
            dictionaryRootDir.resolve("mueller-base.dict.dz"),
            dictionaryRootDir.resolve("mueller-base.index"),
        ))

        val dictEntry = dict.find("enjoyable")

        assertThat(dictEntry).isNotNull
        assertThat(dictEntry.word).isEqualTo("enjoyable")
        assertThat(dictEntry.transcription).isEqualTo("[ınˈdʒɔıəbl]")
        assertThat(dictEntry.translations).containsExactly("_a. приятный, доставляющий удовольствие")

    }

    private fun getProjectDir() = getProjectDirectory(this.javaClass)

}
