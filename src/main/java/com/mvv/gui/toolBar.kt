package com.mvv.gui

import com.mvv.gui.javafx.button24xIcon
import com.mvv.gui.javafx.buttonIcon
import com.mvv.gui.javafx.new24xButton
import com.mvv.gui.javafx.newButton
import javafx.scene.Node
import javafx.scene.control.ContentDisplay
import javafx.scene.control.Control
import javafx.scene.control.Label
import javafx.scene.control.ToolBar


@Suppress("unused")
class ToolBarController (val controller: LearnWordsController) {

    private val controls: List<Control> = listOf(
        newButton("Load file", "Open internal or memo-word csv, or srt file",
            buttonIcon("/icons/open16x16.gif")) { controller.loadWordsFromFile() },
        newButton("From clipboard", "Parse text from clipboard", buttonIcon("/icons/paste.gif")) {
            controller.loadFromClipboard() },
        newButton("Save All", "Save current file in internal and memo-word csv format and save ignored words",
            buttonIcon("/icons/disks.png", -1.0)) { controller.saveAll() },
        newButton("Split", "Split current big set to several ones",
            buttonIcon("/icons/slidesstack.png")) { controller.splitCurrentWords() },

        Label("  "),

        newButton("To ignore >>", "Move selected words to ignored",
            buttonIcon("icons/removememory_tsk.png")) { controller.moveSelectedToIgnored() } // rem_all_co.png
            .also { it.styleClass.add("middleBarButton") },
        newButton("Translate", "Translate all words", buttonIcon("/icons/translate-16-01.png")) {
            controller.translateAll() },
        newButton("Remove words from other sets", "Remove words from other sets") {
            controller.removeWordsFromOtherSetsFromCurrentWords() },

        Label("  "),

        newButton("Insert above", buttonIcon("/icons/insertAbove-01.png")) {
            controller.insertWordCard(LearnWordsController.InsertPosition.Below) }
            .also { it.contentDisplay = ContentDisplay.RIGHT },
        newButton("Insert below", buttonIcon("/icons/insertBelow-01.png")) {
            controller.insertWordCard(LearnWordsController.InsertPosition.Below) },

        Label("  "),

        newButton("Add all missed base words", "Add all possible missed base words.", buttonIcon("/icons/icons8-layers-16-with-plus.png")) {
            controller.addAllBaseWordsInSet() },
        newButton("No base word", "Ignore warning 'no base word in set'.", buttonIcon("/icons/icons8-layers-16-with-cross.png")) {
            controller.ignoreNoBaseWordInSet() },
        newButton("Add transcriptions", "Add missed transcription.", buttonIcon("/icons/transcription-1.png")) {
            controller.addTranscriptions() },
    )

    fun fillToolBar(toolBar: ToolBar) =
        toolBar.items.addAll(controls)

}


class ToolBarControllerBig (val controller: LearnWordsController) {

    private val controls: List<Node> = listOf(
        new24xButton("New", "Create new words set",
            button24xIcon("/icons/big/new.png")) { controller.newDocument() },
        new24xButton("Open", "Open internal or memo-word csv, or srt file",
            button24xIcon("/icons/big/open-blue-02.png")) { controller.loadWordsFromFile() },
        new24xButton("Save All", "Save current file in internal and memo-word csv format and save ignored words",
            button24xIcon("/icons/big/save-all.png")) { controller.saveAll() },

        stub(),
        new24xButton("Paste", "Parse text from clipboard",
            button24xIcon("/icons/big/paste.png")) { controller.loadFromClipboard() },

        stub(),
        new24xButton("Ignore", "Move selected words to ignored",
            button24xIcon("icons/big/delete-02-gray.png")) { controller.moveSelectedToIgnored() },
        new24xButton("Delete", "Delete selected words",
            button24xIcon("icons/big/delete-02.png")) { controller.removeSelected() },
        //new24xButton("Remove words from other sets", "Remove words from other sets") {
        //    controller.removeWordsFromOtherSetsFromCurrentWords() },

        stub(),
        new24xButton("Translate", "Translate all words", button24xIcon("/icons/big/translate.png")) {
            controller.translateAll() },

        stub(),
        new24xButton("Insert", "Insert above", button24xIcon("/icons/big/curve-above-with-plus.png")) {
            controller.insertWordCard(LearnWordsController.InsertPosition.Below) },
        new24xButton("Insert", "Insert below", button24xIcon("/icons/big/curve-below-with-plus.png")) {
            controller.insertWordCard(LearnWordsController.InsertPosition.Below) },

        stub(),
        new24xButton("Add base", "Add all possible missed base words.",
            button24xIcon("/icons/big/add-base-words-with-plus.png")) { controller.addAllBaseWordsInSet() },
        new24xButton("No base", "Ignore warning 'no base word in set'.",
            button24xIcon("/icons/big/add-base-words-with-cross.png")) { controller.ignoreNoBaseWordInSet() },

        stub(),
        new24xButton("Refresh", "Sort & Refresh'.",
            button24xIcon("/icons/iu_update_obj.png")) { // TODO: find 24x24 icon
            controller.currentWordsList.sort(); controller.currentWordsList.refresh() },
    )

    private fun stub(width: Double = 6.0): Node = Label(" ").also { it.prefWidth = width }

    val toolBar: ToolBar = ToolBar().also { it.items.addAll(controls) }
}
