package com.mvv.gui.words

import com.mvv.gui.util.*


enum class Direction { Forward, Backward }


/**
 * Complicated tree based a prefix finder which uses shared subtrees
 * for verbs, pronouns, prepositions, articles-and-others.
 * Such approach probably makes search a bit slower,
 * but it allows to build tree (or rebuild to exclude 'ignored' words) very quickly.
 * For using shared trees we need wrap shared trees with delegate tree node.
 * If we successfully completed search by all leafs of shared tree
 * we continue search using child nodes of delegate tree node.
 * Variable/param 'toContinueFromNodeAfterShared' is used to keep delegate tree node reference
 * while we follow over leaves of shared tree.
 */
open class SubSequenceFinder (
    patternTemplates: Alt<Seq<String>>,
    val direction: Direction,
    val ignoredWords: Collection<String>,
    languageRules: SubSequenceLanguageRules,
) {

    private val rootNode: TreeNode = buildPrefixesMatchTree(patternTemplates, direction, ignoredWords, languageRules)

    fun findMatchedSubSequence(phrase: String): String? =
        rootNode.findMatchedSubSequence(phrase, direction)

    fun removeMatchedSubSequence(phrase: String): String {
        val matchedSubSequenceStr = findMatchedSubSequence(phrase)
            ?: return phrase

        val s = phrase.trim().removeRepeatableSpaces()
        return when (direction) {
            Direction.Forward  -> s.substring(matchedSubSequenceStr.length).trimStart()
            Direction.Backward -> s.substring(0, s.length - matchedSubSequenceStr.length).trimEnd()
        }
    }

}


// The shortest IDs to minimize equals.
// These nodes are 'word' of root node of shared readonly trees (readonly because they are shared).
internal const val verbsSharedNodeName = "{v}"
internal const val artsSharedNodeName = "{a}"
internal const val prepositionsSharedNodeName = "{p}"
internal const val pronounsSharedNodeName = "{pp}"
//internal val sharedNodeNames: Array<String> = arrayOf(verbsSharedNodeName, artsSharedNodeName, prepositionsSharedNodeName, pronounsSharedNodeName)



// "to {verb}" // for this/similar case we need to use canBeEnd
// "to {verb} to"
// "to {verb} {art}"
//
internal class SharedWrapper (
    val sharedTree: TreeNode,
    ) {
    val sharedId: String = sharedTree.word
}

enum class NodeType {
    SimpleNode,
    CanBeEndOfPrefix,
    CanBeEndOfShared,
}

internal class TreeNode private constructor (
    val word: String, // in case of wrapping shared tree it is ID of shared tree
    val shared: SharedWrapper?,
    var canBeEnd: NodeType = NodeType.SimpleNode,
    private val children: MutableMap<String, TreeNode> = HashMap(),
    ) {

    constructor(word: String) : this(word, null)
    constructor(shared: SharedWrapper) : this(shared.sharedId, shared)

    override fun toString(): String = "TreeNode { '$word'" +
            (if (shared != null) ", is shared" else "") +
            (if (canBeEnd !== NodeType.SimpleNode) ", $canBeEnd" else "") +
            " }"

    val asNextNode: TreeNode? = if (shared != null) TreeNode(word, null, this.canBeEnd, this.children) else null

    internal fun addChildNode(word: String) =
        this.children.computeIfAbsent(word) { TreeNode(word) }
    internal fun addChildNode(shared: SharedWrapper) =
        this.children.computeIfAbsent(shared.sharedId) { TreeNode(shared) }
    internal fun addChildNode(word: String, canBeEnd: NodeType): TreeNode =
        this.children.computeIfAbsent(word) { TreeNode(word) }.also { if (canBeEnd !== NodeType.SimpleNode) it.canBeEnd = canBeEnd }
    internal fun addChildNode(shared: SharedWrapper, canBeEnd: NodeType): TreeNode =
        this.children.computeIfAbsent(shared.sharedId) { TreeNode(shared) }.also { if (canBeEnd !== NodeType.SimpleNode) it.canBeEnd = canBeEnd }
    internal fun getChildNode(word: String): TreeNode? = this.children[word]
    internal fun hasChildNodes(): Boolean = this.children.isNotEmpty()

    fun findChildNodes(word: String): List<TreeNodeRes> {

        if (children.isEmpty()) return emptyList()

        val child1 = children[word]
        val child1Res = child1?.let { TreeNodeRes(it, null) }

        // We can use there list, but I decide to unwrap possible loops to improve performance.
        val delegate1 = children[artsSharedNodeName]
        val delegate2 = children[verbsSharedNodeName]
        val delegate3 = children[prepositionsSharedNodeName]
        val delegate4 = children[pronounsSharedNodeName]

        val sharedRes1 = delegate1?.sharedChildAsTreeNodeRes(word)
        val sharedRes2 = delegate2?.sharedChildAsTreeNodeRes(word)
        val sharedRes3 = delegate3?.sharedChildAsTreeNodeRes(word)
        val sharedRes4 = delegate4?.sharedChildAsTreeNodeRes(word)

        return listOfNonNulls(child1Res, sharedRes1, sharedRes2, sharedRes3, sharedRes4)
    }
}


