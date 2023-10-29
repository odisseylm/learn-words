package com.mvv.gui.words

import com.mvv.gui.util.filterNotBlank
import com.mvv.gui.util.skipFirst
import org.apache.commons.lang3.NotImplementedException
import org.apache.commons.lang3.math.NumberUtils.max
import java.util.Collections.singletonList
import java.util.function.BiFunction
import java.util.function.Function


//private val log = mu.KotlinLogging.logger {}


internal typealias Alt<T> = List<T> // Alt = alternative (OR) values of words/phrases/sequences
internal typealias Seq<T> = List<T> // Seq = sequence of words or alt values or other sequences or alt sequences, so on

@Suppress("NOTHING_TO_INLINE", "SameParameterValue")
private inline fun <T> seq(value: T): Seq<T> = singletonList(value)
@Suppress("NOTHING_TO_INLINE")
private inline fun <T> seq(first: T, other: Seq<T>): Seq<T> {
    val l = ArrayList<T>(1 + other.size)
    l.add(first)
    l.addAll(other)
    return l
}
@Suppress("NOTHING_TO_INLINE")
private inline fun <T> alt(value: T): Alt<T> = singletonList(value)


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
            val wordNode = node.getChildByWord(w)
                ?: break

            prefixNodes.add(w)
            if (wordNode.canBeEnd) {
                lastPrefix = ArrayList(prefixNodes)
            }
            node = wordNode
        }

        return lastPrefix?.joinToString(" ")
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

            val firstWord = wordsSequence[0]
            val wordsCount = wordsSequence.size

            val currentTemplatePartIsLast = (wordsCount == 1) &&
                    (nextPrefixTemplatePart.isEmpty() || nextPrefixTemplatePart.containsEmptyWord())

            if (wordsCount == 1 && firstWord.isEmpty()) {
                addToTree(parentTreeNode, nextPrefixTemplatePart)
                continue
            }

            var wordNode: PrefixWordTreeNode? = parentTreeNode.getChildByWord(firstWord)
            if (wordNode == null) {
                wordNode = PrefixWordTreeNode(firstWord)
                parentTreeNode.addTreeNode(wordNode)
            }

            if (currentTemplatePartIsLast)
                wordNode.canBeEnd = true

            val remainedWords: Seq<String> = wordsSequence.skipFirst()

            val prefixTemplatePart1: Seq<Alt<Seq<String>>> =
                if (remainedWords.isEmpty()) nextPrefixTemplatePart
                else seq(alt(remainedWords), nextPrefixTemplatePart)

            addToTree(wordNode, prefixTemplatePart1)
        }
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
                        else     -> alt(seq(it))
                    }
                }
                prefixTemplateParts
            }

            return preparedPrefixTemplates
        }
    }

}


// !!! inlined version comparing with standard kotlin one !!!
@Suppress("NOTHING_TO_INLINE")
private inline fun <T> List<T>.firstOrNull(): T? = if (isEmpty()) null else this[0]


private fun Seq<Alt<Seq<String>>>.containsEmptyWord(): Boolean {

    // Seems this short version is a bit slower... but I'm not totally sure.
    //return this.any { altValues: Alt<Seq<String>> -> altValues.any { words: Seq<String> -> words.isEmpty() || words[0].isEmpty() } }

    val it = this.iterator()
    if (!it.hasNext()) return false

    while (it.hasNext()) {
        val altValues: Alt<Seq<String>> = it.next()
        val containsEmpty: Boolean = altValues.any { words: Seq<String> ->
            words.isEmpty() ||
            // !!! empty string MUST be FIRST !!!
            words[0].isEmpty()
        }

        if (!containsEmpty)
            return false
    }

    return true
}

private fun String.removeRepeatableSpaces(): String {
    //TODO: ("Not yet implemented")
    return this
}


private val possibleNonRelevantForSortingPrefixTemplates: Alt<Seq<String>> = sequenceOf(
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
    .toList()



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



private val maxExpectedNodesCount = max(verbs.size, articlesAndSimilar.size, prepositions.size)


private class PrefixWordTreeNode ( // PrefixWord
    val word: String,
    var canBeEnd: Boolean = false,

    // Seems HashMap are the best/fastest from HashMap/TreeMap/ArrayList (seems TreeMap is the slowest).
    // Explicit declaration type (HashMap instead of MutableMap) is used to allow JIT to do the best optimization.
    //
    var children: HashMap<String, PrefixWordTreeNode> = emptyNodesMap
) {
    override fun toString(): String = "Tree { $word ${if (canBeEnd) ", can be end " else ""}, children: ${children.size} }"

    @Suppress("NOTHING_TO_INLINE")
    inline fun getChildByWord(word: String): PrefixWordTreeNode? = children.getChildByWordImpl(word)

    @Suppress("NOTHING_TO_INLINE")
    inline fun addTreeNode(node: PrefixWordTreeNode) {
        if (this.children === emptyNodesMap)
            // This optimization really adds a bit of performance (5%).
            //  1) Empty map class is final with fast final methods (with simple/constant method body, probably inlinable)
            //  2) Really new HashMap instance is created only if/when it is needed
            this.children = HashMap(maxExpectedNodesCount)

        this.children.addTreeNodeImpl(node)
    }
}


//@Suppress("NOTHING_TO_INLINE")
//private inline fun <L: Collection<PrefixWordTreeNode>> L.getChildByWordImpl(word: String): PrefixWordTreeNode? = this.find { it.word == word }
//@Suppress("NOTHING_TO_INLINE")
//private inline fun <L: MutableCollection<PrefixWordTreeNode>> L.addTreeNodeImpl(node: PrefixWordTreeNode) { this.add(node) }

@Suppress("NOTHING_TO_INLINE")
private inline fun <M: Map<String, PrefixWordTreeNode>> M.getChildByWordImpl(word: String): PrefixWordTreeNode? = this[word]
@Suppress("NOTHING_TO_INLINE")
private inline fun <M: MutableMap<String, PrefixWordTreeNode>> M.addTreeNodeImpl(node: PrefixWordTreeNode) { this[node.word] = node }


private val emptyNodesMap: HashMap<String, PrefixWordTreeNode> = object : HashMap<String, PrefixWordTreeNode>() {
    override fun get(key: String): PrefixWordTreeNode? = null
    override fun getOrDefault(key: String, defaultValue: PrefixWordTreeNode): PrefixWordTreeNode = defaultValue
    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun put(key: String, value: PrefixWordTreeNode): PrefixWordTreeNode = throw NotImplementedException()
    override fun putIfAbsent(key: String, value: PrefixWordTreeNode): PrefixWordTreeNode = throw NotImplementedException()
    override fun putAll(from: Map<out String, PrefixWordTreeNode>) = throw NotImplementedException()
    override fun compute(key: String, remappingFunction: BiFunction<in String, in PrefixWordTreeNode?, out PrefixWordTreeNode?>): PrefixWordTreeNode = throw NotImplementedException()
    override fun computeIfAbsent(key: String, mappingFunction: Function<in String, out PrefixWordTreeNode>): PrefixWordTreeNode = throw NotImplementedException()
    override fun computeIfPresent(key: String, remappingFunction: BiFunction<in String, in PrefixWordTreeNode, out PrefixWordTreeNode?>): PrefixWordTreeNode = throw NotImplementedException()
}
