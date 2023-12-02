package com.mvv.gui.words

import com.mvv.gui.test.runTests
import com.mvv.gui.test.useAssertJSoftAssertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


private fun SubSequenceFinder.removeMatchedSubSequence(phrase: String): String =
    this.removeMatchedSubSequence(phrase, SubSequenceFinderOptions(true))

class RussianFinderTest {
    private val f = russianOptionalTrailingFinder()

    @Test
    fun removeMatchedTrailing_simple() {
        SoftAssertions().runTests {
            assertThat(f.removeMatchedSubSequence("удержать к-л")).isEqualTo("удержать")
            assertThat(f.removeMatchedSubSequence("удержать тп")).isEqualTo("удержать")
            assertThat(f.removeMatchedSubSequence("столовое серебро, тп")).isEqualTo("столовое серебро,")
        }
    }

    @Test
    fun removeMatchedTrailing_withDotAtTheEnd() {
        useAssertJSoftAssertions {
            assertThat(f.removeMatchedSubSequence("удержать т.п.")).isEqualTo("удержать")
            assertThat(f.removeMatchedSubSequence("столовое серебро, т.п.")).isEqualTo("столовое серебро,")
            //assertThat(f.removeMatchedSubSequence("столовое серебро, тому подобное")).isEqualTo("столовое серебро, тому подобное")

            val notCleanPunctuationAtEndOfSentenceOptions = SubSequenceFinderOptions(false)
            assertThat(f.removeMatchedSubSequence("удержать т.п.", notCleanPunctuationAtEndOfSentenceOptions)).isEqualTo("удержать")
            assertThat(f.removeMatchedSubSequence("столовое серебро, т.п.", notCleanPunctuationAtEndOfSentenceOptions)).isEqualTo("столовое серебро,")

            val cleanPunctuationAtEndOfSentenceOptions = SubSequenceFinderOptions(true)
            assertThat(f.removeMatchedSubSequence("удержать т.п.", cleanPunctuationAtEndOfSentenceOptions)).isEqualTo("удержать")
            assertThat(f.removeMatchedSubSequence("столовое серебро, т.п.", cleanPunctuationAtEndOfSentenceOptions)).isEqualTo("столовое серебро,")
        }
    }

    @Test
    fun removeMatchedTrailing_withOneWordPreposition() {
        useAssertJSoftAssertions {
            assertThat(f.removeMatchedSubSequence("удержаться от ч-л")).isEqualTo("удержаться")
        }
    }

    @Test
    fun removeMatchedTrailing_withMultiWordPreposition() {
        useAssertJSoftAssertions {
            assertThat(f.removeMatchedSubSequence("уплыть в зависимости от ч-л")).isEqualTo("уплыть")
        }
    }

    @Test
    @DisplayName("removeStress")
    fun test_removeStress() {
        assertThat("не́кто".removeStress()).isEqualTo("некто")
    }
}
