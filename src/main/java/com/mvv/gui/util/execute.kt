package com.mvv.gui.util

import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.PumpStreamHandler
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets.UTF_8


fun MutableList<String>.addParam(param: String): MutableList<String> = this.also { this.add(param) }


fun MutableList<String>.addParam(param: String, paramValue: Any?): MutableList<String> {
    if (paramValue == null) return this

    this.add(param); this.add(paramValue.toString())
    return this
}


private fun Iterable<String>.toCommandLine(): CommandLine =
    CommandLine(this.first())
        .also { cl -> this.asSequence().drop(1).forEach { cl.addArgument(it) } }

fun commandLine(vararg args: String) = args.asIterable().toCommandLine()


@Suppress("unused")
fun executeCommand(vararg args: String) = executeCommand(args.asIterable())

fun executeCommand(args: Iterable<String>, successExitValues: Iterable<Int> = emptyList()) =
    DefaultExecutor()
        .also { it.streamHandler = PumpStreamHandler(System.out, System.err) }
        .also { if (successExitValues.iterator().hasNext()) it.setExitValues(successExitValues.toList().toIntArray()) }
        .execute(args.toCommandLine())


fun executeCommandWithOutput(vararg args: String): String = executeCommandWithOutput(args.asIterable())

fun executeCommandWithOutput(args: Iterable<String>): String {

    val bout = ByteArrayOutputStream()

    DefaultExecutor()
        .also { it.streamHandler = PumpStreamHandler(bout) }
        .execute(args.toCommandLine())

    return bout.toString(UTF_8)
}
