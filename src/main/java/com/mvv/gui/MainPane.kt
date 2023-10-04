package com.mvv.gui

import com.mvv.gui.javafx.*
import com.mvv.gui.javafx.ExTextFieldTableCell.TextFieldType
import com.mvv.gui.util.trimToNull
import com.mvv.gui.words.*
import com.mvv.gui.words.WarnAboutMissedBaseWordsMode.NotWarnWhenSomeBaseWordsPresent
import com.mvv.gui.words.WarnAboutMissedBaseWordsMode.WarnWhenSomeBaseWordsMissed
import com.mvv.gui.words.WordCardStatus.*
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.util.Callback
import javafx.util.StringConverter
import javafx.util.converter.DefaultStringConverter
import org.apache.commons.lang3.NotImplementedException


const val appTitle = "Words"

//private val log = mu.KotlinLogging.logger {}


class MainWordsPane : BorderPane() {

    lateinit var controller: LearnWordsController

    internal val currentWordsLabel = Text("File/Clipboard")
    internal val ignoredWordsLabel = Text("Ignored words")
    internal val allProcessedWordsLabel = Text("All processed words")

    internal val topPane = VBox()
    //internal val toolBar = ToolBar()

    internal val currentWordsList       = TableView<CardWordEntry>()
    private  val numberColumn           = TableColumn<CardWordEntry, Int>("No.")
    internal val fromColumn             = TableColumn<CardWordEntry, String>("English")
    private  val fromWordCountColumn    = TableColumn<CardWordEntry, Int>() // "N")
    private  val statusesColumn         = TableColumn<CardWordEntry, Set<WordCardStatus>>() // "S"
    internal val toColumn               = TableColumn<CardWordEntry, String>("Russian")
    private  val translationCountColumn = TableColumn<CardWordEntry, Int>() // "N"
    internal val transcriptionColumn    = TableColumn<CardWordEntry, String>("Transcription")
    private  val exampleCountColumn     = TableColumn<CardWordEntry, ExampleCountEntry>() //"ExN")
    internal val examplesColumn         = TableColumn<CardWordEntry, String>("Examples")
    private  val predefinedSetsColumn   = TableColumn<CardWordEntry, Set<PredefinedSet>>() // "predefinedSets")
    private  val sourcePositionsColumn  = TableColumn<CardWordEntry, List<Int>>() // "Source Positions")
    internal val sourceSentencesColumn  = TableColumn<CardWordEntry, String>("Source Sentences")

    internal val ignoredWordsList = ListView<String>()
    internal val allProcessedWordsList = ListView<String>()

    internal val removeIgnoredButton = Button("Remove ignored")

    private val warnWordCountsTextFormat = " (%d words with warning)"
    private val warnWordCountsText = Text(warnWordCountsTextFormat)
    private val warnWordCountsTextItems = listOf(
        Text("      "),
        ImageView("/icons/warning_obj.gif").also { it.translateY = 3.0 },
        warnWordCountsText,
    )

    internal fun updateWarnWordCount(wordCountWithWarning: Int) {
        val shouldBeVisible = (wordCountWithWarning != 0)
        warnWordCountsTextItems.forEach { it.isVisible = shouldBeVisible; it.isManaged = shouldBeVisible }
        warnWordCountsText.text = warnWordCountsTextFormat.format(wordCountWithWarning)
    }

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


