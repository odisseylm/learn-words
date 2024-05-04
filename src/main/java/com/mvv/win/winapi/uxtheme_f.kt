@file:Suppress("FunctionName", "unused", "SpellCheckingInspection", "PackageDirectoryMismatch")
package com.mvv.win.winapi.uxtheme

import com.mvv.win.WinModule
import com.mvv.win.call
import com.mvv.win.functionHandle
import com.mvv.win.nativeContext
import com.mvv.win.winapi.*
import com.mvv.win.winapi.error.HR_SUCCEEDED
import com.mvv.win.winapi.gdi.COLORREF
import com.mvv.win.winapi.gdi.HBRUSH
import com.mvv.win.winapi.gdi.ValueLayout_COLORREF
import com.mvv.win.winapi.gdi.ValueLayout_HBRUSH
import com.mvv.win.winapi.window.HWND
import com.mvv.win.winapi.window.ValueLayout_HWND


// Links
//   https://github.com/komiyamma/win32-darkmode/
//   https://gist.github.com/xv/43bd4c944202a593ac8ec6daa299b471
//


// https://learn.microsoft.com/en-us/windows/win32/api/uxtheme/nf-uxtheme-getwindowtheme
//
fun GetWindowTheme(hWnd: HWND): HTHEME = nativeContext {
    // UxTheme.dll / UxTheme.lib / uxtheme.h
    // HTHEME GetWindowTheme( [in] HWND hwnd )
    //
    functionHandle(WinModule.UxTheme, "GetWindowTheme", ValueLayout_HTHEME, ValueLayout_HWND)
        .call(hWnd)
}


// https://learn.microsoft.com/en-us/windows/win32/api/uxtheme/nf-uxtheme-openthemedata
//
fun OpenThemeData(hWnd: HWND, classList: String): HTHEME = nativeContext {
    // HTHEME OpenThemeData(
    //  [in] HWND    hwnd,
    //  [in] LPCWSTR pszClassList
    // )
    functionHandle(WinModule.UxTheme, "OpenThemeData", ValueLayout_HTHEME, ValueLayout_HWND, ValueLayout_LPCWSTR)
        .call<HTHEME>(hWnd, allocateWinUtf16String(classList))
}


// https://learn.microsoft.com/en-us/windows/win32/api/uxtheme/nf-uxtheme-setwindowtheme
//
// See also
//  SetWindowThemeNonClientAttributes
//  SetWindowThemeAttribute
//  GetThemeXXX functions
//
fun SetWindowTheme(hWnd: HWND, subAppName: String, subIdList: String? = null): HRESULT = nativeContext {
    // uxtheme.h / UxTheme.dll / UxTheme.lib
    // HRESULT SetWindowTheme(
    //  [in] HWND    hwnd,
    //  [in] LPCWSTR pszSubAppName,
    //  [in] LPCWSTR pszSubIdList
    // )
    //
    // SetWindowTheme(hwndList, L"Explorer", NULL);
    //
    functionHandle(WinModule.UxTheme, "SetWindowTheme", ValueLayout_HRESULT, ValueLayout_HWND, ValueLayout_LPCWSTR, ValueLayout_LPCWSTR)
        .call<HRESULT>(hWnd, allocateWinUtf16String(subAppName), allocateWinUtf16StringOrNull(subIdList))
}


// https://learn.microsoft.com/en-us/windows/win32/api/uxtheme/nf-uxtheme-getthemesyscolor
//
fun GetThemeSysColor(hTheme: HTHEME, iColorId: Int): COLORREF = nativeContext {
    // UxTheme.dll/ UxTheme.lib / uxtheme.h
    // COLORREF GetThemeSysColor(
    //  [in] HTHEME hTheme,
    //  [in] int    iColorId
    // )
    functionHandle(WinModule.UxTheme, "GetThemeSysColor", ValueLayout_COLORREF, ValueLayout_HTHEME, ValueLayout_C_INT)
        .call(hTheme, iColorId)
}


