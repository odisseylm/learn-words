package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.cardeditor.RecentDocuments
import com.mvv.gui.javafx.showConfirmation
import com.mvv.gui.javafx.showErrorAlert
import com.mvv.gui.javafx.showTextInputDialog
import com.mvv.gui.util.ActionCanceledException
import com.mvv.gui.util.containsWhiteSpaceInMiddle
import com.mvv.gui.util.withFileExt
import com.mvv.gui.words.*
import javafx.scene.control.ButtonType
import java.nio.file.Path
import kotlin.io.path.*


/**
 * Throws exception if document is not saved and user would cancel saving.
 *
 * Actually there exception is used for (less/more) flow what is an anti-pattern...
 * but in this case it is make sense in my opinion because you do not need to write 'if'-s
 * and can use it as assert/require flows.
 * If you want to use boolean function you just can use doIsCurrentDocumentIsSaved().
 */
fun LearnWordsController.validateCurrentDocumentIsSaved(currentAction: String = "") {

    fun throwCanceled(msg: String = "Action [$currentAction] was cancelled."): Nothing { throw ActionCanceledException(currentAction, msg)}

    if (documentIsDirty) {
        val selectedButton = showConfirmation(pane,
            "Current words are not saved and last changes can be lost.\n" +
                "Do you want save current words?",
            currentAction, ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)

        selectedButton.ifPresentOrElse(
            { btn ->
                when (btn) {
                    ButtonType.NO  -> { /* Nothing to do */ }
                    ButtonType.YES -> {
                        saveCurrentWords()
                        if (documentIsDirty) throwCanceled("Seems save action was cancelled.")
                    }
                    ButtonType.CANCEL -> throwCanceled()
                    ButtonType.CLOSE  -> throwCanceled()
                    else -> throw IllegalStateException("Unexpected user choice [$btn].")
                }
            },
            { throwCanceled() }
        )
    }
}


fun LearnWordsController.saveIgnored() {
    val ignoredWords = this.ignoredWordsSorted
    if (ignoredWords.isNotEmpty())
        saveWordsToTxtFile(ignoredWordsFile, ignoredWords)
}


fun LearnWordsController.saveAll(): List<FileSaveResult> =
    try {
        val changed = saveCurrentWords()
        saveIgnored()
        changed
    }
    catch (ex: Exception) {
        showErrorAlert(pane, "Error of saving words\n${ex.message}")
        emptyList()
    }


fun LearnWordsController.saveCurrentWords(): List<FileSaveResult> = doSaveCurrentWords { filePath ->
    RecentDocuments().addRecent(filePath.useFilenameSuffix(internalWordCardsFileExt))
    val files: List<FileSaveResult> = saveWordsImpl(currentWords, filePath)
    resetDocumentIsDirty()
    files
}


class FileSaveResult (
    val file: Path,
    val operation: Operation,
) {
    enum class Operation { Saved, Deleted }
}

fun LearnWordsController.saveWordsImpl(words: List<CardWordEntry>, filePath: Path): List<FileSaveResult> {

    val result = mutableListOf<FileSaveResult>()

    val internalFormatFile = filePath.useFilenameSuffix(internalWordCardsFileExt)
    saveWordCards(internalFormatFile, CsvFormat.Internal, words)
    result.add(FileSaveResult(internalFormatFile, FileSaveResult.Operation.Saved))

    val memoWordSubDir = filePath.parent.resolve("_MemoWord").resolve(filePath.baseWordsFilename)
    memoWordSubDir.createDirectories()

    val memoWordFile = memoWordSubDir.resolve(filePath.name).useMemoWordFilenameSuffix()
    val memoWordIntFile = memoWordSubDir.resolve(filePath.name).useInternalFilenameSuffix()

    if (words.size <= maxMemoCardWordCount) {
        saveWordCards(memoWordFile, CsvFormat.MemoWord, words)
        result.add(FileSaveResult(memoWordFile, FileSaveResult.Operation.Saved))

        saveWordCards(memoWordIntFile, CsvFormat.Internal, words)
        result.add(FileSaveResult(memoWordIntFile, FileSaveResult.Operation.Saved))
    }
    else {
        memoWordFile.deleteIfExists()
        memoWordIntFile.deleteIfExists()

        result.add(FileSaveResult(memoWordFile, FileSaveResult.Operation.Deleted))
        result.add(FileSaveResult(memoWordIntFile, FileSaveResult.Operation.Deleted))
        log.info { "Saving MemoWord file ${memoWordFile.name} is skipped since it has too many entries ${words.size} (> $maxMemoCardWordCount)." }
    }

    val splitFilesDir = memoWordSubDir.resolve("split")
    val oldSplitFiles: List<Path> =
        if (splitFilesDir.exists()) {
            val oldSplitFiles: List<Path> = splitFilesDir.listDirectoryEntries("*${filePath.baseWordsFilename}*.csv").sorted()
            if (oldSplitFiles.isNotEmpty())
                log.debug { "### Removing old split files: \n  ${oldSplitFiles.joinToString("\n  ")}" }
            oldSplitFiles.forEach { it.deleteIfExists() }
            oldSplitFiles
        }
        else
            emptyList()

    splitFilesDir.createDirectories()
    val memoFiles = saveSplitWordCards(filePath, words, splitFilesDir, settingsPane.splitWordCountPerFile) //, CsvFormat.MemoWord)

    // without phrases
    val onlyWords = words.filterNot { it.from.containsWhiteSpaceInMiddle() }
    val memoOnlyPureWordsFiles = saveSplitWordCards(
        memoWordSubDir.resolve(filePath.baseWordsFilename + " (OnlyWords).csv"),
        onlyWords, splitFilesDir,
        settingsPane.splitWordCountPerFile) // , CsvFormat.MemoWord)

    result.addAll(memoFiles.map { FileSaveResult(it, FileSaveResult.Operation.Saved) })
    result.addAll(memoOnlyPureWordsFiles.map { FileSaveResult(it, FileSaveResult.Operation.Saved) })

    val fullyRemoved = oldSplitFiles.toSet() - memoFiles.toSet() - memoOnlyPureWordsFiles.toSet()
    result.addAll(fullyRemoved.map { FileSaveResult(it, FileSaveResult.Operation.Deleted) })

    return result
}


fun LearnWordsController.doSaveCurrentWords(saveAction:(Path)->List<FileSaveResult>): List<FileSaveResult> {

    var filePath: Path? = this.currentWordsFile
    if (filePath == null) {
        filePath = showTextInputDialog(pane, "Enter new words filename")
            .map { wordsFilename -> dictDirectory.resolve(wordsFilename.withFileExt(internalWordCardsFileExt)) }
            .orElse(null)
    }

    if (filePath == null) {
        //showErrorAlert(pane, "Filename is not specified.")
        return emptyList()
    }

    val resultFiles = saveAction(filePath)

    // !!! ONLY if success !!!
    updateCurrentWordsFile(filePath)

    return resultFiles
}
