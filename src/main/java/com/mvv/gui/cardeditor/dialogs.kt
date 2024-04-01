package com.mvv.gui.cardeditor

import com.mvv.gui.javafx.*
import com.mvv.gui.util.*
import com.mvv.gui.words.*
import javafx.application.Platform
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.util.Callback
import javafx.util.StringConverter
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.name


enum class ShowFilesMode { Combo, List }

fun showCopyToOtherSetDialog(
    parent: Node,
    currentWordsFile: Path?,
    allWordCardSetsManager: AllWordCardSetsManager,
    showFilesMode: ShowFilesMode,
    ): Path? {

    val currentWordsFileParent = currentWordsFile?.parent ?: dictDirectory
    val pathToSetNameConv = DictionaryPathToRelativeNameConverter(allWordCardSetsManager, currentWordsFileParent)
    val otherCachedSetsFiles: List<Path> = pathToSetNameConv.otherCachedSetsFiles

    val dialog = Dialog<Path>()

    // Dynamic state (or we can convert it to class)
    var filterBy: String? = null
    var otherRealSetsFiles: List<Path>? = null


    val filterToggleGroup = ToggleGroup()

    val uiFiles: ObservableList<Path>
    val filesSelection: SelectionModel<Path>

    val filesContainer = when (showFilesMode) {
        ShowFilesMode.List -> listView(
                items = otherCachedSetsFiles,
                isEditable = true,
                cellFactory = stringConverterListCellFactory(
                    DelegateStringConverter{ p -> pathToSetNameConv.toString(p).replace("/", "/ ") } ),
            ).also {
                uiFiles = it.items
                filesSelection = it.selectionModel

                it.onDoubleClicked = {
                    if (filesSelection.selectedItem != null) {
                        dialog.result = filesSelection.selectedItem
                        dialog.close()
                    }
                }
            }

        ShowFilesMode.Combo -> comboBox(
                items = otherCachedSetsFiles,
                isEditable = true,
                converter = pathToSetNameConv,
            ).also {
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
        otherRealSetsFiles = pathToSetNameConv.getRealtimeDictionarySets()
        fillFiles()
    }

    fun filterButton(label: String, btnFilterBy: String?) = radioButton(
        label       = label,
        toggleGroup = filterToggleGroup,
        onAction    = EventHandler { filterBy = btnFilterBy; fillFiles() },
    )

    val filterPane = flowPane(
        children = listOf(
            filterButton("All", null).also { it.isSelected = true },
            filterButton("Root", "/"),
            filterButton("Grouped", "/grouped/"),
            filterButton("Synonyms", "/synonyms/"),
            filterButton("Base verbs", "/base-verbs/"),
            filterButton("Films", "/films/"),
            filterButton("Homophones", "/homophones/"),
        ),
        hGap = 20.0,
        vGap = 10.0,
        styleClass = "filterPane",
    )

    val contentPane = borderPane(
        center = HBox(refreshFilesButton { refreshSetFiles() }, filesContainer),
        top = filterPane,
        styleClass = "chooseOtherSetDialogPane",
        ).apply {
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


fun chooseDictionaryDialog(parent: Node,
                           currentWordsFile: Path?,
                           allWordCardSetsManager: AllWordCardSetsManager,
                           label: String,
                           dictionariesDirectory: Path?,
                          ): Path? {

    val dictDirectory = dictionariesDirectory ?: dictDirectory
    val pathToSetNameConv = DictionaryPathToRelativeNameConverter(allWordCardSetsManager, dictDirectory)

    val dialog = Dialog<Path>()

    // Dynamic state (or we can convert it to class)
    var filterBy: Char? = null
    var otherRealSetsFiles: List<Path>? = null


    val filterToggleGroup = ToggleGroup()

    val filesList = listView(
        items = pathToSetNameConv.otherCachedSetsFiles,
        isEditable = true,
        cellFactory = stringConverterListCellFactory(
            DelegateStringConverter{ p -> pathToSetNameConv.toString(p).replace("/", "/ ") } ),
    ).also {
        it.onDoubleClicked = { _ ->
            val selectedItem = it.selectionModel.selectedItem
            if (selectedItem != null) {
                dialog.result = selectedItem
                dialog.close()
            }
        }
    }


    fun getFiltered(startsWith: Char?): List<Path> {
        val allDictFiles = otherRealSetsFiles ?: pathToSetNameConv.otherCachedSetsFiles

        val filtered = if (startsWith == null || startsWith == ' ') allDictFiles
                       else allDictFiles.filter {
                           it != currentWordsFile &&
                           it.name.startsWith(startsWith, ignoreCase = true)
                       }
        return filtered
    }

    fun fillFiles() { filesList.items.setAll(getFiltered(filterBy)) }

    fun refreshSetFiles() {
        otherRealSetsFiles = pathToSetNameConv.getRealtimeDictionarySets()
        fillFiles()
    }

    fun filterButton(label: Char) = toggleButton(
        label = label.toString(),
        toggleGroup = filterToggleGroup,
        styleClass = "letterButton",
        onAction = { filterBy = label; fillFiles() }
    )

    val filterPane = flowPane(
        children = listOf(filterButton(' ')) + ('A'..'Z').map { filterButton(it) },
        hGap = 10.0,
        vGap = 10.0,
        styleClass = "filterPane",
    )

    val contentPane = borderPane(
        top = VBox(label(label, styleClass = "dictionariesGroupingLabel"), filterPane),
        center = HBox(refreshFilesButton { refreshSetFiles() }, filesList),
        styleClass = "chooseOtherSetDialogPane",
        ).apply {
            HBox.setHgrow(filesList, Priority.ALWAYS)
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
            if (buttonType.buttonData == ButtonBar.ButtonData.OK_DONE) filesList.selectionModel.selectedItem else null
        }
    }

    Platform.runLater { filesList.requestFocus() }

    return dialog.showAndWait().orElse(null)
}

private fun refreshFilesButton(action: ()->Unit): Button =
    newButton(buttonIcon("icons/iu_update_obj.png"), action).also {
        it.styleClass.add("refreshFilesButton")
    }


// T O D O: Refactor or rename. It contains not only path-string logic
private class DictionaryPathToRelativeNameConverter (
    private val allWordCardSetsManager: AllWordCardSetsManager,
    private val dictionariesDirectory: Path,
    ) : StringConverter<Path>() {

    fun getRealtimeDictionarySets(): List<Path> = getAllExistentSetFiles(
        dictionariesDirectory,
        includeMemoWordFile = false,
        toIgnoreBaseWordsFilename = allWordCardSetsManager.ignoredFile?.baseWordsFilename,
        ).sortedWith(PathCaseInsensitiveComparator())

    val otherCachedSetsFiles: List<Path> = allWordCardSetsManager.allCardSets
        .filter { it.startsWith(dictionariesDirectory) }
        .sortedWith(PathCaseInsensitiveComparator())
        .ifEmpty { getRealtimeDictionarySets() }

    private val allSetsParentsAreParentOfCurrent: Boolean
    private val commonAllOtherSetsSubParent: Path

    init {
        val allOtherSetsParents = otherCachedSetsFiles.map { it.parent }.distinct()

        commonAllOtherSetsSubParent =
            if (allOtherSetsParents.size == 1) allOtherSetsParents[0]
            else allOtherSetsParents.minByOrNull { it.nameCount } ?: dictionariesDirectory

        allSetsParentsAreParentOfCurrent =
            (allOtherSetsParents.size == 1) && (allOtherSetsParents[0] == dictionariesDirectory)
    }

    override fun toString(value: Path?): String = when {
        allSetsParentsAreParentOfCurrent -> value?.baseWordsFilename
        else -> value?.toString()?.removePrefix(commonAllOtherSetsSubParent.toString())?.removePrefix("/")
    } ?: ""

    override fun fromString(string: String?): Path? {
        if (string.isNullOrBlank()) return null

        val destFileOrSetNameAsPath = Path(string)
        return if (destFileOrSetNameAsPath.exists()) destFileOrSetNameAsPath
            else {
                if (destFileOrSetNameAsPath.isInternalCsvFormat)
                    commonAllOtherSetsSubParent.resolve(destFileOrSetNameAsPath)
                else {
                    val setName = destFileOrSetNameAsPath.toString()
                    require(!setName.endsWithOneOf(internalWordCardsFileExt, plainWordsFileExt)) {
                        "Please, specify set name or full absolute path to set file."}

                    dictionariesDirectory.resolve(setName.withFileExt(internalWordCardsFileExt))
                }
            }
    }
}
