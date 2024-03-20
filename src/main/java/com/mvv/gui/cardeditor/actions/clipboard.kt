package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.util.toIgnoreCaseSet
import com.mvv.gui.words.*
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.input.DataFormat


fun LearnWordsController.copySelectedWord() = copyWordsToClipboard(currentWordsSelection.selectedItems)


fun LearnWordsController.loadFromClipboard() {
    val toIgnoreWords = if (settingsPane.autoRemoveIgnoredWords) ignoredWords.toIgnoreCaseSet() else emptySet()
    val words = extractWordsFromClipboard(Clipboard.getSystemClipboard(), settingsPane.sentenceEndRule, toIgnoreWords)
        .map { it.adjustCard() }

    currentWords.addAll(words)
    rebuildPrefixFinder()

    reanalyzeAllWords()
}


internal fun LearnWordsController.extractWordsFromClipboard(clipboard: Clipboard, sentenceEndRule: SentenceEndRule, ignoredWords: Set<String>): List<CardWordEntry> {

    val content = clipboard.getContent(DataFormat.PLAIN_TEXT)

    log.info("clipboard content: [${content}]")
    if (content == null) return emptyList()

    //val words = extractWordsFromText(content.toString(), ignoredWords)
    val words = mergeDuplicates(
        extractWordsFromText_New(content.toString(), sentenceEndRule, ignoredWords))

    log.info("clipboard content as words: $words")
    return words
}


internal fun copyWordsToClipboard(words: Iterable<CardWordEntry>) {

    val wordCardsAsString = words
        .joinToString("\n") { "${it.from}  ${it.to}" }

    val clipboardContent = ClipboardContent()
    clipboardContent.putString(wordCardsAsString)
    Clipboard.getSystemClipboard().setContent(clipboardContent)
}
