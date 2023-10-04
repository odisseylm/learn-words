package com.mvv.gui

import com.mvv.gui.LearnWordsController.InsertPosition
import com.mvv.gui.javafx.*
import com.mvv.gui.words.CardWordEntry
import com.mvv.gui.words.WordCardStatus.TooManyExampleNewCardCandidates
import com.mvv.gui.words.englishBaseWords
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.control.TableView


//private val log = mu.KotlinLogging.logger {}


class ContextMenuController (val controller: LearnWordsController) {
    val contextMenu = ContextMenu()

    private val addMissedBaseWordsMenuItem = newMenuItem("Add missed base word",
        buttonIcon("/icons/icons8-layers-16-with-plus.png")) {
        controller.addBaseWordsInSetForSelected() }
    private val ignoreNoBaseWordMenuItem = newMenuItem("Ignore 'No base word'",
        "Ignore warning 'no base word in set'", buttonIcon("/icons/icons8-layers-16-with-cross.png")) {
        controller.ignoreNoBaseWordInSet() }

    private val ignoreTooManyExampleCardCandidatesMenuItem = newMenuItem("Ignore example card candidates",
        "Ignore warning TooManyExampleCardCandidates in set", buttonIcon("/icons/skip_brkp.png")) {
        controller.ignoreTooManyExampleCardCandidates() }

    private val translateMenuItem = newMenuItem("Translate selected", buttonIcon("icons/translate-16-01.png"),
        translateSelectedKeyCombination ) { controller.translateSelected() }

    private val showSourceSentenceMenuItem = newMenuItem("Show source sentences", buttonIcon("icons/receiptstext.png")) {
        this.selectedCard?.also { controller.showSourceSentences(it) } }

    private val currentWordsList: TableView<CardWordEntry> get() = controller.currentWordsList

    init { fillContextMenu() }

    private fun fillContextMenu() {
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
            newMenuItem("To ignore >>", buttonIcon("icons/removememory_tsk.png")) { // rem_co.png removememory_tsk.png
                controller.moveSelectedToIgnored() },
            newMenuItem("Remove", buttonIcon("icons/cross-1.png")) {
                controller.removeSelected() },
            translateMenuItem,

            SeparatorMenuItem(),
            ignoreNoBaseWordMenuItem,
            ignoreTooManyExampleCardCandidatesMenuItem,
            addMissedBaseWordsMenuItem,

            SeparatorMenuItem(),
            newMenuItem("Play", buttonIcon("icons/ear.png")) { controller.playSelectedWord() },

            SeparatorMenuItem(),
            newMenuItem("Add to 'Difficult' Set") { controller.addToDifficultSet() },
            newMenuItem("Add to 'Listen' Set") { controller.addToListenSet() },

            SeparatorMenuItem(),
            showSourceSentenceMenuItem,

            SeparatorMenuItem(),
            newMenuItem("Translate by Google", buttonIcon("icons/gt-02.png")) {
                this.currentWordsList.selectionModel.selectedItem
                    ?.also { openGoogleTranslate(it.from) } },
            newMenuItem("Translate by Abby", buttonIcon("icons/abby-icon-01.png")) {
                this.currentWordsList.selectionModel.selectedItem
                    ?.also { openAbbyLingvoTranslate(it.from) } },
        )

        contextMenu.items.addAll(menuItems)

        // I cannot understand is it reliable to change menu items visibility in onShowing ??
        //contextMenu.onShowing = EventHandler { updateItemsVisibility() }

        //com.mvv.gui.javafx.useMenuStateDumping(contextMenu)
    }

    fun updateItemsVisibility() {
        contextMenu.items.forEach { it.isVisible = true }

        showSourceSentenceMenuItem.isVisible = selectedCard?.sourceSentences?.isNotBlank() ?: false
        updateIgnoreNoBaseWordMenuItem(ignoreNoBaseWordMenuItem)
        updateAddMissedBaseWordsMenuItem(addMissedBaseWordsMenuItem)
        updateTranslateMenuItem(translateMenuItem)

        ignoreTooManyExampleCardCandidatesMenuItem.isVisible = selectedCards.any {
            TooManyExampleNewCardCandidates in it.wordCardStatuses }

        contextMenu.hideRepeatedMenuSeparators()
    }

    private fun updateIgnoreNoBaseWordMenuItem(menuItem: MenuItem) {
        val oneOfSelectedWordsHasNoBaseWord = controller.isOneOfSelectedWordsHasNoBaseWord()
        menuItem.isVisible = oneOfSelectedWordsHasNoBaseWord

        val selectedCards = this.selectedCards
        val menuItemText =
            if (selectedCards.size == 1 && oneOfSelectedWordsHasNoBaseWord)
                 "Ignore no base words [${englishBaseWords(selectedCards[0].from, controller.dictionary).joinToString("|")}]"
            else "Ignore 'No base word'"
        menuItem.text = menuItemText
    }

    private fun updateAddMissedBaseWordsMenuItem(menuItem: MenuItem) {
        val oneOfSelectedWordsHasNoBaseWord = controller.isOneOfSelectedWordsHasNoBaseWord()
        menuItem.isVisible = oneOfSelectedWordsHasNoBaseWord

        val selectedCards = this.selectedCards
        val menuItemText =
            if (selectedCards.size == 1 && oneOfSelectedWordsHasNoBaseWord)
                 "Add base word(s) '${selectedCards[0].missedBaseWords.joinToString("|") }'"
            else "Add missed base word"
        menuItem.text = menuItemText
    }

    private fun updateTranslateMenuItem(menuItem: MenuItem) {
        val oneOfSelectedIsNotTranslated = currentWordsList.selectionModel.selectedItems.any { it.to.isBlank() }
        menuItem.isVisible = oneOfSelectedIsNotTranslated

        val selectedCards = this.selectedCards
        val menuItemText =
            if (selectedCards.size == 1 && oneOfSelectedIsNotTranslated)
                 "Translate '${selectedCards[0].from}'"
            else "Translate selected"
        menuItem.text = menuItemText
    }

    private val selectedCard: CardWordEntry? get() = currentWordsList.singleSelection
    private val selectedCards: List<CardWordEntry> get() = currentWordsList.selectionModel.selectedItems
}
