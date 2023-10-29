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


class PrefixFinderTest {

    @Test
    fun findPrefix() {

        val sw = startStopWatch("PrefixFinder creation")

        val pf = PrefixFinder()
        sw.logInfo(log)


        val a = SoftAssertions()

        val asw = startStopWatch("PrefixFinder assertions")

        a.assertThat(pf.findPrefix("to allow credit")).isEqualTo("to allow")
        a.assertThat(pf.removePrefix("to allow credit")).isEqualTo("credit")

        a.assertThat(pf.removePrefix("to allow credit")).isEqualTo("credit")
        a.assertThat(pf.removePrefix(" \t \n to allow credit")).isEqualTo("credit")
        a.assertThat(pf.removePrefix("TO ALLOW CREDIT")).isEqualTo("CREDIT")

        a.assertThat(pf.removePrefix("to be cut up")).isEqualTo("cut up")
        a.assertThat(pf.removePrefix("to be in trouble")).isEqualTo("trouble")
        a.assertThat(pf.removePrefix("to be off one's nut")).isEqualTo("nut")
        a.assertThat(pf.removePrefix("to be on a roll")).isEqualTo("roll")
        a.assertThat(pf.removePrefix("to be on the watch for")).isEqualTo("watch for")
        a.assertThat(pf.removePrefix("to be up to")).isEqualTo("")
        a.assertThat(pf.calculateBaseOfFromForSorting("to be up to")).isEqualTo("up to")

        a.assertThat(pf.removePrefix("to do smb. credit")).isEqualTo("credit")

        a.assertThat(pf.removePrefix("to get a sleep")).isEqualTo("sleep")
        a.assertThat(pf.removePrefix("to get enough sleep")).isEqualTo("sleep")
        a.assertThat(pf.removePrefix("to get into trouble")).isEqualTo("trouble")
        a.assertThat(pf.removePrefix("to get over the hump")).isEqualTo("hump")
        a.assertThat(pf.removePrefix("to get the hump")).isEqualTo("hump")

        a.assertThat(pf.removePrefix("to give the mitten")).isEqualTo("mitten")

        a.assertThat(pf.removePrefix("to go into the question")).isEqualTo("question")
        a.assertThat(pf.removePrefix("to go to the woods")).isEqualTo("woods")
        a.assertThat(pf.removePrefix("to handle without mittens")).isEqualTo("mittens")
        a.assertThat(pf.removePrefix("to have a common purse")).isEqualTo("purse")
        a.assertThat(pf.removePrefix("to take smb. welcome")).isEqualTo("welcome")
        a.assertThat(pf.removePrefix("to take the trouble")).isEqualTo("trouble")
        a.assertThat(pf.removePrefix("to take the trouble")).isEqualTo("trouble")
        a.assertThat(pf.removePrefix("to try one's luck")).isEqualTo("luck")

        a.assertThat(pf.removePrefix("a door to success")).isEqualTo("door to success")
        a.assertThat(pf.removePrefix("out of doors")).isEqualTo("doors")
        a.assertThat(pf.removePrefix("on the neck")).isEqualTo("neck")
        a.assertThat(pf.removePrefix("on the neck")).isEqualTo("neck")
        a.assertThat(pf.removePrefix("not for nuts")).isEqualTo("nuts")
        a.assertThat(pf.removePrefix("not a snap")).isEqualTo("snap")
        a.assertThat(pf.removePrefix("not a snap")).isEqualTo("snap")
        a.assertThat(pf.removePrefix("under a spell")).isEqualTo("spell")
        a.assertThat(pf.removePrefix("what's the trouble?")).isEqualTo("trouble?")
        a.assertThat(pf.removePrefix("at present")).isEqualTo("present")
        a.assertThat(pf.removePrefix("at present")).isEqualTo("present")
        a.assertThat(pf.removePrefix("bad luck")).isEqualTo("luck")
        a.assertThat(pf.removePrefix("by feel")).isEqualTo("feel")
        a.assertThat(pf.removePrefix("front door")).isEqualTo("door")
        a.assertThat(pf.removePrefix("good luck")).isEqualTo("luck")
        a.assertThat(pf.removePrefix("good luck")).isEqualTo("luck")
        a.assertThat(pf.removePrefix("next door")).isEqualTo("door")
        a.assertThat(pf.removePrefix("next door")).isEqualTo("door")
        a.assertThat(pf.removePrefix("up to the neck")).isEqualTo("neck")
        a.assertThat(pf.removePrefix("to be on the watch for")).isEqualTo("watch for")

        a.assertThat(pf.removePrefix("to go at")).isEqualTo("")
        a.assertThat(pf.calculateBaseOfFromForSorting("to go at")).isEqualTo("go at")

        //a.assertThat(pf.removePrefix("to go at")).isEqualTo("at")

        a.assertThat(pf.removePrefix("on no account")).isEqualTo("account")
        a.assertThat(pf.removePrefix("long odds")).isEqualTo("odds")
        a.assertThat(pf.removePrefix("on one's own account")).isEqualTo("account")
        a.assertThat(pf.removePrefix("to have one's own way")).isEqualTo("way")

        asw.logInfo(log)

        a.assertAll()
    }

