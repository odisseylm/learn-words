package com.mvv.gui.javafx

import com.sun.javafx.binding.ExpressionHelper
import javafx.beans.InvalidationListener
import javafx.beans.property.ReadOnlyProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.beans.value.WritableObjectValue
import kotlin.reflect.KMutableProperty0


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



enum class UpdateSet { Set, Remove }


// optimized setter/updater helper to avoid unneeded changes and unneeded FxJava possible value change notifications
fun <T> updateSetProperty(setProperty: KMutableProperty0<Set<T>>, value: T, action: UpdateSet) =
    updateSetPropertyImpl({ setProperty.get() }, { setProperty.set(it) }, value, action)

fun <T> updateSetProperty(setProperty: WritableObjectValue<Set<T>>, value: T, action: UpdateSet) =
    updateSetPropertyImpl({ setProperty.get() }, { setProperty.set(it) }, value, action)

private fun <T> updateSetPropertyImpl(get: ()->Set<T>, set: (Set<T>)->Unit, value: T, action: UpdateSet) {
    val currentSetValue = get()
    when {
        action == UpdateSet.Set    && !currentSetValue.contains(value) -> set(currentSetValue + value)
        action == UpdateSet.Remove &&  currentSetValue.contains(value) -> set(currentSetValue - value)
    }
}
