package com.mvv.gui

import javafx.beans.value.WritableObjectValue
import kotlin.reflect.KMutableProperty0



fun <K, V> Map<K, V>.containsOneOfKeys(keys: Iterable<K>): Boolean =
    keys.any { this.containsKey(it) }

fun <K, V> Map<K, V>.containsOneOfKeys(vararg keys: K): Boolean =
    this.containsOneOfKeys(keys.asIterable())


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


fun Array<String>.getOrEmpty(index: Int) = this.getOrElse(index) { "" }


@Suppress("UNCHECKED_CAST")
fun <K, V> Sequence<Pair<K,V?>>.filterNotNullPairValue(): Sequence<Pair<K,V>> =
    this.filter { it.second != null } as Sequence<Pair<K,V>>
