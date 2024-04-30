package com.mvv.win

import com.mvv.gui.test.useAssertJSoftAssertions
import com.mvv.gui.util.toUHex
import com.mvv.win.winapi.keyboardex.toHKLEntries
import com.mvv.win.winapi.keyboardex.toHKLLanguage
import com.mvv.win.winapi.keyboardex.toLanguage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS


class WinKeyboardKtTest {

    @Test
    fun toHklEntries() { useAssertJSoftAssertions {

        // English
        val enGb = 0x8090809L
        assertThat(enGb.toHKLEntries().languageID).isEqualTo(0x0809)
        assertThat(enGb.toHKLEntries().primaryLanguageID).isEqualTo(9)
        assertThat(enGb.toHKLEntries().subLanguageID).isEqualTo(2)
        assertThat(enGb.toHKLEntries().sortID).isEqualTo(9)

        val enUs = 0x4090409L
        assertThat(enUs.toHKLEntries().languageID).isEqualTo(0x0409)
        assertThat(enUs.toHKLEntries().primaryLanguageID).isEqualTo(9)
        assertThat(enUs.toHKLEntries().subLanguageID).isEqualTo(1)
        assertThat(enUs.toHKLEntries().sortID).isEqualTo(9)

        val enCa = 0x4091009L
        assertThat(enCa.toHKLEntries().languageID).isEqualTo(0x1009)
        assertThat(enCa.toHKLEntries().primaryLanguageID).isEqualTo(9)
        assertThat(enCa.toHKLEntries().subLanguageID).isEqualTo(4)
        assertThat(enCa.toHKLEntries().sortID).isEqualTo(9)

        val enAu = 0x4090c09L
        assertThat(enAu.toHKLEntries().languageID).isEqualTo(0x0c09)
        assertThat(enAu.toHKLEntries().primaryLanguageID).isEqualTo(9)
        assertThat(enAu.toHKLEntries().subLanguageID).isEqualTo(3)
        assertThat(enAu.toHKLEntries().sortID).isEqualTo(9)

        //
        val ru = 0x4190419L
        assertThat(ru.toHKLEntries().languageID).isEqualTo(0x0419)
        assertThat(ru.toHKLEntries().primaryLanguageID).isEqualTo(25)
        assertThat(ru.toHKLEntries().subLanguageID).isEqualTo(1)
        assertThat(ru.toHKLEntries().sortID).isEqualTo(9)

        val ruBel = 0x4192000L
        assertThat(ruBel.toHKLEntries().languageID).isEqualTo(0x2000)
        assertThat(ruBel.toHKLEntries().primaryLanguageID).isEqualTo(0) // hm...
        assertThat(ruBel.toHKLEntries().subLanguageID).isEqualTo(8)
        assertThat(ruBel.toHKLEntries().sortID).isEqualTo(9)

        val ruUa = 0x4192400L
        assertThat(ruUa.toHKLEntries().languageID).isEqualTo(0x2400)
        assertThat(ruUa.toHKLEntries().primaryLanguageID).isEqualTo(0) // hm..
        assertThat(ruUa.toHKLEntries().subLanguageID).isEqualTo(9)
        assertThat(ruUa.toHKLEntries().sortID).isEqualTo(9)

        // ukrainian
        val ukr = -0xf57fbdeL
        //val ukr = 0xfffffffff0a80422L
        assertThat(ukr.toHKLEntries().languageID).isEqualTo(0x0422)
        assertThat(ukr.toHKLEntries().primaryLanguageID).isEqualTo(34)
        assertThat(ukr.toHKLEntries().subLanguageID).isEqualTo(1)
        assertThat(ukr.toHKLEntries().sortID).isEqualTo(8)

        // the same
        val ukrTheSame = 0x7ffffffff0a80422L
        assertThat(ukrTheSame.toHKLEntries().languageID).isEqualTo(0x0422)
        assertThat(ukrTheSame.toHKLEntries().primaryLanguageID).isEqualTo(34)
        assertThat(ukrTheSame.toHKLEntries().subLanguageID).isEqualTo(1)
        assertThat(ukrTheSame.toHKLEntries().sortID).isEqualTo(8)
    } }

