package com.mvv.gui

import com.mvv.gui.util.endsWithOneOf
import com.mvv.gui.util.ifNotBlank
import com.mvv.gui.util.lastChar
import com.mvv.gui.util.lastCharOrNull
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test



class StringsTest {

    @Test
    @DisplayName("lastChar")
    fun test_lastChar() {
        assertThat("qwerty".lastChar).isEqualTo('y')
        assertThatCode { "".lastChar }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Empty string.")
    }

    @Test
    @DisplayName("lastCharOrNull")
    fun test_lastCharOrNull() {
        assertThat("qwerty".lastCharOrNull).isEqualTo('y')
        assertThat("".lastCharOrNull).isNull()
    }

    @Test
    @DisplayName("endsWithOneOf")
    fun test_endsWithOneOf() {
        assertThat("qwerty.txt".endsWithOneOf(listOf(".txt", ".csv"))).isTrue()
        assertThat("qwerty.csv".endsWithOneOf(listOf(".txt", ".csv"))).isTrue()

        assertThat("qwerty.csv".endsWithOneOf(listOf("qwerty"))).isFalse()

        assertThat("qwerty.csv".endsWithOneOf(listOf(".doc"))).isFalse()
    }

    @Test
    @DisplayName("ifNotBlank")
    fun test_ifNotBlank() {
        assertThat("qwerty".ifNotBlank { "alt value" }).isEqualTo("alt value")
        assertThat(" ".ifNotBlank { "alt value" }).isEqualTo(" ")
        assertThat("\t".ifNotBlank { "alt value" }).isEqualTo("\t")
    }

}
