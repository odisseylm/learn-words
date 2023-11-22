package com.mvv.gui.util



fun <S: CharSequence> S.endsWithOneOf(suffixes: Iterable<String>): Boolean = suffixes.any { this.endsWith(it) }
fun <S: CharSequence> S.endsWithOneOf(vararg suffixes: String): Boolean = endsWithOneOf(suffixes.asIterable())

fun <S: CharSequence> S.startsWithOneOf(suffixes: Iterable<String>): Boolean = suffixes.any { this.startsWith(it) }
fun <S: CharSequence> S.startsWithOneOf(vararg suffixes: String): Boolean = this.startsWithOneOf(suffixes.asIterable())

fun <S: CharSequence> S.startsWithOneOf(suffixes: Iterable<String>, ignoreCase: Boolean): Boolean = suffixes.any { this.startsWith(it, ignoreCase) }
fun <S: CharSequence> S.startsWithOneOf(vararg suffixes: String, ignoreCase: Boolean): Boolean = this.startsWithOneOf(suffixes.asIterable(), ignoreCase)

fun <S: CharSequence> S.containsOneOf(suffixes: Iterable<String>): Boolean = suffixes.any { this.contains(it) }
fun <S: CharSequence> S.containsOneOf(vararg suffixes: String): Boolean = this.containsOneOf(suffixes.asIterable())


fun String.removeSuffixCaseInsensitive(suffix: String): String =
    when {
        this.endsWith(suffix) -> this.removeSuffix(suffix)
        this.lowercase().endsWith(suffix.lowercase()) -> this.substring(0, this.length - suffix.length)
        else -> this
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
    var s = this

    do {
        val prefix = suffixes.chars()
            .filter { s.endsWith(it.toChar()) }
            .findFirst()

        if (prefix.isEmpty) return s

        s = s.substring(0, s.length - 1)
    } while (true)
}


fun CharSequence.removeCharPrefixesRepeatably(prefixes: String): CharSequence {
    var s = this

    do {
        val prefix = prefixes.chars()
            .filter { s.startsWith(it.toChar()) }
            .findFirst()

        if (prefix.isEmpty) return s

        s = s.substring(1)
    } while (true)
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

    return res.reversed()
}


fun CharSequence.startsWithOneOfChars(chars: String) =
    if (this.isEmpty()) false else this[0] in chars

fun CharSequence.endsWithOneOfChars(chars: String) =
    if (this.isEmpty()) false else this[this.length - 1] in chars


// T O D O: compare with manuals loops
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


fun <S: CharSequence> S.ifNotBlank(action: (S)->S): S = if (this.isBlank()) this else action(this)

fun <S: CharSequence> S?.doIfNotBlank(action: ()->Unit) {
    if (!this.isNullOrBlank()) action()
}


fun <T: CharSequence> T?.trimToNull(): T? =
    if (this.isNullOrBlank()) null else this


enum class SpaceCharPolicy { KeepExistent, UseSpaceOnly }

fun String.removeRepeatableSpaces(spaceCharPolicy: SpaceCharPolicy = SpaceCharPolicy.KeepExistent): String {

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

    if (processFromIndex == -1) return this

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


// TODO: optimize to avoid trim
fun String.containsWhiteSpaceInMiddle(): Boolean = this.trim().containsWhiteSpace()

fun String.containsWhiteSpace(): Boolean {
    for (i in this.indices) {
        if (this[i].isWhitespace()) return true
    }
    return false
}


val String.lastChar: Char get() {
    require(this.isNotEmpty()) { "Empty string."}
    return this[this.length - 1]
}

val String.lastCharOrNull: Char? get() = if (this.isNotEmpty()) this[this.length - 1] else null


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


//@Suppress("NOTHING_TO_INLINE")
//inline fun Int.isEnglishLetter(): Boolean {
//    return (this in 'a'.code..'z'.code) || (this in 'A'.code..'Z'.code)
//}
//fun CharSequence.containsEnglishLetters(): Boolean =
//    this.codePoints().anyMatch { it.isEnglishLetter() }



@Suppress("NOTHING_TO_INLINE") // TODO: add tests
inline fun Char.isRussianLetter(): Boolean {
    return (this in 'а'..'я') || (this in 'А'..'Я')
}
