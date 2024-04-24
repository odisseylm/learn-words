package com.mvv.gnome.shell.keyboard

import com.mvv.gnome.gsettings.currentGSettingsInputSources
import com.mvv.gnome.gsettings.findInputSourceKey
import com.mvv.gnome.gsettings.removeWrappingQuotes
import com.mvv.gui.util.*
import org.xml.sax.InputSource
import java.io.StringReader
import java.util.*
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory


private val log = mu.KotlinLogging.logger {}


// See
//  https://extensions.gnome.org/extension/6691/shyriiwook/
//  https://github.com/madhead/shyriiwook
//

//fun <T> Boolean.map(no: T, yes: T): T = if (this) yes else no

val isShyriiwookExtensionAvailable: Boolean by lazy { isShyriiwookExtensionAvailableImpl()
            .also { log.info("Shyriiwook gnome extension is${it.asAdverb} available.") } }

internal fun isShyriiwookExtensionAvailableImpl(): Boolean {
    val response = executeCommandWithOutput(
        "gdbus", "introspect", "--session", "--dest", "org.gnome.Shell",
        "--object-path", "/me/madhead/Shyriiwook", "--only-properties", "--xml")

    val dbf = DocumentBuilderFactory.newInstance()
    dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
    dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)

    val db = dbf.newDocumentBuilder()
    val doc = db.parse(InputSource(StringReader(response)))

    val childCount = doc.childNodes.length
    return childCount != 0
}


fun selectInputSourceByShyriiwookExtension(locale: Locale) {

    val inputSourceKeys = currentGSettingsInputSources()
    if (inputSourceKeys.size <= 1)
        throw IllegalStateException("No input source to select since there are only $inputSourceKeys.")

    val targetInputSource = inputSourceKeys.findInputSourceKey(locale)
        ?: throw IllegalStateException("InputSource for [${locale}] is not found.")

    val currentId = getCurrentInputSourceByShyriiwookExt()

    if (targetInputSource.id == currentId) {
        log.debug { "Gnome keyboard [$currentId] is already selected." }
        return
    }

    executeCommandWithOutput(
        "gdbus", "call", "--session", "--dest", "org.gnome.Shell",
        "--object-path", "/me/madhead/Shyriiwook",
        "--method", "me.madhead.Shyriiwook.activate",
        targetInputSource.id)
}


internal fun getCurrentInputSourceByShyriiwookExt(): String {

    //val xmlResponse = executeCommandWithOutput(
    //    "gdbus", "introspect", "--session", "--dest", "org.gnome.Shell",
    //    "--object-path", "/me/madhead/Shyriiwook", "--only-properties", "--xml")
    //
    // Ha-ha-ha... When we use '--xml' values are NOT printed :-(
    // We cannot use standard XML parsing (with xPath).

    val response = executeCommandWithOutput(
        "gdbus", "introspect", "--session", "--dest", "org.gnome.Shell",
        "--object-path", "/me/madhead/Shyriiwook", "--only-properties")

    return parseCurrentInputSourceByShyriiwookExt(response)
}


internal fun parseCurrentInputSourceByShyriiwookExt(response: String): String {

    // Output:
    //    ...
    //    readonly s currentLayout = 'us';
    //    ...

    val currentLayoutLine: String = response.lines()
        .find { it.contains("currentLayout") }
        ?: throw IllegalStateException("currentLayout is not found.")

    val currentLayout = currentLayoutLine
        .substringAfter("currentLayout").trim()
        .removePrefix("=").trim().removeSuffix(";").trim()
        .removeWrappingQuotes()

    return currentLayout
}
