package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.javafx.*
import com.mvv.gui.util.isEnglishLetter
import com.mvv.gui.words.*
import javafx.beans.property.StringPropertyBase
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextArea
import javafx.scene.control.TextInputControl


fun LearnWordsController.moveSubTextToSeparateCard() {
    val focusOwner = pane.scene.focusOwner
    val editingCard = currentWordsList.editingItem
    val editingTableColumn: TableColumn<CardWordEntry, *>? = currentWordsList.editingCell?.column?.let { currentWordsList.columns[it] }

    if (editingCard != null && (editingTableColumn === currentWordsList.toColumn || editingTableColumn === currentWordsList.examplesColumn)
        && focusOwner is TextArea && focusOwner.belongsToParent(currentWordsList)) {
        moveSubTextToSeparateCard(focusOwner, editingCard, editingTableColumn)
    }
}


fun LearnWordsController.moveSubTextToSeparateCard(editor: TextInputControl, item: CardWordEntry, tableColumn: TableColumn<CardWordEntry, String>) {
    createCardFromSelectionOrCurrentLine(editor, tableColumn, item)
        ?.also { reanalyzeOnlyWords(it) }
        ?.adjustCard()
}


fun LearnWordsController.moveSubTextToExamplesAndSeparateCard(editor: TextInputControl, item: CardWordEntry, tableColumn: TableColumn<CardWordEntry, String>) {
    tryToAdToExamples(editor.selectionOrCurrentLine, item)
    moveSubTextToSeparateCard(editor, item, tableColumn)
}


private fun LearnWordsController.createCardFromSelectionOrCurrentLine(
    textInput: TextInputControl,
    tableColumn: TableColumn<CardWordEntry, String>,
    currentCard: CardWordEntry,
): CardWordEntry? {

    val currentWords: TableView<CardWordEntry> = currentWordsList

    val selectionOrCurrentLine = textInput.selectionOrCurrentLine
    if (selectionOrCurrentLine.isBlank()) return null

    val newCard: CardWordEntry = selectionOrCurrentLine.parseToCard()?.adjustCard() ?: return null

    val alreadyContains = currentWords.items.containsAlmostTheSameCard(newCard)
    println("### alreadyContains [${newCard.from} => ${newCard.to}]   - $alreadyContains")

    if (currentWords.items.containsAlmostTheSameCard(newCard)) return null

    if (textInput.selectedText.isEmpty()) {
        textInput.selectRange(textInput.selectionOrCurrentLineRange)
    }

    // replace/remove also ending '\n'
    if (textInput.selection.end < textInput.text.length && textInput.text[textInput.selection.end] == '\n')
        textInput.selectRange(textInput.selection.start, textInput.selection.end + 1)
    // verify again (in case of end "\n\n")
    if (textInput.selection.end < textInput.text.length && textInput.text[textInput.selection.end] == '\n')
        textInput.selectRange(textInput.selection.start, textInput.selection.end + 1)

    textInput.replaceSelection("")

    val caretPosition = textInput.caretPosition
    val scrollLeft = if (textInput is TextArea) textInput.scrollLeft else 0.0
    val scrollTop = if (textInput is TextArea) textInput.scrollTop else 0.0

    // After adding new card editor loses focus, and we will lose our changes
    // I do not know how to avoid this behaviour...
    // And for that reason I emulate cell commit and reopening it again.

    val prop: StringPropertyBase = tableColumn.cellValueFactory.call(
        TableColumn.CellDataFeatures(currentWords, tableColumn, currentCard)) as StringPropertyBase

    // emulate cell commit to avoid losing changes on focus lost
    prop.set(textInput.text)

    val currentCardIndex = currentWords.items.indexOf(currentCard)

    // We need this workaround because after adding new card current selected one looses focus, and we need to reselect it again...
    // And to avoid unneeded word's autoplaying we disable playing and re-enable it again after re-selecting card.
    val currentToPlayWordOnSelect = toPlayWordOnSelect
    toPlayWordOnSelect = false

    currentWords.runWithScrollKeeping(
        {
            // TODO: initially insert in proper place
            currentWords.items.add(currentCardIndex + 1, newCard)
        },
        {
            cellEditorStates[Pair(currentWordsList.examplesColumn, currentCard)] = CellEditorState(scrollLeft, scrollTop, caretPosition)

            currentWords.selectItem(currentCard)
            currentWords.edit(currentWords.items.indexOf(currentCard), tableColumn)

            runLaterWithDelay(50L) { toPlayWordOnSelect = currentToPlayWordOnSelect }
        })

    return newCard
}


