package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.javafx.showErrorAlert
import com.mvv.gui.javafx.showTextInputDialog
import com.mvv.gui.words.CsvFormat
import com.mvv.gui.words.saveSplitWordCards
import java.text.SimpleDateFormat
import java.util.*



fun LearnWordsController.splitCurrentWords(): List<FileSaveResult> = doSaveCurrentWords { filePath ->
    val words = currentWords
    val df = SimpleDateFormat("yyyyMMdd-HHmmss")
    val splitFilesDir = filePath.parent.resolve("split-${df.format(Date())}")

    //val defaultSplitWordCountPerFile = settingsPane.splitWordCountPerFile
    val strSplitWordCountPerFile = showTextInputDialog(pane,
        "Current words will be split into several files and put into directory $splitFilesDir.\n" +
                "Please, enter word count for every file.", "Splitting current words",
        //"$defaultSplitWordCountPerFile")
        "")

    if (strSplitWordCountPerFile.isEmpty) emptyList()
    else {
        try {
            val wordCountPerFile: Int = strSplitWordCountPerFile.get().toInt()
            require(wordCountPerFile >= 20) { "Word count should be positive value." }

            //require(wordCountPerFile <= maxMemoCardWordCount) {
            //    "Word count should be less than 300 since memo-word supports only $maxMemoCardWordCount sized word sets." }

            saveSplitWordCards(filePath, words, splitFilesDir, wordCountPerFile, CsvFormat.Internal)
                .map { FileSaveResult(it, FileSaveResult.Operation.Saved) }
        }
        catch (ex: Exception) {
            log.error(ex) { "Splitting words error: ${ex.message}" }
            showErrorAlert(pane, ex.message ?: "Unknown error", "Error of splitting.")
            emptyList()
        }
    }
}
