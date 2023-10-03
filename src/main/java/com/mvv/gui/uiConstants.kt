package com.mvv.gui

import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination


val newDocumentKeyCodeCombination = KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN)
val openDocumentKeyCodeCombination = KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN)
val saveDocumentKeyCodeCombination = KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN)

val lowerCaseKeyCombination = KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN)
val translateSelectedKeyCombination = KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN)

val moveSelectedTextToExamplesKeyCombination = listOf(
    KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN),
    KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN))

val moveSelectedToSeparateCardKeyCombination = listOf(
    KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN),
    KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN))
