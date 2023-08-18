package com.mvv.gui

import com.mvv.gui.TextFieldTableCellUtils.Companion.createTextArea
import com.mvv.gui.TextFieldTableCellUtils.Companion.createTextField
import javafx.application.Platform
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCombination
import javafx.scene.input.KeyEvent
import javafx.scene.layout.HBox
import javafx.util.Callback
import javafx.util.StringConverter
import javafx.util.converter.DefaultStringConverter


/**
 * @param <S> The type of the TableView generic type
 * @param <T> The type of the elements contained within the TableColumn.
 */
typealias BeanPropertySetter<S,T> = (T, S) -> Unit


/**
 * A class containing a [TableCell] implementation that draws a
 * [TextField] node inside the cell.
 *
 *
 * By default, the TextFieldTableCell is rendered as a [Label] when not
 * being edited, and as a TextField when in editing mode. The TextField will, by
 * default, stretch to fill the entire table cell.
 *
 * @param <S> The type of the TableView generic type
 * @param <T> The type of the elements contained within the TableColumn.
 * @since JavaFX 2.2
</T></S> */
class ExTextFieldTableCell<S, T>

    /**
     * Creates a TextFieldTableCell that provides a [TextField] when put
     * into editing mode that allows editing of the cell content. This method
     * will work on any TableColumn instance, regardless of its generic type.
     * However, to enable this, a [StringConverter] must be provided that
     * will convert the given String (from what the user typed in) into an
     * instance of type T. This item will then be passed along to the
     * [TableColumn.onEditCommitProperty] callback.
     *
     * @param converter A [converter][StringConverter] that can convert
     * the given String (from what the user typed in) into an instance of
     * type T.
     */
    private constructor(
        private val textFieldType: TextFieldType,
        converter: StringConverter<T>,
        private val customValueSetter: BeanPropertySetter<S,T>? = null,
    )

    : TableCell<S, T>() {

    enum class TextFieldType { TextField, TextArea }

    /* *************************************************************************
     *                                                                         *
     * Fields                                                                  *
     *                                                                         *
     **************************************************************************/
    private var textField: TextInputControl? = null

    /* *************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/
    // --- converter
    private val converter: ObjectProperty<StringConverter<T>?> = SimpleObjectProperty(this, "converter")

    init {
        styleClass.add("text-field-table-cell")
        // !!! after 'private val converter' TODO: refactor this strange behavior/dependency
        setConverter(converter)
    }


    /**
     * The [StringConverter] property.
     * @return the [StringConverter] property
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun converterProperty(): ObjectProperty<StringConverter<T>?> = converter

    /**
     * Sets the [StringConverter] to be used in this cell.
     * @param value the [StringConverter] to be used in this cell
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun setConverter(value: StringConverter<T>?) = converterProperty().set(value)

    /**
     * Returns the [StringConverter] used in this cell.
     * @return the [StringConverter] used in this cell
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun getConverter(): StringConverter<T>? = converterProperty().get()


    /* *************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/
    /** {@inheritDoc}  */
    override fun startEdit() {
        super.startEdit()
        if (!isEditing) {
            return
        }
        if (textField == null) {
            textField = when (textFieldType) {
                TextFieldType.TextArea  -> createTextArea(this, getConverter(), customValueSetter)
                TextFieldType.TextField -> createTextField(this, getConverter(), customValueSetter)
            }
        }

        TextFieldTableCellUtils.startEdit(this, getConverter(), null, null, textField)
    }

    /** {@inheritDoc}  */
    override fun cancelEdit() {
        super.cancelEdit()
        TextFieldTableCellUtils.cancelEdit(this, getConverter(), null)
    }

    /** {@inheritDoc}  */
    public override fun updateItem(item: T, empty: Boolean) {
        super.updateItem(item, empty)
        TextFieldTableCellUtils.updateItem(this, getConverter(), null, null, textField)
    }


    companion object {
        /* *************************************************************************
         *                                                                         *
         * Static cell factories                                                   *
         *                                                                         *
         **************************************************************************/

        /**
         * Provides a [TextField] that allows editing of the cell content when
         * the cell is double-clicked, or when
         * [TableView.edit] is called.
         * This method will only  work on [TableColumn] instances which are of
         * type String.
         *
         * @param <S> The type of the TableView generic type
         * @return A [Callback] that can be inserted into the
         * [cell factory property][TableColumn.cellFactoryProperty] of a
         * TableColumn, that enables textual editing of the content.
        </S> */
        fun <S> forStringTableColumn(textFieldType: TextFieldType): Callback<TableColumn<S, String>, TableCell<S, String>> =
            forTableColumn<S,String>(textFieldType, DefaultStringConverter())

        @Suppress("unused")
        fun <S> forStringTableColumn(textFieldType: TextFieldType, customValueSetter: (String, S) -> Unit): Callback<TableColumn<S, String>, TableCell<S, String>> =
            forTableColumn<S,String>(textFieldType, DefaultStringConverter(), customValueSetter)

        /**
         * Provides a [TextField] that allows editing of the cell content when
         * the cell is double-clicked, or when
         * [TableView.edit] is called.
         * This method will work  on any [TableColumn] instance, regardless of
         * its generic type. However, to enable this, a [StringConverter] must
         * be provided that will convert the given String (from what the user typed
         * in) into an instance of type T. This item will then be passed along to the
         * [TableColumn.onEditCommitProperty] callback.
         *
         * @param <S> The type of the TableView generic type
         * @param <T> The type of the elements contained within the TableColumn
         * @param converter A [StringConverter] that can convert the given String
         * (from what the user typed in) into an instance of type T.
         * @return A [Callback] that can be inserted into the
         * [cell factory property][TableColumn.cellFactoryProperty] of a
         * TableColumn, that enables textual editing of the content.
        </T></S> */
        @Suppress("MemberVisibilityCanBePrivate")
        fun <S, T> forTableColumn(textFieldType: TextFieldType, converter: StringConverter<T>): Callback<TableColumn<S, T>, TableCell<S, T>> =
            Callback { _: TableColumn<S, T>? -> ExTextFieldTableCell(textFieldType, converter) }

        @Suppress("MemberVisibilityCanBePrivate")
        fun <S, T> forTableColumn(textFieldType: TextFieldType, converter: StringConverter<T>, customValueSetter: BeanPropertySetter<S,T>): Callback<TableColumn<S, T>, TableCell<S, T>> =
            Callback { _: TableColumn<S, T>? -> ExTextFieldTableCell(textFieldType, converter, customValueSetter) }

    } // companion end
}


