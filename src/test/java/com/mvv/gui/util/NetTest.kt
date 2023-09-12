package com.mvv.gui.util

import com.mvv.gui.dictionary.getProjectDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class NetTest {

    @Test
    fun downloadUrl_File() {
        val file = getProjectDirectory(NetTest::class).resolve("src/test/resources/door.mp3")
        downloadUrl(file.toUri())

        assertThat(downloadUrl(file.toUri())).hasSize(2508)
        assertThat(downloadUrl(file.toUri().toURL())).hasSize(2508)
        assertThat(downloadUrl(file.toUri().toString())).hasSize(2508)
        assertThat(downloadUrl(file.toUri().toURL().toString())).hasSize(2508)
    }

    @Test
    fun downloadUrl_web() {
        val file = getProjectDirectory(NetTest::class).resolve("src/test/resources/door.mp3")
        downloadUrl(file.toUri())

        assertThat(downloadUrl("https://www.google.com")).hasSizeGreaterThan(50)
    }
}
