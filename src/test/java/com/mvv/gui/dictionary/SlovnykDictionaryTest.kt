package com.mvv.gui.dictionary

import com.mvv.gui.getProjectDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test


class SlovnykDictionaryTest {

    companion object {
        lateinit var dict: SlovnykDictionary

        @BeforeAll
        @JvmStatic
        fun setUoBeforeAll() {
            dict = SlovnykDictionary(
                SlovnykDictionarySource(
                    getProjectDirectory().resolve("dicts/slovnyk/slovnyk_en-us_ru-ru.csv.gz")))
        }
    }


    @Test
    fun findByExactExact() {
        val dictEntry = dict.find("be cold")

        assertThat(dictEntry).isNotNull
        assertThat(dictEntry.word).isEqualTo("Be cold")
        assertThat(dictEntry.transcription).isNull() // this kind of dictionary does not have transcription
        assertThat(dictEntry.translations).containsExactlyInAnyOrder(
            "Замерзать",
            "Знобить",
            "Зябнуть",
            "Мерзнуть",
            "Морозить",
        )
    }


    @Test
    fun findByExactExact2() {
        val dictEntry = dict.find("to cool")

        assertThat(dictEntry).isNotNull
        assertThat(dictEntry.word).isEqualTo("To cool")
        assertThat(dictEntry.transcription).isNull() // this kind of dictionary does not have transcription
        assertThat(dictEntry.translations).containsExactlyInAnyOrder("Охлаждать")
    }


    @Test
    fun findByNotExact() {
        val dictEntry = dict.find("cool")

        assertThat(dictEntry).isNotNull

        // unclear which will be picked up first
        assertThat(dictEntry.word).isIn("Be cool", "Be Cool", "To cool") // unclear which will be picked up first

        assertThat(dictEntry.transcription).isNull() // this kind of dictionary does not have transcription

        assertThat(dictEntry.translations).containsExactlyInAnyOrder(
            "Охлаждать",
            "Прохладный",
            "Будь круче!",
        )
    }


    @Test
    fun findNotExistentWord() {
        val dictEntry = dict.find("NotExistentWord")

        assertThat(dictEntry).isNotNull
        assertThat(dictEntry.word).isEqualTo("NotExistentWord")
        assertThat(dictEntry.transcription).isNull()
        assertThat(dictEntry.translations).isEmpty()
    }


    @Test
    fun findApple() {
        val dictEntry = dict.find("apple")

        assertThat(dictEntry).isNotNull
        assertThat(dictEntry.word).isIn("The Apple", "an apple")
        assertThat(dictEntry.transcription).isNull()
        assertThat(dictEntry.translations).containsExactlyInAnyOrder(
            "Нью-Йорк",
            "Нью-йорк",
            "Яблоко",
        )
    }
}
