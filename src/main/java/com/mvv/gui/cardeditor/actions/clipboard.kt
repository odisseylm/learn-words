package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.util.toIgnoreCaseSet
import com.mvv.gui.words.copyWordsToClipboard
import com.mvv.gui.words.extractWordsFromClipboard
import javafx.scene.input.Clipboard


fun LearnWordsController.copySelectedWord() = copyWordsToClipboard(currentWordsSelection.selectedItems)


fun LearnWordsController.loadFromClipboard() {
    val toIgnoreWords = if (settingsPane.autoRemoveIgnoredWords) ignoredWords.toIgnoreCaseSet() else emptySet()
    val words = extractWordsFromClipboard(Clipboard.getSystemClipboard(), settingsPane.sentenceEndRule, toIgnoreWords)
        .map { it.adjustCard() }

    currentWords.addAll(words)
    rebuildPrefixFinder()

    reanalyzeAllWords()
}
