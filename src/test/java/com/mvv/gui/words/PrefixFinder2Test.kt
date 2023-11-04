package com.mvv.gui.words

import com.mvv.gui.util.logInfo
import com.mvv.gui.util.startStopWatch
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.RepetitionInfo
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path


private val log = mu.KotlinLogging.logger {}


class PrefixFinder2Test {

    private val verbs = createSharedVerbTrees()
    private val arts = createSharedArtsTrees()

    //@Test
    @org.junit.jupiter.api.RepeatedTest(2)
    fun modelTest() {

        val root = TreeNode("{root}")

        // "to {verb}"
        root.addChildNode("to").addChildNode(SharedWrapper(verbs), NodeType.CanBeEndOfPrefix)

        // to model "to go {art} word222 baseWord"
        root.addChildNode("to").addChildNode("go").addChildNode(SharedWrapper(arts)).addChildNode("word222", NodeType.CanBeEndOfPrefix)

        // "to {verb} to"
        root.addChildNode("to").addChildNode(SharedWrapper(verbs)).addChildNode("to", NodeType.CanBeEndOfPrefix)

        // "to {verb} {art}"
        root.addChildNode("to").addChildNode(SharedWrapper(verbs)).addChildNode(SharedWrapper(arts), NodeType.CanBeEndOfPrefix)

        // "to {verb} {art}"
        root.addChildNode("to").addChildNode(SharedWrapper(verbs)).addChildNode("to").addChildNode(SharedWrapper(arts), NodeType.CanBeEndOfPrefix)

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
        rootNode.addChildNode("somebody", NodeType.CanBeEndOfShared)
        rootNode.addChildNode("a", NodeType.CanBeEndOfShared)
        rootNode.addChildNode("a").addChildNode("long", NodeType.CanBeEndOfShared)
        rootNode.addChildNode("a").addChildNode("long").addChildNode("somebody", NodeType.CanBeEndOfShared)
        rootNode.addChildNode("the").addChildNode("shortest", NodeType.CanBeEndOfShared)

        return rootNode
    }


    private fun createSharedVerbTrees(): TreeNode {
        // "have", "go", "have no"

        val rootNode = TreeNode(verbsSharedNodeName)
        rootNode.addChildNode("have", NodeType.CanBeEndOfShared)
        rootNode.addChildNode("go", NodeType.CanBeEndOfShared)
        rootNode.addChildNode("have").addChildNode("no", NodeType.CanBeEndOfShared)

        return rootNode
    }


