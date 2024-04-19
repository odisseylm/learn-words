package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.javafx.belongsToParent
import com.mvv.gui.util.containsLetter
import com.mvv.gui.util.splitToWords
import com.mvv.gui.util.wordCount
import com.mvv.gui.words.MatchMode
import com.mvv.gui.words.articlesAndSimilar
import com.mvv.gui.words.prepositions
import javafx.geometry.Point2D
import javafx.scene.control.TextInputControl


fun LearnWordsController.showSynonymOfCurrentWord() {
    val focusOwner = pane.scene?.focusOwner

    val currentWordOrPhrase: String? =
        if (focusOwner is TextInputControl && focusOwner.belongsToParent(currentWordsList)) {
            val selectedText = focusOwner.selectedText
            if (selectedText.wordCount in 1..3) selectedText.trim()
            else focusOwner.text.getWordAt(focusOwner.caretPosition)
        }
        else null

    if (!currentWordOrPhrase.isNullOrBlank() && !toIgnoreSynonymFor(currentWordOrPhrase)) {
        if (synonymsPopup.wordOrPhrase != currentWordOrPhrase || !synonymsPopup.isShowing) {
            if (!showSynonymsOf(currentWordOrPhrase)) synonymsPopup.hide()
        }
    }
    else synonymsPopup.hide()
}


private val toIgnoreSynonymsFor: Set<String> = (
            prepositions.flatMap { it.words }
            + articlesAndSimilar.flatMap { it.splitToWords() }
            + listOf(
                "do", "does", "be", "is", "have", "has", "have no", "has no", "get", "gets", "go", "goes",
                "can", "could", "may", "might", "must", "shall", "should",
                // https://enginform.com/article/contractions-in-english
                "cannot", "can't", "couldn't", "mayn't", "mightn't", "mustn't", "shouldn't", "needn’t", "oughtn’t",
                "isn't", "aren't", "wasn't", "weren't", "won't", "wouldn’t",
                "doesn't", "don't", "didn't",
                "hasn't", "haven't", "hadn't",
                // "_v", "_v.", "(v.)",
                // "[02",
                // "хим.
            ).flatMap { it.splitToWords() })
        .toSet()

private fun toIgnoreSynonymFor(currentWordOrPhrase: String): Boolean {
    val word = currentWordOrPhrase.trim().lowercase()

    if (word in toIgnoreSynonymsFor) return true

    if (!word.containsLetter()) return true

    return false
}

private fun LearnWordsController.showSynonymsOf(word: String): Boolean {

    val synonymCards = allWordCardSetsManager.findBy(word, MatchMode.Exact)
    if (synonymCards.isEmpty()) return false

    synonymsPopup.show(pane, word, synonymCards) {
        val mainWnd = pane.scene.window
        val bottomPopup = foundCardsViewPopup
        val xOffset = 20.0;  val yOffset = 50.0

        val newY = if (bottomPopup.isShowing)
                        // If some other popup is already shown at bottom, we will show synonym popup a bit above it.
                        bottomPopup.y - synonymsPopup.height - 10.0
                        // Otherwise we will show it at the most bottom position.
                   else mainWnd.y + mainWnd.height - synonymsPopup.height - yOffset
        Point2D(
            mainWnd.x + mainWnd.width - synonymsPopup.width - xOffset,
            newY)

    }
    return true
}


/** @param caretPosition it can be in range 0…length (in contrast index can be only in range 0…length-1) */
internal fun CharSequence.getWordAt(caretPosition: Int): String? {
    if (caretPosition < 0 || caretPosition > this.length) return null

    fun Char.isPartOfWord() = this.isLetter() || this == '-'

    val wordPos = when {
        caretPosition < this.length && this[caretPosition].isPartOfWord() -> caretPosition
        caretPosition > 0 && this[caretPosition - 1].isPartOfWord() -> caretPosition - 1
        else -> return null
    }

    var startWordIndex = 0
    for (i in wordPos - 1 downTo 0) {
        val ch = this[i]

        if (!ch.isPartOfWord()) {
            startWordIndex = i + 1
            break
        }
    }

    var endWordIndex = this.length
    for (i in wordPos until this.length) {
        if (!this[i].isPartOfWord()) {
            endWordIndex = i
            break
        }
    }

    return this.substring(startWordIndex, endWordIndex)
}
