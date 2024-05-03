@file:Suppress("unused", "SpellCheckingInspection", "FunctionName", "PackageDirectoryMismatch")
package com.mvv.win.winapi.window

import com.mvv.foreign.*
import com.mvv.win.*
import com.mvv.win.winapi.*


fun GetForegroundWindow(): HWND = nativeContext {
    // User32.dll / User32.lib
    // winuser.h (include Windows.h)
    // HWND GetForegroundWindow()
    functionHandle(WinModule.User, "GetForegroundWindow", ValueLayout_HWND)
        .call()
}


// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-iswindow
//
fun IsWindow(hWnd: HWND): Boolean = nativeContext {
    // User32.dll / User32.lib / winuser.h
    // BOOL IsWindow( [in, optional] HWND hWnd )
    functionHandle(WinModule.User, "IsWindow", ValueLayout_BOOL, ValueLayout_HWND)
        .call<BOOL>(hWnd).asBool
}


// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-postmessagew
//
fun PostMessage(hWnd: HWND, msg: UINT, wParam: WPARAM, lParam: LPARAM): BOOL = nativeContext {

    // User32.dll / User32.lib
    // winuser.h (include Windows.h)
    //
    // BOOL PostMessageW(
    //  [in, optional] HWND   hWnd,
    //  [in]           UINT   Msg,
    //  [in]           WPARAM wParam,
    //  [in]           LPARAM lParam
    // )

    functionHandle(WinModule.User, "PostMessageW",
        returnLayout = ValueLayout_BOOL,
        ValueLayout_HWND, ValueLayout_UINT, ValueLayout_WPARAM, ValueLayout_LPARAM)
            .call(hWnd, msg, wParam, lParam)
}


// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-sendmessagew
//
fun SendMessage(hWnd: HWND, msg: UINT, wParam: WPARAM, lParam: LPARAM): BOOL = nativeContext {

    // User32.dll / User32.lib
    // winuser.h (include Windows.h)
    //
    // BOOL SendMessageW(
    //  [in, optional] HWND   hWnd,
    //  [in]           UINT   Msg,
    //  [in]           WPARAM wParam,
    //  [in]           LPARAM lParam
    // )

    functionHandle(WinModule.User, "SendMessageW",
        returnLayout = ValueLayout_BOOL,
        ValueLayout_HWND, ValueLayout_UINT, ValueLayout_WPARAM, ValueLayout_LPARAM)
            .call(hWnd, msg, wParam, lParam)
}


// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-getwindowrect
//
fun GetWindowRect(hWnd: HWND): RECT? = nativeContext {
    // User32.dll / User32.lib / winuser.h (windows.h)
    // BOOL GetWindowRect( [in] HWND hWnd,  [out] LPRECT lpRect )
    //
    val rect = arena.allocate(StructLayout_RECT)
    val success = functionHandle(WinModule.User, "GetWindowRect", ValueLayout_BOOL,
        ValueLayout_HWND, ValueLayout_PTR.withTargetLayout(StructLayout_RECT))
        .call<BOOL>(hWnd, rect).asBool
    if (success) rect.rect else null
}


// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-setwindowpos
//
// uFlags - bit mask of SWP_XXX flags
//
fun SetWindowPos(hWnd: HWND, hWndInsertAfter: HWND, x: Int, y: Int, cx: Int, cy: Int, uFlags: UINT = 0): Boolean = nativeContext {
    // User32.dll / User32.lib / winuser.h
    // BOOL SetWindowPos(
    //  [in]           HWND hWnd,
    //  [in, optional] HWND hWndInsertAfter,
    //  [in]           int  X,
    //  [in]           int  Y,
    //  [in]           int  cx,
    //  [in]           int  cy,
    //  [in]           UINT uFlags
    // )
    functionHandle(WinModule.User, "SetWindowPos",
        ValueLayout_BOOL,
        ValueLayout_HWND, ValueLayout_HWND,
        ValueLayout_C_INT, ValueLayout_C_INT, ValueLayout_C_INT, ValueLayout_C_INT,
        ValueLayout_UINT)
        .call<BOOL>(hWnd, hWndInsertAfter, x, y, cx, cy, uFlags).asBool
}


