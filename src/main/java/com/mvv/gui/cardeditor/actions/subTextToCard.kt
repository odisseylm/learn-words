package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.javafx.*
import com.mvv.gui.words.CardWordEntry
import javafx.beans.property.StringPropertyBase
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextArea
import javafx.scene.control.TextInputControl


fun LearnWordsController.moveSubTextToSeparateCard() {
    val focusOwner = pane.scene.focusOwner
    val editingCard = currentWordsList.editingItem
    val editingTableColumn: TableColumn<CardWordEntry, *>? = currentWordsList.editingCell?.column?.let { currentWordsList.columns[it] }

    if (editingCard != null && (editingTableColumn === currentWordsList.toColumn || editingTableColumn === currentWordsList.examplesColumn)
        && focusOwner is TextArea && focusOwner.belongsToParent(currentWordsList)) {
        moveSubTextToSeparateCard(focusOwner, editingCard, editingTableColumn)
    }
}


fun LearnWordsController.moveSubTextToSeparateCard(editor: TextInputControl, item: CardWordEntry, tableColumn: TableColumn<CardWordEntry, String>) {
    createCardFromSelectionOrCurrentLine(editor, tableColumn, item, currentWordsList)
        ?.also { reanalyzeOnlyWords(it) }
        ?.adjustCard()
}


fun LearnWordsController.moveSubTextToExamplesAndSeparateCard(editor: TextInputControl, item: CardWordEntry, tableColumn: TableColumn<CardWordEntry, String>) {
    tryToAdToExamples(editor.selectionOrCurrentLine, item)
    moveSubTextToSeparateCard(editor, item, tableColumn)
}


private fun LearnWordsController.createCardFromSelectionOrCurrentLine(
    textInput: TextInputControl,
    tableColumn: TableColumn<CardWordEntry, String>,
    currentCard: CardWordEntry,
    currentWords: TableView<CardWordEntry>,
): CardWordEntry? {

    val selectionOrCurrentLine = textInput.selectionOrCurrentLine
    if (selectionOrCurrentLine.isBlank()) return null

    val newCard: CardWordEntry = selectionOrCurrentLine.parseToCard()?.adjustCard() ?: return null

    if (textInput.selectedText.isEmpty()) {
        textInput.selectRange(textInput.selectionOrCurrentLineRange)
    }

    // replace/remove also ending '\n'
    if (textInput.selection.end < textInput.text.length && textInput.text[textInput.selection.end] == '\n')
        textInput.selectRange(textInput.selection.start, textInput.selection.end + 1)
    // verify again (in case of end "\n\n")
    if (textInput.selection.end < textInput.text.length && textInput.text[textInput.selection.end] == '\n')
        textInput.selectRange(textInput.selection.start, textInput.selection.end + 1)

    textInput.replaceSelection("")

    val caretPosition = textInput.caretPosition
    val scrollLeft = if (textInput is TextArea) textInput.scrollLeft else 0.0
    val scrollTop = if (textInput is TextArea) textInput.scrollTop else 0.0

    // After adding new card editor loses focus, and we will lose our changes
    // I do not know how to avoid this behaviour...
    // And for that reason I emulate cell commit and reopening it again.

    val prop: StringPropertyBase = tableColumn.cellValueFactory.call(
        TableColumn.CellDataFeatures(currentWords, tableColumn, currentCard)) as StringPropertyBase

    // emulate cell commit to avoid losing changes on focus lost
    prop.set(textInput.text)

    val currentCardIndex = currentWords.items.indexOf(currentCard)

    // We need this workaround because after adding new card current selected one looses focus, and we need to reselect it again...
    // And to avoid unneeded word's autoplaying we disable playing and re-enable it again after re-selecting card.
    val currentToPlayWordOnSelect = toPlayWordOnSelect
    toPlayWordOnSelect = false

    currentWords.runWithScrollKeeping(
        {
            // TODO: initially insert in proper place
            currentWords.items.add(currentCardIndex + 1, newCard)
        },
        {
            cellEditorStates[Pair(currentWordsList.examplesColumn, currentCard)] = CellEditorState(scrollLeft, scrollTop, caretPosition)

            currentWords.selectItem(currentCard)
            currentWords.edit(currentWords.items.indexOf(currentCard), tableColumn)

            runLaterWithDelay(50L) { toPlayWordOnSelect = currentToPlayWordOnSelect }
        })

    return newCard
}
