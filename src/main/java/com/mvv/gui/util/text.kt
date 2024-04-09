package com.mvv.gui.util


@Suppress("NOTHING_TO_INLINE")
private inline fun Char.isWordSeparator(): Boolean {
    val ch = this
    return (ch == ' ' || ch == '\t' || ch == '\n' || ch == ',' || ch == ';')
}

val CharSequence.wordCount: Int get() {
    var prevIsSpace = true
    var wordCount = 0

    for (ch in this) {
        val isWordSeparator = ch.isWordSeparator()
        if (prevIsSpace && !isWordSeparator) {
            wordCount++
        }
        prevIsSpace = isWordSeparator
    }

    return wordCount
}

fun CharSequence.splitToWords(): List<String> = this.split(' ', '\t', '\n', ',', ';').filter { it.isNotEmpty() }

// TODO: optimize
fun CharSequence.lastWord(): String? = this.splitToWords().lastOrNull()
fun CharSequence.firstWord(): String? = this.splitToWords().firstOrNull()


data class TextPosition (
    val row: Int,
    val column: Int,
)

fun CharSequence.toTextPosition(index: Int): TextPosition {
    require(index < length) { "Index ($index) must be less than string length ($length)." }

    var row = 0
    var col = 0

    for (i in 0..index) {
        val ch = this[i]
        if (ch == '\n') {
            row++
            col = 0
        }
        else
            col++
    }
    return TextPosition(row, col)
}
