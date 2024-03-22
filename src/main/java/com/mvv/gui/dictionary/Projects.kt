package com.mvv.gui.dictionary

import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.NotImplementedException
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.net.URL
import java.nio.file.Path
import java.util.*
import java.util.Objects.requireNonNull
import kotlin.reflect.KClass


fun Any.getProjectDirectory() = getProjectDirectory(this.javaClass)

fun getProjectDirectory(thatProjectTopClass: KClass<*>): Path = getProjectDirectory(thatProjectTopClass.java)

fun getProjectDirectory(thatProjectTopClass: Class<*>): Path {

    val classAsResourcePath = '/'.toString() + thatProjectTopClass.getName().replace('.', '/')
    val possibleFiles = listOf(
        "$classAsResourcePath.class",
        classAsResourcePath + "Kt",
        classAsResourcePath + "Kt.class",
        classAsResourcePath
    )
    val classAsResource = possibleFiles.stream()
        .map { name -> thatProjectTopClass.getResource(name) }
        .filter { obj: URL? -> Objects.nonNull(obj) }
        .findAny()
        .orElseThrow { IllegalStateException("Error of finding resource for class " + thatProjectTopClass.getName()) }

    requireNonNull(classAsResource, "Error of getting class " + thatProjectTopClass.getName() + " as resource.")
    if (classAsResource.protocol == "file") {
        val packageSubDirsCount = StringUtils.countMatches(thatProjectTopClass.getName(), '.')
        val levelsToUp = packageSubDirsCount + 2 // 'target' and 'classes'/'test-classes'
        var directory: File = FileUtils.toFile(classAsResource).getParentFile()
        for (i in 0 until levelsToUp) {
            directory = directory.getParentFile()
        }
        return directory.toPath()
    }
    throw NotImplementedException("Support of " + classAsResource.protocol + " is not implemented yet.")
}
