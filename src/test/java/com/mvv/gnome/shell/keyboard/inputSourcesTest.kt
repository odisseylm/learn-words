package com.mvv.gnome.shell.keyboard

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.mvv.gui.javafx.findInputSource
import com.mvv.gui.test.useAssertJSoftAssertions
import com.mvv.gui.util.locale
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS
import org.mockito.kotlin.mock
import java.util.*
import kotlin.collections.LinkedHashMap



class GnomeShellInputSourcesTest {

    @Test
    @EnabledOnOs(OS.LINUX)
    fun `is ShellEval available`() { useAssertJSoftAssertions {
        assertThat(isShellEvalAvailableImpl()).isTrue
    } }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    fun `is ShellEval unavailable on Windows`() { useAssertJSoftAssertions {
        assertThat(isShellEvalAvailableImpl()).isFalse
    } }

    @Test
    fun deserializeInputSource() { useAssertJSoftAssertions {

        val s = jsonMapper.readValue<ShellInputSource>("""
            {
                "type":"type1",
                "id":"id1",
                "displayName":"displayName1",
                "_shortName":"shortName1",
                "index":555,
                "properties":null,
                "xkbId":"xkbId1",
                "_signalConnections":null,
                "_nextConnectionId":3
            } """)

        assertThat(s.type).isEqualTo("type1")
        assertThat(s.id).isEqualTo("id1")
        assertThat(s.displayName).isEqualTo("displayName1")
        assertThat(s.shortName).isEqualTo("shortName1")
        assertThat(s.index).isEqualTo(555)
        assertThat(s.xkbId).isEqualTo("xkbId1")

        val s2 = jsonMapper.readValue<ShellInputSource>(
            """ {
                  "type":"xkb",
                  "id":"us",
                  "displayName":"English (US)",
                  "_shortName":"en",
                  "index":0,
                  "properties":null,
                  "xkbId":"us",
                  "_signalConnections":[{"id":1,"name":"activate","disconnected":false},{"id":2,"name":"changed","disconnected":false}],
                  "_nextConnectionId":3}
            } """)

        assertThat(s2.type).isEqualTo("xkb")
        assertThat(s2.id).isEqualTo("us")
        assertThat(s2.displayName).isEqualTo("English (US)")
        assertThat(s2.shortName).isEqualTo("en")
        assertThat(s2.index).isEqualTo(0)
        assertThat(s2.xkbId).isEqualTo("us")
    } }


