package com.mvv.gnome.gsettings

import com.mvv.gui.util.removeOneOfPrefixes
import com.mvv.gui.util.removeOneOfSuffixes


// Input example/format:
//   ('xkb', 'us')  => Pair("'xkb'", "'us'")  // !!! "'" is kept!!!
//   (xkb,    us )  => Pair("xkb", "us")
//
internal fun parsePair(s: String): Pair<String, String> =
    s.trim()
        .removePrefix("(").removeSuffix(")")
        .splitByComma()
        .let {
            require(it.size == 2) { "Two item is expected in [$s]." }
            Pair(it[0], it[1])
        }


internal fun CharSequence.splitByComma(): List<String> {

    var bracesLevel = 0

    val items = mutableListOf<String>()

    var inQuotes = false
    var lastCommaIndex = -1

    for (i in indices) {
        val ch = this[i]

        if (ch == '\'') inQuotes = !inQuotes

        if (!inQuotes) {
            if (ch == '(')
                bracesLevel++
            if (ch == ')')
                bracesLevel--

            if (ch == ',' && bracesLevel == 0) {
                items.add(this.substring(lastCommaIndex + 1, i).trim())
                lastCommaIndex = i
            }
        }
    }

    if (lastCommaIndex < this.length) items.add(this.substring(lastCommaIndex + 1, this.length).trim())

    return items
}


fun String.removeWrappingQuotes(): String =
    this.removeOneOfPrefixes("'", "\"")
        .removeOneOfSuffixes("'", "\"")
