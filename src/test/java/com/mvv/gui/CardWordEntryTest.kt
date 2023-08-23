package com.mvv.gui

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


class CardWordEntryTest {

    @Test
    @DisplayName("cardWordEntryComparator")
    fun test_cardWordEntryComparator() {

        val cards = listOf(
            CardWordEntry("a", ""),
            CardWordEntry("z", ""),
            CardWordEntry("B", ""),
            CardWordEntry("c", ""),
        )

        val sorted = cards.sortedWith(cardWordEntryComparator)

        assertThat(sorted.map { it.from })
            .containsExactly("a", "B", "c", "z")
    }

}