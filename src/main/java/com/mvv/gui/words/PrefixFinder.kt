package com.mvv.gui.words

import com.mvv.gui.util.*
import org.apache.commons.lang3.NotImplementedException
import org.apache.commons.lang3.math.NumberUtils.max
import java.util.Collections.singletonList
import java.util.function.BiFunction
import java.util.function.Function


private val log = mu.KotlinLogging.logger {}


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


/**
 * Simple (but optimized) tree based a prefix finder.
 * It performs very fast search.
 * But creation is very expensive because too many tree nodes are created.
 * As optimization shared trees (tree nodes) for ENDING '{prep} {art}','{prep}','{art}' can be used.
 *
 * But now there is another new PrefixFinder impl which can use (and uses) shared trees (for '{prep}','{art}', '{verb}', so on) at any place,
 * and because of using shared trees it recreates full search tree very fast.
 *
 * Of course search by new impl can be a bit slower than PrefixFinder_Old, since it uses much more complicated algorithm (with delegation and recursion).
 * BUT test show a bit another unexpected result:
 *  search by PrefixFinder_Old = 0.0632ms, search by new PrefixFinder = 0.015ms !?!, and I cannot explain this strange unlogical result!
 */
class PrefixFinder_Old internal constructor(@Suppress("SameParameterValue") prefixTemplates: Alt<Seq<Alt<Seq<String>>>>, val ignoredInPrefix: Set<String> = emptySet()) /*: AbstractObservable<Any>()*/ {

    constructor(toIgnoreInPrefix: Set<String> = emptySet())
            : this(prepareTemplates(possibleNonRelevantForSortingPrefixTemplates, toIgnoreInPrefix), toIgnoreInPrefix)

    private val expressionsTree: PrefixWordTreeNode = buildTreeImpl(prefixTemplates)

    ///** For internal usage only to support Observable change listeners. */
    //override fun getValue(): Any = expressionsTree
    //
    //// !!! This method uses only predefined prefix templates and IGNORES
    //fun rebuildPrefixTree(toIgnoreInPrefix: Set<String>) {
    //    expressionsTree = buildTreeImpl(prepareTemplates(possibleNonRelevantForSortingPrefixTemplates, toIgnoreInPrefix))
    //}

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
            .removeRepeatableSpaces()
            .removeCharSuffixesRepeatably("!.?…")
            .trimEnd()

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

    private fun buildTreeImpl(prefixTemplates: Alt<Seq<Alt<Seq<String>>>>): PrefixWordTreeNode {
        val sw = startStopWatch("${this.javaClass.simpleName}.buildTree()")

        val rootNode = PrefixWordTreeNode("")
        for (prefixTemplate in prefixTemplates)
            addToTree(rootNode, prefixTemplate)

        sw.logInfo(log)
        return rootNode
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
        private fun prepareTemplates(prefixTemplates: Alt<Seq<String>>, toIgnoreInPrefix: Iterable<String>): Alt<Seq<Alt<Seq<String>>>> {

            val toIgnoreInPrefixLC = toIgnoreInPrefix.map { it.lowercase() }.toSet()

            val preps: Alt<Seq<String>> = prepositions.map { it.words }.excludeIgnoredWords(toIgnoreInPrefixLC)
            // !!! seq("") MUST be at 1st position for optimization !!!
            val arts:  Alt<Seq<String>> = alt(seq("")) + articlesAndSimilar.map { it.split(' ') }.excludeIgnoredWords(toIgnoreInPrefixLC)
            val verbs: Alt<Seq<String>> = verbs.map { it.split(' ') }.excludeIgnoredWords(toIgnoreInPrefixLC)

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


private fun Alt<Seq<String>>.excludeIgnoredWords(toIgnoreWords: Set<String>): Alt<Seq<String>> =
    this.filterNot { it.any { word -> word in toIgnoreWords } }


private val possibleNonRelevantForSortingPrefixTemplates: Alt<Seq<String>> = sequenceOf(
    "to {verb} to {prep} {art}",
    "to {verb} {prep} {art}",
    "to {verb} to {art}",
    "to {verb} {art}",

    /*
    "{verb} to {prep} {art}",
    "{verb} {prep} {art}",
    "{verb} to {art}",
    "{verb} {art}",

    "he {verb} to {prep} {art}",
    "he {verb} {prep} {art}",
    "he {verb} to {art}",
    "he {verb} {art}",

    "it {verb} to {prep} {art}",
    "it {verb} {prep} {art}",
    "it {verb} to {art}",
    "it {verb} {art}",
    */

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
    "all of a", "all on",

    "what a", "what the",
    "what's a", "what's the",
    "what is a", "what is the",

    "who",
)
    .distinct()
    .map { it.split(' ').filterNotBlank() }
    .toList()


/**
 *  It contains only verbs which can be used very often.
 *  Do NOT include too many words there, because it may degrade performance of PrefixFinder creation.
 */
private val verbs = listOf(
    "do", "does", "be", "is", "have", "has", "have no", "has no", "get", "gets", "go", "goes",
    "can", "could", "may", "might", "must", "shall", "should",

    "add", "allow", "answer", "appear", "ask",
    "become", "begin", "believe", "break", "bring", "build", "buy",
    "call", "carry", "change", "clean", "close",
    "come", "consider", "continue", "cook", "count", "cost", "cover", "create", "cut",
    "dance", "decide", "die", "dream", "drink", "drive",
    "eat", "expect", "explain",
    "fall", "feel", "find", "finish", "flow", "fly", "follow", "forget",
    "gain", "get", "give", "grow",
    "handle", "happen", "hang", "hear", "help", "hold",
    "include",
    "keep", "kill", "know",
    "lack", "lead", "learn", "leave", "let", "like", "live", "listen", "lose", "look", "love",
    "make", "make no", "mean", "meet", "move",
    "need",
    "offer", "open",
    "pay", "pass", "pick", "play", "provide", "pull", "put",
    "raise", "require", "relax", "remain", "remember", "report", "ride", "run", "reach", "read",
    "say", "see", "seem", "sell", "send", "serve", "set", "show", "sing", "sit", "ski",
    "sleep", "speak", "spend", "stand", "start", "stay", "stop", "swim", "suggest",
    "take", "talk", "tell", "think", "try",
    "tell", "think", "try to keep", "teach", "turn", //"travel",
    "understand", "use",
    "visit",
    "wait", "walk", "want", "watch", "win", "work", "write", "would",
)

/*private*/ val articlesAndSimilar = listOf(
    "one's", "ones", "one",
    "smb's.", "smb's", "smbs'", "smbs", "smb.", "smb",
    "smb's. a", "smb's a", "smbs' a", "smbs a", "smb. a", "smb a",
    "smb's. the", "smb's the", "smbs' the", "smbs the", "smb. the", "smb the",
    "every", "one", "your", "mine", "one's own",
    "daily",
    "forbidden",
    "slightly",
    "front", "the next", "next",
    "a short", "short",
    "a long", "long",
    "a high", "high",
    "a low", "low",
    "a full", "full",
    "a big", "big", "a large", "large", "a small", "small", "a tiny", "tiny",
    "a tall", "tall",
    "a thick", "thick", "a thin", "thin",
    "a bad", "bad", "worse", "the worst",
    "a good", "good", "better", "the best",
    "a hard", "hard", "harder",
    "a great", "great",
    "a half", "half",
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
