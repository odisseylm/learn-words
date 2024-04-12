package com.mvv.gui.javafx

import javafx.application.Platform
import javafx.beans.value.WritableValue
import javafx.event.ActionEvent
import javafx.event.Event
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.util.Callback
import javafx.util.StringConverter



class CheckComboBox<T,C:Collection<T>> (
    private val stringLabelConverter: StringConverter<Collection<T>>,
    private val stringDropDownConverter: StringConverter<T>,
            val items: List<T>,
                getter: (()->C), // Just now getter is used only in constructor. T O D O: use it later too.
    private val setter: ((C)->Unit),
                valueToListConverter: (C)->List<T>,
    private val listToValueConverter: (List<T>)->C,
    ) : BorderPane() {
    private val menuButton = MenuButton().also {
        it.styleClass.add("CheckComboBox")
    }

    init { this.styleClass.add("CheckComboBoxContainer") }

    private var valuesAsListInternal: List<T> = valueToListConverter(getter())
    var valuesAsList: List<T>
        get() = valuesAsListInternal
        set(value) {
            valuesAsListInternal = value
            updateTopLabel()
            updateCheckBoxes()
        }

    private val checkListener: EventHandler<ActionEvent> = EventHandler {
        valuesAsListInternal = checked()
        updateTopLabel()
        setter(listToValueConverter(valuesAsList))
    }

    init {
        center = menuButton

        menuButton.items.addAll(
            items.map { CheckMenuItem(it, stringDropDownConverter.toString(it), false, checkListener) }
        )
        updateTopLabel()
        updateCheckBoxes()
    }

    private fun checked(): List<T> =
        checkMenuItems()
            .filter { it.isSelected }
            .map { it.value }
            .toList()

    private fun checkMenuItems() = menuButton.items
        .filterIsInstance<CheckMenuItem<*>>()
        .map {
            @Suppress("UNCHECKED_CAST")
            it as CheckMenuItem<T>
        }

    private fun updateCheckBoxes() {
        val toSelectSet = valuesAsList.toSet()
        val checkMenuItems = checkMenuItems()

        checkMenuItems.forEach {
            it.isSelected = it.value in toSelectSet
        }
    }

    private fun updateTopLabel() {
        menuButton.text = stringLabelConverter.toString(valuesAsList)
    }

    fun showMenu() = menuButton.show()

    fun hideMenu() = menuButton.hide()

    fun isMenuShown(): Boolean = menuButton.isShowing

    //fun addMenuHiddenEventHandler(eventHandler: EventHandler<Event>) =
    //    menuButton.addEventHandler(MenuButton.ON_HIDDEN, eventHandler)

    private class CheckMenuItem<T>(val value: T, label: String, checked: Boolean, actionHandler: EventHandler<ActionEvent>) : CustomMenuItem() {
        private val check = CheckBox(label).also {
            it.isSelected = checked
            it.style = " -fx-padding: 0; -fx-border-insets: 0; -fx-border-width: 0; -fx-alignment: center; "
            it.addEventHandler(ActionEvent.ACTION, actionHandler)
        }
        init {
            content = check
            isHideOnClick = false
            style = " -fx-padding: 0; -fx-border-insets: 0; -fx-border-width: 0; -fx-alignment: center; "
        }

        var isSelected: Boolean
            get() = check.isSelected
            set(value) { check.isSelected = value }
    }
}


class CheckComboBoxCell<S, T, C:Collection<T>>(
    private val stringLabelConverter: StringConverter<Collection<T>>,
    private val stringDropDownConverter: StringConverter<T>,
    val items: List<T>,
    private val valueToListConverter: (C)->List<T>,
    private val listToValueConverter: (List<T>)->C,
    private val altSetter: ((TableCell<S,C>,C)->Unit)? = null,
    private val altSetProperty: ((TableCell<S,C>)->WritableValue<C>)? = null,
    private val updateCellAttrs: ((TableCell<S,C>, S, C)->Unit)? = null,
) : TableCell<S, C>() {

    private val comboBox: CheckComboBox<T,C> by lazy {
        val combo: CheckComboBox<T,C> = CheckComboBox(
            stringLabelConverter,
            stringDropDownConverter,
            items,
            { this.item },
            { newValue ->

                Event.fireEvent(tableColumn, TableColumn.CellEditEvent<S, C>(
                    tableView,
                    TablePosition(tableView, index, this.tableColumn), // getEditingCellAtStartEdit(),
                    TableColumn.editCommitEvent(),
                    newValue
                ))

                this.item = newValue

                val altSetter = this.altSetter
                if (altSetter != null) altSetter(this, newValue)

                val altSetProperty = this.altSetProperty
                if (altSetProperty != null) altSetProperty(this).value = newValue
            },
            valueToListConverter,
            listToValueConverter,
        )

        // Seems it is unsafe!?! Do not do this :-)
        // combo.addMenuHiddenEventHandler { updateItem(this.item, false); cancelEdit() }

        combo
    }

    override fun startEdit() {
        super.startEdit()
        if (!isEditing) return

        comboBox.valuesAsList = valueToListConverter(item)

        text = null
        graphic = comboBox

        runLaterWithDelay(50) { comboBox.showMenu() }
    }

    override fun cancelEdit() {
        Platform.runLater { comboBox.hideMenu() } // to make sure (runLater is used to avoid recursion)
        super.cancelEdit()

        text = stringLabelConverter.toString(item)
        graphic = null
    }

    public override fun updateItem(item: C?, empty: Boolean) {
        super.updateItem(item, empty)
        // item can be null in case of 'empty row'
        if (item == null || empty) {
            text = null
            return
        }

        comboBox.valuesAsList = valueToListConverter(item)

        val isReallyEditing = comboBox.isMenuShown()
        if (graphic == comboBox && !isReallyEditing) {
            graphic = null
        }

        text = if (graphic != comboBox) stringLabelConverter.toString(item)
               else null

        val updateCellAttrs = this.updateCellAttrs
        val rowValue = this.tableRow.item
        if (updateCellAttrs != null && rowValue != null)
            updateCellAttrs(this, rowValue, item)
    }

    companion object {
        fun <S, T, C:Collection<T>> forTableColumn(
            stringLabelConverter: StringConverter<Collection<T>>,
            stringDropDownConverter: StringConverter<T>,
            items: List<T>,
            valueToListConverter: (C)->List<T>,
            listToValueConverter: (List<T>)->C,
            altSetter: ((TableCell<S,C>,C)->Unit)? = null,
            altSetProperty: ((TableCell<S,C>)->WritableValue<C>)? = null,
            updateCellAttrs: ((TableCell<S,C>, S, C)->Unit)? = null,
        ): Callback<TableColumn<S, C>, TableCell<S, C>> =
            Callback {
                CheckComboBoxCell(
                    stringLabelConverter = stringLabelConverter,
                    stringDropDownConverter = stringDropDownConverter,
                    items = items,
                    valueToListConverter = valueToListConverter,
                    listToValueConverter = listToValueConverter,
                    altSetter = altSetter,
                    altSetProperty = altSetProperty,
                    updateCellAttrs = updateCellAttrs,
                )
            }
    }
}
