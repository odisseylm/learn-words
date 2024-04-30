package com.mvv.gnome.shell.keyboard

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.mvv.gnome.gsettings.GnomeInputSource
import com.mvv.gui.javafx.findInputSource
import com.mvv.gui.util.*
import java.util.Locale


private val log = mu.KotlinLogging.logger {}


// See
//  https://askubuntu.com/questions/1039950/ubuntu-18-04-how-to-change-keyboard-layout-from-a-script
//  https://unix.stackexchange.com/questions/316998/how-to-change-keyboard-layout-in-gnome-3-from-command-line
//  https://discourse.gnome.org/t/how-to-set-gnome-keyboard-layout-programatically/9459
//
// If works properly, "org.gnome.desktop.input-sources.mru-sources" (most-recently-used sources)
// looks like what you want, and can be monitored by (like listening event)
//  `gsettings monitor org.gnome.desktop.input-sources mru-sources`
//
// gdbus call --session --dest org.gnome.Shell --object-path /org/gnome/Shell --method org.gnome.Shell.Eval 'imports.ui.status.keyboard.getInputSourceManager().inputSources'
// gdbus call --session --dest org.gnome.Shell --object-path /org/gnome/Shell --method org.gnome.Shell.Eval 'imports.ui.status.keyboard.getInputSourceManager().currentSource'
// gdbus call --session --dest org.gnome.Shell --object-path /org/gnome/Shell --method org.gnome.Shell.Eval 'imports.ui.status.keyboard.getInputSourceManager().currentSource.id'
// gdbus call --session --dest org.gnome.Shell --object-path /org/gnome/Shell --method org.gnome.Shell.Eval 'imports.ui.status.keyboard.getInputSourceManager().inputSources[0].activate()'


// {"0":{
//       "type":"xkb",
//       "id":"us",
//       "displayName":"English (US)",
//       "_shortName":"en₁",
//       "index":0,
//       "properties":null,
//       "xkbId":"us",
//       "_signalConnections":[{"id":1,"name":"activate","disconnected":false},{"id":2,"name":"changed","disconnected":false}],
//       "_nextConnectionId":3},
//  "1":{
//       "type":"xkb",
//       "id":"gb",
//       "displayName":"English (UK)",
//       "_shortName":"en₂",
//       "index":1,
//       "properties":null,
//       "xkbId":"gb",
//       "_signalConnections":[{"id":1,"name":"activate","disconnected":false},{"id":2,"name":"changed","disconnected":false}],
//       "_nextConnectionId":3},
//  "2":{
//       "type":"xkb",
//       "id":"au",
//       "displayName":"English (Australian)",
//       "_shortName":"en₃",
//       "index":2,
//       "properties":null,
//       "xkbId":"au",
//       "_signalConnections":[{"id":1,"name":"activate","disconnected":false},{"id":2,"name":"changed","disconnected":false}],
//       "_nextConnectionId":3},
//  "3":{
//       "type":"xkb",
//       "id":"ru",
//       "displayName":"Russian",
//       "_shortName":"ru₁",
//       "index":3,
//       "properties":null,
//       "xkbId":"ru",
//       "_signalConnections":[{"id":1,"name":"activate","disconnected":false},{"id":2,"name":"changed","disconnected":false}],
//       "_nextConnectionId":3},
//  "4":{
//       "type":"xkb",
//       "id":"ge+ru",
//       "displayName":"Russian (Georgia)",
//       "_shortName":"ru₂",
//       "index":4,
//       "properties":null,
//       "xkbId":"ge+ru",
//       "_signalConnections":[{"id":1,"name":"activate","disconnected":false},{"id":2,"name":"changed","disconnected":false}],
//       "_nextConnectionId":3},
//  "5":{
//       "type":"xkb",
//       "id":"ua",
//       "displayName":"Ukrainian",
//       "_shortName":"uk",
//       "index":5,
//       "properties":null,
//       "xkbId":"ua",
//       "_signalConnections":[{"id":1,"name":"activate","disconnected":false},{"id":2,"name":"changed","disconnected":false}],
//       "_nextConnectionId":3}
// }



data class ShellInputSource (
    override val type: String,        // "xkb"
    override val id: String,          // "us"
    val index: Int,                   // 0, 1, 2, 3
    val xkbId: String,                // "us"
    override val displayName: String, // "English (US)"
    private val _shortName: String?,  // "en"
) : GnomeInputSource {
    override val shortName: String get() = _shortName ?: id
}


val isShellEvalAvailable: Boolean by lazy { isShellEvalAvailableImpl()
    .also { log.info("org.gnome.Shell.Eval is${it.asAdverb} available.") } }

internal fun isShellEvalAvailableImpl() :Boolean =
    try {
        val response = executeGnomeShellEval("1+1").trim()
        response == "(true, '2')"
    } catch (ex: Exception) { false }


internal fun getInputSources(): Map<String, ShellInputSource> {

    val response = executeGnomeShellEval("imports.ui.status.keyboard.getInputSourceManager().inputSources")

    val inputSourceMap = gnomeJsonMapper.readValue<LinkedHashMap<String, ShellInputSource>>(
        response.extractGnomeShellEvalResponseBody())

    return inputSourceMap
}


internal fun currentInputSource(): ShellInputSource {
    val response = executeGnomeShellEval("imports.ui.status.keyboard.getInputSourceManager().currentSource")

    return gnomeJsonMapper.readValue<ShellInputSource>(
        response.extractGnomeShellEvalResponseBody())
}


fun selectInputSource(locale: Locale) {

    log.debug { "selectGnomeInputSource($locale)" }

    val inputSources = getInputSources()

    val targetInputSource = inputSources.values.findInputSource(locale)
    checkNotNull(targetInputSource) { "InputSource/KeyboardLayout for $locale is not found." }

    selectInputSource(targetInputSource)
}

fun selectInputSource(inputSource: ShellInputSource) {

    log.debug { "selectGnomeInputSource(${inputSource.id} / ${inputSource.displayName})" }

    val currentInputSource = currentInputSource()

    if (currentInputSource.id == inputSource.id) {
        log.debug { "Gnome keyboard [${inputSource.id} / ${inputSource.displayName}] is already selected." }
        return
    }

    val expr = "imports.ui.status.keyboard.getInputSourceManager().inputSources[${inputSource.index}].activate()"
    //val expr = "imports.ui.status.keyboard.getInputSourceManager().inputSources[${targetInputSourceEntry.key}].activate()"
    executeGnomeShellEval(expr)

    // result/output
    // (true, '')
}

private fun executeGnomeShellEval(expression: String): String {
    val response = executeCommandWithOutput(
        "gdbus",
        "call",
        "--session",
        "--dest", "org.gnome.Shell",
        "--object-path", "/org/gnome/Shell",
        "--method", "org.gnome.Shell.Eval",
        expression).trim()

    validateGnomeShellEvalResponse(response, expression)
    return response
}


private fun String.extractGnomeShellEvalResponseBody(): String =
    this.trim().removeOneOfPrefixes("(true, '", "(true,'").removeSuffix("')")


private fun validateGnomeShellEvalResponse(response: CharSequence, cmd: String) {
    if (response.startsWith("(false"))
        throw IllegalStateException("Gnome shell [$cmd] failed.")

    check(response.startsWithOneOf("(true, '", "(true,'") && response.endsWith("')")) {
        "Gnome shell [$cmd] returned unexpected response." }
}

private val gnomeJsonMapper =
    jsonMapper { configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) }
    .registerKotlinModule()
