package com.mvv.gui.javafx

import javafx.application.Platform
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import kotlin.math.max
import kotlin.math.min


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


val <S> TableView<S>.visibleRows: IntRange get() {
    val rowIndex1 = this.virtualFlow?.firstVisibleCell?.index ?: -1
    val rowIndex2 = this.virtualFlow?.lastVisibleCell?.index ?: -1
    return IntRange(min(rowIndex1, rowIndex2), max(rowIndex1, rowIndex2))
}


/** Selects and scrolls to this item. */
fun <S> TableView<S>.selectItem(item: S) {
    val table = this
    table.selectionModel.clearSelection()
    table.selectionModel.select(item)
    val rowIndex = table.items.indexOf(item)

    if (rowIndex != -1 && !table.visibleRows.contains(rowIndex))
        table.scrollTo(item)
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


fun <S,T> fixSortingAfterCellEditCommit(column: TableColumn<S, T>) {

    column.addEventHandler(TableColumn.CellEditEvent.ANY) {

        if (it.eventType == TableColumn.editCommitEvent<S,T>()) {

            //val index = column.tableView?.editingCell?.row ?: -1
            //val editedEntry: S? = if (index >= 0) column.tableView.items[index] else null

            Platform.runLater {
                column.tableView.sort()

                //if (editedEntry != null) column.tableView.selectionModel.select(editedEntry)

                // seems bug in JavaFx, after cell edition complete table view looses focus
                if (!column.tableView.isFocused) column.tableView.requestFocus()
            }
        }
    }
}