    @Test
    fun `findInputSource  general real cases`() { useAssertJSoftAssertions {

        val inputSources = inputSources(""" {
             "0":{
                  "type":"xkb",
                  "id":"us",
                  "displayName":"English (US)",
                  "_shortName":"en",
                  "index":0,
                  "properties":null,
                  "xkbId":"us",
                  "_signalConnections":[{"id":1,"name":"activate","disconnected":false},{"id":2,"name":"changed","disconnected":false}],
                  "_nextConnectionId":3
                 },
             "1":{
                  "type":"xkb",
                  "id":"ua",
                  "displayName":"Ukrainian",
                  "_shortName":"uk",
                  "index":1,
                  "properties":null,
                  "xkbId":"ua",
                  "_signalConnections":[{"id":1,"name":"activate","disconnected":false},{"id":2,"name":"changed","disconnected":false}],
                  "_nextConnectionId":3
                 },
             "2":{
                  "type":"xkb",
                  "id":"ru",
                  "displayName":"Russian",
                  "_shortName":"ru",
                  "index":2,
                  "properties":null,
                  "xkbId":"ru",
                  "_signalConnections":[{"id":1,"name":"activate","disconnected":false},{"id":2,"name":"changed","disconnected":false}],
                  "_nextConnectionId":3
                 }
            } """)

        val matchByLanguage = inputSources.findInputSource(Locale.ENGLISH)
        assertThat(matchByLanguage).isNotNull
        if (matchByLanguage != null) {
            assertThat(matchByLanguage.displayName).isEqualTo("English (US)")
        }

        val matchByLanguage2 = inputSources.findInputSource(locale("en"))
        assertThat(matchByLanguage2).isNotNull
        if (matchByLanguage2 != null) {
            assertThat(matchByLanguage2.displayName).isEqualTo("English (US)")
        }

        val matchByLanguageAndCountryUS = inputSources.findInputSource(Locale.US)
        assertThat(matchByLanguageAndCountryUS).isNotNull
        if (matchByLanguageAndCountryUS != null) {
            assertThat(matchByLanguageAndCountryUS.id).isEqualTo("us")
        }

        val matchByLanguageAndCountryUK = inputSources.findInputSource(Locale.UK)
        assertThat(matchByLanguageAndCountryUK).isNotNull
        if (matchByLanguageAndCountryUK != null) {
            assertThat(matchByLanguageAndCountryUK.id).isEqualTo("us")
        }

        val ru = inputSources.findInputSource(locale("ru"))
        assertThat(ru).isNotNull
        if (ru != null) {
            assertThat(ru.id).isEqualTo("ru")
            assertThat(ru.displayName).isEqualTo("Russian")
        }

        val ruRu = inputSources.findInputSource(locale("ru", "RU"))
        assertThat(ruRu).isNotNull
        if (ruRu != null) {
            assertThat(ruRu.id).isEqualTo("ru")
            assertThat(ruRu.displayName).isEqualTo("Russian")
        }

        val ruUa = inputSources.findInputSource(locale("ru" ,"UA"))
        assertThat(ruUa).isNotNull
        if (ruUa != null) {
            assertThat(ruUa.id).isEqualTo("ru")
            assertThat(ruUa.displayName).isEqualTo("Russian")
        }

        val ua = inputSources.findInputSource(locale("uk"))
        //val ua = inputSources.findInputSource(Locale("uk", "UA"))
        assertThat(ua).isNotNull
        if (ua != null) {
            assertThat(ua.id).isEqualTo("ua")
            assertThat(ua.displayName).isEqualTo("Ukrainian")
        }

        val spanish = inputSources.findInputSource(locale("es"))
        assertThat(spanish).isNull()

        val spanishEs = inputSources.findInputSource(locale("es", "ES"))
        assertThat(spanishEs).isNull()
    } }


    @Test
    fun `findInputSource by shortName`() { useAssertJSoftAssertions {

        val inputSources = inputSources(""" {
             "1":{
                  "type":"xkb",
                  "id":"ua",
                  "displayName":"Ukrainian",
                  "_shortName":"uk",
                  "index":1,
                  "properties":null,
                  "xkbId":"ua"
                  },
             "0":{
                  "type":"xkb",
                  "id":"us",
                  "displayName":"English (US)",
                  "_shortName":"bla",
                  "index":0,
                  "properties":null,
                  "xkbId":"us"
                  }
            } """)

        val matchByLanguage = inputSources.findInputSource(locale("bla"))
        assertThat(matchByLanguage).isNotNull
        if (matchByLanguage != null) {
            assertThat(matchByLanguage.shortName).isEqualTo("bla")
        }
    } }


    @Test
    fun `findInputSource by displayName`() { useAssertJSoftAssertions {
        val inputSources = inputSources(""" {
             "0":{
                  "type":"xkb",
                  "id":"us",
                  "displayName":"English1 (bla-bla)",
                  "_shortName":"en",
                  "index":0,
                  "properties":null,
                  "xkbId":"us"
                  }
            } """)

        val locale = mockLocale(displayLanguage = "English1")

        val matchByLanguage = inputSources.findInputSource(locale)
        assertThat(matchByLanguage).isNotNull
        if (matchByLanguage != null) {
            assertThat(matchByLanguage.displayName).isEqualTo("English1 (bla-bla)")
        }
    } }


