package com.mvv.gui.util

import org.apache.commons.lang3.time.StopWatch
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.junit.platform.commons.util.ReflectionUtils
import java.lang.reflect.Field
import java.time.Duration
import kotlin.reflect.KClass


class StopWatchTest {
    private val log = mu.KotlinLogging.logger {}

    //@Test
    @DisplayName("timeString")
    @RepeatedTest(2)
    fun test_timeString() {

        val a = SoftAssertions()

        a.assertThat(
            createStopWatch(Duration.ofSeconds(2)).timeString)
            .isEqualTo("2000ms")

        a.assertThat(
            createStopWatch(Duration.ofMillis(500)).timeString)
            .isEqualTo("500ms")

        a.assertThat(
            createStopWatch(Duration.ofMillis(10)).timeString)
            .isEqualTo("10.00ms")

        a.assertThat(
            createStopWatch(Duration.ofMillis(9)).timeString)
            .isEqualTo("9.00ms")
        a.assertThat(
            createStopWatch(Duration.ofMillis(5)).timeString)
            .isEqualTo("5.00ms")

        a.assertThat(
            createStopWatch(Duration.ofMillis(2)).timeString)
            .isEqualTo("2.00ms")
        a.assertThat(
            createStopWatch(Duration.ofNanos(1_456_852L)).timeString)
            .isEqualTo("1.46ms")
        a.assertThat(
            createStopWatch(Duration.ofNanos(465_852L)).timeString)
            .isEqualTo("465mcs")
        a.assertThat(
            createStopWatch(Duration.ofNanos(65_852L)).timeString)
            .isEqualTo("65mcs")

        a.assertThat(
            createStopWatch(Duration.ofNanos(852L)).timeString)
            .isEqualTo("852ns")

        a.assertThat(
            createStopWatch(Duration.ofNanos(52L)).timeString)
            .isEqualTo("52ns")

        a.assertAll()
    }


    @Test
    fun justUseCode() {
        val stopWatch = startStopWatch()
        stopWatch.logInfo(log)
        stopWatch.logInfo(log, "message1")

        stopWatch.debugInfo(log)
        stopWatch.debugInfo(log, "message1")
    }


    private fun createStopWatch(duration: Duration): StopWatch {
        val sw = startStopWatch()
        sw.stop()

        val startTimeMillisF: Field = StopWatch::class.findField("startTimeMillis")
        val startTimeNanosF:  Field = StopWatch::class.findField("startTimeNanos")

        val stopTimeMillisF: Field = StopWatch::class.findField("stopTimeMillis")
        val stopTimeNanosF:  Field = StopWatch::class.findField("stopTimeNanos")

        stopTimeMillisF.set(sw, (startTimeMillisF.get(sw)) as Long + duration.toMillis())
        stopTimeNanosF .set(sw, (startTimeNanosF .get(sw)) as Long + duration.toNanos())

        return sw
    }
}

private fun KClass<*>.findField(name: String): Field = this.java.findField(name)

private fun Class<*>.findField(name: String): Field {
    return ReflectionUtils.findFields(this, { it.name == name }, ReflectionUtils.HierarchyTraversalMode.TOP_DOWN)
        .firstOrNull()?.also { it.trySetAccessible() } ?: throw IllegalArgumentException("Field ${this.name}.${name} is not found.")
}
