@file:Suppress("FunctionName", "unused", "PackageDirectoryMismatch", "SpellCheckingInspection")
package com.mvv.win.winapi.gdi

import com.mvv.win.winapi.*
import java.lang.foreign.ValueLayout


typealias HBRUSH = HANDLE
val ValueLayout_HBRUSH: ValueLayout.OfLong = ValueLayout_HANDLE.withName("HBRUSH")

typealias HGDIOBJ = HANDLE
val ValueLayout_HGDIOBJ: ValueLayout.OfLong = ValueLayout_HANDLE.withName("HGDIOBJ")

typealias COLORREF = DWORD
val ValueLayout_COLORREF: ValueLayout.OfInt = ValueLayout_DWORD.withName("COLORREF")
