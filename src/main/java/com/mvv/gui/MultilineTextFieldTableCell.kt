package com.mvv.gui

import javafx.application.Platform
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.KeyCode
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
class MultilineTextFieldTableCell<S, T>

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
        converter: StringConverter<T>,
        private val customValueSetter: BeanPropertySetter<S,T>? = null,
    )

    : TableCell<S, T>() {

    /* *************************************************************************
     *                                                                         *
     * Fields                                                                  *
     *                                                                         *
     **************************************************************************/
    private var textField: TextArea? = null

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
            textField = CellUtils2.createTextArea(this, getConverter(), customValueSetter)
        }

        CellUtils2.startEdit(this, getConverter(), null, null, textField)
    }

    /** {@inheritDoc}  */
    override fun cancelEdit() {
        super.cancelEdit()
        CellUtils2.cancelEdit(this, getConverter(), null)
    }

    /** {@inheritDoc}  */
    public override fun updateItem(item: T, empty: Boolean) {
        super.updateItem(item, empty)
        CellUtils2.updateItem(this, getConverter(), null, null, textField)
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
        fun <S> forStringTableColumn(): Callback<TableColumn<S, String>, TableCell<S, String>> =
            forTableColumn<S,String>(DefaultStringConverter())

        @Suppress("unused")
        fun <S> forStringTableColumn(customValueSetter: (String, S) -> Unit): Callback<TableColumn<S, String>, TableCell<S, String>> =
            forTableColumn<S,String>(DefaultStringConverter(), customValueSetter)

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
        fun <S, T> forTableColumn(converter: StringConverter<T>): Callback<TableColumn<S, T>, TableCell<S, T>> =
            Callback { _: TableColumn<S, T>? -> MultilineTextFieldTableCell(converter) }

        @Suppress("MemberVisibilityCanBePrivate")
        fun <S, T> forTableColumn(converter: StringConverter<T>, customValueSetter: BeanPropertySetter<S,T>): Callback<TableColumn<S, T>, TableCell<S, T>> =
            Callback { _: TableColumn<S, T>? -> MultilineTextFieldTableCell(converter, customValueSetter) }

    } // companion end
}


class CellUtils2 {

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


        @Suppress("unused")
        fun <S,T> createTextField(cell: TableCell<S,T>, converter: StringConverter<T>?, customValueSetter: ((T, S)->Unit)?): TextField {
            val textField = TextField(getItemText(cell, converter))

            // Use onAction here rather than onKeyReleased (with check for Enter),
            // as otherwise we encounter RT-34685
            textField.onAction = EventHandler { event: ActionEvent ->
                commitEdit(converter, textField, cell, customValueSetter)
                event.consume()
            }

            textField.onKeyReleased = EventHandler { t: KeyEvent ->
                if (t.code == KeyCode.ESCAPE) {
                    cell.cancelEdit()
                    t.consume()
                }
            }
            return textField
        }


        /**
         * @param <S> The type of the TableView generic type
         * @param <T> The type of the elements contained within the TableColumn.
         */
        fun <T,S> createTextArea(cell: TableCell<S,T>, converter: StringConverter<T>?, customValueSetter: ((T,S)->Unit)?): TextArea {

            val textField = TextArea(getItemText(cell, converter))

            /*
            val l: ChangeListener<in Number> = object : ChangeListener<Number> {
                override fun changed(observable: ObservableValue<out Number>?, oldValue: Number?, newValue: Number?) {
                    println("height changed from $oldValue to $newValue")
                    Exception("height changed from $oldValue to $newValue").printStackTrace()
                }
            }
            textField.heightProperty().addListener(l)
            */

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

            textField.onKeyReleased = EventHandler { t: KeyEvent ->

                if (t.isControlDown && (t.character == "\n" || t.code == KeyCode.ENTER)) {

                    val index = cell.index
                    val tableColumn = cell.tableColumn

                    commitEdit(converter, textField, cell, customValueSetter)
                    t.consume()

                    if (index >= 0) Platform.runLater {
                        cell.tableView.requestFocus()
                        cell.tableView.selectionModel.select(index, tableColumn)
                    }
                }

                if (t.code == KeyCode.ESCAPE) {
                    cell.cancelEdit()
                    t.consume()
                }
            }
            return textField
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
