package com.mvv.gui.javafx

import com.mvv.gui.initThemeAndStyles
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Control
import javafx.scene.control.PopupControl
import javafx.scene.control.Tooltip
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.stage.PopupWindow
import javafx.stage.Stage
import javafx.stage.Window
import javafx.util.Duration


private val log = mu.KotlinLogging.logger {}


internal fun emptyIcon16x16() = Rectangle(16.0, 16.0, Color(0.0, 0.0, 0.0, 0.0))

// In general using shared ImageView is risky,
// but this instance should not be really used/rendered.
// It is designed to be used only as flag.
private val noIconSingleton = ImageView()
fun noIcon(): ImageView = noIconSingleton


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


fun Node.belongsToParent(possibleParent: Parent): Boolean {
    var parentLeaf: Parent? = this.parent

    val maxDeep = 100
    var i = 0

    while (i <= maxDeep && parentLeaf !== null && parentLeaf !== possibleParent) {
        parentLeaf = parentLeaf.parent
        i++
    }

    if (i == maxDeep) log.warn { "${this.javaClass.simpleName}.belongsToParent() has too many iterations." }

    return parentLeaf === possibleParent
}



fun runLaterWithDelay(delayMillis: Long, action: ()->Unit) {
    val timeline = Timeline(KeyFrame(Duration.millis(delayMillis.toDouble()), { Platform.runLater(action) }))
    timeline.cycleCount = 1
    timeline.play()
}


fun <C: Node> C.addIsShownHandler(action: (C)->Unit) {

    val scene = this.scene
    val window = scene?.window

    fun addWndShownListener(wnd: Window?) = wnd?.showingProperty()?.addListener { _, _, isShown -> if (isShown) action(this) }

    fun addWndListener(wndProp: ReadOnlyObjectProperty<Window>) = wndProp.addListener { _, _, newWnd ->
            if (newWnd?.isShowing == true) action(this)
            else if (newWnd != null) addWndShownListener(newWnd)
        }

    fun addSceneListener(sceneProp: ReadOnlyObjectProperty<Scene>) =
        sceneProp.addListener { _,_, newScene ->
            if (newScene?.window?.isShowing == true) action(this)
            else addWndListener(newScene.windowProperty())
        }

    when {
        window != null -> if (window.isShowing) action(this)
                          else addWndShownListener(window)
        scene  != null -> addWndListener(scene.windowProperty())
        else           -> addSceneListener(this.sceneProperty())
    }
}



//*************************************************************************************************
//             Helper functions to write the same code for PopupControl and Stage
//*************************************************************************************************

fun setSceneRoot(popup: PopupControl, content: Parent) { popup.scene.root = content   }
fun setSceneRoot(stage: Stage, content: Parent)        { stage.scene = Scene(content) }

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
