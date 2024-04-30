@file:Suppress("FunctionName", "unused", "PackageDirectoryMismatch", "NOTHING_TO_INLINE", "SpellCheckingInspection")
package com.mvv.win.winapi.keyboard

import com.mvv.win.*
import com.mvv.win.winapi.*
import com.mvv.win.winapi.locale.LANGID
import com.mvv.win.winapi.locale.LCID
import com.mvv.win.winapi.locale.toLANGID
import java.lang.foreign.*


// Language Identifier Constants and Strings
//   https://learn.microsoft.com/en-us/windows/win32/intl/language-identifier-constants-and-strings
// Windows Language Code Identifier (LCID) Reference
//   https://learn.microsoft.com/en-us/openspecs/windows_protocols/ms-lcid/70feba9f-294e-491e-b6eb-56532684c37f




// #define MAKELANGID(p,s) ((((WORD)(s)) << 10) | (WORD)(p))
//fun MAKELANGID(primaryLang: DWORD, subLang: DWORD): DWORD = ((subLang.toWORD().toDWORD() shl 10) or (primaryLang.toWORD().toDWORD()))
inline fun MAKELANGID(primaryLang: LANGID, subLang: LANGID): LANGID = ((subLang.toDWORD() shl 10) or primaryLang.toDWORD()).toLANGID()
inline fun MAKELANGID(primaryLang: DWORD, subLang: DWORD): LANGID = MAKELANGID(primaryLang.toLANGID(), subLang.toLANGID())

// #define PRIMARYLANGID(lgid) ((WORD)(lgid) & 0x3ff)
inline fun PRIMARYLANGID(lgid: LANGID): LANGID = (lgid.toDWORD() and 0x3ff).toLANGID()
inline fun PRIMARYLANGID(lgid: Int): LANGID = PRIMARYLANGID(lgid.toLANGID())

// #define SUBLANGID(lgid) ((WORD)(lgid) >> 10)
inline fun SUBLANGID(lgid: LANGID): LANGID = (lgid.toDWORD() ushr 10).toLANGID()
inline fun SUBLANGID(lgid: Int): LANGID = SUBLANGID(lgid.toLANGID())


// #define MAKELCID(lgid, srtid)  ((ULONG)((((ULONG)((USHORT)(srtid))) << 16) | ((ULONG)((USHORT)(lgid)))))
inline fun MAKELCID(lgid: LANGID, srtid: Int) = (srtid.toUSHORT().toULONG() shl 16) or lgid.toUSHORT().toULONG()
inline fun MAKELCID(lgid: Int, srtid: Int) = MAKELCID(lgid.toLANGID(), srtid)

// #define MAKESORTLCID(lgid, srtid, ver) ((ULONG)((MAKELCID(lgid, srtid)) | (((ULONG)((USHORT)(ver))) << 20)))
inline fun MAKESORTLCID(lgid: LANGID, srtid: Int, ver: Int) = MAKELCID(lgid, srtid).toULONG() or (ver.toUSHORT().toULONG() shl 20)
inline fun MAKESORTLCID(lgid: Int, srtid: Int, ver: Int) = MAKESORTLCID(lgid.toLANGID(), srtid, ver)

//#define LANGIDFROMLCID(lcid)   ((USHORT)(lcid))
inline fun LANGIDFROMLCID(lcid: LCID): LANGID = lcid.toUSHORT()
//inline fun LANGIDFROMLCID(lcid: Long): LANGID = LANGIDFROMLCID(lcid.toDWORD())

//#define SORTIDFROMLCID(lcid)   ((USHORT)((((ULONG)(lcid)) >> 16) & 0xf))
inline fun SORTIDFROMLCID(lcid: LCID):  Int = ((lcid.toULONG() ushr 16) and 0xf).toUSHORT().toDWORD()
inline fun SORTIDFROMLCID(lcid: Long): Int = SORTIDFROMLCID(lcid.toDWORD())

// #define SORTVERSIONFROMLCID(lcid)  ((USHORT)((((ULONG)(lcid)) >> 20) & 0xf))
inline fun SORTVERSIONFROMLCID(lcid: LCID): Int = ((lcid.toULONG() ushr 20) and 0xf).toUSHORT().toDWORD()
inline fun SORTVERSIONFROMLCID(lcid: Long): Int = SORTVERSIONFROMLCID(lcid.toDWORD())



// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-getkeyboardlayoutnamew
//
fun GetKeyboardLayoutName_Old(): String {

    // User32.dll / User32.lib
    // winuser.h (include Windows.h)
    // BOOL GetKeyboardLayoutNameW( [out] LPWSTR pwszKLID );
    //
    // typedef int BOOL;
    // typedef PVOID HANDLE;
    // #define CALLBACK __stdcall

    val fdGetKeyboardLayoutNameW: FunctionDescriptor = FunctionDescriptor.of(
        ValueLayout_BOOL,
        // args
        ValueLayout.ADDRESS,
    )

    //val fdGetKeyboardLayoutNameW_22: FunctionDescriptor = FunctionDescriptor.of(
    //    // res
    //    C_LONG,
    //    C_INT,
    //    //CallArranger.C_LONG,
    //    // args
    //    C_POINTER
    //)

    // val C_LONG = ValueLayout.JAVA_LONG
    // val C_POINTER = ValueLayout.ADDRESS.withTargetLayout(MemoryLayout.sequenceLayout(Long.MAX_VALUE, ValueLayout.JAVA_BYTE))

    val arena = Arena.ofAuto()

    val functionName = "GetKeyboardLayoutNameW"
    val memorySegment = SymbolLookup
        .libraryLookup("User32.dll", arena)
        .find(functionName)
        .orElseThrow { IllegalStateException("Function [$functionName] is not found.") }

    val fHandle = Linker.nativeLinker().downcallHandle(
        //Linker.systemLookup().lookup("strlen").get(),
        //SymbolLookup.loaderLookup().lookup("multiplyMatrix").orElseThrow(),
        memorySegment,
        fdGetKeyboardLayoutNameW,
        //Linker.Option.
    )

    val charsSegment = arena.allocateWinUtf16String(KL_NAMELENGTH)

    val result = fHandle.invoke(charsSegment) as BOOL
    check(result.asBool) { "$functionName is failed." }

    return charsSegment.winUtf16StringToJavaString()
}


// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-getkeyboardlayoutnamew
//
fun GetKeyboardLayoutName(): String = nativeContext {
    // User32.dll / User32.lib / winuser.h (Windows.h)
    // BOOL GetKeyboardLayoutNameW( [out] LPWSTR pwszKLID );

    val fGetKeyboardLayoutName = functionHandle(WinModule.User, "GetKeyboardLayoutNameW",
        ValueLayout_BOOL, ValueLayout.ADDRESS)

    val keyboardLayoutNameBuffer = allocateWinUtf16String(KL_NAMELENGTH)

    val result = fGetKeyboardLayoutName.call<BOOL>(keyboardLayoutNameBuffer)
    check(result.asBool) { "$fGetKeyboardLayoutName is failed." }

    val keyboardLayoutName = keyboardLayoutNameBuffer.winUtf16StringToJavaString()

    dumpNative(fGetKeyboardLayoutName, keyboardLayoutName)
    keyboardLayoutName
}


// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-getkeyboardlayoutlist
//
fun GetKeyboardLayoutList(): List<HKL> = nativeContext {
    // User32.dll / User32.lib / winuser.h (include Windows.h)
    // int GetKeyboardLayoutList( [in]  int nBuff, [out] HKL *lpList )

    val fGetKeyboardLayoutList = functionHandle(WinModule.User, "GetKeyboardLayoutList",
        ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS)

    val keyboardCount = fGetKeyboardLayoutList.call<Int>(0, allocateNullPtr())
    if (keyboardCount == 0) return@nativeContext emptyList()

    val lpList = arena.allocate(ValueLayout_HKL, keyboardCount.toLong())

    val loadedKeyboardCount = fGetKeyboardLayoutList.call<Int>(keyboardCount, lpList)
    if (loadedKeyboardCount == 0) return@nativeContext emptyList()

    lpList.toList(ValueLayout_HKL, count = loadedKeyboardCount)
            .also { dumpNative(fGetKeyboardLayoutList, it) }
}


// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-getkeyboardlayout
//
// If uiThreadId = 0 then current thread is used (according to MSDN).
//
fun GetKeyboardLayout(uiThreadId: DWORD = 0): HKL = nativeContext {
    // User32.dll / User32.lib / winuser.h (include Windows.h)
    // HKL GetKeyboardLayout( [in] DWORD idThread )
    functionHandle(WinModule.User, "GetKeyboardLayout", ValueLayout_HKL, ValueLayout_DWORD)
        .call<HKL>(uiThreadId)
}
