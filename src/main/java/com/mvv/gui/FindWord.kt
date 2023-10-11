package com.mvv.gui

import com.mvv.gui.javafx.selectItem
import com.mvv.gui.words.CardWordEntry
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.scene.layout.FlowPane


class FindWord (private val currentWords: TableView<CardWordEntry>)  {

    private val editor = TextField().also {
        it.prefColumnCount = 10
        it.onAction = EventHandler { findAndSelectWord() }
    }

    private val goButton = Button("Go").also {
        it.onAction = EventHandler { findAndSelectWord() }
    }

    val pane = FlowPane().also { it.children.addAll(editor, goButton) }

    private fun findAndSelectWord() {
        val toFind = editor.text.trim().lowercase()
        if (toFind.isNotEmpty()) {
            val fittingCard = currentWords.items.find { it.from.startsWith(toFind, ignoreCase = true) }
            if (fittingCard != null)
                currentWords.selectItem(fittingCard)
        }
    }
}
