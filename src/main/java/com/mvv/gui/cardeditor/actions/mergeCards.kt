package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.javafx.runWithScrollKeeping
import com.mvv.gui.util.firstWord
import com.mvv.gui.words.mergeCards


fun LearnWordsController.isSelectionMergingAllowed(): Boolean {
    val selectedCards = currentWordsSelection.selectedItems
    if (selectedCards.size != 2) return false

    val from1baseWord = selectedCards[0].baseWordOfFromProperty.value.firstWord()
    val from2baseWord = selectedCards[1].baseWordOfFromProperty.value.firstWord()

    return from1baseWord != null && from2baseWord != null &&
          (from1baseWord.startsWith(from2baseWord) || from2baseWord.startsWith(from1baseWord))
}


fun LearnWordsController.mergeSelected() {
    val selectedCards = currentWordsSelection.selectedItems.toList() // safe copy

    val merged = mergeCards(selectedCards).adjustCard()
    // currentWordsSelection.selectedIndex is not used because it returns the most recently selected item.
    val firstCardIndex = currentWords.indexOf(selectedCards[0])

    currentWordsList.runWithScrollKeeping {
        removeCards(selectedCards)
        currentWords.add(firstCardIndex, merged)

        reanalyzeOnlyWords(merged)
    }
}
