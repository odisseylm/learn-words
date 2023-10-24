package com.mvv.gui

import com.mvv.gui.javafx.*
import com.mvv.gui.words.CardWordEntry
import javafx.application.Platform
import javafx.collections.ListChangeListener
import javafx.scene.control.Button
import javafx.scene.control.TableView
import javafx.scene.image.ImageView
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.input.TouchEvent
import javafx.scene.layout.HBox



enum class NavigationDirection { Back, Forward }


@Suppress("MemberVisibilityCanBePrivate")
class NavigationHistoryState (
    val visited: List<CardWordEntry>,
    val currentPos: Int,
) {
    override fun toString(): String = "NavigationHistoryState { currentPos: $currentPos, size: ${visited.size} }"
}


class NavigationHistory : AbstractObservable<NavigationHistoryState>() {
    private val maxSize = 50
    // T O D O: find better collection with auto-removing 1st unneeded elements (probably some kind of queue)
    private val list: MutableList<CardWordEntry> = mutableListOf()
    private var currentPos: Int = -1

    override fun getValue(): NavigationHistoryState = NavigationHistoryState(this.list, this.currentPos)

    fun clear() {
        val historyUpdated = list.isNotEmpty() || currentPos != -1

        list.clear()
        currentPos = -1

        if (historyUpdated) fireValueChangedEvent()
    }

    fun removeFromHistory(removed: List<CardWordEntry>) {
        var historyUpdated = false

        // I use this approach with iterators to remove DUPLICATED entries in history list.
        val historyIt = this.list.iterator()
        while (historyIt.hasNext()) {
            val v = historyIt.next()
            if (v in removed) { // probably it can be optimized if convert 'removed' to HashSet, but I'm not sure that it really makes sense
                historyIt.remove()
                currentPos--    // most probably too simple approach
                historyUpdated = true
            }
        }

        if (currentPos < -1) currentPos = -1

        if (historyUpdated) fireValueChangedEvent()
    }

    fun visited(entry: CardWordEntry) {
        var historyUpdated = false

        if (list.isEmpty() || list.last() !== entry) {
            list.add(entry)
            historyUpdated = true
        }

        while (list.size > maxSize) {
            list.removeAt(0)
            historyUpdated = true
        }

        if (currentPos != list.size - 1) {
            currentPos = list.size - 1
            historyUpdated = true
        }

        if (historyUpdated) fireValueChangedEvent()
    }

    fun visitBack(): CardWordEntry {
        var historyUpdated = false

        if (currentPos > 0) {
            currentPos--
            historyUpdated = true
        }

        val prevEntry = list[currentPos]

        if (historyUpdated) fireValueChangedEvent()
        return prevEntry
    }

    fun visitForward(): CardWordEntry {
        var historyUpdated = false

        if (currentPos < list.size - 1) {
            currentPos++
            historyUpdated = true
        }

        val nextEntry = list[currentPos]

        if (historyUpdated) fireValueChangedEvent()
        return nextEntry
    }

    fun canVisitBack(): Boolean = currentPos > 0
    fun canVisitForward(): Boolean = currentPos < list.size - 1
}




fun navigateToCard(direction: NavigationDirection, history: NavigationHistory, cardsTable: TableView<CardWordEntry>) {
    when (direction) {
        NavigationDirection.Back ->
            if (history.canVisitBack()) {
                val prev = history.visitBack()
                cardsTable.selectItem(prev)
            }

        NavigationDirection.Forward ->
            if (history.canVisitForward()) {
                val next = history.visitForward()
                cardsTable.selectItem(next)
            }
    }
}


fun installNavigationHistoryUpdates(cardsTable: TableView<CardWordEntry>, history: NavigationHistory) {
    cardsTable.items.addListener(ListChangeListener {
        if (cardsTable.items.isEmpty())
            history.clear()
        history.removeFromHistory(it.removed)
    })

    fun updateLastSelectedByUserAction() = cardsTable.singleSelection?.also { history.visited(it) }

    cardsTable.addEventHandler(MouseEvent.MOUSE_PRESSED)  { updateLastSelectedByUserAction() }
    cardsTable.addEventHandler(MouseEvent.MOUSE_RELEASED) { updateLastSelectedByUserAction() }
    cardsTable.addEventHandler(MouseEvent.MOUSE_CLICKED)  { updateLastSelectedByUserAction() }
    cardsTable.addEventHandler(TouchEvent.TOUCH_PRESSED)  { updateLastSelectedByUserAction() }
    cardsTable.addEventHandler(TouchEvent.TOUCH_RELEASED) { updateLastSelectedByUserAction() }
    cardsTable.addEventHandler(KeyEvent.KEY_PRESSED)      { updateLastSelectedByUserAction() }
    cardsTable.addEventHandler(KeyEvent.KEY_RELEASED)     { updateLastSelectedByUserAction() }
}


class NavigationHistoryPane (
    private val cardsTable: TableView<CardWordEntry>,
    private val history: NavigationHistory,
    private val toolBarButtonType: ToolBarButtonType,
    ) : HBox() {

    private val backButton = toolBarButton("Back", "Back: Go to previous selected card",
        ImageView("/icons/backward_nav.png"), ImageView("/icons/big/left-yellow-01.png")) {
            navigateToCard(NavigationDirection.Back, history, cardsTable) }
    private val forwardButton = toolBarButton("Forward", "Forward: Go to next selected card",
        ImageView("/icons/forward_nav.png"), ImageView("/icons/big/right-yellow-01.png")) {
            navigateToCard(NavigationDirection.Forward, history, cardsTable) }

    init {
        children.addAll(backButton, forwardButton)
        history.addListener {_,_,_ -> Platform.runLater { enableDisable() } }
        enableDisable()
    }

    private fun toolBarButton(label: String, toolTip: String,
                              smallImage: ImageView, middleImage: ImageView,
                              action: ()->Unit): Button =
        when (toolBarButtonType) {
            ToolBarButtonType.Small  -> newButton("", toolTip, smallImage, action)
            ToolBarButtonType.Middle -> new24xButton(label, toolTip, middleImage, action)
        }

    private fun enableDisable() {
        backButton.isDisable = !history.canVisitBack()
        forwardButton.isDisable = !history.canVisitForward()
    }
}
