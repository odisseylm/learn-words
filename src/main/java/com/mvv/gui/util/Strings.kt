package com.mvv.gui.util

import kotlin.math.min


fun <S: CharSequence> S.endsWithOneOf(suffixes: Iterable<String>): Boolean = suffixes.any { this.endsWith(it) }
fun <S: CharSequence> S.endsWithOneOf(suffixes: Iterable<String>, ignoreCase: Boolean): Boolean = suffixes.any { this.endsWith(it, ignoreCase) }
fun <S: CharSequence> S.endsWithOneOf(vararg suffixes: String): Boolean = endsWithOneOf(suffixes.asIterable())
fun <S: CharSequence> S.endsWithOneOf(vararg suffixes: String, ignoreCase: Boolean): Boolean = endsWithOneOf(suffixes.asIterable(), ignoreCase)

fun <S: CharSequence> S.startsWithOneOf(suffixes: Iterable<String>): Boolean = suffixes.any { this.startsWith(it) }
fun <S: CharSequence> S.startsWithOneOf(vararg suffixes: String): Boolean = this.startsWithOneOf(suffixes.asIterable())

fun <S: CharSequence> S.startsWithOneOf(suffixes: Iterable<String>, ignoreCase: Boolean): Boolean = suffixes.any { this.startsWith(it, ignoreCase) }
fun <S: CharSequence> S.startsWithOneOf(vararg suffixes: String, ignoreCase: Boolean): Boolean = this.startsWithOneOf(suffixes.asIterable(), ignoreCase)

fun <S: CharSequence> S.containsOneOf(strings: Iterable<CharSequence>): Boolean = strings.any { this.contains(it) }
fun <S: CharSequence> S.containsOneOf(vararg strings: CharSequence): Boolean = this.containsOneOf(strings.asIterable())
// !!! Returned index may be not 1st index in source string !!!
fun <S: CharSequence> S.indexOfAnyOfOneOf(strings: Iterable<String>): Int {
    for (s in strings) {
        val index = this.indexOf(s)
        if (index != -1) return index
    }
    return -1
}


fun CharSequence.removeSuffixCaseInsensitive(suffix: CharSequence): CharSequence =
    if (this.endsWith(suffix, ignoreCase = true)) this.subSequence(0, this.length - suffix.length) else this
fun String.removeSuffixCaseInsensitive(suffix: String): String =
    if (this.endsWith(suffix, ignoreCase = true)) this.substring(0, this.length - suffix.length) else this
fun String.removeSuffixCaseInsensitive(suffixes: Iterable<String>): String {
    for (suffix in suffixes)
        if (this.endsWith(suffix, ignoreCase = true))
            return this.substring(0, this.length - suffix.length)
    return this
}


fun CharSequence.removeSuffixesRepeatably(vararg suffixes: String): CharSequence {
    var s = this

    do {
        val changed = suffixes
            .find { s.endsWith(it) }
            ?.let { s.removeSuffix(it) }
            ?: return s

        s = changed
    } while (true)
}


fun CharSequence.removePrefixesRepeatably(vararg prefixes: String): CharSequence {
    var s = this

    do {
        val changed = prefixes
            .find { s.startsWith(it) }
            ?.let { s.removePrefix(it) }
            ?: return s

        s = changed
    } while (true)
}


fun CharSequence.removeCharSuffixesRepeatably(suffixes: String): CharSequence {
    for (i in this.length - 1 downTo 0) {
        val ch = this[i]
        val isPrefixChar = ch in suffixes

        if (!isPrefixChar) return this.subSequence(0, i + 1)
    }
    return this
}

fun String.removeCharSuffixesRepeatably(suffixes: String): String {
    for (i in this.length - 1 downTo 0) {
        val ch = this[i]
        val isPrefixChar = ch in suffixes

        if (!isPrefixChar) return this.substring(0, i + 1)
    }
    return this
}


fun CharSequence.removeCharPrefixesRepeatably(prefixes: String): CharSequence {
    for (i in indices) {
        val ch = this[i]
        val isPrefixChar = ch in prefixes

        if (!isPrefixChar) return this.subSequence(i, this.length)
    }
    return this
}


fun CharSequence.separateCharPrefixesRepeatably(prefixes: String): List<String> {
    val res = mutableListOf<String>()

    for (i in this.indices) {
        val ch = this[i]
        if (ch in prefixes)
            // TODO: use cached string
            res.add(this.substring(i, i + 1))
        else {
            res.add(this.substring(i))
            break
        }
    }

    return res
}


