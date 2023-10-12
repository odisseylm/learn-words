package com.mvv.gui.javafx

import com.mvv.gui.util.containsOneOf
import javafx.css.CssParser
import javafx.css.ParsedValue
import javafx.css.Rule
import javafx.css.Selector
import javafx.scene.paint.Color
import kotlin.math.roundToInt


private val log = mu.KotlinLogging.logger {}

private class TempResourceLocator


@Suppress("SameParameterValue")
fun getStyles(selector: String, cssFiles: Iterable<String>): List<Rule> {

    val cssRules: List<Rule> = cssFiles.flatMap { cssFile ->
        val cssParser = CssParser()
        val r = cssParser.parse(
            TempResourceLocator::class.java.getResource(cssFile)
                ?: TempResourceLocator::class.java.getResource("/$cssFile")
        )
        r.rules
    }

    val ruleSelectors = listOf(selector, ".$selector", "*.$selector").map { Selector.createSelector(it) }
    return cssRules.filter { it.selectors.containsOneOf(ruleSelectors) }
}

val List<Rule>.asCssText: String get() {
    val aa: List<String> = this
        .asReversed()
        .flatMap { it.declarations }
        .map { it.property + ": " + it.parsedValue.toCssStringValue() }

    return aa.joinToString("; ")
}


private fun Any?.toCssStringValue(): String =
    when (this) {
        null -> ""
        is ParsedValue<*, *> ->
            when (val value = this.value) {
                is ParsedValue<*, *> -> value.toCssStringValue()

                is Array<*> -> value.joinToString(" ") {
                    if (it is ParsedValue<*, *>) it.toCssStringValue() else it.toCssStringValue() }

                is Iterable<*> -> value.joinToString(" ") {
                    if (it is ParsedValue<*, *>) it.toCssStringValue() else it.toCssStringValue()
                }

                else -> value.toCssStringValue()
            }
        is Color -> this.toCssColor()
        is String -> this
        else -> {
            log.warn { "${this.javaClass.simpleName} probably is converted to CCS improperly." }
            this.toString()
        }
    }

fun Color.toCssColor(): String =
    "#%02x%02x%02x%02x".format(
        (this.red * 255).roundToInt(),
        (this.green * 255).roundToInt(),
        (this.blue * 255).roundToInt(),
        (this.opacity * 255).roundToInt(),
    )
