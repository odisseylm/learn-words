package com.mvv.gui

import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination


val lowerCaseKeyCombination = KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN)
val translateSelectedKeyCombination = KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN)
val copyKeyCombinations = listOf(
    KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN),
    KeyCodeCombination(KeyCode.INSERT, KeyCombination.CONTROL_DOWN),
)