    init {

        val contentPane = GridPane()

        contentPane.alignment = Pos.CENTER
        contentPane.hgap = 10.0; contentPane.vgap = 10.0
        contentPane.padding = Insets(10.0, 10.0, 10.0, 10.0)

        val currentWordsTopHeader = BorderPane()

        val currentWordsLabelsTextFlow = TextFlow(
            *(listOf(currentWordsLabel) + warnWordCountsTextItems).toTypedArray())

        currentWordsTopHeader.center = BorderPane().also {
            // Workaround with using BorderPane as container/wrapper over TextFlow
            // since TextFlow does not have possibility to vertically align content to the bottom.
            it.bottom = currentWordsLabelsTextFlow }

        updateWarnWordCount(0) // hide WarnWordCount by default


        currentWordsTopHeader.right = warnAboutMissedBaseWordsModeDropDown
        contentPane.add(currentWordsTopHeader, 0, 0)

        contentPane.add(currentWordsList, 0, 1, 1, 3)
        GridPane.setFillWidth(currentWordsList, true)
        GridPane.setHgrow(currentWordsList, Priority.ALWAYS)


        this.top = topPane
        //topPane.children.add(toolBar)

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
        currentWordsList.selectionModel.selectionMode = SelectionMode.MULTIPLE

        GridPane.setFillWidth(currentWordsList, true)
        GridPane.setHgrow(currentWordsList, Priority.ALWAYS)
        currentWordsList.prefWidth = 10_000.0

        numberColumn.id = "N"
        numberColumn.text = "No."
        numberColumn.isEditable = false
        numberColumn.isSortable = false
        numberColumn.cellFactory = Callback { createIndexTableCell<CardWordEntry>(IndexStartFrom.One) }

        fromColumn.id = "fromColumn"
        fromColumn.isEditable = true
        fromColumn.cellValueFactory = Callback { p -> p.value.fromProperty }
        fromColumn.cellFactory = ExTextFieldTableCell.forStringTableColumn(TextFieldType.TextField)

        fromWordCountColumn.id = "fromWordCountColumn"
        fromWordCountColumn.isEditable = false
        fromWordCountColumn.cellValueFactory = Callback { p -> p.value.fromWordCountProperty }

        fromWordCountColumn.graphic = Label("N").also { it.tooltip = Tooltip("Word count") }
        fromWordCountColumn.styleClass.add("fromWordCountColumn")

        toColumn.id = "toColumn"
        toColumn.isEditable = true
        toColumn.cellValueFactory = Callback { p -> p.value.toProperty }
        toColumn.cellFactory = ExTextFieldTableCell.forStringTableColumn(TextFieldType.TextArea,
            onEditCreate = { cell, editor ->
                addKeyBinding(editor, moveSelectedTextToExamplesKeyCombination) {
                    moveSelectedTextToExamples(cell.tableRow.item, editor) }

                addKeyBinding(editor, moveSelectedToSeparateCardKeyCombination) {
                    controller.moveSelectedToSeparateCard(editor, cell.tableRow.item, toColumn) }

                editor.keepCellEditorState( { Pair(toColumn, cell.tableRow.item) }, controller.cellEditorStates)
            })

        // alternative approach
        //toColumn.cellValueFactory = PropertyValueFactory("to")
        //toColumn.cellFactory = MultilineTextFieldTableCell.forStringTableColumn { toText, card -> card.to = toText }

        translationCountColumn.id = "translationCountColumn"
        translationCountColumn.isEditable = false
        translationCountColumn.cellValueFactory = Callback { p -> p.value.translationCountProperty }

        translationCountColumn.graphic = Label("N").also { it.tooltip = Tooltip("Translation count") }
        translationCountColumn.styleClass.add("translationCountColumn")

        translationCountColumn.cellFactory = LabelStatusTableCell.forTableColumn { cell, _, translationCount ->
            val translationCountStatus = translationCount?.toTranslationCountStatus ?: TranslationCountStatus.Ok
            cell.styleClass.removeAll(TranslationCountStatus.allCssClasses)
            cell.styleClass.add(translationCountStatus.cssClass)
        }

        transcriptionColumn.id = "transcriptionColumn"
        transcriptionColumn.isEditable = true
        transcriptionColumn.cellValueFactory = Callback { p -> p.value.transcriptionProperty }
        transcriptionColumn.cellFactory = ExTextFieldTableCell.forStringTableColumn(TextFieldType.TextField)

        exampleCountColumn.id = "exampleCountColumn"
        exampleCountColumn.isEditable = false
        exampleCountColumn.graphic = Label("N").also { it.tooltip = Tooltip("Example count and possible new card candidate count.") }
        exampleCountColumn.styleClass.add("exampleCountColumn")

        exampleCountColumn.cellValueFactory = Callback { p -> p.value.exampleCountProperty
            .mapCached({ exampleCount: Int -> ExampleCountEntry(exampleCount, p.value.exampleNewCardCandidateCount) },
                p.value.exampleNewCardCandidateCountProperty ) }


        examplesColumn.id = "examplesColumn"
        examplesColumn.isEditable = true
        examplesColumn.cellValueFactory = Callback { p -> p.value.examplesProperty }
        examplesColumn.cellFactory = Callback { ExTextFieldTableCell<CardWordEntry, String>(
            TextFieldType.TextArea, DefaultStringConverter(),
            onEditCreate = { cell, editor ->
                addKeyBinding(editor, moveSelectedToSeparateCardKeyCombination) {
                    controller.moveSelectedToSeparateCard(editor, cell.tableRow.item!!, examplesColumn) }

                editor.keepCellEditorState( { Pair(examplesColumn, cell.tableRow.item) }, controller.cellEditorStates)
            })
            .also { it.styleClass.add("examplesColumnCell") } }
        examplesColumn.styleClass.add("examplesColumn")

        predefinedSetsColumn.id = "predefinedSetsColumn"
        predefinedSetsColumn.isEditable = true
        predefinedSetsColumn.graphic = Label("Pr Sets").also { it.tooltip = Tooltip("Predefined Sets") }
        predefinedSetsColumn.cellValueFactory = Callback { p -> p.value.predefinedSetsProperty }
        predefinedSetsColumn.cellFactory = PredefinedSetsCell.forTableColumn()
        predefinedSetsColumn.styleClass.add("predefinedSetsColumn")

        sourcePositionsColumn.id = "sourcePositionsColumn"
        sourcePositionsColumn.isEditable = true
        sourcePositionsColumn.graphic = Label("Src").also { it.tooltip = Tooltip("Source Positions") }
        sourcePositionsColumn.cellValueFactory = Callback { p -> p.value.sourcePositionsProperty }
        sourcePositionsColumn.cellFactory = ExTextFieldTableCell.forTableColumn(TextFieldType.TextField, ListStringConverter(), ToolTipMode.ShowAllContent)
        sourcePositionsColumn.styleClass.add("sourcePositionsColumn")

        sourceSentencesColumn.id = "sourceSentencesColumn"
        sourceSentencesColumn.isEditable = false
        sourceSentencesColumn.cellValueFactory = Callback { p -> p.value.sourceSentencesProperty }
        sourceSentencesColumn.cellFactory = ExTextFieldTableCell.forStringTableColumn(TextFieldType.TextArea, ToolTipMode.ShowAllContent)
        sourceSentencesColumn.styleClass.add("sourceSentencesColumn")


        // Seems it is not allowed to share ImageView instance (between different cells rendering)
        // It causes disappearing/erasing icons in table view during scrolling
        // Most probably it is a bug or probably feature :-) */
        //
        // val iconView = ImageView(icon)

        statusesColumn.id = "statusesColumn"
        statusesColumn.isEditable = false
        statusesColumn.cellValueFactory = Callback { p ->
            // We use this trick with examplesProperty to recalculate/refresh tooltip
            // and show updated examples count in tooltip (even if statuses are not changed)
            // !! I'm sure that it is bug in JavaFX design that we need to set tooltip instead of generating it by request !!
            p.value.examplesProperty
                .flatMap { p.value.statusesProperty } }

        statusesColumn.graphic = Label("S").also { it.tooltip = Tooltip("Status") }
        statusesColumn.styleClass.add("statusesColumn")

        val statusesIcons = StatusesIcons()

        statusesColumn.cellFactory = LabelStatusTableCell.forTableColumn(EmptyTextStringConverter()) { cell, card, _ ->
            updateWordCardStatusesCell(cell, card, statusesIcons)

            /*
            cell.styleClass.removeAll(WordCardStatus.allCssClasses)
            cell.graphic = null

            val toolTips = mutableListOf<String>()

            if (TooManyExampleCardCandidates in card.statuses) {
                toolTips.add(TooManyExampleCardCandidates.toolTipF(card))
                cell.styleClass.add(TooManyExampleCardCandidates.cssClass)

                // Setting icon in CSS does not work. See my other comments regarding it.
                cell.graphic = ImageView(iconTooManyExamples)
            }

            if (card.showNoBaseWordInSet) {
                toolTips.add(NoBaseWordInSet.toolTipF(card))
                cell.styleClass.add(NoBaseWordInSet.cssClass)

                // Setting icon in CSS does not work. See my other comments regarding it.
                cell.graphic = ImageView(iconLowPriority)
            }

            if (NoTranslation in card.statuses) {
                toolTips.add(NoTranslation.toolTipF(card))
                cell.styleClass.add(NoTranslation.cssClass)

                // Setting icon in CSS does not work. See my other comments regarding it.
                cell.graphic = ImageView(iconHighPriority)
            }

            if (TranslationIsNotPrepared in card.statuses) {
                toolTips.add(TranslationIsNotPrepared.toolTipF(card))
                cell.styleClass.add(TranslationIsNotPrepared.cssClass)

                // Setting icon in CSS does not work. See my other comments regarding it.
                cell.graphic = ImageView(iconHighPriority)
            }

            if (Duplicates in card.statuses) {
                toolTips.add(Duplicates.toolTipF(card))
                cell.styleClass.add(Duplicates.cssClass)

                // Setting icon in CSS does not work. See my other comments regarding it.
                cell.graphic = ImageView(iconHighPriority)
            }

            val toolTipText = toolTips.joinToString("\n").trimToNull()
            cell.toolTipText = toolTipText
            */
        }

        // Impossible to move it to CSS ?!
        numberColumn.prefWidth = 40.0
        fromColumn.prefWidth = 200.0
        fromWordCountColumn.prefWidth = 30.0
        statusesColumn.prefWidth = 50.0
        toColumn.prefWidth = 550.0
        translationCountColumn.prefWidth = 50.0
        transcriptionColumn.prefWidth = 120.0
        exampleCountColumn.prefWidth = 60.0
        examplesColumn.prefWidth = 350.0
        predefinedSetsColumn.prefWidth = 70.0
        sourcePositionsColumn.prefWidth = 70.0
        sourceSentencesColumn.prefWidth = 300.0


        currentWordsList.columns.setAll(
            numberColumn,
            fromColumn, fromWordCountColumn,
            statusesColumn,
            transcriptionColumn,
            translationCountColumn, toColumn,
            exampleCountColumn, examplesColumn,
            predefinedSetsColumn,
            sourcePositionsColumn, sourceSentencesColumn,
        )


        this.center = contentPane
    }

}


