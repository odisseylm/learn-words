@file:Suppress("FunctionName", "SpellCheckingInspection", "NOTHING_TO_INLINE", "PackageDirectoryMismatch", "unused")
package com.mvv.win.winapi.error

import com.mvv.win.*
import com.mvv.win.winapi.*
import com.mvv.win.winapi.keyboard.MAKELANGID
import com.mvv.win.winapi.locale.LANG_NEUTRAL
import com.mvv.win.winapi.locale.SUBLANG_DEFAULT


// #define SUCCEEDED(hr) ((HRESULT)(hr) >= 0)
// #define HR_SUCCEEDED(_hr) SUCCEEDED((SCODE)(_hr))
inline fun SUCCEEDED(hr: HRESULT): Boolean = hr.toHRESULT() >= 0
inline fun HR_SUCCEEDED(hr: HRESULT): Boolean = SUCCEEDED(hr.toSCODE())

// #define FAILED(hr) ((HRESULT)(hr) < 0)
// #define HR_FAILED(_hr) FAILED((SCODE)(_hr))
inline fun FAILED(hResult: HRESULT): Boolean = hResult.toHRESULT() < 0
inline fun HR_FAILED(hResult: HRESULT): Boolean = FAILED(hResult.toSCODE())

inline val HRESULT.isHRSuccess: Boolean get() = HR_SUCCEEDED(this) // this in 0x0..0x7FFFFFFF
inline val HRESULT.isHRFailure: Boolean get() = HR_FAILED(this)

// #define HRESULT_CODE(hr) ((hr) & 0xFFFF)
inline fun HRESULT_CODE(hr: Int) : Int = ((hr) and 0xFFFF)
inline fun HRESULT_CODE(hr: Long): Int = ((hr) and 0xFFFF).toInt()


// #define SCODE_CODE(sc) ((sc) & 0xFFFF)
inline fun SCODE_CODE(sc: Int) : Int = ((sc) and 0xFFFF)
inline fun SCODE_CODE(sc: Long): Int = ((sc) and 0xFFFF).toInt()


// #define IS_ERROR(Status) ((unsigned __LONG32)(Status) >> 31==SEVERITY_ERROR)
//
inline fun HR_IS_ERROR(status: Long): Boolean =
    //((unsigned __LONG32)(Status) >> 31==SEVERITY_ERROR)
    ((status.toDWORD() ushr 31) == SEVERITY_ERROR)
inline fun HR_IS_ERROR(status: Int): Boolean =
    //((unsigned __LONG32)(Status) >> 31==SEVERITY_ERROR)
    ((status.toDWORD() ushr 31) == SEVERITY_ERROR)


// #define MAKE_HRESULT(sev,fac,code) ((HRESULT) (((unsigned __LONG32)(sev)<<31) | ((unsigned __LONG32)(fac)<<16) | ((unsigned __LONG32)(code))))
// #define MAKE_HRESULT(sev,fac,code) ((HRESULT) (((unsigned long)    (sev)<<31) | ((unsigned long)    (fac)<<16) | ((unsigned long)    (code))))
inline fun MAKE_HRESULT(sev: Int, facility: Int, code: Int): HRESULT = (((sev.toDWORD()) shl 31) or ((facility.toDWORD()) shl 16) or ((code.toDWORD()))).toHRESULT()
//fun MAKE_HRESULT(sev: Long, fac: Long, code: Long): HRESULT = (((sev.toDWORD()) shl 31) or ((fac.toDWORD()) shl 16) or ((code.toDWORD()))).toHRESULT()
inline fun MAKE_HRESULT(sev: Long, fac: Long, code: Long): HRESULT = MAKE_HRESULT(sev.toDWORD(), fac.toDWORD(), code.toDWORD())


// #define __HRESULT_FROM_WIN32(x) ((HRESULT)(x) <= 0 ? ((HRESULT)(x)) : ((HRESULT) (((x) & 0x0000FFFF) | (FACILITY_WIN32 << 16) | 0x80000000)))
// __CRT_INLINE HRESULT HRESULT_FROM_WIN32(__LONG32 x) { return x <= 0 ? (HRESULT)x : (HRESULT) (((x) & 0x0000FFFF) | (FACILITY_WIN32 << 16) | 0x80000000);}
//
inline fun HRESULT_FROM_WIN32(x: Int): HRESULT =
    if (x <= 0) x.toHRESULT() else ((x and 0x0000FFFF) or (FACILITY_WIN32 shl 16) or 0x80000000L.toInt()).toHRESULT()


// #define HRESULT_FACILITY(hr) (((hr) >> 16) & 0x1fff)
inline fun HRESULT_FACILITY(hr: HRESULT): Int = (((hr) ushr 16) and 0x1fff)

// #define SCODE_FACILITY(sc) (((sc) >> 16) & 0x1fff)
inline fun SCODE_FACILITY(sc: Int) : Int = (((sc) ushr 16) and 0x1fff)
inline fun SCODE_FACILITY(sc: Long): Int = (((sc) ushr 16) and 0x1fff).toInt()


// #define HRESULT_SEVERITY(hr) (((hr) >> 31) & 0x1)
// #define SCODE_SEVERITY(sc) (((sc) >> 31) & 0x1)
// #define MAKE_HRESULT(sev,fac,code) ((HRESULT) (((unsigned __LONG32)(sev)<<31) | ((unsigned __LONG32)(fac)<<16) | ((unsigned __LONG32)(code))))
// #define MAKE_SCODE(sev,fac,code) ((SCODE) (((unsigned __LONG32)(sev)<<31) | ((unsigned __LONG32)(fac)<<16) | ((unsigned __LONG32)(code))))
// #define GetScode(hr) ((SCODE) (hr))
// #define ResultFromScode(sc) ((HRESULT) (sc))
// #define PropagateResult(hrPrevious,scBase) ((HRESULT) scBase)
// #if defined (RC_INVOKED) || defined (__WIDL__)

