package com.mvv.gui

import com.mvv.gui.javafx.*
import com.mvv.gui.javafx.ExTextFieldTableCell.TextFieldType
import com.mvv.gui.util.addOnceEventHandler
import com.mvv.gui.words.CardWordEntry
import com.mvv.gui.words.baseWordsFilename
import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.TableColumn
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.stage.Window
import javafx.stage.WindowEvent
import javafx.util.Callback
import java.nio.file.Path



class OtherCardsViewPopup :
    javafx.stage.Stage(javafx.stage.StageStyle.UNDECORATED) {
    //javafx.scene.control.PopupControl() {

    private val controller = LearnWordsController(isReadOnly = true) // TODO: use light version without modification logic
    private val cardsTable = OtherWordCardsTable(controller).also { it.id = "currentWords" }

    private val wordLabel = Label().also { it.styleClass.add("cardDuplicatesTitle") }
    private val content = BorderPane(cardsTable).also { it.styleClass.add("cardDuplicatesContainer") }
    private val titleBar: Node

    init {
        //styleClass.add("cardDuplicatesPopup")

        val closeButton = newButton(ImageView("icons/cross(5).png")) { this.hide() }
            .also { it.style = " -fx-padding: 0; -fx-background-color: transparent; -fx-border-insets: 0; -fx-border-width: 0; " }

        titleBar = BorderPane().also { title ->
            title.left  = wordLabel
            title.right = closeButton
        }

        content.top = titleBar

        content.maxWidth  = 500.0
        content.maxHeight = 200.0

        this.sceneRoot = content

        // temp
        //val w: Any = this
        //@Suppress("KotlinConstantConditions")
        //if (w is javafx.stage.Stage) {
        //    w.isResizable = true
        //}

        sizeToScene()

        addWindowMovingFeature(this, titleBar)
        addWindowResizingFeature(this, content)
    }

    var cards: List<CardWordEntry>
        get() = cardsTable.items
        set(value) { cardsTable.items.setAll(value) }

    var word: String
        get() = wordLabel.properties["word"] as String? ?: ""
        set(value) { wordLabel.properties["word"] = value; wordLabel.text = "Word '$value' already exists in other sets" }

    fun show(parentWindow: Window, wordOrPhrase: String, cards: List<SearchEntry>, getPos: ()-> Point2D) {

        this.word  = wordOrPhrase
        this.cards = cards.map { it.card }

        // for non-popup impl
        if (this.isResizable && this.width > 100 || this.height > 20) {

            val prevW = this.width; val prevH = this.height

            this.show() // Stage.show() sets size to pref size

            // let's keep old bounds (or otherwise, lets change x/y too)
            this.width = prevW; this.height = prevH
            return
        }

        if (this.width > 0 || this.height > 0) {
            this.show(parentWindow, getPos())
            return
        }

        // I do not know how to calculate desired x/y at this time, because
        // I do not know how to calculate 'calculated' pref size
        // and sizeToScene() does not work till window is showing (in contrast with Swing Window.pack() which works before real showing window)
        content.prefWidth  = 1.0
        content.prefHeight = 1.0

        addOnceEventHandler(WindowEvent.WINDOW_SHOWN) {

            val useComputedSize = javafx.scene.layout.Region.USE_COMPUTED_SIZE
            content.prefWidth  = useComputedSize
            content.prefHeight = useComputedSize

            sizeToScene()

            val pos = getPos()
            x = pos.x
            y = pos.y
        }

        show(parentWindow, getPos())
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
