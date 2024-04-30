package com.mvv.win

import java.nio.file.Path


val programFiles: Path = Path.of(
    System.getenv("ProgramFiles")
        .ifBlank { "C:/Program Files" } )

val windowsDir: Path = Path.of(
    System.getenv("SystemRoot") // or "windir"
        .ifBlank { "C:/Windows" } )

fun aa() {

}