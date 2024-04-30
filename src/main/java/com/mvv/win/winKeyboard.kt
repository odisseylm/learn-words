package com.mvv.win


import com.mvv.gui.javafx.InputSource
import com.mvv.gui.javafx.findInputSource
import com.mvv.win.winapi.keyboard.*
import com.mvv.win.winapi.keyboardex.ActivateKeyboardLayoutAlt
import com.mvv.win.winapi.keyboardex.toHKLEntries
import com.mvv.win.winapi.locale.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap


//
// Java foreign
// https://docs.oracle.com/en%2Fjava%2Fjavase%2F22%2Fdocs%2Fapi%2F%2F/java.base/java/lang/foreign/Linker.html
// https://foojay.io/today/writing-c-code-in-java/
// Deprecated (about incubator phase)
//  https://www.baeldung.com/java-project-panama
//  https://javarush.com/groups/posts/4090-java-foreign-linker-api-rabotaem-s-pamjatjhju-bez-pererihvov-i-vihkhodnihkh
//  https://developer.okta.com/blog/2022/04/08/state-of-ffi-java
//
//
// https://learn.microsoft.com/en-us/windows/win32/inputdev/keyboard-input
// https://learn.microsoft.com/en-us/windows/win32/intl/language-names
// https://learn.microsoft.com/en-us/windows/win32/intl/locale-names
// https://learn.microsoft.com/en-us/windows/win32/intl/pseudo-locales
// https://learn.microsoft.com/en-us/windows/win32/intl/locales-and-languages
// https://learn.microsoft.com/en-us/windows/win32/intl/language-identifiers
// https://learn.microsoft.com/en-us/windows/win32/intl/language-identifier-constants-and-strings
//
// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-activatekeyboardlayout
// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-getkeyboardlayout
// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-getkeyboardlayoutnamew
// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-getkeyboardlayoutlist
// https://learn.microsoft.com/en-us/windows/win32/api/winnls/nf-winnls-getlocaleinfoa
// https://learn.microsoft.com/en-us/windows/win32/api/winnt/nf-winnt-makelangid
//
// https://learn.microsoft.com/ru-ru/windows/win32/winprog/windows-data-types
//
//
// https://sheroz.com/pages/blog/programmatically-switching-keyboard-layouts.html
// https://stackoverflow.com/questions/9685560/how-can-change-language-keyboard-layout-in-windows-c

// The next command changes the system keyboard layout to English:
// PostMessage(GetForegroundWindow(), WM_INPUTLANGCHANGEREQUEST, 1, 0x04090409);
//
// to Russian:
// PostMessage(GetForegroundWindow(), WM_INPUTLANGCHANGEREQUEST, 1, 0x04190419);
//
// https://microsoft.github.io/windows-docs-rs/doc/windows/Win32/UI/WindowsAndMessaging/constant.KL_NAMELENGTH.html
//
// See PowerShell  Get-WinUserLanguageList
//
// See HKEY_LOCAL_MACHINE\system\CurrentControlSet\control\keyboard layouts
//     HKLM\SystemCurrentControlSet\Control\Keyboard Layouts
//     https://github.com/dotnet/winforms/issues/4345
//
//  https://superuser.com/questions/1553611/passing-the-output-of-getkeyboardlayout-into-loadkeyboardlayout-doesnt-work
//  https://stackoverflow.com/questions/64995173/how-to-create-a-list-of-keyboards-extract-klid-from-hkl
//
// https://stackoverflow.com/questions/61878528/is-getlocaleinfo-with-locale-sabbrevlangname-deprecated


private val log = mu.KotlinLogging.logger {}


/*

[90409, 90419, 80422, 91009, 92000, c0c, 30423, 92400, 90809, 90c09]
90409    409     HKL( 409 = 9/2, 9)    409  us
90419    419     HKL( 419 = 19/2, 9)   419  Russian
80422    422     HKL( 422 = 22/2, 8)   422  Ukrainian
91009    1009    HKL( 1009 = 9/8, 9)  1009  English Canada
c0c      c0c     HKL( c0c = c/6, 0)    c0c  French Canada
30423    423     HKL( 423 = 23/2, 3)   423  Belarusian
90809    809     HKL( 809 = 9/4, 9)    809  English GB (UK)
90c09    c09     HKL( c09 = 9/6, 9)    c09  English AU
4192000  2000    HKL( 2000 = 0/10, 9) 2000  ??? Russian (Ukraine) or Russian (Belarus) ???
4192400  2400    HKL( 2400 = 0/12, 9) 2400  ??? Russian (Ukraine) or Russian (Belarus) ???


Belarus 1042
English Canada 1109     1009      91009    1009    HKL( 1009 = 9/8, 9)   1009
English GB (UK)????      809      90809     809    HKL( 809  = 9/4, 9)    809
English AU     ????      C09      90c09     c09    HKL( c09  = 9/6, 9)    c09
English US     1121      409      90409     409    HKL( 409  = 9/2, 9)    409
French Canada  1160      C0C      c0c       c0c    HKL( c0c  = c/6, 0)    c0c
French French            40C
Russian        1390      419      90419     419    HKL( 419 = 19/2, 9)    419
Ukrainian      1489      422      80422     422    HKL( 422 = 22/2, 8)    422
Ukr Enhanced           20422
Russian (Belarus) 1821
Russian (Ukraine) 1826

Swiss French             10C      ???
American Western       1042B      ???
UnitedStates International 20409
US ...  30409 50409
Russian Mnemonic       20419


Power-Shell
Get-WinUserLanguageList

LanguageTag     : ru-BY
Autonym         : русский (Беларусь)
EnglishName     : Russian
LocalizedName   : Russian (Belarus)
ScriptName      : Cyrillic
HKL = 4192000
InputMethodTips : {2000:00000419}
Spellchecking   : True
Handwriting     : False
--


fffffffff0a80422    422    HKL( 422 = 22/2, 8)   422
0a80422    422    HKL( 422 = 22/2, 8)   422
???  00020422 + 04220000 = 4240422 ??
???  0a80000 - 00020000 - a6_0000
LanguageTag     : uk
Autonym         : українська
EnglishName     : Ukrainian
LocalizedName   : Ukrainian
ScriptName      : Cyrillic
InputMethodTips : {0422:00020422}
Spellchecking   : True
Handwriting     : False





*/

