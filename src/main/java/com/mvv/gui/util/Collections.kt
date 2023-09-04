package com.mvv.gui.util



fun <K, V> Map<K, V>.containsOneOfKeys(keys: Iterable<K>): Boolean =
    keys.any { this.containsKey(it) }

fun <K, V> Map<K, V>.containsOneOfKeys(vararg keys: K): Boolean =
    this.containsOneOfKeys(keys.asIterable())


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
