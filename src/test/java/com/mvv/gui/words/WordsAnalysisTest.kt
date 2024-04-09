package com.mvv.gui.words

import com.mvv.gui.dictionary.Dictionary
import com.mvv.gui.dictionary.DictionaryEntry
import com.mvv.gui.test.useAssertJSoftAssertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


class WordsAnalysisTest {

    @Test
    fun areAllVerbs() { useAssertJSoftAssertions {
        assertThat("""(v.) РАСКАИВАТЬСЯ
                      сокрушаться (сожалеть)
                      """.areAllVerbs).isTrue

        assertThat("""(бот.) ползучий
                      (зоол.) ПРЕСМЫКАЮЩИЙСЯ
                      (rıˈpent) (v.) РАСКАИВАТЬСЯ
                      сокрушаться (сожалеть)
                      """.areAllVerbs).isFalse
    } }

    @Test
    @DisplayName("hasUnpairedBrackets")
    fun test_hasUnpairedBrackets() {
        useAssertJSoftAssertions {
            assertThat("доверять".hasUnpairedBrackets()).isFalse
            assertThat("доверять(ся)".hasUnpairedBrackets()).isFalse
            assertThat("доверять(ся) (полагать(ся))".hasUnpairedBrackets()).isFalse

            assertThat("доверять(ся".hasUnpairedBrackets()).isTrue
            assertThat("доверяться)".hasUnpairedBrackets()).isTrue
            assertThat("доверять(ся)) (полагать(ся))".hasUnpairedBrackets()).isTrue
            assertThat("доверять((ся) (полагать(ся))".hasUnpairedBrackets()).isTrue
            assertThat("доверять(ся) (полагать(ся)))".hasUnpairedBrackets()).isTrue
            assertThat("доверять(ся) ((полагать(ся))".hasUnpairedBrackets()).isTrue
        }
    }

    @Test
    @DisplayName("analyzeWordCards")
    fun test_analyzeWordCards() {
        val card = CardWordEntry("limply", "вяло\nслабо\nбезвольно")
        analyzeWordCards(
            listOf(card),
            WarnAboutMissedBaseWordsMode.WarnWhenSomeBaseWordsMissed,
            listOf(card), EmptyDictionary()
        )

        assertThat(card.statuses).isEmpty()
    }


    private class EmptyDictionary : Dictionary {
        override fun find(word: String): DictionaryEntry = DictionaryEntry(word, null, emptyList())
    }

}