    //@Test
    @org.junit.jupiter.api.RepeatedTest(2)
    fun findMatchedPrefix() {

        val sw = startStopWatch("PrefixFinder creation")

        val pf = PrefixFinder_New()
        sw.logInfo(log)


        val a = SoftAssertions()

        val asw = startStopWatch("PrefixFinder assertions")

        a.assertThat(pf.findMatchedPrefix("to allow the credit")).isEqualTo("to allow the")

        a.assertThat(pf.findMatchedPrefix("to allow credit")).isEqualTo("to allow")
        a.assertThat(pf.removeMatchedPrefix("to allow credit")).isEqualTo("credit")

        a.assertThat(pf.removeMatchedPrefix("to allow credit")).isEqualTo("credit")
        a.assertThat(pf.removeMatchedPrefix(" \t \n to allow credit")).isEqualTo("credit")
        a.assertThat(pf.removeMatchedPrefix("TO ALLOW CREDIT")).isEqualTo("CREDIT")

        a.assertThat(pf.removeMatchedPrefix("to be cut up")).isEqualTo("cut up")
        a.assertThat(pf.removeMatchedPrefix("to be in trouble")).isEqualTo("trouble")
        a.assertThat(pf.removeMatchedPrefix("to be off one's nut")).isEqualTo("nut")
        a.assertThat(pf.removeMatchedPrefix("to be on a roll")).isEqualTo("roll")
        a.assertThat(pf.removeMatchedPrefix("to be on the watch for")).isEqualTo("watch for")
        a.assertThat(pf.removeMatchedPrefix("to be up to")).isEqualTo("")
        a.assertThat(pf.calculateBaseOfFromForSorting("to be up to")).isEqualTo("up to")

        a.assertThat(pf.removeMatchedPrefix("to do smb. credit")).isEqualTo("credit")

        a.assertThat(pf.removeMatchedPrefix("to get a sleep")).isEqualTo("sleep")
        a.assertThat(pf.removeMatchedPrefix("to get enough sleep")).isEqualTo("sleep")
        a.assertThat(pf.removeMatchedPrefix("to get into trouble")).isEqualTo("trouble")
        a.assertThat(pf.removeMatchedPrefix("to get over the hump")).isEqualTo("hump")
        a.assertThat(pf.removeMatchedPrefix("to get the hump")).isEqualTo("hump")

        a.assertThat(pf.removeMatchedPrefix("to give the mitten")).isEqualTo("mitten")

        a.assertThat(pf.removeMatchedPrefix("to go into the question")).isEqualTo("question")
        a.assertThat(pf.removeMatchedPrefix("to go to the woods")).isEqualTo("woods")
        a.assertThat(pf.removeMatchedPrefix("to handle without mittens")).isEqualTo("mittens")
        a.assertThat(pf.removeMatchedPrefix("to have a common purse")).isEqualTo("purse")
        a.assertThat(pf.removeMatchedPrefix("to take smb. welcome")).isEqualTo("welcome")
        a.assertThat(pf.removeMatchedPrefix("to take the trouble")).isEqualTo("trouble")
        a.assertThat(pf.removeMatchedPrefix("to take the trouble")).isEqualTo("trouble")
        a.assertThat(pf.removeMatchedPrefix("to try one's luck")).isEqualTo("luck")

        a.assertThat(pf.removeMatchedPrefix("a door to success")).isEqualTo("door to success")
        a.assertThat(pf.removeMatchedPrefix("out of doors")).isEqualTo("doors")
        a.assertThat(pf.removeMatchedPrefix("on the neck")).isEqualTo("neck")
        a.assertThat(pf.removeMatchedPrefix("on the neck")).isEqualTo("neck")
        a.assertThat(pf.removeMatchedPrefix("not for nuts")).isEqualTo("nuts")
        a.assertThat(pf.removeMatchedPrefix("not a snap")).isEqualTo("snap")
        a.assertThat(pf.removeMatchedPrefix("not a snap")).isEqualTo("snap")
        a.assertThat(pf.removeMatchedPrefix("under a spell")).isEqualTo("spell")
        a.assertThat(pf.removeMatchedPrefix("what's the trouble?")).isEqualTo("trouble?")
        a.assertThat(pf.removeMatchedPrefix("at present")).isEqualTo("present")
        a.assertThat(pf.removeMatchedPrefix("at present")).isEqualTo("present")
        a.assertThat(pf.removeMatchedPrefix("bad luck")).isEqualTo("luck")
        a.assertThat(pf.removeMatchedPrefix("by feel")).isEqualTo("feel")
        a.assertThat(pf.removeMatchedPrefix("front door")).isEqualTo("door")
        a.assertThat(pf.removeMatchedPrefix("good luck")).isEqualTo("luck")
        a.assertThat(pf.removeMatchedPrefix("good luck")).isEqualTo("luck")
        a.assertThat(pf.removeMatchedPrefix("next door")).isEqualTo("door")
        a.assertThat(pf.removeMatchedPrefix("next door")).isEqualTo("door")
        a.assertThat(pf.removeMatchedPrefix("up to the neck")).isEqualTo("neck")
        a.assertThat(pf.removeMatchedPrefix("to be on the watch for")).isEqualTo("watch for")

        a.assertThat(pf.removeMatchedPrefix("to go at")).isEqualTo("")
        a.assertThat(pf.calculateBaseOfFromForSorting("to go at")).isEqualTo("go at")

        //a.assertThat(pf.removeMatchedPrefix("to go at")).isEqualTo("at")

        a.assertThat(pf.removeMatchedPrefix("on no account")).isEqualTo("account")
        a.assertThat(pf.removeMatchedPrefix("long odds")).isEqualTo("odds")
        a.assertThat(pf.removeMatchedPrefix("on one's own account")).isEqualTo("account")
        a.assertThat(pf.removeMatchedPrefix("to have one's own way")).isEqualTo("way")

        asw.logInfo(log)

        a.assertAll()
    }

