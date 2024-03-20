package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.javafx.UpdateSet
import com.mvv.gui.javafx.updateSetProperty
import com.mvv.gui.words.PredefinedSet
import com.mvv.gui.words.predefinedSets



fun LearnWordsController.addToOrRemoveFromPredefinedSet(set: PredefinedSet) {
    val cards = currentWordsSelection.selectedItems
    if (cards.isEmpty()) return

    val someCardsAreNotAddedToPredefSet = cards.any { set !in it.predefinedSets }

    // For me this logic is more expected/desired.
    // We can use just simple inversion logic.
    val addOrRemoveAction = if (someCardsAreNotAddedToPredefSet) UpdateSet.Set else UpdateSet.Remove

    cards.forEach {
        updateSetProperty(it.predefinedSetsProperty, set, addOrRemoveAction) }
}
