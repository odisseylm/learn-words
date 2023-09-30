package com.mvv.gui

import com.mvv.gui.javafx.*
import com.mvv.gui.util.toEnumSet
import com.mvv.gui.words.CardWordEntry
import com.mvv.gui.words.WordCardStatus
import javafx.beans.value.ChangeListener
import javafx.scene.control.ComboBox
import javafx.scene.control.TableView
import javafx.scene.layout.FlowPane


class NextPrevWarningWord (private val currentWords: TableView<CardWordEntry>) {
    private val warningsDropDown = ComboBox<WordCardStatusesDropDownEntry>().also {
        it.styleClass.add("warningsDropDown")
        it.items.setAll(allDropDownWarnings())
        it.selectionModel.select(0)
    }

    private val prevButton = newButton(buttonIcon("icons/search_prev.gif")) { prevItemWithWarning() }
    private val nextButton = newButton(buttonIcon("icons/search_next.gif")) { nextItemWithWarning() }
    val pane = FlowPane().also { it.children.addAll(nextButton, warningsDropDown, prevButton) }

    private fun nextItemWithWarning() {
        if (currentWords.items.isEmpty()) return

        val currentItem = currentWords.singleSelection
            ?: currentWords.items.getOrNull(currentWords.visibleRows.first)
            ?: currentWords.items.first()
        selectNextWarnItemImpl(currentWords.items, currentItem)
    }

    private fun prevItemWithWarning() {
        if (currentWords.items.isEmpty()) return

        val currentItem = currentWords.singleSelection
            ?: currentWords.items.getOrNull(currentWords.visibleRows.last)
            ?: currentWords.items.last()
        selectNextWarnItemImpl(currentWords.items.asReversed(), currentItem)
    }

    private fun selectNextWarnItemImpl(items: Iterable<CardWordEntry>, startFrom: CardWordEntry) {
        val warningsToFind = warningsDropDown.selectionModel.selectedItem.warnings
        items
            .asSequence()
            .dropWhile { it !== startFrom }
            .drop(1)
            .find { card -> card.wordCardStatuses.any { it in warningsToFind } } // compare by priority
            ?.also { currentWords.selectItem(it) }
    }

    private fun allDropDownWarnings(): List<WordCardStatusesDropDownEntry> {
        val allPossibleWarnings: Set<WordCardStatus> = WordCardStatus.values()
            .filter { it.isWarning }
            .toEnumSet()

        return listOf(WordCardStatusesDropDownEntry(allPossibleWarnings, " -- Any warning -- ")) +
                allPossibleWarnings.map { WordCardStatusesDropDownEntry(setOf(it), it.shortWarnDescr()) }
    }

    val selectedWarnings: Set<WordCardStatus> get() = warningsDropDown.selectionModel.selectedItem.warnings

    fun addSelectedWarningsChangeListener(l: ChangeListener<Set<WordCardStatus>>) =
        warningsDropDown.selectionModel.selectedItemProperty().addListener { obs,prev,new ->
            l.changed(
                ReadOnlyWrapper<WordCardStatusesDropDownEntry,Set<WordCardStatus>>(obs) { it.warnings },
                prev?.warnings ?: emptySet(),
                new?.warnings ?: emptySet(),
            ) }
}

private class WordCardStatusesDropDownEntry (
    val warnings: Set<WordCardStatus>,
    private val asString: String,
) {
    override fun toString(): String = asString
}

private fun WordCardStatus.shortWarnDescr(): String =
    when (this) {
        WordCardStatus.NoBaseWordInSet -> "No base word"
        WordCardStatus.BaseWordDoesNotExist -> throw IllegalArgumentException("It is not warning.")
        WordCardStatus.NoTranslation -> "No translation"
        WordCardStatus.TranslationIsNotPrepared -> "Translation is not prepared"
        WordCardStatus.TooManyExamples -> "Too many examples"
    }
