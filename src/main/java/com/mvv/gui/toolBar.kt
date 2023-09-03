package com.mvv.gui

import com.mvv.gui.javafx.buttonIcon
import com.mvv.gui.javafx.newButton
import com.mvv.gui.words.WarnAboutMissedBaseWordsMode
import javafx.event.EventHandler
import javafx.scene.control.ComboBox
import javafx.scene.control.ContentDisplay
import javafx.scene.control.Control
import javafx.scene.control.Label
import javafx.scene.control.ToolBar


class ToolBarController (val controller: LearnWordsController) {

    private val warnAboutMissedBaseWordsModeDropDown = ComboBox<WarnAboutMissedBaseWordsMode>().also {
        it.items.setAll(com.mvv.gui.words.WarnAboutMissedBaseWordsMode.values().toList())
        it.value = WarnAboutMissedBaseWordsMode.WhenAllBaseWordsMissed
        it.onAction = EventHandler { controller.reanalyzeAllWords() }
    }

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
            buttonIcon("icons/rem_all_co.png")) { controller.moveToIgnored() }
            .also { it.styleClass.add("middleBarButton") },
        newButton("Translate", "Translate all words", buttonIcon("/icons/forward_nav.png")) {
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

        warnAboutMissedBaseWordsModeDropDown,
        newButton("No base word", "Ignore warning 'no base word in set'.", buttonIcon("/icons/skip_brkp.png")) {
            controller.ignoreNoBaseWordInSet() },
        newButton("Add all missed base words", "Add all possible missed base words.", buttonIcon("/icons/toggleexpand.png")) {
            controller.addAllBaseWordsInSet() },
        newButton("Add transcriptions", "Add missed transcription.", buttonIcon("/icons/transcription-1.png")) {
            controller.addTranscriptions() },
    )

    fun fillToolBar(toolBar: ToolBar) =
        toolBar.items.addAll(controls)

    val warnAboutMissedBaseWordsMode: WarnAboutMissedBaseWordsMode get() = warnAboutMissedBaseWordsModeDropDown.value

}
