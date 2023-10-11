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

        run {
            val card = "Lower Chamber нижняя палата".parseToCard()

            a.assertThat(card).isNotNull
            a.assertThat(card?.from).isEqualTo("Lower Chamber")
            a.assertThat(card?.to).isEqualTo("нижняя палата")
        }

        run {
            val card = "Star Chamber. (ист.) Звёздная палата".parseToCard()

            a.assertThat(card).isNotNull
            a.assertThat(card?.from).isEqualTo("Star Chamber.")
            a.assertThat(card?.to).isEqualTo("(ист.) Звёздная палата")

            //assertThat(card?.isGoodLearnCardCandidate()).isTrue()
        }

        run {
            val card = "to take the change on smb. _разг. обмануть кого-л.".parseToCard()

            a.assertThat(card).isNotNull
            a.assertThat(card?.from).isEqualTo("to take the change on smb.")
            a.assertThat(card?.to).isEqualTo("_разг. обмануть кого-л.")

            //assertThat(card?.isGoodLearnCardCandidate()).isTrue()
        }

        run {
            val card = " to take the change out of a person(разг.) отомстить кому-л".parseToCard()

            a.assertThat(card).isNotNull
            a.assertThat(card?.from).isEqualTo("to take the change out of a person")
            a.assertThat(card?.to).isEqualTo("(разг.) отомстить кому-л")

            //assertThat(card?.isGoodLearnCardCandidate()).isTrue()
        }

        run {
            val card = "to get no change out of smb. _разг. ничего не добиться от кого-л.".parseToCard()

            a.assertThat(card).isNotNull
            a.assertThat(card?.from).isEqualTo("to get no change out of smb.")
            a.assertThat(card?.to).isEqualTo("_разг. ничего не добиться от кого-л.")

            //assertThat(card?.isGoodLearnCardCandidate()).isTrue()
        }

        run {
            val card = "to change one's mind передумать, изменить решение".parseToCard()

            a.assertThat(card).isNotNull
            a.assertThat(card?.from).isEqualTo("to change one's mind")
            a.assertThat(card?.to).isEqualTo("передумать, изменить решение")
        }

        run {
            val card = "to climb (up) a tree влезать на дерево;".parseToCard()

            a.assertThat(card).isNotNull
            a.assertThat(card?.from).isEqualTo("to climb (up) a tree")
            a.assertThat(card?.to).isEqualTo("влезать на дерево")
        }

        a.assertAll()
    }

    // Lower Chamber нижняя палата
}
