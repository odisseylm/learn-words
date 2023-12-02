package com.mvv.gui.words

import com.mvv.gui.util.startStopWatch
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path


private val log = mu.KotlinLogging.logger {}

//private val pf = PrefixFinder_Old()
private val pf = englishPrefixFinder()

fun String.calculateBaseOfFromForSorting_22(): String = pf.calculateBaseOfFromForSorting(this)

class SortCardsTest {

    @Test
    @DisplayName("calculateBaseOfFromForSorting_22")
    fun test_calculateBaseOfFromForSorting_22() {
        val a = SoftAssertions()

        a.assertThat("to allow credit".calculateBaseOfFromForSorting_22()).isEqualTo("credit")
        a.assertThat(" \t \n to allow credit".calculateBaseOfFromForSorting_22()).isEqualTo("credit")
        a.assertThat("TO ALLOW CREDIT".calculateBaseOfFromForSorting_22()).isEqualTo("credit")

        a.assertThat("to be cut up".calculateBaseOfFromForSorting_22()).isEqualTo("cut up")
        a.assertThat("to be in trouble".calculateBaseOfFromForSorting_22()).isEqualTo("trouble")
        a.assertThat("to be off one's nut".calculateBaseOfFromForSorting_22()).isEqualTo("nut")
        a.assertThat("to be on a roll".calculateBaseOfFromForSorting_22()).isEqualTo("roll")
        a.assertThat("to be on the watch for".calculateBaseOfFromForSorting_22()).isEqualTo("watch for")
        a.assertThat("to be up to".calculateBaseOfFromForSorting_22()).isEqualTo("up to")

        a.assertThat("to do smb. credit".calculateBaseOfFromForSorting_22()).isEqualTo("credit")

        a.assertThat("to get a sleep".calculateBaseOfFromForSorting_22()).isEqualTo("sleep")
        a.assertThat("to get enough sleep".calculateBaseOfFromForSorting_22()).isEqualTo("sleep")
        a.assertThat("to get into trouble".calculateBaseOfFromForSorting_22()).isEqualTo("trouble")
        a.assertThat("to get over the hump".calculateBaseOfFromForSorting_22()).isEqualTo("hump")
        a.assertThat("to get the hump".calculateBaseOfFromForSorting_22()).isEqualTo("hump")

        a.assertThat("to give the mitten".calculateBaseOfFromForSorting_22()).isEqualTo("mitten")

        a.assertThat("to go into the question".calculateBaseOfFromForSorting_22()).isEqualTo("question")
        a.assertThat("to go to the woods".calculateBaseOfFromForSorting_22()).isEqualTo("woods")
        a.assertThat("to handle without mittens".calculateBaseOfFromForSorting_22()).isEqualTo("mittens")
        a.assertThat("to have a common purse".calculateBaseOfFromForSorting_22()).isEqualTo("purse")
        a.assertThat("to take smb. welcome".calculateBaseOfFromForSorting_22()).isEqualTo("welcome")
        a.assertThat("to take the trouble".calculateBaseOfFromForSorting_22()).isEqualTo("trouble")
        a.assertThat("to take the trouble".calculateBaseOfFromForSorting_22()).isEqualTo("trouble")
        a.assertThat("to try one's luck".calculateBaseOfFromForSorting_22()).isEqualTo("luck")

        a.assertThat("a door to success".calculateBaseOfFromForSorting_22()).isEqualTo("door to success")
        a.assertThat("out of doors".calculateBaseOfFromForSorting_22()).isEqualTo("doors")
        a.assertThat("on the neck".calculateBaseOfFromForSorting_22()).isEqualTo("neck")
        a.assertThat("on the neck".calculateBaseOfFromForSorting_22()).isEqualTo("neck")
        a.assertThat("not for nuts".calculateBaseOfFromForSorting_22()).isEqualTo("nuts")
        a.assertThat("not a snap".calculateBaseOfFromForSorting_22()).isEqualTo("snap")
        a.assertThat("not a snap".calculateBaseOfFromForSorting_22()).isEqualTo("snap")
        a.assertThat("under a spell".calculateBaseOfFromForSorting_22()).isEqualTo("spell")
        a.assertThat("what's the trouble?".calculateBaseOfFromForSorting_22()).isEqualTo("trouble?")
        a.assertThat("at present".calculateBaseOfFromForSorting_22()).isEqualTo("present")
        a.assertThat("at present".calculateBaseOfFromForSorting_22()).isEqualTo("present")
        a.assertThat("bad luck".calculateBaseOfFromForSorting_22()).isEqualTo("luck")
        a.assertThat("by feel".calculateBaseOfFromForSorting_22()).isEqualTo("feel")
        a.assertThat("front door".calculateBaseOfFromForSorting_22()).isEqualTo("door")
        a.assertThat("good luck".calculateBaseOfFromForSorting_22()).isEqualTo("luck")
        a.assertThat("good luck".calculateBaseOfFromForSorting_22()).isEqualTo("luck")
        a.assertThat("next door".calculateBaseOfFromForSorting_22()).isEqualTo("door")
        a.assertThat("next door".calculateBaseOfFromForSorting_22()).isEqualTo("door")
        a.assertThat("up to the neck".calculateBaseOfFromForSorting_22()).isEqualTo("neck")
        a.assertThat("to be on the watch for".calculateBaseOfFromForSorting_22()).isEqualTo("watch for")

        a.assertThat("to go at".calculateBaseOfFromForSorting_22()).isEqualTo("go at")
        //a.assertThat("to go at".calculateBaseOfFromForSorting_22()).isEqualTo("at")

        a.assertThat("on no account".calculateBaseOfFromForSorting_22()).isEqualTo("account")
        a.assertThat("long odds".calculateBaseOfFromForSorting_22()).isEqualTo("odds")
        a.assertThat("on one's own account".calculateBaseOfFromForSorting_22()).isEqualTo("account")
        a.assertThat("to have one's own way".calculateBaseOfFromForSorting_22()).isEqualTo("way")

        a.assertAll()
    }

    @Test
    fun performanceTest() {
        val words = Files.readAllLines(Path.of("/home/vmelnykov/projects/words/learn-words/src/test/resources/Friends S01E03.txt"))

        log.info { "performanceTest => words (${words.size}) are loaded" }
        log.info { "performanceTest => calculating started" }
        val sw = startStopWatch()

        words.forEach { it.calculateBaseOfFromForSorting_22() }
        log.info { "performanceTest => calculating completed (took ${sw.time}ms)" }
    }
}
