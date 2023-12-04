package com.mvv.gui.words

import com.mvv.gui.test.useAssertJSoftAssertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName


class EnglishVerbsTest {

    @Test
    @DisplayName("irregularVerbs variable")
    fun test_irregularVerbs() {
        val v = irregularVerbs.find { it.base == "throw" }

        assertThat(v).isNotNull
        requireNotNull(v)

        assertThat(v.base).isEqualTo("throw")
        assertThat(v.pastTense).containsExactly("threw")
        assertThat(v.pastParticiple).containsExactly("thrown")
    }

    @Test
    fun getIrregularInfinitive() {
        val a = SoftAssertions()

        val verbs = EnglishVerbs()

        a.assertThat(verbs.getIrregularInfinitive("heard")).isEqualTo("hear")

        a.assertThat(verbs.getIrregularInfinitive("throw")).isEqualTo("throw")
        a.assertThat(verbs.getIrregularInfinitive("thrown")).isEqualTo("throw")
        a.assertThat(verbs.getIrregularInfinitive("threw")).isEqualTo("throw")

        a.assertThat(verbs.getIrregularInfinitive("be")).isEqualTo("be")
        a.assertThat(verbs.getIrregularInfinitive("was")).isEqualTo("be")
        a.assertThat(verbs.getIrregularInfinitive("were")).isEqualTo("be")
        //
        a.assertThat(verbs.getIrregularInfinitive("is")).isEqualTo("be")
        a.assertThat(verbs.getIrregularInfinitive("am")).isEqualTo("be")
        a.assertThat(verbs.getIrregularInfinitive("are")).isEqualTo("be")

        a.assertAll()
    }

    @Test
    fun getIrregularInfinitive_forGerund() { useAssertJSoftAssertions {
        val verbs = EnglishVerbs()

        assertThat(verbs.getIrregularInfinitive("throwing")).isEqualTo("throw")
        assertThat(verbs.getIrregularInfinitive("cutting")).isEqualTo("cut")
        assertThat(verbs.getIrregularInfinitive("running")).isEqualTo("run")
        assertThat(verbs.getIrregularInfinitive("buying")).isEqualTo("buy")
        assertThat(verbs.getIrregularInfinitive("inlaying")).isEqualTo("inlay")

        // assertThat(verbs.getIrregularInfinitive("dying")).isEqualTo("die")
    } }

    @Test
    fun getIrregularInfinitive_for3rdForm() { useAssertJSoftAssertions {
        val verbs = EnglishVerbs()

        assertThat(verbs.getIrregularInfinitive("throws")).isEqualTo("throw")
        assertThat(verbs.getIrregularInfinitive("cuts")).isEqualTo("cut")
        assertThat(verbs.getIrregularInfinitive("makes")).isEqualTo("make")
    } }

    @Test
    @Disabled
    @Suppress("DEPRECATION")
    fun getInfinitive() {
        val a = SoftAssertions()

        val verbs = EnglishVerbs()

        // irregular
        a.assertThat(verbs.getInfinitive("heard")).isEqualTo("hear")

        // -ied
        a.assertThat(verbs.getInfinitive("hurried")).isEqualTo("hurry")
        a.assertThat(verbs.getInfinitive("died")).isEqualTo("die")

        // others
        a.assertThat(verbs.getInfinitive("played")).isEqualTo("play")

        a.assertThat(verbs.getInfinitive("cooked")).isEqualTo("cook")

        a.assertThat(verbs.getInfinitive("rained")).isEqualTo("rain")
        a.assertThat(verbs.getInfinitive("complained")).isEqualTo("complain")
        a.assertThat(verbs.getInfinitive("waited")).isEqualTo("wait")

        a.assertThat(verbs.getInfinitive("crossed")).isEqualTo("cross")

        a.assertThat(verbs.getInfinitive("sailed")).isEqualTo("sail")

        // -e
        a.assertThat(verbs.getInfinitive("arrived")).isEqualTo("arrive")

        a.assertThat(verbs.getInfinitive("washed")).isEqualTo("wash")
        a.assertThat(verbs.getInfinitive("finished")).isEqualTo("finish")

        // doubled
        a.assertThat(verbs.getInfinitive("visited")).isEqualTo("visit")
        a.assertThat(verbs.getInfinitive("stopped")).isEqualTo("stop")
        a.assertThat(verbs.getInfinitive("planned")).isEqualTo("plan")
        a.assertThat(verbs.getInfinitive("preferred")).isEqualTo("prefer")
        a.assertThat(verbs.getInfinitive("admitted")).isEqualTo("admit")


        a.assertThat(verbs.getInfinitive("postponed")).isEqualTo("postpone")

        a.assertAll()
    }
}