    @Test
    fun findPrefix_22() {

        val swCreation = startStopWatch("PrefixFinder creation")
        val pf = PrefixFinder_New()
        swCreation.logInfo(log)


        val swProcessing = startStopWatch("PrefixFinder processing")

        pf.findMatchedPrefix("to allow credit")
        pf.removeMatchedPrefix("to allow credit")

        pf.removeMatchedPrefix("to allow credit")
        pf.removeMatchedPrefix(" \t \n to allow credit")
        pf.removeMatchedPrefix("TO ALLOW CREDIT")

        pf.removeMatchedPrefix("to be cut up")
        pf.removeMatchedPrefix("to be in trouble")
        pf.removeMatchedPrefix("to be off one's nut")
        pf.removeMatchedPrefix("to be on a roll")
        pf.removeMatchedPrefix("to be on the watch for")
        pf.removeMatchedPrefix("to be up to")
        pf.calculateBaseOfFromForSorting("to be up to")

        pf.removeMatchedPrefix("to do smb. credit")

        pf.removeMatchedPrefix("to get a sleep")
        pf.removeMatchedPrefix("to get enough sleep")
        pf.removeMatchedPrefix("to get into trouble")
        pf.removeMatchedPrefix("to get over the hump")
        pf.removeMatchedPrefix("to get the hump")

        pf.removeMatchedPrefix("to give the mitten")

        pf.removeMatchedPrefix("to go into the question")
        pf.removeMatchedPrefix("to go to the woods")
        pf.removeMatchedPrefix("to handle without mittens")
        pf.removeMatchedPrefix("to have a common purse")
        pf.removeMatchedPrefix("to take smb. welcome")
        pf.removeMatchedPrefix("to take the trouble")
        pf.removeMatchedPrefix("to take the trouble")
        pf.removeMatchedPrefix("to try one's luck")

        pf.removeMatchedPrefix("a door to success")
        pf.removeMatchedPrefix("out of doors")
        pf.removeMatchedPrefix("on the neck")
        pf.removeMatchedPrefix("on the neck")
        pf.removeMatchedPrefix("not for nuts")
        pf.removeMatchedPrefix("not a snap")
        pf.removeMatchedPrefix("not a snap")
        pf.removeMatchedPrefix("under a spell")
        pf.removeMatchedPrefix("what's the trouble?")
        pf.removeMatchedPrefix("at present")
        pf.removeMatchedPrefix("at present")
        pf.removeMatchedPrefix("bad luck")
        pf.removeMatchedPrefix("by feel")
        pf.removeMatchedPrefix("front door")
        pf.removeMatchedPrefix("good luck")
        pf.removeMatchedPrefix("good luck")
        pf.removeMatchedPrefix("next door")
        pf.removeMatchedPrefix("next door")
        pf.removeMatchedPrefix("up to the neck")
        pf.removeMatchedPrefix("to be on the watch for")

        pf.removeMatchedPrefix("to go at")
        pf.calculateBaseOfFromForSorting("to go at")

        pf.removeMatchedPrefix("to go at")

        pf.removeMatchedPrefix("on no account")
        pf.removeMatchedPrefix("long odds")
        pf.removeMatchedPrefix("on one's own account")
        pf.removeMatchedPrefix("to have one's own way")

        swProcessing.logInfo(log)
    }

    @Test
    @Disabled("for debug")
    fun calculateBaseOfFromForSorting_forSpecial() {

        val sw = startStopWatch("PrefixFinder creation")

        val pf = PrefixFinder_New()
        sw.logInfo(log)


        val a = SoftAssertions()

        //a.assertThat(pf.calculateBaseOfFromForSorting("to handle without mittens")).isEqualTo("mittens")
        //a.assertThat(pf.calculateBaseOfFromForSorting("a door to success")).isEqualTo("door to success")
        //a.assertThat(pf.calculateBaseOfFromForSorting("next door")).isEqualTo("door")
        //a.assertThat(pf.calculateBaseOfFromForSorting("to go at")).isEqualTo("go at")
        //
        //a.assertThat(pf.calculateBaseOfFromForSorting("to have a common purse")).isEqualTo("purse")

        a.assertThat(pf.removeMatchedPrefix("to have a common purse")).isEqualTo("purse")

        a.assertThat(pf.removeMatchedPrefix("out of doors")).isEqualTo("doors")
        a.assertThat(pf.removeMatchedPrefix("up to the neck")).isEqualTo("neck")
        a.assertThat(pf.removeMatchedPrefix("to go at")).isEqualTo("")
        a.assertThat(pf.removeMatchedPrefix("on one's own account")).isEqualTo("account")

        a.assertThat(pf.removeMatchedPrefix("to handle without mittens")).isEqualTo("mittens")
        a.assertThat(pf.removeMatchedPrefix("a door to success")).isEqualTo("door to success")
        a.assertThat(pf.removeMatchedPrefix("next door")).isEqualTo("door")

        a.assertAll()
    }


