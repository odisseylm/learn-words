package com.mvv.gui.util



fun <S: CharSequence> S.endsWithOneOf(suffixes: Iterable<String>): Boolean = suffixes.any { this.endsWith(it) }
fun <S: CharSequence> S.endsWithOneOf(vararg suffixes: String): Boolean = endsWithOneOf(suffixes.asIterable())

fun <S: CharSequence> S.startsWithOneOf(suffixes: Iterable<String>): Boolean = suffixes.any { this.startsWith(it) }
fun <S: CharSequence> S.startsWithOneOf(vararg suffixes: String): Boolean = this.startsWithOneOf(suffixes.asIterable())

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


fun <S: CharSequence> S.ifNotBlank(action: (S)->S): S = if (this.isBlank()) this else action(this)


fun <T: CharSequence> T?.trimToNull(): T? =
    if (this.isNullOrBlank()) null else this


val String.lastChar: Char get() {
    require(this.isNotEmpty()) { "Empty string."}
    return this[this.length - 1]
}

val String.lastCharOrNull: Char? get() = if (this.isNotEmpty()) this[this.length - 1] else null
