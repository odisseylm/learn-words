@file:Suppress("FunctionName", "unused", "PackageDirectoryMismatch")
package com.mvv.win.winapi.locale

import com.mvv.win.*
import com.mvv.win.winapi.*
import com.mvv.win.winapi.keyboard.*
import com.mvv.foreign.*



// https://learn.microsoft.com/en-us/windows/win32/api/winnls/nf-winnls-getlocaleinfow
//
fun GetLocaleInfo(localeId: LCID, lcType: LCTYPE): String = nativeContext {
    // winnls.h  Kernel32.dll
    //
    // int GetLocaleInfoW(
    //  [in]            LCID   Locale,
    //  [in]            LCTYPE LCType,
    //  [out, optional] LPWSTR lpLCData,
    //  [in]            int    cchData
    // )

    val fGetLocaleInfo = functionHandle(WinModule.Kernel, "GetLocaleInfoW",
        ValueLayout_C_INT,
        // args
        ValueLayout_LCID, ValueLayout_LCTYPE, ValueLayout.ADDRESS, ValueLayout_C_INT)

    val requiredBufferLength = fGetLocaleInfo.call<Int>(localeId, lcType, allocateNullPtr(), 0)

    val buffer = allocateWinUtf16String(requiredBufferLength)
    val copiedLength = fGetLocaleInfo.call<Int>(localeId, lcType, buffer, requiredBufferLength)

    val requestedLocaleSubData = buffer.winUtf16StringToJavaString(charCount = copiedLength)

    dumpNative(fGetLocaleInfo, requestedLocaleSubData)
    requestedLocaleSubData
}


// https://learn.microsoft.com/en-us/windows/win32/api/winnls/nf-winnls-getlocaleinfoex
//
fun GetLocaleInfoEx(localeName: String, lcType: LCTYPE): String = nativeContext {
    // Kernel32.dll / Kernel32.lib / winnls.h
    // int GetLocaleInfoEx(
    //  [in, optional]  LPCWSTR lpLocaleName,
    //  [in]            LCTYPE  LCType,
    //  [out, optional] LPWSTR  lpLCData,
    //  [in]            int     cchData
    // )

    val fGetLocaleInfoEx = functionHandle(WinModule.Kernel, "GetLocaleInfoEx",
        ValueLayout_C_INT,
        // args
        ValueLayout.ADDRESS, ValueLayout_LCTYPE, ValueLayout.ADDRESS, ValueLayout_C_INT)

    val localeNameNative = allocateWinUtf16String(localeName)
    @Suppress("DuplicatedCode")
    val requiredBufferLength = fGetLocaleInfoEx.call<Int>(localeNameNative, lcType, allocateNullPtr(), 0)

    val buffer = allocateWinUtf16String(requiredBufferLength)
    val copiedLength = fGetLocaleInfoEx.call<Int>(localeNameNative, lcType, buffer, requiredBufferLength)

    val requestedLocaleSubData = buffer.winUtf16StringToJavaString(charCount = copiedLength)

    dumpNative(fGetLocaleInfoEx, requestedLocaleSubData)
    requestedLocaleSubData
}


// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-activatekeyboardlayout
//
fun ActivateKeyboardLayout(hkl: HKL, flags: Long = 0): HKL = nativeContext {
    // User32.dll / User32.lib / winuser.h (include Windows.h)
    // HKL ActivateKeyboardLayout( [in] HKL  hkl, [in] UINT Flags )
    functionHandle(WinModule.User, "ActivateKeyboardLayout", ValueLayout_HKL, ValueLayout_HKL, ValueLayout_UINT)
        .call(hkl, flags.toInt())
}
