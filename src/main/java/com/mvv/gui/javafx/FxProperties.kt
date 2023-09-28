package com.mvv.gui.javafx

import com.sun.javafx.binding.ExpressionHelper
import javafx.beans.InvalidationListener
import javafx.beans.property.ReadOnlyProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.beans.value.WritableObjectValue
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KMutableProperty0


@Deprecated("Use method ObservableValue.map.")
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
fun <T> updateSetProperty(setProperty: KMutableProperty0<Set<T>>, value: T, action: UpdateSet): Boolean =
    updateSetPropertyImpl({ setProperty.get() }, { setProperty.set(it) }, value, action)

fun <T> updateSetProperty(setProperty: WritableObjectValue<Set<T>>, value: T, action: UpdateSet): Boolean =
    updateSetPropertyImpl({ setProperty.get() }, { setProperty.set(it) }, value, action)

private fun <T> updateSetPropertyImpl(get: ()->Set<T>, set: (Set<T>)->Unit, value: T, action: UpdateSet): Boolean {
    val currentSetValue = get()
    return when {
        action == UpdateSet.Set    && !currentSetValue.contains(value) -> { set(currentSetValue + value); true }
        action == UpdateSet.Remove &&  currentSetValue.contains(value) -> { set(currentSetValue - value); true }
        else -> false
    }
}


//private val emptyObservables: Array<ObservableValue<*>> = arrayOf<ObservableValue<*>>()
private val emptyObservable: ObservableValue<*> = object : ObservableValue<Any> {
    override fun addListener(listener: ChangeListener<in Any>)     { }
    override fun addListener(listener: InvalidationListener)       { }
    override fun removeListener(listener: InvalidationListener)    { }
    override fun removeListener(listener: ChangeListener<in Any>?) { }
    override fun getValue(): Any = ""
}

fun <S,T> ObservableValue<S>.mapCached(map: (S)->T): ObservableValue<T> = this.mapCached(map, emptyObservable)

fun <S,T> ObservableValue<S>.mapCached(map: (S)->T, otherDependent: ObservableValue<*>, vararg otherDependents: ObservableValue<*>): ObservableValue<T> {

    var ref: T? = null

    fun addListeners(obs: ObservableValue<*>) {
        obs.addListener(InvalidationListener { ref = null })
        obs.addListener { _, _, _ -> ref = map(this.value) }
    }

    addListeners(this)
    addListeners(otherDependent)
    otherDependents.forEach { addListeners(it) }

    val cachedMapper: (S)->T = {
        val currentRefValue = ref
        if (currentRefValue != null) currentRefValue
        else {
            val newValue = map(this.value)
            ref = newValue
            newValue
        }
    }

    return this.map(cachedMapper)
}


fun <S,T> ObservableValue<S>.mapCachedAtomic(map: (S)->T): ObservableValue<T> = this.mapCachedAtomic(map, emptyObservable)

fun <S,T> ObservableValue<S>.mapCachedAtomic(map: (S)->T, otherDependent: ObservableValue<*>, vararg otherDependents: ObservableValue<*>): ObservableValue<T> {

    val ref = AtomicReference<T?>()

    fun addListeners(obs: ObservableValue<*>) {
        obs.addListener(InvalidationListener { ref.set(null) })
        obs.addListener { _, _, _ -> ref.set(map(this.value)) }
    }

    addListeners(this)
    addListeners(otherDependent)
    otherDependents.forEach { addListeners(it) }

    val cachedMapper: (S)->T = {
        ref.get() ?: ref.updateAndGet { map(this.value) }!!
    }

    return this.map(cachedMapper)
}
