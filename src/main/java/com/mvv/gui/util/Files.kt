package com.mvv.gui.util

import org.apache.commons.io.IOUtils
import java.io.FileInputStream
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.math.min


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


class PathCaseInsensitiveComparator : Comparator<Path> {
    override fun compare(o1: Path, o2: Path): Int = String.CASE_INSENSITIVE_ORDER.compare(o1.toString(), o2.toString())
}


fun predictFileEncoding(filePath: Path): Charset {
    val bytes = filePath.readBytes(min(10U, Files.size(filePath).toUInt()))

    val feByte = 0xFE.toByte()
    val ffByte = 0xFF.toByte()

    return if (bytes.size >= 2 && (
                   (bytes[0] == feByte && bytes[1] == ffByte)
                || (bytes[1] == feByte && bytes[0] == ffByte)))
           Charsets.UTF_16
           else Charsets.UTF_8
}