internal class TreeNodeRes (
    val node: TreeNode,
    val nextNode: TreeNode?,
) {
    override fun toString(): String = "TreeNodeRes { $node ${ if (nextNode != null) ", next: $nextNode" else "" } }"
}


internal fun TreeNode?.sharedChildAsTreeNodeRes(word: String): TreeNodeRes? {
    if (this == null) return null

    val childNode = this.shared!!.sharedTree.getChildNode(word)
    return if (childNode == null) null
           else TreeNodeRes(childNode, this.asNextNode)
}


internal fun TreeNode.findMatchedSubSequence(phrase: String, direction: Direction): String? {

    var phraseFixed = phrase
        .lowercase().trim()
        .removeRepeatableSpaces()
        .removeCharSuffixesRepeatably("!?…")
        .trimEnd()

    val lastWord = phraseFixed.lastWord()
        ?: return null

    if (lastWord.endsWith('.')) {
        val matchedLastWord = doSearchMatchedPrefixSequence(listOf(lastWord), this, null, null, null)
        val lastWordIsSpecial = matchedLastWord.isNotEmpty()
        if (!lastWordIsSpecial) {
            phraseFixed = phraseFixed.removeCharSuffixesRepeatably(".")
        }
    }

    val words = phraseFixed.splitToWords()
        .let { if (direction == Direction.Backward) it.reversed() else it }

    val matchedPrefixSequence = doSearchMatchedPrefixSequence(words, this, null, null, null)
    return matchedPrefixSequence
        .ifEmpty { null }
        ?.let { if (direction == Direction.Backward) it.reversed() else it }
        ?.joinToString(" ")
}

internal fun doSearchMatchedPrefixSequence(words: List<String>, initialRootNode: TreeNode, initialToContinueFromNodeAfterShared: TreeNode?,
                                           initialAllPrefixWords: List<String>?, initialMatchedLastPrefix: List<String>?): List<String> {

    var node = initialRootNode

    var allPrefixWords: MutableList<String>? = null
    var matchedLastPrefix: List<String>? = initialMatchedLastPrefix

    var toContinueFromNodeAfterShared: TreeNode? = initialToContinueFromNodeAfterShared

    for (i in words.indices) {

        val word = words[i]
        var childNodes: List<TreeNodeRes> = node.findChildNodes(word)

        // Last leaf.
        // If it is shared last leaf we need to continue processing nodes after shared.
        if (!node.hasChildNodes()) {

            if (toContinueFromNodeAfterShared == null) {
                matchedLastPrefix = allPrefixWords ?: initialAllPrefixWords

                return matchedLastPrefix ?: emptyList()
            }

            else {
                node = toContinueFromNodeAfterShared
                toContinueFromNodeAfterShared = null

                childNodes = node.findChildNodes(word)

                if (childNodes.isEmpty()) {
                    matchedLastPrefix = allPrefixWords ?: initialAllPrefixWords

                    return matchedLastPrefix ?: emptyList()
                }
            }
        }

        if (childNodes.isEmpty()) {

            if (toContinueFromNodeAfterShared != null) {
                if (node.canBeEnd === NodeType.CanBeEndOfShared) {
                    childNodes = toContinueFromNodeAfterShared.findChildNodes(word)
                    toContinueFromNodeAfterShared = null

                    if (childNodes.isEmpty())
                        // end of prefix (last tree node)
                        matchedLastPrefix = allPrefixWords ?: initialAllPrefixWords
                }
            }
            else
                // end of prefix (last tree node)
                matchedLastPrefix = allPrefixWords ?: initialAllPrefixWords

            // if is still empty
            if (childNodes.isEmpty())
                return (matchedLastPrefix ?: initialMatchedLastPrefix) ?: emptyList()
        }

        // optimization for childNodes.size = 1 to avoid recursion (and make it is faster)
        if (childNodes.size == 1) {
            // optimization to avoid memory allocation if node does not contain relevant data at all
            allPrefixWords = allPrefixWords.makeSureNotNullOr(initialAllPrefixWords)

            allPrefixWords.add(word)

            val childNode = childNodes[0]
            when (childNode.node.canBeEnd) {
                NodeType.CanBeEndOfPrefix -> matchedLastPrefix = allPrefixWords
                // for case if the whole phrase is prefix (and it is last phrase word)
                NodeType.CanBeEndOfShared -> if (i == words.lastIndex) matchedLastPrefix = allPrefixWords
                NodeType.SimpleNode       -> { }
            }

            node = childNode.node

            if (childNode.nextNode != null) {
                require(toContinueFromNodeAfterShared == null) {
                    "Expects only one endNodeAfterShared or child.nextNode is present at any time" +
                            " (childNodes[0].nextNode: ${childNode.nextNode}, endNodeAfterShared: ${toContinueFromNodeAfterShared})" }

                toContinueFromNodeAfterShared = childNode.nextNode
            }

            continue
        }

        else {
            // optimization to avoid memory allocation if node does not contain relevant data at all
            allPrefixWords = allPrefixWords.makeSureNotNullOr(initialAllPrefixWords)

            allPrefixWords.add(word)

            if (childNodes.canBeEndOfPrefix())
                matchedLastPrefix = allPrefixWords

            val results = childNodes.map { childNode ->

                val nextNode = childNode.node
                val nextEndNodeAfterShared = if (childNode.nextNode != null) {
                    require(toContinueFromNodeAfterShared == null) {
                        "Expects when we find shared node, the previous shared nod is already processed and its previous endNodeAfterShared is set to null" +
                                " (childNode.nextNode: ${childNode.nextNode}, current toContinueFromNodeAfterShared: ${toContinueFromNodeAfterShared})" }

                    childNode.nextNode
                } else null

                doSearchMatchedPrefixSequence(words.subList(i + 1), nextNode, nextEndNodeAfterShared, allPrefixWords, matchedLastPrefix)

            }.minByOrNull { -it.size }

            return results ?: emptyList()
        }
    }

    return matchedLastPrefix ?: emptyList()
}


