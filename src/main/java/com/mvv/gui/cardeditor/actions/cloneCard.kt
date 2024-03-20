package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.javafx.runWithScrollKeeping
import com.mvv.gui.javafx.singleSelection
import com.mvv.gui.words.CardWordEntry


fun LearnWordsController.cloneWordCard() {
    val selected: CardWordEntry? = currentWordsList.singleSelection

    selected?.also {
        val pos = currentWordsList.selectionModel.selectedIndex
        val cloned: CardWordEntry = selected.copy().adjustCard()

        currentWordsList.runWithScrollKeeping { currentWords.add(pos, cloned) }
    }
}



