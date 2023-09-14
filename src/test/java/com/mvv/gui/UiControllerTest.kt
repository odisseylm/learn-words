package com.mvv.gui

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


class UiControllerTest {

    @Test
    @DisplayName("highlightWords")
    fun test_highlightWords() {

        assertThat(
            "My cat wants to play, but another cat is crazy."
                .highlightWords(" CaT ", "green"))
            .isEqualTo(
                "My " +
                        "<span style=\"color:green; font-weight: bold;\"><strong><b><font color=\"green\">" +
                        "cat" +
                        "</font></b></strong></span> " +
                        "wants to play, but another " +
                        "<span style=\"color:green; font-weight: bold;\"><strong><b><font color=\"green\">" +
                        "cat" +
                        "</font></b></strong></span>" +
                        " is crazy.")
    }
}
