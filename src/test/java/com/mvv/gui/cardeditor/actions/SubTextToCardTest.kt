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
    } }

    @Test
    fun isSenseAlmostTheSame_withSpaces() { useAssertJSoftAssertions {
        assertThat(isSenseAlmostTheSame("break", " break ")).isTrue
        assertThat(isSenseAlmostTheSame(" break ", "break")).isTrue
    } }

    @Test
    fun isSenseAlmostTheSame_withCommas() { useAssertJSoftAssertions {
        assertThat(isSenseAlmostTheSame("break", " , break ; ")).isTrue
        assertThat(isSenseAlmostTheSame(" , break ; ", "break")).isTrue
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


    @Test
    @DisplayName("isSenseBetter")
    fun test_isSenseBetter() { useAssertJSoftAssertions {

        assertThat("".isSenseBetter("")).isEqualTo(CompareSenseResult.AlmostSame)

        assertThat("break".isSenseBetter("break")).isEqualTo(CompareSenseResult.AlmostSame)
    } }

    @Test
    fun isSenseBetter_withSpaces() { useAssertJSoftAssertions {
        assertThat("break".isSenseBetter(" break ")).isEqualTo(CompareSenseResult.AlmostSame)
        assertThat(" break ".isSenseBetter("break")).isEqualTo(CompareSenseResult.AlmostSame)
    } }

    @Test
    fun isSenseBetter_withCommas() { useAssertJSoftAssertions {
        assertThat("break".isSenseBetter(" ;, break ; ")).isEqualTo(CompareSenseResult.AlmostSame)
        assertThat(" ,,, break; ".isSenseBetter("break")).isEqualTo(CompareSenseResult.AlmostSame)
    } }

    @Test
    fun isSenseBetter_withSpacesInsideWord() { useAssertJSoftAssertions {
        // Ideally it should be considered as not same, but I have no idea how to do it easy.
        // Let's live with that.
        assertThat("closed".isSenseBetter(" close d ")).isEqualTo(CompareSenseResult.AlmostSame)
    } }

    @Test
    fun isSenseBetter_witIgnoredCase() { useAssertJSoftAssertions {

        assertThat("break".isSenseBetter(" BREAK ")).isEqualTo(CompareSenseResult.Worse)
        assertThat("break".isSenseBetter(" brEAk ")).isEqualTo(CompareSenseResult.Worse)

        assertThat("BREAK".isSenseBetter(" break ")).isEqualTo(CompareSenseResult.Better)
        assertThat("breAK".isSenseBetter(" break ")).isEqualTo(CompareSenseResult.Better)

        assertThat(" break jail ".isSenseBetter("break JAIL")).isEqualTo(CompareSenseResult.Worse)
        assertThat(" break JAIL ".isSenseBetter("break jail")).isEqualTo(CompareSenseResult.Better)
    } }

    @Test
    fun isSenseBetter_withIgnoredNonLetters() { useAssertJSoftAssertions {
        assertThat(" break (jail) ".isSenseBetter("break JAIL")).isEqualTo(CompareSenseResult.Worse)
        assertThat(" break (jail) ".isSenseBetter("break,; JAIL")).isEqualTo(CompareSenseResult.Worse)

        assertThat(" break (JAIL) ".isSenseBetter("break jail ")).isEqualTo(CompareSenseResult.Better)
        assertThat(" break (JAil) ".isSenseBetter("break,; Jail")).isEqualTo(CompareSenseResult.Better)
    } }

    @Test
    fun isSenseBetter_forDifferent() { useAssertJSoftAssertions {
        //assertThat("break".isSenseBetter("")).isEqualTo(CompareSenseResult.Better)
        //assertThat("".isSenseBetter("break")).isEqualTo(CompareSenseResult.Worse)
        assertThat("break".isSenseBetter("")).isEqualTo(CompareSenseResult.Different)
        assertThat("".isSenseBetter("break")).isEqualTo(CompareSenseResult.Different)

        assertThat("break".isSenseBetter("close")).isEqualTo(CompareSenseResult.Different)
        assertThat("close".isSenseBetter("break")).isEqualTo(CompareSenseResult.Different)

        assertThat(" break jail ".isSenseBetter("not break jail")).isEqualTo(CompareSenseResult.Different)
        assertThat(" break jail ".isSenseBetter("not break JAIL")).isEqualTo(CompareSenseResult.Different)

        assertThat(" break jail ".isSenseBetter("not break jail")).isEqualTo(CompareSenseResult.Different)
        assertThat(" break jail ".isSenseBetter("not break JAIL")).isEqualTo(CompareSenseResult.Different)
    } }
}
