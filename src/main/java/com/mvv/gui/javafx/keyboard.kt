package com.mvv.gui.javafx

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.mvv.gui.util.*
import java.util.Locale


private val log = mu.KotlinLogging.logger {}


internal data class InputSource (
    val type: String,        // "xkb"
    val id: String,          // "us"
    val displayName: String, // "English (US)"
    private val _shortName: String?, // "en"
    val index: Int,          // 0, 1, 2, 3
    val xkbId: String,       // "us"
) {
    val shortName: String? get() = _shortName
}


internal fun InputSource.languageIds() = listOf(
        displayName.substringBefore("(").trim(),
        shortName,
    )
    .filterNotBlank()
    .map { it.lowercase() }
    .toSet()


internal fun InputSource.countryIds() = listOf(
        displayName.substringAfter("(", "").removeSuffix(")").trim(),
        id,
        xkbId,
    )
    .filterNotBlank()
    .map { it.lowercase() }
    .toSet()


internal fun getGnomeInputSources(): Map<String, InputSource> {

    val response = executeGnomeShellCmd("imports.ui.status.keyboard.getInputSourceManager().inputSources")

    // (true, '
    // {
    //  "0":{
    //       "type":"xkb",
    //       "id":"us",
    //       "displayName":"English (US)",
    //       "_shortName":"en",
    //       "index":0,
    //       "properties":null,
    //       "xkbId":"us",
    //       "_signalConnections":[{"id":1,"name":"activate","disconnected":false},{"id":2,"name":"changed","disconnected":false}],"_nextConnectionId":3},
    //  "1":{
    //       "type":"xkb",
    //       "id":"ua",
    //       "displayName":"Ukrainian",
    //       "_shortName":"uk",
    //       "index":1,
    //       "properties":null,
    //       "xkbId":"ua",
    //       "_signalConnections":[{"id":1,"name":"activate","disconnected":false},{"id":2,"name":"changed","disconnected":false}],"_nextConnectionId":3},
    //  "2":{
    //       "type":"xkb",
    //       "id":"ru",
    //       "displayName":"Russian",
    //       "_shortName":"ru",
    //       "index":2,
    //       "properties":null,
    //       "xkbId":"ru",
    //       "_signalConnections":[{"id":1,"name":"activate","disconnected":false},{"id":2,"name":"changed","disconnected":false}],"_nextConnectionId":3}
    // }')

    val inputSourceMap = gnomeJsonMapper.readValue<LinkedHashMap<String, InputSource>>(
        response.extractGnomeShellCmdResponseBody())

    return inputSourceMap
}


internal fun currentGnomeInputSource(): InputSource {
    val response = executeGnomeShellCmd("imports.ui.status.keyboard.getInputSourceManager().currentSource")

    return gnomeJsonMapper.readValue<InputSource>(
        response.extractGnomeShellCmdResponseBody())
}


// Java locales:
//  https://www.oracle.com/java/technologies/javase/java8locales.html
//
internal fun findGnomeInputSource(inputSources: Map<String, InputSource>, locale: Locale): Map.Entry<String, InputSource>? {

    val localeLanguageIds = listOf(
            locale.language,
            locale.isO3Language,
            locale.displayLanguage,
            locale.getDisplayLanguage(Locale.ENGLISH),
            locale.toLanguageTag(),
        )
        .map { it.lowercase() }
        .filterNotBlank()
        .distinct()

    val localeCountryIds = listOf(
            locale.country,
            locale.isO3Country,
            locale.displayCountry,
            locale.getDisplayCountry(Locale.ENGLISH),
        )
        .map { it.lowercase() }
        .filterNotBlank()
        .distinct()

    val fullMatch = inputSources.entries.find { (_, s) ->
        val sourceLanguageIds = s.languageIds()
        val sourceCountryIds  = s.countryIds()
        sourceLanguageIds.containsOneOf(localeLanguageIds) && sourceCountryIds.containsOneOf(localeCountryIds)
    }

    if (fullMatch != null) return fullMatch

    val byLangMatch = inputSources.entries.find { (_, s) ->
        val sourceLanguageIds = s.languageIds()
        sourceLanguageIds.containsOneOf(localeLanguageIds)
    }

    return byLangMatch
}


fun selectGnomeInputSource(locale: Locale) {

    log.debug { "selectGnomeInputSource($locale)" }

    val inputSources = getGnomeInputSources()
    val currentInputSource = currentGnomeInputSource()

    val targetInputSourceEntry = findGnomeInputSource(inputSources, locale)
    checkNotNull(targetInputSourceEntry) { "InputSource/KeyboardLayout for $locale is not found." }

    if (currentInputSource.id == targetInputSourceEntry.value.id) {
        log.debug { "Gnome keyboard [$locale] is already selected." }
        return
    }

    //val cmd = "imports.ui.status.keyboard.getInputSourceManager().inputSources[${targetInputSource.value.index}].activate()"
    val cmd = "imports.ui.status.keyboard.getInputSourceManager().inputSources[${targetInputSourceEntry.key}].activate()"
    executeGnomeShellCmd(cmd)

    // result/output
    // (true, '')
}

private fun executeGnomeShellCmd(cmd: String): String {
    val response = executeCommandWithOutput(
        "gdbus",
        "call",
        "--session",
        "--dest", "org.gnome.Shell",
        "--object-path", "/org/gnome/Shell",
        "--method", "org.gnome.Shell.Eval",
        cmd).trim()

    validateGnomeShellCmdResponse(response, cmd)
    return response
}


private fun String.extractGnomeShellCmdResponseBody(): String =
    this.trim().removeOneOfPrefixes("(true, '", "(true,'").removeSuffix("')")


private fun validateGnomeShellCmdResponse(response: CharSequence, cmd: String) {
    if (response.startsWith("(false"))
        throw IllegalStateException("Gnome shell [$cmd] failed.")

    check(response.startsWithOneOf("(true, '", "(true,'") && response.endsWith("')")) {
        "Gnome shell [$cmd] returned unexpected response." }
}

private val gnomeJsonMapper =
    jsonMapper { configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) }
    .registerKotlinModule()
