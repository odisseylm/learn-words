package com.mvv.gui.javafx

import javafx.event.EventType
import javafx.scene.Node
import javafx.scene.input.KeyCombination
import javafx.scene.input.KeyEvent



fun <C: Node> addLocalKeyBinding(control: C, keyBinding: KeyCombination, action: (C)-> Unit) =
    addLocalKeyBinding(control, KeyEvent.KEY_RELEASED, keyBinding, action)

fun <C: Node> addLocalKeyBinding(control: C, keyEventType: EventType<KeyEvent>, keyBinding: KeyCombination, action: (C)-> Unit) =
    addLocalKeyBindings(control, setOf(keyEventType), mapOf(keyBinding to action))

fun <C: Node> addLocalKeyBindings(control: C, keyBindings: Iterable<KeyCombination>, action: (C)-> Unit) =
    keyBindings.forEach { keyBinding -> addLocalKeyBinding(control, KeyEvent.KEY_RELEASED, keyBinding, action) }

fun <C: Node> addLocalKeyBindings(control: C, keyBindings: Map<KeyCombination, (C)-> Unit>) =
    addLocalKeyBindings(control, setOf(KeyEvent.KEY_RELEASED), keyBindings)

fun <C: Node> addLocalKeyBindings(control: C, keyEventTypes: Set<EventType<KeyEvent>>, keyBindings: Map<KeyCombination, (C)-> Unit>) {
    if (control.scene != null)
        addLocalKeyBindingsImpl(control, keyEventTypes, keyBindings)
    else
        // T O D O: would be nice to add protection from repeated call
        // We use some timeout to make sure that menu accelerators are already set up (to avoid repeating action's calls).
        control.sceneProperty().addListener { _,_, newScene ->
            if (newScene != null) runLaterWithDelay(100L) { addLocalKeyBindingsImpl(control, keyEventTypes, keyBindings) } }
}

private fun <C: Node> addLocalKeyBindingsImpl(control: C, keyEventTypes: Set<EventType<KeyEvent>>, keyBindings: Map<KeyCombination, (C)-> Unit>) =
    keyEventTypes.forEach { keyEventType -> // KeyEvent.KEY_PRESSED, KeyEvent.KEY_TYPED, KeyEvent.KEY_RELEASED

        val rootKeyCombinations: Set<KeyCombination> = control.scene.accelerators.keys
        val notRegisteredYetKeyBindings = keyBindings.filterNot { it.key in rootKeyCombinations }

        control.addEventHandler(keyEventType) {
            notRegisteredYetKeyBindings.forEach { (keyBinding, action) -> if (keyBinding.match(it)) action(control) }
        }
    }




fun <C: Node> addGlobalKeyBinding(control: C, keyBinding: KeyCombination, action: (C)-> Unit) =
    addGlobalKeyBindings(control, mapOf(keyBinding to action))

fun <C: Node> addGlobalKeyBindings(control: C, keyBindings: Iterable<KeyCombination>, action: (C)-> Unit) =
    keyBindings.forEach { keyBinding -> addGlobalKeyBinding(control, keyBinding, action) }

fun <C: Node> addGlobalKeyBindings(control: C, keyBindings: Map<KeyCombination, (C)-> Unit>) {
    if (control.scene != null)
        addGlobalKeyBindingsImpl(control, keyBindings)
    else
        // T O D O: would be nice to add protection from repeated call
        control.sceneProperty().addListener { _,_, newScene ->
            // We use some timeout to make sure that menu accelerators are already set up,
            // however for global binding it is not so important since later binding just overwrites previous one
            // because bindings are just kept in simple map
            if (newScene != null) runLaterWithDelay(100L) { addGlobalKeyBindingsImpl(control, keyBindings) } }
}

private fun <C: Node> addGlobalKeyBindingsImpl(control: C, keyBindings: Map<KeyCombination, (C)-> Unit>) =
    keyBindings.forEach { (keyBinding, action) -> control.scene.accelerators.putIfAbsent(keyBinding) { action(control) } }