// https://learn.microsoft.com/en-us/windows/win32/api/uxtheme/nf-uxtheme-getthemesyscolorbrush
//
fun GetThemeSysColorBrush(hTheme: HTHEME, iColorId: Int): HBRUSH = nativeContext {
    // UxTheme.dll/ UxTheme.lib / uxtheme.h
    // HBRUSH GetThemeSysColorBrush(
    //  [in] HTHEME hTheme,
    //  [in] int    iColorId
    // )
    functionHandle(WinModule.UxTheme, "GetThemeSysColorBrush", ValueLayout_HBRUSH, ValueLayout_HTHEME, ValueLayout_C_INT)
        .call(hTheme, iColorId)
}


// https://learn.microsoft.com/en-us/windows/win32/api/uxtheme/nf-uxtheme-isthemeactive
//
fun IsThemeActive(): Boolean = nativeContext {
    // BOOL IsThemeActive()
    functionHandle(WinModule.UxTheme, "IsThemeActive", ValueLayout_BOOL)
        .call<BOOL>().asBool
}


// https://learn.microsoft.com/en-us/windows/win32/api/uxtheme/nf-uxtheme-isappthemed
//
fun IsAppThemed(): Boolean = nativeContext {
    // BOOL IsAppThemed()
    functionHandle(WinModule.UxTheme, "IsAppThemed", ValueLayout_BOOL)
        .call<BOOL>().asBool
}


// https://learn.microsoft.com/en-us/windows/win32/api/uxtheme/nf-uxtheme-isthemepartdefined
//
//fun IsThemePartDefined() { }


// https://learn.microsoft.com/en-us/windows/win32/api/uxtheme/nf-uxtheme-getthemefilename
//
fun GetThemeFilename(hTheme: HTHEME, iPartId: Int, iStateId: Int, iPropId: Int): String? = nativeContext {
    // HRESULT GetThemeFilename(
    //  [in]  HTHEME hTheme,
    //  [in]  int    iPartId,
    //  [in]  int    iStateId,
    //  [in]  int    iPropId,
    //  [out] LPWSTR pszThemeFileName,
    //  [in]  int    cchMaxBuffChars
    // )
    val bufSize = 1024
    val nativeStr = allocateWinUtf16String(bufSize)
    val result = functionHandle(WinModule.UxTheme, "GetThemeFilename",
        ValueLayout_HRESULT,
        ValueLayout_HTHEME,
        ValueLayout_C_INT, ValueLayout_C_INT, ValueLayout_C_INT,
        ValueLayout_PTR, ValueLayout_C_INT)
            .call<HRESULT>(hTheme, iPartId, iStateId, iPropId, nativeStr, bufSize)

    if (HR_SUCCEEDED(result)) null else nativeStr.winUtf16StringToJavaString()
}


// Seems it is undocumented??
// https://gist.github.com/rounk-ctrl/b04e5622e30e0d62956870d5c22b7017
// https://lise.pnfsoftware.com/winpdb/A635109301F1013287C203CFC50484C87AC8E446C8924F1FB3D21B2080F57BD5-uxtheme.html
//
fun AllowDarkModeForWindow(hWnd: HWND, allowDark: Boolean): Boolean = nativeContext {
    functionHandle(WinModule.UxTheme, "AllowDarkModeForWindow", ValueLayout_BOOL, ValueLayout_HWND, ValueLayout_BOOL)
        .call<BOOL>(hWnd, allowDark.asBOOL).asBool
}


// Seems it is undocumented??
// https://gist.github.com/rounk-ctrl/b04e5622e30e0d62956870d5c22b7017
// https://lise.pnfsoftware.com/winpdb/A635109301F1013287C203CFC50484C87AC8E446C8924F1FB3D21B2080F57BD5-uxtheme.html
//
fun DarkModeEnabled(hWnd: HWND): Boolean = nativeContext {
    functionHandle(WinModule.UxTheme, "DarkModeEnabled", ValueLayout_BOOL, ValueLayout_HWND)
        .call<BOOL>(hWnd).asBool
}


