package com.mvv.gui.javafx

import com.mvv.gui.util.urlEncode
import javafx.application.Platform
import javafx.concurrent.Worker
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.*
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.input.ContextMenuEvent
import javafx.scene.layout.BorderPane
import javafx.scene.web.WebView
import javafx.stage.Modality
import javafx.util.StringConverter
import java.util.*


fun showTextInputDialog(parent: Node, msg: String, title: String = "", defaultValue: String = ""): Optional<String> {
    val textInputDialog = TextInputDialog(defaultValue)

    textInputDialog.title = title
    textInputDialog.headerText = msg

    initDialogParentAndModality(textInputDialog, parent)
    return textInputDialog.showAndWait()
}


fun <T> initDialogParentAndModality(dialog: Dialog<T>, parent: Node): Dialog<T> {

    // It can be needed if stylesheets are not set to parent.scene (but only to some panel),
    // otherwise stylesheets will be inherited automatically.
    //
    //if (parent is Parent) dialog.dialogPane.stylesheets.addAll(parent.stylesheets)

    dialog.initModality(Modality.WINDOW_MODAL)
    dialog.initOwner(parent.scene.window)
    return dialog
}



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


fun showConfirmation(parent: Node, msg: String, title: String, vararg buttonTypes: ButtonType): Optional<ButtonType> {
    val alert = Alert(Alert.AlertType.CONFIRMATION, msg, *buttonTypes)
    alert.title = title

    initDialogParentAndModality(alert, parent)
    return alert.showAndWait()
}


@Suppress("unused")
fun showTextAreaPreviewDialog(parent: Node, title: String, textAreaContent: String, wrapText: Boolean = true,
                              width: Double? = null, height: Double? = null) {
    val dialog = Dialog<Unit>()
    dialog.title = title
    val type = ButtonType("Ok", ButtonBar.ButtonData.OK_DONE)

    dialog.dialogPane.content = BorderPane(
            TextArea(textAreaContent).also { it.isWrapText = wrapText; it.isEditable = false }
        )
        .also { it.padding = Insets(0.0, 4.0, 0.0, 4.0) }
    dialog.dialogPane.buttonTypes.add(type)

    if (width != null)  dialog.width  = width
    if (height != null) dialog.height = height

    dialog.isResizable = true
    initDialogParentAndModality(dialog, parent)

    dialog.showAndWait()
}


fun showHtmlTextPreviewDialog(parent: Node, title: String, html: String) {
    val dialog = Dialog<Unit>()
    dialog.title = title
    val type = ButtonType("Ok", ButtonBar.ButtonData.OK_DONE)

    val rootStylesheets = (parent.scene.stylesheets +
            if (parent is Parent) parent.stylesheets else emptyList())
        .distinct()

    val htmlTextPreviewCssStyle = getStyles("htmlTextPreview", rootStylesheets).asCssText

    dialog.dialogPane.content = BorderPane(
        WebView().also { webView ->
            webView.styleClass.add("htmlTextPreview")

            webView.isContextMenuEnabled = false // disable default context menu
            webView.onContextMenuRequested = EventHandler { showHtmlTextPreviewContextMenu(webView, it) }

            val webEngine = webView.engine
            webEngine.isJavaScriptEnabled = false

            webEngine.loadWorker.stateProperty().addListener { _, _, newState ->
                if (newState == Worker.State.SUCCEEDED) {
                    val doc = webEngine.document
                    val styleNode = doc.createElement("style")
                    styleNode.appendChild(doc.createTextNode("body { $htmlTextPreviewCssStyle } "))
                    doc.documentElement.getElementsByTagName("head").item(0).appendChild(styleNode)
                }
            }

            webEngine.loadContent(html)
        })
        .also { it.padding = Insets(0.0, 4.0, 0.0, 4.0); }

    dialog.dialogPane.buttonTypes.add(type)

    dialog.isResizable = true
    initDialogParentAndModality(dialog, parent)

    dialog.dialogPane.styleClass.add("htmlTextPreviewDialogPane")

    dialog.showAndWait()
}

private fun showHtmlTextPreviewContextMenu(webView: WebView, ev: ContextMenuEvent) {

    val selectedText = webView.selectedText
    val text = webView.textContent

    val menu = ContextMenu()
    //useMenuStateDumping(menu)

    if (selectedText.isNotBlank())
    menu.items.add(newMenuItem("Copy", buttonIcon("icons/copy16x16.gif")) {
        putNonBlankToClipboard(selectedText) })

    menu.items.add(newMenuItem("Copy All", buttonIcon("icons/copy16x16.gif")) {
        putNonBlankToClipboard(text) })

    if (selectedText.isNotBlank())
    menu.items.add(SeparatorMenuItem())

    if (selectedText.isNotBlank())
    menu.items.add(newMenuItem("Translate by Google", buttonIcon("icons/gt-02.png")) {
        openGoogleTranslate(selectedText) })

    menu.items.add(newMenuItem("Translate All by Google", buttonIcon("icons/gt-02.png")) {
        openGoogleTranslate(text) })

    if (selectedText.isNotBlank())
    menu.items.add(SeparatorMenuItem())

    if (selectedText.isNotBlank())
    menu.items.add(newMenuItem("Translate word by Abby", buttonIcon("icons/abby-icon-01.png")) {
        openAbbyLingvoTranslate(selectedText) })

    // If you pass window popup/context menu is successfully hidden
    // contrary if you pass directly 'webView' node, menu is not always auto-hidden properly.
    menu.show(webView.scene.window, ev.screenX, ev.screenY)
}


private fun putNonBlankToClipboard(text: String?) {
    if (!text.isNullOrBlank())
        Clipboard.getSystemClipboard()
            .setContent(ClipboardContent().also { it.putString(text) })
}

fun openGoogleTranslate(text: String) =
    openDefaultWebBrowser("https://translate.google.com/?sl=en&tl=ru&op=translate&text=${urlEncode(text)}")

fun openAbbyLingvoTranslate(text: String) =
    // hm... strange Abby site does not process '+' as space char, need to escape it as '%20'
    // and now it is not accessible without VPN (for that reason we use Opera)
    openWebBrowser(BrowserType.Opera, "https://www.lingvolive.com/en-us/translate/en-ru/${urlEncode(text).replace("+", "%20")}")


fun <T> showDropDownDialog(
    parent: Node, title: String, items: List<T>, converter: StringConverter<T>,
    hasEditor: Boolean,
    width: Double? = null, height: Double? = null,
): T? {

    // We can replace it by javafx.scene.control.ChoiceDialog
    // I just didn't know about it :-)
    //
    val dialog = Dialog<T>()
    dialog.title = title

    val comboBox = ComboBox<T>().also {
        it.items.setAll(items)
        it.isEditable = hasEditor
        it.converter  = converter

        Platform.runLater { it.requestFocus() }
    }

    dialog.dialogPane.content = BorderPane(comboBox)
    dialog.dialogPane.buttonTypes.addAll(
        ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE),
        ButtonType("Ok", ButtonBar.ButtonData.OK_DONE),
    )

    if (width  != null) dialog.width  = width
    if (height != null) dialog.height = height

    dialog.isResizable = true
    initDialogParentAndModality(dialog, parent)

    dialog.setResultConverter { buttonType ->
        if (buttonType.buttonData == ButtonBar.ButtonData.OK_DONE) comboBox.selectionModel.selectedItem else null
    }

    return dialog.showAndWait().orElse(null)
}
