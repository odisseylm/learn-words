package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.javafx.showInfoAlert
import com.mvv.gui.words.createdAt
import com.mvv.gui.words.updatedAt
import java.time.ZonedDateTime


internal fun LearnWordsController.doAction(actionName: String, action: ()->Unit) {
    try { action() }
    catch (ex: Exception) {
        log.error(ex) { "Error of $actionName." }
        showInfoAlert(pane, "Error of $actionName.\n\n${ex.message}")
    }
}


internal fun LearnWordsController.fixTimestamps() =
    currentWords.forEach { card ->
        val now = ZonedDateTime.now()
        if (card.createdAt == null) card.createdAt = now
        if (card.updatedAt == null) card.updatedAt = now
    }
