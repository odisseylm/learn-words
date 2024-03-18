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

    val allSetsParentsAreParentOfCurrent = (allOtherSetsParents.size == 1) && (allOtherSetsParents[0] == currentWordsFileParent)

    val pathToSetNameConv = object : StringConverter<Path>() {
        override fun toString(value: Path?): String = when {
            allSetsParentsAreParentOfCurrent -> value?.baseWordsFilename
            else -> value?.toString()?.removePrefix(commonAllOtherSetsSubParent.toString())?.removePrefix("/")
        } ?: ""
        override fun fromString(string: String?): Path? = if (string.isNullOrBlank()) null else Path.of(string.trim())
    }

    var otherRealSetsFiles: List<Path>? = null

    fun toFilter(filterString: String?): List<Path> {
        val otherRealSetsFilesSafeRef = otherRealSetsFiles
        val otherSetsFiles = otherRealSetsFilesSafeRef ?: otherCachedSetsFiles

        val files =
            if (filterString == "/") {
                otherSetsFiles.filterNot {
                    pathToSetNameConv.toString(it).containsOneOf("/", "\\")
                }
            }
            else {
                val filterStringAlt = filterString?.replace('/', '\\') ?: ""
                otherSetsFiles.filter {
                    filterString.isNullOrBlank() || it.toString().containsOneOf(filterString, filterStringAlt)
                }
            }

        return files
    }

    fun getDictionarySets(): List<Path> {
        val ignoredFileBaseName = allWordCardSetsManager.ignoredFile?.baseWordsFilename
        return getAllExistentSetFiles(includeMemoWordFile = false, toIgnoreBaseWordsFilename = ignoredFileBaseName)
            .sortedWith(PathCaseInsensitiveComparator())
    }


    val dialog = Dialog<Path>()
    dialog.title = "Select cards' set to copy cards to"

    val filesComboBox = ComboBox<Path>().also {
        it.items.setAll(otherCachedSetsFiles)
        it.isEditable = true
        it.converter  = pathToSetNameConv
    }

    var filterBy: String? = null
    fun fillComboBox() { filesComboBox.items.setAll(toFilter(filterBy)) }

    val refreshButton = newButton(buttonIcon("icons/iu_update_obj.png")) { // iu_update_obj.png arrow(9).png arrow(12).png
        otherRealSetsFiles = getDictionarySets()
        fillComboBox()
    }
    refreshButton.styleClass.add("refreshFilesButton")

    val toggleGroup = ToggleGroup()

    val filterRadioButtons = listOf(
        RadioButton("All")       .also { it.isSelected = true },
        RadioButton("Root")      .also { it.userData = "/"            },
        RadioButton("Grouped")   .also { it.userData = "/grouped/"    },
        RadioButton("Synonyms")  .also { it.userData = "/synonyms/"   },
        RadioButton("Base verbs").also { it.userData = "/base-verbs/" },
        RadioButton("Films")     .also { it.userData = "/films/"      },
        RadioButton("Homophones").also { it.userData = "/homophones/" },
    )
    filterRadioButtons.forEach { radioButton ->
        radioButton.toggleGroup = toggleGroup
        radioButton.onAction = EventHandler { filterBy = radioButton.userData as String?; fillComboBox() }
    }

    val filterPane = FlowPane()
    filterPane.hgap = 20.0
    filterPane.vgap = 10.0
    filterPane.children.addAll(filterRadioButtons)

    val contentPane = BorderPane(HBox(refreshButton, filesComboBox))
    contentPane.styleClass.add("chooseOtherSetDialogPane")
    contentPane.top = filterPane.also { it.styleClass.add("filterPane") }

    dialog.dialogPane.content = contentPane
    dialog.dialogPane.buttonTypes.addAll(
        ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE),
        ButtonType("Ok", ButtonBar.ButtonData.OK_DONE),
    )

    dialog.isResizable = true
    initDialogParentAndModality(dialog, parent)

    dialog.setResultConverter { buttonType ->
        if (buttonType.buttonData == ButtonBar.ButtonData.OK_DONE) filesComboBox.selectionModel.selectedItem else null
    }

    Platform.runLater { filesComboBox.requestFocus() }

    return dialog.showAndWait().orElse(null)
}
