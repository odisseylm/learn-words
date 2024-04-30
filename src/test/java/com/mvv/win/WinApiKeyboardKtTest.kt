package com.mvv.win

import com.mvv.gui.test.useAssertJSoftAssertions
import com.mvv.win.winapi.keyboard.GetKeyboardLayout
import com.mvv.win.winapi.keyboard.GetKeyboardLayoutList
import com.mvv.win.winapi.keyboard.GetKeyboardLayoutName
import com.mvv.win.winapi.keyboard.KL_NAMELENGTH
import com.mvv.win.winapi.keyboardex.ActivateKeyboardLayoutAlt
import com.mvv.win.winapi.keyboardex.toHKLEntries
import com.mvv.win.winapi.locale.*
import com.mvv.win.winapi.toDWORD
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS


@EnabledOnOs(OS.WINDOWS)
class WinApiKeyboardKtTest {

    @Test
    fun getKeyboardLayoutName() { useAssertJSoftAssertions {
        assertThat(GetKeyboardLayoutName())
            .isNotEmpty
            .hasSizeGreaterThanOrEqualTo(KL_NAMELENGTH - 1)
            //.isEqualTo("00000409") // for US English
            //.isEqualTo("00000809") // for UK English
    } }

    @Test
    fun getKeyboardLayoutList() { useAssertJSoftAssertions {
        val keyboardLayouts = GetKeyboardLayoutList()

        assertThat(keyboardLayouts)
            //.hasSizeGreaterThan(0)
            .isNotEmpty
            // If you developer you should have at least some English keyboard layout
            .containsAnyOf(
                0x4090409, // English (United States)
                0x8090809, // English (United Kingdom)
                0x4090c09, // English (Australia)
                0x4091009, // English (Canada)
            )

        keyboardLayouts.map { it.toHKLEntries().primaryLanguageID }.contains(9) // English
    } }

    @Test
    fun getKeyboardLayout() { useAssertJSoftAssertions {
        assertThat(GetKeyboardLayout(0))
            .isNotEqualTo(0)
            .isNotEqualTo(1)
            .isNotEqualTo(-1)
    } }

    @Test
    fun getLocaleInfo() { useAssertJSoftAssertions {

        // hm... Is it ok? It does not work for full HKL keyboard ID.
        //assertThat(GetLocaleInfo(0x8090809, LOCALE_SENGLISHDISPLAYNAME)).isEqualTo("English (United Kingdom)")
        // ...

        val languageId = 0x8090809.toHKLEntries().languageID.toDWORD()
        assertThat(languageId).isEqualTo(0x0809     )
        assertThat(GetLocaleInfo(languageId, LOCALE_SENGLISHDISPLAYNAME)).isEqualTo("English (United Kingdom)")
        assertThat(GetLocaleInfo(languageId, LOCALE_SENGLISHLANGUAGENAME)).isEqualTo("English")
        assertThat(GetLocaleInfo(languageId, LOCALE_SENGLISHCOUNTRYNAME)).isEqualTo("United Kingdom")
        assertThat(GetLocaleInfo(languageId, LOCALE_SABBREVLANGNAME)).isEqualTo("ENG")
        assertThat(GetLocaleInfo(languageId, LOCALE_SABBREVCTRYNAME)).isEqualTo("GBR")
        assertThat(GetLocaleInfo(languageId, LOCALE_ILANGUAGE)).isEqualTo("0809")

        assertThat(GetLocaleInfo(languageId, LOCALE_SNAME)).isEqualTo("en-GB")
        assertThat(GetLocaleInfo(languageId, LOCALE_SISO639LANGNAME)).isEqualTo("en")
        assertThat(GetLocaleInfo(languageId, LOCALE_SISO3166CTRYNAME)).isEqualTo("GB")
    } }

    @Test
    fun getLocaleInfoEx() { useAssertJSoftAssertions {
        assertThat(GetLocaleInfoEx("en-GB", LOCALE_SENGLISHDISPLAYNAME)).isEqualTo("English (United Kingdom)")
        assertThat(GetLocaleInfoEx("en-GB", LOCALE_SENGLISHLANGUAGENAME)).isEqualTo("English")
        assertThat(GetLocaleInfoEx("en-GB", LOCALE_SENGLISHCOUNTRYNAME)).isEqualTo("United Kingdom")
        assertThat(GetLocaleInfoEx("en-GB", LOCALE_SABBREVLANGNAME)).isEqualTo("ENG")
        assertThat(GetLocaleInfoEx("en-GB", LOCALE_SABBREVCTRYNAME)).isEqualTo("GBR")
        assertThat(GetLocaleInfoEx("en-GB", LOCALE_ILANGUAGE)).isEqualTo("0809")

        assertThat(GetLocaleInfoEx("en-GB", LOCALE_SNAME)).isEqualTo("en-GB")
        assertThat(GetLocaleInfoEx("en-GB", LOCALE_SISO639LANGNAME)).isEqualTo("en")
        assertThat(GetLocaleInfoEx("en-GB", LOCALE_SISO3166CTRYNAME)).isEqualTo("GB")
    } }

    @Test
    @Disabled("It is bad idea to change language during tests")
    fun activateKeyboardLayout() {
        assertDoesNotThrow { ActivateKeyboardLayout(0x4090409) }
    }

    @Test
    @Disabled("It is bad idea to change language during tests")
    fun activateKeyboardLayoutAlt() {
        assertDoesNotThrow { ActivateKeyboardLayoutAlt(0x4090409) }
    }
}
