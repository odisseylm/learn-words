package com.mvv.gui.util


fun <T> logTime(log: mu.KLogger, actionName: String, action: ()->T): T {
    val started = System.currentTimeMillis()
    return try { action() }
    finally { log.info("{} took {}ms", actionName, System.currentTimeMillis() - started) }
}
