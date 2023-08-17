package com.mvv.gui


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

    fun parse(text: CharSequence): List<String> {

        val wordsSeq = text.splitToSequence(delimiters = delimiters)
        val words = wordsSeq
            .filter { it.isNotEmpty() }
            .filter { it.length > 1 }
            .map { it.removePrefix("-") }
            .filter { !it.isNumber }
            .distinctBy { it.uppercase() }
            //.filter { !ignoredWordsSorted.contains(it) }
            //.distinct()
            .toList()

        return words
    }

}


private const val numberChars = "1234567890-_.Ee"

private val String.isNumber: Boolean get() {
    return try { this.toInt(); true }
    catch (ignore: NumberFormatException) {
        this.codePoints().allMatch { ch -> numberChars.contains(ch.toChar()) }
    }
}
