package com.mvv.gui.words

import com.mvv.gui.dictionary.Dictionary
import com.mvv.gui.dictionary.DictionaryEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


class WordsAnalysisTest {

    @Test
    @DisplayName("analyzeWordCards")
    fun test_analyzeWordCards() {
        val card = CardWordEntry("limply", "вяло\nслабо\nбезвольно")
        analyzeWordCards(
            listOf(card),
            WarnAboutMissedBaseWordsMode.WarnWhenSomeBaseWordsMissed,
            listOf(card), EmptyDictionary()
        )

        assertThat(card.wordCardStatuses).isEmpty()
    }


    private class EmptyDictionary : Dictionary {
        override fun find(word: String): DictionaryEntry = DictionaryEntry(word, null, emptyList())
    }

}
