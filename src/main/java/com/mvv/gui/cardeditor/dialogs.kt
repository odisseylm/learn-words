package com.mvv.gui.cardeditor

import com.mvv.gui.javafx.buttonIcon
import com.mvv.gui.javafx.initDialogParentAndModality
import com.mvv.gui.javafx.newButton
import com.mvv.gui.util.PathCaseInsensitiveComparator
import com.mvv.gui.util.containsOneOf
import com.mvv.gui.words.AllWordCardSetsManager
import com.mvv.gui.words.baseWordsFilename
import com.mvv.gui.words.dictDirectory
import com.mvv.gui.words.getAllExistentSetFiles
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.util.StringConverter
import java.nio.file.Path



fun showCopyToOtherSetDialog(
    parent: Node,
    currentWordsFile: Path?,
    allWordCardSetsManager: AllWordCardSetsManager,
    ): Path? {

    val currentWordsFileParent = currentWordsFile?.parent ?: dictDirectory

    val otherCachedSetsFiles = allWordCardSetsManager.allCardSets.sortedWith(PathCaseInsensitiveComparator())
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

    // Dynamic state (or we can convert it to class)
    var filterBy: String? = null
    var otherRealSetsFiles: List<Path>? = null


    val filterToggleGroup = ToggleGroup()

    val filesComboBox = ComboBox<Path>().also {
        it.items.setAll(otherCachedSetsFiles)
        it.isEditable = true
        it.converter = pathToSetNameConv
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

    fun fillComboBox() { filesComboBox.items.setAll(getFiltered(filterBy)) }

    fun getRealtimeDictionarySets(): List<Path> = getAllExistentSetFiles(
                includeMemoWordFile = false,
                toIgnoreBaseWordsFilename = allWordCardSetsManager.ignoredFile?.baseWordsFilename)
            .sortedWith(PathCaseInsensitiveComparator())

    fun refreshSetFiles() {
        otherRealSetsFiles = getRealtimeDictionarySets()
        fillComboBox()
    }

    fun filterButton(label: String, btnFilterBy: String?) =
        RadioButton(label).apply {
            userData    = btnFilterBy
            toggleGroup = filterToggleGroup
            onAction    = EventHandler { filterBy = userData as String?; fillComboBox() }
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

    val contentPane = BorderPane(HBox(refreshButton, filesComboBox)).apply {
        styleClass.add("chooseOtherSetDialogPane")
        top = filterPane
    }

    val dialog = Dialog<Path>().apply {
        title = "Select cards' set to copy cards to"

        dialogPane.content = contentPane
        dialogPane.buttonTypes.addAll(
            ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE),
            ButtonType("Ok", ButtonBar.ButtonData.OK_DONE),
        )

        isResizable = true
        initDialogParentAndModality(this, parent)

        setResultConverter { buttonType ->
            if (buttonType.buttonData == ButtonBar.ButtonData.OK_DONE) filesComboBox.selectionModel.selectedItem else null
        }
    }

    Platform.runLater { filesComboBox.requestFocus() }

    return dialog.showAndWait().orElse(null)
}
