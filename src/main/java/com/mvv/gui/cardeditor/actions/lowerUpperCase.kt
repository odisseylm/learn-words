package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.words.CardWordEntry
import com.mvv.gui.words.from
import com.mvv.gui.words.to


fun LearnWordsController.toggleTextSelectionCaseOrLowerCaseRow() = currentWordsList.toggleTextSelectionCaseOrLowerCaseRow()


fun wordCardsToLowerCaseRow(wordCards: Iterable<CardWordEntry>) {
    val it = wordCards.iterator()

    it.forEach {
        it.from = it.from.lowercase()
        it.to   = it.to.lowercase()
    }
}