// #define _HRESULT_TYPEDEF_(_sc) _sc
// #define _HRESULT_TYPEDEF_(_sc) ((HRESULT)_sc)
//
// #define MakeResult(_s) ResultFromScode(_s)

// #define ASN1_SUCCEEDED(ret) (((int) (ret)) >= 0)
// #define ASN1_FAILED(ret) (((int) (ret)) < 0)


// https://learn.microsoft.com/en-us/windows/win32/api/errhandlingapi/nf-errhandlingapi-getlasterror
//
fun GetLastError(): DWORD = nativeContext {
    // Kernel32.dll/ Kernel32.lib / errhandlingapi.h
    // DWORD WINAPI GetLastError (VOID);
    functionHandle(WinModule.Kernel, "GetLastError", ValueLayout_DWORD)
        .call<DWORD>()
}


// https://learn.microsoft.com/en-us/windows/win32/api/errhandlingapi/nf-errhandlingapi-setlasterror
//
fun SetLastError(dwErrCode: DWORD): Unit = nativeContext {
    // Kernel32.dll/ Kernel32.lib / endpointvolume.idl
    // void SetLastError( [in] DWORD dwErrCode )
    functionHandle(WinModule.Kernel, "SetLastError", returnLayout = null, ValueLayout_DWORD)
        .call<Any?>(dwErrCode)
}


private const val FORMAT_MESSAGE_IGNORE_INSERTS  = 0x00000200
private const val FORMAT_MESSAGE_FROM_STRING     = 0x00000400
private const val FORMAT_MESSAGE_FROM_HMODULE    = 0x00000800
private const val FORMAT_MESSAGE_FROM_SYSTEM     = 0x00001000
private const val FORMAT_MESSAGE_ARGUMENT_ARRAY  = 0x00002000
private const val FORMAT_MESSAGE_MAX_WIDTH_MASK  = 0x000000ff
private const val FORMAT_MESSAGE_ALLOCATE_BUFFER = 0x00000100


// Returns the last Win32 error, in string format. Returns an empty string if there is no error.
//
// Ported form C++ code https://stackoverflow.com/questions/1387064/how-to-get-the-error-message-from-the-error-code-returned-by-getlasterror
//
fun GetLastErrorAsString(errorCode: Int? = null): String = nativeContext{
    //Get the error message ID, if any.
    //val errorMessageID = GetLastError()
    //if(errorMessageID == 0)
    //    return "" // No error message has been recorded
    //
    //LPSTR messageBuffer = nullptr;
    //
    ////Ask Win32 to give us the string version of that message ID.
    ////The parameters we pass in, tell Win32 to create the buffer that holds the message for us (because we don't yet know how long the message string will be).
    //size_t size = FormatMessageW(FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS,
    //NULL, errorMessageID, MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), (LPSTR)&messageBuffer, 0, NULL);
    //
    ////Copy the error message into a std::string.
    //std::string message(messageBuffer, size);
    //
    ////Free the Win32's string's buffer.
    //LocalFree(messageBuffer);
    //
    //return message;

    // https://learn.microsoft.com/en-us/windows/win32/api/winbase/nf-winbase-formatmessagew
    //
    // Kernel32.dll / Kernel32.lib / winbase.h
    // DWORD FormatMessageW (
    //  [in]           DWORD   dwFlags,
    //  [in, optional] LPCVOID lpSource,
    //  [in]           DWORD   dwMessageId,
    //  [in]           DWORD   dwLanguageId,
    //  [out]          LPWSTR  lpBuffer,
    //  [in]           DWORD   nSize,
    //  [in, optional] va_list *Arguments
    // )

    val errorMessageID = errorCode ?: GetLastError()
    val bufSize = 256
    val buffer = allocateWinUtf16String(256)
    val langId = MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT)

    val copiedCharCount = functionHandle(
        WinModule.Kernel, "FormatMessageW", ValueLayout_DWORD,
        ValueLayout_DWORD, ValueLayout_PTR,   // flags + source
        ValueLayout_DWORD, ValueLayout_DWORD, // msgId + lang/locale
        ValueLayout_PTR, ValueLayout_DWORD,   // buffer
        // var args
    )
        .call<DWORD>(
            FORMAT_MESSAGE_ALLOCATE_BUFFER or FORMAT_MESSAGE_FROM_SYSTEM or FORMAT_MESSAGE_IGNORE_INSERTS,
            allocateNullPtr(),
            errorMessageID,
            langId,
            buffer, bufSize
        )

    buffer.winUtf16StringToJavaString(copiedCharCount)
}


// https://learn.microsoft.com/en-us/windows/win32/api/errhandlingapi/nf-errhandlingapi-setlasterror
//
// void SetLastError( [in] DWORD dwErrCode )
//


// https://learn.microsoft.com/en-us/windows/win32/api/errhandlingapi/nf-errhandlingapi-geterrormode
//
// UINT GetErrorMode();
//
// SEM_FAILCRITICALERRORS, SEM_NOGPFAULTERRORBOX, SEM_NOOPENFILEERRORBOX
//
