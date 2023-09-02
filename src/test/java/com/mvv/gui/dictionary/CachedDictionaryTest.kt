package com.mvv.gui.dictionary

import com.mvv.gui.util.hashMapCache
import com.mvv.gui.util.weakHashMapCache
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


class CachedDictionaryTest {

    @Test
    @DisplayName("hashMapCache")
    fun test_hashMapCache() {
        val cache = hashMapCache<Int, String>()

        var count = 0
        val getter: (Int) -> String = { count++; "1" }

        assertThat(cache.get(1, getter)).isEqualTo("1")
        assertThat(cache.get(1, getter)).isEqualTo("1")
        assertThat(cache.get(1, getter)).isEqualTo("1")

        assertThat(count).isEqualTo(1)

        getter(1)
        assertThat(count).isEqualTo(2)
    }

    @Test
    @DisplayName("weakHashMapCache")
    fun test_weakHashMapCache() {
        val cache = weakHashMapCache<Int, String>()

        var count = 0
        val getter: (Int) -> String = { count++; "1" }

        assertThat(cache.get(1, getter)).isEqualTo("1")
        assertThat(cache.get(1, getter)).isEqualTo("1")
        assertThat(cache.get(1, getter)).isEqualTo("1")

        assertThat(count).isEqualTo(1)

        getter(1)
        assertThat(count).isEqualTo(2)
    }

    @Test
    @DisplayName("weakHashMapCache with nullable")
    fun test_weakHashMapCache_withNullable() {
        val cache = weakHashMapCache<Int, String>()

        var count = 0
        val getter: (Int) -> String? = { count++; "1" }

        assertThat(cache.getNullable(1, getter)).isEqualTo("1")
        assertThat(cache.getNullable(1, getter)).isEqualTo("1")
        assertThat(cache.getNullable(1, getter)).isEqualTo("1")

        assertThat(count).isEqualTo(1)

        getter(1)
        assertThat(count).isEqualTo(2)
    }

}
