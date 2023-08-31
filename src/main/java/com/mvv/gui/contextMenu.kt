package com.mvv.gui

import com.mvv.gui.LearnWordsController.InsertPosition
import com.mvv.gui.javafx.buttonIcon
import com.mvv.gui.javafx.newMenuItem
import com.mvv.gui.words.CardWordEntry
import com.mvv.gui.words.possibleBestEnglishBaseWord
import com.mvv.gui.words.possibleEnglishBaseWords
import javafx.event.EventHandler
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.TableView


class ContextMenuController (val controller: LearnWordsController) {

    private val currentWordsList: TableView<CardWordEntry> get() = controller.currentWordsList

    fun fillContextMenu(contextMenu: ContextMenu) {
        val ignoreNoBaseWordMenuItem = newMenuItem("Ignore 'No base word'",
            "Ignore warning 'no base word in set'", buttonIcon("/icons/skip_brkp.png")
        ) { controller.ignoreNoBaseWordInSet() }

        val addMissedBaseWordsMenuItem = newMenuItem("Add missed base word",
            buttonIcon("/icons/toggleexpand.png")) { controller.addBaseWordsInSetForSelected() }
        val translateMenuItem = newMenuItem("Translate selected", buttonIcon("icons/forward_nav.png"),
            translateSelectedKeyCombination ) { controller.translateSelected() }

        val menuItems = listOf(
            newMenuItem("Insert above", buttonIcon("/icons/insertAbove-01.png")) {
                controller.insertWordCard(InsertPosition.Above) },
            newMenuItem("Insert below", buttonIcon("/icons/insertBelow-01.png")) {
                controller.insertWordCard(InsertPosition.Below) },
            newMenuItem("Lower case", buttonIcon("/icons/toLowerCase.png"), lowerCaseKeyCombination) {
                controller.toLowerCaseRow() },
            newMenuItem("To ignore >>", buttonIcon("icons/rem_all_co.png")) {
                controller.moveToIgnored() },
            newMenuItem("Remove", buttonIcon("icons/cross-1.png")) {
                controller.removeSelected() },
            translateMenuItem,
            ignoreNoBaseWordMenuItem,
            addMissedBaseWordsMenuItem,
        )

        contextMenu.items.addAll(menuItems)

        contextMenu.onShowing = EventHandler {
            updateIgnoreNoBaseWordMenuItem(ignoreNoBaseWordMenuItem)
            updateAddMissedBaseWordsMenuItem(addMissedBaseWordsMenuItem)
            updateTranslateMenuItem(translateMenuItem)
        }
    }

    private fun updateIgnoreNoBaseWordMenuItem(menuItem: MenuItem) {
        val oneOfSelectedWordsHasNoBaseWord = controller.isOneOfSelectedWordsHasNoBaseWord()
        menuItem.isVisible = oneOfSelectedWordsHasNoBaseWord

        val selectedCards = currentWordsList.selectionModel.selectedItems
        val menuItemText =
            if (selectedCards.size == 1 && oneOfSelectedWordsHasNoBaseWord)
                "Ignore no base words [${possibleEnglishBaseWords(selectedCards[0].from).joinToString("|")}]"
            else "Ignore 'No base word'"
        menuItem.text = menuItemText
    }

    private fun updateAddMissedBaseWordsMenuItem(menuItem: MenuItem) {
        val oneOfSelectedWordsHasNoBaseWord = controller.isOneOfSelectedWordsHasNoBaseWord()
        menuItem.isVisible = oneOfSelectedWordsHasNoBaseWord

        val selectedCards = currentWordsList.selectionModel.selectedItems
        val menuItemText =
            if (selectedCards.size == 1 && oneOfSelectedWordsHasNoBaseWord) {
                val possibleBaseWord = possibleBestEnglishBaseWord(selectedCards[0].from)
                val showBaseWord = if (possibleBaseWord != null && !possibleBaseWord.endsWith('e'))
                    "${possibleBaseWord}(e)" else possibleBaseWord
                "Add base word '$showBaseWord'"
            }
            else "Add missed base word"
        menuItem.text = menuItemText
    }

    private fun updateTranslateMenuItem(menuItem: MenuItem) {
        val oneOfSelectedIsNotTranslated = currentWordsList.selectionModel.selectedItems.any { it.to.isBlank() }
        menuItem.isVisible = oneOfSelectedIsNotTranslated

        val selectedCards = currentWordsList.selectionModel.selectedItems
        val menuItemText =
            if (selectedCards.size == 1 && oneOfSelectedIsNotTranslated)
                "Translate '${selectedCards[0].from}'"
            else "Translate selected"
        menuItem.text = menuItemText
    }

}
