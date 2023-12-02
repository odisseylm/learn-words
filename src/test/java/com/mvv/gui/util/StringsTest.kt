package com.mvv.gui.util

import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


class StringsTest {

    @Test
    @DisplayName("removeRepeatableSpaces")
    fun test_removeRepeatableSpaces() {
        val a = SoftAssertions()

        a.assertThat("".removeRepeatableSpaces()).isEqualTo("")
        a.assertThat("Mama washed a rama.".removeRepeatableSpaces()).isEqualTo("Mama washed a rama.")
        a.assertThat(" Mama washed a rama. ".removeRepeatableSpaces()).isEqualTo(" Mama washed a rama. ")

        a.assertThat(" Mama  washed a rama. ".removeRepeatableSpaces()).isEqualTo(" Mama washed a rama. ")
        a.assertThat(" Mama \n washed \n a rama. ".removeRepeatableSpaces()).isEqualTo(" Mama washed a rama. ")
        a.assertThat(" Mama\n  washed\n a rama. ".removeRepeatableSpaces()).isEqualTo(" Mama\nwashed\na rama. ")
        a.assertThat(" Mama\n \n washed \na rama. ".removeRepeatableSpaces()).isEqualTo(" Mama\nwashed a rama. ")

        a.assertAll()
    }

    @Test
    @DisplayName("removeRepeatableSpaces useSpaceOnly")
    fun test_removeRepeatableSpaces_useSpaceOnly() {
        val a = SoftAssertions()

        a.assertThat("".removeRepeatableSpaces(SpaceCharPolicy.UseSpaceOnly)).isEqualTo("")
        a.assertThat("Mama washed a rama.".removeRepeatableSpaces(SpaceCharPolicy.UseSpaceOnly)).isEqualTo("Mama washed a rama.")
        a.assertThat(" Mama washed a rama. ".removeRepeatableSpaces(SpaceCharPolicy.UseSpaceOnly)).isEqualTo(" Mama washed a rama. ")

        a.assertThat(" Mama  washed a rama. ".removeRepeatableSpaces(SpaceCharPolicy.UseSpaceOnly)).isEqualTo(" Mama washed a rama. ")
        a.assertThat(" Mama\nwashed\na\nrama. ".removeRepeatableSpaces(SpaceCharPolicy.UseSpaceOnly)).isEqualTo(" Mama washed a rama. ")
        a.assertThat(" Mama \n washed \n a rama. ".removeRepeatableSpaces(SpaceCharPolicy.UseSpaceOnly)).isEqualTo(" Mama washed a rama. ")
        a.assertThat(" Mama\n  washed\n a rama. ".removeRepeatableSpaces(SpaceCharPolicy.UseSpaceOnly)).isEqualTo(" Mama washed a rama. ")
        a.assertThat(" Mama\n \n washed \na rama. ".removeRepeatableSpaces(SpaceCharPolicy.UseSpaceOnly)).isEqualTo(" Mama washed a rama. ")

        a.assertAll()
    }

    @Test
    @DisplayName("removeRepeatableSpaces verifyOriginInstanceUsed ifInitialStringOk")
    fun test_removeRepeatableSpaces_verifyOriginInstanceUsed_ifInitialStringOk() {
        val a = SoftAssertions()

        a.assertThat("".removeRepeatableSpaces(SpaceCharPolicy.UseSpaceOnly)).isEqualTo("")

        val okString1 = "Mama washed a rama."
        a.assertThat(okString1.removeRepeatableSpaces(SpaceCharPolicy.UseSpaceOnly)).isSameAs(okString1)

        val okString2 = " Mama washed a rama. "
        a.assertThat(okString2.removeRepeatableSpaces(SpaceCharPolicy.UseSpaceOnly)).isSameAs(okString2)

        val okString3 = " Mama\nwashed a rama. "
        a.assertThat(okString3.removeRepeatableSpaces(SpaceCharPolicy.KeepExistent)).isSameAs(okString3)

        a.assertAll()
    }

    @Test
    @DisplayName("startsWithOneOf")
    fun test_startsWithOneOf() {
        val a = SoftAssertions()

        a.assertThat("abc".startsWithOneOf("ab")).isTrue
        a.assertThat("abc".startsWithOneOf("abc")).isTrue
        a.assertThat("abc".startsWithOneOf("abc 1")).isFalse

        a.assertThat("abc".startsWithOneOf("aB", ignoreCase = true)).isTrue
        a.assertThat("abc".startsWithOneOf("aBc", ignoreCase = true)).isTrue
        a.assertThat("abc".startsWithOneOf("aBc 1", ignoreCase = true)).isFalse

        a.assertThat("abc".startsWithOneOf("aB", ignoreCase = false)).isFalse
        a.assertThat("abc".startsWithOneOf("aBc", ignoreCase = false)).isFalse
        a.assertThat("abc".startsWithOneOf("aBc 1", ignoreCase = false)).isFalse

        // ------------------------------------------------------------------------------

        a.assertThat("abc".startsWithOneOf(listOf("ab"))).isTrue
        a.assertThat("abc".startsWithOneOf(listOf("abc"))).isTrue
        a.assertThat("abc".startsWithOneOf(listOf("abc 1"))).isFalse

        a.assertThat("abc".startsWithOneOf(listOf("aB"), ignoreCase = true)).isTrue
        a.assertThat("abc".startsWithOneOf(listOf("aBc"), ignoreCase = true)).isTrue
        a.assertThat("abc".startsWithOneOf(listOf("aBc 1"), ignoreCase = true)).isFalse

        a.assertThat("abc".startsWithOneOf(listOf("aB"), ignoreCase = false)).isFalse
        a.assertThat("abc".startsWithOneOf(listOf("aBc"), ignoreCase = false)).isFalse
        a.assertThat("abc".startsWithOneOf(listOf("aBc 1"), ignoreCase = false)).isFalse

        a.assertAll()
    }

    @Test
    @DisplayName("removeSuffixCaseInsensitive")
    fun test_removeSuffixCaseInsensitive() {
        val a = SoftAssertions()

        a.assertThat("abcdf".removeSuffixCaseInsensitive("df")).isEqualTo("abc")
        a.assertThat("abcDf".removeSuffixCaseInsensitive("df")).isEqualTo("abc")
        a.assertThat("abcdF".removeSuffixCaseInsensitive("DF")).isEqualTo("abc")

        a.assertThat("abcdf".asCharSequence().removeSuffixCaseInsensitive("df")).isEqualTo("abc")
        a.assertThat("abcDf".asCharSequence().removeSuffixCaseInsensitive("df")).isEqualTo("abc")
        a.assertThat("abcdF".asCharSequence().removeSuffixCaseInsensitive("DF")).isEqualTo("abc")

        a.assertAll()
    }
}

private fun String.asCharSequence(): CharSequence = this
