@file:Suppress("FunctionName", "unused", "PackageDirectoryMismatch")
package com.mvv.win.winapi.gdi

import com.mvv.win.*
import com.mvv.win.winapi.*



// https://learn.microsoft.com/en-us/windows/win32/api/wingdi/nf-wingdi-deleteobject
//
fun DeleteObject(hi: HGDIOBJ): Boolean = nativeContext {
    // Gdi32.dll / Gdi32.lib / wingdi.h
    // BOOL DeleteObject( [in] HGDIOBJ ho );
    functionHandle(WinModule.Gdi, "CreateSolidBrush",
        ValueLayout_BOOL, ValueLayout_HGDIOBJ)
        .call<BOOL>(hi).asBool
}


// https://learn.microsoft.com/en-us/windows/win32/api/wingdi/nf-wingdi-createsolidbrush
//
fun CreateSolidBrush(color: COLORREF): HBRUSH = nativeContext {
    // Gdi32.dll / Gdi32.lib / wingdi.h
    // HBRUSH CreateSolidBrush( [in] COLORREF color )
    functionHandle(WinModule.Gdi, "CreateSolidBrush",
        ValueLayout_HBRUSH, ValueLayout_COLORREF)
        .call(color)
}


// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-getsyscolorbrush
//
fun GetSysColorBrush(nIndex: Int): HBRUSH = nativeContext {
    // User32.dll / User32.lib / wingdi.h
    // HBRUSH GetSysColorBrush( [in] int nIndex )
    functionHandle(WinModule.User, "GetSysColorBrush",
        ValueLayout_HBRUSH, ValueLayout_C_INT)
        .call(nIndex)
}


// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-getsyscolor
//
fun GetSysColor(nIndex: Int): HBRUSH = nativeContext {
    // User32.dll / User32.lib / winuser.h
    // DWORD GetSysColor( [in] int nIndex )
    functionHandle(WinModule.User, "GetSysColor",
        ValueLayout_DWORD, ValueLayout_C_INT)
        .call(nIndex)
}
