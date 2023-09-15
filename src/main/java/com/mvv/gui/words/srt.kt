package com.mvv.gui.words

import com.mvv.gui.util.safeSubstring
import java.io.BufferedReader
import java.io.Reader
import java.io.StringReader
import java.util.regex.Pattern


fun loadOnlyTextFromSrt(string: String): String = StringReader(string).use { loadOnlyTextFromSrt(it) }

fun loadOnlyTextFromSrt(reader: Reader): String {
    val result = StringBuilder()

    reader.use {
        val br = BufferedReader(reader)

        br.forEachLine { line ->
            when {
                line.isEmpty() -> { }
                line.isSrtNumber -> { }
                line.isSrtTimeStampsLine -> { }
                else -> result.append(line).append('\n')
            }
        }
    }

    return result.toString()
}

// Example: 00:23:03,281 --> 00:23:05,681
internal val String.isSrtTimeStampsLine: Boolean get() {

    val timeStampLen = 12
    val arrowLen = 5
    val strLength = 2 * timeStampLen + arrowLen

    val str1 = this.safeSubstring(0, timeStampLen)
    val str2 = this.safeSubstring(timeStampLen, timeStampLen + arrowLen)
    val str3 = this.safeSubstring(timeStampLen + arrowLen)

    return this.length == strLength && str1.isSrtTimestamp && str2 == " --> " && str3.isSrtTimestamp
}


private val srtNumber = Pattern.compile("(\\b\\d+\\b)")
// Example: 00:23:03,281
private val srtTimestampPattern = Pattern.compile("(\\b\\d{2}\\b):(\\b\\d{2}\\b):(\\b\\d{2}\\b),(\\b\\d{3}\\b)")

private val String.isSrtNumber: Boolean get() = srtNumber.asPredicate().test(this)


// 00:23:03,281
internal val String.isSrtTimestamp: Boolean get() = srtTimestampPattern.asPredicate().test(this)

/*
internal fun String.isSrtTimestamp(): Boolean =
    Scanner(this).use { s ->

        val matchedString = s.findInLine(srtTimestampPattern)
        if (matchedString != null) {
            val result = s.match()

            println(this)
            for (i in 1..result.groupCount())
                println(result.group(i))

            result.groupCount() == 4
        }
        else false
    }
*/