/**
 * It is modified copy of javafx.scene.control.cell.CellUtils (only functions which are required for TableView)
 */
internal class TextFieldTableCellUtils {

    companion object {

        fun <T> cancelEdit(cell: Cell<T>, converter: StringConverter<T>?, graphic: Node?) {
            cell.text = getItemText(cell, converter)
            cell.graphic = graphic
        }

        private fun <T> getItemText(cell: Cell<T>, converter: StringConverter<T>?): String =
            if (converter == null) { if (cell.item == null) "" else cell.item.toString() }
            else converter.toString(cell.item)


        fun <T> updateItem(cell: Cell<T>, converter: StringConverter<T>?, hbox: HBox?, graphic: Node?, textField: TextInputControl?) {

            if (cell.isEmpty) {
                cell.text = null
                cell.setGraphic(null)
            } else {
                if (cell.isEditing) {
                    if (textField != null) {
                        textField.text = getItemText(cell, converter)
                    }
                    cell.text = null
                    if (graphic != null) {
                        hbox?.children?.setAll(graphic, textField)
                        cell.setGraphic(hbox)
                    } else {
                        cell.setGraphic(textField)
                    }
                } else {
                    cell.text = getItemText(cell, converter)
                    cell.setGraphic(graphic)
                }
            }
        }


        fun <T> startEdit(cell: Cell<T>, converter: StringConverter<T>?, hbox: HBox?, graphic: Node?, textField: TextInputControl?) {

            if (textField != null) {
                val itemText = getItemText(cell, converter)

                if (textField is TextArea) {
                    textField.prefRowCount = itemText.split("\n").size
                }

                textField.text = itemText
            }
            cell.text = null
            if (graphic != null) {
                hbox?.children?.setAll(graphic, textField)
                cell.setGraphic(hbox)
            } else {
                cell.setGraphic(textField)
            }
            textField?.selectAll()

            // requesting focus so that key input can immediately go into the
            // TextField (see RT-28132)
            textField?.requestFocus()
        }


        fun <S,T> createTextField(cell: TableCell<S,T>, converter: StringConverter<T>?, customValueSetter: ((T, S)->Unit)?): TextField {
            val textField = TextField(getItemText(cell, converter))

            // Use onAction here rather than onKeyReleased (with check for Enter),
            // as otherwise we encounter RT-34685
            textField.onAction = EventHandler { event: ActionEvent ->
                commitEdit(converter, textField, cell, customValueSetter)
                event.consume()
            }

            addKeyBinding(textField, lowerCaseKeyCombination) { toLowerCase(it) }

            textField.addEventHandler(KeyEvent.KEY_RELEASED) {
                processEditCompletion(textField, it, cell, converter, customValueSetter) }

            return textField
        }


        /**
         * @param <S> The type of the TableView generic type
         * @param <T> The type of the elements contained within the TableColumn.
         */
        fun <T,S> createTextArea(cell: TableCell<S,T>, converter: StringConverter<T>?, customValueSetter: ((T,S)->Unit)?): TextArea {

            val textField = TextArea(getItemText(cell, converter))

            /*
            textField.focusedProperty().addListener { prop, oldPropertyValue, newPropertyValue ->
                if (!newPropertyValue) {
                    Math.random()
                }

                if (!newPropertyValue && cell.isEditing) {
                    commitEdit(converter, textField, cell, customValueSetter)
                }
            }
            */

            addKeyBinding(textField, lowerCaseKeyCombination) { toLowerCase(it) }

            textField.addEventHandler(KeyEvent.KEY_RELEASED) {
                processEditCompletion(textField, it, cell, converter, customValueSetter) }

            return textField
        }

        private fun <S, T> processEditCompletion(
            textField: TextInputControl, keyEvent: KeyEvent, cell: TableCell<S, T>,
            converter: StringConverter<T>?, customValueSetter: ((T, S) -> Unit)?
        ) {
            if (keyEvent.isControlDown && (keyEvent.character == "\n" || keyEvent.code == KeyCode.ENTER)) {

                val index = cell.index
                val tableColumn = cell.tableColumn

                commitEdit(converter, textField, cell, customValueSetter)
                keyEvent.consume()

                if (index >= 0) Platform.runLater {
                    cell.tableView.requestFocus()
                    cell.tableView.selectionModel.select(index, tableColumn)
                }
            }

            if (keyEvent.code == KeyCode.ESCAPE) {
                cell.cancelEdit()
                keyEvent.consume()
            }
        }


        private fun <S, T> commitEdit(converter: StringConverter<T>?, textField: TextInputControl,
                                      cell: TableCell<S, T>, customValueSetter: BeanPropertySetter<S,T>?) {
            checkNotNull(converter) {
                ("Attempting to convert text input into Object, but provided "
                        + "StringConverter is null. Be sure to set a StringConverter "
                        + "in your cell factory.")
            }

            val newValue = converter.fromString(textField.text)

            // custom update model procedure (use it if you use standard java beans)
            val tableRowItem: S = cell.tableRow.item
            customValueSetter?.invoke(newValue, tableRowItem)

            // default update model procedure
            cell.commitEdit(converter.fromString(textField.text))
        }

    } // companion object
}


fun <C: Control> addKeyBinding(control: C, keyBinding: KeyCombination, action: (C)-> Unit) =
    addKeyBinding(control, mapOf(keyBinding to action))

fun <C: Control> addKeyBinding(control: C, keyBindings: Map<KeyCombination, (C)-> Unit>) {
    control.addEventHandler(KeyEvent.KEY_PRESSED) {
        keyBindings.forEach { (keyBinding, action) -> if (keyBinding.match(it)) action(control) } }
    control.addEventHandler(KeyEvent.KEY_TYPED) {
        keyBindings.forEach { (keyBinding, action) -> if (keyBinding.match(it)) action(control) } }
    control.addEventHandler(KeyEvent.KEY_RELEASED) {
        keyBindings.forEach { (keyBinding, action) -> if (keyBinding.match(it)) action(control) } }
}
