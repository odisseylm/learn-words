package com.mvv.gui.javafx

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


class EditTest {

    @Test
    @DisplayName("endExclusive")
    fun test_endExclusive() {

        val range = IntRange(2, 5)
        assertThat(range.first).isEqualTo(2)
        @Suppress("ReplaceRangeStartEndInclusiveWithFirstLast")
        assertThat(range.start).isEqualTo(2)
        assertThat(range.step).isEqualTo(1)
        assertThat(range.last).isEqualTo(5)
        assertThat(range.endExclusive).isEqualTo(6)

        assertThat(range.contains(0)).isFalse()
        assertThat(range.contains(1)).isFalse()
        assertThat(range.contains(2)).isTrue()
        assertThat(range.contains(3)).isTrue()
        assertThat(range.contains(4)).isTrue()
        assertThat(range.contains(5)).isTrue()
        assertThat(range.contains(6)).isFalse()
        assertThat(range.contains(7)).isFalse()
    }

    @Test
    @DisplayName("endExclusive_2")
    fun test_endExclusive_2() {

        val range = 2..5
        assertThat(range.step).isEqualTo(1)

        assertThat(range.first).isEqualTo(2)
        @Suppress("ReplaceRangeStartEndInclusiveWithFirstLast")
        assertThat(range.start).isEqualTo(2)
        assertThat(range.step).isEqualTo(1)
        assertThat(range.last).isEqualTo(5)
        assertThat(range.endExclusive).isEqualTo(6)

        assertThat(range.contains(0)).isFalse()
        assertThat(range.contains(1)).isFalse()
        assertThat(range.contains(2)).isTrue()
        assertThat(range.contains(3)).isTrue()
        assertThat(range.contains(4)).isTrue()
        assertThat(range.contains(5)).isTrue()
        assertThat(range.contains(6)).isFalse()
        assertThat(range.contains(7)).isFalse()
    }

    /*
    @Test
    @DisplayName("endExclusive_reversed")
    fun test_endExclusive_reversed() {

        val range: IntProgression = 5 downTo 2
        assertThat(range.step).isEqualTo(1)

        assertThat(range.first).isEqualTo(2)
        //@Suppress("ReplaceRangeStartEndInclusiveWithFirstLast")
        //assertThat(range.start).isEqualTo(2)
        assertThat(range.step).isEqualTo(1)
        assertThat(range.last).isEqualTo(5)
        assertThat(range.endExclusive).isEqualTo(6)

        assertThat(range.contains(0)).isFalse()
        assertThat(range.contains(1)).isFalse()
        assertThat(range.contains(2)).isTrue()
        assertThat(range.contains(3)).isTrue()
        assertThat(range.contains(4)).isTrue()
        assertThat(range.contains(5)).isTrue()
        assertThat(range.contains(6)).isFalse()
        assertThat(range.contains(7)).isFalse()
    }
    */
}