data class WindowsInputSource (
    override val id: HKL,
    override val displayName: String,
    //val shortName: String,
    override val languageCode: String,
    override val languageName: String,
    override val countryCode: String,
    override val countryName: String

) : InputSource


fun getKeyboardLocaleAsInputSource(hkl: HKL): WindowsInputSource {
    val langId = hkl.toHKLEntries().languageID
    val localeId = MAKELCID(langId, SORT_DEFAULT)
    //return GetLocaleInfo(localeId, LOCALE_SLANGUAGE)
    //return GetLocaleInfo(localeId, LOCALE_SENGLISHDISPLAYNAME) // full name (Like 'English (Canada)')
    //return GetLocaleInfo(localeId, LOCALE_SENGLISHLANGUAGENAME) // single name (Like 'English', without country)
    //return GetLocaleInfo(localeId, LOCALE_SENGLISHCOUNTRYNAME) // country name (Like 'Ukraine', 'Australia')
    //return GetLocaleInfo(localeId, LOCALE_SABBREVLANGNAME)  // short lang names like ENG, ENA, ENU, ENC, RUS, UKR, ZZZ, FRC,
    //return GetLocaleInfo(localeId, LOCALE_SABBREVCTRYNAME)  // short country names like USA, AUS, GBR, CAN, RUS, UKR, BLR, FRC,
    //return GetLocaleInfo(localeId, LOCALE_ILANGUAGE) // languages as digits/number, like 0409, 0419
    //return GetLocaleInfo(localeId, LOCALE_ICOUNTRY) // country as strange (non-repeating) digits/number, like 1, 7, 380, 375
    // +++
    //return GetLocaleInfo(localeId, LOCALE_SNAME) // returns java-like format, like en-US, ru-RU, uk-UA, en-CA

    // val localeAbbr = GetLocaleInfo(localeId, LOCALE_SNAME) // returns java-like format, like en-US, ru-RU, uk-UA, en-CA

    return WindowsInputSource(
        id = hkl,
        displayName = GetLocaleInfo(localeId, LOCALE_SENGLISHDISPLAYNAME),
        //shortName = GetLocaleInfo(localeId, LOCALE_SABBREVLANGNAME), // for some cases it returns 'ZZZ'
        //shortName = GetLocaleInfo(localeId, LOCALE_SISO639LANGNAME),
        //shortName = GetLocaleInfo(localeId, LOCALE_SISO3166CTRYNAME),
        //shortName = GetLocaleInfo(localeId, LOCALE_SABBREVLANGNAME)
        //    .let { if (it != "ZZZ") it else GetLocaleInfo(localeId, LOCALE_SABBREVCTRYNAME) },
        //languageCode = localeAbbr.substringBefore("-", localeAbbr),
        languageCode = GetLocaleInfo(localeId, LOCALE_SISO639LANGNAME),
        languageName = GetLocaleInfo(localeId, LOCALE_SENGLISHLANGUAGENAME),
        //countryCode = localeAbbr.substringAfter("-", "")
        //    .ifEmpty { GetLocaleInfo(localeId, LOCALE_SABBREVCTRYNAME) },
        countryCode = GetLocaleInfo(localeId, LOCALE_SISO3166CTRYNAME),
        countryName = GetLocaleInfo(localeId, LOCALE_SENGLISHCOUNTRYNAME),
    )
}


private val cachedInputSources = ConcurrentHashMap<HKL, WindowsInputSource>()

private fun getInputSourceFor(hkl: HKL): WindowsInputSource =
    cachedInputSources.computeIfAbsent(hkl) { getKeyboardLocaleAsInputSource(it) }


fun selectKeyboard(locale: Locale) {

    val inputSources = GetKeyboardLayoutList().map { getInputSourceFor(it) }

    val targetInputSource = inputSources.findInputSource(locale)
    checkNotNull(targetInputSource) { "InputSource/KeyboardLayout for $locale is not found." }

    val currentKeyboard = GetKeyboardLayout()
    val currentInputSource = getInputSourceFor(currentKeyboard)
    //checkNotNull(currentInputSource) { "InputSource/KeyboardLayout for $locale is not found." }
    if (currentInputSource.id == targetInputSource.id) {
        log.debug { "Windows keyboard [${currentInputSource}] is already selected." }
        return
    }

    // It does not update keyboard in system tray??
    // ActivateKeyboardLayout(targetInputSource.id)

    // This probably undocumented approach works fine.
    ActivateKeyboardLayoutAlt(targetInputSource.id)
}
