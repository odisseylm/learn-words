package com.mvv.gui.words

import com.mvv.gui.util.SpaceCharPolicy
import com.mvv.gui.util.listOfNonNulls
import com.mvv.gui.util.removeRepeatableSpaces
import com.mvv.gui.util.subList


private const val delegateNodeNamePrefix = "->"

// The shortest IDs to minimize equals
// These nodes are 'word' of root node of shared READONLY (because they are shared) trees.
internal const val verbsSharedNodeName = "{v}"
internal const val artsSharedNodeName = "{a}"
internal const val prefixesSharedNodeName = "{p}"
internal val sharedNodeNames: Array<String> = arrayOf(verbsSharedNodeName, artsSharedNodeName, prefixesSharedNodeName)


// Delegates are mutable nodes to connect usual node and shared readonly (you are not allowed to change their 'canBeEnd' flag) trees.
// You can change 'canBeEnd' flag of these 'delegate' nodes.
//internal const val delegateToVerbsSharedNodeName = ">v"
//internal const val delegateToArtsSharedNodeName = ">a"
//internal const val delegateToPrefixesSharedNodeName = ">p"
//internal val delegateNodeNames22: Array<String> = arrayOf(delegateToVerbsSharedNodeName, delegateToArtsSharedNodeName, delegateToPrefixesSharedNodeName)



// "to {verb}" // for this/similar case we need to use canBeEnd
// "to {verb} to"
// "to {verb} {art}"
//
internal class SharedWrapper (
    val sharedTree: TreeNode,
    ) {
    val sharedId: String = sharedTree.word
    //var canBeginOfSharedBeEnd: Boolean = false
    // for example, template "to {verb}", after verb we need to add end marker 'can be end',
    // but on other side it is last, and we can understand that it is end because it is last (no nodes after it)
    //var canBeEnd: Boolean = false

    // should be used after shared (if shared tree matches to search)
    //val lastSimpleNodes: Map<String, TreeNode> = HashMap()
    //val lastSharedNodes: Map<String, SharedWrapper> = HashMap()
}

internal class TreeNode private constructor (
    val word: String, // in case of wrapping shared tree it is ID of shared tree
    val shared: SharedWrapper?, // = emptyTreeNodeStub,
    var canBeEnd: Boolean = false,
    private val children: MutableMap<String, TreeNode> = HashMap(),
    ) {

    constructor(word: String) : this(word, null)
    constructor(shared: SharedWrapper) : this(shared.sharedId, shared)

    override fun toString(): String = "TreeNode { '$word'" +
            (if (shared != null) ", is shared" else "") +
            (if (canBeEnd) ", canBeEnd" else "") +
            " }"

    //var canBeEnd: Boolean = false

    //private val children: MutableMap<String, TreeNode> = HashMap()

    val asNextNode: TreeNode? = if (shared != null) TreeNode(word, null, this.canBeEnd, this.children) else null

    internal fun addChildNode(word: String) =
        this.children.computeIfAbsent(word) { TreeNode(word) }
    internal fun addChildNode(shared: SharedWrapper) =
        this.children.computeIfAbsent(shared.sharedId) { TreeNode(shared) }
    internal fun addChildNode(word: String, canBeEnd: Boolean): TreeNode =
        this.children.computeIfAbsent(word) { TreeNode(word) }.also { if (canBeEnd) it.canBeEnd = true }
    internal fun addChildNode(shared: SharedWrapper, canBeEnd: Boolean): TreeNode =
        this.children.computeIfAbsent(shared.sharedId) { TreeNode(shared) }.also { if (canBeEnd) it.canBeEnd = true }
    internal fun getChildNode(word: String): TreeNode? = this.children[word]
    internal fun hasChildNodes(): Boolean = this.children.isNotEmpty()

    //internal fun TreeNode?.childAsTreeNodeRes(word: String): TreeNodeRes? {
    //    if (this == null) null
    //
    //    this.shared.sharedId
    //
    //    if (this == null) null else TreeNodeRes(this, this.asNextNode)
    //}

    /*
    fun findAllChildNodes(word: String): List<TreeNode> {
        val child1 = children[word]

        val delegate1 = children[delegateToArtsSharedNodeName]
        val delegate2 = children[delegateToVerbsSharedNodeName]
        val delegate3 = children[delegateToPrefixesSharedNodeName]

        return listOfNonNulls(child1, child2, child3, child4)
    }
    */

    fun findSharedChildNodes(word: String): List<TreeNodeRes> {
        //val child1 = children[word]

        val delegate1 = children[artsSharedNodeName]
        val delegate2 = children[verbsSharedNodeName]
        val delegate3 = children[prefixesSharedNodeName]

        val sharedRes1 = delegate1?.sharedChildAsTreeNodeRes(word)
        val sharedRes2 = delegate2?.sharedChildAsTreeNodeRes(word)
        val sharedRes3 = delegate3?.sharedChildAsTreeNodeRes(word)

        return listOfNonNulls(sharedRes1, sharedRes2, sharedRes3)
    }

    fun findChildNodes(word: String): List<TreeNodeRes> {
        val child1 = children[word]
        val child1Res = child1?.let { TreeNodeRes(it, null) }

        val delegate1 = children[artsSharedNodeName]
        val delegate2 = children[verbsSharedNodeName]
        val delegate3 = children[prefixesSharedNodeName]

        val sharedRes1 = delegate1?.sharedChildAsTreeNodeRes(word)
        val sharedRes2 = delegate2?.sharedChildAsTreeNodeRes(word)
        val sharedRes3 = delegate3?.sharedChildAsTreeNodeRes(word)

        return listOfNonNulls(child1Res, sharedRes1, sharedRes2, sharedRes3)
    }
}


