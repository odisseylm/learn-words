package com.mvv.gui.util

import com.mvv.gui.test.useAssertJSoftAssertions
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


    @Test
    @DisplayName("substringStartingFrom")
    fun test_substringStartingFrom() {
        useAssertJSoftAssertions {
            assertThat("".substringStartingFrom("abc", "\n")).isNull()

            assertThat("abcdf".substringStartingFrom("ab", "df")).isEqualTo("abc")
            assertThat("abcdf".substringStartingFrom("ab", "z")).isEqualTo("abcdf")
            assertThat("abcdf".substringStartingFrom("ab", "z", 2)).isEqualTo("ab")
        }
    }


    @Test
    @DisplayName("safeSubstring")
    fun test_safeSubstring() {
        useAssertJSoftAssertions {
            val s = "abcdf"
            val seq: CharSequence = s

            assertThat(s.safeSubstring(1)).isEqualTo("bcdf")
            assertThat(s.safeSubstring(44)).isEqualTo("")
            assertThat(s.safeSubstring(1, 4)).isEqualTo("bcd")
            assertThat(s.safeSubstring(1, 44)).isEqualTo("bcdf")
            assertThat(s.safeSubstring(33, 44)).isEqualTo("")

            assertThat(seq.safeSubSequence(1)).isEqualTo("bcdf")
            assertThat(seq.safeSubSequence(44)).isEqualTo("")
            assertThat(seq.safeSubSequence(1, 4)).isEqualTo("bcd")
            assertThat(seq.safeSubSequence(1, 44)).isEqualTo("bcdf")
            assertThat(seq.safeSubSequence(33, 44)).isEqualTo("")
        }
    }

    @Test
    @DisplayName("replaceSuffix")
    fun test_replaceSuffix() { useAssertJSoftAssertions {
        assertThat("abcdf".replaceSuffix("df", "ef")).isEqualTo("abcef")
        assertThat("abcdf".replaceSuffix("df", "")).isEqualTo("abc")
        assertThat("abcdf".replaceSuffix("ef", "ef")).isEqualTo("abcdf")
        assertThat("".replaceSuffix("z", "ef")).isEqualTo("")

        assertThatCode { assertThat("abcdf".replaceSuffix("", "ef")).isEqualTo("abcdf") }
            .hasMessage("currentSuffix cannot be empty (does not make sense).")
    } }

    @Test
    fun charSequenceComparator() { useAssertJSoftAssertions {
        //val charSequenceComparator = CharSequenceComparator()
        val charSequenceComparator = CharSequenceComparator.INSTANCE

        assertThat(charSequenceComparator.compare("", "")).isEqualTo(0)
        assertThat(charSequenceComparator.compare("aa", "aa")).isEqualTo(0)
        assertThat(charSequenceComparator.compare("aa", "ab")).isEqualTo(-1)
        assertThat(charSequenceComparator.compare("ab", "aa")).isEqualTo(1)
        assertThat(charSequenceComparator.compare("ab", "aa")).isEqualTo(1)
        assertThat(charSequenceComparator.compare("ab", "aac")).isEqualTo(1)
        assertThat(charSequenceComparator.compare("aaa", "aa")).isEqualTo(1)
        assertThat(charSequenceComparator.compare("aa", "aaa")).isEqualTo(-1)
    } }

    @Test
    @DisplayName("containsCharSequence")
    fun test_containsCharSequence() { useAssertJSoftAssertions {

        val s: CharSequence = TestingCharSequence(StringBuilder("aaa"))
        val cs1 = TestingCharSequence(s.subSequence(0, 2))
        val cs2 = TestingCharSequence(s.subSequence(1, 3))

        assertThat(cs1.javaClass).isNotEqualTo(String::class.java)
        assertThat(cs2.javaClass).isNotEqualTo(String::class.java)

        @Suppress("ReplaceCallWithBinaryOperator")
        assertThat(cs1.equals(cs2)).isFalse
        assertThat(cs1 == cs2).isFalse

        assertThat(cs1.hashCode() == cs2.hashCode()).isFalse

        assertThat(cs1.isEqualTo(cs2)).isTrue

        assertThat(cs1 in listOf(cs2)).isFalse
        assertThat(cs2 in listOf(cs1)).isFalse

        assertThat(listOf(cs1).containsCharSequence(cs2)).isTrue
    } }


    // General CharSequence impl without hashCode/equals.
    private class TestingCharSequence (val s: CharSequence) : CharSequence {
        override val length: Int = s.length
        override fun get(index: Int): Char = s[index]
        override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
            TestingCharSequence(s.subSequence(startIndex, endIndex))
    }
}

private fun String.asCharSequence(): CharSequence = this
