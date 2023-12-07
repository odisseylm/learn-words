package com.mvv.gui.javafx

import com.mvv.gui.cardeditor.initThemeAndStyles
import com.mvv.gui.util.addOnceEventHandler
import javafx.application.Platform
import javafx.geometry.Point2D
import javafx.geometry.Rectangle2D
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.PopupControl
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Region
import javafx.scene.layout.Region.USE_COMPUTED_SIZE
import javafx.stage.PopupWindow
import javafx.stage.Stage
import javafx.stage.Window
import javafx.stage.WindowEvent


fun addWindowMovingFeature(stage: Stage, hang: Node) {
    var xOffset = 0.0
    var yOffset = 0.0

    hang.addEventHandler(MouseEvent.MOUSE_PRESSED) {
        xOffset = it.sceneX
        yOffset = it.sceneY
    }

    hang.addEventHandler(MouseEvent.MOUSE_DRAGGED) {
        stage.x = it.screenX - xOffset
        stage.y = it.screenY - yOffset
    }
}

enum class ResizingAction { Top, TopRightCorner, Right, BottomRightCorner, Bottom, BottomLeftCorner, Left, TopLeftCorner }

private const val cornerSize = 10.0


fun addWindowResizingFeature(stage: Stage, borderNode: Node) {

    var resizingAction: ResizingAction? = null
    var initScreenPoint: Point2D? = null
    var initBounds: Rectangle2D? = null

    borderNode.addEventHandler(MouseEvent.MOUSE_PRESSED) {
        resizingAction = determineResizingAction(it, borderNode)
        initScreenPoint = Point2D(it.screenX, it.screenY)
        initBounds = stage.bounds
    }

    borderNode.addEventHandler(MouseEvent.MOUSE_DRAGGED) {

        val initPt = initScreenPoint!!
        val initRc = initBounds!!

        val difX = it.screenX - initPt.x
        val difY = it.screenY - initPt.y

        val newBounds = when (resizingAction) {
            ResizingAction.Top -> Rectangle2D(
                initRc.minX, initRc.minY + difY,
                initRc.width, initRc.height - difY
            )

            ResizingAction.TopRightCorner -> Rectangle2D(
                initRc.minX, initRc.minY + difY,
                initRc.width + difX, initRc.height - difY
            )

            ResizingAction.Right -> Rectangle2D(
                initRc.minX, initRc.minY,
                initRc.width + difX, initRc.height
            )

            ResizingAction.BottomRightCorner -> Rectangle2D(
                initRc.minX, initRc.minY,
                initRc.width + difX, initRc.height + difY
            )

            ResizingAction.Bottom -> Rectangle2D(
                initRc.minX, initRc.minY,
                initRc.width, initRc.height + difY
            )

            ResizingAction.BottomLeftCorner -> Rectangle2D(
                initRc.minX + difX, initRc.minY,
                initRc.width - difX, initRc.height + difY
            )

            ResizingAction.Left -> Rectangle2D(
                initRc.minX + difX, initRc.minY,
                initRc.width - difX, initRc.height
            )

            ResizingAction.TopLeftCorner -> Rectangle2D(
                initRc.minX + difX, initRc.minY + difY,
                initRc.width - difX, initRc.height - difY
            )

            else -> null
        }
        newBounds?.let { bounds -> stage.bounds = bounds }
    }

    borderNode.addEventHandler(MouseEvent.MOUSE_RELEASED) {
        resizingAction = null
        initScreenPoint = null
        initBounds = null
    }
}


fun determineResizingAction(mouseEvent: MouseEvent, hangNode: Node): ResizingAction? {
    val x = mouseEvent.x
    val y = mouseEvent.y
    val w = hangNode.boundsInLocal.width
    val h = hangNode.boundsInLocal.height

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

@Suppress("UnusedReceiverParameter")
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


enum class RelocationPolicy { AlwaysRecalculate, CalculateOnlyOnce }


fun Stage.showPopup(parent: Node, relocationPolicy: RelocationPolicy, getPos: ()-> Point2D) {

    if (this.isIconified) this.isIconified = false

    this.showPopupImpl(
        relocationPolicy,
        getPos,
        { this.show() },
        { val pos = getPos(); this.show(parent.scene.window, pos.x, pos.y, true) },
        { this.isResizable })
}

fun PopupControl.showPopup(parent: Node, relocationPolicy: RelocationPolicy, getPos: ()-> Point2D) =
    this.showPopupImpl(
        relocationPolicy,
        getPos,
        { this.show(parent.scene.window) },
        { val pos = getPos(); this.show(parent.scene.window, pos.x, pos.y) },
        { this.isResizable })

private fun Window.showPopupImpl(
    relocationPolicy: RelocationPolicy,
    getPos: ()-> Point2D,
    setVisibleAgain: ()->Unit, // if it was already created and was already shown
    showInitially: ()->Unit,
    isResizable: ()->Boolean,
) {

    if (relocationPolicy === RelocationPolicy.CalculateOnlyOnce) {
        // for non-popup impl
        if (isResizable() && (this.width > 100 || this.height > 20)) {

            val prevW = this.width
            val prevH = this.height

            setVisibleAgain() // Stage.show() sets size to pref size

            // let's keep old bounds (or otherwise, lets change x/y too)
            this.width = prevW; this.height = prevH
            return
        }

        if (this.width > 0 || this.height > 0) {

            showInitially()
            return
        }
    }
    else if (relocationPolicy === RelocationPolicy.AlwaysRecalculate) {

        sizeToScene()

        showInitially()

        // to surely use proper position after deferred doing (unfortunately) layout
        Platform.runLater(showInitially)
        return
    }

    val content = this.scene.root as Region

    // I do not know how to calculate desired x/y at this time, because
    // I do not know how to calculate 'calculated' pref size
    // and sizeToScene() does not work till window is showing (in contrast with Swing Window.pack() which works before real showing window)
    content.prefWidth  = 1.0
    content.prefHeight = 1.0

    addOnceEventHandler(WindowEvent.WINDOW_SHOWN) {

        val useComputedSize = USE_COMPUTED_SIZE
        content.prefWidth  = useComputedSize
        content.prefHeight = useComputedSize

        sizeToScene()

        val pos = getPos()
        x = pos.x
        y = pos.y
    }

    showInitially()
}


/*
fun Window.setFocusable(focusable: Boolean) {

    WindowHelper.setFocused(this, focusable)

    val implPeerField: Field = Window::class.java.getDeclaredField("impl_peer")
    implPeerField.setAccessible(true)

    val implPeer: Any = implPeerField.get(this)
    val getPlatformWindow: Method = implPeer.javaClass.getDeclaredMethod("getPlatformWindow")!!
    getPlatformWindow.trySetAccessible()

    val platformWindow: com.sun.glass.ui.Window = getPlatformWindow.invoke(implPeer) as com.sun.glass.ui.Window
    platformWindow.setFocusable(focusable);
    //getPlatformWindow.invoke(implPeer).setFocusable(focusable)
}
*/
