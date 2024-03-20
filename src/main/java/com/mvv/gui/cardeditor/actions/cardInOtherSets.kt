package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.javafx.singleSelection
import com.mvv.gui.words.AllCardWordEntry
import com.mvv.gui.words.MatchMode
import com.mvv.gui.words.from
import javafx.geometry.Point2D


fun LearnWordsController.showWarningAboutSelectedCardExistInOtherSet() {
    val selectedWordOrPhrase = currentWordsList.singleSelection?.from ?: ""

    val foundInOtherSets = allWordCardSetsManager.findBy(selectedWordOrPhrase, MatchMode.Exact)
    if (foundInOtherSets.isNotEmpty())
        showThisWordsInLightOtherSetsPopup(selectedWordOrPhrase, foundInOtherSets)
    else
        lightOtherCardsViewPopup.hide()
}


private fun LearnWordsController.showThisWordsInLightOtherSetsPopup(wordOrPhrase: String, cards: List<AllCardWordEntry>) {
    lightOtherCardsViewPopup.show(pane, wordOrPhrase, cards) {
        val mainWnd = pane.scene.window
        val xOffset = 20.0;  val yOffset = 80.0

        Point2D(
            mainWnd.x + mainWnd.width - lightOtherCardsViewPopup.width - xOffset,
            mainWnd.y + yOffset)
    }
}


fun LearnWordsController.showThisWordsInOtherSetsPopup(wordOrPhrase: String, cards: List<AllCardWordEntry>) {
    lightOtherCardsViewPopup.hide()
    otherCardsViewPopup.show(pane, "Word '$wordOrPhrase' already exists in other sets", cards) {
        val mainWnd = pane.scene.window
        val xOffset = 20.0;  val yOffset = 50.0

        Point2D(
            mainWnd.x + mainWnd.width - otherCardsViewPopup.width - xOffset,
            mainWnd.y + yOffset)
    }
}


internal fun LearnWordsController.showSpecifiedWordsInOtherSetsPopup(wordOrPhrase: String, cards: List<AllCardWordEntry>) =
    showInOtherSetsPopup("'$wordOrPhrase' in other sets", cards)


internal fun LearnWordsController.showInOtherSetsPopup(title: String, cards: List<AllCardWordEntry>) {
    foundCardsViewPopup.show(pane, title, cards) {
        val mainWnd = pane.scene.window
        val xOffset = 20.0;  val yOffset = 50.0

        Point2D(
            mainWnd.x + mainWnd.width - foundCardsViewPopup.width - xOffset,
            mainWnd.y + mainWnd.height - foundCardsViewPopup.height - yOffset)
    }
}
