package com.mvv.gui.cardeditor

import com.mvv.gui.cardeditor.actions.*
import com.mvv.gui.javafx.*
import com.mvv.gui.javafx.ExTextFieldTableCell.TextFieldType
import com.mvv.gui.memoword.asSinglePartOfSpeech
import com.mvv.gui.util.filterNotBlank
import com.mvv.gui.util.toEnumSet
import com.mvv.gui.util.trimToNull
import com.mvv.gui.words.*
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.FlowPane
import javafx.util.Callback
import javafx.util.StringConverter
import javafx.util.converter.DefaultStringConverter


open class WordCardsTable(val controller: LearnWordsController) : TableView<CardWordEntry>() {

    internal val numberColumn           = TableColumn<CardWordEntry, Int>("No.")
    private  val baseOfFrom1CharColumn  = TableColumn<CardWordEntry, BaseAndFrom>() //"B")
    internal val fromColumn             = TableColumn<CardWordEntry, String>("English")
    private  val fromWordCountColumn    = TableColumn<CardWordEntry, Int>() // "N")
    private  val partsOfSpeechColumn    = TableColumn<CardWordEntry, Set<PartOfSpeech>>()
    private  val statusesColumn         = TableColumn<CardWordEntry, Set<WordCardStatus>>() // "S"
    internal val toColumn               = TableColumn<CardWordEntry, String>("Russian")
    internal val translationCountColumn = TableColumn<CardWordEntry, Int>() // "N"
    @Suppress("MemberVisibilityCanBePrivate")
    internal val transcriptionColumn    = TableColumn<CardWordEntry, String>("Transcription")
    internal val exampleCountColumn     = TableColumn<CardWordEntry, ExampleCountEntry>() //"ExN")
    internal val examplesColumn         = TableColumn<CardWordEntry, String>("Examples")
    private  val predefinedSetsColumn   = TableColumn<CardWordEntry, Set<PredefinedSet>>() // "predefinedSets")
    private  val sourcePositionsColumn  = TableColumn<CardWordEntry, List<Int>>() // "Source Positions")
    internal val sourceSentencesColumn  = TableColumn<CardWordEntry, String>("Source Sentences")

    private val isReadOnly: Boolean get() = controller.isReadOnly

