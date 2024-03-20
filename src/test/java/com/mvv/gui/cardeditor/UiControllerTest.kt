package com.mvv.gui.cardeditor

import com.mvv.gui.cardeditor.actions.getWordAt
import com.mvv.gui.cardeditor.actions.highlightWords
import com.mvv.gui.cardeditor.actions.parseToCard
import com.mvv.gui.test.useAssertJSoftAssertions
import com.mvv.gui.words.from
import com.mvv.gui.words.to
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

    @Test
    @DisplayName("getWordAt forEmptyString")
    fun test_getWordAt_forEmptyString() { useAssertJSoftAssertions {
        assertThat("".getWordAt(-1)).isEqualTo(null)
        assertThat("".getWordAt(0)).isEqualTo(null)
        assertThat("".getWordAt(1)).isEqualTo(null)
    } }

    @Test
    @DisplayName("getWordAt forBlankString")
    fun test_getWordAt_forBlankString() { useAssertJSoftAssertions {
        assertThat("  ".getWordAt(-1)).isEqualTo(null)
        assertThat("  ".getWordAt(0)).isEqualTo(null)
        assertThat("  ".getWordAt(1)).isEqualTo(null)
        assertThat("  ".getWordAt(2)).isEqualTo(null)
        assertThat("  ".getWordAt(3)).isEqualTo(null)
        assertThat("  ".getWordAt(4)).isEqualTo(null)
    } }

    @Test
    @DisplayName("getWordAt")
    fun test_getWordAt() { useAssertJSoftAssertions {
        assertThat(" cut ".getWordAt(-111)).isEqualTo(null)
        assertThat(" cut ".getWordAt(-2)).isEqualTo(null)
        assertThat(" cut ".getWordAt(-1)).isEqualTo(null)
        assertThat(" cut ".getWordAt(0)).isEqualTo(null)

        assertThat(" cut ".getWordAt(1)).isEqualTo("cut")
        assertThat(" cut ".getWordAt(2)).isEqualTo("cut")
        assertThat(" cut ".getWordAt(3)).isEqualTo("cut")
        assertThat(" cut ".getWordAt(4)).isEqualTo("cut")

        assertThat(" cut ".getWordAt(5)).isEqualTo(null)
        assertThat(" cut ".getWordAt(6)).isEqualTo(null)
        assertThat(" cut ".getWordAt(7)).isEqualTo(null)
    } }

    @Test
    @DisplayName("getWordAt caretPosition at the end of word")
    fun test_getWordAt_caretPositionAtTheEndOfWord() { useAssertJSoftAssertions {
        assertThat("cut".getWordAt(3)).isEqualTo("cut")
        assertThat(" cut ".getWordAt(4)).isEqualTo("cut")
    } }

    @Test
    @DisplayName("getWordAt debug")
    fun test_getWordAt_debug() { useAssertJSoftAssertions {
        assertThat(" cut ".getWordAt(1)).isEqualTo("cut")
    } }

    @Test
    @DisplayName("getWordAt with hyphen")
    fun test_getWordAt_withHyphen() { useAssertJSoftAssertions {
        assertThat(" cut-w ".getWordAt(-1)).isEqualTo(null)
        assertThat(" cut-w ".getWordAt(0)).isEqualTo(null)

        assertThat(" cut-w ".getWordAt(1)).isEqualTo("cut-w")
        assertThat(" cut-w ".getWordAt(2)).isEqualTo("cut-w")
        assertThat(" cut-w ".getWordAt(3)).isEqualTo("cut-w")
        assertThat(" cut-w ".getWordAt(4)).isEqualTo("cut-w")
        assertThat(" cut-w ".getWordAt(5)).isEqualTo("cut-w")
        assertThat(" cut-w ".getWordAt(6)).isEqualTo("cut-w")

        assertThat(" cut-w ".getWordAt(7)).isEqualTo(null)
        assertThat(" cut-w ".getWordAt(8)).isEqualTo(null)
    } }

    @Test
    @DisplayName("getWordAt with digits")
    fun test_getWordAt_withDigits() { useAssertJSoftAssertions {
        assertThat(" 12cut34 ".getWordAt(-1)).isEqualTo(null)
        assertThat(" 12cut34 ".getWordAt(0)).isEqualTo(null)
        assertThat(" 12cut34 ".getWordAt(1)).isEqualTo(null)
        assertThat(" 12cut34 ".getWordAt(2)).isEqualTo(null)

        assertThat(" 12cut34 ".getWordAt(3)).isEqualTo("cut")
        assertThat(" 12cut34 ".getWordAt(4)).isEqualTo("cut")
        assertThat(" 12cut34 ".getWordAt(5)).isEqualTo("cut")
        assertThat(" 12cut34 ".getWordAt(6)).isEqualTo("cut")
        assertThat(" 12cut34 ".getWordAt(7)).isEqualTo(null)
        assertThat(" 12cut34 ".getWordAt(8)).isEqualTo(null)

        assertThat(" 12cut34 ".getWordAt(9)).isEqualTo(null)
        assertThat(" 12cut34 ".getWordAt(10)).isEqualTo(null)
    } }
}