    @Test
    fun `findInputSource by country`() { useAssertJSoftAssertions {
        val inputSources = inputSources(""" {
             "0":{
                  "type":"xkb",
                  "id":"us",
                  "displayName":"English (US)",
                  "_shortName":"en",
                  "index":0,
                  "properties":null,
                  "xkbId":"us"
                 },
             "1":{
                  "type":"xkb",
                  "id":"gb",
                  "displayName":"English (GB)",
                  "_shortName":"en",
                  "index":1,
                  "properties":null,
                  "xkbId":"gb"
                  }
             }
             """)

        val us = inputSources.findInputSource(Locale.US)
        assertThat(us).isNotNull
        if (us != null) {
            assertThat(us.displayName).isEqualTo("English (US)")
        }

        val uk = inputSources.findInputSource(Locale.UK)
        assertThat(uk).isNotNull
        if (uk != null) {
            assertThat(uk.displayName).isEqualTo("English (GB)")
        }
    } }

}


private val jsonMapper = jsonMapper {
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
}.registerKotlinModule()

private fun inputSources(inputSources: String): Collection<ShellInputSource> =
    jsonMapper.readValue<LinkedHashMap<String, ShellInputSource>>(inputSources).values


private fun mockLocale(
    language: String = "",
    isO3Language: String = "",
    displayLanguage: String = "",
    englishDisplayLanguage: String = "",
    languageTag: String = "",
    country: String = "",
    isO3Country: String = "",
    displayCountry: String = "",
    englishDisplayCountry: String = "",
    ): Locale = mock<Locale>() {
        on { this.language }.thenReturn(language)
        on { this.isO3Language }.thenReturn(isO3Language)
        on { this.displayLanguage }.thenReturn(displayLanguage)
        on { this.getDisplayLanguage(Locale.ENGLISH) }.thenReturn(englishDisplayLanguage)
        on { this.toLanguageTag() }.thenReturn(languageTag)
        on { this.country }.thenReturn(country)
        on { this.isO3Country }.thenReturn(isO3Country)
        on { this.displayCountry }.thenReturn(displayCountry)
        on { this.getDisplayCountry(Locale.ENGLISH) }.thenReturn(englishDisplayCountry)
    }

/*
private fun mockLocale2(
    language: String = "",
    isO3Language: String = "",
    displayLanguage: String = "",
    englishDisplayLanguage: String = "",
    languageTag: String = "",
    country: String = "",
    isO3Country: String = "",
    displayCountry: String = "",
    englishDisplayCountry: String = "",
    ): Locale {

    val locale = mock<Locale>()
    whenever(locale.language).thenReturn(language)
    whenever(locale.isO3Language).thenReturn(isO3Language)
    whenever(locale.displayLanguage).thenReturn(displayLanguage)
    whenever(locale.getDisplayLanguage(Locale.ENGLISH)).thenReturn(englishDisplayLanguage)
    whenever(locale.toLanguageTag()).thenReturn(languageTag)
    whenever(locale.country).thenReturn(country)
    whenever(locale.isO3Country).thenReturn(isO3Country)
    whenever(locale.displayCountry).thenReturn(displayCountry)
    whenever(locale.getDisplayCountry(Locale.ENGLISH)).thenReturn(englishDisplayCountry)

    return locale
}


private fun mockLocale3(
    language: String = "",
    isO3Language: String = "",
    displayLanguage: String = "",
    englishDisplayLanguage: String = "",
    languageTag: String = "",
    country: String = "",
    isO3Country: String = "",
    displayCountry: String = "",
    englishDisplayCountry: String = "",
): Locale {

    val locale = Mockito.mock(Locale::class.java)
    `when`(locale.language).thenReturn(language)
    `when`(locale.isO3Language).thenReturn(isO3Language)
    `when`(locale.displayLanguage).thenReturn(displayLanguage)
    `when`(locale.getDisplayLanguage(Locale.ENGLISH)).thenReturn(englishDisplayLanguage)
    `when`(locale.toLanguageTag()).thenReturn(languageTag)
    `when`(locale.country).thenReturn(country)
    `when`(locale.isO3Country).thenReturn(isO3Country)
    `when`(locale.displayCountry).thenReturn(displayCountry)
    `when`(locale.getDisplayCountry(Locale.ENGLISH)).thenReturn(englishDisplayCountry)

    return locale
}
*/
