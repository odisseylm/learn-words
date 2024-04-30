package com.mvv.win

import java.lang.foreign.*
import java.nio.file.Path
import java.nio.file.Paths


enum class WinModule (val dll: Path) {
    Kernel("Kernel32.dll"),
    User("User32.dll"),
    Gdi("Gdi32.dll"),
    Dwmapi("Dwmapi.dll"),
    UxTheme("UxTheme.dll"),
    ;
    constructor(dllName: String) : this(Paths.get(dllName))
}


fun NativeContext.functionHandle(
    module: WinModule,
    functionName: String,
    returnLayout: MemoryLayout? = null,
    vararg argLayouts: MemoryLayout,
): NativeMethodHandle = functionHandle(
    dllName = module.dll,
    functionName = functionName,
    returnLayout = returnLayout,
    argLayouts = argLayouts,
)
