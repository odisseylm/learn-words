package com.mvv.gui.cardeditor

import com.mvv.gui.cardeditor.LearnWordsController.InsertPosition
import com.mvv.gui.javafx.*
import com.mvv.gui.words.*
import com.mvv.gui.words.WordCardStatus.TooManyExampleNewCardCandidates
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.control.TableView


//private val log = mu.KotlinLogging.logger {}


class ContextMenuController (val controller: LearnWordsController) {
    val contextMenu = ContextMenu()

    private val cloneMenuItem = newMenuItem("Clone") { controller.cloneWordCard() }
    private val mergeMenuItem = newMenuItem("Merge", buttonIcon("icons/merge.png")) { controller.mergeSelected() }

    private val selectByBaseWordMenuItem = newMenuItem("Select by base word") { controller.selectByBaseWord() }
    private val copySelectedToOtherSetMenuItem = newMenuItem("Copy selected cards to other set") { controller.copySelectToOtherSet() }
    /*
    private val exportSelectedFromOtherSetMenuItem = newMenuItem("Export selected from other set") { controller.exportSelectFromOtherSet() }
     */

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

    private val playWordMenuItem = newMenuItem("Play", buttonIcon("icons/ear.png")) { controller.playSelectedWord() }

    private val translateByGoogleMenuItem = newMenuItem("Translate by Google", buttonIcon("icons/gt-02.png")) {
        selectedCard?.from?.also { openGoogleTranslate(it) } }
    private val translateByAbbyMenuItem = newMenuItem("Translate by Abby", buttonIcon("icons/abby-icon-01.png")) {
        selectedCard?.from?.also { openAbbyLingvoTranslate(it) } }

    private val showInitialTranslationMenuItem = newMenuItem("Show initial translation") { controller.showInitialTranslationOfSelected() }
    private val addInitialTranslationMenuItem = newMenuItem("Add initial translation") { controller.addInitialTranslationOfSelected() }


    private val currentWordsList: TableView<CardWordEntry> get() = controller.currentWordsList

    init { fillContextMenu() }

    private fun fillContextMenu() {
        val menuItems = listOf(
            cloneMenuItem,

            SeparatorMenuItem(),
            newMenuItem("Insert above", buttonIcon("/icons/insertAbove-01.png")) {
                controller.insertWordCard(InsertPosition.Above) },
            newMenuItem("Insert below", buttonIcon("/icons/insertBelow-01.png")) {
                controller.insertWordCard(InsertPosition.Below) },

            SeparatorMenuItem(),
            selectByBaseWordMenuItem,
            copySelectedToOtherSetMenuItem,
            /*
            exportSelectedFromOtherSetMenuItem,
             */

            SeparatorMenuItem(),
            newMenuItem("Toggle/lower case", buttonIcon("/icons/toLowerCase.png"), lowerCaseKeyCombination) {
                controller.toggleTextSelectionCaseOrLowerCaseRow() },

            SeparatorMenuItem(),
            mergeMenuItem,

            SeparatorMenuItem(),
            newMenuItem("To ignore >>", buttonIcon("icons/removememory_tsk.png")) { // rem_co.png removememory_tsk.png
                controller.moveSelectedToIgnored() },
            newMenuItem("Remove", buttonIcon("icons/cross-1.png")) {
                controller.removeSelected() },
            translateMenuItem,

            SeparatorMenuItem(),
            translateByGoogleMenuItem,
            translateByAbbyMenuItem,

            SeparatorMenuItem(),
            showInitialTranslationMenuItem,
            addInitialTranslationMenuItem,

            SeparatorMenuItem(),
            ignoreNoBaseWordMenuItem,
            ignoreTooManyExampleCardCandidatesMenuItem,
            addMissedBaseWordsMenuItem,

            SeparatorMenuItem(),
            playWordMenuItem,

            SeparatorMenuItem(),
            newMenuItem("To 'Difficult' Set") { controller.addToOrRemoveFromPredefinedSet(PredefinedSet.DifficultSense)    },
            newMenuItem("To 'Listen' Set")    { controller.addToOrRemoveFromPredefinedSet(PredefinedSet.DifficultToListen) },

            SeparatorMenuItem(),
            showSourceSentenceMenuItem,
        )

        contextMenu.items.addAll(menuItems)

        // I cannot understand is it reliable to change menu items visibility in onShowing ??
        //contextMenu.onShowing = EventHandler { updateItemsVisibility() }

        //com.mvv.gui.javafx.useMenuStateDumping(contextMenu)
    }

    fun updateItemsVisibility() {
        contextMenu.items.forEach { it.isVisible = true }

        val selectedCards = this.selectedCards
        val onlyOneCardIsSelected = selectedCards.size == 1

        cloneMenuItem.isVisible = onlyOneCardIsSelected
        mergeMenuItem.isVisible = controller.isSelectionMergingAllowed()

        updateSelectByBaseWordMenuItem()
        copySelectedToOtherSetMenuItem.isVisible = currentWordsList.hasSelection
        /*
        exportSelectedFromOtherSetMenuItem.isVisible = currentWordsList.singleSelection != null
         */

        showSourceSentenceMenuItem.isVisible = selectedCard?.sourceSentences?.isNotBlank() ?: false
        updateIgnoreNoBaseWordMenuItem(ignoreNoBaseWordMenuItem)
        updateAddMissedBaseWordsMenuItem(addMissedBaseWordsMenuItem)
        updateTranslateMenuItem(translateMenuItem)

        playWordMenuItem.isVisible = onlyOneCardIsSelected

        translateByGoogleMenuItem.isVisible = onlyOneCardIsSelected
        translateByAbbyMenuItem.isVisible = onlyOneCardIsSelected
        showInitialTranslationMenuItem.isVisible = onlyOneCardIsSelected

        ignoreTooManyExampleCardCandidatesMenuItem.isVisible = selectedCards.any {
            TooManyExampleNewCardCandidates in it.statuses }

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

    private fun updateSelectByBaseWordMenuItem() {
        val baseWord = currentWordsList.selectionModel.selectedItem?.baseWordOfFrom
        selectByBaseWordMenuItem.isVisible = !baseWord.isNullOrBlank()
        selectByBaseWordMenuItem.text = "Select cards with base word '${baseWord}*'"
    }

    private val selectedCard: CardWordEntry? get() = currentWordsList.singleSelection
    private val selectedCards: List<CardWordEntry> get() = currentWordsList.selectionModel.selectedItems
}
