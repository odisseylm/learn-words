package com.mvv.gui.util


val CharSequence.wordCount: Int get() {
    var prevIsSpace = true
    var wordCount = 0

    for (ch in this) {
        val isSpace = (ch == ' ' || ch == '\t' || ch == '\n' || ch == ',' || ch == ';')
        if (prevIsSpace && !isSpace) {
            wordCount++
        }
        prevIsSpace = isSpace
    }

    return wordCount
}

fun CharSequence.splitToWords(): List<String> = this.split(' ', '\t', '\n', ',', ';').filter { it.isNotEmpty() }
