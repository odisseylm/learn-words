package com.mvv.gui.words

import com.mvv.gui.util.*


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
class PrefixFinder (
    patternTemplates: Alt<Seq<String>>,
    val ignoredWords: Collection<String>,
    ) {

    constructor(ignoredWords: Collection<String> = emptySet()) : this(possibleNonRelevantForSortingPrefixTemplates, ignoredWords)

    private val rootNode: TreeNode = buildPrefixesMatchTree(patternTemplates, ignoredWords)

    fun findMatchedPrefix(phrase: String): String? =
        rootNode.findMatchedPrefix(phrase)

    fun removeMatchedPrefix(phrase: String): String {
        val matchedPrefix = findMatchedPrefix(phrase)
        return if (matchedPrefix == null) phrase
               else phrase.trim().removeRepeatableSpaces().substring(matchedPrefix.length).trimStart()
    }

    fun calculateBaseOfFromForSorting(phrase: String): String {
        val fixedPhrase = phrase.lowercase().trim()

        val prefix = findMatchedPrefix(fixedPhrase)
            ?: return fixedPhrase

        var base = fixedPhrase.removeRepeatableSpaces().substring(prefix.length).trimStart()
        if (base.isNotBlank()) return base

        base = fixedPhrase.removePrefix("to be ").removePrefix("to ")
        if (base.isNotBlank()) return base

        return fixedPhrase
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


internal fun TreeNode.findMatchedPrefix(phrase: String): String? {
    val words = phrase.lowercase().trim()
        .removeRepeatableSpaces()
        .removeSuffixesRepeatably("!", ".", "?")
        .trimEnd()
        .split(' ', '\n', '\t')

    val matchedPrefixSequence = doSearchMatchedPrefixSequence(words, this, null, null, null)
    return matchedPrefixSequence.ifEmpty { null }?.joinToString(" ")
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

private fun buildSharedTree(sequences: Alt<Seq<String>>, rootNodeName: String, toIgnoreWords: Set<String>, toLowerCase: Boolean = false): TreeNode {
    val rootNode = TreeNode(rootNodeName)

    val fixedSequences = if (toLowerCase) sequences.map { seq -> seq.map { it.lowercase() } } else sequences

    for (seq in fixedSequences) {

        val toIgnore = seq.isEmpty() || seq.any { word -> word in toIgnoreWords }
        if (toIgnore) continue

        var node: TreeNode = rootNode
        for (word in seq) {
            node = node.addChildNode(word)
        }
        node.canBeEnd = NodeType.CanBeEndOfShared
    }

    return rootNode
}


internal class BuildContext (toIgnoreWords: Iterable<String>) {
    val toIgnoreWords: Set<String> = toIgnoreWords.map { it.trim().lowercase() }.toSet()

    val artsSharedTree = buildSharedTree(articlesAndSimilar, artsSharedNodeName, this.toIgnoreWords)
    val prepositionsSharedTree = buildSharedTree(prepositions.map { it.words }, prepositionsSharedNodeName, this.toIgnoreWords)
    val verbsSharedTree = buildSharedTree(verbs, verbsSharedNodeName, this.toIgnoreWords)
    val pronounsSharedTree = buildSharedTree(pronouns, pronounsSharedNodeName, this.toIgnoreWords)
}

internal fun buildPrefixesMatchTree(patterns: Alt<Seq<String>>, toIgnoreWords: Iterable<String>): TreeNode {

    val context = BuildContext(toIgnoreWords)
    val rootNode = TreeNode("{root}")

    for (pattern in patterns) {

        val toIgnore = pattern.isEmpty() || pattern.any { word -> word in context.toIgnoreWords }
        if (toIgnore) continue

        var node: TreeNode = rootNode
        for (word in pattern) {
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


private fun Sequence<String>.splitToWords(): Alt<Seq<String>> =
    this.distinct()
        .map { it.split(' ').filterNotBlank() }
        .toList()


private val possibleNonRelevantForSortingPrefixTemplates: Alt<Seq<String>> = sequenceOf(
    "{verb}",
    "{verb} no",
    "{verb} no {art}",

    "{verb} {art}",
    "{verb} {prep} {art}",

    "{verb} to {art}",
    "{verb} to {prep} {art}",

    "to {verb}",
    "to {verb} no",
    "to {verb} no {art}",

    "to {verb} {art}",
    "to {verb} {prep} {art}",
    "not to {verb} {art}",
    "not to {verb} {prep} {art}",

    "to {verb} to",
    "to {verb} to {art}",
    "to {verb} to {prep} {art}",

    "{pp} {verb} to {prep} {art}",
    "{pp} {verb} {prep} {art}",
    "{pp} {verb} to {art}",
    "{pp} {verb} {art}",

    "{verb} {pp} to {prep} {art}",
    "{verb} {pp} {prep} {art}",
    "{verb} {pp} to {art}",
    "{verb} {pp} {art}",

    "{verb} {pp} {verb} {art}",
    "{verb} {pp} {verb} {prep} {art}",

    "let {pp}",
    "let {pp} {verb}",
    "let {pp} to {verb}",
    "let {pp} {verb} {art}",
    "let {pp} to {verb} {art}",
    "let {pp} {verb} {prep} {art}",
    "let {pp} to {verb} {prep} {art}",

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

    "{art} few",
    "{art} many",

    "beyond all", "at short", "only",
    "all of a", "all on",

    "what a", "what the",
    "what's a", "what's the",
    "what is a", "what is the",
    "what is he",

    "new",
    "{pp}",
).splitToWords()


private val verbs: Alt<Seq<String>> = sequenceOf(
    "do", "does", "be", "is", "are", "have", "has", "have no", "has no", "get", "gets", "go", "gone", "went", "goes", "going",
    "can", "could", "may", "might", "must", "shall", "should",

    "add", "allow", "answer", "appear", "ask",
    "become", "begin", "believe", "break", "bring", "build", "buy",
    "call", "carry", "change", "clean", "close",
    "come", "consider", "continue", "cook", "count", "cost", "cover", "create", "cut",
    "dance", "decide", "die", "dream", "drink", "drive",
    "eat", "expect", "explain",
    "fall", "feel", "find", "finish", "flow", "fly", "follow", "forget",
    "gain", "get", "give", "grow", "guess",
    "handle", "happen", "hang", "hear", "help", "hit", "hold",
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
    "tell", "think", "try to keep", "teach", "turn", "travel",
    "understand", "use",
    "visit",
    "wait", "walk", "want", "watch", "win", "work", "write", "would",
).splitToWords()


private val articlesAndSimilar: Alt<Seq<String>> = sequenceOf(
    "one's", "ones", "one",
    "somebody's", "somebody", "smb's.", "smb's", "smbs'", "smbs", "smb.", "smb",
    "somebody's a", "somebody a", "smb's. a", "smb's a", "smbs' a", "smbs a", "smb. a", "smb a",
    "somebody's the", "somebody the", "smb's. the", "smb's the", "smbs' the", "smbs the", "smb. the", "smb the",
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
    "a big", "big", "a large", "large",
    "a small", "small", "a tiny", "tiny", "a little", "little",
    "a tall", "tall",
    "a thick", "thick", "a thin", "thin",
    "a bad", "bad", "worse", "the worst",
    "a good", "good", "better", "the best",
    "a hard", "hard", "harder",
    "a great", "great",
    "a half", "half",
    "the public","a public", "public",
    "enough", "a common", "common",

    // Personal pronouns
    // Subject Pronouns
    "i", "you", "he", "she", "it", "we", "you", "they",
    // Object pronouns
    "me", "you", "him", "her", "it", "us", "you", "them",
    // Possessive pronouns
    "mine", "yours", "his", "hers", "its", "ours", "yours", "theirs",

    "my", "his", "her", "their", "its",

    // Demonstrative pronouns
    "this", "these", "that", "those",
    // Interrogative pronouns
    "who", "whom", "which", "what",
    // Relative pronouns
    "who", "whom", "that", "which", "whoever", "whichever", "whomever",
    // Indefinite pronouns
    "all", "another", "any", "anybody", "anyone", "anything", "each", "everybody", "everyone", "everything",
    "few", "many", "nobody", "none", "one", "several", "some", "somebody", "and someone",
    // Reflexive pronouns
    "myself", "yourself", "himself", "herself", "ourselves", "yourselves", "themselves",

    // articles
    "the", "an", "a",
).splitToWords()


//private val subjectPronouns = sequenceOf("I", "you", "he", "she", "it", "we", "you", "they").splitToWords()
//private val objectPronouns = sequenceOf("me", "you", "him", "her", "it", "us", "you", "them").splitToWords()
//private val possessivePronouns = sequenceOf("mine", "yours", "his", "hers", "its", "ours", "yours", "theirs").splitToWords()
//private val interrogativePronouns = sequenceOf("who", "whom", "which", "what").splitToWords()

private val pronouns = sequenceOf(
    // Personal pronouns
    // Subject Pronouns
    "i", "you", "he", "she", "it", "we", "you", "they",
    // Object pronouns
    "me", "you", "him", "her", "it", "us", "you", "them",
    // Possessive pronouns
    "mine", "yours", "his", "hers", "its", "ours", "yours", "theirs",

    // Demonstrative pronouns
    "this", "these", "that", "those",
    // Interrogative pronouns
    "who", "whom", "which", "what",
    // Relative pronouns
    "who", "whom", "that", "which", "whoever", "whichever", "whomever",
    // Indefinite pronouns
    "all", "another", "any", "anybody", "anyone", "anything", "each", "everybody", "everyone", "everything",
    "few", "many", "nobody", "none", "one", "several", "some", "somebody", "and someone",
    // Reflexive pronouns
    "myself", "yourself", "himself", "herself", "ourselves", "yourselves", "themselves",
).splitToWords()

//private val maxExpectedNodesCount = max(verbs.size, articlesAndSimilar.size, prepositions.size)
