@file:Suppress("unused", "SpellCheckingInspection", "PackageDirectoryMismatch")
package com.mvv.win.winapi.window

import com.mvv.win.winapi.HANDLE
import com.mvv.win.winapi.ValueLayout_HANDLE
import com.mvv.foreign.AddressLayout
import com.mvv.foreign.ValueLayout


//#ifndef __MSABI_LONG
//#  ifndef __LP64__
//#    define __MSABI_LONG(x) x ## l
//#  else
//#    define __MSABI_LONG(x) x
//#  endif
//#endif


typealias HWND = HANDLE
val ValueLayout_HWND = ValueLayout_HANDLE


typealias HDWP = HANDLE
val ValueLayout_HDWP = ValueLayout_HANDLE


// typedef UINT_PTR WPARAM;
typealias WPARAM = Long
val ValueLayout_WPARAMAsPlain: ValueLayout.OfLong = ValueLayout.JAVA_LONG.withName("WPARAM")
val ValueLayout_WPARAMAsPtr: AddressLayout = ValueLayout.ADDRESS.withName("WPARAM")
val ValueLayout_WPARAM = ValueLayout_WPARAMAsPlain


// typedef LONG_PTR LPARAM;
typealias LPARAM = Long
val ValueLayout_LPARAMAsPlain: ValueLayout.OfLong = ValueLayout.JAVA_LONG.withName("LPARAM")
val ValueLayout_LPARAMAsPtr: AddressLayout = ValueLayout.ADDRESS.withName("LPARAM")
val ValueLayout_LPARAM = ValueLayout_LPARAMAsPlain


// typedef LONG_PTR LRESULT;
typealias LRESULT = Long
val ValueLayout_LRESULTAsPlain: ValueLayout.OfLong = ValueLayout.JAVA_LONG.withName("LRESULT")
val ValueLayout_LRESULTAsPtr: AddressLayout = ValueLayout.ADDRESS.withName("LRESULT")
val ValueLayout_LRESULT = ValueLayout_LRESULTAsPlain



const val HWND_TOP       : HWND = 0
const val HWND_BOTTOM    : HWND = 1
const val HWND_TOPMOST   : HWND = -1
const val HWND_NOTOPMOST : HWND = -2


const val WS_OVERLAPPED   = 0x00000000
const val WS_POPUP        = 0x80000000L.toInt()
const val WS_CHILD        = 0x40000000
const val WS_MINIMIZE     = 0x20000000
const val WS_VISIBLE      = 0x10000000
const val WS_DISABLED     = 0x08000000
const val WS_CLIPSIBLINGS = 0x04000000
const val WS_CLIPCHILDREN = 0x02000000
const val WS_MAXIMIZE     = 0x01000000
const val WS_CAPTION      = 0x00C00000
const val WS_BORDER       = 0x00800000
const val WS_DLGFRAME     = 0x00400000
const val WS_VSCROLL      = 0x00200000
const val WS_HSCROLL      = 0x00100000
const val WS_SYSMENU      = 0x00080000
const val WS_THICKFRAME   = 0x00040000
const val WS_GROUP        = 0x00020000
const val WS_TABSTOP      = 0x00010000
const val WS_MINIMIZEBOX  = 0x00020000
const val WS_MAXIMIZEBOX  = 0x00010000
const val WS_TILED        = WS_OVERLAPPED
const val WS_ICONIC       = WS_MINIMIZE
const val WS_SIZEBOX      = WS_THICKFRAME

const val WS_OVERLAPPEDWINDOW = (WS_OVERLAPPED or WS_CAPTION or WS_SYSMENU or WS_THICKFRAME or WS_MINIMIZEBOX or WS_MAXIMIZEBOX)
const val WS_TILEDWINDOW      =  WS_OVERLAPPEDWINDOW


const val WS_POPUPWINDOW = (WS_POPUP or WS_BORDER or WS_SYSMENU)
const val WS_CHILDWINDOW = (WS_CHILD)

