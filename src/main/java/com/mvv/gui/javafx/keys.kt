package com.mvv.gui.javafx

import javafx.event.EventType
import javafx.scene.control.Control
import javafx.scene.input.KeyCombination
import javafx.scene.input.KeyEvent



fun <C: Control> addKeyBinding(control: C, keyBinding: KeyCombination, action: (C)-> Unit) =
    addKeyBinding(control, KeyEvent.KEY_RELEASED, keyBinding, action)

fun <C: Control> addKeyBinding(control: C, keyBindings: Iterable<KeyCombination>, action: (C)-> Unit) =
    keyBindings.forEach { keyBinding -> addKeyBinding(control, KeyEvent.KEY_RELEASED, keyBinding, action) }

fun <C: Control> addKeyBinding(control: C, keyEventType: EventType<KeyEvent>, keyBinding: KeyCombination, action: (C)-> Unit) =
    addKeyBindings(control, setOf(keyEventType), mapOf(keyBinding to action))

fun <C: Control> addKeyBindings(control: C, keyBindings: Map<KeyCombination, (C)-> Unit>) =
    addKeyBindings(control, setOf(KeyEvent.KEY_RELEASED), keyBindings)

fun <C: Control> addKeyBindings(control: C, keyEventTypes: Set<EventType<KeyEvent>>, keyBindings: Map<KeyCombination, (C)-> Unit>) {
    if (control.scene != null)
        addKeyBindingsImpl(control, keyEventTypes, keyBindings)
    else
        // T O D O: would be nice to add protection from repeated call
        control.sceneProperty().addListener { _,_, newScene ->
            if (newScene != null) addKeyBindingsImpl(control, keyEventTypes, keyBindings) }
}

private fun <C: Control> addKeyBindingsImpl(control: C, keyEventTypes: Set<EventType<KeyEvent>>, keyBindings: Map<KeyCombination, (C)-> Unit>) =
    keyEventTypes.forEach { keyEventType -> // KeyEvent.KEY_PRESSED, KeyEvent.KEY_TYPED, KeyEvent.KEY_RELEASED

        val rootKeyCombinations: Set<KeyCombination> = control.scene.accelerators.keys
        val notRegisteredYetKeyBindings = keyBindings.filterNot { it.key in rootKeyCombinations }

        control.addEventHandler(keyEventType) {
            notRegisteredYetKeyBindings.forEach { (keyBinding, action) -> if (keyBinding.match(it)) action(control) }
        }
    }
