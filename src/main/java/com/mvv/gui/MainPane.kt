package com.mvv.gui

import com.mvv.gui.dictionary.Dictionary
import com.mvv.gui.javafx.EmptyTextStringConverter
import com.mvv.gui.javafx.ExTextFieldTableCell
import com.mvv.gui.javafx.LabelStatusTableCell
import com.mvv.gui.javafx.toolTipText
import com.mvv.gui.util.trimToNull
import com.mvv.gui.words.*
import com.mvv.gui.words.WarnAboutMissedBaseWordsMode.NotWarnWhenSomeBaseWordsPresent
import com.mvv.gui.words.WarnAboutMissedBaseWordsMode.WarnWhenSomeBaseWordsMissed
import com.mvv.gui.words.WordCardStatus.*
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.util.Callback
import javafx.util.StringConverter
import org.apache.commons.lang3.NotImplementedException


const val appTitle = "Words"

//private val log = mu.KotlinLogging.logger {}


class MainWordsPane : BorderPane() /*GridPane()*/ {

    internal val currentWordsLabel = Text("File/Clipboard")
    internal val ignoredWordsLabel = Text("Ignored words")
    internal val allProcessedWordsLabel = Text("All processed words")

    internal val topPane = VBox()
    internal val toolBar = ToolBar()

    internal val currentWordsList = TableView<CardWordEntry>()
    internal val fromColumn = TableColumn<CardWordEntry, String>("English")
    private val wordCardStatusesColumn = TableColumn<CardWordEntry, Set<WordCardStatus>>("St")
    internal val toColumn = TableColumn<CardWordEntry, String>("Russian")
    private val translationCountColumn = TableColumn<CardWordEntry, Int>("n")
    internal val transcriptionColumn = TableColumn<CardWordEntry, String>("transcription")
    internal val examplesColumn = TableColumn<CardWordEntry, String>("examples")

    internal val ignoredWordsList = ListView<String>()
    internal val allProcessedWordsList = ListView<String>()

    internal val removeIgnoredButton = Button("Remove ignored")

    internal val warnAboutMissedBaseWordsModeDropDown = ComboBox<WarnAboutMissedBaseWordsMode>().also {
        it.items.setAll(com.mvv.gui.words.WarnAboutMissedBaseWordsMode.values().toList())
        it.value = WarnWhenSomeBaseWordsMissed

        it.converter = object : StringConverter<WarnAboutMissedBaseWordsMode>() {
            override fun toString(v: WarnAboutMissedBaseWordsMode): String = when (v) {
                WarnWhenSomeBaseWordsMissed     -> "Warn when at least one base word missed"
                NotWarnWhenSomeBaseWordsPresent -> "Do not warn when at least one base word is present"
            }

            override fun fromString(string: String?): WarnAboutMissedBaseWordsMode = throw NotImplementedException("Should not be used!")
        }
    }


    internal lateinit var dictionary: Dictionary // TODO: refactor to avoid this logic inside pane

