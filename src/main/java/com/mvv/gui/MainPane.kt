package com.mvv.gui

import com.mvv.gui.words.WarnAboutMissedBaseWordsMode
import com.mvv.gui.words.WarnAboutMissedBaseWordsMode.NotWarnWhenSomeBaseWordsPresent
import com.mvv.gui.words.WarnAboutMissedBaseWordsMode.WarnWhenSomeBaseWordsMissed
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.ComboBox
import javafx.scene.control.ListView
import javafx.scene.control.SelectionMode
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.util.StringConverter
import org.apache.commons.lang3.NotImplementedException


const val appTitle = "Words"

//private val log = mu.KotlinLogging.logger {}


class MainWordsPane(val controller: LearnWordsController) : BorderPane() {

    internal val wordEntriesLabel = Text("File/Clipboard")
    internal val ignoredWordsLabel = Text("Ignored words")
    internal val allProcessedWordsLabel = Text("All processed words")

    internal val topPane = VBox()

    internal val wordEntriesTable = WordCardsTable(controller)

    internal val ignoredWordsList = ListView<String>()
    internal val allProcessedWordsList = ListView<String>()

    private val warnWordEntryCountsTextFormat = " (%d words with warning)"
    private val warnWordEntryCountsText = Text(warnWordEntryCountsTextFormat)
    private val warnWordEntryCountsTextItems = listOf(
        Text("      "),
        ImageView("/icons/warning_obj.gif").also { it.translateY = 3.0 },
        warnWordEntryCountsText,
    )

    internal fun updateWarnWordCount(wordCountWithWarning: Int) {
        val shouldBeVisible = (wordCountWithWarning != 0)
        warnWordEntryCountsTextItems.forEach { it.isVisible = shouldBeVisible; it.isManaged = shouldBeVisible }
        warnWordEntryCountsText.text = warnWordEntryCountsTextFormat.format(wordCountWithWarning)
    }

    internal val warnAboutMissedBaseWordsModeDropDown = ComboBox<WarnAboutMissedBaseWordsMode>().also {
        it.items.setAll(WarnAboutMissedBaseWordsMode.values().toList())
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

        val wordEntriesTopHeader = BorderPane()

        val currentWordsLabelsTextFlow = TextFlow(
            *(listOf(wordEntriesLabel) + warnWordEntryCountsTextItems).toTypedArray())

        wordEntriesTopHeader.center = BorderPane().also {
            // Workaround with using BorderPane as container/wrapper over TextFlow
            // since TextFlow does not have possibility to vertically align content to the bottom.
            it.bottom = currentWordsLabelsTextFlow }

        updateWarnWordCount(0) // hide WarnWordCount by default


        wordEntriesTopHeader.right = warnAboutMissedBaseWordsModeDropDown
        contentPane.add(wordEntriesTopHeader, 0, 0)

        GridPane.setFillWidth(wordEntriesTable, true)
        GridPane.setHgrow(wordEntriesTable, Priority.ALWAYS)
        contentPane.add(wordEntriesTable, 0, 1, 1, 3)


        this.top = topPane
        //topPane.children.add(toolBar)

        val buttonsMiddleBar = VBox(5.0)
        buttonsMiddleBar.isFillWidth = true


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

        wordEntriesTable.id = "currentWords"
        wordEntriesTable.isEditable = true
        wordEntriesTable.selectionModel.selectionMode = SelectionMode.MULTIPLE

        GridPane.setFillWidth(wordEntriesTable, true)
        GridPane.setHgrow(wordEntriesTable, Priority.ALWAYS)
        wordEntriesTable.prefWidth = 10_000.0

        this.center = contentPane
    }

}


internal class ListStringConverter<T> : StringConverter<List<T>>() {
    override fun toString(value: List<T>?): String = value?.joinToString(", ") ?: ""

    override fun fromString(string: String?): List<T> = throw IllegalStateException("Unsupported.")
}

