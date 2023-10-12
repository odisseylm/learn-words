package com.mvv.gui

import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ToolBar


class ToolBar2 (controller: LearnWordsController) : ToolBar() {

    val nextPrevWarningWord = NextPrevWarningWord(controller.currentWordsList)
    private val findWord = FindWord(controller.currentWordsList)
    private val navigation = NavigationHistoryPane(controller.currentWordsList)

    init {
        items.addAll(
            nextPrevWarningWord.pane,
            //Separator(),
            stub(),
            findWord.pane,
            stub(),
            navigation.pane,
        )

        findWord.addSelectEventHandler { navigation.visited(it.item) }
    }

    private fun stub(width: Double = 6.0): Node = Label(" ").also { it.prefWidth = width }
}
