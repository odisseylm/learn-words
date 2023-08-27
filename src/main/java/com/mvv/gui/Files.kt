package com.mvv.gui

import java.nio.file.Path
import kotlin.io.path.name


fun Path.replaceFilenamesSuffix(oldExt: String, newExt: String): Path {
    val newFilename = this.name.removeSuffix(oldExt) + newExt
    return this.parent.resolve(newFilename)
}


fun useFileExt(filename: String, fileExt: String): String {
    val fileExtFixed = if (fileExt.startsWith('.')) fileExt else ".$fileExt"
    return if (filename.lowercase().endsWith(fileExtFixed)) filename else filename + fileExtFixed
}
