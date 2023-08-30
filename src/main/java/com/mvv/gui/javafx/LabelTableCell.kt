package com.mvv.gui.javafx

import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.cell.TextFieldTableCell
import javafx.util.Callback
import javafx.util.StringConverter


class LabelStatusTableCell<S,T>(private val stringConverter: StringConverter<T>, private val updateCellAttrs: (TableCell<S,T>,S,T?)->Unit) : TextFieldTableCell<S, T>() {

    override fun updateItem(item: T?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || tableRow == null) {
            text = null
            setGraphic(null)
        } else {
            text = stringConverter.toString(item)
            updateCellAttrs(this, tableRow.item, item)
        }
    }

    companion object {
        @Suppress("MemberVisibilityCanBePrivate")
        fun <S, T> forTableColumn(stringConverter: StringConverter<T>, updateCellAttrs: (TableCell<S,T>, S, T?)->Unit): Callback<TableColumn<S, T>, TableCell<S, T>> =
            Callback { _: TableColumn<S, T>? -> LabelStatusTableCell(stringConverter, updateCellAttrs) }
        fun <S, T> forTableColumn(updateCellAttrs: (TableCell<S,T>,S,T?)->Unit): Callback<TableColumn<S, T>, TableCell<S, T>> =
            forTableColumn<S, T>(DefaultReadOnlyObjectStringConverter<T>(), updateCellAttrs)
    }
}


class DefaultReadOnlyObjectStringConverter<T> : StringConverter<T>() {
    override fun toString(value: T): String = value?.toString() ?: ""

    override fun fromString(string: String?): T =
        throw IllegalStateException("fromString is not implemented.")
}

class EmptyTextStringConverter<T> : StringConverter<T>() {
    override fun toString(value: T): String = ""

    override fun fromString(string: String?): T =
        throw IllegalStateException("fromString is not implemented.")
}