    @Test
    fun findPrefix_22() {

        val swCreation = startStopWatch("PrefixFinder creation")
        val pf = PrefixFinder()
        swCreation.logInfo(log)


        val swProcessing = startStopWatch("PrefixFinder processing")

        pf.findPrefix("to allow credit")
        pf.removePrefix("to allow credit")

        pf.removePrefix("to allow credit")
        pf.removePrefix(" \t \n to allow credit")
        pf.removePrefix("TO ALLOW CREDIT")

        pf.removePrefix("to be cut up")
        pf.removePrefix("to be in trouble")
        pf.removePrefix("to be off one's nut")
        pf.removePrefix("to be on a roll")
        pf.removePrefix("to be on the watch for")
        pf.removePrefix("to be up to")
        pf.calculateBaseOfFromForSorting("to be up to")

        pf.removePrefix("to do smb. credit")

        pf.removePrefix("to get a sleep")
        pf.removePrefix("to get enough sleep")
        pf.removePrefix("to get into trouble")
        pf.removePrefix("to get over the hump")
        pf.removePrefix("to get the hump")

        pf.removePrefix("to give the mitten")

        pf.removePrefix("to go into the question")
        pf.removePrefix("to go to the woods")
        pf.removePrefix("to handle without mittens")
        pf.removePrefix("to have a common purse")
        pf.removePrefix("to take smb. welcome")
        pf.removePrefix("to take the trouble")
        pf.removePrefix("to take the trouble")
        pf.removePrefix("to try one's luck")

        pf.removePrefix("a door to success")
        pf.removePrefix("out of doors")
        pf.removePrefix("on the neck")
        pf.removePrefix("on the neck")
        pf.removePrefix("not for nuts")
        pf.removePrefix("not a snap")
        pf.removePrefix("not a snap")
        pf.removePrefix("under a spell")
        pf.removePrefix("what's the trouble?")
        pf.removePrefix("at present")
        pf.removePrefix("at present")
        pf.removePrefix("bad luck")
        pf.removePrefix("by feel")
        pf.removePrefix("front door")
        pf.removePrefix("good luck")
        pf.removePrefix("good luck")
        pf.removePrefix("next door")
        pf.removePrefix("next door")
        pf.removePrefix("up to the neck")
        pf.removePrefix("to be on the watch for")

        pf.removePrefix("to go at")
        pf.calculateBaseOfFromForSorting("to go at")

        pf.removePrefix("to go at")

        pf.removePrefix("on no account")
        pf.removePrefix("long odds")
        pf.removePrefix("on one's own account")
        pf.removePrefix("to have one's own way")

        swProcessing.logInfo(log)
    }

    @Test
    @Disabled("for debug")
    fun calculateBaseOfFromForSorting_forSpecial() {

        val sw = startStopWatch("PrefixFinder creation")

        val pf = PrefixFinder()
        sw.logInfo(log)


        val a = SoftAssertions()

        //a.assertThat(pf.calculateBaseOfFromForSorting("to handle without mittens")).isEqualTo("mittens")
        //a.assertThat(pf.calculateBaseOfFromForSorting("a door to success")).isEqualTo("door to success")
        //a.assertThat(pf.calculateBaseOfFromForSorting("next door")).isEqualTo("door")
        //a.assertThat(pf.calculateBaseOfFromForSorting("to go at")).isEqualTo("go at")
        //
        //a.assertThat(pf.calculateBaseOfFromForSorting("to have a common purse")).isEqualTo("purse")

        a.assertThat(pf.removePrefix("to have a common purse")).isEqualTo("purse")

        a.assertThat(pf.removePrefix("out of doors")).isEqualTo("doors")
        a.assertThat(pf.removePrefix("up to the neck")).isEqualTo("neck")
        a.assertThat(pf.removePrefix("to go at")).isEqualTo("")
        a.assertThat(pf.removePrefix("on one's own account")).isEqualTo("account")

        a.assertThat(pf.removePrefix("to handle without mittens")).isEqualTo("mittens")
        a.assertThat(pf.removePrefix("a door to success")).isEqualTo("door to success")
        a.assertThat(pf.removePrefix("next door")).isEqualTo("door")

        a.assertAll()
    }

    @Test
    fun aaa() {
        val src: List<List<List<List<String>>>> = alt(
            //listOf(listOf(listOf("to")), listOf(listOf(listOf("have"))), listOf(listOf(listOf("a", "common"))))
            seq(
                alt(seq("to")),
                alt(seq("have")),
                alt(seq("a", "common")),
                //alt(wordsSeq("a")),
            )
        )
        val pf = PrefixFinder(src)
        log.info { pf }

        //assertThat(pf.findPrefix("to have a bar")).isEqualTo("to have a")
        assertThat(pf.findPrefix("to have a bar")).isEqualTo(null)
        assertThat(pf.findPrefix("to have a common bar")).isEqualTo("to have a common")
    }

    //@Test
    @org.junit.jupiter.api.RepeatedTest(2)
    fun performanceTest(ri: RepetitionInfo) {
        val words = Files.readAllLines(Path.of("/home/vmelnykov/projects/words/learn-words/src/test/resources/Friends S01E03.txt"))
        log.info { "performanceTest => words (${words.size}) are loaded" }

        if (ri.currentRepetition == 1) {
            val creatingSW = startStopWatch("performanceTest => creating PrefixFinder")
            val pf = PrefixFinder()
            creatingSW.logInfo(log)

            log.info { "performanceTest => calculating started" }
            val sw = startStopWatch("performanceTest => processing of ${words.size}")
            words.forEach { pf.calculateBaseOfFromForSorting(it) }
            sw.logInfo(log)
        }

        if (ri.currentRepetition == ri.totalRepetitions) {
            val count = 20
            val sw = startStopWatch("performanceTest => creating PrefixFinder $count times")
            for (i in 1..count) PrefixFinder()
            sw.logInfo(log)

            log.info { "Average creation time is ${sw.time/count}ms." }
        }
    }

}

private fun <T> seq(vararg values: T): Seq<T> = listOf(*values)
private fun <T> alt(vararg values: T): Alt<T> = listOf(*values)
