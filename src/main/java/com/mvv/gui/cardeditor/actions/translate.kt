package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.javafx.showTextAreaPreviewDialog
import com.mvv.gui.javafx.singleSelection
import com.mvv.gui.util.doIfNotEmpty
import com.mvv.gui.words.*
import com.mvv.gui.words.translateWord
import com.mvv.gui.words.translateWords
import javafx.beans.value.WritableValue


fun LearnWordsController.translateSelected() =
    currentWordsSelection.selectedItems.doIfNotEmpty {
        dictionary.translateWords(it)
        markDocumentIsDirty()
        reanalyzeOnlyWords(it)
        currentWordsList.refresh()
    }


fun LearnWordsController.translateAll() {
    dictionary.translateWords(currentWords)
    markDocumentIsDirty()
    reanalyzeAllWords()
    currentWordsList.refresh()
}


fun LearnWordsController.showInitialTranslationOfSelected() {
    val card = currentWordsList.singleSelection ?: return

    val translated = dictionary.translateWord(card.from.trim())
    showTextAreaPreviewDialog(currentWordsList, "Translation of '${card.from}'",
        translated.to + "\n" + translated.examples, wrapText = true, width = 400.0, height = 400.0)
}


fun LearnWordsController.addInitialTranslationOfSelected() {
    val card = currentWordsList.singleSelection ?: return

    val translated = dictionary.translateWord(card.from.trim())

    addToCardProp(card, translated) { it.toProperty }
    addToCardProp(card, translated) { it.examplesProperty }
    addToCardProp(card, translated) { it.transcriptionProperty }

    reanalyzeOnlyWords(card)
}


private fun addToCardProp(card: CardWordEntry, addFromCard: CardWordEntry, prop: (CardWordEntry)-> WritableValue<String>) {
    val cardProp = prop(card)
    val cardPropValue = cardProp.value
    val valueToAdd    = prop(addFromCard).value

    if (cardPropValue == valueToAdd) return

    if (cardPropValue.isNotBlank()) cardProp.value += "\n\n"
    cardProp.value += valueToAdd
}
