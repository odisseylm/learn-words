package com.mvv.gui.util

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test


class UtilsTest {

    @Test
    fun test_doIfTrue() {
        run {
            var v = 2
            true.doIfTrue { v += 3 }

            assertThat(v).isEqualTo(5)
        }
        run {
            var v = 2
            false.doIfTrue { v += 3 }

            assertThat(v).isEqualTo(2)
        }
    }

    @Test
    fun test_doIfSuccess() {
        run {
            var v = 2
            true.doIfSuccess { v += 3 }

            assertThat(v).isEqualTo(5)
        }
        run {
            var v = 2
            false.doIfSuccess { v += 3 }

            assertThat(v).isEqualTo(2)
        }
    }

    @Test
    fun test_timerTask() {
        var v = 2
        val t = timerTask { v += 3 }
        t.run()

        assertThat(v).isEqualTo(5)
    }

    @Test
    fun test_timerTask_exceptionIsCaughtInside() {
        val t = timerTask { throw IllegalStateException("Test error") }

        assertThatCode { t.run() }.doesNotThrowAnyException()
    }

    @Test
    fun test_uiTimerTask() {
        var v = 2
        val t = uiTimerTask { v += 3 }
        t.run()

        // task code should be called inside JavaFX EDT, but since it is not accessible there will be error in log
        //  java.lang.IllegalStateException: Toolkit not initialized
        //	  at com.sun.javafx.application.PlatformImpl.runLater(PlatformImpl.java:436)
        //    at ...
        assertThat(v).isEqualTo(2)
    }

    @Test
    fun test_isEven() {
        assertThat(0.isEven).isTrue()
        assertThat(1.isEven).isFalse()
        assertThat(2.isEven).isTrue()
        assertThat(3.isEven).isFalse()
        assertThat(4.isEven).isTrue()
        assertThat(5.isEven).isFalse()
    }

    @Test
    fun test_isOdd() {
        assertThat(0.isOdd).isFalse()
        assertThat(1.isOdd).isTrue()
        assertThat(2.isOdd).isFalse()
        assertThat(3.isOdd).isTrue()
        assertThat(4.isOdd).isFalse()
        assertThat(5.isOdd).isTrue()
    }

    @Test
    fun test_isOneOf() {
        assertThat(1.isOneOf(2)).isFalse()
        assertThat(1.isOneOf(1)).isTrue()

        assertThat(1.isOneOf(2, 3)).isFalse()

        assertThat(2.isOneOf(2, 3)).isTrue()
        assertThat(3.isOneOf(2, 3)).isTrue()

        assertThat(4.isOneOf(2, 3)).isFalse()
    }

    @Test
    fun test_skipFirst() {
        assertThat(listOf(1, 2, 3).skipFirst()).containsExactly(2, 3)
        assertThat(listOf(1, 2).skipFirst()).containsExactly(2)

        assertThat(listOf(1).skipFirst()).isEmpty()
        assertThat(listOf(1).skipFirst()).isSameAs(emptyList<Int>()) // optimized impl

        // ??? should be there error ???
        // this function is over-optimized and there is no special generation of good error message
        // and really there is no such use-case
        assertThatCode { listOf<Int>().skipFirst() }.hasMessage("List is empty")
    }

    @Test
    fun test_skipFirst_forIterable() {
        assertThat(listOf(1, 2, 3).asIterable().skipFirst()).containsExactly(2, 3)
        assertThat(listOf(1, 2).asIterable().skipFirst()).containsExactly(2)

        assertThat(listOf(1).asIterable().skipFirst()).isEmpty()
        assertThat(listOf(1).asIterable().skipFirst()).isSameAs(emptyList<Int>()) // optimized impl

        // ??? should be there error ???
        assertThat(listOf<Int>().asIterable().skipFirst()).isEmpty()
        assertThat(emptyList<Int>().asIterable().skipFirst()).isEmpty()
    }

    @Test
    fun test_doTry() {
        var v = 2
        doTry { v += 3 }

        assertThat(v).isEqualTo(5)

        fun f1(): Int = v + 5

        assertThat( doTry<Int> { f1() }  ).isEqualTo(10)

        //val i1: Int? = doTry { 12 }
        //assertThat(i1).isEqualTo(12)
        //
        //assertThat( doTry { 12 }  ).isEqualTo(12)
    }

    @Test
    fun test_doTry_inCaseOfError_ErrorShouldBeOnlyLogged() {
        assertThatCode { doTry { throw IllegalStateException("Test error") } }.doesNotThrowAnyException()

        fun f1(): Int = throw IllegalStateException("Test error")

        assertThatCode { doTry<Int> { f1() } }.doesNotThrowAnyException()
    }
}
