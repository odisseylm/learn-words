package com.mvv.gui.words


class TextParser {

    private val delimiters = arrayOf(
        " ", "\n", "\r", "\t",
        ".", ",", ";", ":", "!", "?",
        "=", "—",
        "!", "@", "$", "%", "^", "&", "*",
        "+", "*", "/",
        "<", ">",
        "\"", "\'", "/", "|",
        "~", "`", "“",
        "(", ")", "[", "]", "{", "}",
        "\\", "/",
    )

    fun parse(text: CharSequence): List<String> =
        text.splitToSequence(delimiters = delimiters)
            .filter { it.isNotEmpty() }
            .filter { it.length > 1 }
            .map { it.removePrefix("-") }
            .filter { !it.isNumber }
            .distinctBy { it.lowercase() }
            //.filter { !ignoredWordsSorted.contains(it) }
            //.distinct()
            .toList()

}


private const val numberChars = "1234567890-_.Ee"

private val String.isNumber: Boolean get() {
    return try { this.toInt(); true }
    catch (ignore: NumberFormatException) {
        this.codePoints().allMatch { ch -> numberChars.contains(ch.toChar()) }
    }
}
