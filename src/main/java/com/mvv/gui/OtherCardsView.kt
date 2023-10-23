package com.mvv.gui

import com.mvv.gui.javafx.DelegateStringConverter
import com.mvv.gui.javafx.ExTextFieldTableCell
import com.mvv.gui.javafx.ExTextFieldTableCell.TextFieldType
import com.mvv.gui.javafx.ToolTipMode
import com.mvv.gui.javafx.newButton
import com.mvv.gui.util.addOnceEventHandler
import com.mvv.gui.words.CardWordEntry
import com.mvv.gui.words.baseWordsFilename
import javafx.geometry.Point2D
import javafx.scene.control.Label
import javafx.scene.control.PopupControl
import javafx.scene.control.TableColumn
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.stage.Window
import javafx.stage.WindowEvent
import javafx.util.Callback
import java.nio.file.Path


private class OtherCardsView : BorderPane() {

    private val controller = LearnWordsController() // TODO: use light version without modification logic
    private val cardsTable = OtherWordCardsTable(controller).also { it.id = "currentWords" }

    init {
        styleClass.add("cardDuplicatesContainer")
        center = cardsTable
    }

    var cards: List<CardWordEntry>
        get() = cardsTable.items
        set(value) { cardsTable.items.setAll(value) }
}


class OtherCardsViewPopup : PopupControl() {

    private val otherCardsView = OtherCardsView()
    private val wordLabel = Label().also { it.styleClass.add("cardDuplicatesTitle") }
    private val content = BorderPane(otherCardsView)

    init {
        styleClass.add("cardDuplicatesPopup")

        val closeButton = newButton(ImageView("icons/cross(5).png")) { this.hide() }
            .also { it.style = " -fx-padding: 0; -fx-background-color: transparent; -fx-border-insets: 0; -fx-border-width: 0; " }
        content.top = BorderPane().also { title ->
            title.left  = wordLabel
            title.right = closeButton
        }

        content.maxWidth  = 500.0
        content.maxHeight = 200.0

        scene.root = content
        sizeToScene()
    }

    var cards: List<CardWordEntry>
        get() = otherCardsView.cards
        set(value) { otherCardsView.cards = value }

    var word: String
        get() = wordLabel.properties["word"] as String? ?: ""
        set(value) { wordLabel.properties["word"] = value; wordLabel.text = "Word '$value' already exists in other sets" }

    fun show(parentWindow: Window, wordOrPhrase: String, cards: List<SearchEntry>, getPos: ()-> Point2D) {

        this.word  = wordOrPhrase
        this.cards = cards.map { it.card }

        if (this.width > 0 || this.height > 0) {
            val pos = getPos()
            this.show(parentWindow, pos.x, pos.y)
            return
        }

        // I do not know how to calculate desired x/y at this time, because
        // I do not know how to calculate 'calculated' pref size
        // and sizeToScene() does not work till window is showing (in contrast with Swing Window.pack() which works before real showing window)
        content.prefWidth  = 1.0
        content.prefHeight = 1.0

        addOnceEventHandler(WindowEvent.WINDOW_SHOWN) {

            content.prefWidth  = USE_COMPUTED_SIZE
            content.prefHeight = USE_COMPUTED_SIZE

            sizeToScene()

            val pos = getPos()
            x = pos.x
            y = pos.y
        }

        val pos = getPos()
        show(parentWindow, pos.x, pos.y)
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
