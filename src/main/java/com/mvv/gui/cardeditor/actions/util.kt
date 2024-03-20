package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.javafx.showInfoAlert


internal fun LearnWordsController.doAction(actionName: String, action: ()->Unit) {
    try { action() }
    catch (ex: Exception) {
        log.error(ex) { "Error of $actionName." }
        showInfoAlert(pane, "Error of $actionName.\n\n${ex.message}")
    }
}
