package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.javafx.runLaterWithDelay
import com.mvv.gui.javafx.runWithScrollKeeping
import com.mvv.gui.words.CardWordEntry


fun LearnWordsController.addChangeCardListener(card: CardWordEntry) {
    card.fromProperty.addListener(changeCardListener)
    card.fromWithPrepositionProperty.addListener(changeCardListener)
    card.toProperty.addListener(changeCardListener)
    card.transcriptionProperty.addListener(changeCardListener)
    card.examplesProperty.addListener(changeCardListener)
    card.statusesProperty.addListener(changeCardListener)
    card.predefinedSetsProperty.addListener(changeCardListener)
    card.sourcePositionsProperty.addListener(changeCardListener)
    card.sourceSentencesProperty.addListener(changeCardListener)
}


fun LearnWordsController.removeChangeCardListener(cards: Iterable<CardWordEntry>) =
    cards.forEach { removeChangeCardListener(it) }


private fun LearnWordsController.removeChangeCardListener(card: CardWordEntry) {
    card.fromProperty.removeListener(changeCardListener)
    card.fromWithPrepositionProperty.removeListener(changeCardListener)
    card.toProperty.removeListener(changeCardListener)
    card.transcriptionProperty.removeListener(changeCardListener)
    card.examplesProperty.removeListener(changeCardListener)
    card.statusesProperty.removeListener(changeCardListener)
    card.predefinedSetsProperty.removeListener(changeCardListener)
    card.sourcePositionsProperty.removeListener(changeCardListener)
    card.sourceSentencesProperty.removeListener(changeCardListener)
}


enum class InsertPosition { Above, Below }


fun LearnWordsController.insertWordCard(insertPosition: InsertPosition) {
    val isWordCardsSetEmpty = currentWords.isEmpty()
    val currentlySelectedIndex = currentWordsSelection.selectedIndex

    val positionToInsert = when {
        isWordCardsSetEmpty -> 0

        currentlySelectedIndex != -1 -> when (insertPosition) {
            InsertPosition.Above -> currentlySelectedIndex
            InsertPosition.Below -> currentlySelectedIndex + 1
        }

        else -> -1
    }

    if (positionToInsert != -1) {
        val newCardWordEntry = CardWordEntry("", "").adjustCard()

        if (currentWordsList.editingCell?.row != -1)
            currentWordsList.edit(-1, null)

        currentWordsList.runWithScrollKeeping( {

                currentWords.add(positionToInsert, newCardWordEntry)
                currentWordsSelection.clearAndSelect(positionToInsert, currentWordsList.fromColumn)
            },
            {
                // JavaFX bug.
                // Without this call at the end of view editing cell appears twice (in right position and in wrong some below position)
                // and erases data in wrong (below) position !!!
                // Probably such behavior happens due to my hack in TableView.fixEstimatedContentHeight() (which uses scrolling over all rows)
                // but now I have no better idea how to fix EstimatedContentHeight.
                //
                currentWordsList.refresh()

                // JavaFX bug: without runLaterWithDelay() editing cell does not get focus (you need to click on it or use Tab key)
                // if column cells were not present before (if TableView did not have content yet).
                // Platform.runLater() also does not help.
                //
                runLaterWithDelay(50) { currentWordsList.edit(positionToInsert, currentWordsList.fromColumn) }
            }
        )
    }
}
