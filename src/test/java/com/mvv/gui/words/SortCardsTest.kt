package com.mvv.gui.words

import com.mvv.gui.util.startStopWatch
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path


private val log = mu.KotlinLogging.logger {}


class SortCardsTest {

    @Test
    @DisplayName("calculateBaseOfFromForSorting")
    fun test_calculateBaseOfFromForSorting() {
        val a = SoftAssertions()

        a.assertThat("to allow credit".calculateBaseOfFromForSorting()).isEqualTo("credit")
        a.assertThat(" \t \n to allow credit".calculateBaseOfFromForSorting()).isEqualTo("credit")
        a.assertThat("TO ALLOW CREDIT".calculateBaseOfFromForSorting()).isEqualTo("credit")

        a.assertThat("to be cut up".calculateBaseOfFromForSorting()).isEqualTo("cut up")
        a.assertThat("to be in trouble".calculateBaseOfFromForSorting()).isEqualTo("trouble")
        a.assertThat("to be off one's nut".calculateBaseOfFromForSorting()).isEqualTo("nut")
        a.assertThat("to be on a roll".calculateBaseOfFromForSorting()).isEqualTo("roll")
        a.assertThat("to be on the watch for".calculateBaseOfFromForSorting()).isEqualTo("watch for")
        a.assertThat("to be up to".calculateBaseOfFromForSorting()).isEqualTo("to")

        a.assertThat("to do smb. credit".calculateBaseOfFromForSorting()).isEqualTo("credit")

        a.assertThat("to get a sleep".calculateBaseOfFromForSorting()).isEqualTo("sleep")
        a.assertThat("to get enough sleep".calculateBaseOfFromForSorting()).isEqualTo("sleep")
        a.assertThat("to get into trouble".calculateBaseOfFromForSorting()).isEqualTo("trouble")
        a.assertThat("to get over the hump".calculateBaseOfFromForSorting()).isEqualTo("hump")
        a.assertThat("to get the hump".calculateBaseOfFromForSorting()).isEqualTo("hump")

        a.assertThat("to give the mitten".calculateBaseOfFromForSorting()).isEqualTo("mitten")

        a.assertThat("to go into the question".calculateBaseOfFromForSorting()).isEqualTo("question")
        a.assertThat("to go to the woods".calculateBaseOfFromForSorting()).isEqualTo("woods")
        a.assertThat("to handle without mittens".calculateBaseOfFromForSorting()).isEqualTo("mittens")
        a.assertThat("to have a common purse".calculateBaseOfFromForSorting()).isEqualTo("purse")
        a.assertThat("to take smb. welcome".calculateBaseOfFromForSorting()).isEqualTo("welcome")
        a.assertThat("to take the trouble".calculateBaseOfFromForSorting()).isEqualTo("trouble")
        a.assertThat("to take the trouble".calculateBaseOfFromForSorting()).isEqualTo("trouble")
        a.assertThat("to try one's luck".calculateBaseOfFromForSorting()).isEqualTo("luck")

        a.assertThat("a door to success".calculateBaseOfFromForSorting()).isEqualTo("door to success")
        a.assertThat("out of doors".calculateBaseOfFromForSorting()).isEqualTo("doors")
        a.assertThat("on the neck".calculateBaseOfFromForSorting()).isEqualTo("neck")
        a.assertThat("on the neck".calculateBaseOfFromForSorting()).isEqualTo("neck")
        a.assertThat("not for nuts".calculateBaseOfFromForSorting()).isEqualTo("nuts")
        a.assertThat("not a snap".calculateBaseOfFromForSorting()).isEqualTo("snap")
        a.assertThat("not a snap".calculateBaseOfFromForSorting()).isEqualTo("snap")
        a.assertThat("under a spell".calculateBaseOfFromForSorting()).isEqualTo("spell")
        a.assertThat("what's the trouble?".calculateBaseOfFromForSorting()).isEqualTo("trouble?")
        a.assertThat("at present".calculateBaseOfFromForSorting()).isEqualTo("present")
        a.assertThat("at present".calculateBaseOfFromForSorting()).isEqualTo("present")
        a.assertThat("bad luck".calculateBaseOfFromForSorting()).isEqualTo("luck")
        a.assertThat("by feel".calculateBaseOfFromForSorting()).isEqualTo("feel")
        a.assertThat("front door".calculateBaseOfFromForSorting()).isEqualTo("door")
        a.assertThat("good luck".calculateBaseOfFromForSorting()).isEqualTo("luck")
        a.assertThat("good luck".calculateBaseOfFromForSorting()).isEqualTo("luck")
        a.assertThat("next door".calculateBaseOfFromForSorting()).isEqualTo("door")
        a.assertThat("next door".calculateBaseOfFromForSorting()).isEqualTo("door")
        a.assertThat("up to the neck".calculateBaseOfFromForSorting()).isEqualTo("neck")
        a.assertThat("to be on the watch for".calculateBaseOfFromForSorting()).isEqualTo("watch for")

        // TODO: ?? is it ok ??
        a.assertThat("to go at".calculateBaseOfFromForSorting()).isEqualTo("at")
        //a.assertThat("to go at".calculateBaseOfFromForSorting()).isEqualTo("at")

        a.assertThat("on no account".calculateBaseOfFromForSorting()).isEqualTo("account")
        a.assertThat("long odds".calculateBaseOfFromForSorting()).isEqualTo("odds")
        a.assertThat("on one's own account".calculateBaseOfFromForSorting()).isEqualTo("account")
        a.assertThat("to have one's own way".calculateBaseOfFromForSorting()).isEqualTo("way")

        a.assertAll()
    }

    @Test
    fun performanceTest() {
        val words = Files.readAllLines(Path.of("/home/vmelnykov/projects/words/learn-words/src/test/resources/Friends S01E03.txt"))

        log.info { "performanceTest => words (${words.size}) are loaded" }
        log.info { "performanceTest => calculating started" }
        val sw = startStopWatch()

        words.forEach { it.calculateBaseOfFromForSorting() }
        log.info { "performanceTest => calculating completed (took ${sw.time}ms)" }
    }
}
