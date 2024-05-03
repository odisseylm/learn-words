@file:Suppress("unused", "SpellCheckingInspection", "PackageDirectoryMismatch", "Since15")
package com.mvv.win.winapi.window

import com.mvv.win.winapi.LONG
import com.mvv.win.winapi.ValueLayout_LONG
import com.mvv.foreign.MemoryLayout
import com.mvv.foreign.MemorySegment
import com.mvv.foreign.StructLayout


//------------------------------------------------------------------------------
//                                   RECT
//------------------------------------------------------------------------------

// https://learn.microsoft.com/en-us/windows/win32/api/windef/ns-windef-rect
//
// Windef.h
//
// typedef struct tagRECT {
//  LONG left; LONG top; LONG right; LONG bottom;
// } RECT, *PRECT, *NPRECT, *LPRECT;
//
data class RECT (val left: LONG, val top: LONG, val right: LONG, val bottom: LONG)

inline val RECT.width : LONG get() = right - left
inline val RECT.height: LONG get() = bottom - top

val StructLayout_RECT: StructLayout = MemoryLayout.structLayout(
    ValueLayout_LONG.withName("left"),
    ValueLayout_LONG.withName("top"),
    ValueLayout_LONG.withName("right"),
    ValueLayout_LONG.withName("bottom"),
).withName("RECT")

var MemorySegment.rect: RECT
    get() = RECT(
        left   = this.getAtIndex(ValueLayout_LONG, 0),
        top    = this.getAtIndex(ValueLayout_LONG, 1),
        right  = this.getAtIndex(ValueLayout_LONG, 2),
        bottom = this.getAtIndex(ValueLayout_LONG, 3),
    )
    set(value) {
        this.setAtIndex(ValueLayout_LONG, 0, value.left)
        this.setAtIndex(ValueLayout_LONG, 1, value.right)
        this.setAtIndex(ValueLayout_LONG, 2, value.top)
        this.setAtIndex(ValueLayout_LONG, 3, value.bottom)
    }
