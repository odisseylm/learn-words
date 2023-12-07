package com.mvv.gui.cardeditor

import com.mvv.gui.javafx.UpdateSet
import com.mvv.gui.javafx.updateSetProperty
import com.mvv.gui.util.*
import javafx.beans.value.WritableObjectValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


class CollectionsTest {

    @Test
    @DisplayName("containsOneOfKeys")
    fun test_containsOneOfKeys() {

        val map = mapOf(1 to 1, 2 to 2, 3 to 3)

        assertThat(map.containsOneOfKeys()).isFalse()
        assertThat(map.containsOneOfKeys(1)).isTrue()
        assertThat(map.containsOneOfKeys(1, 4)).isTrue()
        assertThat(map.containsOneOfKeys(4, 2)).isTrue()
        assertThat(map.containsOneOfKeys(1, 2)).isTrue()
        assertThat(map.containsOneOfKeys(2, 1)).isTrue()

        assertThat(map.containsOneOfKeys(4)).isFalse()
        assertThat(map.containsOneOfKeys(4, 5)).isFalse()

        assertThat(map.containsOneOfKeys(listOf(1, 2))).isTrue()
    }

    @Test
    @DisplayName("containsOneOf")
    fun test_containsOneOf() {

        val map = setOf(1, 2, 3)

        assertThat(map.containsOneOf()).isFalse()
        assertThat(map.containsOneOf(1)).isTrue()
        assertThat(map.containsOneOf(1, 4)).isTrue()
        assertThat(map.containsOneOf(4, 2)).isTrue()
        assertThat(map.containsOneOf(1, 2)).isTrue()
        assertThat(map.containsOneOf(2, 1)).isTrue()

        assertThat(map.containsOneOf(4)).isFalse()
        assertThat(map.containsOneOf(4, 5)).isFalse()

        assertThat(map.containsOneOf(listOf())).isFalse()
        assertThat(map.containsOneOf(listOf(1, 2))).isTrue()
    }

    @Test
    @DisplayName("updateSetProperty, set action, for JavaFx prop")
    fun test_updateSetProperty_setAction() {
        run {
            val p = TestFxProp(setOf(1))
            updateSetProperty(p, 2, UpdateSet.Set)

            assertThat(p.value).containsExactly(1, 2)
            assertThat(p.updated).isTrue()
        }
        run {
            val p = TestFxProp(setOf(1))
            updateSetProperty(p, 1, UpdateSet.Set)

            assertThat(p.value).containsExactly(1)
            assertThat(p.updated).isFalse()
        }
    }

    @Test
    @DisplayName("updateSetProperty, remove action, for JavaFx prop")
    fun test_updateSetProperty_removeAction() {
        run {
            val p = TestFxProp(setOf(1))
            updateSetProperty(p, 2, UpdateSet.Remove)

            assertThat(p.value).containsExactly(1)
            assertThat(p.updated).isFalse()
        }
        run {
            val p = TestFxProp(setOf(1, 2))
            updateSetProperty(p, 1, UpdateSet.Remove)

            assertThat(p.value).containsExactly(2)
            assertThat(p.updated).isTrue()
        }
    }

    @Test
    @DisplayName("updateSetProperty, set action, for kotlin prop")
    fun test_updateSetProperty_kotlin_setAction() {
        run {
            val p = TestKotlinClass(setOf(1))
            updateSetProperty(p::prop1, 2, UpdateSet.Set)

            assertThat(p.prop1).containsExactly(1, 2)
            //assertThat(p.updated).isTrue()
        }
        run {
            val p = TestKotlinClass(setOf(1))
            updateSetProperty(p::prop1, 1, UpdateSet.Set)

            assertThat(p.prop1).containsExactly(1)
            //assertThat(p.updated).isFalse()
        }
    }

    @Test
    @DisplayName("updateSetProperty, remove action, for kotlin prop")
    fun test_updateSetProperty_kotlin_removeAction() {
        run {
            val p = TestKotlinClass(setOf(1))
            updateSetProperty(p::prop1, 2, UpdateSet.Remove)

            assertThat(p.prop1).containsExactly(1)
            //assertThat(p.updated).isFalse()
        }
        run {
            val p = TestKotlinClass(setOf(1, 2))
            updateSetProperty(p::prop1, 1, UpdateSet.Remove)

            assertThat(p.prop1).containsExactly(2)
            //assertThat(p.updated).isTrue()
        }
    }


    @Test
    @DisplayName("filterNotNullPairValue")
    fun test_filterNotNullPairValue() {

        val items: List<Pair<String?, String?>> = listOf(
            Pair("0", "0"),
            Pair(null, null),
            Pair(null, "1"),
            Pair("2", null),
            Pair("3", "4"),
        )

        val filtered: List<Pair<String, String>> = items
            .asSequence()
            .filterNotNullPairValue()
            .toList()

        assertThat(filtered).containsExactly(
            Pair("0", "0"),
            Pair("3", "4"),
        )
    }


    @Test
    @DisplayName("containsAllKeys")
    fun test_containsAllKeys() {
        val map = mapOf("1" to 1, "2" to 2, "3" to 3)

        assertThat(map.containsAllKeys("1")).isTrue()
        assertThat(map.containsAllKeys("1", "2")).isTrue()
        assertThat(map.containsAllKeys("1", "2", "3")).isTrue()

        assertThat(map.containsAllKeys("1", "2", "3", "4")).isFalse()
        assertThat(map.containsAllKeys("2", "3", "4")).isFalse()
    }


    @Test
    @DisplayName("containsAllKeys for empty keys")
    fun test_containsAllKeys_forEmpty() {
        val map = mapOf("1" to 1, "2" to 2, "3" to 3)

        assertThat(map.containsAllKeys()).isFalse()
        assertThat(map.containsAllKeys(emptyList())).isFalse()
    }


    @Test
    @DisplayName("firstOr")
    fun test_firstOr() {
        assertThat(listOf(1, 2, 3).firstOr { 11 }).isEqualTo(1)
        assertThat(emptyList<Int>().firstOr { 11 }).isEqualTo(11)

        assertThat(sequenceOf(1, 2, 3).firstOr { 11 }).isEqualTo(1)
        assertThat(emptySequence<Int>().firstOr { 11 }).isEqualTo(11)
    }
}


private class TestKotlinClass<T> (var prop1: Set<T>)


private class TestFxProp<T>(initilaValue: T) : WritableObjectValue<T> {
    private var value: T = initilaValue
    var updated = false
        private set

    override fun getValue(): T = value
    override fun get(): T = value

    override fun set(value: T) = setValue(value)

    override fun setValue(value: T)  {
        this.value = value
        this.updated = true
    }
}
