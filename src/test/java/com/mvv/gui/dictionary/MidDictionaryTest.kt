package com.mvv.gui.dictionary

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MidDictionaryTest {

    private fun getProjectDir() = getProjectDirectory(this.javaClass)

    @Test
    fun findExisting1() {

        val dict = MidDictionary(MidDictionarySource(getProjectDir().resolve("dicts/DictionaryForMIDs_EngRus_Mueller.jar")))

        // dict.find("a") // fileIndex=1, Ok
        // dict.find("adobe") // fileIndex=2, Ok
        //dict.find("alien friend") // fileIndex=3, Ok

        // dict.find("allllien") // fileIndex=3, Ok

        val translation = dict.find("adobe")
        println(translation)
    }


    @Test
    fun findExisting2() {

        val dict = MidDictionary(MidDictionarySource(getProjectDir().resolve("dicts/DictionaryForMIDs_EngRus_Mueller.jar")))

        val dictEntry = dict.find("apple")
        println(dictEntry)

        assertThat(dictEntry).isNotNull
        assertThat(dictEntry.word).isEqualTo("apple")
        assertThat(dictEntry.transcription).isEqualTo("[01(ˈæpl)]")
        assertThat(dictEntry.translations).containsExactly(
            "[02 (n.)]",
            "1. яблоко",
            "2. яблоня",
            // not ideal...
            "apple of discord яблоко раздора; apple of one's eye а> зрачок; б> зеница ока;" +
                    " the rotten apple injures its neighbours [02 (посл.)] паршивая овца всё стадо портит",
        )
    }


    @Test
    fun findExisting2_caseInsensitive() {

        val dict = MidDictionary(MidDictionarySource(getProjectDir().resolve("dicts/DictionaryForMIDs_EngRus_Mueller.jar")))

        val dictEntry = dict.find("aPpLe")
        println(dictEntry)

        assertThat(dictEntry).isNotNull
        assertThat(dictEntry.word).isEqualTo("apple")
        assertThat(dictEntry.transcription).isEqualTo("[01(ˈæpl)]")
        assertThat(dictEntry.translations).containsExactly(
            "[02 (n.)]",
            "1. яблоко",
            "2. яблоня",
            // not ideal...
            "apple of discord яблоко раздора; apple of one's eye а> зрачок; б> зеница ока;" +
                    " the rotten apple injures its neighbours [02 (посл.)] паршивая овца всё стадо портит",
        )
    }


    @Test
    fun findNonExisting1() {

        val dict = MidDictionary(MidDictionarySource(getProjectDir().resolve("dicts/DictionaryForMIDs_EngRus_Mueller.jar")))

        val dictEntry = dict.find("NonExistentWord")
        println(dictEntry)

        assertThat(dictEntry).isNotNull
        assertThat(dictEntry.word).isEqualTo("nonexistentword")
        assertThat(dictEntry.transcription).isNull()
        assertThat(dictEntry.translations).isEmpty()
    }


    @Test
    fun findWithDuplicates() {

        val dict = MidDictionary(MidDictionarySource(getProjectDir().resolve("dicts/DictionaryForMIDs_EngRus_Mueller.jar")))

        val dictEntry = dict.find("un")
        println(dictEntry)

        assertThat(dictEntry).isNotNull
        assertThat(dictEntry.word).isEqualTo("un")
        assertThat(dictEntry.transcription).isEqualTo("[01(ʌn)]")
        assertThat(dictEntry.translations).containsExactlyInAnyOrder(
            "[02 (pref.)]",
            "1. придаёт глаголу противоположное значение: to undo уничтожать сделанное",
            "to undeceive выводить из заблуждения",
            "2. глаголам, образованным от существительных, придаёт обыкновенно значение лишать, освобождать от: to uncage выпускать из клетки",
            "to unmask снимать маску",
            "3. придаёт прилагательным, причастиям и существительным с их производными, а тж. наречиям отриц. значение не-, без-",
            "happy счастливый, unhappy несчастный; unhappily несчастливо; unsuccess неудача",
            "4. усиливает отриц. значение глагола, напр., to unloose",
            "[02 (разг.)] см. one 4",
        )
    }


    @Test
    fun findExisting_fromDfM_OmegaWiki_EngRus() {

        val dict = MidDictionary(MidDictionarySource(getProjectDir().resolve("dicts/DfM_OmegaWiki_EngRus_3.5.9.jar")))

        val dictEntry = dict.find("apple")
        println(dictEntry)

        assertThat(dictEntry).isNotNull
        assertThat(dictEntry.word).isEqualTo("apple")
        assertThat(dictEntry.transcription).isNull()
        assertThat(dictEntry.translations).containsExactly(
            "[02noun]",
            "[01 The popular, crisp, round fruit of the apple tree, usually with red, yellow or green skin, light-coloured flesh and pips inside.]",
            "яблоко",
            "[01 The wood of the apple tree.]",
            "яблоня",
        )
    }


    @Test
    fun findExisting_fromDfM_OmegaWiki_Eng__illumination() {

        val dict = MidDictionary(MidDictionarySource(getProjectDir().resolve("dicts/DfM_OmegaWiki_Eng_3.5.9.jar")))

        val dictEntry = dict.find("illumination")
        println(dictEntry)

        assertThat(dictEntry).isNotNull
        assertThat(dictEntry.word).isEqualTo("illumination")
        assertThat(dictEntry.transcription).isNull()
        assertThat(dictEntry.translations).containsExactlyInAnyOrder(
            "[01The act of illuminating, or supplying with light; the state of being illuminated.]",
        )
    }


    // TODO: to test ???  washer laundry machine washer	16-57682-S (with OmegaWiki_EngRus) ???
    // TODO: to test ???  washing machine clothes washer laundry machine washer	16-57682-B (with OmegaWiki_EngRus) ???

}
