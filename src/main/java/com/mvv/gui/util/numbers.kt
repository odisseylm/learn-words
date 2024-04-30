package com.mvv.gui.util


//fun Byte.toUHex(): String = (this.toInt() and 0xFF).toString(16).uppercase()
//fun Short.toUHex(): String = (this.toInt() and 0xFFFF).toString(16).uppercase()
//fun Int.toUHex(): String = (this.toLong() and 0xFFFFFFFF).toString(16).uppercase()
//fun Long.toUHex(): String = java.lang.Long.toUnsignedString(this, 16)

fun Byte .toUHex(): String = this.toUByte() .toString(16).uppercase().padStart(2, '0')
fun Short.toUHex(): String = this.toUShort().toString(16).uppercase().padStart(4, '0')
fun Int  .toUHex(): String = this.toUInt()  .toString(16).uppercase().padStart(8, '0')
fun Long .toUHex(): String = this.toULong() .toString(16).uppercase().padStart(16, '0')
