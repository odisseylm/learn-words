package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.*
import com.mvv.gui.javafx.runWithScrollKeeping
import com.mvv.gui.javafx.showConfirmation
import com.mvv.gui.javafx.singleSelection
import com.mvv.gui.words.*
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.control.ButtonType
import java.nio.file.Path
import kotlin.io.path.exists


fun LearnWordsController.copySelectToOtherSet() = doAction("copying selected cards to other set file") { copySelectToOtherSetImpl() }


fun LearnWordsController.showInternalFormatOpenDialog(editablePath: Boolean): Path? {

    val currentWordsFile = this.currentWordsFile // local safe ref

    val showFilesMode = if (editablePath) ShowFilesMode.Combo else ShowFilesMode.List
    val destFilePath = showCopyToOtherSetDialog(pane, currentWordsFile, allWordCardSetsManager, showFilesMode) ?: return null

    require(destFilePath.isInternalCsvFormat) {
        "It looks strange to load/save data in memo-word format [$destFilePath]." }

    return destFilePath
}


private fun LearnWordsController.copySelectToOtherSetImpl() {

    val selected = currentWordsSelection.selectedItems
    if (selected.isEmpty()) return

    // TODO: make list dialog editable too
    val destFilePath = showInternalFormatOpenDialog(false) ?: return

    copyToOtherSetImpl(destFilePath, selected)
}


private fun LearnWordsController.copyToOtherSetImpl(destFilePath: Path, toCopy: List<CardWordEntry>) {

    val existentCards = if (destFilePath.exists()) loadWordCards(destFilePath) else emptyList()

    val duplicates = verifyDuplicates(existentCards, toCopy)

    val cardToSave: List<CardWordEntry> =
        if (duplicates.duplicatedByOnlyFrom.isNotEmpty()) {
            val similarFrom = duplicates.duplicatedByOnlyFrom.map { it.from }.sorted()
            val showDuplicateCount = 2 //7
            val showDuplicatePadding = "  "
            val similarFromStr = similarFrom
                .joinToString("\n", "", "", showDuplicateCount, "$showDuplicatePadding...") {
                    "$showDuplicatePadding'$it'" }

            val mergeButton = ButtonType("Merge", ButtonData.OTHER)
            val skipButton  = ButtonType("Skip", ButtonData.OTHER)

            val res = showConfirmation(currentWordsList,
                "This set already contains similar ${duplicates.duplicatedByOnlyFrom.size} cards:\n" +
                    "${similarFromStr}\n\n" +
                    "Do you want to merge them or skip?",
                "Copy cards to other set",
                ButtonType.CANCEL, mergeButton, skipButton,
            )

            if (res.isEmpty || res.get() == ButtonType.CANCEL) return

            when (res.get()) {
                skipButton  -> existentCards + toCopy.skipCards(duplicates.fullDuplicates, duplicates.duplicatedByOnlyFrom)
                mergeButton -> mergeCards(existentCards, toCopy.skipCards(duplicates.fullDuplicates))
                else        -> throw IllegalStateException("Unexpected button [${res.get()}]")
            }
        }
        else existentCards + toCopy.skipCards(duplicates.fullDuplicates)

    saveWordsImpl(cardToSave, destFilePath)
}


fun LearnWordsController.exportSelectFromOtherSet() = doAction("exporting selected card from other set", this::exportSelectFromOtherSetImpl)


private fun LearnWordsController.exportSelectFromOtherSetImpl() {

    val selected: CardWordEntry = currentWordsList.singleSelection
        ?: throw IllegalStateException("No single selection.")

    val found: List<AllCardWordEntry> = allWordCardSetsManager.findBy(selected.from, MatchMode.Exact)

    val cardToExport = when {
        found.isEmpty() -> null
        found.size == 1 -> found.first()
        else -> showSelectionCardDialog(appContext, pane, "Select card, please", found)
    }

    if (cardToExport != null) {
        cardToExport.copyBasePropsTo(selected)
        currentWordsList.runWithScrollKeeping {
            // T O D O: actually it should work without that??!!
            // Need this hack to increase/refresh rendered row height.
            currentWordsList.refresh()
        }
    }
}


internal fun List<CardWordEntry>.skipCards(toSkip: Set<CardWordEntry>): List<CardWordEntry> =
    this.filterNot { it in toSkip }
internal fun List<CardWordEntry>.skipCards(toSkip1: Set<CardWordEntry>, toSkip2: Set<CardWordEntry>): List<CardWordEntry> =
    this.filterNot { it in toSkip1 || it in toSkip2 }

internal fun mergeCards(existentCards: List<CardWordEntry>, newCards: List<CardWordEntry>): List<CardWordEntry> {
    val all = (existentCards + newCards).groupBy { it.from.lowercase() }
    return all.values.map { cardsWitTheSameFrom -> mergeCards(cardsWitTheSameFrom).also { it.from = cardsWitTheSameFrom.first().from } }
}


internal fun LearnWordsController.addToFrom(cardsGroup: CardsGroup) {

    val card = currentWordsList.singleSelection ?: return
    val chosenDict: Path? = chooseDictionaryDialog(pane, currentWordsFile, allWordCardSetsManager, cardsGroup.groupName,
        cardsGroup.directory, cardsGroup.fileBelongsToGroup)

    if (chosenDict != null)
        copyToOtherSetImpl(chosenDict, listOf(card))
}