fun CharSequence.separateCharSuffixesRepeatably(prefixes: String): List<String> {
    val res = mutableListOf<String>()

    for (i in this.indices.reversed()) {
        val ch = this[i]
        if (ch in prefixes)
            // TODO: use cached string
            res.add(this.substring(i, i + 1))
        else {
            res.add(this.substring(0, i + 1))
            break
        }
    }

    return res.reversedList()
}


fun CharSequence.startsWithOneOfChars(chars: String) =
    if (this.isEmpty()) false else this[0] in chars

fun CharSequence.endsWithOneOfChars(chars: String) =
    if (this.isEmpty()) false else this[this.length - 1] in chars


fun CharSequence.indexOfOneOfChars(chars: String): Int {
    for (charIndex in this.indices) {
        if (this[charIndex] in chars) return charIndex
    }
    return -1
}
fun CharSequence.indexOfOneOfChars(chars: String, index: Int): Int {
    for (charIndex in index until this.length) {
        if (this[charIndex] in chars) return charIndex
    }
    return -1
}

fun CharSequence.lastIndexOfOneOfChars(chars: String): Int {
    for (charIndex in this.indices.reversed()) {
        if (this[charIndex] in chars) return charIndex
    }
    return -1
}

fun CharSequence.lastIndexOfOneOfChars(chars: String, startingFrom: Int): Int =
    lastIndexOfOneOfChars(chars, startingFrom, this.length - 1)

fun CharSequence.lastIndexOfOneOfChars(chars: String, startingFrom: Int, end: Int): Int {
    for (charIndex in end downTo startingFrom) {
        if (this[charIndex] in chars) return charIndex
    }
    return -1
}


//fun CharSequence.removeOneOfPrefixes(vararg prefixes: CharSequence): CharSequence {
//    for (prefix in prefixes)
//        if (this.startsWith(prefix)) return this.subSequence(prefix.length, this.length)
//    return this
//}
fun String.removeOneOfPrefixes(vararg prefixes: CharSequence): String {
    for (prefix in prefixes)
        if (prefix.isNotEmpty() && this.startsWith(prefix)) return this.substring(prefix.length)
    return this
}
fun String.removeOneOfSuffixes(vararg suffixes: CharSequence): String {
    for (suffix in suffixes)
        if (suffix.isNotEmpty() && this.endsWith(suffix)) return this.substring(0, this.length - suffix.length)
    return this
}

fun <S: CharSequence> S.ifNotBlank(action: (S)->S): S = if (this.isBlank()) this else action(this)

inline fun <C : CharSequence> C?.ifNullOrBlank(defaultValue: () -> C): C =
    if (this.isNullOrBlank()) defaultValue() else this


fun <S: CharSequence> S?.doIfNotBlank(action: ()->Unit) {
    if (!this.isNullOrBlank()) action()
}


fun <T: CharSequence> T?.trimToNull(): T? =
    if (this.isNullOrBlank()) null else this


enum class SpaceCharPolicy { KeepExistent, UseSpaceOnly }

fun CharSequence.removeRepeatableSpaces(spaceCharPolicy: SpaceCharPolicy = SpaceCharPolicy.KeepExistent): String {

    val useOnlySpaceAsWhiteSpaceChar: Boolean = (spaceCharPolicy === SpaceCharPolicy.UseSpaceOnly)

    var processFromIndex = -1
    var lastCharIsSpaceChar = false

    for (i in this.indices) {
        val ch = this[i]

        if (ch.isWhitespace()) {
            if (lastCharIsSpaceChar) {
                processFromIndex = i
                break
            }

            if (useOnlySpaceAsWhiteSpaceChar && ch != ' ') {
                processFromIndex = i
                break
            }

            lastCharIsSpaceChar = true
        }
        else lastCharIsSpaceChar = false
    }

    if (processFromIndex == -1) return this.toString()

    val result = StringBuilder(this.length)
    result.appendRange(this, 0, processFromIndex)
    if (useOnlySpaceAsWhiteSpaceChar && result.isNotEmpty()
        && result.last().isWhitespace() && result.last() != ' ')
        result[result.length - 1] = ' '

    for (i in processFromIndex until this.length) {
        val ch = this[i]

        @Suppress("LiftReturnOrAssignment")
        if (ch.isWhitespace()) {
            if (!lastCharIsSpaceChar) result.append( if (useOnlySpaceAsWhiteSpaceChar) ' ' else ch )
            lastCharIsSpaceChar = true
        }
        else {
            result.append(ch)
            lastCharIsSpaceChar = false
        }
    }

    return result.toString()
}


