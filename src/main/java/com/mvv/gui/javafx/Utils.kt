package com.mvv.gui.javafx

import com.mvv.gui.util.containsOneOf
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.concurrent.Worker
import javafx.css.CssParser
import javafx.css.ParsedValue
import javafx.css.Rule
import javafx.css.Selector
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.*
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.BorderPane
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.web.WebView
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.util.Duration
import java.io.ByteArrayInputStream
import java.nio.file.Path
import java.util.*
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.math.roundToInt


private val log = mu.KotlinLogging.logger {}


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
fun showTextAreaPreviewDialog(parent: Node, title: String, textAreaContent: String, wrapText: Boolean = true) {
    val dialog = Dialog<Unit>()
    dialog.title = title
    val type = ButtonType("Ok", ButtonData.OK_DONE)

    dialog.dialogPane.content = BorderPane(
            TextArea(textAreaContent).also { it.isWrapText = wrapText }
        )
        .also { it.padding = Insets(0.0, 4.0, 0.0, 4.0) }
    dialog.dialogPane.buttonTypes.add(type)

    dialog.isResizable = true
    initDialogParentAndModality(dialog, parent)

    dialog.showAndWait()
}


private class TempResourceLocator()

fun showHtmlTextPreviewDialog(parent: Node, title: String, html: String) {
    val dialog = Dialog<Unit>()
    dialog.title = title
    val type = ButtonType("Ok", ButtonData.OK_DONE)

    val rootStylesheets = (parent.scene.stylesheets +
            if (parent is Parent) parent.stylesheets else emptyList())
        .distinct()

    val htmlTextPreviewCssStyle = getStyles("htmlTextPreview", rootStylesheets).asCssText

    dialog.dialogPane.content = BorderPane(
            WebView().also { webView ->
                webView.styleClass.add("htmlTextPreview")

                val webEngine = webView.engine
                webEngine.isJavaScriptEnabled = false

                webEngine.loadWorker.stateProperty().addListener { obs, oldState, newState ->
                    if (newState == Worker.State.SUCCEEDED) {
                        val doc = webEngine.document
                        val styleNode = doc.createElement("style")
                        styleNode.appendChild(doc.createTextNode("body { $htmlTextPreviewCssStyle } "))
                        doc.documentElement.getElementsByTagName("head").item(0).appendChild(styleNode)
                    }
                }

                webEngine.loadContent(html)
            }
        )
        .also { it.padding = Insets(0.0, 4.0, 0.0, 4.0); }

    dialog.dialogPane.buttonTypes.add(type)

    dialog.isResizable = true
    initDialogParentAndModality(dialog, parent)

    dialog.dialogPane.styleClass.add("htmlTextPreviewDialogPane")

    dialog.showAndWait()
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


private fun emptyIcon16x16() = Rectangle(16.0, 16.0, Color(0.0, 0.0, 0.0, 0.0))

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
        action?.let         { menuItem.onAction = EventHandler { it() } }
    }


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


fun buttonIcon(path: String, iconSize: Double = 16.0): ImageView {

    val image = if (Path.of(path).exists())
        Image(ByteArrayInputStream(Path.of(path).readBytes()), iconSize, iconSize, true, true)
    else
        Image(path, iconSize, iconSize, true, true)

    return ImageView(image)
}


val TableView<*>.isEditing: Boolean get() {
    val editingCell = this.editingCell
    return (editingCell != null) && editingCell.row != -1 && editingCell.column != -1
}


val <T> TableView<T>.singleSelection: T? get() {
    val selected = this.selectionModel.selectedItems
    return if (selected.size == 1) selected.first() else null
}


fun runLaterWithDelay(delayMillis: Long, action: ()->Unit) {
    val timeline = Timeline(KeyFrame(Duration.millis(delayMillis.toDouble()), { Platform.runLater(action) }))
    timeline.cycleCount = 1
    timeline.play()
}


fun toLowerCase(textInput: TextInputControl) {

    val currentText = textInput.text
    val hasUpperCaseChars = currentText.any { it.isUpperCase() }
    if (!hasUpperCaseChars) return

    val caretPosition = textInput.caretPosition
    val anchor = textInput.anchor

    textInput.text = currentText.lowercase()
    textInput.selectRange(anchor, caretPosition)
}


@Suppress("SameParameterValue")
private fun getStyles(selector: String, cssFiles: Iterable<String>): List<Rule> {

    val cssRules: List<Rule> = cssFiles.flatMap { cssFile ->
        val cssParser = CssParser()
        val r = cssParser.parse(
            TempResourceLocator::class.java.getResource(cssFile)
                ?: TempResourceLocator::class.java.getResource("/$cssFile")
        )
        r.rules
    }

    val ruleSelectors = listOf(selector, ".$selector", "*.$selector").map { Selector.createSelector(it) }
    return cssRules.filter { it.selectors.containsOneOf(ruleSelectors) } // .also { println("asCssText: ${it.asCssText}") }
}

private val List<Rule>.asCssText: String get() {
    val aa: List<String> = this
        .asReversed()
        .flatMap { it.declarations }
        .map { it.property + ": " + it.parsedValue.toCssStringValue() }

    return aa.joinToString("; ")
}


private fun Any?.toCssStringValue(): String =
    when (this) {
        null -> ""
        is ParsedValue<*,*> ->
            when (val value = this.value) {
                is ParsedValue<*,*> -> value.toCssStringValue()

                is Array<*> -> value.joinToString(" ") {
                    if (it is ParsedValue<*, *>) it.toCssStringValue() else it.toCssStringValue() }

                is Iterable<*> -> value.joinToString(" ") {
                    if (it is ParsedValue<*, *>) it.toCssStringValue() else it.toCssStringValue()
                }

                else -> value.toCssStringValue()
            }
        is Color -> this.toCssColor()
        is String -> this
        else -> {
            log.warn { "${this.javaClass.simpleName} probably is converted to CCS improperly." }
            this.toString()
        }
    }

fun Color.toCssColor(): String =
    "#%02x%02x%02x%02x".format(
        (this.red * 255).roundToInt(),
        (this.green * 255).roundToInt(),
        (this.blue * 255).roundToInt(),
        (this.opacity * 255).roundToInt(),
    )
