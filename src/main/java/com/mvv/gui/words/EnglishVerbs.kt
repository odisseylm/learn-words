package com.mvv.gui.words

import com.mvv.gui.util.endsWithOneOf
import com.mvv.gui.util.lastChar


class EnglishVerbs {

    private val irregularVerbsToInfinitive: Map<String, String> = (
            irregularVerbs.asSequence().flatMap { it.pastParticiple.map { pastParticiple -> Pair(pastParticiple, it.base) } } +
            irregularVerbs.asSequence().flatMap { it.pastTense.map { pastTense -> Pair(pastTense, it.base) } } +
            // special cases
            listOf(Pair("is", "be"), Pair("am", "be"), Pair("are", "be"))
        )
        .associate { it }

    private val irregularInfinitives: Set<String> = irregularVerbs.map { it.base }.toSet()

    /** @param verb verbs should be in lower case */
    internal fun getIrregularInfinitive(verb: String): String? {
        var inf = getIrregularInfinitiveImpl(verb)
        if (inf != null) return inf

        inf = getIrregularInfinitiveWithSuffix(verb, "ing")
        if (inf != null) return inf

        inf = getIrregularInfinitiveWithSuffix(verb, "s")
        if (inf != null) return inf

        inf = getIrregularInfinitiveWithSuffix(verb, "es")
        if (inf != null) return inf

        return null
    }

    private fun getIrregularInfinitiveWithSuffix(verb: String, suffix: String): String? {
        val withoutSuffix = verb.removeSuffix(suffix)
        if (withoutSuffix.isEmpty()) return null

        var inf = getIrregularInfinitiveImpl(withoutSuffix)
        if (inf != null) return inf

        inf = getIrregularInfinitiveImpl(withoutSuffix + "y")
        if (inf != null) return inf

        inf = getIrregularInfinitiveImpl(withoutSuffix + "e")
        if (inf != null) return inf

        inf = getIrregularInfinitiveImpl(withoutSuffix + withoutSuffix.lastChar)
        if (inf != null) return inf

        if (withoutSuffix.length >= 4 && withoutSuffix[withoutSuffix.length - 2] == withoutSuffix[withoutSuffix.length - 1])
            inf = getIrregularInfinitiveImpl(withoutSuffix.substring(0, withoutSuffix.length - 1))

        return inf
    }

    private fun getIrregularInfinitiveImpl(verb: String): String? =
        if (verb in irregularInfinitives) verb else irregularVerbsToInfinitive[verb]

    // T O D O: Add removing "s"/"ing" for regular if it is POSSIBLE???
    /** Returns infinitive if it surely can be determined, otherwise it returns null. */
    @Deprecated("Not implemented. I'm not sure that it is possible.")
    internal fun getInfinitive(verb: String): String? {
        @Suppress("NAME_SHADOWING")
        val verb = verb.lowercase()
        val irregularInfinitive = getIrregularInfinitive(verb)
        if (irregularInfinitive != null) return irregularInfinitive

        if (!verb.endsWith("d")) return verb

        val infinite = when {
            verb.endsWith("yied") -> verb.removeSuffix("ied")
            // exception 'died'
            verb == "died"        -> "die"
            verb.endsWith("ied")  -> verb.removeSuffix("ied") + "y"

            verb.endsWithOneOf("yed", "ued", "ained", "ched", "nted", "ived", "ayed", "rned")
                 -> verb.removeSuffix("ed")
            else -> null
        }

        return infinite
    }
}
