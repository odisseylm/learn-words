package com.mvv.gui.util

import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName


class TextTest {

    @Test
    @DisplayName("wordCount")
    fun test_wordCount() {
        val a = SoftAssertions()

        a.assertThat("word1 word2".wordCount).isEqualTo(2)
        a.assertThat(" word1 word2".wordCount).isEqualTo(2)
        a.assertThat("word1 word2 ".wordCount).isEqualTo(2)
        a.assertThat(" word1 word2 ".wordCount).isEqualTo(2)

        a.assertThat("\nword1\tword2 ".wordCount).isEqualTo(2)
        a.assertThat("word1,word2 ".wordCount).isEqualTo(2)
        a.assertThat("word1;word2 ".wordCount).isEqualTo(2)

        a.assertThat("\nword1\t word2 ".wordCount).isEqualTo(2)
        a.assertThat("word1, word2 ".wordCount).isEqualTo(2)
        a.assertThat("word1; word2 ".wordCount).isEqualTo(2)

        a.assertThat("word1 , word2 ".wordCount).isEqualTo(2)
        a.assertThat("word1 ; word2 ".wordCount).isEqualTo(2)

        a.assertThat("word1 ,word2 ".wordCount).isEqualTo(2)
        a.assertThat("word1 ;word2 ".wordCount).isEqualTo(2)

        a.assertAll()
    }

    @Test
    @DisplayName("splitToWords")
    fun test_splitToWords() {
        val a = SoftAssertions()

        a.assertThat("word1 word2".splitToWords()).containsExactly("word1", "word2")
        a.assertThat(" word1 word2".splitToWords()).containsExactly("word1", "word2")
        a.assertThat("word1 word2 ".splitToWords()).containsExactly("word1", "word2")
        a.assertThat(" word1 word2 ".splitToWords()).containsExactly("word1", "word2")

        a.assertThat("\nword1\tword2 ".splitToWords()).containsExactly("word1", "word2")
        a.assertThat("word1,word2 ".splitToWords()).containsExactly("word1", "word2")
        a.assertThat("word1;word2 ".splitToWords()).containsExactly("word1", "word2")

        a.assertThat("\nword1\t word2 ".splitToWords()).containsExactly("word1", "word2")
        a.assertThat("word1, word2 ".splitToWords()).containsExactly("word1", "word2")
        a.assertThat("word1; word2 ".splitToWords()).containsExactly("word1", "word2")

        a.assertThat("word1 , word2 ".splitToWords()).containsExactly("word1", "word2")
        a.assertThat("word1 ; word2 ".splitToWords()).containsExactly("word1", "word2")

        a.assertThat("word1 ,word2 ".splitToWords()).containsExactly("word1", "word2")
        a.assertThat("word1 ;word2 ".splitToWords()).containsExactly("word1", "word2")

        a.assertAll()
    }
}
