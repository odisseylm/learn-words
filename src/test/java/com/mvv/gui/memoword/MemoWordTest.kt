package com.mvv.gui.memoword

import com.mvv.gui.test.useAssertJSoftAssertions
import org.junit.jupiter.api.Test

import java.time.ZonedDateTime


class MemoWordTest {

    @Test
    fun parseMemoDate() { useAssertJSoftAssertions {
        val instant = """\/Date(1711155077894)\/""".parseMemoDate()
        assertThat(instant.toString())
            .isEqualTo("2024-03-23T00:51:17.894Z")

        assertThat("""/Date(1711155077894)/""".parseMemoDate().toString())
            .isEqualTo("2024-03-23T00:51:17.894Z")

        assertThat("Date(1711155077894)".parseMemoDate().toString())
            .isEqualTo("2024-03-23T00:51:17.894Z")
    } }

    @Test
    fun toMemoDate() { useAssertJSoftAssertions {
        val instant = ZonedDateTime.parse("2024-03-23T00:51:17Z") // """\/Date(1711155077894)\/""".parseMemoDate()
        assertThat(instant.toMemoDate()).isEqualTo("/Date(1711155077000)/")
    } }
}
