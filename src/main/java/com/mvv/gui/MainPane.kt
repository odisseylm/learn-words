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

    internal val currentWordsLabel = Text("File/Clipboard")
    internal val ignoredWordsLabel = Text("Ignored words")
    internal val allProcessedWordsLabel = Text("All processed words")

    internal val topPane = VBox()

    internal val currentWordsList       = WordCardsTable(controller)

    internal val ignoredWordsList = ListView<String>()
    internal val allProcessedWordsList = ListView<String>()

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

        GridPane.setFillWidth(currentWordsList, true)
        GridPane.setHgrow(currentWordsList, Priority.ALWAYS)
        contentPane.add(currentWordsList, 0, 1, 1, 3)


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

        currentWordsList.id = "currentWords"
        currentWordsList.isEditable = true
        currentWordsList.selectionModel.selectionMode = SelectionMode.MULTIPLE

        GridPane.setFillWidth(currentWordsList, true)
        GridPane.setHgrow(currentWordsList, Priority.ALWAYS)
        currentWordsList.prefWidth = 10_000.0

        this.center = contentPane
    }

}


internal class ListStringConverter<T> : StringConverter<List<T>>() {
    override fun toString(value: List<T>?): String = value?.joinToString(", ") ?: ""

    override fun fromString(string: String?): List<T> = throw IllegalStateException("Unsupported.")
}

