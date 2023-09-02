package com.mvv.gui.util

import java.util.*
import kotlin.collections.HashMap


private val log = mu.KotlinLogging.logger {}


interface Cache<K,V> {
    fun getNullable(key: K, getter: (K)->V?): V?
    fun get(key: K, getter: (K)->V): V
}


internal class MapCache<K, V: Any> (map: ()->MutableMap<K, Optional<V>>) : Cache<K,V> {
    private val cache: MutableMap<K, Optional<V>> = Collections.synchronizedMap(map())

    override fun getNullable(key: K, getter: (K)->V?): V? =
        cache.computeIfAbsent(key) { k: K ->
            val v: V? = getter(k)
            log.debug { " value for [$key] is taken from source " }
            Optional.ofNullable(v)
        }.orElse(null)

    override fun get(key: K, getter: (K)->V): V =
        cache.computeIfAbsent(key) { k: K ->
            val v: V = getter(k)
            log.debug { " value for [$key] is taken from source " }
            Optional.ofNullable(v)
        }.orElse(null)
}

fun <K,V:Any> hashMapCache(): Cache<K,V> = MapCache { HashMap() }
fun <K,V:Any> weakHashMapCache(): Cache<K,V> = MapCache { WeakHashMap() }