internal class ListStringConverter<T> : StringConverter<List<T>>() {
    override fun toString(value: List<T>?): String = value?.joinToString(", ") ?: ""

    override fun fromString(string: String?): List<T> = throw IllegalStateException("Unsupported.")
}


private class PredefinedSetsCell : TableCell<CardWordEntry, Set<PredefinedSet>>() {

    init {
        styleClass.add("predefined-sets-table-cell")
    }

    public override fun updateItem(item: Set<PredefinedSet>?, empty: Boolean) {
        super.updateItem(item, empty)
        val cell = this

        if (cell.isEmpty) {
            cell.text = null
            cell.graphic = null
        } else {
            val toolTipText = item?.sorted()?.joinToString(", ") { it.humanName } ?: ""
            val itemIcons = item?.sorted()?.map { iconFor(it) } ?: emptyList()

            cell.text = null
            cell.graphic = FlowPane( *itemIcons.map { Label(null, ImageView(it)) }.toTypedArray() )
                .also { it.alignment = Pos.CENTER; it.hgap = 4.0 }
            cell.toolTipText = toolTipText

            cell.requestLayout()
        }
    }

    companion object {
        private val difficultToListen: Image by lazy { Image("icons/ear.png") }
        private val difficultSense: Image by lazy { Image("icons/sad.png") }

        private fun iconFor(predefinedSet: PredefinedSet): Image = when (predefinedSet) {
            PredefinedSet.DifficultToListen -> difficultToListen
            PredefinedSet.DifficultSense    -> difficultSense
        }

        fun forTableColumn(): Callback<TableColumn<CardWordEntry, Set<PredefinedSet>>, TableCell<CardWordEntry, Set<PredefinedSet>>> =
            Callback { _: TableColumn<CardWordEntry, Set<PredefinedSet>>? -> PredefinedSetsCell() }
    }
}


