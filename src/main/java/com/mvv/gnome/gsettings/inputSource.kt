package com.mvv.gnome.gsettings

import com.mvv.gui.util.containsOneOf
import com.mvv.gui.util.filterNotBlank
import java.util.*


interface InputSource {
    val type: String        // "xkb"
    val id: String          // "us"
    val displayName: String // "English (US)"
    val shortName: String   // "en"
}


// Returns lowercase values.
fun InputSource.languageIds(): List<String> = listOf(
        displayName.substringBefore("(").trim(),
        shortName,
    )
    .filterNotBlank()
    .map { it.lowercase() }


// Returns lowercase values.
fun InputSource.countryIds(): List<String> = listOf(
        displayName.substringAfter("(", "").removeSuffix(")").trim(),
        id,
        //xkbId,
    )
    .filterNotBlank()
    .map { it.lowercase() }



// Java locales:
//  https://www.oracle.com/java/technologies/javase/java8locales.html
//
internal fun <T: InputSource> Iterable<T>.findInputSource(/*inputSources: Map<String, T>,*/ locale: Locale): T? {

    val inputSources = this.toList()

    val localeLanguageIds = listOf(
            locale.language,
            locale.isO3Language,
            locale.displayLanguage,
            locale.getDisplayLanguage(Locale.ENGLISH),
            locale.toLanguageTag(),
        )
        .map { it.lowercase() }
        .filterNotBlank()
        .distinct()

    val localeCountryIds = listOf(
            locale.country,
            locale.isO3Country,
            locale.displayCountry,
            locale.getDisplayCountry(Locale.ENGLISH),
        )
        .map { it.lowercase() }
        .filterNotBlank()
        .distinct()

    val fullMatch = inputSources.find { s ->
        val sourceLanguageIds = s.languageIds()
        val sourceCountryIds  = s.countryIds()
        sourceLanguageIds.containsOneOf(localeLanguageIds) && sourceCountryIds.containsOneOf(localeCountryIds)
    }

    if (fullMatch != null) return fullMatch

    val byLangMatch = inputSources.find { s ->
        val sourceLanguageIds = s.languageIds()
        sourceLanguageIds.containsOneOf(localeLanguageIds)
    }

    return byLangMatch
}
