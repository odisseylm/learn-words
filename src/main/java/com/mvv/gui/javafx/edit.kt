package com.mvv.gui.javafx

import com.mvv.gui.util.ifIndexNotFound
import javafx.application.Platform
import javafx.scene.control.IndexRange
import javafx.scene.control.TextArea
import javafx.scene.control.TextInputControl
import kotlin.math.max
import kotlin.math.min



fun toggleCase(textInput: TextInputControl) =
    doSelectionOrAllTextAction(textInput) { it.toggleCase() }

fun String.toggleCase(): String {
    if (this.isBlank()) return this

    val allLowerCase = this.all { it.isLowerCase() || !it.isLetter() }
    return if (allLowerCase) this.uppercase() else this.lowercase()
}


/*
fun toLowerCase(textInput: TextInputControl) {

    val currentText = textInput.text
    val hasUpperCaseChars = currentText.any { it.isUpperCase() }
    if (!hasUpperCaseChars) return

    val caretPosition = textInput.caretPosition
    val anchor = textInput.anchor

    textInput.replaceText(0, textInput.text.length, currentText.lowercase())

    //textInput.text = currentText.lowercase()
    textInput.selectRange(anchor, caretPosition)
}
*/

internal fun doSelectionOrAllTextAction(textInput: TextInputControl, action:  (String)->String): Boolean =
    if (textInput.selectedText.isNotBlank())
        doSelectionAction(textInput, action)
    else
        doAllTextAction(textInput, action)

internal fun doSelectionAction(textInput: TextInputControl, action:  (String)->String): Boolean {

    val caretPosition = textInput.caretPosition
    val anchor = textInput.anchor

    val selectedText = textInput.selectedText
    if (selectedText.isBlank()) return false

    val transformedText = action(selectedText)
    if (transformedText == selectedText) return false

    textInput.replaceSelection(transformedText)

    textInput.selectRange(anchor, caretPosition)
    return true
}

internal fun doAllTextAction(textInput: TextInputControl, action:  (String)->String): Boolean {

    val currentText = textInput.text

    val transformedText = action(currentText)
    if (transformedText == currentText) return false

    val caretPosition = textInput.caretPosition
    val anchor = textInput.anchor

    textInput.replaceText(0, textInput.text.length, transformedText)
    textInput.selectRange(anchor, caretPosition)

    return true
}


val TextInputControl.selectionOrCurrentLine: String get() =
    this.text.substring(this.selectionOrCurrentLineRange)

val TextInputControl.selectionOrCurrentLineRange: IntRange get() {
    val currentText = this.text

    return if (this.selectedText.isNotEmpty())
        this.selection.toIntRange()
    else {
        val startLineIndex = currentText.lastIndexOf('\n', 0, caretPosition) + 1
        val lastLineIndexInclusive = if (currentText[caretPosition] == '\n') caretPosition
        else currentText.indexOf('\n', caretPosition)
                        .ifIndexNotFound { currentText.length - 1 }
        IntRange(startLineIndex, lastLineIndexInclusive)
    }
}


fun TextInputControl.replaceText(range: IntRange, text: String) =
    this.replaceText(range.toIndexRange(), text)

fun TextInputControl.selectRange(range: IntRange) =
    // !!! '+ 1' is needed because it is caret position !!!
    this.selectRange(min(range.first, range.last), max(range.first, range.last) + 1)


fun IndexRange.toIntRange(): IntRange = IntRange(this.start, this.end - 1)
fun IntRange.toIndexRange(): IndexRange = IndexRange(min(this.first, this.last), max(this.first, this.last) + 1)

// to easy port java code to kotlin
val IntRange.endExclusive: Int get() = max(this.first, this.last) + 1

fun copySelectedOrCurrentLine(textInput: TextInputControl) {

    val currentText = textInput.text
    if (currentText.isBlank()) return

    val selectedText = textInput.selectedText
    if (selectedText.isNotEmpty()) {
        val caretPosition = textInput.caretPosition
        val anchor = textInput.anchor

        textInput.insertText(min(caretPosition, anchor), selectedText)
        textInput.selectRange(anchor + selectedText.length, caretPosition + selectedText.length)
    }
    else {
        val caretPosition = textInput.caretPosition
        val anchor = textInput.anchor

        val startLineIndex = currentText.lastIndexOf('\n', 0, caretPosition) + 1
        val lastLineIndexInclusive =
            if (currentText[caretPosition] == '\n') caretPosition
            else currentText.indexOf('\n', caretPosition)
                            .ifIndexNotFound { currentText.length - 1 }

        val currentLine = currentText.substring(startLineIndex, lastLineIndexInclusive + 1)
            .let { if (it.endsWith('\n')) it else it + '\n' }

        textInput.insertText(startLineIndex, currentLine)
        textInput.selectRange(anchor + selectedText.length, caretPosition + selectedText.length)
    }
}


fun removeCurrentLine(textInput: TextArea) {

    val currentText = textInput.text
    if (currentText.isBlank()) return

    val caretPosition = textInput.caretPosition
    val anchor = textInput.anchor

    val startLineIndex = currentText.lastIndexOf('\n', 0, min(anchor, caretPosition)) + 1
    val lastLineIndexInclusive = currentText.indexOf('\n', max(anchor, caretPosition))
                                            .ifIndexNotFound { currentText.length - 1 }

    textInput.deleteText(startLineIndex, lastLineIndexInclusive + 1)
    textInput.selectRange(startLineIndex, startLineIndex)
}


/**
 * @param endIndexExclusive exclusive
 */
private fun String.lastIndexOf(char: Char, startIndex: Int, endIndexExclusive: Int): Int {
    if (endIndexExclusive  <= 0) return -1

    for (i in endIndexExclusive - 1 downTo startIndex) {
        if (this[i] == char) return i
    }

    return -1
}


data class CellEditorState (
    val scrollLeft: Double,
    val scrollTop: Double,
    val caretPosition: Int,
)


fun <StateKey> TextInputControl.keepCellEditorState(stateKey: ()->StateKey, stateKeyContainer: MutableMap<StateKey, CellEditorState>) {

    val editor = this

    editor.focusedProperty().addListener { _, _, isFocused ->
        val textArea = editor as TextArea

        if (isFocused) {
            val editorState = stateKeyContainer.getOrElse(stateKey()) {
                CellEditorState(0.0, 0.0, 0) }

            val caretPos = if (editorState.caretPosition > editor.text.length)
                editor.text.trimEnd().lastIndexOf('\n')
            else editorState.caretPosition

            textArea.selectRange(caretPos, caretPos)
            textArea.selectPositionCaret(caretPos)
            textArea.scrollLeft = editorState.scrollLeft
            textArea.scrollTop = editorState.scrollTop

            // hacks hacks hacks :-(
            // double-take
            Platform.runLater {
                textArea.selectRange(caretPos, caretPos)
                textArea.selectPositionCaret(caretPos)

                // hack. Sometimes editor loses focus (after re-scrolling table row)
                if (!editor.isFocused) editor.requestFocus()

                // hack again. Sometimes setting scrollTop really does not change scroll position

                // This IF does not help - real scroll position is not synchronized with property textArea.scrollTop :-(
                //if (textArea.scrollTop != editorState.scrollTop) {
                    textArea.scrollTop = 0.0
                    textArea.scrollTop = editorState.scrollTop
                //}
            }
        }
        else {
            stateKeyContainer[stateKey()] =
                CellEditorState(textArea.scrollLeft, textArea.scrollTop, textArea.caretPosition)
        }
    }
}
