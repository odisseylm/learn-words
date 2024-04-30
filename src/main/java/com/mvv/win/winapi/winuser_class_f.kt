@file:Suppress("FunctionName", "PackageDirectoryMismatch", "unused")
package com.mvv.win.winapi.window

import com.mvv.win.WinModule
import com.mvv.win.call
import com.mvv.win.functionHandle
import com.mvv.win.nativeContext
import com.mvv.win.winapi.*



// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-getclasslongw
//
fun GetClassLong(hWnd: HWND, nIndex: Int): DWORD = nativeContext {
    // User32.dll / User32.lib / winuser.h
    // DWORD GetClassLongW( [in] HWND hWnd, [in] int nIndex )
    //
    functionHandle(WinModule.User, "GetClassLongW",
        ValueLayout_DWORD,ValueLayout_HWND, ValueLayout_C_INT)
        .call<DWORD>(hWnd, nIndex)
}


// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-setclasslongw
//
fun SetClassLong(hWnd: HWND, nIndex: Int, dwNewLong: LONG): DWORD = nativeContext {
    // User32.dll / User32.lib / winuser.h
    // DWORD SetClassLongW( [in] HWND hWnd, [in] int nIndex, [in] LONG dwNewLong )
    //
    functionHandle(WinModule.User, "SetClassLongW",
        ValueLayout_DWORD,
        ValueLayout_HWND, ValueLayout_C_INT, ValueLayout_LONG)
        .call<DWORD>(hWnd, nIndex, dwNewLong)
}


// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-getclasslongptrw
//
fun GetClassLongPtr(hWnd: HWND, nIndex: Int): PTR = nativeContext {
    // User32.dll / User32.lib / winuser.h
    // ULONG_PTR GetClassLongPtrW( [in] HWND hWnd, [in] int nIndex )
    //
    functionHandle(WinModule.User, "SetClassLongPtrW",
        ValueLayout_PTR,
        ValueLayout_HWND, ValueLayout_C_INT)
        .call<PTR>(hWnd, nIndex)
}


// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-setclasslongptrw
//
fun SetClassLongPtr(hWnd: HWND, nIndex: Int, dwNewLong: PTR): PTR = nativeContext {
    // User32.dll / User32.lib / winuser.h
    // ULONG_PTR SetClassLongPtrW( [in] HWND hWnd, [in] int nIndex, [in] LONG_PTR dwNewLong )
    //
    functionHandle(WinModule.User, "SetClassLongPtrW",
        ValueLayout_PTRPlain,
        ValueLayout_HWND, ValueLayout_C_INT, ValueLayout_PTRPlain)
        .call<PTR>(hWnd, nIndex, dwNewLong)
}


// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-getwindowlongw
//
fun GetWindowLong(hWnd: HWND, nIndex: Int): LONG = nativeContext {
    // User32.dll / User32.lib / winuser.h
    // LONG GetWindowLongW( [in] HWND hWnd, [in] int nIndex )
    //
    functionHandle(WinModule.User, "GetWindowLongW",
        ValueLayout_LONG, ValueLayout_HWND, ValueLayout_C_INT)
        .call<LONG>(hWnd, nIndex)
}


// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-setwindowlongw
//
fun SetWindowLong(hWnd: HWND, nIndex: Int, dwNewLong: LONG): LONG = nativeContext {
    // User32.dll / User32.lib / winuser.h
    // LONG SetWindowLongW( [in] HWND hWnd, [in] int nIndex, [in] LONG dwNewLong )
    //
    functionHandle(WinModule.User, "SetWindowLongW",
        ValueLayout_LONG, ValueLayout_HWND, ValueLayout_C_INT, ValueLayout_LONG)
        .call<LONG>(hWnd, nIndex, dwNewLong)
}


// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-getwindowlongptrw
//
fun GetWindowLongPtr(hWnd: HWND, nIndex: Int): PTR = nativeContext {
    // User32.dll / User32.lib / winuser.h
    // LONG_PTR GetWindowLongPtrW( [in] HWND hWnd, [in] int  nIndex )
    //
    functionHandle(WinModule.User, "GetWindowLongPtrW",
        ValueLayout_PTR, ValueLayout_HWND, ValueLayout_C_INT)
        .call<PTR>(hWnd, nIndex)
}


// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-setwindowlongptrw
//
fun SetWindowLongPtr(hWnd: HWND, nIndex: Int, dwNewLong: PTR): PTR = nativeContext {
    // User32.dll / User32.lib / winuser.h
    // LONG_PTR SetWindowLongPtrW( [in] HWND hWnd, [in] int nIndex, [in] LONG_PTR dwNewLong )
    //
    functionHandle(WinModule.User, "SetWindowLongPtrW",
        ValueLayout_PTR, ValueLayout_HWND, ValueLayout_C_INT, ValueLayout_PTR)
        .call<PTR>(hWnd, nIndex, dwNewLong)
}


fun GetWindowStyle(hWnd: HWND): DWORD = GetWindowLong(hWnd, GWL_STYLE)
fun SetWindowStyle(hWnd: HWND, style: LONG): DWORD = SetWindowLong(hWnd, GWL_STYLE, style)
fun GetWindowExStyle(hWnd: HWND): DWORD = GetWindowLong(hWnd, GWL_EXSTYLE)
fun SetWindowExStyle(hWnd: HWND, style: LONG): DWORD = SetWindowLong(hWnd, GWL_EXSTYLE, style)


// swpFlags - bit mask of SWP_XXX flags
//
fun ModifyWindowStyle(hWnd: HWND, dwRemove: DWORD = 0, dwAdd: DWORD = 0, swpFlags: UINT = 0): DWORD {
    val current = GetWindowStyle(hWnd)
    val modified = (current and dwRemove.inv()) or dwAdd
    SetWindowStyle(hWnd, modified)

    if (swpFlags != 0) SetWindowPos(hWnd, 0, null, swpFlags)
    return modified
}


// swpFlags - bit mask of SWP_XXX flags
//
fun ModifyWindowExStyle(hWnd: HWND, dwAdd: DWORD = 0, dwRemove: DWORD = 0, swpFlags: UINT = 0): DWORD {
    val current = GetWindowExStyle(hWnd)
    val modified = (current and dwRemove.inv()) or dwAdd
    SetWindowExStyle(hWnd, modified)

    if (swpFlags != 0) SetWindowPos(hWnd, 0, null, swpFlags)
    return modified
}