fun List<CardWordEntry>.containsAlmostTheSameCard(newCard: CardWordEntry, compareExtraParams: Boolean = false): Boolean {
    val existentPossiblySameCards = this.filter { it.from == newCard.from }

    val sameCard = existentPossiblySameCards.find { isSenseAlmostTheSame(newCard, it, compareExtraParams) }
    return sameCard != null
}

fun isSenseAlmostTheSame(card1: CardWordEntry, card2: CardWordEntry, compareExtraParams: Boolean = false): Boolean {
    val isToAlmostSame = isSenseAlmostTheSame(card1.to, card2.to)
    if (!isToAlmostSame) return false

    val isExamplesAlmostSame = isSenseAlmostTheSame(card1.examples, card2.examples)
    if (!isExamplesAlmostSame) return false

    if (!compareExtraParams) return true

    val isSourcePositionsAlmostSame = card1.sourcePositions.toSet() == card2.sourcePositions.toSet()
    val isSourceSentencesAlmostSame = isSenseAlmostTheSame(card1.sourceSentences, card2.sourceSentences)
    return isSourcePositionsAlmostSame && isSourceSentencesAlmostSame
}

/*
fun isSenseAlmostTheSame(str1: CharSequence, str2: CharSequence): Boolean {

    var i1 = 0
    var i2 = 0
    var ch1: Char
    var ch2: Char

    fun Char.toLowerCaseOrSpace(): Char =
        if (this.isEnglishLetter() || this.isRussianLetter()) this.lowercaseChar() else ' '

    while (true) {
        ch1 = ' '
        ch2 = ' '

        while (i1 < str1.length && ch1 == ' ') {
            ch1 = str1[i1++].toLowerCaseOrSpace()
        }

        while (i2 < str2.length && ch2 == ' ') {
            ch2 = str2[i2++].toLowerCaseOrSpace()
        }

        when {
            ch1 == ' ' && ch2 == ' ' -> return true
            ch1 != ch2 -> return false
        }
    }
}
*/

enum class CompareSenseResult {
    AlmostSame,
    Better,
    Worse,
    Different,
}

fun isSenseAlmostTheSame(str1: CharSequence, str2: CharSequence): Boolean =
    doCompareSense(str1, str2) != CompareSenseResult.Different
fun CharSequence.isSenseBetter(another: CharSequence): CompareSenseResult =
    doCompareSense(this, another)

private fun doCompareSense(str1: CharSequence, str2: CharSequence): CompareSenseResult {

    var i1 = 0
    var i2 = 0
    var ch1: Char
    var ch2: Char
    var lowerCh1: Char
    var lowerCh2: Char

    var count1 = 0
    var upperCount1 = 0
    var count2 = 0
    var upperCount2 = 0
    //var areEqual = true

    fun Char.toLetterOrSpace(): Char =
        if (this.isEnglishLetter() || this.isRussianLetter()) this else ' '

    while (true) {
        ch1 = ' '; lowerCh1 = ' '
        ch2 = ' '; lowerCh2 = ' '

        while (i1 < str1.length && ch1 == ' ') {
            ch1 = str1[i1++].toLetterOrSpace()
            lowerCh1 = ch1.lowercaseChar()
        }

        while (i2 < str2.length && ch2 == ' ') {
            ch2 = str2[i2++].toLetterOrSpace()
            lowerCh2 = ch2.lowercaseChar()
        }

        if (lowerCh1 != ' ') {
            count1++
            if (lowerCh1 != ch1) upperCount1++
        }

        if (lowerCh2 != ' ') {
            count2++
            if (lowerCh2 != ch2) upperCount2++
        }

        when {
            lowerCh1 != lowerCh2 ->
                return CompareSenseResult.Different

            // end of any/both strings
            ch1 == ' ' && ch2 == ' ' ->
                return when {
                    upperCount1 > upperCount2 -> CompareSenseResult.Better
                    upperCount1 < upperCount2 -> CompareSenseResult.Worse
                    else -> CompareSenseResult.AlmostSame
                }

            else -> { }
        }
    }
}
