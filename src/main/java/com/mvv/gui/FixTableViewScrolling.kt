package com.mvv.gui

import com.mvv.gui.SetViewPortAbsoluteOffsetMode.*
import javafx.application.Platform
import javafx.scene.control.TableView
import javafx.scene.control.skin.VirtualFlow
import javafx.scene.control.skin.absoluteOffsetValue
import javafx.scene.control.skin.callAdjustPosition
import mu.KotlinLogging


private val log = KotlinLogging.logger {}



// We can make viewPortAbsoluteOffset public if it is really needed.
//
internal var <S> TableView<S>.viewPortAbsoluteOffset: Double?
    get() = this.virtualFlow?.absoluteOffsetValue
    set(value) { value?.let { this.setViewPortAbsoluteOffsetImpl(value) } }

internal val <S> TableView<S>.virtualFlow: VirtualFlow<*>? get() = lookupAll("VirtualFlow")
    .filterIsInstance<VirtualFlow<*>>()
    .firstOrNull { it.isVertical }


enum class SetViewPortAbsoluteOffsetMode {
    /** Setting is performed immediately */
    /** Setting is performed immediately */
    Immediately,
    /** Setting is performed later using Platform.runLater() and probably with runWithDelay(several millis) */
    Later,
    /** Setting is performed immediately (to avoid or minimize blinking)
      * and to make sure it is set again a bit later.
      */
    ImmediatelyAndLater,
}


internal fun <S> TableView<S>.setViewPortAbsoluteOffsetImpl(
    absoluteOffset: Double, nextAction: ()->Unit = { }, setMode: SetViewPortAbsoluteOffsetMode = ImmediatelyAndLater
) {

    val virtualFlow = this.virtualFlow
    if (virtualFlow == null) {
        log.warn("VirtualFlow is not found")
        return
    }

    fixEstimatedContentHeight()

    fun setAdjustPosIml(pos: Double) {
        virtualFlow.absoluteOffsetValue = pos
        virtualFlow.callAdjustPosition()
    }

    // !!! These IFs cannot be replaced with 'when' !!!
    //
    if (setMode == Immediately) {
        setAdjustPosIml(absoluteOffset)
        nextAction()
    }

    if (setMode == ImmediatelyAndLater) {
        setAdjustPosIml(absoluteOffset)
        // nextAction() will be called later
    }

    if (setMode == Later
     || setMode == ImmediatelyAndLater) {

        // If it was not picked up immediately (due to conflicts with other table deferred changes).
        Platform.runLater { setAdjustPosIml(absoluteOffset) }

        // If it was not picked up immediately after Platform.runLater() (due to conflicts with other table deferred changes).
        runLaterWithDelay(25) {
            setAdjustPosIml(absoluteOffset)
            Platform.runLater(nextAction)
        }
    }
}


// JavaFx has bug when after adding new item its estimated content height (see VirtualFlow.estimatedSize)
// is broken (for example, before adding item estimatedSize=4500, after adding new item is estimatedSize=3500)
// For that reason we need to recalculate it properly.
// The simplest known for me solution is just scroll over all items/rows to recalculate them again.
// TODO: unsafe method with scrolling over all rows, we need to find better way
private fun <S> TableView<S>.fixEstimatedContentHeight() {
    val safeItemsCopy = this.items.toList()

    // It does not work
    //val virtualFlow = this.virtualFlow
    //safeItemsCopy.forEachIndexed { index, _ -> virtualFlow?.getCell(index) }

    safeItemsCopy.forEach { this.scrollTo(it) }
}


typealias RestoreScrollPositionFunction = ()->Unit


fun <R, S> TableView<S>.runWithScrollKeeping(action: (RestoreScrollPositionFunction)->R): R =
    this.runWithScrollKeeping(action, { })


