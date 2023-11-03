package com.mvv.gui.words

import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test


class PrefixFinder2Test {

    internal val verbs = createSharedVerbTrees()
    internal val arts = createSharedArtsTrees()

    //@Test
    @org.junit.jupiter.api.RepeatedTest(2)
    fun modelTest() {

        val root = TreeNode("{root}")

        // "to {verb}"
        root.addChildNode("to").addChildNode(SharedWrapper(verbs), true)

        // to model "to go {art} word222 baseWord"
        root.addChildNode("to").addChildNode("go").addChildNode(SharedWrapper(arts)).addChildNode("word222", true)

        // "to {verb} to"
        root.addChildNode("to").addChildNode(SharedWrapper(verbs)).addChildNode("to", true)

        // "to {verb} {art}"
        root.addChildNode("to").addChildNode(SharedWrapper(verbs)).addChildNode(SharedWrapper(arts), true)

        // "to {verb} {art}"
        root.addChildNode("to").addChildNode(SharedWrapper(verbs)).addChildNode("to").addChildNode(SharedWrapper(arts), true)

        val a = SoftAssertions()

        a.assertThat(root.findMatchedPrefix("to go home"))
            .isEqualTo("to go")

        a.assertThat(root.findMatchedPrefix("to go to hotel"))
            .isEqualTo("to go to")

        a.assertThat(root.findMatchedPrefix("to go the shortest hotel"))
            .isEqualTo("to go the shortest")
        a.assertThat(root.findMatchedPrefix("to go to the shortest hotel"))
            .isEqualTo("to go to the shortest")

        a.assertThat(root.findMatchedPrefix("to go a hotel"))
            .isEqualTo("to go a")
        a.assertThat(root.findMatchedPrefix("to go a long hotel"))
            .isEqualTo("to go a long")

        a.assertAll()
    }


    private fun createSharedArtsTrees(): TreeNode {

        // "somebody", "a", "a long", "a long somebody", "the shortest"

        val rootNode = TreeNode(artsSharedNodeName)
        rootNode.addChildNode("somebody", true)
        rootNode.addChildNode("a", true)
        rootNode.addChildNode("a").addChildNode("long", true)
        rootNode.addChildNode("a").addChildNode("long").addChildNode("somebody", true)
        rootNode.addChildNode("the").addChildNode("shortest", true)

        return rootNode
    }


    private fun createSharedVerbTrees(): TreeNode {
        // "have", "go", "have no"

        val rootNode = TreeNode(verbsSharedNodeName)
        rootNode.addChildNode("have", true)
        rootNode.addChildNode("go", true)
        rootNode.addChildNode("have").addChildNode("no", true)

        return rootNode
    }

}
