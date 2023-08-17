package com.mvv.gui.dictionary

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class DictionaryTest {

    @Test
    fun mergeDictionaryEntries() {

        val merged = mergeDictionaryEntries("apple", listOf(
            DictionaryEntry("apple", "[ˈæpl]", listOf("1) яблоко", "2) яблоня")),
            DictionaryEntry("apple", "[ˈæpl]", listOf("1) яблоко", "2) яблоня")),
        ))

        assertThat(merged).isNotNull
        assertThat(merged.word).isEqualTo("apple")
        assertThat(merged.transcription).isEqualTo("[ˈæpl]")
        assertThat(merged.translations).containsExactly("1) яблоко", "2) яблоня")
    }


    @Test
    fun mergeDictionaryEntries_withAllNullTranscription() {

        val merged = mergeDictionaryEntries("apple", listOf(
            DictionaryEntry("apple", null, listOf("1) яблоко", "2) яблоня")),
            DictionaryEntry("apple", null, listOf("1) яблоко", "2) яблоня")),
        ))

        assertThat(merged).isNotNull
        assertThat(merged.word).isEqualTo("apple")
        assertThat(merged.transcription).isNull()
        assertThat(merged.translations).containsExactly("1) яблоко", "2) яблоня")
    }


    @Test
    fun mergeDictionaryEntries_withOneNullTranscription() {

        val merged = mergeDictionaryEntries("apple", listOf(
            DictionaryEntry("apple", null, listOf("1) яблоко", "2) яблоня")),
            DictionaryEntry("apple", "[ˈæpl]", listOf("1) яблоко", "2) яблоня")),
        ))

        assertThat(merged).isNotNull
        assertThat(merged.word).isEqualTo("apple")
        assertThat(merged.transcription).isEqualTo("[ˈæpl]")
        assertThat(merged.translations).containsExactly("1) яблоко", "2) яблоня")
    }


    @Test
    fun mergeDictionaryEntries_withDifferentTranscriptions() {

        val merged = mergeDictionaryEntries("apple", listOf(
            DictionaryEntry("apple", "[ˈæpl 1]", listOf("1) яблоко", "2) яблоня")),
            DictionaryEntry("apple", "[ˈæpl 2]", listOf("1) яблоко", "2) яблоня")),
        ))

        assertThat(merged).isNotNull
        assertThat(merged.word).isEqualTo("apple")
        assertThat(merged.transcription).isEqualTo("[ˈæpl 1][ˈæpl 2]")
        assertThat(merged.translations).containsExactly("1) яблоко", "2) яблоня")
    }


    @Test
    fun mergeDictionaryEntries_withTranslationsInDifferentOrder() {

        val merged = mergeDictionaryEntries("apple", listOf(
            DictionaryEntry("apple", "[ˈæpl]", listOf("1) яблоко", "2) яблоня")),
            DictionaryEntry("apple", "[ˈæpl]", listOf("1) яблоня", "2) яблоко")),
        ))

        assertThat(merged).isNotNull
        assertThat(merged.word).isEqualTo("apple")
        assertThat(merged.transcription).isEqualTo("[ˈæpl]")
        assertThat(merged.translations).containsExactly("1) яблоко", "2) яблоня")
    }


    @Test
    fun mergeDictionaryEntries_withDuplicatedTranslationsWithoutLeadingNumber() {

        val merged = mergeDictionaryEntries("apple", listOf(
            DictionaryEntry("apple", "[ˈæpl]", listOf("1) яблоко", "2) яблоня")),
            DictionaryEntry("apple", "[ˈæpl]", listOf("яблоко", "яблоня")),
        ))

        assertThat(merged).isNotNull
        assertThat(merged.word).isEqualTo("apple")
        assertThat(merged.transcription).isEqualTo("[ˈæpl]")
        assertThat(merged.translations).containsExactly("1) яблоко", "2) яблоня")
    }


    @Test
    fun mergeDictionaryEntries_withDifferentTranslations() {

        val merged = mergeDictionaryEntries("apple", listOf(
            DictionaryEntry("apple", "[ˈæpl]", listOf("1) яблоко", "2) яблоня")),
            DictionaryEntry("apple", null, listOf("яблоняяяяяяя")),
        ))

        assertThat(merged).isNotNull
        assertThat(merged.word).isEqualTo("apple")
        assertThat(merged.transcription).isEqualTo("[ˈæpl]")
        assertThat(merged.translations).containsExactly("1) яблоко", "2) яблоня", "яблоняяяяяяя")
    }

}
