package com.mvv.gui

import com.sun.javafx.binding.ExpressionHelper
import javafx.beans.InvalidationListener
import javafx.beans.property.ReadOnlyProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue



class AroundReadOnlyIntegerProperty<OtherPropertyType>(
    private val bean: Any,
    private val name: String,
    private val baseProperty: ObservableValue<OtherPropertyType>,
    val convertFunction: (OtherPropertyType?)->Int,
) : ReadOnlyProperty<Int>, ObservableValue<Int> {

    private var helper: ExpressionHelper<Int>? = null

    init {
        baseProperty.addListener { _, _, _ -> fireValueChangedEvent() } // <= Do I need this??
        baseProperty.addListener { _ -> fireValueChangedEvent() }
    }

    override fun addListener(listener: InvalidationListener) {
        helper = ExpressionHelper.addListener(helper, this, listener)
    }

    override fun removeListener(listener: InvalidationListener) {
        helper = ExpressionHelper.removeListener(helper, listener)
    }

    override fun getValue(): Int = convertFunction(baseProperty.value)

    override fun addListener(listener: ChangeListener<in Int>) {
        helper = ExpressionHelper.addListener(helper, this, listener)
    }

    override fun removeListener(listener: ChangeListener<in Int>) {
        helper = ExpressionHelper.removeListener(helper, listener)
    }

    private fun fireValueChangedEvent() = ExpressionHelper.fireValueChangedEvent(helper)

    override fun getBean(): Any = bean

    override fun getName(): String = name
}
