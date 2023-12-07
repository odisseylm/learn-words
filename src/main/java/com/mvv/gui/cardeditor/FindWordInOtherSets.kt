package com.mvv.gui.cardeditor

import com.mvv.gui.javafx.newButton
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import java.util.concurrent.CompletableFuture


class FindWordInOtherSets (private val controller: LearnWordsController) {

    private val editor = TextField().also {
        it.prefColumnCount = 10
        it.onAction = EventHandler { findAndShow() }
    }

    private val matchDropDown = ComboBox<MatchMode>().also {
        it.items.addAll(MatchMode.values())
        it.selectionModel.selectFirst()
    }

    private val goButton = newButton(ImageView("/icons/magnifierzoom.png")) { findAndShow() }

    val pane = HBox().also { it.children.addAll(editor, matchDropDown, goButton) }

    private fun findAndShow() {
        findAndShow(matchDropDown.value)
    }

    private fun findAndShow(mode: MatchMode) {
        // Actually search by 'exact' match is fast and no need for async operations,
        // but using other matching types can require some time (they are not optimized)
        //
        CompletableFuture.runAsync {
            val found = controller.allWordCardSetsManager.findBy(editor.text, mode)

            if (found.isNotEmpty()) {
                Platform.runLater { controller.showSpecifiedWordsInOtherSetsPopup(editor.text, found) }
            }
        }
    }
}
