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
inline val Int.isOdd: Boolean get() = (this % 2) == 1


fun <T> T.isOneOf(vararg values: T): Boolean = values.any { it == this }


@Suppress("NOTHING_TO_INLINE")
inline fun <T> Iterable<T>.skipFirst(): List<T> = this.drop(1)

@Suppress("NOTHING_TO_INLINE")
inline fun <T> List<T>.skipFirst(): List<T> =
    when (val count = this.size) {
        1    -> // Kind of optimisation that really works (for PrefixFinder).
                // I think because EmptyList is final and do not use any redirections but just have simple inlinable methods.
                emptyList()
        0    -> throw IllegalArgumentException("List is empty")
        else -> this.subList(1, count)
    }


inline fun doTry(block: ()->Unit): Boolean = try { block(); true } catch (_: Exception) { false }

// It works only if specify type explicitly like ` doTry<Int> { 123 } ` :-(
// T O D O: it should be rewritten in some way...
inline fun <T> doTry(block: ()->T): T? = try { block() } catch (_: Exception) { null }
