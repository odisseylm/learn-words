package com.mvv.gui.javafx

import com.mvv.gui.util.isDebuggerPresent
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import javafx.scene.layout.Region.USE_COMPUTED_SIZE
import javafx.stage.PopupWindow
import javafx.stage.WindowEvent


private val log = mu.KotlinLogging.logger {}


@Suppress("unused")
fun newMenuItem(label: String, action: ()->Unit): MenuItem =
    newMenuItemImpl(label = label, action = action)

@Suppress("unused")
fun newMenuItem(label: String, icon: ImageView, action: ()->Unit): MenuItem =
    newMenuItemImpl(label = label, icon = icon, action = action)

fun newMenuItem(label: String, tooltip: String, action: ()->Unit): MenuItem =
    newMenuItemImpl(label = label, tooltip = tooltip, action = action)

@Suppress("unused")
fun newMenuItem(label: String, keyCombination: KeyCombination, action: ()->Unit): MenuItem =
    newMenuItemImpl(label, null, null, keyCombination, action)

fun newMenuItem(label: String, icon: ImageView, keyCombination: KeyCombination, action: ()->Unit): MenuItem =
    newMenuItemImpl(label, null, icon, keyCombination, action)

fun newMenuItem(label: String, tooltip: String, icon: ImageView, action: ()->Unit): MenuItem =
    newMenuItemImpl(label, tooltip, icon, null, action)

fun newMenuItem(label: String, tooltip: String, icon: ImageView, keyCombination: KeyCodeCombination, action: ()->Unit): MenuItem =
    newMenuItemImpl(label, tooltip, icon, keyCombination, action)


private fun newMenuItemImpl(label: String, tooltip: String? = null, icon: ImageView? = null, keyCombination: KeyCombination? = null, action: (()->Unit)? = null): MenuItem =
    if (tooltip == null) MenuItem(label, icon ?: emptyIcon16x16())
    else {
        // Standard MenuItem class does not support tooltips,
        // but CustomMenuItem does not show accelerator :-)
        // T O D O: fix showing accelerator and tooltip together

        keyCombination?.also { log.warn { "Menu item key-combination [$keyCombination] most probably will not be shown." } }

        CustomMenuItem(Label(label, icon ?: emptyIcon16x16()).also {
            it.styleClass.add("custom-menu-item-content")
            Tooltip.install(it, Tooltip(tooltip))
        })
    }
    .also { menuItem ->
        keyCombination?.let { menuItem.accelerator = it }
        action?.let         { menuItem.onAction = safeRunMenuCommand(it) }
    }


private val underDebug: Boolean = isDebuggerPresent()

// Under Ubuntu during debugging context menu action, all Ubuntu UI feezes (hangs up) :-(
// as workaround we start action execution after menu/popup hiding.
//
fun safeRunMenuCommand(action: ()->Unit): EventHandler<ActionEvent> = EventHandler { ev ->
    if (underDebug) {
        val popups: List<PopupWindow> = sequenceOf(ev.source, ev.target)
            .filterNotNull()
            .filterIsInstance<Node>() // { it is Node }
            .map { it.scene?.window }
            .filterNotNull()
            .filter { it.isShowing }
            .filterIsInstance<PopupWindow>()
            .toList()
        // hiding popups to avoid freezing on Ubuntu
        popups.forEach { it.hide() }

        runLaterWithDelay(1000) { action() }
    }
    else action()
}


fun ContextMenu.hideRepeatedMenuSeparators() = hideRepeatedMenuSeparators(this.items)


fun hideRepeatedMenuSeparators(menuItems: Iterable<MenuItem>) {
    var isPrevItemIsVisibleSeparator = false
    menuItems
        .filter { it.isVisible }
        .forEach { menuItem ->
            if (menuItem is SeparatorMenuItem && isPrevItemIsVisibleSeparator)
                menuItem.isVisible = false
            else
                isPrevItemIsVisibleSeparator = menuItem is SeparatorMenuItem && menuItem.isVisible
        }
}


