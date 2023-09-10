package com.mvv.gui.words

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class WordActionsTest {

    @Test
    fun extractWordsFromText() {
        val cards = extractWordsFromText_New("John has come in the room.", emptySet())

        assertThat(cards.map { it.from }).containsExactly(
            "john", "has",
            "come",
            "come in", // with preposition
            "in", "the", "room",
        )
    }

    @Test
    fun extractWordsFromText_withMultiWordsPreposition() {

        val cards = extractWordsFromText_New("Businesses have costs to cover in addition to your salary.", emptySet())
        assertThat(cards.map { it.from }).containsExactly(
            "businesses", "have",
            "costs",
            "costs to", // with preposition
            "to",
            "cover",
            "cover in addition to", // with preposition
            "in", "addition",
            "addition to", // would be nice to exclude it in some way?!
            "to", "your", "salary",
        )
    }
}
