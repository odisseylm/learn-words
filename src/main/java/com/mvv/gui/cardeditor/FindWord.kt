package com.mvv.gui.cardeditor

import com.mvv.gui.javafx.*
import com.mvv.gui.util.lastChar
import com.mvv.gui.util.startsWithOneOf
import com.mvv.gui.words.CardWordEntry
import com.mvv.gui.words.from
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.scene.Node
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import java.util.concurrent.CopyOnWriteArrayList


class FindWord (private val currentWords: TableView<CardWordEntry>)  {

    private val selectEventHandlers = CopyOnWriteArrayList<EventHandler<SelectEvent<CardWordEntry>>>()

    private val editor = TextField().also {
        it.prefColumnCount = 10
        it.onAction = EventHandler { findAndSelectWord() }
    }

    private val goButton = newButton(ImageView("/icons/binocular.png")) { findAndSelectWord() }

    val pane = HBox().also { it.children.addAll(editor, goButton) }

    private fun findAndSelectWord() {
        val toFind = editor.text.lowercase()
        if (toFind.isBlank()) return

        var searchFrom = 0

        if (currentWords.hasSelection) {

            val singleSelectedIndex = currentWords.singleSelectionIndex
            if (singleSelectedIndex != -1 && matchedByPrefix(currentWords.items[singleSelectedIndex], toFind))
                searchFrom = singleSelectedIndex + 1

            if (searchFrom == 0) {
                val firstSelectedIndex: Int = currentWords.selectionModel.selectedIndices.minOrNull() ?: -1
                if (firstSelectedIndex != -1 && matchedByPrefix(currentWords.items[firstSelectedIndex], toFind))
                    searchFrom = firstSelectedIndex + 1
            }
        }

        val fittingCard = findByPrefix(toFind, searchFrom)
        if (fittingCard != null) {
            currentWords.selectItem(fittingCard)

            val selectEvent = SelectEvent(fittingCard, currentWords)
            selectEventHandlers.forEach { it.handle(selectEvent) }
        }
    }

    private fun findByPrefix(toFind: String, searchFrom: Int): CardWordEntry? {
        val asPrefixes = toFindAsPrefixes(toFind)
        val searchByExact = toFind.isNotBlank() && toFind.lastChar == ' '
        val exactToFind = toFind.trim()

        return currentWords.items.asSequence()
            .drop(searchFrom)
            .find { it.from.startsWithOneOf(asPrefixes, ignoreCase = true) ||
                    (searchByExact && it.from.equals(exactToFind, ignoreCase = true)) }
    }
    private fun matchedByPrefix(card: CardWordEntry, toFind: String): Boolean =
        card.from.startsWithOneOf(toFindAsPrefixes(toFind), ignoreCase = true) ||
                (toFind.isNotBlank() && toFind.lastChar == ' ' && card.from.equals(toFind.trim(), ignoreCase = true))
    private fun toFindAsPrefixes(toFind: String) = listOf(toFind, "to $toFind", "to be $toFind")

    fun addSelectEventHandler(handler: EventHandler<SelectEvent<CardWordEntry>>) {
        selectEventHandlers.add(handler)
    }


    class SelectEvent<T>(val item: T, source: Node) : javafx.event.Event(source, source, selectItemEventType) {
        companion object {
            val selectItemEventType = EventType<SelectEvent<Any>>("selectItem")
        }
    }

}
