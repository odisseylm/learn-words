package com.mvv.gui.cardeditor

import com.mvv.gui.javafx.ToolBarButtonType
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ToolBar


class ToolBar2 (controller: LearnWordsController) : ToolBar() {

    val nextPrevWarningWord = NextPrevWarningWord(controller.currentWordsList)
    private val findWord = FindWord(controller.currentWordsList)
    private val findWordInOtherSets = FindWordInOtherSets(controller)
    private val navigation = NavigationHistoryPane(controller.currentWordsList, controller.navigationHistory, ToolBarButtonType.Small)

    init {
        items.addAll(
            nextPrevWarningWord.pane,
            //Separator(),
            stub(),
            findWord.pane,
            stub(),
            navigation,
            stub(),
            findWordInOtherSets.pane,
        )

        findWord.addSelectEventHandler { controller.navigationHistory.visited(it.item) }
    }

    private fun stub(width: Double = 6.0): Node = Label(" ").also { it.prefWidth = width }
}
