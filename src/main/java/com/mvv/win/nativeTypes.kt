@file:Suppress("unused")

package com.mvv.win

import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout



fun Any?.nativeToDebugString(): String = when (this) {
    is Byte   -> "$this (" + this.toUByte() .toString(16) + ")"
    is Short  -> "$this (" + this.toUShort().toString(16) + ")"
    is Int    -> "$this (" + this.toUInt()  .toString(16) + ")"
    is Long   -> "$this (" + this.toULong() .toString(16) + ")"
    else      -> this.toString()
}


fun MemorySegment.toList(elementLayout: ValueLayout.OfInt): List<Int> =
    this.toArray(elementLayout).toList()
fun MemorySegment.toList(elementLayout: ValueLayout.OfLong): List<Long> =
    this.toArray(elementLayout).toList()
fun MemorySegment.toList(elementLayout: ValueLayout.OfLong, count: Int): List<Long> =
    this.toArray(elementLayout).toList(0, count)


fun LongArray.toList(fromIndex: Int, toIndex: Int): List<Long> {
    val list = ArrayList<Long>(toIndex - fromIndex)
    for (i in fromIndex until toIndex)
        list.add(this[i])
    return list
}


fun MemorySegment.copyFrom(elementLayout: ValueLayout.OfByte, from: ByteArray) {
    require(this.byteSize() >= elementLayout.byteSize() * from.size)

    for (i in from.indices)
        this.set(elementLayout, i.toLong(), from[i])
}

fun MemorySegment.copyFrom(elementLayout: ValueLayout.OfChar, from: CharArray) {
    require(this.byteSize() >= elementLayout.byteSize() * from.size)

    for (i in from.indices)
        this.setAtIndex(elementLayout, i.toLong(), from[i])
}


fun Arena.allocateNullPtr(): MemorySegment =
    this.allocate(ValueLayout.ADDRESS.byteSize())
fun NativeContext.allocateNullPtr(): MemorySegment =
    this.arena.allocateNullPtr()


fun Arena.allocateInt(value: Int = 0): MemorySegment =
    this.allocate(ValueLayout.JAVA_INT.byteSize()).also {
        it.set(ValueLayout.JAVA_INT, 0, value)
    }


var MemorySegment.intValue: Int
    get() = this.get(ValueLayout.JAVA_INT, 0)
    set(value) = this.set(ValueLayout.JAVA_INT, 0, value)
