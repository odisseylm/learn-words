package com.mvv.gui.javafx

import javafx.scene.control.TextInputControl


fun toLowerCase(textInput: TextInputControl) {

    val currentText = textInput.text
    val hasUpperCaseChars = currentText.any { it.isUpperCase() }
    if (!hasUpperCaseChars) return

    val caretPosition = textInput.caretPosition
    val anchor = textInput.anchor

    textInput.text = currentText.lowercase()
    textInput.selectRange(anchor, caretPosition)
}