const val WS_EX_DLGMODALFRAME  = 0x00000001
const val WS_EX_NOPARENTNOTIFY = 0x00000004
const val WS_EX_TOPMOST        = 0x00000008
const val WS_EX_ACCEPTFILES    = 0x00000010
const val WS_EX_TRANSPARENT    = 0x00000020
const val WS_EX_MDICHILD       = 0x00000040
const val WS_EX_TOOLWINDOW     = 0x00000080
const val WS_EX_WINDOWEDGE     = 0x00000100
const val WS_EX_CLIENTEDGE     = 0x00000200
const val WS_EX_CONTEXTHELP    = 0x00000400
const val WS_EX_RIGHT          = 0x00001000
const val WS_EX_LEFT           = 0x00000000
const val WS_EX_RTLREADING     = 0x00002000
const val WS_EX_LTRREADING     = 0x00000000
const val WS_EX_LEFTSCROLLBAR  = 0x00004000
const val WS_EX_RIGHTSCROLLBAR = 0x00000000
const val WS_EX_CONTROLPARENT  = 0x00010000
const val WS_EX_STATICEDGE     = 0x00020000
const val WS_EX_APPWINDOW      = 0x00040000

const val WS_EX_OVERLAPPEDWINDOW = (WS_EX_WINDOWEDGE or WS_EX_CLIENTEDGE)
const val WS_EX_PALETTEWINDOW    = (WS_EX_WINDOWEDGE or WS_EX_TOOLWINDOW or WS_EX_TOPMOST)
const val WS_EX_LAYERED          = 0x00080000
const val WS_EX_NOINHERITLAYOUT  = 0x00100000
//#if WINVER >= 0x0602
const val WS_EX_NOREDIRECTIONBITMAP = 0x00200000
//#endif
const val WS_EX_LAYOUTRTL  = 0x00400000
const val WS_EX_COMPOSITED = 0x02000000
const val WS_EX_NOACTIVATE = 0x08000000

const val CS_VREDRAW     = 0x0001
const val CS_HREDRAW     = 0x0002
const val CS_DBLCLKS     = 0x0008
const val CS_OWNDC       = 0x0020
const val CS_CLASSDC     = 0x0040
const val CS_PARENTDC    = 0x0080
const val CS_NOCLOSE     = 0x0200
const val CS_SAVEBITS    = 0x0800
const val CS_BYTEALIGNCLIENT  = 0x1000
const val CS_BYTEALIGNWINDOW  = 0x2000
const val CS_GLOBALCLASS = 0x4000
const val CS_IME         = 0x00010000
const val CS_DROPSHADOW  = 0x00020000
//#endif



const val SWP_NOSIZE       = 0x0001
const val SWP_NOMOVE       = 0x0002
const val SWP_NOZORDER     = 0x0004
const val SWP_NOREDRAW     = 0x0008
const val SWP_NOACTIVATE   = 0x0010
const val SWP_FRAMECHANGED = 0x0020
const val SWP_SHOWWINDOW   = 0x0040
const val SWP_HIDEWINDOW   = 0x0080
const val SWP_NOCOPYBITS   = 0x0100
const val SWP_NOOWNERZORDER  = 0x0200
const val SWP_NOSENDCHANGING = 0x0400

const val SWP_DRAWFRAME      = SWP_FRAMECHANGED
const val SWP_NOREPOSITION   = SWP_NOOWNERZORDER
const val SWP_DEFERERASE     = 0x2000
const val SWP_ASYNCWINDOWPOS = 0x4000


// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-redrawwindow
//
const val RDW_INVALIDATE    = 0x0001
const val RDW_INTERNALPAINT = 0x0002
const val RDW_ERASE         = 0x0004

const val RDW_VALIDATE      = 0x0008
const val RDW_NOINTERNALPAINT = 0x0010
const val RDW_NOERASE       = 0x0020

