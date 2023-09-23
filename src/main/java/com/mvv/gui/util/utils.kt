package com.mvv.gui.util

import java.util.TimerTask


private val log = mu.KotlinLogging.logger {}


fun Boolean.doIfTrue(action: ()->Unit) {
    if (this) action()
}


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


inline val Int.isEven: Boolean get() = (this % 2) == 0