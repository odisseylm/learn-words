
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


class PrefixFinder
    internal constructor(@Suppress("SameParameterValue") prefixTemplates: Alt<Seq<Alt<Seq<String>>>>, val ignoredInPrefix: Set<String>,
                         @Suppress("UNUSED_PARAMETER") ignoredParam: Boolean)

    /*: AbstractObservable<Any>()*/ {

    constructor(toIgnoreInPrefix: Set<String> = emptySet())
            : this(possibleNonRelevantForSortingPrefixTemplates, toIgnoreInPrefix)

    constructor(templates: Alt<Seq<String>>, toIgnoreInPrefix: Set<String> = emptySet())
            : this(prepareTemplates(templates, toIgnoreInPrefix), toIgnoreInPrefix, false)

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

        //var node = expressionsTree
        val words = phraseFixed.split(' ').filterNotBlank()

        return TODO() // findPrefix2(words, expressionsTree, emptyList(), null)

        /*
        val prefixNodes: MutableList<String> = mutableListOf()
        var lastPrefix: List<String>? = null

        //for (w in words) {
        for (i in words.indices) {
            val w = words[i]
            val wordNodes = node.getWordNodes(w)

            /*
            var wordNode = node.getChildByWord(w) // T O D O: !!! we need to perform search by also by delegate/shared and return the best result

            // T  O D O: !!! we need to perform search by also by delegate/shared and return the best result

            val nodeHasChildren = node.children.isNotEmpty()
            if (nodeHasChildren) {
                if (wordNode == null)
                    wordNode = node.getChildByWord(delegateNodeName1)?.getChildByWord(w)
                if (wordNode == null)
                    wordNode = node.getChildByWord(delegateNodeName2)?.getChildByWord(w)
                if (wordNode == null)
                    wordNode = node.getChildByWord(delegateNodeName3)?.getChildByWord(w)

                if (wordNode == null) {
                    val lastCanBeEnd: Boolean =
                        (node.getChildByWord(delegateNodeName1)?.canBeEnd ?: false) ||
                        (node.getChildByWord(delegateNodeName2)?.canBeEnd ?: false) ||
                        (node.getChildByWord(delegateNodeName3)?.canBeEnd ?: false) ||
                        (node.getChildByWord(delegateNodeName1)?.getChildByWord("")?.canBeEnd ?: false) ||
                        (node.getChildByWord(delegateNodeName2)?.getChildByWord("")?.canBeEnd ?: false) ||
                        (node.getChildByWord(delegateNodeName3)?.getChildByWord("")?.canBeEnd ?: false)

                    if (lastCanBeEnd) {
                        lastPrefix = ArrayList(prefixNodes)
                    }

                    break
                }
            }
            */

            if (wordNodes.isEmpty())
                break
            else if (wordNodes.size == 1) {
                val wordNode = wordNodes[0]

                prefixNodes.add(w)
                if (wordNode.canBeEnd) {
                    lastPrefix = ArrayList(prefixNodes)
                }
                node = wordNode
            }
            else {
                return wordNodes
                    .asSequence()
                    .map { findPrefix2(words.subList(i, words.size), node, prefixNodes, lastPrefix) }
                    .filterNotNull()
                    .sortedBy { it.length }
                    .firstOrNull()
            }
        }

        return lastPrefix?.joinToString(" ")
        */
    }

    /*
    private fun findPrefix2(
        remainedWords: Seq<String>,
        findFromNode: PrefixWordTreeNode,
        parentPrefixNodes: List<String>,
        parentLastPrefix: List<String>?,
    ): String? {

        var node = findFromNode
        var prefixNodes: MutableList<String>? = null
        var lastPrefix: List<String>? = parentLastPrefix

        for (i in remainedWords.indices) {
            val w = remainedWords[i]
            val wordNodesData = node.getWordNodesData(w)
            val wordNodes = wordNodesData.first
            //can = wordNodesData.first


            prefixNodes.add(w)
            if (wordNodesData.second) {
                lastPrefix = ArrayList(prefixNodes ?: parentPrefixNodes)
            }

            if (wordNodes.isEmpty())
                break
            else if (wordNodes.size == 1) {
                val wordNode = wordNodes[0]

                if (prefixNodes == null)
                    prefixNodes = ArrayList(parentPrefixNodes)

                prefixNodes.add(w)

                //if (wordNode.canBeEnd) {
                //    lastPrefix = ArrayList(prefixNodes)
                //}
                node = wordNode
            }
            else {
                return wordNodes
                    .asSequence()
                    .map { findPrefix2(remainedWords.subList(i, remainedWords.size), node, prefixNodes ?: emptyList(), lastPrefix) }
                    .filterNotNull()
                    .sortedBy { it.length }
                    .firstOrNull()
            }
        }

        return lastPrefix?.joinToString(" ")
    }
    */

    private fun buildTreeImpl(prefixTemplates: Alt<Seq<Alt<Seq<String>>>>): PrefixWordTreeNode {
        val sw = startStopWatch("${this.javaClass.simpleName}.buildTree()")

        val context = BuildContext(this.ignoredInPrefix)

        val rootNode = PrefixWordTreeNode("")
        for (prefixTemplate in prefixTemplates)
            addToTree(rootNode, prefixTemplate, context)

        sw.logInfo(log)
        return rootNode
    }

    /*
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
    */


    companion object {
        @Suppress("SameParameterValue")
        private fun prepareTemplates(prefixTemplates: Alt<Seq<String>>, toIgnoreInPrefix: Iterable<String>): Alt<Seq<Alt<Seq<String>>>> {

            val toIgnoreInPrefixLC = toIgnoreInPrefix.map { it.lowercase() }.toSet()

            val preps: Alt<Seq<String>> = prepositions.map { it.words }.excludeIgnoredWords(toIgnoreInPrefixLC)
            // !!! seq("") MUST be at 1st position for optimization !!!
            val arts:  Alt<Seq<String>> = alt(seq("")) + articlesAndSimilar.map { it.split(' ') }.excludeIgnoredWords(toIgnoreInPrefixLC)
            val verbs: Alt<Seq<String>> = verbs.map { it.split(' ') }.excludeIgnoredWords(toIgnoreInPrefixLC)

            val prefixTemplates01 = prefixTemplates.map { it.replaceLastNodesWithShared() }

            val preparedPrefixTemplates = prefixTemplates01.map { prefixTemplate ->
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
    //"to {verb} to {prep} {art}",
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

private val articlesAndSimilar = listOf(
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


private const val delegateNodeNamePrefix = "->"
private const val delegateNodeName  = delegateNodeNamePrefix
private const val delegateNodeName1 = "${delegateNodeNamePrefix}a"
private const val delegateNodeName2 = "${delegateNodeNamePrefix}p"
private const val delegateNodeName3 = "${delegateNodeNamePrefix}pa"
private val delegateNodeNames: Array<String> = arrayOf(delegateNodeName1, delegateNodeName2, delegateNodeName3)

private class BuildContext (toIgnoreInPrefix: Set<String>) {
    val toIgnoreInPrefixLC = toIgnoreInPrefix.map { it.lowercase() }.toSet()

    val prepsAsAlt: Alt<Seq<String>> = prepositions.map { it.words }
        .excludeIgnoredWords(toIgnoreInPrefixLC)
    // !!! seq("") MUST be at 1st position for optimization !!!
    val artsAsAlt:  Alt<Seq<String>> = alt(seq("")) + articlesAndSimilar.map { it.split(' ') }
        .excludeIgnoredWords(toIgnoreInPrefixLC)
    val verbsAsAlt: Alt<Seq<String>> = verbs.map { it.split(' ') }
        .excludeIgnoredWords(toIgnoreInPrefixLC)

    // {art-node}
    val artsAsTree  = buildSharedTree(PrefixWordTreeNode(delegateNodeName1, true), alt(seq(artsAsAlt)))
    // {preps-node}
    val prepsAsTree = buildSharedTree(PrefixWordTreeNode(delegateNodeName2, true), alt(seq(prepsAsAlt)))
    // {prep-art-node}
    val prepsAndArtsAsTree = buildSharedTree(PrefixWordTreeNode(delegateNodeName3, true), alt(seq(prepsAsAlt)))
        .also { addLastNode(it, artsAsTree) }
}


private inline val PrefixWordTreeNode.isDelegate: Boolean get() = this.word.startsWith(delegateNodeNamePrefix)

private fun addLastNode(treeNode: PrefixWordTreeNode, lastLeafToAdd: PrefixWordTreeNode) {
    if (treeNode.children.isEmpty() || treeNode.canBeEnd) {
        treeNode.addTreeNode(lastLeafToAdd)
    }

    for (childNode in treeNode.children) {
        if (lastLeafToAdd.isDelegate && childNode.value.word == lastLeafToAdd.word)
            continue

        addLastNode(childNode.value, lastLeafToAdd)
    }
}


private fun buildSharedTree(rootNode: PrefixWordTreeNode, toAdd: Alt<Seq<Alt<Seq<String>>>>/*, markLastLeafAsCanBeEnd: Boolean*/): PrefixWordTreeNode {
    //val sw = startStopWatch("${this.javaClass.simpleName}.buildTree()")

    //val rootNode = PrefixWordTreeNode("")
    for (prefixTemplate in toAdd)
        addToTree(rootNode, prefixTemplate, null)

    //sw.logInfo(log)
    return rootNode
}


private fun Seq<String>.replaceLastNodesWithShared(): Seq<String> {
    // "to {verb} to {prep} {art}",
    // "to {verb} {prep} {art}",
    // "to {verb} to {art}",
    // "{prep} {art}"
    // "{art}"
    // "{prep}"
    //val rev = this.asReversed()

    val last = this.getOrNull(this.size - 1)
    val preLast = this.getOrNull(this.size - 2)
    val prePreLast = this.getOrNull(this.size - 3)

    return when {
        /*prePreLast.isNullOrExpr &&*/ preLast == "{prep}" && last == "{art}" -> {
            val changedSeq = ArrayList(this)
            changedSeq.removeLast()
            changedSeq[changedSeq.lastIndex] = "{prep-art-node}"
            changedSeq
        }
        /*preLast.isNullOrExpr &&*/ last == "{art}" -> {
            val changedSeq = ArrayList(this)
            changedSeq[changedSeq.lastIndex] = "{art-node}"
            changedSeq
        }
        else -> this
    }
}

private inline val String.isExpr get() = this.length > 2 && this[0] == '{' && this[this.lastIndex] == '}'
private inline val String?.isNullOrExpr get() = this== null || this.isExpr

private fun addToTree(parentTreeNode: PrefixWordTreeNode, prefixTemplatePart: Seq<Alt<Seq<String>>>, context: BuildContext?) {
    val currentTemplatePart: Alt<Seq<String>>? = prefixTemplatePart.firstOrNull()
    if (currentTemplatePart.isNullOrEmpty()) {
        parentTreeNode.canBeEnd = true
        return
    }

    for (wordsSequence: Seq<String> in currentTemplatePart) {

        val nextPrefixTemplatePart = prefixTemplatePart.skipFirst()
        if (wordsSequence.isEmpty()) {
            addToTree(parentTreeNode, nextPrefixTemplatePart, context)
            continue
        }

        val firstWord = wordsSequence[0]
        val wordsCount = wordsSequence.size

        if (wordsCount == 1 && context != null) {
            if (firstWord == "{prep-art-node}") {
                parentTreeNode.addTreeNode(context.prepsAndArtsAsTree)
                continue
            } else if (firstWord == "{prep-node}") {
                parentTreeNode.addTreeNode(context.prepsAsTree)
                continue
            } else if (firstWord == "{art-node}") {
                parentTreeNode.addTreeNode(context.artsAsTree)
                continue
            }
        }

        val currentTemplatePartIsLast = (wordsCount == 1) &&
                (nextPrefixTemplatePart.isEmpty() || nextPrefixTemplatePart.containsEmptyWord())

        if (wordsCount == 1 && firstWord.isEmpty()) {
            addToTree(parentTreeNode, nextPrefixTemplatePart, context)
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

        addToTree(wordNode, prefixTemplatePart1, context)
    }
}


private fun PrefixWordTreeNode.getWordNodesData(word: String): Pair<List<PrefixWordTreeNode>, Boolean> {
    val wordNodes = ArrayList<PrefixWordTreeNode>(4)
    var canBeEnd = false


    val wordNode = this.getChildByWord(word)
    if (wordNode != null) {
        wordNodes.add(wordNode)
        canBeEnd = wordNode.canBeEnd
    }

    val nodeHasChildren = this.children.isNotEmpty()
    if (nodeHasChildren) {
        for (delegateNodeName in delegateNodeNames) {
            val delegateChildNode = this.getChildByWord(delegateNodeName)
            if (delegateChildNode != null) {
                canBeEnd = canBeEnd || delegateChildNode.canBeEnd
                wordNodes.addNotNull(delegateChildNode.getChildByWord(word))
            }
        }
    }

    return Pair(wordNodes, canBeEnd)
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






private class SuperPuperTreeNode {
    val children: List<SuperPuperTreeNode> = TODO()
    val sharedNode: PrefixWordTreeNode = TODO()
}



