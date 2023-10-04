package com.mvv.gui.javafx

import com.mvv.gui.CellEditorState
import javafx.application.Platform
import javafx.scene.control.TextArea
import javafx.scene.control.TextInputControl


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
