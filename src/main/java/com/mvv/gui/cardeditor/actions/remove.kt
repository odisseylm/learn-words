package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.javafx.runWithScrollKeeping
import com.mvv.gui.javafx.showInfoAlert
import com.mvv.gui.words.CardWordEntry
import com.mvv.gui.words.from
import com.mvv.gui.words.loadWords
import com.mvv.gui.words.removeWordsFromOtherSetsFromCurrentWords



fun LearnWordsController.removeSelected() {
    val toRemoveSafeCopy = currentWordsSelection.selectedItems.toList()
    currentWordsSelection.clearSelection()
    currentWordsList.runWithScrollKeeping { removeCards(toRemoveSafeCopy) }
}


fun LearnWordsController.removeCards(toRemove: List<CardWordEntry>) {
    currentWords.removeAll(toRemove)
    removeChangeCardListener(toRemove)
}


fun LearnWordsController.removeWordsOfOtherSet() {
    val file = showOpenDialog(extensions = listOf("*.csv")) ?: return

    if (file == currentWordsFile) {
        showInfoAlert(pane, "You cannot remove themself :-)")
        return
    }

    val words: List<String> = loadWords(file.toPath())
    val wordsSet: Set<String> = words.toSortedSet(String.CASE_INSENSITIVE_ORDER)

    val cardsToRemove = currentWords.filter { it.from in wordsSet }

    // Let's try to remove as one bulk operation to minimize notifications hell.
    removeCards(cardsToRemove)
}


fun LearnWordsController.removeIgnoredFromCurrentWords() {
    val toRemove = currentWords.asSequence()
        .filter { word -> ignoredWordsSorted.contains(word.from) }
        .toList()
    currentWordsList.runWithScrollKeeping { removeCards(toRemove) }
}


fun LearnWordsController.removeWordsFromOtherSetsFromCurrentWords() =
    removeWordsFromOtherSetsFromCurrentWords(currentWords, this.currentWordsFile)
        .also { removeChangeCardListener(it) }