fun SetWindowPos(hWnd: HWND, rect: RECT?, uFlags: UINT = 0): Boolean =
    SetWindowPos(hWnd, 0, rect, uFlags)

fun SetWindowPos(hWnd: HWND, hWndInsertAfter: HWND, rect: RECT?, uFlags: UINT = 0): Boolean =
    if (rect != null)
        SetWindowPos(hWnd, hWndInsertAfter, rect.left, rect.top, rect.width, rect.height, uFlags)
    else
        SetWindowPos(hWnd, hWndInsertAfter, 0, 0, 0, 0, uFlags or SWP_NOMOVE or SWP_NOSIZE)


// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-redrawwindow
//
fun RedrawWindow(hWnd: HWND, flags: UINT): Boolean = nativeContext {
    // User32.dll / User32.lib / winuser.h
    //
    // BOOL RedrawWindow(
    //  [in] HWND       hWnd,
    //  [in] const RECT *lprcUpdate,
    //  [in] HRGN       hrgnUpdate,
    //  [in] UINT       flags
    // )
    functionHandle(WinModule.User, "RedrawWindow",
        returnLayout = ValueLayout_BOOL,
        ValueLayout_HWND, ValueLayout_PTR, ValueLayout_PTR, ValueLayout_UINT)
            .call<BOOL>(hWnd, allocateNullPtr(), allocateNullPtr(), flags).asBool
}


// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-invalidaterect
//
fun InvalidateRect(hWnd: HWND, bErase: Boolean): Boolean = nativeContext {
    // User32.dll / User32.lib / winuser.h
    //
    // BOOL InvalidateRect(
    //  [in] HWND       hWnd,
    //  [in] const RECT *lpRect,
    //  [in] BOOL       bErase
    // )
    functionHandle(WinModule.User, "InvalidateRect",
        returnLayout = ValueLayout_BOOL,
        ValueLayout_HWND, ValueLayout_PTR, ValueLayout_BOOL)
            .call<BOOL>(hWnd, allocateNullPtr(), bErase.asBOOL).asBool
}


// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-begindeferwindowpos
//
fun BeginDeferWindowPos(nNumWindows: Int) : HDWP = nativeContext {
    // User32.dll / User32.lib / winuser.h
    // HDWP BeginDeferWindowPos( [in] int nNumWindows )
    //
    functionHandle(WinModule.User, "BeginDeferWindowPos", ValueLayout_HDWP, ValueLayout_C_INT)
        .call<HDWP>(nNumWindows)
}

// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-deferwindowpos
//
fun DeferWindowPos(hWinPosInfo: HDWP, hWnd: HWND, hWndInsertAfter: HWND,
                   x: Int, y: Int, cx: Int, cy: Int,
                   uFlags: UINT): HDWP = nativeContext {
    // User32.dll / winuser.h
    // HDWP DeferWindowPos(
    //  [in]           HDWP hWinPosInfo,
    //  [in]           HWND hWnd,
    //  [in, optional] HWND hWndInsertAfter,
    //  [in]           int  x,
    //  [in]           int  y,
    //  [in]           int  cx,
    //  [in]           int  cy,
    //  [in]           UINT uFlags
    // )
    functionHandle(WinModule.User, "DeferWindowPos", ValueLayout_HDWP,
        ValueLayout_HDWP, ValueLayout_HWND, ValueLayout_HWND,
        ValueLayout_C_INT, ValueLayout_C_INT, ValueLayout_C_INT, ValueLayout_C_INT,
        ValueLayout_UINT,
        )
        .call<HDWP>(hWinPosInfo, hWnd, hWndInsertAfter, x, y, cx, cy, uFlags)
}


// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-enddeferwindowpos
//
fun EndDeferWindowPos(hWinPosInfo: HDWP): Boolean = nativeContext {
    // User32.dll
    // BOOL EndDeferWindowPos( [in] HDWP hWinPosInfo )
    //
    functionHandle(WinModule.User, "EndDeferWindowPos", ValueLayout_BOOL, ValueLayout_HDWP)
        .call<BOOL>(hWinPosInfo).asBool
}


/*

 Other useful

 Windows (Windows and Messages)
  https://learn.microsoft.com/en-us/windows/win32/winmsg/windows

 FindWindowExW
  https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-findwindowexw
  Retrieves a handle to a window whose class name and window name match the specified strings.
  The function searches child windows, beginning with the one following the specified child window.
  This function does not perform a case-sensitive search.

 GetWindow
  https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-getwindow
  Retrieves a handle to a window that has the specified relationship (Z-Order or owner) to the specified window.
  GW_CHILD, GW_ENABLEDPOPUP, GW_HWNDFIRST, GW_HWNDLAST, GW_HWNDNEXT, GW_HWNDPREV, GW_OWNER

 FindWindowW
  https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-findwindoww
  Retrieves a handle to the top-level window whose class name and window name match the specified strings.
  This function does not search child windows.
  This function does not perform a case-sensitive search.

 GetMessageExtraInfo
  https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-getmessageextrainfo
  Retrieves the extra message information for the current thread.
  Extra message information is an application- or driver-defined value associated with the current thread's message queue.

 GetShellWindow
  https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-getshellwindow
  Retrieves a handle to the Shell's desktop window.
  (introduced in Windows 8)

 GetDesktopWindow
  https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-getdesktopwindow
   The return value is a handle to the desktop window.
   (introduced in Windows 8)

 GetWindowInfo
  https://learn.microsoft.com/en-us/windows/win32/api/winuser/ns-winuser-windowinfo

 GetWindowPlacement / SetWindowPlacement
  Retrieves the show state and the restored, minimized, and maximized positions of the specified window.
  https://learn.microsoft.com/en-us/windows/win32/api/winuser/ns-winuser-windowplacement

 GetWindowModuleFileNameW
  https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-getwindowmodulefilenamew

 WindowFromPoint
 WindowFromPhysicalPoint

 GetTitleBarInfo
  https://learn.microsoft.com/en-us/windows/win32/api/winuser/ns-winuser-titlebarinfo

 GetSystemMetrics
  https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-getsystemmetrics

 GetWindowThreadProcessId

 GetWindowWord

 InSendMessage
 InSendMessageEx

 ++
 IsWindow
  https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-iswindow
  BOOL IsWindow( [in, optional] HWND hWnd );

 RealChildWindowFromPoint
 RealGetWindowClassW

 RegisterShellHookWindow

 ++
 SendMessageCallbackW
 SendNotifyMessageW

 ++
 ShowOwnedPopups

 ShowWindow
 ShowWindowAsync

 SetClassLongPtrW
  ULONG_PTR SetClassLongPtrW( [in] HWND hWnd, [in] int nIndex, [in] LONG_PTR dwNewLong);
 SetClassLongW
  DWORD SetClassLongW( [in] HWND hWnd, [in] int nIndex, [in] LONG dwNewLong );
 SetClassWord
  WORD SetClassWord( [in] HWND hWnd, [in] int nIndex, [in] WORD wNewWord );
 SetWindowLongW
 SetWindowLongPtrW
  Changes an attribute of the specified window. The function also sets a value at the specified offset in the extra window memory.

 SetLayeredWindowAttributes
  Sets the opacity and transparency color key of a layered window.

 SetMessageExtraInfo
  Sets the extra message information for the current thread.
  Extra message information is an application- or driver-defined value associated with the current thread's message queue.
  An application can use the GetMessageExtraInfo function to retrieve a thread's extra message information.
  LPARAM SetMessageExtraInfo( [in] LPARAM lParam );

 SetProcessDefaultLayout

 SetTimer

 SetWindowsHookExW
*/
