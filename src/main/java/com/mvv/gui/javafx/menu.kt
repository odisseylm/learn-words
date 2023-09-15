package com.mvv.gui.javafx

import javafx.event.EventHandler
import javafx.scene.control.CustomMenuItem
import javafx.scene.control.Label
import javafx.scene.control.MenuItem
import javafx.scene.control.Tooltip
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination


private val log = mu.KotlinLogging.logger {}


@Suppress("unused")
fun newMenuItem(label: String, action: ()->Unit): MenuItem =
    newMenuItemImpl(label = label, action = action)

@Suppress("unused")
fun newMenuItem(label: String, icon: ImageView, action: ()->Unit): MenuItem =
    newMenuItemImpl(label = label, icon = icon, action = action)

fun newMenuItem(label: String, tooltip: String, action: ()->Unit): MenuItem =
    newMenuItemImpl(label = label, tooltip = tooltip, action = action)

@Suppress("unused")
fun newMenuItem(label: String, keyCombination: KeyCombination, action: ()->Unit): MenuItem =
    newMenuItemImpl(label, null, null, keyCombination, action)

fun newMenuItem(label: String, icon: ImageView, keyCombination: KeyCombination, action: ()->Unit): MenuItem =
    newMenuItemImpl(label, null, icon, keyCombination, action)

fun newMenuItem(label: String, tooltip: String, icon: ImageView, action: ()->Unit): MenuItem =
    newMenuItemImpl(label, tooltip, icon, null, action)

fun newMenuItem(label: String, tooltip: String, icon: ImageView, keyCombination: KeyCodeCombination, action: ()->Unit): MenuItem =
    newMenuItemImpl(label, tooltip, icon, keyCombination, action)


private fun newMenuItemImpl(label: String, tooltip: String? = null, icon: ImageView? = null, keyCombination: KeyCombination? = null, action: (()->Unit)? = null): MenuItem =
    if (tooltip == null) MenuItem(label, icon ?: emptyIcon16x16())
    else {
        // Standard MenuItem class does not support tooltips,
        // but CustomMenuItem does not show accelerator :-)
        // T O D O: fix showing accelerator and tooltip together

        keyCombination?.also { log.warn { "Menu item key-combination [$keyCombination] most probably will not be shown." } }

        CustomMenuItem(Label(label, icon ?: emptyIcon16x16()).also {
            it.styleClass.add("custom-menu-item-content")
            Tooltip.install(it, Tooltip(tooltip))
        })
    }
    .also { menuItem ->
        keyCombination?.let { menuItem.accelerator = it }
        action?.let         { menuItem.onAction = EventHandler { it() } }
    }


