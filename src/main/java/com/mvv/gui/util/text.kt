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

fun CharSequence.lastWord(): String? = this.splitToWords().lastOrNull()
