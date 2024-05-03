@file:Suppress("unused", "Since15")

package com.mvv.win

import com.mvv.foreign.*


fun Any?.nativeToDebugString(): String = when (this) {
    is Byte   -> "$this (" + this.toUByte() .toString(16) + ")"
    is Short  -> "$this (" + this.toUShort().toString(16) + ")"
    is Int    -> "$this (" + this.toUInt()  .toString(16) + ")"
    is Long   -> "$this (" + this.toULong() .toString(16) + ")"
    else      -> this.toString()
}


fun MemorySegment.toList(elementLayout: OfInt): List<Int> =
    this.toArray(elementLayout).toList()
fun MemorySegment.toList(elementLayout: OfLong): List<Long> =
    this.toArray(elementLayout).toList()
fun MemorySegment.toList(elementLayout: OfLong, count: Int): List<Long> =
    this.toArray(elementLayout).toList(0, count)


fun LongArray.toList(fromIndex: Int, toIndex: Int): List<Long> {
    val list = ArrayList<Long>(toIndex - fromIndex)
    for (i in fromIndex until toIndex)
        list.add(this[i])
    return list
}


fun MemorySegment.copyFrom(elementLayout: OfByte, from: ByteArray) {
    require(this.byteSize() >= elementLayout.byteSize() * from.size)

    for (i in from.indices)
        this.set(elementLayout, i.toLong(), from[i])
}

fun MemorySegment.copyFrom(elementLayout: OfChar, from: CharArray) {
    require(this.byteSize() >= elementLayout.byteSize() * from.size)

    for (i in from.indices)
        this.setAtIndex(elementLayout, i.toLong(), from[i])
}

fun MemorySegment.copyFrom(elementLayout: OfShort, from: ShortArray) {
    require(this.byteSize() >= elementLayout.byteSize() * from.size)

    for (i in from.indices)
        this.setAtIndex(elementLayout, i.toLong(), from[i])
}

fun MemorySegment.copyFrom(elementLayout: OfShort, from: CharArray) {
    require(this.byteSize() >= elementLayout.byteSize() * from.size)

    for (i in from.indices)
        this.setAtIndex(elementLayout, i.toLong(), from[i].code.toShort())
}


fun Arena.allocateNullPtr(): MemorySegment =
    this.allocate(ValueLayout.ADDRESS.byteSize())
fun NativeContext.allocateNullPtr(): MemorySegment =
    this.arena.allocateNullPtr()


fun Arena.allocateInt(value: Int = 0): MemorySegment =
    this.allocate(ValueLayout.JAVA_INT.byteSize()).also {
        it.setAtIndex(ValueLayout.JAVA_INT, 0, value)
    }


var MemorySegment.intValue: Int
    get() = this.getAtIndex(ValueLayout.JAVA_INT, 0)
    set(value) = this.setAtIndex(ValueLayout.JAVA_INT, 0, value)
