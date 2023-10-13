package com.mvv.gui

import com.mvv.gui.javafx.newButton
import com.mvv.gui.javafx.selectItem
import com.mvv.gui.words.CardWordEntry
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
        val toFind = editor.text.trim().lowercase()
        if (toFind.isNotEmpty()) {
            val fittingCard = currentWords.items.find { it.from.startsWith(toFind, ignoreCase = true) }
            if (fittingCard != null) {
                currentWords.selectItem(fittingCard)

                val selectEvent = SelectEvent(fittingCard, currentWords)
                selectEventHandlers.forEach { it.handle(selectEvent) }
            }
        }
    }

    fun addSelectEventHandler(handler: EventHandler<SelectEvent<CardWordEntry>>) {
        selectEventHandlers.add(handler)
    }


    class SelectEvent<T>(val item: T, source: Node) : javafx.event.Event(source, source, selectItemEventType) {
        companion object {
            val selectItemEventType = EventType<SelectEvent<Any>>("selectItem")
        }
    }

}
