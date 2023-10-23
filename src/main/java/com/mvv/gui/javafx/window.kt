package com.mvv.gui.javafx

import com.mvv.gui.initThemeAndStyles
import javafx.event.EventHandler
import javafx.geometry.Point2D
import javafx.geometry.Rectangle2D
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.PopupControl
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.MouseEvent
import javafx.stage.PopupWindow
import javafx.stage.Stage
import javafx.stage.Window


fun addWindowMovingFeature(stage: Stage, hang: Node) {
    var xOffset = 0.0
    var yOffset = 0.0

    //grab your root here
    hang.onMousePressed = EventHandler {
        xOffset = it.sceneX
        yOffset = it.sceneY
    }

    //move around here
    hang.onMouseDragged = EventHandler {
        stage.x = it.screenX - xOffset
        stage.y = it.screenY - yOffset
    }
}

enum class ResizingAction { Top, TopRightCorner, Right, BottomRightCorner, Bottom, BottomLeftCorner, Left, TopLeftCorner }

private const val cornerSize = 10.0


fun addWindowResizingFeature(stage: Stage, hang: Node) {

    var resizingAction: ResizingAction? = null
    var initScreenPoint: Point2D? = null
    var initBounds: Rectangle2D? = null

    hang.onMousePressed = EventHandler {
        resizingAction = determineResizingAction(it, hang)
        initScreenPoint = Point2D(it.screenX, it.screenY)
        initBounds = stage.bounds
    }

    //move around here
    hang.onMouseDragged = EventHandler {

        val initPt = initScreenPoint!!
        val initRc = initBounds!!

        val difX = it.screenX - initPt.x
        val difY = it.screenY - initPt.y

        val newBounds = when (resizingAction) {
            ResizingAction.Top -> Rectangle2D( // +++
                initRc.minX, initRc.minY + difY,
                initRc.width, initRc.height - difY
            )

            ResizingAction.TopRightCorner -> Rectangle2D( // ???
                initRc.minX, initRc.minY + difY,
                initRc.width + difX, initRc.height - difY
            )

            ResizingAction.Right -> Rectangle2D( // +++
                initRc.minX, initRc.minY,
                initRc.width + difX, initRc.height
            )

            ResizingAction.BottomRightCorner -> Rectangle2D( // +++
                initRc.minX, initRc.minY,
                initRc.width + difX, initRc.height + difY
            )

            ResizingAction.Bottom -> Rectangle2D( // +++
                initRc.minX, initRc.minY,
                initRc.width, initRc.height + difY
            )

            ResizingAction.BottomLeftCorner -> Rectangle2D( // ???
                initRc.minX + difX, initRc.minY,
                initRc.width - difX, initRc.height + difY
            )

            ResizingAction.Left -> Rectangle2D( // +++
                initRc.minX + difX, initRc.minY,
                initRc.width - difX, initRc.height
            )

            ResizingAction.TopLeftCorner -> Rectangle2D( // +++
                initRc.minX + difX, initRc.minY + difY,
                initRc.width - difX, initRc.height - difY
            )

            else -> {
                null
            }
        }
        newBounds?.let { bounds -> stage.bounds = bounds }
    }

    hang.onMouseReleased = EventHandler {
        resizingAction = null
        initScreenPoint = null
        initBounds = null
    }
}


fun determineResizingAction(mouseEvent: MouseEvent, hang: Node): ResizingAction? {
    val x = mouseEvent.x
    val y = mouseEvent.y
    val w = hang.boundsInLocal.width
    val h = hang.boundsInLocal.height

    return when {
        x >= cornerSize && x < w - cornerSize && y < cornerSize ->
            ResizingAction.Top
        x >= w - cornerSize && y < cornerSize ->
            ResizingAction.TopRightCorner
        x >= w - cornerSize && y >= cornerSize && y < h - cornerSize ->
            ResizingAction.Right
        x >= w - cornerSize && y >= h -cornerSize ->
            ResizingAction.BottomRightCorner
        x >= cornerSize && x < w - cornerSize && y >= h - cornerSize ->
            ResizingAction.Bottom
        x < cornerSize && y >= h - cornerSize ->
            ResizingAction.BottomLeftCorner
        x < cornerSize && y >= cornerSize && y < h - cornerSize ->
            ResizingAction.Left
        x < cornerSize && y < cornerSize ->
            ResizingAction.TopLeftCorner
        else -> null
    }
}


var Stage.bounds: Rectangle2D
    get() = Rectangle2D(this.x, this.y, this.width, this.height)
    set(value) {
        this.x = value.minX
        this.y = value.minY
        this.width  = value.width
        this.height = value.height
    }



//*************************************************************************************************
//             Helper functions to write the same code for PopupControl and Stage
//*************************************************************************************************

var PopupControl.sceneRoot: Parent?
    get() = this.scene.root
    set(value) { this.scene.root = value }
var Stage.sceneRoot: Parent?
    get() = this.scene?.root
    set(value) { if (this.scene == null) this.scene = Scene(value) else this.scene.root = value }

fun PopupWindow.show(parentWindow: Window, pos: Point2D) = this.show(parentWindow, pos.x, pos.y)
fun Stage.show(parentWindow: Window, pos: Point2D) = this.show(parentWindow, pos.x, pos.y)

val PopupWindow.isResizable: Boolean get() = false


fun Stage.show(parentWindow: Window, x: Double, y: Double, asPopup: Boolean = true) {

    if (this.owner == null) {
        this.initOwner(parentWindow)
        this.scene.root.initThemeAndStyles()

        if (asPopup) {
            // it is not needed for PopupControl
            addGlobalKeyBindings(this.scene.root,
                listOf(KeyCodeCombination(KeyCode.CANCEL), KeyCodeCombination(KeyCode.ESCAPE))) { hide() }
        }
    }

    this.x = x
    this.y = y
    this.show()
}