    @Test
    fun toHklLanguage() { useAssertJSoftAssertions {
        val enGb = 0x8090809L
        val hklEntries = enGb.toHKLEntries()

        assertThat(hklEntries.languageID).isEqualTo(0x0809)
        assertThat(hklEntries.primaryLanguageID).isEqualTo(9)
        assertThat(hklEntries.subLanguageID).isEqualTo(2)

        assertThat(hklEntries.toLanguage().toUHex()).isEqualTo("0809")
        assertThat(hklEntries.toHKLLanguage().toUHex()).isEqualTo("0000000000000809")
    } }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    fun getKeyboardLocaleAsInputSource() { useAssertJSoftAssertions {

        assertThat(getKeyboardLocaleAsInputSource(0x4090409))
            .isEqualTo(
                WindowsInputSource(
                    id = 67699721,
                    displayName = "English (United States)",
                    //shortName = "ENU",
                    languageCode = "en",
                    languageName = "English",
                    countryCode = "US",
                    countryName = "United States",
                )
            )

        assertThat(getKeyboardLocaleAsInputSource(0x8090809))
            .isEqualTo(
                WindowsInputSource(
                    id = 134809609,
                    displayName = "English (United Kingdom)",
                    //shortName = "ENG",
                    languageCode = "en",
                    languageName = "English",
                    countryCode = "GB",
                    countryName = "United Kingdom",
                )
            )

        assertThat(getKeyboardLocaleAsInputSource(0x4090c09))
            .isEqualTo(
                WindowsInputSource(
                    id = 67701769,
                    displayName = "English (Australia)",
                    //shortName = "ENA",
                    languageCode = "en",
                    languageName = "English",
                    countryCode = "AU",
                    countryName = "Australia",
                )
            )

        assertThat(getKeyboardLocaleAsInputSource(0x4091009))
            .isEqualTo(
                WindowsInputSource(
                    id = 67702793,
                    displayName = "English (Canada)",
                    //shortName = "ENC",
                    languageCode = "en",
                    languageName = "English",
                    countryCode = "CA",
                    countryName = "Canada",
                )
            )

        assertThat(getKeyboardLocaleAsInputSource(0x4190419))
            .isEqualTo(
                WindowsInputSource(
                    id = 68748313,
                    displayName = "Russian (Russia)",
                    //shortName = "RUS",
                    languageCode = "ru",
                    languageName = "Russian",
                    countryCode = "RU",
                    countryName = "Russia",
                )
            )

        assertThat(getKeyboardLocaleAsInputSource(0x4192000))
            .isEqualTo(
                WindowsInputSource(
                    id = 68755456,
                    displayName = "Russian (Belarus)",
                    //shortName = "ZZZ", // ??? Is it ok
                    languageCode = "ru",
                    languageName = "Russian",
                    countryCode = "BY",
                    countryName = "Belarus"
                )
            )

        assertThat(getKeyboardLocaleAsInputSource(0x4192400))
            .isEqualTo(
                WindowsInputSource(
                    id = 68756480,
                    displayName = "Russian (Ukraine)",
                    //shortName = "ZZZ", // ??? Is it ok
                    languageCode = "ru",
                    languageName = "Russian",
                    countryCode = "UA",
                    countryName = "Ukraine",
                )
            )

        assertThat(getKeyboardLocaleAsInputSource(-0xf57fbde))
            .isEqualTo(
                WindowsInputSource(
                    id = -257424350,
                    displayName = "Ukrainian (Ukraine)",
                    //shortName = "UKR",
                    languageCode = "uk",
                    languageName = "Ukrainian",
                    countryCode = "UA",
                    countryName = "Ukraine",
                )
            )

        assertThat(getKeyboardLocaleAsInputSource(-0xfdff3f4))
            .isEqualTo(
                WindowsInputSource(
                    id = -266335220,
                    displayName = "French (Canada)",
                    //shortName = "FRC",
                    languageCode = "fr",
                    languageName = "French",
                    countryCode = "CA",
                    countryName = "Canada",
                )
            )

        assertThat(getKeyboardLocaleAsInputSource(0x4230423))
            .isEqualTo(
                WindowsInputSource(
                    id = 69403683,
                    displayName = "Belarusian (Belarus)",
                    //shortName = "BEL",
                    languageCode = "be",
                    languageName = "Belarusian",
                    countryCode = "BY",
                    countryName = "Belarus",
                )
            )
    } }
}
