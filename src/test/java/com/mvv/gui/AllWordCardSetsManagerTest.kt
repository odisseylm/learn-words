package com.mvv.gui

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.DisplayName


class AllWordCardSetsManagerTest {

    @Test
    @DisplayName("splitTranslation withoutBrackets")
    fun test_splitTranslation_withoutBrackets() {
        val a = SoftAssertions()

        a.assertThat(" страшный ".splitTranslation()).containsExactly("страшный")
        a.assertThat(" страшный человек ".splitTranslation()).containsExactly("страшный человек")
        a.assertThat(" страшный  человек ".splitTranslation()).containsExactly("страшный человек")

        a.assertAll()
    }

    @Test
    @DisplayName("splitTranslation skipMiddleWordsInBrackets")
    fun test_splitTranslation_skipMiddleWordsInBrackets() {
        assertThat(" страшный  (ужасный)  человек".splitTranslation())
            .containsExactlyInAnyOrder("страшный (ужасный) человек", "страшный человек")

        assertThat(" страшный  (ужасный)  (какой-то текст)  человек ".splitTranslation())
            .containsExactlyInAnyOrder("страшный (ужасный) (какой-то текст) человек", "страшный человек")

        assertThat(" (разг.) столкнуться (с чем-л.) ".splitTranslation())
            .containsExactlyInAnyOrder("(разг.) столкнуться (с чем-л.)", "столкнуться")
    }

    // TODO: to test
    // "алкогольный (спиртовой)"
    // TODO: to impl
    // "кишащий (переполненный) чем-то" // remove "чем-то" and process as usual
    // "класть (деньги) в банк"  ??? should we use key "класть" ??? only 1st verb
    // "бить (сильно ударять)"   ???
    // "(амер.)(разг.) гулянка (весёлое сборище)" ???
    // "бык или корова (откормленные на убой)"
    // "задувать (гасить тушить) (свечу керосиновую лампу и т.п.)"
    // "(разг.) пошёл вон!, убирайся!"
    // "вперёд!; продолжайте!"
    // "идёт?; по рукам?"
    // "(разг.) бросаться на к-л"
    // "энергично браться за ч-л"
    // "превышать ч-л"
    // parse translations correctly
    //   "иметь большое значение, влияние (to, towards, with)"
    //   "изучать (основания, данные)"
    //
    // "идти за ч-л"
    // "стремиться к ч-л"
    // "стать хуже (испортиться  о мясе и тп)"
    // "отделаться от ч-л"
    // "продолжать делание ч-л"
    // "огорчать кого-л."
    // "быть заодно с к-л"
    // "подходить (гармонировать согласовываться соответствовать)"
    // "обходиться без чего-л."
    // "выпустить что-л. из рук"
    // "собираться сделать что-то"
    // "проваливайте!"
    // "большой (объёмистый)"
    // "грубый (немузыкальный) слух"
    // "защищать от ч-л (xxx from)"
    // "стой!; подожди!"
    // "выдерживать (держаться до конца)"
    // "(ам.) утаить от кого-л."
    // "уважать кого-л."
    // "возлагать на кого-л. ответственность"
    // ??? "праздник (день отдыха)"
    // "оттиск (штемпель печать чего-л.)"
    // "продолжать делать (ч-л)"
    // "продолжать делание (ч-л)"
    // "делать (ч-л) с упорством (настойчиво)"
    // "заставлять (к-л) делать (ч-л)"
    // "воздерживаться от чего-л."
    // "оставаться в хороших отношениях с кем-л."
    // "(разг.) беспрестанно бранить кого-л."
    // "рассчитывать на ч-л"
    // "смотри(те) в оба!"
    // "заботиться о
    // "следить за"
    // "рассчитывать на (надеяться на)"
    // "стремиться (быть направленным) к ч-л (на ч-л)"
    // "иметь склонность к ч-л"
    // "указывать на""


    @Test
    @DisplayName("splitTranslation 2 adjectives")
    fun test_splitTranslation_twoAdjectives() {
        assertThat("страшный (ужасный)".splitTranslation())
            .containsExactlyInAnyOrder("страшный (ужасный)", "страшный", "ужасный")
    }

    @Test
    @DisplayName("splitTranslation 3 adjectives")
    fun test_splitTranslation_threeAdjectives() {
        assertThat(" страшный (ужасный жуткий) ".splitTranslation())
            .containsExactlyInAnyOrder("страшный (ужасный жуткий)", "страшный", "ужасный", "жуткий")
    }

    @Test
    @DisplayName("splitTranslation verbCase1")
    fun test_splitTranslation_verbCase1() {
        assertThat("доверять(ся) (полагать(ся))".splitTranslation())
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
        assertThat(" доверяться (полагаться опираться) ".splitTranslation())
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
        assertThat(" доверять(ся) (полагаться опираться) ".splitTranslation())
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
        assertThat(" доверять(ся) (полагать опирать) ".splitTranslation())
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
        assertThat(" доверять(ся) (полагать(ся) опирать(ся)) ".splitTranslation())
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
    @DisplayName("splitTranslation verbCase4")
    fun test_splitTranslation_verbCase4() {
        val a = SoftAssertions()

        a.assertThat(" доверять к-л ".splitTranslation())
            .containsExactlyInAnyOrder("доверять к-л", "доверять")

        a.assertThat(" доверять кому-л ".splitTranslation())
            .containsExactlyInAnyOrder("доверять кому-л", "доверять")

        a.assertThat(" доверять кому-либо ".splitTranslation())
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