    @Test
    @Disabled("for manual debug")
    fun debugTest() {
        val pf = PrefixFinder_New()
        //val pf = PrefixFinder_New(listOf(
        //    listOf("to", "{verb}", "{art}"),
        //    listOf("to", "{verb}", "{prep}"),
        //    listOf("to", "abc", "def"),
        //), emptySet())
        //assertThat(pf.removeMatchedPrefix("to be in trouble")).isEqualTo("trouble")
        assertThat(pf.removeMatchedPrefix("to be up to")).isEqualTo("")
    }

    /*
    @Test
    fun simplePrefixFinding() {
        val src: Alt<Seq<Alt<Seq<String>>>> = alt(
            //listOf(listOf(listOf("to")), listOf(listOf(listOf("have"))), listOf(listOf(listOf("a", "common"))))
            seq(
                alt(seq("to")),
                alt(seq("have")),
                alt(seq("a", "common")),
                //alt(wordsSeq("a")),
            )
        )
        val pf = PrefixFinder_New(src, emptySet())
        log.info { pf }

        //assertThat(pf.findMatchedPrefix("to have a bar")).isEqualTo("to have a")
        Assertions.assertThat(pf.findMatchedPrefix("to have a bar")).isEqualTo(null)
        Assertions.assertThat(pf.findMatchedPrefix("to have a common bar")).isEqualTo("to have a common")
    }
    */

    //@Test
    @org.junit.jupiter.api.RepeatedTest(2)
    fun performanceTest(ri: RepetitionInfo) {
        val words = Files.readAllLines(Path.of("/home/vmelnykov/projects/words/learn-words/src/test/resources/Friends S01E03.txt"))
        log.info { "performanceTest => words (${words.size}) are loaded" }

        if (ri.currentRepetition == 1) {
            val creatingSW = startStopWatch("performanceTest => creating PrefixFinder")
            val pf = PrefixFinder_New()
            creatingSW.logInfo(log)

            log.info { "performanceTest => calculating started" }
            val sw = startStopWatch("performanceTest => processing of ${words.size}")
            words.forEach { pf.calculateBaseOfFromForSorting(it) }
            sw.logInfo(log)
        }

        if (ri.currentRepetition == ri.totalRepetitions) {
            val count = 20
            val sw = startStopWatch("performanceTest => creating PrefixFinder $count times")

            for (i in 1..count) PrefixFinder_New()

            sw.logInfo(log)
            log.info { "Average creation time is ${sw.time / count}ms." }
        }

        if (ri.currentRepetition == ri.totalRepetitions) {
            val count = 10_000
            val phrases = listOf(
                "to do smb. credit",
                "to get a sleep",
                "to get enough sleep",
                "to get into trouble",
                "to get over the hump",
                "to get the hump",
                "to give the mitten",
                "to go into the question",
                "to go to the woods",
                "to handle without mittens",
            )

            val allPhrases = (0 until count/phrases.size).flatMap { phrases }

            val sw = startStopWatch("performanceTest => processing phrases ${allPhrases.size}")

            val pf = PrefixFinder_New()
            allPhrases.forEach { pf.calculateBaseOfFromForSorting(it) }

            sw.logInfo(log)
            log.info { "Average extracting prefix time is ${sw.time.toDouble() / count}ms." }
        }
    }
    
}


//private fun <T> seq(vararg values: T): Seq<T> = listOf(*values)
//private fun <T> alt(vararg values: T): Alt<T> = listOf(*values)
