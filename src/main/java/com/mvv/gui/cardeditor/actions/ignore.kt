package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.javafx.runWithScrollKeeping
import com.mvv.gui.words.from


fun LearnWordsController.moveSelectedToIgnored() {

    val selectedWords = currentWordsSelection.selectedItems.toList() // safe copy

    log.debug("selectedWords: {} moved to IGNORED.", selectedWords)

    val newIgnoredWordEntries = selectedWords.filter { !ignoredWordsSorted.contains(it.from) }
    val newIgnoredWords = newIgnoredWordEntries.map { it.from }
    log.debug("newIgnored: {}", newIgnoredWords)
    ignoredWords.addAll(newIgnoredWords)

    currentWordsList.runWithScrollKeeping {
        currentWordsSelection.clearSelection()
        removeCards(selectedWords)
    }
}

/*
fun moveSelectedToIgnored() {

    val sw = startStopWatch("moveSelectedToIgnored")

    val selectedWords = currentWordsSelection.selectedItems.toList()

    log.debug("selectedWords: {} moved to IGNORED.", selectedWords)

    val newIgnoredWordEntries = selectedWords.filter { !ignoredWordsSorted.contains(it.from) }
    sw.logInfo(log, "filter newIgnoredWordEntries")

    val newIgnoredWords = newIgnoredWordEntries.map { it.from }
    log.debug("newIgnored: {}", newIgnoredWords)
    sw.logInfo(log, "creating newIgnoredWords")

    ignoredWords.addAll(newIgnoredWords)
    sw.logInfo(log, "adding newIgnoredWords")

    currentWordsList.runWithScrollKeeping {
        currentWordsSelection.clearSelection()
        sw.logInfo(log, "clearSelection")

        removeCards(selectedWords)
        sw.logInfo(log, "removeAll(selectedWords)")
    }
}
*/