// Seems it is undocumented??
// https://gist.github.com/rounk-ctrl/b04e5622e30e0d62956870d5c22b7017
// https://lise.pnfsoftware.com/winpdb/A635109301F1013287C203CFC50484C87AC8E446C8924F1FB3D21B2080F57BD5-uxtheme.html
//
fun ShouldAppsUseDarkMode(): Boolean = nativeContext {
    functionHandle(WinModule.UxTheme, "ShouldAppsUseDarkMode", ValueLayout_BOOL)
        .call<BOOL>().asBool
}


// Seems it is undocumeneted??
// https://gist.github.com/rounk-ctrl/b04e5622e30e0d62956870d5c22b7017
// https://lise.pnfsoftware.com/winpdb/A635109301F1013287C203CFC50484C87AC8E446C8924F1FB3D21B2080F57BD5-uxtheme.html
//
// See PreferredAppMode
//
fun SetPreferredAppMode(preferredAppMode: PreferredAppMode): Boolean =
    SetPreferredAppMode(preferredAppMode.nativeValue)
fun SetPreferredAppMode(preferredAppMode: Int): Boolean = nativeContext {
    functionHandle(WinModule.UxTheme, "SetPreferredAppMode", ValueLayout_BOOL, ValueLayout_C_INT)
        .call<BOOL>(preferredAppMode).asBool
}


fun SetThemeAppProperties(dwFlags: DWORD) = nativeContext {
    // void SetThemeAppProperties( DWORD dwFlags )
    functionHandle(WinModule.UxTheme, "SetThemeAppProperties", returnLayout = null, ValueLayout_DWORD)
        .call<Any?>(dwFlags)
}


/*

https://stackoverflow.com/questions/53501268/win10-dark-theme-how-to-use-in-winapi

using fnRtlGetNtVersionNumbers = void (WINAPI *)(LPDWORD major, LPDWORD minor, LPDWORD build);
// 1809 17763
using fnShouldAppsUseDarkMode = bool (WINAPI *)(); // ordinal 132
using fnAllowDarkModeForWindow = bool (WINAPI *)(HWND hWnd, bool allow); // ordinal 133
using fnAllowDarkModeForApp = bool (WINAPI *)(bool allow); // ordinal 135, removed since 18334
using fnFlushMenuThemes = void (WINAPI *)(); // ordinal 136
using fnRefreshImmersiveColorPolicyState = void (WINAPI *)(); // ordinal 104
using fnIsDarkModeAllowedForWindow = bool (WINAPI *)(HWND hWnd); // ordinal 137
using fnGetIsImmersiveColorUsingHighContrast = bool (WINAPI *)(IMMERSIVE_HC_CACHE_MODE mode); // ordinal 106
using fnOpenNcThemeData = HTHEME(WINAPI *)(HWND hWnd, LPCWSTR pszClassList); // ordinal 49
// Insider 18290
using fnShouldSystemUseDarkMode = bool (WINAPI *)(); // ordinal 138
// Insider 18334
using fnSetPreferredAppMode = PreferredAppMode (WINAPI *)(PreferredAppMode appMode); // ordinal 135, since 18334
using fnIsDarkModeAllowedForApp = bool (WINAPI *)(); // ordinal 139


void InitDarkMode()
{
    fnRtlGetNtVersionNumbers RtlGetNtVersionNumbers = reinterpret_cast<fnRtlGetNtVersionNumbers>(GetProcAddress(GetModuleHandleW(L"ntdll.dll"), "RtlGetNtVersionNumbers"));
    if (RtlGetNtVersionNumbers)
    {
        DWORD major, minor;
        RtlGetNtVersionNumbers(&major, &minor, &g_buildNumber);
        g_buildNumber &= ~0xF0000000;
        if (major == 10 && minor == 0 && 17763 <= g_buildNumber && g_buildNumber <= 18363) // Windows 10 1809 10.0.17763 - 1909 10.0.18363
        {
            HMODULE hUxtheme = LoadLibraryExW(L"uxtheme.dll", nullptr, LOAD_LIBRARY_SEARCH_SYSTEM32);
            if (hUxtheme)
            {
                _OpenNcThemeData = reinterpret_cast<fnOpenNcThemeData>(GetProcAddress(hUxtheme, MAKEINTRESOURCEA(49)));
                _RefreshImmersiveColorPolicyState = reinterpret_cast<fnRefreshImmersiveColorPolicyState>(GetProcAddress(hUxtheme, MAKEINTRESOURCEA(104)));
                _GetIsImmersiveColorUsingHighContrast = reinterpret_cast<fnGetIsImmersiveColorUsingHighContrast>(GetProcAddress(hUxtheme, MAKEINTRESOURCEA(106)));
                _ShouldAppsUseDarkMode = reinterpret_cast<fnShouldAppsUseDarkMode>(GetProcAddress(hUxtheme, MAKEINTRESOURCEA(132)));
                _AllowDarkModeForWindow = reinterpret_cast<fnAllowDarkModeForWindow>(GetProcAddress(hUxtheme, MAKEINTRESOURCEA(133)));

                auto ord135 = GetProcAddress(hUxtheme, MAKEINTRESOURCEA(135));
                if (g_buildNumber < 18334)
                    _AllowDarkModeForApp = reinterpret_cast<fnAllowDarkModeForApp>(ord135);
                else
                    _SetPreferredAppMode = reinterpret_cast<fnSetPreferredAppMode>(ord135);

                //_FlushMenuThemes = reinterpret_cast<fnFlushMenuThemes>(GetProcAddress(hUxtheme, MAKEINTRESOURCEA(136)));
                _IsDarkModeAllowedForWindow = reinterpret_cast<fnIsDarkModeAllowedForWindow>(GetProcAddress(hUxtheme, MAKEINTRESOURCEA(137)));

                if (_OpenNcThemeData &&
                    _RefreshImmersiveColorPolicyState &&
                    _ShouldAppsUseDarkMode &&
                    _AllowDarkModeForWindow &&
                    (_AllowDarkModeForApp || _SetPreferredAppMode) &&
                    //_FlushMenuThemes &&
                    _IsDarkModeAllowedForWindow)
                {
                    g_darkModeSupported = true;

                    AllowDarkModeForApp(true);
                    _RefreshImmersiveColorPolicyState();

                    g_darkModeEnabled = _ShouldAppsUseDarkMode() && !IsHighContrast();

                    FixDarkScrollBar();
                }
            }
        }
    }
}



Windows 11
  https://learn.microsoft.com/en-us/windows/apps/desktop/modernize/apply-windows-themes

inline bool IsColorLight(Windows::UI::Color& clr)
{
    return (((5 * clr.G) + (2 * clr.R) + clr.B) > (8 * 128));
}
*/



