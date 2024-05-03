@file:Suppress("FunctionName", "unused", "PackageDirectoryMismatch", "SpellCheckingInspection", "Since15")
package com.mvv.win.winapi.gdi

import com.mvv.win.winapi.*


typealias HBRUSH = HANDLE
val ValueLayout_HBRUSH: ValueLayout_HANDLE_Type = ValueLayout_HANDLE.withName("HBRUSH")

typealias HGDIOBJ = HANDLE
val ValueLayout_HGDIOBJ: ValueLayout_HANDLE_Type = ValueLayout_HANDLE.withName("HGDIOBJ")

typealias COLORREF = DWORD
val ValueLayout_COLORREF: ValueLayout_DWORD_Type = ValueLayout_DWORD.withName("COLORREF")
