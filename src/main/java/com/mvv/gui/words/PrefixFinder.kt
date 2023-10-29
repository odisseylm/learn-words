package com.mvv.gui.words

import com.mvv.gui.util.*
import java.util.*
import kotlin.collections.ArrayList


//private val log = mu.KotlinLogging.logger {}


private class PrefixWordTreeNode ( // PrefixWord
    val word: String,
    var canBeEnd: Boolean = false,
    //val children: TreeSet<PrefixWordTreeNode> = TreeSet(Comparator.comparing { it.word }),
    val children: MutableList<PrefixWordTreeNode> = mutableListOf(),
) {
    override fun toString(): String = "Tree { $word ${if (canBeEnd) ", can be end " else ""}, children: ${children.size} }"
}


internal typealias Alt<T> = List<T> // Alt = alternative (OR) values of words/phrases/sequences
internal typealias Seq<T> = List<T> // Seq = sequence of words or alt values or other sequences or alt sequences, so on

@Suppress("NOTHING_TO_INLINE")
internal inline fun <T> seq(vararg values: T): List<T> = listOf(*values)
@Suppress("NOTHING_TO_INLINE")
internal inline fun <T> seqMut(vararg values: T): MutableList<T> = mutableListOf(*values)
@Suppress("NOTHING_TO_INLINE")
internal inline fun <T> alt(vararg values: T): List<T> = listOf(*values)


class PrefixFinder internal constructor(@Suppress("SameParameterValue") prefixTemplates: Alt<Seq<Alt<Seq<String>>>>) {

    constructor() : this(prepareTemplates(possibleNonRelevantForSortingPrefixTemplates)) { }

    private val expressionsTree: PrefixWordTreeNode = PrefixWordTreeNode("")

    init {
        buildTreeImpl(prefixTemplates)
    }

    fun removePrefix(phrase: String): String {
        val prefix = findPrefix(phrase)
        return if (prefix == null) phrase
               else phrase.trim().removeRepeatableSpaces().substring(prefix.length).trimStart()
    }

    fun calculateBaseOfFromForSorting(phrase: String): String {
        val fixedPhrase = phrase.lowercase().trim()

        val prefix = findPrefix(fixedPhrase)
            ?: return fixedPhrase

        var base = fixedPhrase.removeRepeatableSpaces().substring(prefix.length).trimStart()
        if (base.isNotBlank()) return base

        base = fixedPhrase.removePrefix("to be ").removePrefix("to ")
        if (base.isNotBlank()) return base

        return fixedPhrase
    }

    fun findPrefix(phrase: String): String? {

        val phraseFixed = phrase.lowercase().trim()

        var node = expressionsTree
        val words = phraseFixed.split(' ').filterNotBlank()

        val prefixNodes: MutableList<String> = mutableListOf()
        var lastPrefix: List<String>? = null

        for (w in words) {
            val wordNode = node.children.getChildByWord(w)
                ?: break

            prefixNodes.add(w)
            if (wordNode.canBeEnd) {
                lastPrefix = ArrayList(prefixNodes)
            }
            node = wordNode
        }

        return lastPrefix?.joinToString(" ")
    }

    companion object {
        @Suppress("SameParameterValue")
        private fun prepareTemplates(prefixTemplates: Alt<Seq<String>>): Alt<Seq<Alt<Seq<String>>>> {

            val preps: Alt<Seq<String>> = prepositions.map { it.words }
            // !!! seq("") MUST be at 1st position for optimization !!!
            val arts:  Alt<Seq<String>> = alt(seq("")) + articlesAndSimilar.map { it.split(' ') }
            val verbs: Alt<Seq<String>> = verbs.map { it.split(' ') }

            val preparedPrefixTemplates = prefixTemplates.map { prefixTemplate ->
                val prefixTemplateParts: Seq<Alt<Seq<String>>> = prefixTemplate.map {
                    when (it) {
                        "{prep}" -> preps
                        "{verb}" -> verbs
                        "{art}"  -> arts
                        else     -> listOf(listOf(it))
                    }
                }
                prefixTemplateParts
            }

            return preparedPrefixTemplates
        }
    }

    private fun buildTreeImpl(prefixTemplates: Alt<Seq<Alt<Seq<String>>>>) {
        for (prefixTemplate in prefixTemplates)
            addToTree(this.expressionsTree, prefixTemplate)
    }