    init {
        numberColumn.id = "N"
        numberColumn.text = "No."
        numberColumn.isEditable = false
        numberColumn.isSortable = false
        numberColumn.cellFactory = Callback { createIndexTableCell<CardWordEntry>(IndexStartFrom.One) }

        baseOfFrom1CharColumn.id = "baseOfFrom1CharColumn"
        baseOfFrom1CharColumn.isEditable = false
        baseOfFrom1CharColumn.isSortable = true
        baseOfFrom1CharColumn.cellValueFactory = Callback { p -> p.value.baseWordAndFromProperty }
        baseOfFrom1CharColumn.cellFactory = Callback { ExTextFieldTableCell(
            TextFieldType.TextField, DelegateStringConverter { it.base.firstOrNull()?.uppercase() ?: "" }, toolTipF = { it.base } ) }
        baseOfFrom1CharColumn.graphic = Label("B").also { it.tooltip = Tooltip(
            "Base word.\nUse this column to sort by 'base' word or use explicit sorting by 'From'.") }
        baseOfFrom1CharColumn.styleClass.add("baseOfFrom1CharColumn")

        fromColumn.id = "fromColumn"
        fromColumn.isEditable = true
        fromColumn.cellValueFactory = Callback { p -> p.value.fromProperty }
        fromColumn.cellFactory = ExTextFieldTableCell.forStringTableColumn(TextFieldType.TextField)

        fromColumn.isSortable = true
        fromColumn.comparator = String.CASE_INSENSITIVE_ORDER
        fromColumn.sortType = TableColumn.SortType.ASCENDING

        fromWordCountColumn.id = "fromWordCountColumn"
        fromWordCountColumn.isEditable = false
        fromWordCountColumn.cellValueFactory = Callback { p -> p.value.fromWordCountProperty }
        fromWordCountColumn.graphic = Label("N").also { it.tooltip = Tooltip("Word count") }
        fromWordCountColumn.styleClass.add("fromWordCountColumn")

        partsOfSpeechColumn.id = "partsOfSpeech"
        partsOfSpeechColumn.isEditable = true
        partsOfSpeechColumn.cellValueFactory = Callback { p -> p.value.partsOfSpeechProperty
            .ifNullOrEmptyCached( { setOf(p.value.predictedPartOfSpeechProperty.value) }, p.value.toProperty) }
        partsOfSpeechColumn.graphic = Label("P").also { it.tooltip = Tooltip("Part of speech") }
        partsOfSpeechColumn.styleClass.add("partsOfSpeech")

        partsOfSpeechColumn.cellFactory = CheckComboBoxCell.forTableColumn<CardWordEntry, PartOfSpeech, Set<PartOfSpeech>>(
            stringLabelConverter    = PartsOfSpeechStringConverter(),
            stringDropDownConverter = PartOfSpeechStringConverter(),
            items = PartOfSpeech.values().toList(),
            valueToListConverter = { it.toList() },
            listToValueConverter = { it.toEnumSet() },
            altSetProperty       = { cell -> cell.tableRow.item.partsOfSpeechProperty },
            updateCellAttrs      = { cell, card, _ -> updatePartOfSpeechCell(cell, card) },
        )

        toColumn.id = "toColumn"
        toColumn.isEditable = true
        toColumn.cellValueFactory = Callback { p -> p.value.toProperty }
        toColumn.cellFactory = ExTextFieldTableCell.forStringTableColumn(TextFieldType.TextArea,
            onEditCreate = { cell, editor ->
                editor.keepCellEditorState( { Pair(toColumn, cell.tableRow.item) }, controller.cellEditorStates)

                if (!isReadOnly) {
                    addLocalKeyBindings(editor, moveSubTextToExamplesKeyCombination) {
                        moveSubTextToExamples(cell.tableRow.item, editor) }

                    addLocalKeyBindings(editor, moveSubTextToSeparateCardKeyCombination) {
                        controller.moveSubTextToSeparateCard(editor, cell.tableRow.item, toColumn) }

                    addLocalKeyBindings(editor, moveSubTextToExamplesAndSeparateCardKeyCombination) {
                        controller.moveSubTextToExamplesAndSeparateCard(editor, cell.tableRow.item, toColumn) }
                }
            })

        // alternative approach
        //toColumn.cellValueFactory = PropertyValueFactory("to")
        //toColumn.cellFactory = MultilineTextFieldTableCell.forStringTableColumn { toText, card -> card.to = toText }

        translationCountColumn.id = "translationCountColumn"
        translationCountColumn.isEditable = false
        translationCountColumn.cellValueFactory = Callback { p -> p.value.translationCountProperty }

        translationCountColumn.graphic = Label("N").also { it.tooltip = Tooltip("Translation count") }
        translationCountColumn.styleClass.add("translationCountColumn")

        translationCountColumn.cellFactory = LabelTableCell.forTableColumn { cell, _, translationCount ->
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
                editor.keepCellEditorState( { Pair(examplesColumn, cell.tableRow.item) }, controller.cellEditorStates)

                if (!isReadOnly)
                    addLocalKeyBindings(editor, moveSubTextToSeparateCardKeyCombination) {
                        controller.moveSubTextToSeparateCard(editor, cell.tableRow.item!!, examplesColumn) }

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

        statusesColumn.cellFactory = LabelTableCell.forTableColumn(EmptyTextStringConverter()) { cell, card, _ ->
            updateWordCardStatusesCell(cell, card, statusesIcons) }

        // Impossible to move it to CSS ?!
        numberColumn.prefWidth = 40.0
        baseOfFrom1CharColumn.prefWidth = 40.0
        fromColumn.prefWidth = 200.0
        fromWordCountColumn.prefWidth = 30.0
        partsOfSpeechColumn.prefWidth = 40.0
        statusesColumn.prefWidth = 50.0
        toColumn.prefWidth = 550.0
        translationCountColumn.prefWidth = 50.0
        transcriptionColumn.prefWidth = 120.0
        exampleCountColumn.prefWidth = 60.0
        examplesColumn.prefWidth = 350.0
        predefinedSetsColumn.prefWidth = 70.0
        sourcePositionsColumn.prefWidth = 70.0
        sourceSentencesColumn.prefWidth = 300.0

        //sortOrder.add(fromColumn)
        sortOrder.add(baseOfFrom1CharColumn)
        //this.sortPolicy

        // It is needed if SortedList is used as TableView items
        // ??? just needed :-) (otherwise warning in console)
        //currentWordsSorted.comparatorProperty().bind(currentWordsList.comparatorProperty());

        // Platform.runLater is used to perform analysis AFTER word card changing
        // It would be nice to find better/proper event (with already changed underlying model after edit commit)
        val doOnWordChanging: (card: CardWordEntry)->Unit = { controller.markDocumentIsDirty(); Platform.runLater { controller.reanalyzeOnlyWords(it) } }

        fromColumn.addEventHandler(TableColumn.editCommitEvent<CardWordEntry,String>()) { doOnWordChanging(it.rowValue) }
        toColumn.addEventHandler(TableColumn.editCommitEvent<CardWordEntry,String>())   { doOnWordChanging(it.rowValue) }

        isEditable = !isReadOnly

        this.columns.setAll(
            numberColumn,
            baseOfFrom1CharColumn,
            fromColumn, fromWordCountColumn,
            partsOfSpeechColumn,
            statusesColumn,
            transcriptionColumn,
            translationCountColumn, toColumn,
            exampleCountColumn, examplesColumn,
            predefinedSetsColumn,
            sourcePositionsColumn, sourceSentencesColumn,
        )

        addAltScrollingBinding()
        addKeyBindings()

        // T O D O: probably this should be adjustable via check-box??
        fixSortingAfterCellEditCommit(fromColumn)
    }

    private fun startEditingFrom() = startEditingColumnCell(fromColumn)
    private fun startEditingTo() = startEditingColumnCell(toColumn)
    private fun startEditingTranscription() = startEditingColumnCell(transcriptionColumn)
    private fun startEditingRemarks() = startEditingColumnCell(examplesColumn)

    private fun startEditingColumnCell(column: TableColumn<CardWordEntry, String>) {
        val selectedIndex = currentWordsSelection.selectedIndex
        if (selectedIndex != -1) edit(selectedIndex, column)
    }

    private fun addKeyBindings() {
        if (isReadOnly) return

        addGlobalKeyBinding(this, lowerCaseKeyCombination) { toggleTextSelectionCaseOrLowerCaseRow() }

        addGlobalKeyBinding(this, KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.CONTROL_DOWN)) { startEditingFrom() }
        addGlobalKeyBinding(this, KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.CONTROL_DOWN)) { startEditingTranscription() }
        addGlobalKeyBinding(this, KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.CONTROL_DOWN)) { startEditingTo() }
        addGlobalKeyBinding(this, KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.CONTROL_DOWN)) { startEditingRemarks() }
    }

    private fun addAltScrollingBinding() {
        addLocalKeyBinding(this, KeyCodeCombination(KeyCode.DOWN, KeyCombination.ALT_DOWN)) {
            val current = this.selectionModel.selectedIndex
            if (current >= 0 && current < this.items.lastIndex) {
                val nextRowIndex = current + 1
                this.selectItem(nextRowIndex)
                this.scrollTo(this.visibleRows.first + 1)
            }
        }
        addLocalKeyBinding(this, KeyCodeCombination(KeyCode.UP, KeyCombination.ALT_DOWN)) {
            val current = this.selectionModel.selectedIndex
            if (current >= 1) {
                val prevRowIndex = current - 1
                this.selectItem(prevRowIndex)
                this.scrollTo(this.visibleRows.first - 1)
            }
        }
    }

    internal fun toggleTextSelectionCaseOrLowerCaseRow() {
        if (!isEditing)
            wordCardsToLowerCaseRow(currentWordsSelection.selectedItems)
        else {
            val focusOwner = scene.focusOwner
            if (focusOwner is TextInputControl) {
                toggleCase(focusOwner)
            }
        }
    }

    private val currentWordsSelection: TableViewSelectionModel<CardWordEntry> get() = selectionModel

    // companion object { }
}


