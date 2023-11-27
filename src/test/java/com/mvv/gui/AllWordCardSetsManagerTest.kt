package com.mvv.gui

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.DisplayName

class AllWordCardSetsManagerTest {

    @Test
    @DisplayName("splitTranslation withoutBrackets")
    fun test_splitTranslation_withoutBrackets() {
        val a = SoftAssertions()

        a.assertThat(" страшный ".splitTranslation()).containsExactly("страшный")
        a.assertThat(" страшный человек ".splitTranslation()).containsExactly("страшный человек")
        a.assertThat(" страшный  человек ".splitTranslation()).containsExactly("страшный  человек")

        a.assertAll()
    }

    @Test
    @DisplayName("splitTranslation skipMiddleWordsInBrackets")
    fun test_splitTranslation_skipMiddleWordsInBrackets() {
        assertThat(" страшный  (ужасный)  человек".splitTranslation())
            .containsExactlyInAnyOrder("страшный  (ужасный)  человек", "страшный человек")

        assertThat(" страшный  (ужасный)  (какой-то текст)  человек ".splitTranslation())
            .containsExactlyInAnyOrder("страшный  (ужасный)  (какой-то текст)  человек", "страшный человек")
    }

    @Test
    @DisplayName("splitTranslation 2 adjectives")
    fun test_splitTranslation_twoAdjectives() {
        assertThat("страшный (ужасный)".splitTranslation())
            .containsExactlyInAnyOrder("страшный (ужасный)", "страшный", "ужасный")
    }

    @Test
    @DisplayName("splitTranslation verbCase1")
    fun test_splitTranslation_verbCase1() {
        assertThat("доверять(ся) (полагать(ся))".splitTranslation())
            .containsExactlyInAnyOrder(
                // full unchanged string
                "доверять(ся) (полагать(ся))",
                // first
                "доверять", "доверять(ся)", "доверяться",
                // second
                "полагать", "полагать(ся)", "полагаться",
            )
    }

    @Test
    @DisplayName("splitByBrackets")
    fun test_splitByBrackets() {
        val a = SoftAssertions()

        run {
            val s = "страшный (ужасный) человек"
            a.assertThat(s.splitByBrackets().map { it.asSubContent() }).containsExactly("страшный ", "(ужасный)", " человек")
            a.assertThat(s.splitByBrackets().map { it.asTextOnly() }).containsExactly("страшный ", "ужасный", " человек")
            a.assertThat(s.splitByBrackets()).containsExactly(
                Part.withoutBrackets(s, 0, 9),
                Part.inBrackets(s, 9, 17),
                Part.withoutBrackets(s, 18, s.length),
            )
        }

        run {
            val s = "доверять(ся) (полагать(ся))"
            a.assertThat(s.splitByBrackets().map { it.asSubContent() }).containsExactly("доверять", "(ся)", " ", "(полагать(ся))")
            a.assertThat(s.splitByBrackets().map { it.asTextOnly() }).containsExactly("доверять", "ся", " ", "полагать(ся)")
            a.assertThat(s.splitByBrackets()).containsExactly(
                Part.withoutBrackets(s, 0, 8),
                Part.inBrackets(s, 8, 11),
                Part.withoutBrackets(s, 12, 13),
                Part.inBrackets(s, 13, 26),
            )
        }

        a.assertAll()
    }
}
