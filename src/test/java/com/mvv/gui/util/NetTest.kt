package com.mvv.gui.util

import com.mvv.gui.dictionary.getProjectDirectory
import com.mvv.gui.test.useAssertJSoftAssertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.io.path.readText


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
    fun downloadUrlText_File() { useAssertJSoftAssertions {
        val file = getProjectDirectory(NetTest::class).resolve("src/test/resources/nuance.txt")
        downloadUrlText(file.toUri())

        val expectedContentTextLength = file.readText(Charsets.UTF_8).length

        assertThat(downloadUrlText(file.toUri())).hasSize(expectedContentTextLength).contains("POST https://tts.api.nuance.com/api/v1/synthesize")
        assertThat(downloadUrlText(file.toUri().toURL())).hasSize(expectedContentTextLength)
        assertThat(downloadUrlText(file.toUri().toString())).hasSize(expectedContentTextLength)
        assertThat(downloadUrlText(file.toUri().toURL().toString())).hasSize(expectedContentTextLength)
    } }

    @Test
    fun downloadUrl_web() {
        val file = getProjectDirectory(NetTest::class).resolve("src/test/resources/door.mp3")
        downloadUrl(file.toUri())

        assertThat(downloadUrl("https://www.google.com")).hasSizeGreaterThan(50)
    }
}
