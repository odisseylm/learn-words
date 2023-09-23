package com.mvv.gui

import com.mvv.gui.LearnWordsController.InsertPosition
import com.mvv.gui.javafx.buttonIcon
import com.mvv.gui.javafx.newMenuItem
import com.mvv.gui.words.CardWordEntry
import com.mvv.gui.words.englishBaseWords
import javafx.event.EventHandler
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.control.TableView


//private val log = mu.KotlinLogging.logger {}


class ContextMenuController (val controller: LearnWordsController) {

    private val currentWordsList: TableView<CardWordEntry> get() = controller.currentWordsList

    fun fillContextMenu(): ContextMenu {
        val contextMenu = ContextMenu()
        fillContextMenu(contextMenu)
        return contextMenu
    }

    private fun fillContextMenu(contextMenu: ContextMenu) {
        val ignoreNoBaseWordMenuItem = newMenuItem("Ignore 'No base word'",
            "Ignore warning 'no base word in set'", buttonIcon("/icons/skip_brkp.png")
        ) { controller.ignoreNoBaseWordInSet() }

        val addMissedBaseWordsMenuItem = newMenuItem("Add missed base word",
            buttonIcon("/icons/toggleexpand.png")) { controller.addBaseWordsInSetForSelected() }
        val translateMenuItem = newMenuItem("Translate selected", buttonIcon("icons/forward_nav.png"),
            translateSelectedKeyCombination ) { controller.translateSelected() }

        val menuItems = listOf(
            newMenuItem("Clone") { controller.cloneWordCard() },

            SeparatorMenuItem(),
            newMenuItem("Insert above", buttonIcon("/icons/insertAbove-01.png")) {
                controller.insertWordCard(InsertPosition.Above) },
            newMenuItem("Insert below", buttonIcon("/icons/insertBelow-01.png")) {
                controller.insertWordCard(InsertPosition.Below) },
            newMenuItem("Lower case", buttonIcon("/icons/toLowerCase.png"), lowerCaseKeyCombination) {
                controller.toLowerCaseRow() },

            SeparatorMenuItem(),
            newMenuItem("To ignore >>", buttonIcon("icons/rem_all_co.png")) {
                controller.moveSelectedToIgnored() },
            newMenuItem("Remove", buttonIcon("icons/cross-1.png")) {
                controller.removeSelected() },
            translateMenuItem,

            SeparatorMenuItem(),
            ignoreNoBaseWordMenuItem,
            addMissedBaseWordsMenuItem,

            SeparatorMenuItem(),
            newMenuItem("Play", buttonIcon("icons/ear.png")) { controller.playSelectedWord() },

            SeparatorMenuItem(),
            newMenuItem("Add to 'Difficult' Set") { controller.addToDifficultSet() },
            newMenuItem("Add to 'Listen' Set") { controller.addToListenSet() },

            SeparatorMenuItem(),
            newMenuItem("Show source sentences", buttonIcon("icons/receiptstext.png")) {
                this.currentWordsList.selectionModel.selectedItem
                    ?.also { controller.showSourceSentences(it) } },
        )

        contextMenu.items.addAll(menuItems)

        contextMenu.onShowing = EventHandler {

            contextMenu.items.forEach { it.isVisible = true }

            updateIgnoreNoBaseWordMenuItem(ignoreNoBaseWordMenuItem)
            updateAddMissedBaseWordsMenuItem(addMissedBaseWordsMenuItem)
            updateTranslateMenuItem(translateMenuItem)

            val visibleMenuItems = contextMenu.items.filter { it.isVisible }
            visibleMenuItems.forEachIndexed { index, menuItem ->
                    if (menuItem.isVisible && menuItem is SeparatorMenuItem) {
                        val isPrevItemIsVisibleSeparator = visibleMenuItems.getOrNull(index - 1)
                            ?.let { it is SeparatorMenuItem && it.isVisible }
                            ?: false
                        if (isPrevItemIsVisibleSeparator) menuItem.isVisible = false
                    }
                }
        }
    }

    private fun updateIgnoreNoBaseWordMenuItem(menuItem: MenuItem) {
        val oneOfSelectedWordsHasNoBaseWord = controller.isOneOfSelectedWordsHasNoBaseWord()
        menuItem.isVisible = oneOfSelectedWordsHasNoBaseWord

        val selectedCards = currentWordsList.selectionModel.selectedItems
        val menuItemText =
            if (selectedCards.size == 1 && oneOfSelectedWordsHasNoBaseWord)
                 "Ignore no base words [${englishBaseWords(selectedCards[0].from, controller.dictionary).joinToString("|")}]"
            else "Ignore 'No base word'"
        menuItem.text = menuItemText
    }

    private fun updateAddMissedBaseWordsMenuItem(menuItem: MenuItem) {
        val oneOfSelectedWordsHasNoBaseWord = controller.isOneOfSelectedWordsHasNoBaseWord()
        menuItem.isVisible = oneOfSelectedWordsHasNoBaseWord

        val selectedCards = currentWordsList.selectionModel.selectedItems
        val menuItemText =
            if (selectedCards.size == 1 && oneOfSelectedWordsHasNoBaseWord)
                 "Add base word(s) '${selectedCards[0].missedBaseWords.joinToString("|") }'"
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
