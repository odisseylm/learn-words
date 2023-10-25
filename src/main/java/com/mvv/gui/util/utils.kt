package com.mvv.gui.util

import javafx.application.Platform
import java.util.TimerTask


private val log = mu.KotlinLogging.logger {}


inline fun Boolean.doIfTrue(action: ()->Unit) {
    if (this) action()
}
// just alias
inline fun Boolean.doIfSuccess(action: ()->Unit) = this.doIfTrue(action)


//fun <T> List<T>.addIf(predicate: Boolean, additional: ()->Iterable<T>): List<T> =
//    if (predicate) this + additional() else this


fun timerTask(action: ()->Unit): TimerTask =
    object : TimerTask() {
        override fun run() {
            // we need to catch, otherwise java.util.Timer will stop (strange behaviour)
            try { action() }
            catch (ex: Throwable) { log.error(ex) { "Error in timer: $ex" } }
        }
    }

@Suppress("unused")
fun uiTimerTask(action: ()->Unit): TimerTask = timerTask { Platform.runLater(action) }


inline val Int.isEven: Boolean get() = (this % 2) == 0


fun <T> T.isOneOf(vararg values: T): Boolean = values.any { it == this }


@Suppress("NOTHING_TO_INLINE")
inline fun <T> Iterable<T>.skipFirst(): List<T> = this.drop(1)


inline fun doTry(block: ()->Unit): Boolean = try { block(); true } catch (_: Exception) { false }
inline fun <T> doTry(block: ()->T): T? = try { block() } catch (_: Exception) { null }