fun <R, S> TableView<S>.runWithScrollKeeping(action: (RestoreScrollPositionFunction)->R, nextAction: ()->Unit): R {

    val prevViewPortOffset = this.viewPortAbsoluteOffset
    val restoreScrollPositionFunction: RestoreScrollPositionFunction = {
        prevViewPortOffset?.let {
            this.setViewPortAbsoluteOffsetImpl(prevViewPortOffset, { }, Immediately) } }

    return try { action(restoreScrollPositionFunction) }
           finally { prevViewPortOffset?.let {
               this.setViewPortAbsoluteOffsetImpl(prevViewPortOffset, nextAction, ImmediatelyAndLater) }
           }
}


/*

// Investigation code. Let's keep it in git history.

import javafx.beans.value.ObservableValue
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.control.TableColumn.CellDataFeatures
import javafx.scene.control.cell.*
import javafx.scene.control.skin.TableHeaderRow
import javafx.scene.control.skin.VirtualFlow
import javafx.scene.control.skin.callAdjustPosition
import javafx.scene.layout.Pane
import javafx.util.Callback



class ScrollPositionSate (
    internal val verticalScrollBarValue: Double?,
    internal val absoluteOffsetValue: Double?,
    internal val contentHeight: Double?,
) {
    companion object {
        fun getCurrentState(table: TableView<*>): ScrollPositionSate =
            ScrollPositionSate (
                table.verticalScrollBarValue,
                table.virtualFlow?.absoluteOffsetValue,
                table.contentHeight,
            )

        fun <S,T> setCurrentState(table: TableView<S>, scrollState: ScrollPositionSate, mandatoryVisibleItem: S, tempPane: Pane) {

            var scrollPositionIsRestored = false
            try {
                fixRecalculatedCachedTableSize(table, tempPane)

                val virtualFlow = table.virtualFlow
                val newAbsoluteOffsetValue = scrollState.absoluteOffsetValue

                if (virtualFlow != null && newAbsoluteOffsetValue != null) {
                    //virtualFlow.absoluteOffsetValue = newAbsoluteOffsetValue
                    virtualFlow.callAdjustPosition(newAbsoluteOffsetValue)
                    scrollPositionIsRestored = true
                }
            } catch (ignore: Exception) {
                ignore.printStackTrace() // T O D O: log warning
            } finally {
                if (!scrollPositionIsRestored)
                    table.scrollTo(mandatoryVisibleItem)
            }
        }

        fun <S> fixRecalculatedCachedTableSize(table: TableView<S>, pane: Pane) {

            val columns = table.columns
            val items = table.items

            val tempTableRow = TableRow<S>()
            pane.children.add(tempTableRow)

            try {

                val columnsI: List<ColumnInfo<S, Any>> = columns
                    .filterIsInstance<TableColumn<S, Any>>()
                    .map {
                        //ColumnInfo(it as TableColumn<S, Any>, pane, tempTableRow)
                        ColumnInfo(it, pane, tempTableRow)
                    }

                val itemsHeight: List<Pair<S, Double>> = items
                    .mapIndexed { index, rowItem ->
                        Pair(rowItem, columnsI
                            .map { col -> col.calculatePrefRowHeight(index, rowItem) }
                            .maxOrNull()!!)
                    }
                    .toList()

                itemsHeight.forEach {
                    log.info("{}", it)
                }


            } finally {
                pane.children.remove(tempTableRow)
            }

            // T O D O("Not yet implemented")
        }

        private class ColumnInfo<S,T> (
            val table: TableView<S>,
            val column: TableColumn<S,T>,
            val cellValueFactory: Callback<CellDataFeatures<S, T>, ObservableValue<T>>,
            val cellFactory: Callback<TableColumn<S, T>, TableCell<S, T>>,
            val pane: Pane,
            val tableRow: TableRow<S>,
        ) : AutoCloseable {

            val cell: TableCell<S, T> = cellFactory.call(column)

            init { pane.children.add(cell) }

            //constructor(tableColumn: TableColumn<S, T>) : this(
            //    tableColumn.tableView,
            //    tableColumn,
            //    tableColumn.cellValueFactory as Callback<CellDataFeatures<S, T>, ObservableValue<T>>,
            //    tableColumn.cellFactory as Callback<TableColumn<S, T>, TableCell<S, T>>,) {
            //
            //}

            override fun close() { pane.children.remove(cell) }

            fun calculatePrefRowHeight(index: Int, rowValue: S): Double {
                val width = if (column.width > 0) column.width else column.prefWidth

                tableRow.updateTableView(table)
                tableRow.updateIndex(index)
                tableRow.updateSelected(false)
                tableRow.item = rowValue

                //if (tableRow.skin == null) {
                //    tableRow.skin =
                //}

                cell.updateTableView(table)
                cell.updateTableRow(tableRow)
                cell.updateTableColumn(column)
                cell.updateIndex(index)
                val cellItem = this.cellValueFactory.call(CellDataFeatures(table, column, rowValue)).value
                cell.item = cellItem

                updateItem(cell, cellItem)

                return cell.prefHeight(width)
            }

            // we need it because Cell has 'protected' updateItem()
            private fun updateItem(cell: TableCell<S, T>, item: T) {
                when (cell) {
                    is LabelStatusTableCell<S,T> -> cell.updateItem(item, false)
                    is TextFieldTableCell<S, T>  -> cell.updateItem(item, false)
                    //
                    is ExTextFieldTableCell<S,T> -> cell.updateItem(item, false)
                    is CheckBoxTableCell<S,T>    -> cell.updateItem(item, false)
                    is ChoiceBoxTableCell<S, T>  -> cell.updateItem(item, false)
                    is ComboBoxTableCell<S, T>   -> cell.updateItem(item, false)
                    //is ProgressBarTableCell<S> -> cell.updateItem(item, false)
                    //
                    // Add others cells which you use
                }
            }

            companion object {
                operator fun <S,T> invoke(tableColumn: TableColumn<S, T>, pane: Pane, tableRow: TableRow<S>): ColumnInfo<S,T> =
                    ColumnInfo<S,T>(
                        tableColumn.tableView,
                        tableColumn,
                        tableColumn.cellValueFactory as Callback<CellDataFeatures<S, T>, ObservableValue<T>>,
                        tableColumn.cellFactory as Callback<TableColumn<S, T>, TableCell<S, T>>,
                        pane,
                        tableRow,
                    )
            }
        }
    }
}



val TableView<*>.contentViewPortHeight: Double? get() {

    val clippedContainer = this.clippedContainer
    if (clippedContainer != null) {
        return clippedContainer.boundsInParent.height
    }

    val vScrollBar = this.verticalScrollBar
    val hScrollBar = this.horizontalScrollBar

    val header = lookupAll("TableHeaderRow").firstOrNull()
        ?: lookupAll("column-header-background").firstOrNull()
                as TableHeaderRow?

    if (vScrollBar != null && header != null) {
        val hScrollBarHeight = if (hScrollBar != null && hScrollBar.isVisible) hScrollBar.boundsInParent.height else 0.0
        return this.boundsInParent.height - header.boundsInParent.height - hScrollBarHeight
    }

    return null
}

// VirtualFlow$ClippedContainer@4f372107[styleClass=clipped-container]
val TableView<*>.clippedContainer: Node? get() = lookupAll(".clipped-container").firstOrNull()
// VirtualFlow[id=virtual-flow, styleClass=virtual-flow]
val <S> TableView<S>.virtualFlow: VirtualFlow<*>? get() = lookupAll("VirtualFlow")
    .filterIsInstance<VirtualFlow<*>>()
    .firstOrNull { it.isVertical }

//inline val <reified S, reified T> TableView<S>.virtualFlow2: VirtualFlow<T>? get() = lookupAll("VirtualFlow")
//inline val <reified S, reified T, reified CellType: IndexedCell<T>> TableView<S>.virtualFlow2: VirtualFlow<CellType>? get() =
//    lookupAll("VirtualFlow")
//        .filterIsInstance<VirtualFlow<CellType>>()
//        .firstOrNull { it.isVertical }
//
//inline fun <reified S, reified T, reified CellType: IndexedCell<T>> TableView<S>.getVirtualFlow3(): VirtualFlow<CellType>? =
//    lookupAll("VirtualFlow")
//        .filterIsInstance<VirtualFlow<CellType>>()
//        .firstOrNull { it.isVertical }


val TableView<*>.pageCount: Double? get() {

    // !!! This code is risky !!!
    // It works for me on Ubuntu Linux in (now in 2023) but it may not work
    // on other platforms
    // T O D O: would be nicer to find reliable solution

    val vScrollBar = this.verticalScrollBar
        ?: return null

    return vScrollBar.max / vScrollBar.visibleAmount
    // or ??? (vScrollBar.max - vScrollBar.min) / vScrollBar.visibleAmount
}


val TableView<*>.contentHeight: Double? get() {
    // safe non-null values
    val contentViewPortHeight = this.contentViewPortHeight
    val pageCount = this.pageCount

    return if (contentViewPortHeight != null && pageCount != null)
        contentViewPortHeight * pageCount
    else null
}


var TableView<*>.contentViewPortTop: Double?
    get() {
        // !!! This code is risky !!!
        // It works for me on Ubuntu Linux in (now in 2023) but it may not work on other platforms
        // T O D O: would be nicer to find reliable solution

        val vScrollBar = this.verticalScrollBar
            ?: return 0.0

        val contentHeight = this.contentHeight
        return contentHeight?.let { it * vScrollBar.value }
    }
    set(value) = setContentViewPortTop2(this, value)

fun setContentViewPortTop2(table: TableView<*>, contentViewPortTop: Double?, contentHeight: Double? = null) {
    // !!! This code is risky !!!
    // It works for me on Ubuntu Linux in (now in 2023) but it may not work on other platforms
    // T O D O: would be nicer to find reliable solution

    val vScrollBar = table.verticalScrollBar
    val contentHeightFixed = contentHeight ?: table.contentHeight

    if (vScrollBar != null && contentViewPortTop != null && contentHeight != null) {
        vScrollBar.value = contentViewPortTop / contentHeight
    }
}


val TableView<*>.contentHeight3: Double? get() {

    //VirtualFlow.getPosition()/setPosition()
    //
    //ClippedContainer
    //    setClipY(double clipY) {
    //    get/setLayoutY

    // VirtualFlow
    // absoluteOffset = 3342.222222222222 !!! <===
    // estimatedSize = 4554.0
    // itemSizeCache = {ArrayList@5141}  size = 102
    //
    // maxPrefBreadth = 1250.0
    // viewportBreadth = 1236.0
    // viewportLength = 794.0
    // ??? void setViewportLength(double value)
    // recalculateEstimatedSize

    //
    //    /**
    //     * Keep the position constant and adjust the absoluteOffset to
    //     * match the (new) position.
    //     */
    //    void adjustAbsoluteOffset() {
    //        absoluteOffset = (estimatedSize - viewportLength) * getPosition();
    //    }
    //
    //    /**
    //     * Keep the absoluteOffset constant and adjust the position to match
    //     * the (new) absoluteOffset.
    //     */
    //    void adjustPosition() {
    //        if (viewportLength >= estimatedSize) {
    //            setPosition(0.);
    //        } else {
    //            setPosition(absoluteOffset / (estimatedSize - viewportLength));
    //        }
    //    }
    //
    //     /**
    //     * Recalculate the estimated size. If an oldIndex different from  -1 is supplied, that value will
    //     * be respected:
    //     * at the end of this calculation, we make sure that if the current index is calculated, it will
    //     * be the same as the old index. If the oldIndex is -1, there is no guarantee about the new index.
    //     */
    //    private void recalculateAndImproveEstimatedSize(int improve, int oldIndex, double oldOffset)
    //
    //    private void resetSizeEstimates() {
    //        itemSizeCache.clear();
    //        this.estimatedSize = 1d;
    //    }



    T O D O()
}


