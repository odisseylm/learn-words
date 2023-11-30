package com.mvv.gui.words

import com.mvv.gui.util.endsWithOneOf


class EnglishVerbs {

    private val irregularVerbsToInfinitive: Map<String, String> = (
            irregularVerbs.asSequence().flatMap { it.pastParticiple.map { pastParticiple -> Pair(pastParticiple, it.base) } } +
            irregularVerbs.asSequence().flatMap { it.pastTense.map { pastTense -> Pair(pastTense, it.base) } } +
            // special cases
            listOf(Pair("is", "be"), Pair("am", "be"), Pair("are", "be"), )
        )
        .associate { it }

    private val irregularInfinitives: Set<String> = irregularVerbs.map { it.base }.toSet()

    /** @param verb verbs should be in lower case */
    internal fun getIrregularInfinitive(verb: String): String? =
        if (verb in irregularInfinitives) verb else irregularVerbsToInfinitive[verb]

    // TODO: add removing "s"/"ing"
    /** Returns infinitive if it surely can be determined, otherwise it returns null. */
    fun getInfinitive(verb: String): String? {
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