    init {

        val contentPane = GridPane()

        contentPane.alignment = Pos.CENTER
        contentPane.hgap = 10.0; contentPane.vgap = 10.0
        contentPane.padding = Insets(10.0, 10.0, 10.0, 10.0)

        val currentWordsListLabels = BorderPane()

        currentWordsListLabels.left = currentWordsLabel
        setAlignment(currentWordsLabel, Pos.BOTTOM_LEFT)

        currentWordsListLabels.right = warnAboutMissedBaseWordsModeDropDown
        contentPane.add(currentWordsListLabels, 0, 0)

        contentPane.add(currentWordsList, 0, 1, 1, 3)
        GridPane.setFillWidth(currentWordsList, true)
        GridPane.setHgrow(currentWordsList, Priority.ALWAYS)
        currentWordsList.selectionModel.selectionMode = SelectionMode.MULTIPLE


        this.top = topPane
        topPane.children.add(toolBar)

        val buttonsMiddleBar = VBox(5.0)
        buttonsMiddleBar.isFillWidth = true

        removeIgnoredButton.styleClass.add("middleBarButton")
        buttonsMiddleBar.children.add(removeIgnoredButton)

        contentPane.add(buttonsMiddleBar, 1, 1)


        contentPane.add(ignoredWordsLabel, 2, 0)

        ignoredWordsList.id = "ignoredWords"
        contentPane.add(ignoredWordsList, 2, 1)
        GridPane.setFillWidth(ignoredWordsList, true)
        GridPane.setHgrow(ignoredWordsList, Priority.ALWAYS)
        ignoredWordsList.selectionModel.selectionMode = SelectionMode.MULTIPLE

        contentPane.add(allProcessedWordsLabel, 2, 2)

        allProcessedWordsList.id = "allProcessedWords"
        contentPane.add(allProcessedWordsList, 2, 3)
        GridPane.setFillWidth(allProcessedWordsList, true)
        GridPane.setHgrow(allProcessedWordsList, Priority.ALWAYS)

        currentWordsList.id = "currentWords"
        currentWordsList.isEditable = true

        fromColumn.id = "fromColumn"
        fromColumn.isEditable = true
        fromColumn.cellValueFactory = Callback { p -> p.value.fromProperty }
        fromColumn.cellFactory = ExTextFieldTableCell.forStringTableColumn(ExTextFieldTableCell.TextFieldType.TextField)

        toColumn.id = "toColumn"
        toColumn.isEditable = true
        toColumn.cellValueFactory = PropertyValueFactory("to")
        toColumn.cellValueFactory = Callback { p -> p.value.toProperty }
        toColumn.cellFactory = ExTextFieldTableCell.forStringTableColumn(ExTextFieldTableCell.TextFieldType.TextArea)

        // alternative approach
        //toColumn.cellValueFactory = PropertyValueFactory("to")
        //toColumn.cellFactory = MultilineTextFieldTableCell.forStringTableColumn { toText, card -> card.to = toText }

        translationCountColumn.id = "translationCountColumn"
        translationCountColumn.isEditable = false
        translationCountColumn.cellValueFactory = Callback { p -> p.value.translationCountProperty }

        translationCountColumn.cellFactory = LabelStatusTableCell.forTableColumn { cell, _, translationCount ->
            val translationCountStatus = translationCount?.toTranslationCountStatus ?: TranslationCountStatus.Ok
            cell.styleClass.removeAll(TranslationCountStatus.allCssClasses)
            cell.styleClass.add(translationCountStatus.cssClass)
        }

        transcriptionColumn.id = "transcriptionColumn"
        transcriptionColumn.isEditable = true
        transcriptionColumn.cellValueFactory = PropertyValueFactory("transcription")
        transcriptionColumn.cellValueFactory = Callback { p -> p.value.transcriptionProperty }
        transcriptionColumn.cellFactory = ExTextFieldTableCell.forStringTableColumn(ExTextFieldTableCell.TextFieldType.TextField)

        examplesColumn.id = "examplesColumn"
        examplesColumn.isEditable = true
        examplesColumn.cellValueFactory = PropertyValueFactory("examples")
        examplesColumn.cellValueFactory = Callback { p -> p.value.examplesProperty }
        examplesColumn.cellFactory = ExTextFieldTableCell.forStringTableColumn(ExTextFieldTableCell.TextFieldType.TextArea)


        val iconLowPriority = Image("icons/exclamation-1.png")
        val iconHighPriority = Image("icons/exclamation-4.png")

        // Seems it is not allowed to share ImageView instance (between different cells rendering)
        // It causes disappearing/erasing icons in table view during scrolling
        // Most probably it is a bug or probably feature :-) */
        //
        // val iconView = ImageView(icon)

        wordCardStatusesColumn.id = "wordCardStatusesColumn"
        wordCardStatusesColumn.isEditable = false
        wordCardStatusesColumn.cellValueFactory = PropertyValueFactory("wordCardStatuses")
        wordCardStatusesColumn.cellValueFactory = Callback { p -> p.value.wordCardStatusesProperty }

        wordCardStatusesColumn.cellFactory = LabelStatusTableCell.forTableColumn(EmptyTextStringConverter()) { cell, card, _ ->

            cell.styleClass.removeAll(WordCardStatus.allCssClasses)
            cell.graphic = null

            val toolTips = mutableListOf<String>()

            if (card.showNoBaseWordInSet) {
                toolTips.add(NoBaseWordInSet.toolTipF(card))
                cell.styleClass.add(NoBaseWordInSet.cssClass)

                // Setting icon in CSS does not work. See my other comments regarding it.
                cell.graphic = ImageView(iconLowPriority)
            }

            if (NoTranslation in card.wordCardStatuses) {
                toolTips.add(NoTranslation.toolTipF(card))
                cell.styleClass.add(NoTranslation.cssClass)

                // Setting icon in CSS does not work. See my other comments regarding it.
                cell.graphic = ImageView(iconHighPriority)
            }

            if (TranslationIsNotPrepared in card.wordCardStatuses) {
                toolTips.add(TranslationIsNotPrepared.toolTipF(card))
                cell.styleClass.add(TranslationIsNotPrepared.cssClass)

                // Setting icon in CSS does not work. See my other comments regarding it.
                cell.graphic = ImageView(iconHighPriority)
            }

            val toolTipText = toolTips.joinToString("\n").trimToNull()
            cell.toolTipText = toolTipText
        }

        // Impossible to move it to CSS ?!
        fromColumn.prefWidth = 200.0
        wordCardStatusesColumn.prefWidth = 50.0
        toColumn.prefWidth = 400.0
        translationCountColumn.prefWidth = 50.0
        transcriptionColumn.prefWidth = 150.0
        examplesColumn.prefWidth = 400.0


        currentWordsList.columns.setAll(fromColumn, wordCardStatusesColumn, toColumn, translationCountColumn, transcriptionColumn, examplesColumn)


        this.center = contentPane
    }

}
