package com.mvv.gui

import javafx.beans.value.WritableObjectValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


class CollectionsTest {

    @Test
    @DisplayName("containsOneOfKeys")
    fun test_containsOneOfKeys() {

        val map = mapOf(1 to 1, 2 to 2, 3 to 3)

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