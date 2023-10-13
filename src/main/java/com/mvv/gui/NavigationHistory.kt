package com.mvv.gui

import com.mvv.gui.javafx.newButton
import com.mvv.gui.javafx.selectItem
import com.mvv.gui.javafx.singleSelection
import com.mvv.gui.words.CardWordEntry
import javafx.application.Platform
import javafx.beans.InvalidationListener
import javafx.collections.ListChangeListener
import javafx.scene.control.TableView
import javafx.scene.image.ImageView
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.input.TouchEvent
import javafx.scene.layout.HBox


class NavigationHistory {
    private val maxSize = 50
    // T O D O: find better collection with auto-removing 1st unneeded elements (probably some kind of queue)
    private val list: MutableList<CardWordEntry> = mutableListOf()
    private var currentPos: Int = -1

    fun clear() {
        list.clear()
        currentPos = -1
    }

    fun removeFromHistory(removed: List<CardWordEntry>) {
        // I use this approach with iterators to remove DUPLICATED entries in history list.
        val historyIt = this.list.iterator()
        while (historyIt.hasNext()) {
            val v = historyIt.next()
            if (v in removed) { // probably it can be optimized if convert 'removed' to HashSet, but I'm not sure that it really makes sense
                historyIt.remove()
                currentPos--    // most probably too simple approach
            }
        }
    }

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

    fun hasPrev(): Boolean = currentPos > 0
    fun hasNext(): Boolean = currentPos < list.size - 1
}


class NavigationHistoryPane (private val currentWords: TableView<CardWordEntry>) {
    private val history = NavigationHistory()
    private val prevButton = newButton("", "Back: Go to previous selected card", ImageView("/icons/backward_nav.png")) { toPrev() }
    private val nextButton = newButton("", "Forward: Go to next selected card", ImageView("/icons/forward_nav.png"))  { toNext() }

    val pane = HBox().also { it.children.addAll(prevButton, nextButton) }

    init {
        currentWords.items.addListener(InvalidationListener { Platform.runLater { enableDisable() } })
        currentWords.items.addListener(ListChangeListener   { Platform.runLater { enableDisable(); removeFromHistory(it.removed) } })

        enableDisable()
    }

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
        currentWords.singleSelection?.also {
            history.visited(it)
            enableDisable()
        }
    }

    private fun toPrev() {
        val prev = history.visitPrev()
        currentWords.selectItem(prev)
        enableDisable()
    }

    private fun toNext() {
        val next = history.visitNext()
        currentWords.selectItem(next)
        enableDisable()
    }

    fun visited(entry: CardWordEntry) {
        history.visited(entry)
        enableDisable()
    }

    private fun enableDisable() {
        if (currentWords.items.isEmpty()) history.clear()

        prevButton.isDisable = !history.hasPrev()
        nextButton.isDisable = !history.hasNext()
    }

    private fun removeFromHistory(removed: List<CardWordEntry>) {
        history.removeFromHistory(removed)
        enableDisable()
    }
}
