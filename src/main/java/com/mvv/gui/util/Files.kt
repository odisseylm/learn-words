package com.mvv.gui.util

import org.apache.commons.io.IOUtils
import java.io.FileInputStream
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.name


fun Path.replaceFilenamesSuffix(oldExt: String, newExt: String): Path {
    val newFilename = this.name.removeSuffix(oldExt) + newExt
    return this.parent.resolve(newFilename)
}


fun useFileExt(filename: String, fileExt: String): String {
    val fileExtFixed = if (fileExt.startsWith('.')) fileExt else ".$fileExt"
    return if (filename.lowercase().endsWith(fileExtFixed)) filename else filename + fileExtFixed
}

fun String.withFileExt(fileExt: String): String = useFileExt(this, fileExt)


val Path.nullIfNotExists: Path? get() = if (this.exists()) this else null


val userHome: Path get() = Path.of(System.getProperty("user.home"))


fun Path.readBytes(bytesCount: UInt): ByteArray =
    FileInputStream(this.toFile()).use {
        val buffer = ByteArray(bytesCount.toInt())
        IOUtils.read(it, buffer)
        buffer
    }
