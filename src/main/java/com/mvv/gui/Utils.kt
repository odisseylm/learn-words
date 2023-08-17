package com.mvv.gui

import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.Priority
import javafx.stage.Modality
import javafx.stage.Stage
import java.io.FileInputStream
import java.nio.file.Path
import java.util.*
import kotlin.io.path.exists


@Suppress("unused")
fun showInfoAlert(parent: Node, msg: String, title: String = "Info") =
    showAlert(parent, msg, title)

fun showErrorAlert(parent: Node, msg: String, title: String = "Error") =
    showAlert(parent, msg, title)


private fun showAlert(parent: Node, msg: String, title: String) {

    // It looks very ugly comparing with Swing :-(
    //val alert = Alert(Alert.AlertType.ERROR, msg)
    //
    // it does not help to have nicer look :-(
    //alert.initStyle(StageStyle.UNIFIED)


    val alert = Alert(Alert.AlertType.NONE, msg, ButtonType.OK)
    alert.title = title

    initDialogParentAndModality(alert, parent)
    alert.showAndWait()
}


@Suppress("unused")
fun columnConstraints(priority: Priority? = null, hAlignment: HPos? = null, fillWidth: Boolean? = null): ColumnConstraints {

    //val USE_PREF_SIZE = javafx.scene.layout.Region.USE_PREF_SIZE
    //
    //import javafx.scene.layout.Region.USE_PREF_SIZE
    //ColumnConstraints(-1.0, -1.0, -1.0, Priority.NEVER, HPos.RIGHT, false)
    //ColumnConstraints(USE_PREF_SIZE, USE_PREF_SIZE, USE_PREF_SIZE, Priority.NEVER, HPos.RIGHT, false)

    val constr = ColumnConstraints()
    if (priority   != null) constr.hgrow       = priority
    if (hAlignment != null) constr.halignment  = hAlignment
    if (fillWidth  != null) constr.isFillWidth = fillWidth

    return constr
}


fun newButton(label: String, action: ()->Unit): Button {
    val button = Button(label)
    button.onAction = EventHandler { action() }
    return button
}

fun newButton(label: String, icon: ImageView, action: ()->Unit): Button {
    val button = Button(label, icon)
    button.onAction = EventHandler { action() }
    return button
}


fun showTextInputDialog(parent: Node, msg: String, title: String = ""): Optional<String> {
    val textInputDialog = TextInputDialog("")

    textInputDialog.title = title
    textInputDialog.headerText = msg

    initDialogParentAndModality(textInputDialog, parent)
    return textInputDialog.showAndWait()
}


fun <T> initDialogParentAndModality(dialog: Dialog<T>, parent: Node): Dialog<T> {
    dialog.initModality(Modality.WINDOW_MODAL)
    dialog.initOwner(parent.scene.window)
    return dialog
}


fun setWindowTitle(node: Node, title: String) {
    if (node.scene.window is Stage) {
        (node.scene.window as Stage).title = title
    }
}


fun buttonIcon(path: String, iconSize: Double = 16.0): ImageView {

    // TODO: play with params
    val image = if (Path.of(path).exists()) {
        Image(FileInputStream(path), iconSize, iconSize, true, true)
    } else {
        Image(path, iconSize, iconSize, true, true)
    }

    //@Suppress("UnnecessaryVariable")
    //val imageView = ImageView(image)
    //
    //val image = Image(input)
    //if (iconSize > 0) {
    //    imageView.fitWidth = iconSize
    //    imageView.fitHeight = iconSize
    //}
    //
    //return imageView

    return ImageView(image)
}



fun String.removeSuffixCaseInsensitive(suffix: String): String =
    when {
        this.endsWith(suffix) -> this.removeSuffix(suffix)
        this.lowercase().endsWith(suffix.lowercase()) -> this.substring(0, this.length - suffix.length)
        else -> this
    }
