package com.mvv.gui.cardeditor

import com.mvv.gui.util.useFileExt
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


class FilesTest {

    @Test
    @DisplayName("addFileExt")
    fun test_addFileExt() {
        assertThat(useFileExt("file1", "txt")).isEqualTo("file1.txt")
        assertThat(useFileExt("file1", ".txt")).isEqualTo("file1.txt")

        assertThat(useFileExt("file1.txt", "txt")).isEqualTo("file1.txt")
        assertThat(useFileExt("file1.txt", ".txt")).isEqualTo("file1.txt")
    }
}
