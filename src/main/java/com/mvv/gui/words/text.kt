package com.mvv.gui.words


data class Part (
    val content: CharSequence,
    val inBrackets: Boolean,
    // content
    val from: Int,
    /** Exclusive */
    val to: Int,
    // bracket indices
    val openBracketIndex: Int,
    val closingBracketIndex: Int,
) {
    fun asSubContent(): CharSequence = if (inBrackets) content.subSequence(openBracketIndex, closingBracketIndex + 1)
    else content.subSequence(from, to)
    fun asTextOnly(): CharSequence = content.subSequence(from, to)

    companion object {
        fun inBrackets(
            content: CharSequence,
            // bracket indices
            openBracketIndex: Int,
            closingBracketIndex: Int,
        ) = Part(content, true, openBracketIndex + 1, closingBracketIndex, openBracketIndex, closingBracketIndex)
        fun withoutBrackets(
            content: CharSequence,
            from: Int,
            /** Exclusive */
            to: Int,
        ) = Part(content, false, from, to, -1, -1)
    }
}


val Part.withoutBrackets: Boolean get() = !this.inBrackets


fun CharSequence.splitByBrackets(): List<Part> {

    val parts = mutableListOf<Part>()
    var bracketLevel = 0
    var bracketPartStart = -1
    var withoutBracketPartStart = 0

    for (i in this.indices) {
        val ch = this[i]

        if (ch == '(') {
            bracketLevel++

            if (bracketLevel == 1) {
                if (i > withoutBracketPartStart) {
                    parts.add(Part.withoutBrackets(this, withoutBracketPartStart, i))
                    withoutBracketPartStart = -1
                }

                bracketPartStart = i
            }
        }

        if (ch == ')') {
            bracketLevel--

            if (bracketLevel == 0) {
                parts.add(Part.inBrackets(this, bracketPartStart, i))
                bracketPartStart = -1
                withoutBracketPartStart = i + 1
            }
        }
    }

    if (withoutBracketPartStart != -1 && withoutBracketPartStart < this.lastIndex) {
        parts.add(Part.withoutBrackets(this, withoutBracketPartStart, this.length))
    }

    return parts
}
