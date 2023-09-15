package com.mvv.gui.javafx

import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.Tooltip
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import java.io.ByteArrayInputStream
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readBytes


@Suppress("unused")
fun newButton(label: String, action: ()->Unit): Button = newButtonImpl(label, null, null, action)

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
        action?. let { btn.onAction = EventHandler { action() } }
    }


fun buttonIcon(path: String, iconSize: Double = 16.0): ImageView {

    val image = if (Path.of(path).exists())
        Image(ByteArrayInputStream(Path.of(path).readBytes()), iconSize, iconSize, true, true)
    else
        Image(path, iconSize, iconSize, true, true)

    return ImageView(image)
}
