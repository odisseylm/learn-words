package com.mvv.gui

import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.cell.TextFieldTableCell
import javafx.util.Callback


class LabelStatusTableCell<S,T>(private val updateCellAttrs: (TableCell<S,T>,S,T)->Unit) : TextFieldTableCell<S, T>() {

    //override fun updateSelected(selected: Boolean) {
    //    super.updateSelected(selected)
    //    updateCellAttrs(this, tableRow.item, item)
    //}

    override fun updateItem(item: T, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || tableRow == null) {
            text = null
            setGraphic(null)
        } else {
            text = item.toString()
            updateCellAttrs(this, tableRow.item, item)
        }
    }

    companion object {
        fun <S, T> forTableColumn(updateCellAttrs: (TableCell<S,T>,S,T)->Unit /*, converter: StringConverter<T>*/): Callback<TableColumn<S, T>, TableCell<S, T>> =
            Callback { _: TableColumn<S, T>? -> LabelStatusTableCell(updateCellAttrs) }
    }
}
