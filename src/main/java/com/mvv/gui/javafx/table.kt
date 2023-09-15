package com.mvv.gui.javafx

import javafx.scene.control.TableView


val TableView<*>.isEditing: Boolean get() {
    val editingCell = this.editingCell
    return (editingCell != null) && editingCell.row != -1 && editingCell.column != -1
}


val <T> TableView<T>.singleSelection: T? get() {
    val selected = this.selectionModel.selectedItems
    return if (selected.size == 1) selected.first() else null
}


