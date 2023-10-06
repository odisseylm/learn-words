package com.mvv.gui.util

import java.util.Enumeration
import java.util.StringTokenizer


@Suppress("UNCHECKED_CAST")
fun String.splitByCharSeparatorsAndPreserveAllTokens(separatorChars: String): List<String> {
    val st = StringTokenizer(this, separatorChars, true)
    return (st as Enumeration<String>).toList()
}
