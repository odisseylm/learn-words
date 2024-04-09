package com.mvv.gui.util

import org.apache.commons.lang3.time.StopWatch


fun startStopWatch(msg: String): StopWatch = StopWatch(msg).also { it.start() }
fun startStopWatch(): StopWatch = StopWatch().also { it.start() }

//fun StopWatch.Companion.started(): StopWatch = StopWatch().also { it.start() }
//fun StopWatch.Companion.started(msg: String): StopWatch = StopWatch(msg).also { it.start() }


val StopWatch.timeString: String get() {
    val timeMs = this.time
    val timeNs = this.nanoTime
    return when {
        timeMs > 10   -> "${timeMs}ms"
        timeMs > 0    -> "${ "%.2f".format(timeNs/1000_000.0) }ms"
        timeNs > 1000 -> "${timeNs/1000L}mcs"
        else          -> "${this.nanoTime}ns"
    }
}


fun StopWatch.logInfo(log: mu.KLogger, point: String) =
    log.info { "${this.message} => $point - elapsed ${this.timeString}" }

fun StopWatch.logInfo(log: mu.KLogger) =
    log.info { "${this.message} => elapsed ${this.timeString}" }


fun StopWatch.debugInfo(log: mu.KLogger, point: String) =
    log.debug { "${this.message} => $point - elapsed ${this.timeString}ns" }

fun StopWatch.debugInfo(log: mu.KLogger) =
    log.debug { "${this.message} => elapsed ${this.timeString}ns" }


fun <R> measureTime(taskName: String, log: mu.KLogger, task: ()->R): R {
    val stopWatch = startStopWatch(taskName)
    return try { task() }
           finally { stopWatch.logInfo(log) }
}
