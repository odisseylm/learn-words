package com.mvv.gnome.gsettings

import com.mvv.gui.util.*
import java.util.*


private val log = mu.KotlinLogging.logger {}


//
// gsettings list-recursively org.gnome.desktop.input-sources
//   gsettings list-recursively
// or https://github.com/GNOME/gsettings-desktop-schemas/blob/master/schemas/org.gnome.desktop.input-sources.gschema.xml.in
//
// gsettings get org.gnome.desktop.input-sources sources
// setxkbmap -query  => so-so
//
// gsettings get org.gnome.desktop.input-sources mru-sources
//  [('xkb', 'ua'), ('xkb', 'ru'), ('xkb', 'us'), ('xkb', 'gb'), ('xkb', 'ge+ru'), ('xkb', 'au')]
//  Seems we can use 1-st value as 'current'
//  ??
//   gsettings set org.gnome.desktop.input-sources mru-sources "[('xkb', 'au'), ('xkb', 'ru'), ('xkb', 'us'), ('xkb', 'gb'), ('xkb', 'ge+ru'), ('xkb', 'ua')]"
//   gsettings set org.gnome.desktop.input-sources sources "[('xkb', 'au'), ('xkb', 'ru'), ('xkb', 'us'), ('xkb', 'gb'), ('xkb', 'ge+ru'), ('xkb', 'ua')]"
//   gsettings set org.gnome.desktop.input-sources current "('xkb', 'au')"
//
// ??? seems doe NOT work
// gsettings get org.gnome.desktop.input-sources current
//
// setxkbmap -layout ua
//  ?? It switches half-successfully but 'language icon' is not updated ??
//
// ??? what is it
// ??? gsettings get org.gnome.desktop.input-sources show-all-sources
//
// org.gnome.desktop.input-sources show-all-sources false
// org.gnome.desktop.input-sources per-window false
// org.gnome.desktop.input-sources current uint32 0
// org.gnome.desktop.input-sources sources @a(ss) []
// org.gnome.desktop.input-sources xkb-options @as []
//

// gsettings get org.gnome.desktop.input-sources sources
//   [('xkb', 'us'), ('xkb', 'gb'), ('xkb', 'au'), ('xkb', 'ru'), ('xkb', 'ge+ru'), ('xkb', 'ua')]
//
// setxkbmap -query   ??? It even does not see/show/print ru,ge+ru,ua ??? Probably shows only first 4 ones ?
//   rules:      evdev
//   model:      pc105
//   layout:     us,gb,au,us
//   variant:    ,,,


data class InputSourceKey (
    val type: String,
    val id: String,
) {
    constructor(id: String) : this(type = "xkb", id = id)
    override fun toString(): String = "('$type','$id')"
}


fun currentGSettingsInputSources(): List<InputSourceKey> {
    val response = executeCommandWithOutput("gsettings" ,"get", "org.gnome.desktop.input-sources", "sources").trim()

    // response: [('xkb', 'us'), ('xkb', 'ru'), ('xkb', 'ua')]
    val inputSourceKeys = response.parseInputSourceKeys()
    return inputSourceKeys
}


@Deprecated(message = "It very bad/slow/CPU consuming approach.")
fun selectGSettingsInputSource(locale: Locale) {

    val inputSourceKeys = currentGSettingsInputSources()
    if (inputSourceKeys.size <= 1)
        throw IllegalStateException("No input source to select since there are only $inputSourceKeys.")

    val targetInputSource = inputSourceKeys.findInputSourceKey(locale)
        ?: throw IllegalStateException("InputSource for [${locale}] is not found.")

    try {
        executeCommand("gsettings", "set", "org.gnome.desktop.input-sources", "sources",
            "[('${targetInputSource.type}', '${targetInputSource.id}')]")
    } catch (ex: Exception) {
        log.warn("Error of setting $targetInputSource")
    }

    val all = inputSourceKeys.joinToString(prefix = "[", separator = ", ", postfix = "]") { "('${it.type}', '${it.id}')" }
    executeCommand("gsettings", "set", "org.gnome.desktop.input-sources", "sources", all)
}


internal fun List<InputSourceKey>.findInputSourceKey(locale: Locale): InputSourceKey? {
    val inputSourceKeys = this

    val inputSources: List<InputSource> = inputSourceKeys
        .map { allXkbInputSources[it.id]
            ?: throw IllegalStateException("InputSource [${it.id}] is not found in config.")
        }

    val targetInputSource = inputSources.findInputSource(locale)

    return if (targetInputSource == null) null
           else inputSourceKeys.find { it.id == targetInputSource.id }
}


// Input example/format: [('xkb', 'us'), ('xkb', 'ru'), ('xkb', 'ua')]
//
internal fun String.parseInputSourceKeys(): List<InputSourceKey> {
    val sPairs = this
        .trim()
        .removePrefix("[")
        .removeSuffix("]")

    return sPairs.splitByComma()
        .asSequence()
        .map { parsePair(it) }
        .map { InputSourceKey(it.first.removeWrappingQuotes(), it.second.removeWrappingQuotes()) }
        .toList()
}


val allXkbInputSources: Map<String, InputSource> by lazy {
    loadAllXkbInputSources()
        .flatMap { l ->
                    listOf(l.name to l.toInputSource()) +
                    l.variantList.map { "${l.name}+${it.name}" to l.toInputSourceWithVariant(it)} }
        .associate { it }
}
