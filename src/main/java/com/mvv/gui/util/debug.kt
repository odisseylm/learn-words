package com.mvv.gui.util

import java.lang.management.ManagementFactory


fun isDebuggerPresent(): Boolean {
    // Get ahold of the Java Runtime Environment (JRE) management interface
    val runtime = ManagementFactory.getRuntimeMXBean()

    // Get the command line arguments that we were originally passed in
    val args = runtime.inputArguments

    // Check if the Java Debug Wire Protocol (JDWP) agent is used.
    // One of the items might contain something like "-agentlib:jdwp=transport=dt_socket,address=9009,server=y,suspend=n"
    return args.find { it.contains("-agentlib:jdwp") } != null
}