internal class ExampleCountEntry (private val exampleCount: Int, private val newCardCandidateCount: Int): Comparable<ExampleCountEntry> {
    override fun compareTo(other: ExampleCountEntry): Int = this.exampleCount.compareTo(other.exampleCount)
    override fun toString(): String = when {
        exampleCount == 0 -> "0"
        newCardCandidateCount == 0 -> "$exampleCount"
        else -> "$exampleCount ($newCardCandidateCount)"
    }
}


private fun updateWordCardStatusesCell(cell: TableCell<CardWordEntry, Set<WordCardStatus>>, card: CardWordEntry, icons: StatusesIcons) {

    cell.styleClass.removeAll(WordCardStatus.allCssClasses)
    cell.graphic = null

    val toolTips = mutableListOf<String>()

    fun updateCell(status: WordCardStatus) {
        toolTips.add(status.toolTipF(card))
        cell.styleClass.add(status.cssClass)

        // Setting icon in CSS does not work. See my other comments regarding it.
        cell.graphic = ImageView(icons.iconFor(status))
    }

    WordCardStatus.values()
        .filter { status -> status.isWarning && status in card.statuses }
        .forEach { updateCell(it) }

    /*
    // !!! Do NOT use 'when' there !!!
    // they are located in order fro low to high priority
    if (TooManyExampleCardCandidates in card.statuses)
        updateCell(TooManyExampleCardCandidates, icons.iconTooManyExamples)
    if (card.showNoBaseWordInSet)
        updateCell(NoBaseWordInSet, icons.showNoBaseWordInSetIcon)
    if (NoTranslation in card.statuses)
        updateCell(NoTranslation, icons.noTranslationIcon)
    if (TranslationIsNotPrepared in card.statuses)
        updateCell(TranslationIsNotPrepared, icons.translationIsNotPrepared)
    if (Duplicates in card.statuses)
        updateCell(Duplicates, icons.duplicatesIcons)
    */

    val toolTipText = toolTips.joinToString("\n").trimToNull()
    cell.toolTipText = toolTipText
}


