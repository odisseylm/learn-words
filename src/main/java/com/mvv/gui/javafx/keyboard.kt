package com.mvv.gui.javafx

import com.mvv.gnome.selectKeyboardLayout as selectGnomeKeyboardLayout
import com.mvv.win.selectKeyboard as selectWindowsKeyboard
import com.mvv.gui.util.containsOneOf
import com.mvv.gui.util.filterNotBlank
import com.mvv.gui.util.measureTime
import org.apache.commons.lang3.SystemUtils.IS_OS_LINUX
import org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS
import java.util.Locale


private val log = mu.KotlinLogging.logger {}


// See
//  https://askubuntu.com/questions/1039950/ubuntu-18-04-how-to-change-keyboard-layout-from-a-script
//  https://unix.stackexchange.com/questions/316998/how-to-change-keyboard-layout-in-gnome-3-from-command-line
//  https://discourse.gnome.org/t/how-to-set-gnome-keyboard-layout-programatically/9459

//  !!! Dangerous! Kills icon of language in system bar !!!
//  !!! gsettings set org.gnome.desktop.input-sources sources "[('xkb', 'ru')]"

// Other useful command:
//   `gsettings get org.gnome.desktop.input-sources sources`
//
// Output:
//   [('xkb', 'us'), ('xkb', 'ua'), ('xkb', 'ru')]
//
// If works properly, "org.gnome.desktop.input-sources.mru-sources" (most-recently-used sources)
// looks like what you want, and can be monitored by (like listening event)
//  `gsettings monitor org.gnome.desktop.input-sources mru-sources`


fun selectKeyboardLayout(locale: Locale) { measureTime("select keyboard for $locale", log) {
    when {
        IS_OS_LINUX   -> selectGnomeKeyboardLayout(locale)
        IS_OS_WINDOWS -> selectWindowsKeyboard(locale)
    }
} }



interface InputSource {
    val id: Any              // String in Gnome; HKL/DWORD in Windows
    val displayName: String  // "English (US)"
    //val shortName: String  // "en" or "Eng" // not portable, maybe absent in Windows

    val languageCode: String // "en"
    val languageName: String // "English" (preferably should be in English, if it is possible)

    val countryCode: String // preferably "us" (? but may be "usa" ?)
    val countryName: String // preferably "USA", "Ukraine"
}


// Java locales:
//  https://www.oracle.com/java/technologies/javase/java8locales.html
//
internal fun <T: InputSource> Iterable<T>.findInputSource(locale: Locale): T? {

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


// Returns lowercase values.
private fun InputSource.languageIds(): List<String> = listOf(
        languageCode,
        languageName,
    )
    .filterNotBlank()
    .map { it.lowercase() }


// Returns lowercase values.
private fun InputSource.countryIds(): List<String> = listOf(
        countryCode,
        countryName,
    )
    .filterNotBlank()
    .map { it.lowercase() }
