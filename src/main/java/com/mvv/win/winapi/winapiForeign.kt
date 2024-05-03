@file:Suppress("unused")
package com.mvv.win.winapi

import com.mvv.gui.util.ifIndexNotFound
import com.mvv.win.*
import com.mvv.win.winapi.gdi.COLORREF
import com.mvv.win.winapi.gdi.ValueLayout_COLORREF
import com.mvv.foreign.*
import kotlin.math.min



fun Arena.allocateBOOL(value: Boolean = false): MemorySegment =
    this.allocate(ValueLayout_BOOL.byteSize()).also {
        it.setAtIndex(ValueLayout_BOOL, 0, if (value) 1 else 0)
    }
fun NativeContext.allocateBOOL(value: Boolean = false): MemorySegment =
    this.arena.allocateBOOL(value)


var MemorySegment.BOOL_Value: Boolean
    get() = this.getAtIndex(ValueLayout_BOOL, 0) != 0
    set(value) = this.setAtIndex(ValueLayout_BOOL, 0, if (value) 1 else 0)



fun Arena.allocateWinUtf16String(length: Int): MemorySegment = this.allocate((length + 1) * ValueLayout_WCHAR.byteSize())
fun NativeContext.allocateWinUtf16String(length: Int): MemorySegment = this.arena.allocateWinUtf16String(length)

fun Arena.allocateWinUtf16String(string: String): MemorySegment {
    val asChars = string.toCharArray()
    val mem = this.allocate((asChars.size + 1) * ValueLayout_WCHAR.byteSize())
    mem.copyFrom(ValueLayout_WCHAR, asChars)
    mem.setAtIndex(ValueLayout_WCHAR, asChars.size.toLong(), 0.toWCHAR())

    // probably better to use mem.setString() ??
    // mem.setString(0, string, Charsets.UTF_16)

    //val asBytes = string.toByteArray(Charsets.UTF_16)
    //val mem = this.allocate(asBytes.size + ValueLayout_WCHAR.byteSize())
    //mem.copyFrom(ValueLayout_BYTE, asBytes)
    //
    //mem.set(ValueLayout_BYTE, asBytes.size.toLong(), 0)
    //mem.set(ValueLayout_BYTE, asBytes.size.toLong() + 1, 0)

    return mem
}
fun NativeContext.allocateWinUtf16String(string: String): MemorySegment = this.arena.allocateWinUtf16String(string)

fun Arena.allocateWinUtf16StringOrNull(string: String?): MemorySegment =
    if (string != null) this.allocateWinUtf16String(string) else this.allocateNullPtr()
fun NativeContext.allocateWinUtf16StringOrNull(string: String?): MemorySegment =
    this.arena.allocateWinUtf16StringOrNull(string)



fun MemorySegment.winUtf16StringToJavaString(): String {
    require(this.byteSize() % 2 == 0L) { "UTF16 string bytes size should be even (${this.byteSize()})." }
    return winUtf16StringToJavaString(this.byteSize().toInt() / 2)
}

fun MemorySegment.winUtf16StringToJavaString(charCount: Int): String {

    if (charCount == 0) return ""

    require(this.byteSize() % 2 == 0L) { "UTF16 string bytes size should be even (${this.byteSize()})." }

    val chars = this.toArray(ValueLayout_WCHAR).toCharArray()
    val length = min(charCount, chars.indexOf(0.toChar()).ifIndexNotFound(chars.size))

    return String(chars, 0, length)
}


fun Arena.allocateCOLORREF(color: COLORREF): MemorySegment =
    this.allocate(ValueLayout_COLORREF)
        .also { it.setAtIndex(ValueLayout_COLORREF, 0, color) }
fun NativeContext.allocateCOLORREF(color: COLORREF): MemorySegment = this.arena.allocateCOLORREF(color)


fun Arena.allocateDWORD(value: DWORD): MemorySegment =
    this.allocate(ValueLayout_DWORD)
        .also { it.setAtIndex(ValueLayout_DWORD, 0, value) }
fun NativeContext.allocateDWORD(value: DWORD): MemorySegment = this.arena.allocateDWORD(value)


val MemoryLayout.dwByteSize: DWORD get() = this.byteSize().toDWORD()
val MemoryLayout.intByteSize: Int get() = this.byteSize().toInt()
