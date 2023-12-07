package com.mvv.gui.cardeditor

import com.mvv.gui.test.useAssertJSoftAssertions
import com.mvv.gui.words.Part
import com.mvv.gui.words.splitByBrackets
import com.mvv.gui.words.splitTranslationToIndexed
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.RepeatedTest


class AllWordCardSetsManagerTest {

    @Test
    @DisplayName("splitTranslation withoutBrackets")
    fun test_splitTranslation_withoutBrackets() {
        val a = SoftAssertions()

        a.assertThat(" страшный ".splitTranslationToIndexed()).containsExactly("страшный")
        a.assertThat(" страшный человек ".splitTranslationToIndexed()).containsExactly("страшный человек")
        a.assertThat(" страшный  человек ".splitTranslationToIndexed()).containsExactly("страшный человек")

        a.assertAll()
    }

    //@Test
    @RepeatedTest(2)
    @DisplayName("splitTranslation skipMiddleWordsInBrackets")
    fun test_splitTranslation_skipMiddleWordsInBrackets() {
        assertThat(" страшный  (наверное)  человек".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("страшный (наверное) человек", "страшный человек")
        assertThat("алкогольный (спиртовой)".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("алкогольный (спиртовой)", "алкогольный", "спиртовой")

        assertThat(" страшный  (ужасный)  (какой-то текст)  человек ".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("страшный (ужасный) (какой-то текст) человек", "страшный человек")

        assertThat(" (разг.) столкнуться (с чем-л.) ".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("столкнуться (с чем-л.)", "столкнуться")

        assertThat("класть (деньги) в банк".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("класть (деньги) в банк", "класть в банк")
    }

    @Test
    @DisplayName("splitTranslation 333")
    fun test_splitTranslation_333() {
        assertThat(" страшный  (ужасный)  человек".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("страшный (ужасный) человек", "страшный человек", "ужасный человек")
        assertThat("алкогольный (спиртовой)".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("алкогольный (спиртовой)", "алкогольный", "спиртовой")

        assertThat(" страшный  (ужасный)  (какой-то текст)  человек ".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("страшный (ужасный) (какой-то текст) человек", "страшный человек")

        assertThat(" (разг.) столкнуться (с чем-л.) ".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("столкнуться (с чем-л.)", "столкнуться")
    }

    @Test
    @DisplayName("splitTranslation verbsWithPreposition")
    fun test_splitTranslation_verbsWithPreposition() { useAssertJSoftAssertions {
        assertThat("превышать ч-л".splitTranslationToIndexed()).containsExactlyInAnyOrder("превышать ч-л", "превышать")
        assertThat("заботиться о".splitTranslationToIndexed()).containsExactlyInAnyOrder("заботиться о", "заботиться")
        assertThat("следить за".splitTranslationToIndexed()).containsExactlyInAnyOrder("следить за", "следить")
        assertThat("огорчать кого-л.".splitTranslationToIndexed()).containsExactlyInAnyOrder("огорчать кого-л.", "огорчать")
        assertThat("иметь склонность к ч-л".splitTranslationToIndexed()).containsExactlyInAnyOrder("иметь склонность к ч-л", "иметь склонность")
        assertThat("указывать на".splitTranslationToIndexed()).containsExactlyInAnyOrder("указывать на", "указывать")
        assertThat("уважать кого-л.".splitTranslationToIndexed()).containsExactlyInAnyOrder("уважать кого-л.", "уважать")
        assertThat("идти за ч-л".splitTranslationToIndexed()).containsExactlyInAnyOrder("идти за ч-л")
        assertThat("стремиться к ч-л".splitTranslationToIndexed()).containsExactlyInAnyOrder("стремиться к ч-л", "стремиться")
        assertThat("рассчитывать на ч-л".splitTranslationToIndexed()).containsExactlyInAnyOrder("рассчитывать на ч-л", "рассчитывать")
        assertThat("воздерживаться от чего-л.".splitTranslationToIndexed()).containsExactlyInAnyOrder("воздерживаться от чего-л.", "воздерживаться")
        assertThat("быть заодно с к-л".splitTranslationToIndexed()).containsExactlyInAnyOrder("быть заодно с к-л", "быть заодно")
        assertThat("обходиться без".splitTranslationToIndexed()).containsExactlyInAnyOrder("обходиться без", "обходиться")
        assertThat("обходиться без чего-л.".splitTranslationToIndexed()).containsExactlyInAnyOrder("обходиться без чего-л.", "обходиться")
        assertThat("защищать от ч-л (xxx from)".splitTranslationToIndexed()).containsExactlyInAnyOrder("защищать от ч-л (xxx from)", "защищать от ч-л", "защищать")
        assertThat("(разг.) бросаться на к-л".splitTranslationToIndexed()).containsExactlyInAnyOrder("бросаться на к-л", "бросаться")
        assertThat("отделаться от ч-л".splitTranslationToIndexed()).containsExactlyInAnyOrder("отделаться от ч-л", "отделаться")
        assertThat("оставаться в хороших отношениях с кем-л.".splitTranslationToIndexed()).containsExactlyInAnyOrder(
            "оставаться в хороших отношениях с кем-л.", "оставаться в хороших отношениях")
        assertThat("(ам.) утаить от кого-л.".splitTranslationToIndexed()).containsExactlyInAnyOrder("утаить от кого-л.", "утаить")

        assertThat("стремиться (быть направленным) к ч-л (на ч-л)".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("стремиться (быть направленным) к ч-л (на ч-л)", "стремиться к ч-л", "стремиться")
    } }


    @Test
    @DisplayName("splitTranslation synonymsAdjectives")
    fun test_splitTranslation_synonymsAdjectives() { useAssertJSoftAssertions {

        assertThat("кишащий (переполненный) чем-то".splitTranslationToIndexed())
            .containsExactlyInAnyOrder(
                "кишащий (переполненный) чем-то",
                "кишащий (переполненный)",
                "кишащий чем-то",
                "переполненный",
                "переполненный чем-то",
                "кишащий",
            )

        assertThat("большой (объёмистый)".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("большой (объёмистый)", "большой", "объёмистый")
    } }


    @Test
    @DisplayName("splitTranslation synonymsVerbs")
    fun test_splitTranslation_synonymsVerbs() { useAssertJSoftAssertions {

        assertThat("подходить (гармонировать согласовываться соответствовать)".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("подходить (гармонировать согласовываться соответствовать)",
                "подходить", "гармонировать", "согласовываться", "соответствовать")

        assertThat("задувать (гасить тушить) (свечу керосиновую лампу и т.п.)".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("задувать (гасить тушить) (свечу керосиновую лампу и т.п.)", "задувать", "гасить", "тушить")
    } }


    @Test
    @DisplayName("splitTranslation toRemoveOptionalVerb")
    fun test_splitTranslation_toRemoveOptionalVerb() { useAssertJSoftAssertions {
        assertThat("продолжать делание ч-л".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("продолжать делание ч-л", "продолжать")

        assertThat("продолжать делать (ч-л)".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("продолжать делать (ч-л)", "продолжать делать", "продолжать")

        assertThat("собираться сделать что-то".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("собираться сделать что-то", "собираться")

        assertThat("собираться делать что-то".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("собираться делать что-то", "собираться")

        assertThat("заставлять (к-л) делать (ч-л)".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("заставлять (к-л) делать (ч-л)", "заставлять делать", "заставлять")

        assertThat("продолжать делать (ч-л)".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("продолжать делать (ч-л)", "продолжать делать", "продолжать")
    } }


    @Test
    @DisplayName("splitTranslation question")
    fun test_splitTranslation_question() { useAssertJSoftAssertions {
        // without changes
        assertThat("идёт?".splitTranslationToIndexed()).containsExactlyInAnyOrder("идёт?")
        assertThat("по рукам?".splitTranslationToIndexed()).containsExactlyInAnyOrder("по рукам?")
    } }


    @Test
    @DisplayName("splitTranslation expressive")
    fun test_splitTranslation_expressive() { useAssertJSoftAssertions {

        assertThat("стой!".splitTranslationToIndexed()).containsExactlyInAnyOrder("стой!", "стой")
        assertThat("(разг.) стой!".splitTranslationToIndexed()).containsExactlyInAnyOrder("стой!", "стой")
        assertThat("убирайся!".splitTranslationToIndexed()).containsExactlyInAnyOrder("убирайся!", "убирайся")
        assertThat("подожди!".splitTranslationToIndexed()).containsExactlyInAnyOrder("подожди!", "подожди")
        assertThat("проваливайте!".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("проваливайте!", "проваливайте", "проваливай!", "проваливай")
        assertThat("продолжайте!".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("продолжайте!", "продолжайте", "продолжай!", "продолжай")
        assertThat("вперёд!".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("вперёд!", "вперёд")
        assertThat("смотри(те) в оба!".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("смотри(те) в оба!", "смотри(те) в оба", "смотри в оба!", "смотри в оба")
        assertThat("(разг.) пошёл вон!".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("пошёл вон!", "пошёл вон")
    } }


    //@Test
    @RepeatedTest(2) // to see performance at 2nd attempt (1st one takes about 1 second)
    @DisplayName("splitTranslation other cases")
    fun test_splitTranslation_cases() { useAssertJSoftAssertions {

        assertThat("(разг.) беспрестанно бранить кого-л.".splitTranslationToIndexed()).containsExactlyInAnyOrder(
            "беспрестанно бранить кого-л.",
            "беспрестанно бранить",
            "бранить",
            )
        assertThat("(разг.) бранить беспрестанно кого-л.".splitTranslationToIndexed()).containsExactlyInAnyOrder(
            "бранить беспрестанно кого-л.",
            "бранить беспрестанно",
            "бранить",
            )

        assertThat("выдерживать (держаться до конца)".splitTranslationToIndexed()).containsExactlyInAnyOrder(
            "выдерживать (держаться до конца)",
            "выдерживать",
            "держаться до конца",
            )

        assertThat("рассчитывать на (надеяться на)".splitTranslationToIndexed()).containsExactlyInAnyOrder(
            "рассчитывать на (надеяться на)", "рассчитывать на", "рассчитывать", "надеяться на", "надеяться")

        assertThat("энергично браться за ч-л".splitTranslationToIndexed())
            //.containsExactlyInAnyOrder("энергично браться за ч-л", "энергично браться", "браться за ч-л", "браться")
            .containsExactlyInAnyOrder("энергично браться за ч-л", "энергично браться", "браться")

        assertThat("бить (сильно ударять)".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("бить (сильно ударять)", "бить", "сильно ударять", "ударять")
        assertThat("бить (ударять сильно)".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("бить (ударять сильно)", "бить", "ударять сильно", "ударять")

        assertThat("бык или корова (откормленные на убой)".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("бык или корова (откормленные на убой)", "бык или корова", "бык", "корова")
        assertThat("бык или (какая-то) корова".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("бык или (какая-то) корова", "бык или корова", "бык", "корова")
        assertThat("бык или корова".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("бык или корова", "бык", "корова")

        assertThat("грубый (немузыкальный) слух".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("грубый (немузыкальный) слух", "грубый слух", "немузыкальный слух")

        // Probably impossible to impl:
        //  "возлагать на кого-л. ответственность"

        // Impossible to impl:
        //  "оттиск (штемпель печать чего-л.)"
        //  "делать (ч-л) с упорством (настойчиво)"
        //  "праздник (день отдыха)"
        //  "стать хуже (испортиться  о мясе и тп)"
    } }

    @Test
    @Disabled("for debug")
    fun testToDebug() { useAssertJSoftAssertions {

        assertThat("идти за ч-л".splitTranslationToIndexed()).containsExactlyInAnyOrder("идти за ч-л")
    } }



    @Test
    @DisplayName("splitTranslation 2 adjectives")
    fun test_splitTranslation_twoAdjectives() {
        assertThat("страшный (ужасный)".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("страшный (ужасный)", "страшный", "ужасный")
    }

    @Test
    @DisplayName("splitTranslation 3 adjectives")
    fun test_splitTranslation_threeAdjectives() {
        assertThat(" страшный (ужасный жуткий) ".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("страшный (ужасный жуткий)", "страшный", "ужасный", "жуткий")
    }

    @Test
    @DisplayName("splitTranslation verbCase1")
    fun test_splitTranslation_verbCase1() {
        assertThat("доверять(ся) (полагать(ся))".splitTranslationToIndexed())
            .containsExactlyInAnyOrder(
                // full unchanged string
                "доверять(ся) (полагать(ся))",
                // first
                "доверять", "доверять(ся)", "доверяться",
                // second
                "полагать", "полагать(ся)", "полагаться",
            )
    }

    @Test
    @DisplayName("splitTranslation verbCase2_0")
    fun test_splitTranslation_verbCase2_0() {
        assertThat(" доверяться (полагаться опираться) ".splitTranslationToIndexed())
            .containsExactlyInAnyOrder(
                // full unchanged string
                "доверяться (полагаться опираться)",
                // first
                "доверяться",
                // second
                "полагаться",
                // third
                "опираться",
            )
    }

    @Test
    @DisplayName("splitTranslation verbCase2_1")
    fun test_splitTranslation_verbCase2_1() {
        assertThat(" доверять(ся) (полагаться опираться) ".splitTranslationToIndexed())
            .containsExactlyInAnyOrder(
                // full unchanged string
                "доверять(ся) (полагаться опираться)",
                // first
                "доверять", "доверять(ся)", "доверяться",
                // second
                "полагаться",
                // third
                "опираться",
            )
    }

    @Test
    @DisplayName("splitTranslation verbCase2_2")
    fun test_splitTranslation_verbCase2_2() {
        assertThat(" доверять(ся) (полагать опирать) ".splitTranslationToIndexed())
            .containsExactlyInAnyOrder(
                // full unchanged string
                "доверять(ся) (полагать опирать)",
                // first
                "доверять", "доверять(ся)", "доверяться",
                // second
                "полагать",
                // third
                "опирать",
            )
    }

    @Test
    @DisplayName("splitTranslation verbCase3")
    fun test_splitTranslation_verbCase3() {
        assertThat(" доверять(ся) (полагать(ся) опирать(ся)) ".splitTranslationToIndexed())
            .containsExactlyInAnyOrder(
                // full unchanged string
                "доверять(ся) (полагать(ся) опирать(ся))",
                // first
                "доверять", "доверять(ся)", "доверяться",
                // second
                "полагать", "полагать(ся)", "полагаться",
                // third
                "опирать", "опирать(ся)", "опираться",
            )
    }

    @Test
    @DisplayName("splitTranslation verbCase3_1")
    fun test_splitTranslation_verbCase3_1() {
        assertThat(" доверять(ся) (полагать(ся), опирать(ся)) ".splitTranslationToIndexed())
            .containsExactlyInAnyOrder(
                // full unchanged string
                "доверять(ся) (полагать(ся), опирать(ся))",
                // first
                "доверять", "доверять(ся)", "доверяться",
                // second
                "полагать", "полагать(ся)", "полагаться",
                // third
                "опирать", "опирать(ся)", "опираться",
            )
    }

    @Test
    @DisplayName("splitTranslation verbCase4")
    fun test_splitTranslation_verbCase4() {
        val a = SoftAssertions()

        a.assertThat(" доверять к-л ".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("доверять к-л", "доверять")

        a.assertThat(" доверять кому-л ".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("доверять кому-л", "доверять")

        a.assertThat(" доверять кому-либо ".splitTranslationToIndexed())
            .containsExactlyInAnyOrder("доверять кому-либо", "доверять")

        a.assertAll()
    }

    @Test
    @DisplayName("splitByBrackets")
    fun test_splitByBrackets() {
        val a = SoftAssertions()

        run {
            val s = "страшный (ужасный) человек"
            a.assertThat(s.splitByBrackets().map { it.asSubContent() }).containsExactly("страшный ", "(ужасный)", " человек")
            a.assertThat(s.splitByBrackets().map { it.asTextOnly() }).containsExactly("страшный ", "ужасный", " человек")
            a.assertThat(s.splitByBrackets()).containsExactly(
                Part.withoutBrackets(s, 0, 9),
                Part.inBrackets(s, 9, 17),
                Part.withoutBrackets(s, 18, s.length),
            )
        }

        run {
            val s = "доверять(ся) (полагать(ся))"
            a.assertThat(s.splitByBrackets().map { it.asSubContent() }).containsExactly("доверять", "(ся)", " ", "(полагать(ся))")
            a.assertThat(s.splitByBrackets().map { it.asTextOnly() }).containsExactly("доверять", "ся", " ", "полагать(ся)")
            a.assertThat(s.splitByBrackets()).containsExactly(
                Part.withoutBrackets(s, 0, 8),
                Part.inBrackets(s, 8, 11),
                Part.withoutBrackets(s, 12, 13),
                Part.inBrackets(s, 13, 26),
            )
        }

        a.assertAll()
    }
}
