package com.mvv.gui.javafx

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.mvv.gui.test.useAssertJSoftAssertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.util.*
import kotlin.collections.LinkedHashMap


class KeyboardTest {

    @Test
    fun deserializeInputSource() { useAssertJSoftAssertions {

        val s = jsonMapper.readValue<InputSource>("""
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

        val s2 = jsonMapper.readValue<InputSource>(
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

        val matchByLanguage = findGnomeInputSource(inputSources, Locale.ENGLISH)
        assertThat(matchByLanguage).isNotNull
        if (matchByLanguage != null) {
            assertThat(matchByLanguage.key).isEqualTo("0")
            assertThat(matchByLanguage.value).isSameAs(inputSources["0"])
            assertThat(matchByLanguage.value.displayName).isEqualTo("English (US)")
        }

        val matchByLanguage2 = findGnomeInputSource(inputSources, Locale("en"))
        assertThat(matchByLanguage2).isNotNull
        if (matchByLanguage2 != null) {
            assertThat(matchByLanguage2.key).isEqualTo("0")
            assertThat(matchByLanguage2.value).isSameAs(inputSources["0"])
            assertThat(matchByLanguage2.value.displayName).isEqualTo("English (US)")
        }

        val matchByLanguageAndCountryUS = findGnomeInputSource(inputSources, Locale.US)
        assertThat(matchByLanguageAndCountryUS).isNotNull
        if (matchByLanguageAndCountryUS != null) {
            assertThat(matchByLanguageAndCountryUS.key).isEqualTo("0")
            assertThat(matchByLanguageAndCountryUS.value).isSameAs(inputSources["0"])
        }

        val matchByLanguageAndCountryUK = findGnomeInputSource(inputSources, Locale.UK)
        assertThat(matchByLanguageAndCountryUK).isNotNull
        if (matchByLanguageAndCountryUK != null) {
            assertThat(matchByLanguageAndCountryUK.key).isEqualTo("0")
            assertThat(matchByLanguageAndCountryUK.value).isSameAs(inputSources["0"])
        }

        val ru = findGnomeInputSource(inputSources, Locale("ru"))
        assertThat(ru).isNotNull
        if (ru != null) {
            assertThat(ru.key).isEqualTo("2")
            assertThat(ru.value).isSameAs(inputSources["2"])
            assertThat(ru.value.id).isEqualTo("ru")
            assertThat(ru.value.displayName).isEqualTo("Russian")
        }

        val ruRu = findGnomeInputSource(inputSources, Locale("ru", "RU"))
        assertThat(ruRu).isNotNull
        if (ruRu != null) {
            assertThat(ruRu.key).isEqualTo("2")
            assertThat(ruRu.value).isSameAs(inputSources["2"])
            assertThat(ruRu.value.id).isEqualTo("ru")
            assertThat(ruRu.value.displayName).isEqualTo("Russian")
        }

        val ruUa = findGnomeInputSource(inputSources, Locale("ru" ,"UA"))
        assertThat(ruUa).isNotNull
        if (ruUa != null) {
            assertThat(ruUa.key).isEqualTo("2")
            assertThat(ruUa.value).isSameAs(inputSources["2"])
            assertThat(ruUa.value.id).isEqualTo("ru")
            assertThat(ruUa.value.displayName).isEqualTo("Russian")
        }

        val ua = findGnomeInputSource(inputSources, Locale("uk"))
        //val ua = findInputSource(inputSources, Locale("uk", "UA"))
        assertThat(ua).isNotNull
        if (ua != null) {
            assertThat(ua.key).isEqualTo("1")
            assertThat(ua.value).isSameAs(inputSources["1"])
            assertThat(ua.value.id).isEqualTo("ua")
            assertThat(ua.value.displayName).isEqualTo("Ukrainian")
        }

        val spanish = findGnomeInputSource(inputSources, Locale("es"))
        assertThat(spanish).isNull()

        val spanishEs = findGnomeInputSource(inputSources, Locale("es", "ES"))
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
                  "_shortName":"en1",
                  "index":0,
                  "properties":null,
                  "xkbId":"us"
                  }
            } """)

        val matchByLanguage = findGnomeInputSource(inputSources, Locale("en1"))
        assertThat(matchByLanguage).isNotNull
        if (matchByLanguage != null) {
            assertThat(matchByLanguage.key).isEqualTo("0")
            assertThat(matchByLanguage.value).isSameAs(inputSources["0"])
            assertThat(matchByLanguage.value.shortName).isEqualTo("en1")
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

        val matchByLanguage = findGnomeInputSource(inputSources, locale)
        assertThat(matchByLanguage).isNotNull
        if (matchByLanguage != null) {
            assertThat(matchByLanguage.key).isEqualTo("0")
            assertThat(matchByLanguage.value).isSameAs(inputSources["0"])
            assertThat(matchByLanguage.value.displayName).isEqualTo("English1 (bla-bla)")
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

        val us = findGnomeInputSource(inputSources, Locale.US)
        assertThat(us).isNotNull
        if (us != null) {
            assertThat(us.key).isEqualTo("0")
            assertThat(us.value).isSameAs(inputSources["0"])
            assertThat(us.value.displayName).isEqualTo("English (US)")
        }

        val uk = findGnomeInputSource(inputSources, Locale.UK)
        assertThat(uk).isNotNull
        if (uk != null) {
            assertThat(uk.key).isEqualTo("1")
            assertThat(uk.value).isSameAs(inputSources["1"])
            assertThat(uk.value.displayName).isEqualTo("English (GB)")
        }
    } }

}


private val jsonMapper = jsonMapper {
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
}.registerKotlinModule()

private fun inputSources(inputSources: String): Map<String, InputSource> =
    jsonMapper.readValue<LinkedHashMap<String, InputSource>>(inputSources)


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
*/