private class PredefinedSetsCell : TableCell<CardWordEntry, Set<PredefinedSet>>() {

    init { styleClass.add("predefined-sets-table-cell") }

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
        private val difficultSense:    Image by lazy { Image("icons/sad.png") }

        private fun iconFor(predefinedSet: PredefinedSet): Image = when (predefinedSet) {
            PredefinedSet.DifficultToListen -> difficultToListen
            PredefinedSet.DifficultSense    -> difficultSense
        }

        fun forTableColumn(): Callback<TableColumn<CardWordEntry, Set<PredefinedSet>>, TableCell<CardWordEntry, Set<PredefinedSet>>> =
            Callback { _: TableColumn<CardWordEntry, Set<PredefinedSet>>? -> PredefinedSetsCell() }
    }
}


// Creating icons moved out of function updateWordCardStatusesCell()
// to avoid loading them every time
private class StatusesIcons {
    // Or we can use EnumMap, BUT with !MANDATORY! tests.
    fun iconFor(status: WordCardStatus): Image = when (status) {
        WordCardStatus.NoBaseWordInSet                 -> noBaseWordInSetIcon
        WordCardStatus.TooManyExampleNewCardCandidates -> tooManyExampleNewCardCandidatesIcon
        WordCardStatus.TranslationIsNotPrepared        -> translationIsNotPreparedIcon
        WordCardStatus.Duplicates                      -> duplicatesIcon
        WordCardStatus.NoTranslation                   -> noTranslationIcon

        WordCardStatus.BaseWordDoesNotExist -> throw IllegalArgumentException("It is not warning and nothing to show.")
        WordCardStatus.IgnoreExampleCardCandidates -> throw IllegalArgumentException("It is not warning and nothing to show.")
    }

