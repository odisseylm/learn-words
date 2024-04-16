package com.mvv.gui.words

import com.mvv.gui.test.useAssertJSoftAssertions
import org.junit.jupiter.api.Test



class WordCardUtils {

    @Test
    fun guessPartOfSpeech() { useAssertJSoftAssertions {
        assertThat(guessPartOfSpeech(cardWordEntry {
            from = "good fellowship"
            to = "чувство товарищества"
        })).isEqualTo(PartOfSpeech.Phrase)
    } }
}
