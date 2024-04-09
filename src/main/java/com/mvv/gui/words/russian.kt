package com.mvv.gui.words

import com.mvv.gui.util.endsWithOneOf
import com.mvv.gui.util.wordCount


@Suppress("NOTHING_TO_INLINE") // TODO: add tests
inline fun Char.isRussianLetter(): Boolean {
    return (this in 'а'..'я') || (this in 'А'..'Я')
}


val CharSequence.isVerb: Boolean get() {
    val fixed = this.trimEnd()
    return fixed.endsWithOneOf("ть", "ться", ignoreCase = true)
}
val CharSequence.isOneWordVerb: Boolean get() = this.wordCount == 1 && this.isVerb


val CharSequence.isOptionalVerbEnd: Boolean get() {
    val fixed = this.trimEnd()
    return fixed == "ся"
}


val CharSequence.isAdjective: Boolean get() {
    val fixed = this.trimEnd()//.lowercase()
    return fixed.isNotEmpty() && (fixed[fixed.length - 1] == 'й') &&
           fixed.endsWithOneOf("ый", "ий", "ой", "ин", "ын", "ов", "ей", "от")
}


val CharSequence.isProbablyAdverbForVerb: Boolean get() {
    val fixed = this.trimEnd()//.lowercase()
    return fixed.isNotEmpty() && fixed.endsWithOneOf("но", "ро", "ко")
}
