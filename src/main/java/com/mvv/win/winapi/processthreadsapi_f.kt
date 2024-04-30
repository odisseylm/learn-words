@file:Suppress("FunctionName", "PackageDirectoryMismatch")
package com.mvv.win.winapi.processthreadsapi

import com.mvv.win.*
import com.mvv.win.winapi.DWORD
import com.mvv.win.winapi.ValueLayout_DWORD


fun GetCurrentThreadId(): DWORD = nativeContext {
    // Kernel32.dll / Kernel32.lib / processthreadsapi.h
    // DWORD GetCurrentThreadId()
    functionHandle(WinModule.Kernel, "GetCurrentThreadId", ValueLayout_DWORD)
        .call()
}
