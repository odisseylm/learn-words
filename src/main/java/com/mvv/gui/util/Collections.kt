package com.mvv.gui.util

import java.util.EnumSet


fun <K, V> Map<K, V>.containsOneOfKeys(keys: Iterable<K>): Boolean =
    keys.any { this.containsKey(it) }

fun <K, V> Map<K, V>.containsOneOfKeys(vararg keys: K): Boolean =
    this.containsOneOfKeys(keys.asIterable())


fun <V> Collection<V>.containsOneOf(values: Iterable<V>): Boolean =
    values.any { this.contains(it) }

fun <V> Collection<V>.containsOneOf(vararg values: V): Boolean =
    values.any { this.contains(it) }


/**
 * Use it carefully with empty keys!
 * It returns false for empty keys, but it may be a bit of arguable behavior (for you).
 */
fun <K, V> Map<K, V>.containsAllKeys(keys: Iterable<K>): Boolean =
    keys.iterator().hasNext() && keys.all { this.containsKey(it) }

/**
 * Use it carefully with empty keys!
 * It returns false for empty keys, but it may be a bit of arguable behavior (for you).
 */
fun <K, V> Map<K, V>.containsAllKeys(vararg keys: K): Boolean =
    this.containsAllKeys(keys.asIterable())


fun Array<String>.getOrEmpty(index: Int) = this.getOrElse(index) { "" }


@Suppress("UNCHECKED_CAST")
fun <K, V> Sequence<Pair<K?,V?>>.filterNotNullPairValue(): Sequence<Pair<K,V>> =
    this.filter { it.first != null && it.second != null } as Sequence<Pair<K,V>>


fun <T> Iterable<T>.doIfNotEmpty(action: (Iterable<T>)->Unit) {
    if (this.iterator().hasNext()) action(this)
}


fun <T, E: Exception> Sequence<T>.firstOrThrow(ex: ()->E): T =
    this.firstOrNull() ?: throw ex()


fun <T> Sequence<T>.firstOr(other: ()->T): T =
    this.firstOrNull() ?: other()

fun <T> Iterable<T>.firstOr(other: ()->T): T =
    this.firstOrNull() ?: other()


inline fun <reified T: Enum<T>> Iterable<T>.toEnumSet(): EnumSet<T> =
    this.toCollection(EnumSet.noneOf(T::class.java))


@Suppress("NOTHING_TO_INLINE", "unused")
inline fun Int.ifIndexNotFound(altValue: Int): Int =
    if (this == -1) altValue else this

inline fun Int.ifIndexNotFound(altValue: () -> Int): Int =
    if (this == -1) altValue() else this

@Suppress("NOTHING_TO_INLINE")
inline fun <C: CharSequence> Iterable<C>.filterNotEmpty(): List<C> = this.filter { it.isNotEmpty() }
@Suppress("NOTHING_TO_INLINE")
inline fun <C: CharSequence> Iterable<C>.filterNotBlank(): List<C> = this.filter { it.isNotBlank() }
