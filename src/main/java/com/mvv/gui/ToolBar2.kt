package com.mvv.gui

import javafx.scene.control.ToolBar


class ToolBar2 (controller: LearnWordsController) : ToolBar() {

    private val nextPrevWarningWord = NextPrevWarningWord(controller.currentWordsList)

    init {
        items.add(nextPrevWarningWord.pane)
    }
}
