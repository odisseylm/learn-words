package com.mvv.gui

import com.mvv.gui.javafx.newButton
import com.mvv.gui.javafx.selectItem
import com.mvv.gui.javafx.singleSelection
import com.mvv.gui.words.CardWordEntry
import javafx.application.Platform
import javafx.scene.control.TableView
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.input.TouchEvent
import javafx.scene.layout.HBox


class NavigationHistory {
    private val maxSize = 50
    // T O D O: find better collection with auto-removing 1st unneeded elements (probably some kind of queue)
    private val list: MutableList<CardWordEntry> = mutableListOf()
    private var currentPos: Int = -1

    fun visited(entry: CardWordEntry) {
        if (list.isEmpty() || list.last() !== entry)
            list.add(entry)

        while (list.size > maxSize)
            list.removeAt(0)

        currentPos = list.size - 1
    }

    fun visitPrev(): CardWordEntry {
        if (currentPos > 0)
            currentPos--

        return list[currentPos]
    }

    fun visitNext(): CardWordEntry {
        if (currentPos < list.size - 1)
            currentPos++

        return list[currentPos]
    }
}


class NavigationHistoryPane (private val currentWords: TableView<CardWordEntry>) {
    private val history = NavigationHistory()
    private val prevButton = newButton("<<") { toPrev() }
    private val nextButton = newButton(">>") { toNext() }

    val pane = HBox().also { it.children.addAll(prevButton, nextButton) }

    init {
        currentWords.addEventHandler(MouseEvent.MOUSE_PRESSED)  { updateLastSelectedByUserAction() }
        currentWords.addEventHandler(MouseEvent.MOUSE_RELEASED) { updateLastSelectedByUserAction() }
        currentWords.addEventHandler(MouseEvent.MOUSE_CLICKED)  { updateLastSelectedByUserAction() }
        currentWords.addEventHandler(TouchEvent.TOUCH_PRESSED)  { updateLastSelectedByUserAction() }
        currentWords.addEventHandler(TouchEvent.TOUCH_RELEASED) { updateLastSelectedByUserAction() }
        currentWords.addEventHandler(KeyEvent.KEY_PRESSED)      { updateLastSelectedByUserAction() }
        currentWords.addEventHandler(KeyEvent.KEY_RELEASED)     { updateLastSelectedByUserAction() }
    }

    private fun updateLastSelectedByUserAction() = Platform.runLater {
        currentWords.singleSelection?.also { history.visited(it) }
    }

    private fun toPrev() {
        val prev = history.visitPrev()
        currentWords.selectItem(prev)
    }

    private fun toNext() {
        val next = history.visitNext()
        currentWords.selectItem(next)
    }

    fun visited(entry: CardWordEntry) {
        history.visited(entry)
    }
}
