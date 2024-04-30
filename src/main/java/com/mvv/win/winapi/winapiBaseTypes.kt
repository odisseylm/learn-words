@file:Suppress("SpellCheckingInspection", "unused", "NOTHING_TO_INLINE")
package com.mvv.win.winapi

import java.lang.foreign.*
import kotlin.experimental.and


// See
//  https://learn.microsoft.com/en-us/windows/win32/winprog/windows-data-types
//  https://microsoft.github.io/windows-docs-rs/doc/windows/Win32/UI/WindowsAndMessaging/index.html
//  https://learn.microsoft.com/en-us/windows/win32/seccrypto/return-values-in-c-


inline fun Byte .toUInt64(): Long = this.toLong() and 0xFF
inline fun Short.toUInt64(): Long = this.toLong() and 0xFFFF
inline fun Int  .toUInt64(): Long = this.toLong() and 0xFFFFFFFF
inline fun Long .toUInt64(): Long = this


typealias BOOL = Int
val ValueLayout_BOOL: ValueLayout.OfInt = ValueLayout.JAVA_INT
val BOOL.asBool: Boolean get() = (this != 0)
val Boolean.asBOOL: BOOL get() = if (this) 1 else 0

// corrsponds 'int' (lowercase) in windows heasers
typealias C_INT = Int
val ValueLayout_C_INT: ValueLayout.OfInt = ValueLayout.JAVA_INT

typealias INT = Int
val ValueLayout_INT: ValueLayout.OfInt = ValueLayout.JAVA_INT

typealias UINT = Int // UInt
val ValueLayout_UINT: ValueLayout.OfInt = ValueLayout.JAVA_INT

typealias WCHAR = Char
val ValueLayout_WCHAR: ValueLayout.OfChar = ValueLayout.JAVA_CHAR

//typealias ULONG = Int // UInt
//val ValueLayout_ULONG: ValueLayout.OfInt = ValueLayout.JAVA_INT


typealias USHORT = Short // UShort
val ValueLayout_USHORT: ValueLayout.OfShort = ValueLayout.JAVA_SHORT
inline fun Byte .toUSHORT(): WORD = (this.toShort() and 0x00FF.toShort())
inline fun Short.toUSHORT(): WORD = this
inline fun Int  .toUSHORT(): WORD = (this and 0x0000FFFF).toShort()
inline fun Long .toUSHORT(): WORD = (this and 0x0000FFFFL).toShort()


typealias WORD = USHORT
val ValueLayout_WORD: ValueLayout.OfShort = ValueLayout_USHORT
inline fun Byte .toWORD(): WORD = this.toUSHORT()
inline fun Short.toWORD(): WORD = this.toUSHORT()
inline fun Int  .toWORD(): WORD = this.toUSHORT()
inline fun Long .toWORD(): WORD = this.toUSHORT()


typealias LONG = Int // 32 bits
val ValueLayout_LONG: ValueLayout.OfInt = ValueLayout.JAVA_INT
inline fun Byte .toLONG(): DWORD = this.toInt()
inline fun Short.toLONG(): DWORD = this.toInt()
inline fun Int  .toLONG(): DWORD = this
inline fun Long .toLONG(): DWORD = this.toInt()


typealias ULONG = Int // UInt // 32 bits
val ValueLayout_ULONG: ValueLayout.OfInt = ValueLayout.JAVA_INT
inline fun Byte .toULONG(): DWORD = (this.toInt() and 0x00FF)
inline fun Short.toULONG(): DWORD = (this.toInt() and 0x0000FFFF)
inline fun Int  .toULONG(): DWORD = this
inline fun Long .toULONG(): DWORD = (this and 0xFFFFFFFFL).toInt()


typealias DWORD = Int // UInt
val ValueLayout_DWORD: ValueLayout.OfInt = ValueLayout.JAVA_INT
inline fun Byte .toDWORD(): DWORD = this.toULONG()
inline fun Short.toDWORD(): DWORD = this.toULONG()
inline fun Int  .toDWORD(): DWORD = this.toULONG()
inline fun Long .toDWORD(): DWORD = this.toULONG()


typealias PTR = Long
val ValueLayout_PTR: AddressLayout = ValueLayout.ADDRESS
val ValueLayout_PTRPlain: ValueLayout.OfLong = ValueLayout.JAVA_LONG


// typedef LONG HRESULT;
// typedef __LONG32 HRESULT;
//
// See winerror_h.kt and winerror_f.kt regarding processing HRESULT
//
typealias HRESULT = Int
val ValueLayout_HRESULT: ValueLayout = ValueLayout.JAVA_INT
inline fun Int .toHRESULT(): HRESULT = this
inline fun Long.toHRESULT(): HRESULT = this.toInt()


// typedef LONG SCODE;
// typedef SCODE *PSCODE;
typealias SCODE = LONG
inline fun Int .toSCODE(): SCODE = this
inline fun Long.toSCODE(): SCODE = this.toLONG()


typealias HANDLE = Long
val ValueLayout_HANDLE: ValueLayout.OfLong = ValueLayout.JAVA_LONG


//typealias LPCWSTR ???
val ValueLayout_PWSTR: AddressLayout = ValueLayout.ADDRESS.withName("PWSTR")
val ValueLayout_PCWSTR: AddressLayout = ValueLayout.ADDRESS.withName("PCWSTR")
val ValueLayout_LPWSTR: AddressLayout = ValueLayout.ADDRESS.withName("LPWSTR")
val ValueLayout_LPCWSTR: AddressLayout = ValueLayout.ADDRESS.withName("LPCWSTR")
