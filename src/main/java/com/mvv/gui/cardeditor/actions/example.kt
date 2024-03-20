package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.javafx.*
import com.mvv.gui.util.containsOneOf
import com.mvv.gui.words.CardWordEntry
import com.mvv.gui.words.examples
import javafx.scene.control.TableColumn
import javafx.scene.control.TextArea
import javafx.scene.control.TextInputControl


internal fun String.splitExamples(): List<String> {
    if (this.isBlank()) return emptyList()

    val lines = this.split("\n")
    if (lines.isEmpty()) return emptyList()

    val separatedExamples = mutableListOf<String>()
    var currentExample = ""

    for (line in lines) {

        val lineIsBlank = line.isBlank()
        val continueOfPreviousExample = !lineIsBlank && line.startsWith(' ')

        if (lineIsBlank) { // end of previous example
            if (currentExample.isNotBlank())
                separatedExamples.add(currentExample.trim())
            currentExample = ""
        }

        else if (continueOfPreviousExample) { // next line of previous comment
            currentExample += "\n"
            currentExample += line.trimEnd()
        }

        else { // just next example
            if (currentExample.isNotBlank())
                separatedExamples.add(currentExample.trim())
            currentExample = line.trim()
        }
    }

    if (currentExample.isNotBlank())
        separatedExamples.add(currentExample.trim())

    return separatedExamples
}


fun LearnWordsController.moveSubTextToExamplesAndSeparateCard() {
    val focusOwner = pane.scene.focusOwner
    val editingCard = currentWordsList.editingItem
    val editingTableColumn: TableColumn<CardWordEntry, *>? = currentWordsList.editingCell?.column?.let { currentWordsList.columns[it] }

    if (editingCard != null && (editingTableColumn === currentWordsList.toColumn)
        && focusOwner is TextArea && focusOwner.belongsToParent(currentWordsList)) {
        moveSubTextToExamplesAndSeparateCard(focusOwner, editingCard, editingTableColumn)
    }
}


fun LearnWordsController.moveSubTextToExamples() {
    val focusOwner = pane.scene.focusOwner
    val editingCard = currentWordsList.editingItem
    val editingTableColumn: TableColumn<CardWordEntry, *>? = currentWordsList.editingCell?.column?.let { currentWordsList.columns[it] }

    if (editingCard != null && editingTableColumn === currentWordsList.toColumn
        && focusOwner is TextArea && focusOwner.belongsToParent(currentWordsList)) {
        moveSubTextToExamples(editingCard, focusOwner)
    }
}


internal fun moveSubTextToExamples(card: CardWordEntry, textInput: TextInputControl) {
    val textRange = textInput.selectionOrCurrentLineRange

    if (tryToAdToExamples(textInput.selectionOrCurrentLine, card)) {
        //textInput.selectRange(textInput.selectionOrCurrentLineRange)
        textInput.replaceText(textRange, "")
    }
}


fun tryToAdToExamples(example: String, card: CardWordEntry): Boolean {
    if (example.isBlank()) return false

    val examples = card.examples
    val fixedExample = example
        .trim().removePrefix(";").removeSuffix(";")
    val preparedExamplesToMove = fixedExample
        .trim().replace(';', '\n')

    // TODO: use all possible combination of this replacements (the easiest way use numbers as binary number)
    //val possibleReplacements: List<Map<String,String>> = listOf(
    //    mapOf(";" to "\n"),
    //    mapOf("а>" to "а)", "б>" to "б)", "в>" to "в)", "г>" to "г)", "д>" to "д)", "е>" to "е)", ),
    //    mapOf("а>" to "\n", "б>" to "\n", "в>" to "\n", "г>" to "\n", "д>" to "\n", "е>" to "\n", ),
    //    mapOf("а>" to "", "б>" to "", "в>" to "", "г>" to "", "д>" to "", "е>" to "", ),
    //    mapOf(
    //      юр юрид уст спорт амер полит жарг театр воен ист фон посл парл австрал шутл полигр
    //      "(_юр.)" to "(юр.)", "_юр." to "(юр.)",
    //      ),
    //)

    if (examples.containsOneOf(example, fixedExample, preparedExamplesToMove)) return false

    val separator = when {
        examples.isBlank() -> ""
        examples.endsWith("\n\n") -> ""
        examples.endsWith("\n") -> "\n"
        else -> "\n\n"
    }
    val endLine = if (preparedExamplesToMove.endsWith('\n')) "" else "\n"

    card.examples += "$separator$preparedExamplesToMove$endLine"

    return true
}
