@file:Suppress("unused")

package com.mvv.gui.javafx

import javafx.scene.Node
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.input.MouseEvent
import javafx.util.Callback
import javafx.util.StringConverter


fun label(
    label: String,
    styleClass: String? = null,
): Label = Label(label).apply {
    if (styleClass != null) this.styleClass.add(styleClass)
}


var Node.onDoubleClicked: ((MouseEvent)->Unit)
    get() { throw NotImplementedError() }
    set(value) = addEventHandler(MouseEvent.MOUSE_CLICKED) { if (it.clickCount >= 2) value(it) }

fun <T> listView(
    items: Iterable<T>,
    isEditable: Boolean ?= null,
    cellFactory: Callback<ListView<T>, ListCell<T>>? = null,
    onDoubleClicked: ((MouseEvent)->Unit)? = null,
) = ListView<T>().apply {
    this.items.addAll(items)
    if (isEditable  != null) this.isEditable  = isEditable
    if (cellFactory != null) this.cellFactory = cellFactory

    if (onDoubleClicked != null) this.onDoubleClicked = onDoubleClicked
}

fun <T> listView(adjust: ListView<T>.()->Unit) = ListView<T>().apply { this.adjust() }


fun <T> comboBox(
    items: Iterable<T>,
    isEditable: Boolean ?= null,
    cellFactory: Callback<ListView<T>, ListCell<T>>? = null,
    converter: StringConverter<T>? = null,
) = ComboBox<T>().apply {
    this.items.addAll(items)
    if (isEditable  != null) this.isEditable  = isEditable
    if (cellFactory != null) this.cellFactory = cellFactory
    if (converter   != null) this.converter   = converter
}

fun <T> comboBox(adjust: ComboBox<T>.()->Unit) = ComboBox<T>().apply { this.adjust() }
