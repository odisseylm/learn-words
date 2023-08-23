package com.mvv.gui

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test



class StringsTest {

    @Test
    fun test_lastChar() {
        assertThat("qwerty".lastChar).isEqualTo('y')
        assertThatCode { "".lastChar }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Empty string.")
    }

    @Test
    fun test_lastCharOrNull() {
        assertThat("qwerty".lastCharOrNull).isEqualTo('y')
        assertThat("".lastCharOrNull).isNull()
    }

}
