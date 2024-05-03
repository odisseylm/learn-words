@file:Suppress("SpellCheckingInspection", "unused", "NOTHING_TO_INLINE", "Since15")
package com.mvv.win.winapi

import com.mvv.foreign.*


// See
//  https://learn.microsoft.com/en-us/windows/win32/winprog/windows-data-types
//  https://microsoft.github.io/windows-docs-rs/doc/windows/Win32/UI/WindowsAndMessaging/index.html
//  https://learn.microsoft.com/en-us/windows/win32/seccrypto/return-values-in-c-


typealias BOOL = Int32
val ValueLayout_BOOL: ValueLayout_Int32_Type = ValueLayout_Int32

val BOOL.asBool: Boolean get() = (this != 0)
val Boolean.asBOOL: BOOL get() = if (this) 1 else 0


// corrsponds 'int' (lowercase) in windows heasers
typealias C_INT = Int32
val ValueLayout_C_INT: ValueLayout_Int32_Type = ValueLayout_Int32

typealias INT = Int32
val ValueLayout_INT: ValueLayout_Int32_Type = ValueLayout_Int32

typealias UINT = UInt32 // UInt
val ValueLayout_UINT: ValueLayout_UInt32_Type = ValueLayout_Int32

//typealias WCHAR = Char
//val ValueLayout_WCHAR: ValueLayout_Char16_Type = ValueLayout_Int16

typealias WCHAR = Int16
val ValueLayout_WCHAR: ValueLayout_Int16_Type = ValueLayout_Int16
fun Short.toWCHAR(): WCHAR = this.toUInt16()
fun Int  .toWCHAR(): WCHAR = this.toUInt16()
fun Char .toWCHAR(): WCHAR = this.code.toUInt16()


typealias USHORT = UInt16 // UShort
typealias ValueLayout_USHORT_Type = ValueLayout_Int16_Type
val ValueLayout_USHORT: ValueLayout_USHORT_Type = ValueLayout_Int16

inline fun Byte .toUSHORT(): USHORT = this.toUInt16()
inline fun Short.toUSHORT(): USHORT = this.toUInt16()
inline fun Int  .toUSHORT(): USHORT = this.toUInt16()
inline fun Long .toUSHORT(): USHORT = this.toUInt16()


typealias WORD = USHORT
typealias ValueLayout_WORD_Type = ValueLayout_Int16_Type
val ValueLayout_WORD: ValueLayout_WORD_Type = ValueLayout_UInt16

inline fun Byte .toWORD(): WORD = this.toUSHORT()
inline fun Short.toWORD(): WORD = this.toUSHORT()
inline fun Int  .toWORD(): WORD = this.toUSHORT()
inline fun Long .toWORD(): WORD = this.toUSHORT()


typealias LONG = Int32
typealias ValueLayout_LONG_Type = ValueLayout_Int32_Type
val ValueLayout_LONG: ValueLayout_LONG_Type = ValueLayout_Int32

inline fun Byte .toLONG(): LONG = this.toInt()
inline fun Short.toLONG(): LONG = this.toInt()
inline fun Int  .toLONG(): LONG = this
inline fun Long .toLONG(): LONG = this.toInt()


typealias ULONG = UInt32
typealias ValueLayout_ULONG_Type = ValueLayout_UInt32_Type
val ValueLayout_ULONG: ValueLayout_ULONG_Type = ValueLayout_UInt32

inline fun Byte .toULONG(): ULONG = this.toUInt32()
inline fun Short.toULONG(): ULONG = this.toUInt32()
inline fun Int  .toULONG(): ULONG = this.toUInt32()
inline fun Long .toULONG(): ULONG = this.toUInt32()


typealias DWORD = UInt32
typealias ValueLayout_DWORD_Type = ValueLayout_UInt32_Type
val ValueLayout_DWORD: ValueLayout_DWORD_Type = ValueLayout_UInt32
inline fun Byte .toDWORD(): DWORD = this.toUInt32()
inline fun Short.toDWORD(): DWORD = this.toUInt32()
inline fun Int  .toDWORD(): DWORD = this.toUInt32()
inline fun Long .toDWORD(): DWORD = this.toUInt32()


typealias PTR = Int64
typealias PTRPlain = Int64
typealias ValueLayout_PTR_TYPE = AddressLayout
val ValueLayout_PTR: ValueLayout_PTR_TYPE = ValueLayout.ADDRESS
typealias ValueLayout_PTRPlain_TYPE = ValueLayout_Int64_Type
val ValueLayout_PTRPlain: ValueLayout_PTRPlain_TYPE = ValueLayout_Int64


// typedef LONG HRESULT;
// typedef __LONG32 HRESULT;
//
// See winerror_h.kt and winerror_f.kt regarding processing HRESULT
//
typealias HRESULT = Int32
val ValueLayout_HRESULT: ValueLayout = ValueLayout_Int32
inline fun Int .toHRESULT(): HRESULT = this
inline fun Long.toHRESULT(): HRESULT = this.toInt()


// typedef LONG SCODE;
// typedef SCODE *PSCODE;
typealias SCODE = LONG
inline fun Int .toSCODE(): SCODE = this
inline fun Long.toSCODE(): SCODE = this.toLONG()


typealias HANDLE = Int64
typealias ValueLayout_HANDLE_Type = ValueLayout_Int64_Type
val ValueLayout_HANDLE: ValueLayout_Int64_Type = ValueLayout_Int64


val ValueLayout_PWSTR:   AddressLayout = ValueLayout_PTR.withName("PWSTR")
val ValueLayout_PCWSTR:  AddressLayout = ValueLayout_PTR.withName("PCWSTR")
val ValueLayout_LPWSTR:  AddressLayout = ValueLayout_PTR.withName("LPWSTR")
val ValueLayout_LPCWSTR: AddressLayout = ValueLayout_PTR.withName("LPCWSTR")
