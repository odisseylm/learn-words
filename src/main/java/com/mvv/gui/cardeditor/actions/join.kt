package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.javafx.showInfoAlert
import com.mvv.gui.words.CardWordEntry
import com.mvv.gui.words.dictDirectory
import com.mvv.gui.words.loadWordCards
import javafx.stage.FileChooser
import java.io.File


fun LearnWordsController.joinWords() {

    val fc = FileChooser()
    fc.title = "Select words files"
    fc.initialDirectory = dictDirectory.toFile()
    fc.extensionFilters.add(FileChooser.ExtensionFilter("Words file", "*.csv"))
    val selectedFiles: List<File>? = fc.showOpenMultipleDialog(pane.scene.window)?.sorted()

    if (selectedFiles.isNullOrEmpty()) {
        //showInfoAlert(pane, "No files to join.")
        return
    }

    if (selectedFiles.size == 1) {
        showInfoAlert(pane, "No sense to use join only one file. Just use load/open action.")
        return
    }

    val words: List<CardWordEntry> = selectedFiles
        .flatMap { loadWordCards(it.toPath()) }
        .also { analyzeAllWords(it) }

    if (words.isEmpty()) {
        showInfoAlert(pane, "Files ${selectedFiles.map { it.name }} do not contain words.")
        return
    }

    updateCurrentWordsFile(null)
    currentWords.setAll(words)
}
