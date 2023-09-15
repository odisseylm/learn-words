package com.mvv.gui.javafx

import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.scene.Node
import javafx.scene.control.Control
import javafx.scene.control.Tooltip
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.stage.Stage
import javafx.util.Duration


//private val log = mu.KotlinLogging.logger {}


internal fun emptyIcon16x16() = Rectangle(16.0, 16.0, Color(0.0, 0.0, 0.0, 0.0))


fun setWindowTitle(node: Node, title: String) {
    if (node.scene.window is Stage) {
        (node.scene.window as Stage).title = title
    }
}


var Control.toolTipText: String?
    get() = this.tooltip?.text
    set(value) { setToolTip(value, null) }

fun Control.setToolTip(text: String?, maxWidth: Double?) {
    if (text.isNullOrBlank()) {
        // We need set the whole ToolTip object to null!
        // Just setting text to ""/null for existent ToolTip instance does not prevent showing empty tooltip.
        this.tooltip = null
    }
    else {
        if (this.tooltip == null) this.tooltip = Tooltip()
        this.tooltip.text = text

        this.tooltip.isWrapText = true
        maxWidth?.let  { this.tooltip.maxWidth  = it }
    }
}


fun runLaterWithDelay(delayMillis: Long, action: ()->Unit) {
    val timeline = Timeline(KeyFrame(Duration.millis(delayMillis.toDouble()), { Platform.runLater(action) }))
    timeline.cycleCount = 1
    timeline.play()
}
