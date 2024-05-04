package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.javafx.runWithScrollKeeping
import com.mvv.gui.javafx.showInfoAlert
import com.mvv.gui.words.*
import java.nio.file.Path


fun LearnWordsController.removeSelected() {
    val toRemoveSafeCopy = currentWordsSelection.selectedItems.toList()
    currentWordsSelection.clearSelection()
    currentWordsList.runWithScrollKeeping { removeCards(toRemoveSafeCopy) }
}


fun LearnWordsController.removeCards(toRemove: List<CardWordEntry>) {
    currentWords.removeAll(toRemove.toSet())
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


fun removeWordsFromOtherSetsFromCurrentWords(currentWords: MutableList<CardWordEntry>, currentWordsFile: Path?): List<CardWordEntry> {

    val toRemove = loadWordsFromAllExistentDictionaries(currentWordsFile?.baseWordsFilename)
    val toRemoveAsSet = toRemove.asSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toSortedSet(String.CASE_INSENSITIVE_ORDER)

    val currentToRemove = currentWords.filter { it.from.trim() in toRemoveAsSet }

    // perform removing as ONE operation to minimize change events
    currentWords.removeAll(currentToRemove.toSet())

    return currentToRemove
}