/*
bool IsDarkThemeActive()
{
    DWORD   type;
    DWORD   value;
    DWORD   count = 4;
    LSTATUS st = RegGetValue(
            HKEY_CURRENT_USER,
    TEXT("Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize"),
    TEXT("AppsUseLightTheme"),
    RRF_RT_REG_DWORD,
    &type,
    &value,
    &count );
    if ( st == ERROR_SUCCESS && type == REG_DWORD )
        return value == 0;
    return false;
}

When the Light/Dark setting is changed top level windows get WM_SETTINGCHANGE message.


https://learn.microsoft.com/en-us/windows/win32/api/uxtheme/nf-uxtheme-setwindowtheme
https://gist.github.com/rossy/ebd83ba8f22339ce25ef68bfc007dfd2

static void handle_nccreate(HWND window, CREATESTRUCTW *cs)
{
    struct window *data = cs->lpCreateParams;
    SetWindowLongPtrW(window, GWLP_USERDATA, (LONG_PTR)data);

    SetWindowTheme(window, L"DarkMode_Explorer", NULL);
    DwmSetWindowAttribute(window, DWMWA_USE_IMMERSIVE_DARK_MODE,
                          &(BOOL) { TRUE }, sizeof(BOOL));
}

  https://github.com/res2k/Windows10Colors/blob/master/Windows10Colors/Windows10Colors.cpp
  https://www.quppa.net/blog/2013/01/02/retrieving-windows-8-theme-colours/
  https://stackoverflow.com/questions/39261826/change-the-color-of-the-title-bar-caption-of-a-win32-application
  https://learn.microsoft.com/en-us/windows/win32/dwm/customframe

*/
