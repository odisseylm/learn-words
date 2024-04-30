@file:Suppress("FunctionName", "unused", "SpellCheckingInspection", "PackageDirectoryMismatch")
package com.mvv.win.winapi.uxtheme

import com.mvv.win.winapi.HANDLE
import com.mvv.win.winapi.ValueLayout_HANDLE



// Links
//   https://github.com/komiyamma/win32-darkmode/
//   https://gist.github.com/xv/43bd4c944202a593ac8ec6daa299b471
//


typealias HTHEME = HANDLE
val ValueLayout_HTHEME = ValueLayout_HANDLE


const val STAP_ALLOW_NONCLIENT = 1
const val STAP_ALLOW_CONTROLS = 0b10
const val STAP_ALLOW_WEBCONTENT = 0b100
const val STAP_VALIDBITS = (STAP_ALLOW_NONCLIENT or STAP_ALLOW_CONTROLS or STAP_ALLOW_WEBCONTENT)


// Seems it is undocumeneted??
// https://gist.github.com/rounk-ctrl/b04e5622e30e0d62956870d5c22b7017
// https://lise.pnfsoftware.com/winpdb/A635109301F1013287C203CFC50484C87AC8E446C8924F1FB3D21B2080F57BD5-uxtheme.html
//
enum class PreferredAppMode (val nativeValue: Int) {
    SPDAM_DEFAULT (0),
    SPDAM_ALLOWDARK (1),
    SPDAM_FORCEDARK (2),
    SPDAM_FORCELIGHT (3),
    SPDAM_MAX (4),
}
