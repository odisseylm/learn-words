package com.mvv.win

import com.mvv.gui.util.ifNullOrBlank
import java.nio.file.Path


val programFiles: Path = Path.of(
    System.getenv("ProgramFiles")
        .ifNullOrBlank { "C:/Program Files" } )

val windowsDir: Path = Path.of(
    System.getenv("SystemRoot") // or "windir"
        .ifNullOrBlank { "C:/Windows" } )