internal class TreeNodeRes (
    val node: TreeNode,
    val nextNode: TreeNode?,
)


internal fun TreeNode?.sharedChildAsTreeNodeRes(word: String): TreeNodeRes? {
    if (this == null) return null

    val childNode = this.shared!!.sharedTree.getChildNode(word)
    return if (childNode == null) null
           else TreeNodeRes(childNode, this.asNextNode)
}



//
//private val emptyTreeNodeStub: TreeNode = TODO()
//
//
//internal class ComplexTreeNode (val word: String) {
//    var canBeEnd: Boolean = false
//    val simpleChildren: MutableMap<String, SimpleTreeNode> = HashMap()
//    val sharedChildren: MutableMap<String, SimpleTreeNode> = HashMap()
//}

/*
internal class SimpleTreeNode (val word: String) {
    var canBeEnd: Boolean = false
    private val children: MutableMap<String, SimpleTreeNode> = HashMap()

    internal fun addChildNode(word: String) =
        this.children.computeIfAbsent(word) { SimpleTreeNode(word) }
    internal fun addChildNode(word: String, canBeEnd: Boolean) =
        this.children.computeIfAbsent(word) { SimpleTreeNode(word) }.also { if (canBeEnd) it.canBeEnd = true }
    internal fun getChildNode(word: String) = this.children[word]
    internal fun hasChildNodes(): Boolean = this.children.isNotEmpty()
}
*/


internal fun TreeNode.findMatchedPrefix(phrase: String): String {
    val words = phrase.lowercase().trim().removeRepeatableSpaces().split(' ', '\n', '\t')
    return doSearchMatchedPrefixSequence(words, this, null, null, null).joinToString(" ")
}

internal fun doSearchMatchedPrefixSequence(words: List<String>, initialNode: TreeNode, initialEndNodeAfterShared: TreeNode?,
                                           initialPrefixNodes: List<String>?, initialLastPrefix: List<String>?): List<String> {

    var node = initialNode

    var prefixNodes: MutableList<String>? = null
    var lastPrefix: List<String>? = initialLastPrefix

    var endNodeAfterShared: TreeNode? = initialEndNodeAfterShared

    for (i in words.indices) {

        val word = words[i]
        var childNodes: List<TreeNodeRes> = node.findChildNodes(word)

        // Last leaf.
        // If it is shared last leaf we need to continue processing nodes after shared.
        if (!node.hasChildNodes()) {

            if (endNodeAfterShared == null)
                return lastPrefix ?: emptyList()

            else {
                node = endNodeAfterShared
                endNodeAfterShared = null

                childNodes = node.findChildNodes(word)

                if (childNodes.isEmpty())
                    return lastPrefix ?: emptyList()
            }
        }

        if (childNodes.isEmpty()) {
            return lastPrefix ?: emptyList()
        }

        // optimization for childNodes.size = 1 to avoid recursion (and make it is faster)
        else if (childNodes.size == 1) {
            // optimization to avoid memory allocation if node does not contain relevant data at all
            prefixNodes = prefixNodes.makeSureNotNullOr(initialPrefixNodes)

            prefixNodes.add(word)

            val childNode = childNodes[0]
            if (childNode.node.canBeEnd)
                lastPrefix = prefixNodes

            node = childNode.node

            if (childNode.nextNode != null) {
                require(endNodeAfterShared == null) {
                    "Expects only one endNodeAfterShared or child.nextNode is present at any time" +
                            " (childNodes[0].nextNode: ${childNode.nextNode}, endNodeAfterShared: ${endNodeAfterShared})" }

                endNodeAfterShared = childNode.nextNode
            }

            continue
        }

        else {
            // optimization to avoid memory allocation if node does not contain relevant data at all
            prefixNodes = prefixNodes.makeSureNotNullOr(initialPrefixNodes)

            prefixNodes.add(word)

            if (childNodes.any { it.node.canBeEnd })
                lastPrefix = prefixNodes

            val results = childNodes.map { childNode ->

                val nextNode = childNode.node
                val nextEndNodeAfterShared = if (childNode.nextNode != null) {
                    require(endNodeAfterShared == null) {
                        "Expects when we find shared node, the previous shared nod is already processed and its previous endNodeAfterShared is set to null" +
                                " (childNode.nextNode: ${childNode.nextNode}, current endNodeAfterShared: ${endNodeAfterShared})"
                    }

                    childNode.nextNode
                } else null

                doSearchMatchedPrefixSequence(words.subList(i + 1), nextNode, nextEndNodeAfterShared, prefixNodes, lastPrefix)

            }.minByOrNull { -it.size }

            return results ?: emptyList()
        }
    }

    return lastPrefix ?: emptyList()
}


@Suppress("NOTHING_TO_INLINE")
private inline fun <T> arrayList(list: List<T>?): MutableList<T> = if (list != null) ArrayList(list) else  mutableListOf()

@Suppress("NOTHING_TO_INLINE")
private inline fun <T, L: MutableList<T>> L?.makeSureNotNullOr(initialValues: List<T>?): MutableList<T> =
    this ?: if (initialValues != null) ArrayList(initialValues) else  mutableListOf()