private fun Iterable<TreeNodeRes>.canBeEndOfPrefix(): Boolean = this.any { it.node.canBeEnd === NodeType.CanBeEndOfPrefix }

private fun buildSharedTree(sequences: Alt<Seq<String>>, rootNodeName: String, direction: Direction, toIgnoreWords: Set<String>, toLowerCase: Boolean = false): TreeNode {
    val rootNode = TreeNode(rootNodeName)

    val fixedSequences = (if (toLowerCase) sequences.map { seq -> seq.map { it.lowercase() } } else sequences)

    for (seq in fixedSequences) {

        val toIgnore = seq.isEmpty() || seq.any { word -> word in toIgnoreWords }
        if (toIgnore) continue

        var node: TreeNode = rootNode
        val wordSeq = seq.let { if (direction == Direction.Backward) it.reversed() else it }

        for (word in wordSeq) {
            node = node.addChildNode(word)
        }
        node.canBeEnd = NodeType.CanBeEndOfShared
    }

    return rootNode
}


class SubSequenceLanguageRules (
    val articlesAndSimilar: Alt<Seq<String>>,
    val prepositions: List<Preposition>,
    val commonVerbs: Alt<Seq<String>>,
    val pronouns: Alt<Seq<String>>,
)


internal class BuildContext (languageRules: SubSequenceLanguageRules, toIgnoreWords: Iterable<String>, direction: Direction) {
    val toIgnoreWords: Set<String> = toIgnoreWords.map { it.trim().lowercase() }.toSet()

    val artsSharedTree = buildSharedTree(languageRules.articlesAndSimilar, artsSharedNodeName, direction, this.toIgnoreWords)
    val prepositionsSharedTree = buildSharedTree(languageRules.prepositions.map { it.words }, prepositionsSharedNodeName, direction, this.toIgnoreWords)
    // TODO: add 2nd, 3rd, 's forms
    val verbsSharedTree = buildSharedTree(languageRules.commonVerbs, verbsSharedNodeName, direction, this.toIgnoreWords)
    val pronounsSharedTree = buildSharedTree(languageRules.pronouns, pronounsSharedNodeName, direction, this.toIgnoreWords)
}


internal fun buildPrefixesMatchTree(patterns: Alt<Seq<String>>, direction: Direction, toIgnoreWords: Iterable<String>, languageRules: SubSequenceLanguageRules): TreeNode {

    val context = BuildContext(languageRules, toIgnoreWords, direction)
    val rootNode = TreeNode("{root}")

    for (pattern in patterns) {

        val toIgnore = pattern.isEmpty() || pattern.any { word -> word in context.toIgnoreWords }
        if (toIgnore) continue

        val patterWordsSeq = if (direction == Direction.Backward) pattern.reversed() else pattern

        var node: TreeNode = rootNode
        for (word in patterWordsSeq) {
            node = when (word) {
                artsSharedNodeName,         "{art}"  -> node.addChildNode(SharedWrapper(context.artsSharedTree))
                verbsSharedNodeName,        "{verb}" -> node.addChildNode(SharedWrapper(context.verbsSharedTree))
                prepositionsSharedNodeName, "{prep}" -> node.addChildNode(SharedWrapper(context.prepositionsSharedTree))
                pronounsSharedNodeName               -> node.addChildNode(SharedWrapper(context.pronounsSharedTree))
                else -> node.addChildNode(word)
            }
        }

        // last node
        node.canBeEnd = NodeType.CanBeEndOfPrefix
    }

    return rootNode
}


//@Suppress("NOTHING_TO_INLINE")
//private inline fun <T> arrayList(list: List<T>?): MutableList<T> = if (list != null) ArrayList(list) else  mutableListOf()

@Suppress("NOTHING_TO_INLINE")
private inline fun <T, L: MutableList<T>> L?.makeSureNotNullOr(initialValues: List<T>?): MutableList<T> =
    this ?: if (initialValues != null) ArrayList(initialValues) else  mutableListOf()


fun Sequence<String>.splitToWords(): Alt<Seq<String>> =
    this.distinct()
        .map { it.split(' ', ',').map { word -> word.trim() }.filterNotEmpty() }
        .toList()


