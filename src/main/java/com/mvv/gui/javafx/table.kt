package com.mvv.gui.javafx

import javafx.scene.control.TableCell
import javafx.scene.control.TableView


val TableView<*>.isEditing: Boolean get() {
    val editingCell = this.editingCell
    return (editingCell != null) && editingCell.row != -1 && editingCell.column != -1
}


@Suppress("unused")
val <S> TableView<S>.editingItem: S? get() {
    val editingCell = this.editingCell
    return if (editingCell != null) this.items.getOrNull(editingCell.row) else null
}


val <T> TableView<T>.singleSelection: T? get() {
    val selected = this.selectionModel.selectedItems
    return if (selected.size == 1) selected.first() else null
}


enum class IndexStartFrom { Zero, One }

fun <S> createIndexTableCell(startFrom: IndexStartFrom): TableCell<S,Int> =
    object : TableCell<S, Int>() {
        @Override
        override fun updateIndex(index: Int) {
            super.updateIndex(index)
            if (isEmpty || index < 0)
                setText(null)
            else {
                val showIndex = if (startFrom == IndexStartFrom.Zero) index else  index + 1
                setText(showIndex.toString())
            }
        }
    }
