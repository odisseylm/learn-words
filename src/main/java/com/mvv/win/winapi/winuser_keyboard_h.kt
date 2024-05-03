@file:Suppress("FunctionName", "unused", "PackageDirectoryMismatch", "SpellCheckingInspection", "NOTHING_TO_INLINE",
    "Since15"
)
package com.mvv.win.winapi.keyboard

import com.mvv.win.winapi.*


// https://learn.microsoft.com/en-us/windows/win32/intl/language-identifier-constants-and-strings


// An input locale identifier.
// This type is declared in WinDef.h as follows:
// typedef HANDLE HKL;
//
// Actually only 4 (2 lang + 2 others) bytes are used, but it is defined as HANDLE ?!
typealias HKL = HANDLE // 64 bit int
val ValueLayout_HKL: ValueLayout_HANDLE_Type = ValueLayout_HANDLE.withName("HKL")
inline fun Short.toHKL(): HKL = this.toULONG().toUInt64()
inline fun Int  .toHKL(): HKL = this.toUInt64()



const val HKL_NEXT: HKL = 1
const val HKL_PREV: HKL = 0

const val KLF_ACTIVATE = 0x00000001
const val KLF_SUBSTITUTE_OK = 0x00000002
const val KLF_REORDER = 0x00000008
const val KLF_REPLACELANG = 0x00000010
const val KLF_NOTELLSHELL = 0x00000080
const val KLF_SETFORPROCESS = 0x00000100
const val KLF_SHIFTLOCK = 0x00010000
const val KLF_RESET = 0x40000000

const val INPUTLANGCHANGE_SYSCHARSET = 0x0001
const val INPUTLANGCHANGE_FORWARD = 0x0002
const val INPUTLANGCHANGE_BACKWARD = 0x0004


const val KL_NAMELENGTH = 9
