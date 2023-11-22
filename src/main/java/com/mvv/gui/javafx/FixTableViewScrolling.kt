package com.mvv.gui.javafx

import com.mvv.gui.javafx.SetViewPortAbsoluteOffsetMode.*
import com.mvv.gui.util.logInfo
import com.mvv.gui.util.startStopWatch
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
    set(value) { value?.let { this.setViewPortAbsoluteOffsetImpl(value, -1, null) } }

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
    absoluteOffset: Double, visibleFirstRowIndex: Int, visibleFirstRowItem: S?,
    nextAction: ()->Unit = { }, setMode: SetViewPortAbsoluteOffsetMode = ImmediatelyAndLater
) {

    val virtualFlow = this.virtualFlow
    if (virtualFlow == null) {
        log.warn("VirtualFlow is not found")
        return
    }

    val selectedItem: S? = this.selectionModel.selectedItem

    val contentHeightIsFixed = fixEstimatedContentHeight(absoluteOffset)

    fun setAdjustPosIml(pos: Double) {
        virtualFlow.absoluteOffsetValue = pos

        if (contentHeightIsFixed)
            virtualFlow.callAdjustPosition()
        else {
            var scrolled = false

            val newVisibleFirstRowItemIndex = if (visibleFirstRowItem == null) -1 else this.items.indexOf(visibleFirstRowItem)
            if (newVisibleFirstRowItemIndex > 0) {
                virtualFlow.scrollTo(newVisibleFirstRowItemIndex)
                scrolled = true
            }

            if (visibleFirstRowIndex > 0 && !scrolled) {
                virtualFlow.scrollTo(visibleFirstRowIndex)
                scrolled = true
            }

            val selectedIndex = this.items.indexOf(selectedItem)
            if (selectedIndex > 0 && !scrolled) {
                virtualFlow.scrollTo(selectedIndex)
                //scrolled = true
            }
        }
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
// T O D O: unsafe method with scrolling over all rows, we need to find better way
private fun <S> TableView<S>.fixEstimatedContentHeight(desiredAbsoluteOffset: Double): Boolean {
    //val safeItemsCopy = this.items.toList()

    // Otherwise it take too much time
    if (this.selectionModel.selectedIndex > 500) return false

    // It does not work
    //val virtualFlow = this.virtualFlow
    //safeItemsCopy.forEachIndexed { index, _ -> virtualFlow?.getCell(index) }

    val sw = startStopWatch("TableView.fixEstimatedContentHeight")

    for (rowIndex in 0 until this.items.size) {
        if ((this.viewPortAbsoluteOffset ?: 0.0) > desiredAbsoluteOffset + this.height) break
        this.scrollTo(rowIndex)
    }

    //safeItemsCopy.forEach { this.scrollTo(it) }

    sw.logInfo(log) // TODO: takes too much time if table is not small (1000 rows)
    return true
}


typealias RestoreScrollPositionFunction = ()->Unit


// We need to restore view-port offset (scroll position) manually due to JavaFX bug (if a table has rows with different height)
fun <R, S> TableView<S>.runWithScrollKeeping(action: (RestoreScrollPositionFunction)->R): R =
    this.runWithScrollKeeping(action) { }


// We need to restore view-port offset (scroll position) manually due to JavaFX bug (if a table has rows with different height)
fun <R, S> TableView<S>.runWithScrollKeeping(action: (RestoreScrollPositionFunction)->R, nextAction: ()->Unit): R {

    val prevViewPortOffset = this.viewPortAbsoluteOffset
    val visibleFirstRowIndex = this.visibleRows.first
    val visibleFirstRowItem = this.items.elementAtOrNull(this.visibleRows.first)

    val restoreScrollPositionFunction: RestoreScrollPositionFunction = {
        prevViewPortOffset?.let {
            this.setViewPortAbsoluteOffsetImpl(prevViewPortOffset, visibleFirstRowIndex, visibleFirstRowItem, { }, Immediately) } }

    return try { action(restoreScrollPositionFunction) }
           finally { prevViewPortOffset?.let {
               this.setViewPortAbsoluteOffsetImpl(prevViewPortOffset, visibleFirstRowIndex, visibleFirstRowItem, nextAction, ImmediatelyAndLater) }
           }
}
