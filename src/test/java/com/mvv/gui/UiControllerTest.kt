package com.mvv.gui

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
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


    @Test
    @DisplayName("parseToCard")
    fun test_parseToCard() {

        val a = SoftAssertions()

        run {
            val card = " ".parseToCard()
            a.assertThat(card).isNull()
        }

        run {
            val card = "to ring the rounds \t \n ".parseToCard()
            a.assertThat(card).isNull()
        }

        run {
            val card = "to ring the rounds \t \n опередить, обогнать".parseToCard()

            a.assertThat(card).isNotNull
            a.assertThat(card?.from).isEqualTo("to ring the rounds")
            a.assertThat(card?.to).isEqualTo("опередить, обогнать")
        }

        run {
            val card = "to ring the rounds \t \n (разг.) опередить, обогнать".parseToCard()

            a.assertThat(card).isNotNull
            a.assertThat(card?.from).isEqualTo("to ring the rounds")
            a.assertThat(card?.to).isEqualTo("(разг.) опередить, обогнать")
        }

        a.assertAll()
    }
}