@Suppress("unused")
fun useMenuStateDumping(contextMenu: ContextMenu) {
    contextMenu.prefWidth = USE_COMPUTED_SIZE
    val skinNode = contextMenu.skin?.node
    if (skinNode is Region) {
        skinNode.prefWidth = USE_COMPUTED_SIZE
    }

    contextMenu.addEventHandler(WindowEvent.WINDOW_SHOWING) {
        runLaterWithDelay(1000) {
            log.info { "Context menu" +
                    " isShowing: ${contextMenu.isShowing}," +
                    " isAutoFix: ${contextMenu.isAutoFix}," +
                    " size: ${contextMenu.width}/${contextMenu.height}, ${contextMenu.prefWidth}/${contextMenu.prefHeight}, ${contextMenu.minWidth}/${contextMenu.minHeight}, ${contextMenu.maxWidth}/${contextMenu.maxHeight}" +
                    " anchorLocation: ${contextMenu.anchorLocation}," +
                    " contextMenu.skin: ${contextMenu.skin} ${contextMenu.skin.javaClass.name}," +
                    " contextMenu.scene.root.isVisible: ${contextMenu.scene.root.isVisible}" +
                    " contextMenu.scene.root.isManaged: ${contextMenu.scene.root.isManaged}" +
                    " contextMenu.scene.window.isShowing: ${contextMenu.scene.window.isShowing}" +
                    " contextMenu.styleableNode.isVisible: ${contextMenu.styleableNode.isVisible}" +
                    " contextMenu.styleableNode.isManaged: ${contextMenu.styleableNode.isManaged}" +
                    " contextMenu.scene.root.boundsInLocal: ${contextMenu.scene.root.boundsInLocal}" +
                    " contextMenu.scene.window bounds: ${contextMenu.scene.window.x} ${contextMenu.scene.window.y} ${contextMenu.scene.window.width} ${contextMenu.scene.window.height}" +
                    " contextMenu.scene.window renderScaleXY: ${contextMenu.scene.window.renderScaleX} ${contextMenu.scene.window.renderScaleY}"
            }

            // javafx.scene.control.PopupControl$CSSBridge
            log.info { "contextMenu.styleableNode: ${contextMenu.styleableNode.javaClass.name} ${contextMenu.styleableNode}" }

            val styleableNode: Pane = contextMenu.styleableNode as Pane
            log.info { "styleableNode: ${styleableNode.clip}" }

            val skin = contextMenu.skin as javafx.scene.control.skin.ContextMenuSkin
            log.info { "skin:" +
                    " visible: ${skin.node.isVisible}," +
                    " isManaged: ${skin.node.isManaged}," +
                    " isCache: ${skin.node.isCache}," +
                    " isDisabled: ${skin.node.isDisabled}," +
                    " boundsInLocal: ${skin.node.boundsInLocal}," +
                    " boundsInParent: ${skin.node.boundsInParent}," +
                    " layoutBounds: ${skin.node.layoutBounds}," +
                    " cacheHint: ${skin.node.cacheHint}," +
                    " effect: ${skin.node.effect}," +
                    " opacity: ${skin.node.opacity}," +
                    ""
            }
            log.info {  "skin222:" +
                        " window.isShowing: ${skin.node.scene.window.isShowing}," +
                        ""
            }

            log.info { "01 contextMenu.scene.window ${contextMenu.scene.window.width}/${contextMenu.scene.window.height}" }
            contextMenu.scene.window.width += 5
            contextMenu.scene.window.height += 10
            log.info { "02 contextMenu.scene.window ${contextMenu.scene.window.width}/${contextMenu.scene.window.height}" }

            val tkStage: com.sun.javafx.tk.TKStage? = com.sun.javafx.stage.WindowHelper.getPeer(contextMenu.scene.window)
            log.info { "tkStage: $tkStage" }

            //tkStage?.toFront()

            /*
            // It does not work!!!
            if (tkStage is WindowStage) {
                log.info { "needsUpdateWindow()" }
                tkStage.setBounds(-1.0F, -1.0F, false, false, 500.0F, 500.0F, -1.0F, -1.0F, 1.0F, 1.0F, -1.0F, -1.0F)
                tkStage.setOpacity(0.5F)
                tkStage.needsUpdateWindow()
            }
            */

            //val wah: WindowHelper.WindowAccessor? = WindowHelper.getWindowAccessor()
            //wah?.doVisibleChanged(contextMenu.scene.window, true)

            //val wah2 = PopupWindowHelper.getWindowAccessor()
            //wah2?.doVisibleChanging(contextMenu.scene.window, true)
            //wah2?.doVisibleChanged(contextMenu.scene.window, false)
            //wah2?.doVisibleChanged(contextMenu.scene.window, true)
            //wah2?.doVisibleChanged(contextMenu.scene.window, true)
            //wah2?.getPeer()

            //log.info { "03 contextMenu.scene.window ${contextMenu.scene.width}/${contextMenu.scene.height}" }
            //contextMenu.scene.set += 5
            //contextMenu.scene.height += 10
            //log.info { "04 contextMenu.scene.window ${contextMenu.scene.window.width}/${contextMenu.scene.window.height}" }

            //contextMenu.scene.window.width = 1000.0
            //contextMenu.width = 500.0
            //contextMenu.prefWidth = 500.0
            //contextMenu.minWidth = 500.0
            //contextMenu.maxWidth = 500.0

            log.info { "contextMenu.width: ${contextMenu.width}" }

            log.info { "contextMenu.skin.node: ${contextMenu.skin.node.javaClass.name} ${contextMenu.skin.node}" }

            //val aa: com.sun.javafx.scene.control.ContextMenuContent = contextMenu.skin.node as com.sun.javafx.scene.control.ContextMenuContent
            //aa.width = 600.0
            //aa.setMinSize(600.0, 600.0)
            //aa.setMaxSize(600.0, 600.0)
            //aa.setPrefSize(600.0, 600.0)
            //aa.opacity = 0.5
            //aa.prefWidth = aa.width + 10
            //
            //aa.requestLayout()
            //aa.itemsContainer.requestLayout()
            //aa.parent.requestLayout()
            //aa.computeAreaInScreen()
            //aa.autosize()
        }
    }
}
