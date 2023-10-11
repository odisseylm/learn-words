package com.mvv.gui

import javafx.scene.control.ToolBar


class ToolBar2 (controller: LearnWordsController) : ToolBar() {

    val nextPrevWarningWord = NextPrevWarningWord(controller.currentWordsList)
    private val findWord = FindWord(controller.currentWordsList)

    init {
        items.addAll(
            nextPrevWarningWord.pane,
            findWord.pane,
        )
    }
}
