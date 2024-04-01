package com.mvv.gui.cardeditor.actions

import com.mvv.gui.test.useAssertJSoftAssertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName


class SubTextToCardTest {

    @Test
    @DisplayName("isSenseAlmostTheSame")
    fun test_isSenseAlmostTheSame() { useAssertJSoftAssertions {

        assertThat(isSenseAlmostTheSame("", "")).isTrue

        assertThat(isSenseAlmostTheSame("break", "break")).isTrue

        assertThat(isSenseAlmostTheSame("break", " break ")).isTrue
    } }

    @Test
    fun isSenseAlmostTheSame_withSpaces() { useAssertJSoftAssertions {
        assertThat(isSenseAlmostTheSame("break", " break ")).isTrue
    } }

    @Test
    fun isSenseAlmostTheSame_withSpacesInsideWord() { useAssertJSoftAssertions {
        // Ideally it should be considered as not same, but I have no idea how to do it easy.
        // Let's live with that.
        assertThat(isSenseAlmostTheSame("closed", " close d ")).isTrue
    } }

    @Test
    fun isSenseAlmostTheSame_witIgnoredCase() { useAssertJSoftAssertions {

        assertThat(isSenseAlmostTheSame("break", " BREAK ")).isTrue

        assertThat(isSenseAlmostTheSame("break", " brEAk ")).isTrue

        assertThat(isSenseAlmostTheSame(" break jail ", "break JAIL")).isTrue
    } }

    @Test
    fun isSenseAlmostTheSame_withIgnoredNonLetters() { useAssertJSoftAssertions {
        assertThat(isSenseAlmostTheSame(" break (jail) ", "break JAIL")).isTrue
        assertThat(isSenseAlmostTheSame(" break (jail) ", "break,; JAIL")).isTrue
    } }

    @Test
    fun isSenseAlmostTheSame_notSame() { useAssertJSoftAssertions {
        assertThat(isSenseAlmostTheSame("break", "")).isFalse
        assertThat(isSenseAlmostTheSame("", "break")).isFalse

        assertThat(isSenseAlmostTheSame("break", "close")).isFalse
        assertThat(isSenseAlmostTheSame(" break jail ", "not break JAIL")).isFalse
    } }
}
