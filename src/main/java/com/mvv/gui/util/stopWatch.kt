package com.mvv.gui.util

import org.apache.commons.lang3.time.StopWatch


fun startStopWatch(msg: String): StopWatch = StopWatch(msg).also { it.start() }

fun StopWatch.logInfo(log: mu.KLogger, point: String) {
    log.info { "${this.message} => $point - elapsed ${this.time}ms" }
}

fun StopWatch.logInfo(log: mu.KLogger) {
    log.info { "${this.message} => elapsed ${this.time}ms" }
}