// Creating icons moved out of function updateWordCardStatusesCell()
// to avoid loading them every time
private class StatusesIcons {
    // Or we can use EnumMap, BUT with !MANDATORY! tests.
    fun iconFor(status: WordCardStatus): Image = when (status) {
        NoBaseWordInSet                 -> noBaseWordInSetIcon
        TooManyExampleNewCardCandidates -> tooManyExampleNewCardCandidatesIcon
        TranslationIsNotPrepared        -> translationIsNotPrepared
        Duplicates                      -> duplicatesIcons
        NoTranslation                   -> noTranslationIcon

        BaseWordDoesNotExist            -> throw IllegalArgumentException("It is not warning and nothing to show.")
        IgnoreExampleCardCandidates     -> throw IllegalArgumentException("It is not warning and nothing to show.")
    }

    val iconLowPriority  = Image("icons/exclamation-1.png")
    val iconHighPriority = Image("icons/exclamation-4.png")

    // T O D O: would be nice to create individual icon for every warning
    val noBaseWordInSetIcon      = iconLowPriority
    val tooManyExampleNewCardCandidatesIcon = Image("icons/receiptstext-warn.png")
    val noTranslationIcon        = iconHighPriority
    val translationIsNotPrepared = iconHighPriority
    val duplicatesIcons          = iconHighPriority
}

