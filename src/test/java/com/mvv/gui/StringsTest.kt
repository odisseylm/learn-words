package com.mvv.gui

import com.mvv.gui.util.*
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
        assertThat("qwerty.csv".endsWithOneOf(".txt", ".csv")).isTrue()

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


    @Test
    @DisplayName("containsOneOf")
    fun test_containsOneOf() {
        assertThat("qwerty".containsOneOf("qw")).isTrue()
        assertThat("qwerty".containsOneOf("QW")).isFalse()

        assertThat("qwerty".containsOneOf("er")).isTrue()
        assertThat("qwerty".containsOneOf("ER")).isFalse()

        assertThat("qwerty".containsOneOf("ty")).isTrue()
        assertThat("qwerty".containsOneOf("TY")).isFalse()

        assertThat("qwerty".containsOneOf("ab", "qw")).isTrue()
        assertThat("qwerty".containsOneOf("ab", "QW")).isFalse()

        assertThat("qwerty".containsOneOf("ab", "er")).isTrue()
        assertThat("qwerty".containsOneOf("ab", "ER")).isFalse()

        assertThat("qwerty".containsOneOf("ab", "ty")).isTrue()
        assertThat("qwerty".containsOneOf("ab", "TY")).isFalse()
    }


    @Test
    fun removePrefixesRepeatably() {
        assertThat("” Human Resources have teeth".removePrefixesRepeatably("\"", "”", "“", " ", "\n", "\t"))
            .isEqualTo("Human Resources have teeth")
    }


    @Test
    fun removeSuffixesRepeatably() {
        assertThat("Human Resources have teeth.!!??!!".removeSuffixesRepeatably("!", "?", " "))
            .isEqualTo("Human Resources have teeth.")
    }


    @Test
    fun removeCharPrefixesRepeatably() {
        assertThat("” Human Resources have teeth".removeCharPrefixesRepeatably("\"”“ \n\t"))
            .isEqualTo("Human Resources have teeth")
    }


    @Test
    fun removeCharSuffixesRepeatably() {
        assertThat("Human Resources have teeth.!!??!!".removeCharSuffixesRepeatably("!? "))
            .isEqualTo("Human Resources have teeth.")
    }


    @Test
    fun startsWithOneOfChars() {
        assertThat("".startsWithOneOfChars("321")).isFalse()

        assertThat("1word".startsWithOneOfChars("321")).isTrue()
        assertThat("2word".startsWithOneOfChars("321")).isTrue()
        assertThat("3word".startsWithOneOfChars("321")).isTrue()

        assertThat("word1".startsWithOneOfChars("321")).isTrue()
        assertThat("word1".startsWithOneOfChars("321")).isTrue()
        assertThat("word1".startsWithOneOfChars("321")).isTrue()
    }


    @Test
    fun endsWithOneOfChars() {
        assertThat("".endsWithOneOfChars("321")).isFalse()

        assertThat("word1".endsWithOneOfChars("321")).isTrue()
        assertThat("word2".endsWithOneOfChars("321")).isTrue()
        assertThat("word3".endsWithOneOfChars("321")).isTrue()

        assertThat("1word".endsWithOneOfChars("321")).isFalse()
        assertThat("1word".endsWithOneOfChars("321")).isFalse()
        assertThat("1word".endsWithOneOfChars("321")).isFalse()

        assertThat("“Bob!".endsWithOneOfChars("”’»’")).isFalse()
    }


    @Test
    fun indexOfOneOfChars() {
        assertThat("".indexOfOneOfChars("321")).isEqualTo(-1)

        assertThat("word1".indexOfOneOfChars("321")).isEqualTo(4)
        assertThat("word2word".indexOfOneOfChars("321")).isEqualTo(4)
        assertThat("3word".indexOfOneOfChars("321")).isEqualTo(0)

        assertThat("1word".indexOfOneOfChars("321")).isEqualTo(0)
        assertThat("1word".indexOfOneOfChars("321")).isEqualTo(0)
        assertThat("1word".indexOfOneOfChars("321")).isEqualTo(0)
    }


    @Test
    fun indexOfOneOfChars_2() {
        assertThat("".indexOfOneOfChars("321", 999)).isEqualTo(-1)
        assertThat("".indexOfOneOfChars("321", 0)).isEqualTo(-1)

        assertThat("word1".indexOfOneOfChars("321", 0)).isEqualTo(4)
        assertThat("word1".indexOfOneOfChars("321", 1)).isEqualTo(4)
        assertThat("word1".indexOfOneOfChars("321", 2)).isEqualTo(4)
        assertThat("word1".indexOfOneOfChars("321", 3)).isEqualTo(4)
        assertThat("word1".indexOfOneOfChars("321", 4)).isEqualTo(4)
        assertThat("word1word".indexOfOneOfChars("321", 4)).isEqualTo(4)
        assertThat("word1".indexOfOneOfChars("321", 5)).isEqualTo(-1)
        assertThat("1word".indexOfOneOfChars("321", 0)).isEqualTo(0)
    }


    @Test
    fun lastIndexOfOneOfChars() {
        assertThat("".lastIndexOfOneOfChars("321", 999)).isEqualTo(-1)
        assertThat("".lastIndexOfOneOfChars("321", 0)).isEqualTo(-1)

        assertThat("word1word3".lastIndexOfOneOfChars("321", 0)).isEqualTo(9)
    }

}
