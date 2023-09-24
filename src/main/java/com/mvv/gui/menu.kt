package com.mvv.gui

import com.mvv.gui.javafx.buttonIcon
import com.mvv.gui.javafx.newMenuItem
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
            newMenuItem("Load file", "Open internal or memo-word csv, or srt file",
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

            SeparatorMenuItem(),
            newMenuItem("Insert above", buttonIcon("/icons/insertAbove-01.png")) {
                controller.insertWordCard(LearnWordsController.InsertPosition.Below) },
                //.also { it.contentDisplay = ContentDisplay.RIGHT },
            newMenuItem("Insert below", buttonIcon("/icons/insertBelow-01.png")) {
                controller.insertWordCard(LearnWordsController.InsertPosition.Below) },
        )

        val wordsMenu = Menu("_Words", null,
            newMenuItem("Move to ignored", "Move selected words to ignored.",
                buttonIcon("icons/rem_all_co.png")) { controller.moveSelectedToIgnored() },
            newMenuItem("Remove ignored", "Removed ignored words from current words.") { controller.removeIgnoredFromCurrentWords() },
            newMenuItem("Translate Selected", buttonIcon("/icons/forward_nav.png"), translateSelectedKeyCombination) {
                controller.translateSelected() },
            newMenuItem("Translate All", "Translate all words", buttonIcon("/icons/forward_nav.png")) {
                controller.translateAll() },
            newMenuItem("Remove words of other sets", "Remove words from other sets") {
                controller.removeWordsFromOtherSetsFromCurrentWords() },

            SeparatorMenuItem(),
            newMenuItem("No base word", "Ignore warning 'no base word in set'.", buttonIcon("/icons/skip_brkp.png")) {
                controller.ignoreNoBaseWordInSet() },
            newMenuItem("Add all missed base words", "Add all possible missed base words.", buttonIcon("/icons/toggleexpand.png")) {
                controller.addAllBaseWordsInSet() },
            newMenuItem("Add transcriptions", "Add missed transcription.", buttonIcon("/icons/transcription-1.png")) {
                controller.addTranscriptions() },

            SeparatorMenuItem(),
            newMenuItem("Sort") { controller.currentWordsList.sort() },
            newMenuItem("Reanalyze") { controller.reanalyzeAllWords() },
            newMenuItem("Refresh table", buttonIcon("/icons/iu_update_obj.png")) { controller.currentWordsList.refresh() },
        )

        return MenuBar(fileMenu, editMenu, wordsMenu)
    }

    private fun addRecentMenuItems(recentMenuItem: Menu) {
        recentMenuItem.items.clear()

        val recentMenuItems: List<MenuItem> = RecentDocuments().recents.map { recentPath ->
            MenuItem(recentPath.name)
                .also { it.onAction = EventHandler { controller.loadWordsFromFile(recentPath) } }
        }
        recentMenuItem.items.setAll(recentMenuItems)
    }
}
