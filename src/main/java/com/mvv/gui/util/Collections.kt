package com.mvv.gui.util



fun <K, V> Map<K, V>.containsOneOfKeys(keys: Iterable<K>): Boolean =
    keys.any { this.containsKey(it) }

fun <K, V> Map<K, V>.containsOneOfKeys(vararg keys: K): Boolean =
    this.containsOneOfKeys(keys.asIterable())


fun Array<String>.getOrEmpty(index: Int) = this.getOrElse(index) { "" }


@Suppress("UNCHECKED_CAST")
fun <K, V> Sequence<Pair<K?,V?>>.filterNotNullPairValue(): Sequence<Pair<K,V>> =
    this.filter { it.first != null && it.second != null } as Sequence<Pair<K,V>>
