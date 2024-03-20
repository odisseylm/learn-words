package com.mvv.gui.cardeditor

import com.mvv.gui.javafx.*
import com.mvv.gui.util.PathCaseInsensitiveComparator
import com.mvv.gui.util.containsOneOf
import com.mvv.gui.words.AllWordCardSetsManager
import com.mvv.gui.words.baseWordsFilename
import com.mvv.gui.words.dictDirectory
import com.mvv.gui.words.getAllExistentSetFiles
import javafx.application.Platform
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.util.Callback
import javafx.util.StringConverter
import java.nio.file.Path


enum class ShowFilesMode { Combo, List }

fun showCopyToOtherSetDialog(
    parent: Node,
    currentWordsFile: Path?,
    allWordCardSetsManager: AllWordCardSetsManager,
    showFilesMode: ShowFilesMode,
    ): Path? {

    val currentWordsFileParent = currentWordsFile?.parent ?: dictDirectory

    fun getRealtimeDictionarySets(): List<Path> = getAllExistentSetFiles(
        includeMemoWordFile = false,
        toIgnoreBaseWordsFilename = allWordCardSetsManager.ignoredFile?.baseWordsFilename)
        .sortedWith(PathCaseInsensitiveComparator())

    val otherCachedSetsFiles = allWordCardSetsManager.allCardSets.sortedWith(PathCaseInsensitiveComparator())
        .ifEmpty { getRealtimeDictionarySets() }
    val allOtherSetsParents = otherCachedSetsFiles.map { it.parent }.distinct()
    val commonAllOtherSetsSubParent = if (allOtherSetsParents.size == 1) allOtherSetsParents[0]
    else allOtherSetsParents.minByOrNull { it.nameCount } ?: currentWordsFileParent

    val allSetsParentsAreParentOfCurrent =
        (allOtherSetsParents.size == 1) && (allOtherSetsParents[0] == currentWordsFileParent)

    val pathToSetNameConv = object : StringConverter<Path>() {
        override fun toString(value: Path?): String = when {
            allSetsParentsAreParentOfCurrent -> value?.baseWordsFilename
            else -> value?.toString()?.removePrefix(commonAllOtherSetsSubParent.toString())?.removePrefix("/")
        } ?: ""

        override fun fromString(string: String?): Path? = if (string.isNullOrBlank()) null else Path.of(string.trim())
    }

    val dialog = Dialog<Path>()

    // Dynamic state (or we can convert it to class)
    var filterBy: String? = null
    var otherRealSetsFiles: List<Path>? = null


    val filterToggleGroup = ToggleGroup()

    val uiFiles: ObservableList<Path>
    val filesSelection: SelectionModel<Path>

    val filesContainer = when (showFilesMode) {
        ShowFilesMode.List -> ListView<Path>().also {
            it.items.setAll(otherCachedSetsFiles)
            it.isEditable = true
            it.cellFactory = stringConverterListCellFactory(
                DelegateStringConverter<Path>{ p -> pathToSetNameConv.toString(p).replace("/", "/ ") } )

            uiFiles = it.items
            filesSelection = it.selectionModel
            it.addEventHandler(MouseEvent.MOUSE_CLICKED) { ev ->
                if (ev.clickCount >= 2 && filesSelection.selectedItem != null) {
                    dialog.result = filesSelection.selectedItem
                    dialog.close()
                }
            }
        }
        ShowFilesMode.Combo -> ComboBox<Path>().also {
            it.items.setAll(otherCachedSetsFiles)
            it.isEditable = true
            it.converter = pathToSetNameConv

            uiFiles = it.items
            filesSelection = it.selectionModel
        }
    }


    fun getFiltered(filterString: String?): List<Path> {
        val files = otherRealSetsFiles ?: otherCachedSetsFiles
        val altFilterStr = filterString?.replace('/', '\\') ?: ""

        val filtered = when (filterString) {
            "", null -> files
            "/"  -> files.filterNot { pathToSetNameConv.toString(it).containsOneOf("/", "\\") }
            else -> files.filter { it.toString().containsOneOf(filterString, altFilterStr) }
        }

        return filtered
    }

    fun fillFiles() { uiFiles.setAll(getFiltered(filterBy)) }

    fun refreshSetFiles() {
        otherRealSetsFiles = getRealtimeDictionarySets()
        fillFiles()
    }

    fun filterButton(label: String, btnFilterBy: String?) =
        RadioButton(label).apply {
            userData    = btnFilterBy
            toggleGroup = filterToggleGroup
            onAction    = EventHandler { filterBy = userData as String?; fillFiles() }
        }

    val filterPane = FlowPane(
        filterButton("All", null).also { it.isSelected = true },
        filterButton("Root", "/"),
        filterButton("Grouped", "/grouped/"),
        filterButton("Synonyms", "/synonyms/"),
        filterButton("Base verbs", "/base-verbs/"),
        filterButton("Films", "/films/"),
        filterButton("Homophones", "/homophones/"),
    ).apply {
        hgap = 20.0
        vgap = 10.0
        styleClass.add("filterPane")
    }

    val refreshButton = newButton(buttonIcon("icons/iu_update_obj.png")) { refreshSetFiles() }
    refreshButton.styleClass.add("refreshFilesButton")

    val contentPane = BorderPane(HBox(refreshButton, filesContainer)).apply {
        styleClass.add("chooseOtherSetDialogPane")
        top = filterPane

        HBox.setHgrow(filesContainer, Priority.ALWAYS)
    }

    with(dialog) {
        title = "Select cards' set to copy cards to"

        dialogPane.content = contentPane
        dialogPane.buttonTypes.addAll(
            ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE),
            ButtonType("Ok", ButtonBar.ButtonData.OK_DONE),
        )

        isResizable = true
        initDialogParentAndModality(this, parent)

        setResultConverter { buttonType ->
            if (buttonType.buttonData == ButtonBar.ButtonData.OK_DONE) filesSelection.selectedItem else null
        }
    }

    Platform.runLater { filesContainer.requestFocus() }

    return dialog.showAndWait().orElse(null)
}


fun <T> stringConverterListCellFactory(stringConverter: StringConverter<T>): Callback<ListView<T>, ListCell<T>> {
    class CustomListCell : ListCell<T>() {
        override fun updateItem(item: T, empty: Boolean) {
            super.updateItem(item, empty)

            val text = if (isEmpty) null else stringConverter.toString(item)
            this.text = text
        }
    }

    return Callback<ListView<T>, ListCell<T>> { CustomListCell() }
}