const val RDW_NOCHILDREN    = 0x0040
const val RDW_ALLCHILDREN   = 0x0080

const val RDW_UPDATENOW     = 0x0100
const val RDW_ERASENOW      = 0x0200

const val RDW_FRAME         = 0x0400
const val RDW_NOFRAME       = 0x0800


// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-setclasslongptrw
//
//
//#ifndef NOWINOFFSETS
const val GWL_WNDPROC         = -4
const val GWL_HINSTANCE       = -6
const val GWL_HWNDPARENT      = -8
const val GWL_STYLE           = -16
const val GWL_EXSTYLE         = -20
const val GWL_USERDATA        = -21
const val GWL_ID              = -12

const val GWLP_WNDPROC        = -4
const val GWLP_HINSTANCE      = -6
const val GWLP_HWNDPARENT     = -8
const val GWLP_USERDATA       = -21
const val GWLP_ID             = -12

const val GCL_MENUNAME        = -8
const val GCL_HBRBACKGROUND   = -10
const val GCL_HCURSOR         = -12
const val GCL_HICON           = -14
const val GCL_HMODULE         = -16
const val GCL_CBWNDEXTRA      = -18
const val GCL_CBCLSEXTRA      = -20
const val GCL_WNDPROC         = -24
const val GCL_STYLE           = -26
const val GCW_ATOM            = -32
const val GCL_HICONSM         = -34

const val GCLP_MENUNAME       = -8
const val GCLP_HBRBACKGROUND  = -10
const val GCLP_HCURSOR        = -12
const val GCLP_HICON          = -14
const val GCLP_HMODULE        = -16
const val GCLP_WNDPROC        = -24
const val GCLP_HICONSM        = -34
//#endif


// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-getsyscolor
//
const val COLOR_SCROLLBAR       = 0
const val COLOR_BACKGROUND      = 1
const val COLOR_ACTIVECAPTION   = 2
const val COLOR_INACTIVECAPTION = 3
const val COLOR_MENU            = 4
const val COLOR_WINDOW          = 5
const val COLOR_WINDOWFRAME     = 6
const val COLOR_MENUTEXT        = 7
const val COLOR_WINDOWTEXT      = 8
const val COLOR_CAPTIONTEXT     = 9
const val COLOR_ACTIVEBORDER    = 10
const val COLOR_INACTIVEBORDER  = 11
const val COLOR_APPWORKSPACE    = 12
const val COLOR_HIGHLIGHT       = 13
const val COLOR_HIGHLIGHTTEXT   = 14
const val COLOR_BTNFACE         = 15
const val COLOR_BTNSHADOW       = 16
const val COLOR_GRAYTEXT        = 17
const val COLOR_BTNTEXT         = 18
const val COLOR_INACTIVECAPTIONTEXT  = 19
const val COLOR_BTNHIGHLIGHT    = 20

const val COLOR_3DDKSHADOW      = 21
const val COLOR_3DLIGHT         = 22
const val COLOR_INFOTEXT        = 23
const val COLOR_INFOBK          = 24
const val COLOR_HOTLIGHT        = 26
const val COLOR_GRADIENTACTIVECAPTION  = 27
const val COLOR_GRADIENTINACTIVECAPTION  = 28
const val COLOR_MENUHILIGHT     = 29
const val COLOR_MENUBAR         = 30

const val COLOR_DESKTOP         = COLOR_BACKGROUND
const val COLOR_3DFACE          = COLOR_BTNFACE
const val COLOR_3DSHADOW        = COLOR_BTNSHADOW
const val COLOR_3DHIGHLIGHT     = COLOR_BTNHIGHLIGHT
const val COLOR_3DHILIGHT       = COLOR_BTNHIGHLIGHT
const val COLOR_BTNHILIGHT      = COLOR_BTNHIGHLIGHT
