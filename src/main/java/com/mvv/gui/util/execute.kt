package com.mvv.gui.util

import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.PumpStreamHandler


fun MutableList<String>.addParam(param: String): MutableList<String> = this.also { this.add(param) }


fun MutableList<String>.addParam(param: String, paramValue: Any?): MutableList<String> {
    if (paramValue == null) return this

    this.add(param); this.add(paramValue.toString())
    return this
}


@Suppress("unused")
fun executeCommand(vararg args: String) = executeCommand(args.asIterable())

fun executeCommand(args: Iterable<String>) =
    DefaultExecutor()
        .also { it.streamHandler = PumpStreamHandler(System.out, System.err) }
        .execute(
            CommandLine(args.first())
                .also { cl -> args.asSequence().drop(1).forEach { cl.addArgument(it) } }
        )


fun commandLine(vararg args: String) =
    CommandLine(args.first()).also { cmd ->
        args.asSequence()
            .drop(1)
            .forEach { cmd.addArgument(it) }
    }
