@file:Suppress("NOTHING_TO_INLINE", "Since15", "unused")
package com.mvv.win.winapi

import com.mvv.foreign.*


typealias Int8  = Byte
typealias Int16 = Short
typealias Int32 = Int
typealias Int64 = Long

typealias UInt8  = Int8
typealias UInt16 = Int16
typealias UInt32 = Int32
typealias UInt64 = Int64


typealias ValueLayout_Byte_Type = OfByte

typealias ValueLayout_Int8_Type  = OfByte
typealias ValueLayout_Int16_Type = OfShort
typealias ValueLayout_Int32_Type = OfInt
typealias ValueLayout_Int64_Type = OfLong

typealias ValueLayout_Float32_Type  = OfFloat
typealias ValueLayout_Double64_Type = OfDouble

typealias ValueLayout_UInt8_Type  = ValueLayout_Int8_Type
typealias ValueLayout_UInt16_Type = ValueLayout_Int16_Type
typealias ValueLayout_UInt32_Type = ValueLayout_Int32_Type
typealias ValueLayout_UInt64_Type = ValueLayout_Int64_Type


val ValueLayout_Byte:  ValueLayout_Byte_Type  = ValueLayout.JAVA_BYTE

val ValueLayout_Int8:  ValueLayout_Int8_Type  = ValueLayout.JAVA_BYTE
val ValueLayout_Int16: ValueLayout_Int16_Type = ValueLayout.JAVA_SHORT
val ValueLayout_Int32: ValueLayout_Int32_Type = ValueLayout.JAVA_INT
val ValueLayout_Int64: ValueLayout_Int64_Type = ValueLayout.JAVA_LONG

val ValueLayout_UInt8:  ValueLayout_UInt8_Type  = ValueLayout_Int8
val ValueLayout_UInt16: ValueLayout_UInt16_Type = ValueLayout_Int16
val ValueLayout_UInt32: ValueLayout_UInt32_Type = ValueLayout_Int32
val ValueLayout_UInt64: ValueLayout_UInt64_Type = ValueLayout_Int64

val ValueLayout_Float32:  ValueLayout_Float32_Type  = ValueLayout.JAVA_FLOAT
val ValueLayout_Double64: ValueLayout_Double64_Type = ValueLayout.JAVA_DOUBLE


inline fun Byte .toUInt64(): UInt64 = this.toLong() and 0xFF
inline fun Short.toUInt64(): UInt64 = this.toLong() and 0xFFFF
inline fun Int  .toUInt64(): UInt64 = this.toLong() and 0xFFFFFFFF
inline fun Long .toUInt64(): UInt64 = this

inline fun Byte .toUInt32(): UInt32 = this.toInt() and 0xFF
inline fun Short.toUInt32(): UInt32 = this.toInt() and 0xFFFF
inline fun Int  .toUInt32(): UInt32 = this
inline fun Long .toUInt32(): UInt32 = (this and 0xFFFFFFFFL).toInt()

inline fun Byte .toUInt16(): UInt16 = (this.toShort().toInt() and 0xFF).toShort()
inline fun Short.toUInt16(): UInt16 = this
inline fun Int  .toUInt16(): UInt16 = (this and 0xFFFF).toShort()
inline fun Long .toUInt16(): UInt16 = (this and 0xFFFF).toShort()

inline fun Byte .toUInt8():  UInt8  = this
inline fun Short.toUInt8():  UInt8  = (this.toInt() and 0xFF).toByte()
inline fun Int  .toUInt8():  UInt8  = (this and 0xFF).toByte()
inline fun Long .toUInt8():  UInt8  = (this and 0xFF).toByte()