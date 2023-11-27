package com.mvv.gui

import com.mvv.gui.javafx.*
import com.mvv.gui.javafx.ExTextFieldTableCell.TextFieldType
import com.mvv.gui.words.CardWordEntry
import com.mvv.gui.words.baseWordsFilename
import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.PopupControl
import javafx.scene.control.TableColumn
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Region
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.stage.Window
import javafx.util.Callback
import java.nio.file.Path


class OtherCardsViewPopup :
    javafx.stage.Stage(javafx.stage.StageStyle.UNDECORATED) {
    //javafx.scene.control.PopupControl() {

    private val controller = LearnWordsController(isReadOnly = true) // TODO: use light version without modification logic
    private val cardsTable = OtherWordCardsTable(controller).also { it.id = "currentWords" }

    private val captionLabel = Label().also { it.styleClass.add("cardDuplicatesTitle") }
    private val content = BorderPane(cardsTable).also { it.styleClass.add("cardDuplicatesContainer") }
    val contentComponent: Region = content
    private val titleBar: Node

    init {
        //styleClass.add("cardDuplicatesPopup")

        val closeButton = newButton(ImageView("icons/cross(5).png")) { this.hide() }
            .also { it.style = " -fx-padding: 0; -fx-background-color: transparent; -fx-border-insets: 0; -fx-border-width: 0; " }

        titleBar = BorderPane().also { title ->
            title.left  = captionLabel
            title.right = closeButton
        }

        content.top = titleBar

        content.maxWidth  = 500.0
        content.maxHeight = 200.0

        this.sceneRoot = content

        sizeToScene()

        addWindowMovingFeature(this, titleBar)
        addWindowResizingFeature(this, content)
    }

    var cards: List<CardWordEntry>
        get() = cardsTable.items
        set(value) { cardsTable.items.setAll(value) }

    var caption: String
        get() = captionLabel.text
        set(value) { captionLabel.text = value }

    fun show(parentWindow: Window, title: String, cards: List<SearchEntry>, getPos: ()-> Point2D) {

        this.caption = title
        this.cards   = cards.map { it.card }

        this.showPopup(parentWindow, RelocationPolicy.CalculateOnlyOnce, getPos)
    }
}


private class OtherWordCardsTable(controller: LearnWordsController) : WordCardsTable(controller) {

    val fileColumn = TableColumn<CardWordEntry, Path?>("File")

    init {
        fileColumn.id = "file"
        fileColumn.isEditable = false
        fileColumn.cellValueFactory = Callback { p -> p.value.fileProperty }
        fileColumn.cellFactory = ExTextFieldTableCell.forTableColumn(
            TextFieldType.TextField,
            DelegateStringConverter { it?.baseWordsFilename ?: "" },
            ToolTipMode.ShowAllContent)
        fileColumn.prefWidth = 150.0

        isEditable = false

        columns.setAll(
            numberColumn,
            fromColumn,
            fileColumn,
            transcriptionColumn,
            translationCountColumn,
            toColumn,
            exampleCountColumn,
            examplesColumn,
        )
    }
}


class LightOtherCardsViewPopup : PopupControl() {
    private val content = BorderPane().also {
        it.styleClass.add("otherCardsViewPopupContent")
    }

    private val textFlow = TextFlow()
        .also { content.center = it }

    init {
        this.sceneRoot = content
        sizeToScene()
    }

    fun show(parentWindow: Window, wordOrPhrase: String, cards: List<SearchEntry>, getPos: () -> Point2D) {

        textFlow.children.clear()

        cards.forEach { card ->
            if (textFlow.children.isNotEmpty()) textFlow.children.add(Text("\n"))

            textFlow.children.addAll(
                Label("Word [$wordOrPhrase] is already present in the set '"),
                Label(card.file.baseWordsFilename).also { it.style = "-fx-font-weight: bold;" },
                Label("'"),
            )
        }

        content.requestLayout()

        this.showPopup(parentWindow, RelocationPolicy.AlwaysRecalculate, getPos)
    }
}
