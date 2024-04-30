@file:Suppress("PackageDirectoryMismatch")
package com.mvv.win.winapi.keyboardex

import com.mvv.win.winapi.keyboard.*
import com.mvv.win.winapi.locale.LANGID
import com.mvv.win.winapi.locale.toLCID
import com.mvv.win.winapi.messages.WM_INPUTLANGCHANGEREQUEST
import com.mvv.win.winapi.window.GetForegroundWindow
import com.mvv.win.winapi.window.PostMessage



data class HKLEntries (
    val languageID: LANGID,
    val primaryLanguageID: LANGID,
    val subLanguageID: LANGID,
    val sortID: Int,
) {
    override fun toString(): String = "HKL( ${languageID.toString(16)} = ${primaryLanguageID.toString(16)}/${subLanguageID.toString(16)}, ${sortID.toString(16)})"
}


fun Int.toHKLEntries(): HKLEntries = this.toLong().toHKLEntries()

fun HKL.toHKLEntries(): HKLEntries {
    val lang = LANGIDFROMLCID(this.toLCID())
    return HKLEntries(
        // https://learn.microsoft.com/en-us/windows/win32/intl/language-identifiers
        languageID = lang,                       // (this and 0xFFFF).toInt(),
        primaryLanguageID = PRIMARYLANGID(lang), // (this and 0b11_1111_1111).toInt(),
        subLanguageID = SUBLANGID(lang),         // ((this and 0b1111_1100_0000_0000) ushr 10).toInt(),
        sortID = SORTIDFROMLCID(this)            // ((this and 0b1111_0000_0000_0000_0000) ushr 16).toInt(),
    )
}

fun HKLEntries.toLanguage(): LANGID =
    //(primaryLanguageID.toLong() and 0b1_1111_1111) or ((subLanguageID.toLong() and 0b1111_111) shl 10)
    MAKELANGID(primaryLanguageID, subLanguageID)

fun HKLEntries.toHKLLanguage(): HKL = toLanguage().toHKL()


@Suppress("FunctionName") // Name similar to standard WinAPI functions
fun ActivateKeyboardLayoutAlt(hkl: HKL) =
    PostMessage(GetForegroundWindow(), WM_INPUTLANGCHANGEREQUEST, 1, hkl)