fun CharSequence.containsWhiteSpaceInMiddle(): Boolean = this.trim().containsWhiteSpace()

fun CharSequence.containsWhiteSpace(): Boolean {
    for (i in this.indices) {
        if (this[i].isWhitespace()) return true
    }
    return false
}


val CharSequence.lastChar: Char get() {
    require(this.isNotEmpty()) { "Empty string."}
    return this[this.length - 1]
}

val CharSequence.lastCharOrNull: Char? get() = if (this.isNotEmpty()) this[this.length - 1] else null


fun String.safeSubstring(start: Int): String = when {
    start >= this.length -> ""
    else -> safeSubstring(start, this.length)
}

fun String.safeSubstring(start: Int, end: Int): String {
    require(start <= end) { " start($start) > end ($end)"}

    return when {
        start >= length -> ""
        end >= this.length -> this.substring(start)
        else -> this.substring(start, end)
    }
}

fun CharSequence.safeSubSequence(start: Int): CharSequence = when {
    start >= this.length -> ""
    else -> safeSubSequence(start, this.length)
}

fun CharSequence.safeSubSequence(start: Int, end: Int): CharSequence {
    require(start <= end) { " start($start) > end ($end)"}

    return when {
        start >= length -> ""
        end >= this.length -> this.subSequence(start, this.length)
        else -> this.subSequence(start, end)
    }
}


fun CharSequence.substringStartingFrom(begin: String, endExcluding: String, maxLength: Int = Int.MAX_VALUE): String? {

    val startIndex = this.indexOf(begin)
    if (startIndex == -1) return null

    val endIndex = this.indexOf(endExcluding, startIndex)
        .ifIndexNotFound(min(this.length, startIndex + maxLength))

    return this.substring(startIndex, endIndex)
}


fun CharSequence.replaceSuffix(currentSuffix: CharSequence, newSuffix: CharSequence): String {
    require(currentSuffix.isNotEmpty()) { "currentSuffix cannot be empty (does not make sense)." }
    if (this.isEmpty() || !this.endsWith(currentSuffix)) return this.toString()

    return this.substring(0, this.length - currentSuffix.length) + newSuffix
}


@Suppress("NOTHING_TO_INLINE")
inline fun Char.isEnglishLetter(): Boolean {
    return (this in 'a'..'z') || (this in 'A'..'Z')
}
fun CharSequence.containsEnglishLetters(): Boolean {
    //return chars().anyMatch { it.isEnglishLetter() }
    for (i in this.indices)
        if (this[i].isEnglishLetter()) return true
    return false
}

fun CharSequence.containsLetter(): Boolean {
    for (i in this.indices)
        if (this[i].isLetter()) return true
        //if (this[i].isRussianLetter() || this[i].isEnglishLetter()) return true
    return false
}


//@Suppress("NOTHING_TO_INLINE")
//inline fun Int.isEnglishLetter(): Boolean {
//    return (this in 'a'.code..'z'.code) || (this in 'A'.code..'Z'.code)
//}
//fun CharSequence.containsEnglishLetters(): Boolean =
//    this.codePoints().anyMatch { it.isEnglishLetter() }


class CharSequenceComparator : Comparator<CharSequence> {
    override fun compare(o1: CharSequence, o2: CharSequence): Int {

        val minLength = min(o1.length, o2.length)

        for (i in 0 until minLength) {
            val ch1 = o1[i]
            val ch2 = o2[i]

            if (ch1 != ch2) return ch1.compareTo(ch2)
        }

        if (o1.length != o2.length) return o1.length.compareTo(o2.length)

        return 0
    }

    companion object {
        val INSTANCE = CharSequenceComparator()
    }
}


fun CharSequence.isEqualTo(other: CharSequence): Boolean {
    if (this.length != other.length) return false

    for (i in this.indices) {
        if (this[i] != other[i]) return false
    }
    return true
}

fun Iterable<CharSequence>.containsCharSequence(v: CharSequence): Boolean =
    this.any { it.isEqualTo(v) }


fun String.substringAfterLast(delimiter: String, missingDelimiterValue: String = this, ignoreCase: Boolean): String {
    val index = lastIndexOf(delimiter, ignoreCase = ignoreCase)
    return if (index == -1) missingDelimiterValue else substring(index + delimiter.length, length)
}
