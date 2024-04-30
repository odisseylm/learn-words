@file:Suppress("SpellCheckingInspection", "unused", "PackageDirectoryMismatch")
package com.mvv.win.winapi.messages

import com.mvv.win.winapi.UINT


typealias MessageType = UINT

const val WM_INPUTLANGCHANGEREQUEST = 80


const val WM_THEMECHANGED    = 0x031A

const val WM_ACTIVATE        = 0x0006
const val WM_SYSCOLORCHANGE  = 0x0015
const val WM_STYLECHANGING   = 0x007C
const val WM_STYLECHANGED    = 0x007D

const val WM_SETFOCUS        = 0x0007
const val WM_KILLFOCUS       = 0x0008
const val WM_ENABLE          = 0x000A
const val WM_SETREDRAW       = 0x000B

const val WM_NCCREATE        = 0x0081
const val WM_NCDESTROY       = 0x0082
const val WM_NCCALCSIZE      = 0x0083
const val WM_NCHITTEST       = 0x0084
const val WM_NCPAINT         = 0x0085
const val WM_NCACTIVATE      = 0x0086
const val WM_SYNCPAINT       = 0x0088

const val WM_EXITSIZEMOVE    = 0x0232

const val WM_CANCELMODE      = 0x001F


const val WM_DWMCOMPOSITIONCHANGED       = 0x031e
const val WM_DWMNCRENDERINGCHANGED       = 0x031f
const val WM_DWMCOLORIZATIONCOLORCHANGED = 0x0320
const val WM_DWMWINDOWMAXIMIZEDCHANGE    = 0x0321
//#if _WIN32_WINNT >= 0x0601
const val WM_DWMSENDICONICTHUMBNAIL      = 0x0323
const val WM_DWMSENDICONICLIVEPREVIEWBITMAP = 0x0326
//#endif
