package com.mvv.gui.cardeditor

import com.mvv.gui.javafx.buttonIcon
import com.mvv.gui.javafx.newMenuItem
import com.mvv.gui.javafx.noIcon
import javafx.event.EventHandler
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import kotlin.io.path.name


class MenuController (val controller: LearnWordsController) {

    fun fillMenu(): MenuBar {

        val recentMenu = Menu("Recent")

        val fileMenu = Menu("_File", null,
            newMenuItem("New", // new-01.png new-02.png new-03.png new-04.png new-05.png new16x16.gif new-05.gif
                buttonIcon("/icons/new-03.png"), newDocumentKeyCodeCombination) {
                controller.newDocument() },
            newMenuItem("Open/Import file", "Open internal/memo-word csv, or Import srt file",
                buttonIcon("/icons/open16x16.gif"), openDocumentKeyCodeCombination) {
                controller.loadWordsFromFile() },
            recentMenu,
            newMenuItem("Join files", buttonIcon("/icons/join.png")) {
                controller.joinWords() },
            newMenuItem("Save All", "Save current file in internal and memo-word csv format and save ignored words",
                buttonIcon("/icons/disks.png"), saveDocumentKeyCodeCombination) {
                controller.saveAll() },

            SeparatorMenuItem(),
            newMenuItem("Split", "Split current big set to several ones",
                buttonIcon("/icons/slidesstack.png")) { controller.splitCurrentWords() },
        )

        fileMenu.onShown = EventHandler { addRecentMenuItems(recentMenu) }

        val editMenu = Menu("_Edit", null,
            newMenuItem("From clipboard", "Parse text from clipboard", buttonIcon("/icons/paste.gif")) {
                controller.loadFromClipboard() },
            newMenuItem("To toggle/lower case", buttonIcon("/icons/toLowerCase.png"), lowerCaseKeyCombination) {
                controller.toggleTextSelectionCaseOrLowerCaseRow() },

            SeparatorMenuItem(),
            newMenuItem("Insert above", buttonIcon("/icons/insertAbove-01.png")) {
                controller.insertWordCard(LearnWordsController.InsertPosition.Below) },
            newMenuItem("Insert below", buttonIcon("/icons/insertBelow-01.png")) {
                controller.insertWordCard(LearnWordsController.InsertPosition.Below) },

            SeparatorMenuItem(),
            newMenuItem("Line/Selection to example") { controller.moveSubTextToExamples() }
                .also { it.accelerator = moveSubTextToExamplesKeyCombination.first() },
            newMenuItem("Line/Selection to new card") { controller.moveSubTextToSeparateCard() }
                .also { it.accelerator = moveSubTextToSeparateCardKeyCombination.first() },
            newMenuItem("Line/Selection to examples & new card") { controller.moveSubTextToExamplesAndSeparateCard() }
                .also { it.accelerator = moveSubTextToExamplesAndSeparateCardKeyCombination.first() },
        )

        val wordsMenu = Menu("_Words", null,
            newMenuItem("Move to ignored", "Move selected words to ignored.",
                buttonIcon("icons/removememory_tsk.png")) { // rem_all_co.png rem_co.png removememory_tsk.png
                controller.moveSelectedToIgnored() },
            newMenuItem("Remove ignored", "Removed already 'ignored' words from current words.") {
                controller.removeIgnoredFromCurrentWords() },
            newMenuItem("Remove words of All other sets", "Remove words from other sets") {
                controller.removeWordsFromOtherSetsFromCurrentWords() },
            newMenuItem("Remove words of other set...", "Remove words from other sets") {
                controller.removeWordsFromOtherSet() },

            SeparatorMenuItem(),
            newMenuItem("Translate Selected", buttonIcon("/icons/translate-16-01.png"), translateSelectedKeyCombination) {
                controller.translateSelected() },
            newMenuItem("Translate All", "Translate all words", buttonIcon("/icons/translate-16-01.png")) {
                controller.translateAll() },
            newMenuItem("Add transcriptions", "Add missed transcription.", buttonIcon("/icons/transcription-1.png")) {
                controller.addTranscriptions() },

            SeparatorMenuItem(),
            newMenuItem("Add all missed base words", "Add all possible missed base words.", buttonIcon("/icons/icons8-layers-16-with-plus.png")) {
                controller.addAllBaseWordsInSet() },
            newMenuItem("No/Ignore base words", "Ignore warning 'no base word in set'.", buttonIcon("/icons/icons8-layers-16-with-cross.png")) {
                controller.ignoreNoBaseWordInSet() },

            newMenuItem("Ignore example card candidates", "Ignore warning TooManyExampleCardCandidates in set",
                buttonIcon("/icons/skip_brkp.png")) {
                controller.ignoreTooManyExampleCardCandidates() },

            SeparatorMenuItem(),
            newMenuItem("Update all sets indices") {
                controller.allWordCardSetsManager.reloadAllSetsAsync() },
            newMenuItem("Sort & Refresh", buttonIcon("/icons/iu_update_obj.png")) {
                controller.currentWordsList.sort()
                controller.currentWordsList.refresh()
            },
            newMenuItem("Reanalyze") { controller.reanalyzeAllWords() },
        )

        return MenuBar(fileMenu, editMenu, wordsMenu)
    }

    private fun addRecentMenuItems(recentMenuItem: Menu) {
        recentMenuItem.items.clear()

        val recentFiles = RecentDocuments().recentFiles
        val recentDirectories = RecentDocuments().recentDirectories

        val recentFilesMenuItems: List<MenuItem> = recentFiles.map { recentFile ->
            newMenuItem(recentFile.name, recentFile.toString(), noIcon()) { controller.loadWordsFromFile(recentFile) }
        }
        val recentDirsMenuItems: List<MenuItem> = recentDirectories.map { recentDir ->
            newMenuItem(recentDir.name + "/", recentDir.toString(), noIcon()) { controller.loadWordsFromFile(recentDir) }
        }

        recentMenuItem.items.addAll(recentFilesMenuItems)
        if (recentFilesMenuItems.isNotEmpty() && recentDirsMenuItems.isNotEmpty())
            recentMenuItem.items.add(SeparatorMenuItem())
        recentMenuItem.items.addAll(recentDirsMenuItems)
    }
}