    val iconLowPriority  = Image("icons/exclamation-1.png")
    val iconHighPriority = Image("icons/exclamation-4.png")

    // T O D O: would be nice to create individual icon for every warning
    val noBaseWordInSetIcon          = iconLowPriority
    val tooManyExampleNewCardCandidatesIcon = Image("icons/receiptstext-warn.png")
    val noTranslationIcon            = iconHighPriority
    val translationIsNotPreparedIcon = iconHighPriority
    val duplicatesIcon               = iconHighPriority
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

    val toolTipText = toolTips.joinToString("\n").trimToNull()
    cell.toolTipText = toolTipText
}


private fun updatePartOfSpeechCell(cell: TableCell<CardWordEntry, Set<PartOfSpeech>>, card: CardWordEntry) {
    if ("PartOfSpeechCell" !in cell.styleClass)
        cell.styleClass.add("PartOfSpeechCell")

    cell.styleClass.removeAll(PartOfSpeech.allCssClasses)

    val partsOfSpeech = card.partsOfSpeech.ifEmpty { setOf(card.predictedPartOfSpeechProperty.value) }
    val asSinglePartSpeech = card.asSinglePartOfSpeech()

    val cssClass = if (card.from.isBlank()) "" else asSinglePartSpeech.name
    cell.styleClass.add(cssClass)

    // Uncontrolled setting 'text' causes appearing Label and ComboBox at the same time,
    // and causes hiding menu after 1st click on check-box
    //
    //val label = PartsOfSpeechStringConverter().toString(partsOfSpeech)
    //cell.text = label

    val toolTipText = partsOfSpeech.joinToString("\n") { it.name }
    cell.toolTipText = toolTipText
}


internal class ExampleCountEntry (private val exampleCount: Int, private val newCardCandidateCount: Int): Comparable<ExampleCountEntry> {
    override fun compareTo(other: ExampleCountEntry): Int = this.exampleCount.compareTo(other.exampleCount)
    override fun toString(): String = when {
        exampleCount == 0 -> "0"
        newCardCandidateCount == 0 -> "$exampleCount"
        else -> "$exampleCount ($newCardCandidateCount)"
    }
}


class PartOfSpeechStringConverter : StringConverter<PartOfSpeech>() {
    override fun toString(value: PartOfSpeech?): String = value?.name ?: ""
    override fun fromString(string: String): PartOfSpeech = PartOfSpeech.valueOf(string.trim())
}

class PartsOfSpeechStringConverter : StringConverter<Collection<PartOfSpeech>>() {
    override fun toString(value: Collection<PartOfSpeech>?): String {
        if (value.isNullOrEmpty()) return ""

        val asStr = value.sortedBy { it.ordinal }.map { it.name[0] }.joinToString(",")
        return asStr
    }

    override fun fromString(string: String?): Set<PartOfSpeech> {
        if (string.isNullOrBlank()) return emptySet()

        return string.split(",")
            .filterNotBlank()
            .map { PartOfSpeech.valueOf(it.trim()) }
            .toEnumSet()
    }
}
