package com.mvv.gui

import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination


val newDocumentKeyCodeCombination = KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN)
val openDocumentKeyCodeCombination = KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN)
val saveDocumentKeyCodeCombination = KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN)

val previousNavigationKeyCodeCombination = KeyCodeCombination(KeyCode.LEFT, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN)
val nextNavigationKeyCodeCombination = KeyCodeCombination(KeyCode.RIGHT, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN)

val lowerCaseKeyCombination = KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN)
val copySelectedOrCurrentLineCombination = KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN)
val removeCurrentLineCombination = KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN)

val translateSelectedKeyCombination = KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN)

/** SubText = current line or selected text */
val moveSubTextToExamplesKeyCombination = listOf(
    KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN),
    KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN))

val moveSubTextToSeparateCardKeyCombination = listOf(
    KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN),
    KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN))

val moveSubTextToExamplesAndSeparateCardKeyCombination = listOf(
    KeyCodeCombination(KeyCode.J, KeyCombination.CONTROL_DOWN),
    KeyCodeCombination(KeyCode.J, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN))
