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
        a.assertThat(" страшный  человек ".splitTranslation()).containsExactly("страшный человек")

        a.assertAll()
    }

    @Test
    @DisplayName("splitTranslation skipMiddleWordsInBrackets")
    fun test_splitTranslation_skipMiddleWordsInBrackets() {
        assertThat(" страшный  (ужасный)  человек".splitTranslation())
            .containsExactlyInAnyOrder("страшный (ужасный) человек", "страшный человек")

        assertThat(" страшный  (ужасный)  (какой-то текст)  человек ".splitTranslation())
            .containsExactlyInAnyOrder("страшный (ужасный) (какой-то текст) человек", "страшный человек")
    }

    @Test
    @DisplayName("splitTranslation 2 adjectives")
    fun test_splitTranslation_twoAdjectives() {
        assertThat("страшный (ужасный)".splitTranslation())
            .containsExactlyInAnyOrder("страшный (ужасный)", "страшный", "ужасный")
    }

    @Test
    @DisplayName("splitTranslation 3 adjectives")
    fun test_splitTranslation_threeAdjectives() {
        assertThat(" страшный (ужасный жуткий) ".splitTranslation())
            .containsExactlyInAnyOrder("страшный (ужасный жуткий)", "страшный", "ужасный", "жуткий")
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
    @DisplayName("splitTranslation verbCase2_0")
    fun test_splitTranslation_verbCase2_0() {
        assertThat(" доверяться (полагаться опираться) ".splitTranslation())
            .containsExactlyInAnyOrder(
                // full unchanged string
                "доверяться (полагаться опираться)",
                // first
                "доверяться",
                // second
                "полагаться",
                // third
                "опираться",
            )
    }

    @Test
    @DisplayName("splitTranslation verbCase2_1")
    fun test_splitTranslation_verbCase2_1() {
        assertThat(" доверять(ся) (полагаться опираться) ".splitTranslation())
            .containsExactlyInAnyOrder(
                // full unchanged string
                "доверять(ся) (полагаться опираться)",
                // first
                "доверять", "доверять(ся)", "доверяться",
                // second
                "полагаться",
                // third
                "опираться",
            )
    }

    @Test
    @DisplayName("splitTranslation verbCase2_2")
    fun test_splitTranslation_verbCase2_2() {
        assertThat(" доверять(ся) (полагать опирать) ".splitTranslation())
            .containsExactlyInAnyOrder(
                // full unchanged string
                "доверять(ся) (полагать опирать)",
                // first
                "доверять", "доверять(ся)", "доверяться",
                // second
                "полагать",
                // third
                "опирать",
            )
    }

    @Test
    @DisplayName("splitTranslation verbCase3")
    fun test_splitTranslation_verbCase3() {
        assertThat(" доверять(ся) (полагать(ся) опирать(ся)) ".splitTranslation())
            .containsExactlyInAnyOrder(
                // full unchanged string
                "доверять(ся) (полагать(ся) опирать(ся))",
                // first
                "доверять", "доверять(ся)", "доверяться",
                // second
                "полагать", "полагать(ся)", "полагаться",
                // third
                "опирать", "опирать(ся)", "опираться",
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