var TableView<*>.verticalScrollBarValue: Double?
    get() = verticalScrollBar?.value
    set(value) { value?.let { verticalScrollBar?.value = value } }

val TableView<*>.verticalScrollBar: ScrollBar? get() =
    lookupAll(".scroll-bar:vertical")
        .filterIsInstance<ScrollBar>()
        .firstOrNull { it.orientation == Orientation.VERTICAL }

val TableView<*>.horizontalScrollBar: ScrollBar? get() =
    lookupAll(".scroll-bar:horizontal")
        .filterIsInstance<ScrollBar>()
        .firstOrNull { it.orientation == Orientation.HORIZONTAL }
*/


/*
    private fun addAllBaseWordsInSetImpl(wordCards: Iterable<CardWordEntry>) {
        val withoutBaseWord = wordCards
            .asSequence()
            .filter { !it.ignoreNoBaseWordInSet && it.noBaseWordInSet }
            .toSortedSet(cardWordEntryComparator)

        val baseWordsToAddMap: Map<CardWordEntry, CardWordEntry> = withoutBaseWord
            .asSequence()
            .map { Pair(it, possibleBestEnglishBaseWord(it.from)) }
            .filterNotNullPairValue()
            .map { Pair(it.first, CardWordEntry(it.second, "")) }
            .associate { it }

        // this simple approach (but effective!) causes unneeded scrolling
        //currentWordsList.items.addAll(baseWordsToAdd)
        //currentWordsList.sort()
        //reanalyzeWords()

        val prevScrollValue = currentWordsList.verticalScrollBarValue
        val prevContentViewPortTop = currentWordsList.contentViewPortTop
        val prevContentHeight = currentWordsList.contentHeight
        val virtualFlow = currentWordsList.virtualFlow
        log.info("{}", "virtualFlow: $virtualFlow")
        val prevAbsoluteOffset = virtualFlow?.absoluteOffsetValue
        log.info("{}", "prevAbsoluteOffset: $prevAbsoluteOffset")

        // This approach should keep less/more scrolling but due to bug in JavaFx auto-scrolling is still unpredictable :-(
        // This JavaFX bug appears if rows have different height.
        //
        baseWordsToAddMap.forEach { (currentWordCard, baseWordCard) ->
            val index = currentWordsList.items.indexOf(currentWordCard)
            currentWordsList.items.add(index, baseWordCard)
        }
        analyzeWordCards(withoutBaseWord, currentWordsList.items)


        if (prevScrollValue != null && baseWordsToAddMap.size == 1) {

            val newBaseWordCard = baseWordsToAddMap.values.first()
            currentWordsList.verticalScrollBarValue = prevScrollValue    // it may not work

            // currentWordsList.refresh() // it is not enough to update estimated content height


            val safeItemsCopy = currentWordsList.items.toList()
            safeItemsCopy.forEach { currentWordsList.scrollTo(it) }


            runLaterWithDelay(50) { // it is not enough use Platform.runLater()
                // it is a bit bad approach but now there is no better one :-(
                currentWordsList.verticalScrollBarValue = prevScrollValue    // it may not work
                //currentWordsList.refresh()

                //runLaterWithDelay(5) {

                //currentWordsList.contentViewPortTop = prevContentViewPortTop // it may not work
                //setContentViewPortTop2(currentWordsList, prevContentViewPortTop, prevContentHeight!! + 24.0) // it may not work

                if (prevAbsoluteOffset != null) {
                    log.info("{}", "virtualFlow.callAdjustPosition()")
                    virtualFlow.absoluteOffsetValue = prevAbsoluteOffset
                    virtualFlow.callAdjustPosition()
                }

                log.info("{}", "prevScrollValue: ${prevScrollValue}, after attempt to restore: ${currentWordsList.verticalScrollBarValue}")
                log.info("{}", "prevContentHeight: ${prevContentHeight}, after attempt to restore: ${currentWordsList.contentHeight}")
                log.info("{}", "prevContentViewPortTop: ${prevContentViewPortTop}, after attempt to restore: ${currentWordsList.contentViewPortTop}")

                if (currentWordsList.selectionModel.selectedItems.size <= 1) {
                    currentWordsList.selectionModel.clearSelection()
                    currentWordsList.selectionModel.select(newBaseWordCard)
                    currentWordsList.scrollTo(newBaseWordCard)
                }
                //}

                currentWordsList.sort()
                currentWordsList.refresh()

                runLaterWithDelay(5) {
                    if (prevAbsoluteOffset != null) {
                        currentWordsList.verticalScrollBarValue = prevScrollValue    // it may not work
                        log.info("{}", "virtualFlow.callAdjustPosition()")
                        virtualFlow.absoluteOffsetValue = prevAbsoluteOffset
                        virtualFlow.callAdjustPosition()

                        //currentWordsList.scrollTo(newBaseWordCard)
                    }
                }
            }
        }
    }
*/


