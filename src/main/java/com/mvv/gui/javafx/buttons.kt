package com.mvv.gui.javafx

import com.mvv.gui.util.doIfNotBlank
import javafx.scene.control.Button
import javafx.scene.control.ContentDisplay
import javafx.scene.control.Tooltip
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import java.io.ByteArrayInputStream
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readBytes


enum class ToolBarButtonType {
    Small,  // 16x16
    Middle, // 24x24
}


@Suppress("unused")
fun newButton(label: String, action: ()->Unit): Button = newButtonImpl(label, null, null, action)

fun newButton(icon: ImageView, action: ()->Unit): Button = newButtonImpl("", null, icon, action)

fun newButton(label: String, icon: ImageView, action: ()->Unit): Button =
    newButtonImpl(label, null, icon, action)

fun newButton(label: String, toolTip: String, icon: ImageView, action: ()->Unit): Button =
    newButtonImpl(label, toolTip, icon, action)

@Suppress("unused")
fun newButton(label: String, toolTip: String, action: ()->Unit): Button =
    newButtonImpl(label, toolTip, null, action)

private fun newButtonImpl(label: String, toolTip: String?, icon: ImageView?, action: (()->Unit)?): Button =
    Button(label).also { btn ->
        toolTip?.let { btn.tooltip  = Tooltip(toolTip) }
        icon?.   let { btn.graphic  = icon }
        action?. let { btn.onAction = safeRunMenuCommand(action) }
    }


@Suppress("unused")
fun new24xButton(label: String, action: ()->Unit): Button = new24xButtonImpl(label, null, null, action)

fun new24xButton(icon: ImageView, action: ()->Unit): Button = new24xButtonImpl("", null, icon, action)

fun new24xButton(label: String, icon: ImageView, action: ()->Unit): Button =
    new24xButtonImpl(label, null, icon, action)

fun new24xButton(label: String, toolTip: String, icon: ImageView, action: ()->Unit): Button =
    new24xButtonImpl(label, toolTip, icon, action)

@Suppress("unused")
fun new24xButton(label: String, toolTip: String, action: ()->Unit): Button =
    new24xButtonImpl(label, toolTip, null, action)

private fun new24xButtonImpl(label: String?, toolTip: String?, icon: ImageView?, action: (()->Unit)?): Button =
    Button(label).also { btn ->
        val cssClass = if (label.isNullOrBlank()) "toolBarButton24x" else "toolBarLabelledButton24x"
        btn.styleClass.add(cssClass)
        label?.doIfNotBlank {
            btn.text = label
            btn.contentDisplay = ContentDisplay.TOP
        }
        toolTip?.let { btn.tooltip  = Tooltip(toolTip) }
        icon?.   let { btn.graphic  = icon }
        action?. let { btn.onAction = safeRunMenuCommand(action) }
    }


fun button24xIcon(path: String): ImageView = buttonIcon(path, 24.0)

fun buttonIcon(path: String, iconSize: Double = 16.0): ImageView {

    val image = if (Path.of(path).exists())
        Image(ByteArrayInputStream(Path.of(path).readBytes()), iconSize, iconSize, true, true)
    else
        Image(path, iconSize, iconSize, true, true)

    return ImageView(image)
}
