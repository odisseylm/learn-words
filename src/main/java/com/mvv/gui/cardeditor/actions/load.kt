package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.cardeditor.RecentDocuments
import com.mvv.gui.cardeditor.appTitle
import com.mvv.gui.javafx.setWindowTitle
import com.mvv.gui.javafx.showErrorAlert
import com.mvv.gui.util.toIgnoreCaseSet
import com.mvv.gui.words.*
import javafx.stage.FileChooser
import java.io.File
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.name


fun LearnWordsController.newDocument() {
    validateCurrentDocumentIsSaved("New document")

    currentWords.clear()
    navigationHistory.clear()
    cellEditorStates.clear()
    rebuildPrefixFinder()
    updateCurrentWordsFile(null)
    resetDocumentIsDirty()
}


internal fun LearnWordsController.updateCurrentWordsFile(filePath: Path?) {
    this.currentWordsFile = filePath
    updateTitle()

    allWordCardSetsManager.ignoredFile = filePath
    allWordCardSetsManager.reloadAllSetsAsync()
}


fun LearnWordsController.updateTitle() {
    val filePath: Path? = this.currentWordsFile
    val documentNameTitle = if (filePath == null) appTitle else "$appTitle - ${filePath.name}"
    val documentIsDirtySuffix = if (documentIsDirty) " *" else ""
    val windowTitle = "$documentNameTitle$documentIsDirtySuffix"
    setWindowTitle(pane, windowTitle)
}


enum class LoadType {
    /** In case of open action this file will/can/should be overwritten.  */
    Open,
    /** In case of import action during saving filename will/should be requested.  */
    Import,
}


fun LearnWordsController.doLoadAction(fileOrDir: Path?, loadAction: (Path)->LoadType) {

    validateCurrentDocumentIsSaved("Open file")

    val filePath = if (fileOrDir == null || fileOrDir.isDirectory())
        showOpenDialog(dir = fileOrDir)?.toPath()
        else fileOrDir

    if (filePath == null) return

    if (filePath == ignoredWordsFile) {
        showErrorAlert(pane, "You cannot open [${ignoredWordsFile.name}].")
        return
    }

    val loadType = loadAction(filePath)

    when (loadType) {
        LoadType.Import -> markDocumentIsDirty()
        LoadType.Open   -> {
            // !!! Only if success !!!
            updateCurrentWordsFile(filePath)
            resetDocumentIsDirty()
        }
    }
}

fun LearnWordsController.showOpenDialog(dir: Path? = null, extensions: List<String> = emptyList()): File? {

    val allExtensions = listOf("*.csv", "*.words", "*.txt", "*.srt")
    val ext = extensions.ifEmpty { allExtensions }

    val fc = FileChooser()
    fc.title = "Select words file"
    fc.initialDirectory = (dir ?: dictDirectory).toFile()
    fc.extensionFilters.add(FileChooser.ExtensionFilter("Words file", ext))

    return fc.showOpenDialog(pane.scene.window)
}



fun LearnWordsController.loadWordsFromFile() = loadWordsFromFile(null)

fun LearnWordsController.loadWordsFromFile(file: Path?) {
    doLoadAction(file) { filePath ->
        val fileExt = filePath.extension.lowercase()
        val words: List<CardWordEntry> = when (fileExt) {
            "txt"          -> loadWords(filePath).map { CardWordEntry(it, "") }
            "csv", "words" -> loadWordCards(filePath)
            "srt"          -> loadFromSrt(filePath)
            else           -> throw IllegalArgumentException("Unexpected file extension [${filePath}]")
        }
        // Probably we need to do ANY/EVERY call to analyzeAllWords() async
        .also { analyzeAllWords(it) }
        .map { it.adjustCard() }

        currentWords.setAll(words)

        rebuildPrefixFinder()
        currentWordsList.sort()

        RecentDocuments().addRecent(filePath)

        if (filePath.isInternalCsvFormat) LoadType.Open else LoadType.Import
    }
}


private fun LearnWordsController.loadFromSrt(filePath: Path): List<CardWordEntry> {
    val toIgnoreWords = if (settingsPane.autoRemoveIgnoredWords) this.ignoredWords.toIgnoreCaseSet() else emptySet()
    return extractWordsFromSrtFileAndMerge(filePath, toIgnoreWords)
}