    private fun addToTree(parentTreeNode: PrefixWordTreeNode, prefixTemplatePart: Seq<Alt<Seq<String>>>) {
        val currentTemplatePart: Alt<Seq<String>>? = prefixTemplatePart.firstOrNull()
        if (currentTemplatePart.isNullOrEmpty()) return

        for (wordsSequence: Seq<String> in currentTemplatePart) {

            val nextPrefixTemplatePart = prefixTemplatePart.skipFirst()
            if (wordsSequence.isEmpty()) {
                addToTree(parentTreeNode, nextPrefixTemplatePart)
                continue
            }

            val firstWord = wordsSequence.first()
            val wordsCount = wordsSequence.size

            val currentTemplatePartIsLast = (wordsCount == 1) &&
                    (nextPrefixTemplatePart.isEmpty() || nextPrefixTemplatePart.containsEmptyWord())

            if (wordsCount == 1 && firstWord.isEmpty()) {
                addToTree(parentTreeNode, nextPrefixTemplatePart)
                continue
            }

            var wordNode: PrefixWordTreeNode? = parentTreeNode.children.getChildByWord(firstWord)
            if (wordNode == null) {
                wordNode = PrefixWordTreeNode(firstWord)
                parentTreeNode.children.add(wordNode)
            }

            if (currentTemplatePartIsLast)
                wordNode.canBeEnd = true

            val remainedWords: Seq<String> = wordsSequence.skipFirst()

            val prefixTemplatePart1: Seq<Alt<Seq<String>>> =
                if (remainedWords.isEmpty()) nextPrefixTemplatePart
                //else seqMut(alt(remainedWords)).also { it.addAll(nextPrefixTemplatePart) } // It does not give performance benefits
                else seq(alt(remainedWords)) + nextPrefixTemplatePart

            addToTree(wordNode, prefixTemplatePart1)
        }
    }
}


private fun Seq<Alt<Seq<String>>>.containsEmptyWord(): Boolean {

    // Seems this short version is a bit slower... but I'm not totally sure.
    //return this.any { altValues: Alt<Seq<String>> -> altValues.any { words: Seq<String> -> words.isEmpty() || words[0].isEmpty() } }

    val it = this.iterator()
    if (!it.hasNext()) return false

    while (it.hasNext()) {
        val altValues: Alt<Seq<String>> = it.next()
        val containsEmpty: Boolean = altValues.any { words: Seq<String> -> words.isEmpty() || words[0].isEmpty() }
        if (!containsEmpty)
            return false
    }

    return true
}

private fun String.removeRepeatableSpaces(): String {
    //TODO: ("Not yet implemented")
    return this
}

// TODO: try to optimize it or collection type
private fun Collection<PrefixWordTreeNode>.getChildByWord(word: String): PrefixWordTreeNode? =
    this.find { it.word == word }
//private fun Set<PrefixWordTreeNode>.getChildByWord(word: String): PrefixWordTreeNode? =
//    this.find { it.word == word }
//private fun SortedSet<PrefixWordTreeNode>.getChildByWord(word: String): PrefixWordTreeNode? =
//    this.find { it.word == word }

private val emptyTreeSet: TreeSet<PrefixWordTreeNode> = TreeSet<PrefixWordTreeNode>()
private fun TreeSet<PrefixWordTreeNode>.getChildByWord(word: String): PrefixWordTreeNode? {
    val searchKey = PrefixWordTreeNode(word, false) //, emptyTreeSet)
    // TODO: 2 iterations, not good !!!
    val ceil  = this.ceiling(searchKey)  // least elt >= key
    val floor = this.floor(searchKey)    // highest elt <= key
    return if (ceil === floor) ceil else null
}



private val possibleNonRelevantForSortingPrefixTemplates: List<List<String>> = listOf(
    "to {verb} to {prep} {art}",
    "to {verb} {prep} {art}",
    "to {verb} to {art}",
    "to {verb} {art}",

    "not to be {art}",
    "not to {art}",
    "to {art}",

    "even with {art}",
    "{prep} {art}",
    "on no",

    "to {art}",
    "not to {art}",
    "not {prep} {art}",
    "not {art}",

    "by all {art}",
    "all {art}",
    "and {art}",
    "or {art}",

    "{art} no",
    "{art}",
    "no",

    "beyond all", "at short", "only",

    "what is the", "what's the",
)
    .distinct()
    .map { it.split(' ').filterNotBlank() }



private val verbs = listOf(
    "do", "be", "have", "have no", "get", "go", "make", "make no", "take", "give", "bring", "handle", "try",
    "allow", "answer", "ask", "call", "carry", "come", "keep", "lack",
)

private val articlesAndSimilar = listOf(
    "one's", "ones", "one", "smb's.", "smb's", "smbs'", "smbs", "smb.", "smb",
    "every", "one", "your", "mine", "one's own",
    "front", "the next", "next",
    "a short", "a long", "a high", "a low", "a full",
    "short", "low", "thin", "long", "high", "full", "tall", "thick",
    "a bad", "bad", "worse", "the worst",
    "a good", "good", "better", "the best",
    "a hard", "hard", "harder",
    "the public","a public", "public",
    "enough", "a common", "common",
    "this", "that",
    "it",
    "the", "an", "a",
)
