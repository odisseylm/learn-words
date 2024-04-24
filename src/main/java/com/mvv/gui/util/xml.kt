package com.mvv.gui.util

import org.w3c.dom.Node
import org.w3c.dom.NodeList


fun NodeList.asIterator(): Iterator<Node> = object : Iterator<Node> {
    private var index = -1
    override fun hasNext(): Boolean = (index + 1) in 0 until length
    override fun next(): Node = item(++index)
}
fun NodeList.asSequence(): Sequence<Node> = this.asIterator().asSequence()

fun Node.child(tagName: String): Node? =
    this.childNodes.asSequence().find { it.nodeName == tagName }

fun Node.childs(tagName: String): List<Node> =
    this.childNodes.asSequence().filter { it.nodeName == tagName }.toList()

fun Node.subChilds(path: String): List<Node> {
    var childNodes = listOf(this)
    path.trim()
        .split('/')
        .filterNotBlank()
        .forEach { tagName ->
            childNodes = childNodes.find { it.childNodes.length != 0 }?.childs(tagName) ?: emptyList()
        }
    return childNodes
}

fun Node.childText(tagName: String): String? =
    child(tagName)?.textContent
