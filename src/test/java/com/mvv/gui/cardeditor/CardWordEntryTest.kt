package com.mvv.gui.cardeditor

import com.mvv.gui.words.CardWordEntry
import com.mvv.gui.words.cardWordEntry
import com.mvv.gui.words.cardWordEntryComparator
import com.mvv.gui.words.from
import com.mvv.gui.words.to
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
            cardWordEntry {
                from = "c"
                to = ""
            },
        )

        val sorted = cards.sortedWith(cardWordEntryComparator)

        assertThat(sorted.map { it.from })
            .containsExactly("a", "B", "c", "z")
    }

}