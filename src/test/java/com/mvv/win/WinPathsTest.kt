package com.mvv.win

import com.mvv.gui.test.useAssertJSoftAssertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS
import java.nio.file.Path


class WinPathsTest {

    @Test
    @EnabledOnOs(OS.WINDOWS)
    fun `programFiles on Windows`() { useAssertJSoftAssertions {
       assertThat(programFiles)
           .isDirectory
           .isIn(
               Path.of("A:/Program Files"),
               Path.of("B:/Program Files"),
               Path.of("C:/Program Files"),
               Path.of("D:/Program Files"),
               Path.of("E:/Program Files"),
               Path.of("F:/Program Files"),
           )
    } }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    fun `windowsDir on Windows`() { useAssertJSoftAssertions {
        assertThat(windowsDir)
            .isDirectory
            .isIn(
                Path.of("A:/Windows"),
                Path.of("B:/Windows"),
                Path.of("C:/Windows"),
                Path.of("D:/Windows"),
                Path.of("E:/Windows"),
                Path.of("F:/Windows"),
            )
    } }

    @Test
    @EnabledOnOs(OS.LINUX)
    fun `programFiles on Unix`() { useAssertJSoftAssertions {
       assertThat(programFiles)
           .isIn(Path.of("C:/Program Files")) // default value
    } }

    @Test
    @EnabledOnOs(OS.LINUX)
    fun `windowsDir on Unix`() { useAssertJSoftAssertions {
        assertThat(windowsDir)
            .isIn(Path.of("C:/Windows")) // default value
    } }
}
