package com.mvv.gui.util

import java.util.TimerTask


private val log = mu.KotlinLogging.logger {}


fun Boolean.doIfTrue(action: ()->Unit) {
    if (this) action()
}


fun timerTask(action: ()->Unit): TimerTask =
    object : TimerTask() {
        override fun run() {
            // we need to catch, otherwise java.util.Timer will stop (strange behaviour)
            try { action() }
            catch (ex: Throwable) { log.error("Error in timer: $ex", ex) }
        }
    }