/*

class TableView2<S>() : TableView<S>() {

    override fun layoutChildren() {
        super.layoutChildren()

        //printChildren()
    }

    /*
    fun printChildren() {
        log.info("{}", "\n-------------------------------\n")

        childrenUnmodifiable.forEachIndexed { i, node ->
            //log.info("{}", "$i: $node")
            //if (node is Parent) {
            //    node.childrenUnmodifiable
            //}
            printNode(node)
        }
        log.info("{}", "\n-------------------------------\n")
        log.info("{}", "\n--------- Skin ----------------\n")

        //this.typeSelector
        //this.createDefaultSkin()
        @Suppress("UNCHECKED_CAST") val skin = this.skin as TableViewSkin<S>
        skin.children.forEachIndexed { i, node ->
            log.info("{}", "skin $i: $node")
            printNode(node)
        }

        log.info("{}", "\n-------------------------------\n")
    }
    */
}

fun printNode(node: Node) {
    log.info("{}", "$node (${node.boundsInParent.width}, ${node.boundsInParent.height})")
    if (node is Parent) {
        node.childrenUnmodifiable.forEach { printNode(it) }
    }
}

fun printNodeShortly(node: Node) {
    //log.info("{}", "${node.javaClass.name} ${node.typeSelector} (${node.boundsInParent.width}, ${node.boundsInParent.height})")
    //log.info("{}", "(${node.boundsInParent.width}, ${node.boundsInParent.height})   ${node.javaClass.simpleName} ${node.typeSelector}")
    log.info("{}", "(${"%6d".format(node.boundsInParent.height.toInt())})   ${node.javaClass.simpleName}")
    if (node is Parent) {
        node.childrenUnmodifiable.forEach { printNodeShortly(it) }
    }
}

fun calculateTableContentHeight(node: Node): Double {
    var height = 0.0
    if (node is TableRow<*>) {
        height += node.boundsInParent.height
    }

    if (node is Parent) {
        node.childrenUnmodifiable.forEach {
            height += calculateTableContentHeight(it)
        }
    }
    return height
}


data class Accumulator (var height: Double = 0.0, var rowCount: Int = 0)

fun calculateTableContentHeight2(node: Node): Accumulator {
    val ac = Accumulator(0.0, 0)
    calculateTableContentHeight2Impl(node, ac)
    return ac
}
fun calculateTableContentHeight2Impl(node: Node, ac: Accumulator) {
    if (node is TableRow<*>) {
        ac.height += node.boundsInParent.height
        ac.rowCount++
    }

    if (node is Parent) {
        node.childrenUnmodifiable.forEach {
            calculateTableContentHeight2Impl(it, ac)
        }
    }
}
*/
