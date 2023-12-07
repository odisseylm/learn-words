package com.mvv.gui.cardeditor

import com.mvv.gui.javafx.RelocationPolicy
import com.mvv.gui.javafx.sceneRoot
import com.mvv.gui.javafx.showPopup
import javafx.event.EventHandler
import javafx.geometry.Point2D
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.control.PopupControl
import javafx.scene.layout.BorderPane
import javafx.scene.text.Text
import javafx.scene.text.TextFlow


class SynonymsPopup (val controller: LearnWordsController) : PopupControl() {

    private var synonyms: List<SearchEntry> = emptyList()

    private val textFlow  = TextFlow()
    private val wordLabel = Label("").also { it.style = "-fx-font-weight: bold;" }

    private val caption = TextFlow(
        Label("Synonyms of '"),
        wordLabel,
        Label("'"),
    ).also {
        it.styleClass.add("synonymsPopupCaption")
    }

    private val showInOtherButton = Hyperlink("Show with details").also { btn ->
        btn.onAction = EventHandler {
            this.hide()
            controller.showInOtherSetsPopup("Synonyms of '${wordLabel.text}'", synonyms.map { it })
        }
        btn.isFocusTraversable = false
    }

    private val content = BorderPane().also {
        it.styleClass.add("synonymsPopupContent")
        it.top = caption
        it.center = textFlow
        it.bottom = showInOtherButton

        BorderPane.setAlignment(showInOtherButton, Pos.BOTTOM_RIGHT)
    }

    val wordOrPhrase: String get() = wordLabel.text

    init {
        this.sceneRoot = content
        //this.setFocusable(false)
        //content.isDisable = true
        sizeToScene()
    }

    private fun setSynonyms(synonyms: List<SearchEntry>) {
        this.synonyms = synonyms

        textFlow.children.clear()
        synonyms.forEach {
            textFlow.children.add(Text(it.card.from))
            textFlow.children.add(Text("\n"))
        }
    }

    fun show(parent: Node, wordOrPhrase: String, synonyms: List<SearchEntry>, getPos: () -> Point2D) {
        wordLabel.text = wordOrPhrase
        setSynonyms(synonyms)

        content.requestLayout()
        this.showPopup(parent, RelocationPolicy.AlwaysRecalculate, getPos)
    }
}
