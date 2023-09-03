package com.mvv.gui.words

import com.mvv.gui.dictionary.DictDictionary
import com.mvv.gui.dictionary.DictDictionarySource
import com.mvv.gui.dictionary.Dictionary
import com.mvv.gui.dictionary.getProjectDirectory
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class BaseWordsTest {

    private fun getProjectDir() = getProjectDirectory(this.javaClass)

    private fun dictionary(): Dictionary {
        val dictionaryRootDir = getProjectDir().resolve("dicts/mueller-dict-3.1.1/dict")
        return DictDictionary(DictDictionarySource(
            "mueller-base",
            dictionaryRootDir,
            dictionaryRootDir.resolve("mueller-base.dict.dz"),
            dictionaryRootDir.resolve("mueller-base.index"),
        ))
    }


    @Test
    @DisplayName("englishBaseWords -ed")
    fun test_englishBaseWords_endsWithEd() {
        val dictionary = dictionary()

        val assertions = SoftAssertions()

        assertions.assertThat(englishBaseWords("worked", dictionary).map { it.from })
            .containsExactlyInAnyOrder("work")

        assertions.assertThat(englishBaseWords("whipped", dictionary).map { it.from })
            .containsExactlyInAnyOrder("whip")

        assertions.assertAll()
    }


    @Test
    @DisplayName("englishBaseWords -ing")
    fun test_englishBaseWords_endsWithIng() {
        val dictionary = dictionary()

        val assertions = SoftAssertions()

        assertions.assertThat(englishBaseWords("working", dictionary).map { it.from })
            .containsExactlyInAnyOrder("work")

        assertions.assertAll()
    }


    @Test
    @DisplayName("englishBaseWords -ion")
    fun test_englishBaseWords_endsWithIon() {
        val dictionary = dictionary()

        val assertions = SoftAssertions()

        assertions.assertThat(englishBaseWords("possession", dictionary).map { it.from })
            .containsExactlyInAnyOrder("possess")

        assertions.assertAll()
    }


    @Test
    @DisplayName("englishBaseWords -ar")
    fun test_englishBaseWords_endsWithAr() {
        val dictionary = dictionary()

        val assertions = SoftAssertions()

        assertions.assertThat(englishBaseWords("scholar", dictionary).map { it.from })
            .isEmpty()

        assertions.assertThat(englishBaseWords("polar", dictionary).map { it.from })
            .containsExactlyInAnyOrder("pole")

        assertions.assertThat(englishBaseWords("linear", dictionary).map { it.from })
            .containsExactlyInAnyOrder("line")

        assertions.assertThat(englishBaseWords("molecular", dictionary).map { it.from })
            .containsExactlyInAnyOrder("molecule")

        assertions.assertThat(englishBaseWords("beggar", dictionary).map { it.from })
            .containsExactlyInAnyOrder("beg")

        assertions.assertThat(englishBaseWords("liar", dictionary).map { it.from })
            .containsExactlyInAnyOrder("lie")

        assertions.assertThat(englishBaseWords("angular", dictionary).map { it.from })
            .containsExactlyInAnyOrder("angle")

        assertions.assertThat(englishBaseWords("cellular", dictionary).map { it.from })
            .containsExactlyInAnyOrder(
                "cell",
                "cellule", // strange... (like Italian word)
            )

        assertions.assertAll()
    }


    @Test
    @DisplayName("englishBaseWords -es")
    fun test_englishBaseWords_endsWithEs() {
        val dictionary = dictionary()

        val assertions = SoftAssertions()

        assertions.assertThat(englishBaseWords("trees", dictionary).map { it.from })
            .containsExactlyInAnyOrder("tree")

        assertions.assertThat(englishBaseWords("thieves", dictionary).map { it.from })
            .containsExactlyInAnyOrder(
                "thief",  // noun
                "thieve", // verb
            )

        assertions.assertThat(englishBaseWords("bandages", dictionary).map { it.from })
            .containsExactlyInAnyOrder("bandage")

        assertions.assertThat(englishBaseWords("anecdotes", dictionary).map { it.from })
            .containsExactlyInAnyOrder("anecdote")

        assertions.assertThat(englishBaseWords("accretes", dictionary).map { it.from })
            .containsExactlyInAnyOrder("accrete")

        assertions.assertThat(englishBaseWords("hormones", dictionary).map { it.from })
            .containsExactlyInAnyOrder("hormone")

        assertions.assertThat(englishBaseWords("slides", dictionary).map { it.from })
            .containsExactlyInAnyOrder(
                "slid", // strange... it has translation??
                "slide",
            )

        // !!! nice sample
        assertions.assertThat(englishBaseWords("stares", dictionary).map { it.from })
            .containsExactlyInAnyOrder("stare", "star")

        assertions.assertThat(englishBaseWords("toes", dictionary).map { it.from })
            .containsExactlyInAnyOrder("toe")

        assertions.assertThat(englishBaseWords("hooves", dictionary).map { it.from })
            .containsExactlyInAnyOrder(
                "hoof",
                "hoove", // seems it also has translation, but should not
            )

        assertions.assertThat(englishBaseWords("mummies", dictionary).map { it.from })
            .containsExactlyInAnyOrder("mummy")

        assertions.assertThat(englishBaseWords("obvious", dictionary).map { it.from })
            .isEmpty()

        assertions.assertAll()
    }


    @Test
    @DisplayName("englishBaseWords -s")
    fun test_englishBaseWords_endsWithS() {
        val dictionary = dictionary()

        val assertions = SoftAssertions()

        assertions.assertThat(englishBaseWords("culprits", dictionary).map { it.from })
            .containsExactlyInAnyOrder("culprit")

        assertions.assertThat(englishBaseWords("thus", dictionary).map { it.from })
            .isEmpty()

        assertions.assertThat(englishBaseWords("trespass", dictionary).map { it.from })
            .isEmpty()

        assertions.assertThat(englishBaseWords("obvious", dictionary).map { it.from })
            .isEmpty()

        assertions.assertThat(englishBaseWords("embarrass", dictionary).map { it.from })
            .isEmpty()

        assertions.assertThat(englishBaseWords("flatness", dictionary).map { it.from })
            .isEmpty()

        assertions.assertThat(englishBaseWords("folks", dictionary).map { it.from })
            .containsExactlyInAnyOrder("folk")

        assertions.assertThat(englishBaseWords("nervous", dictionary).map { it.from })
            .containsExactlyInAnyOrder("nerve")

        assertions.assertThat(englishBaseWords("pros", dictionary).map { it.from })
            .containsExactlyInAnyOrder("pro")

        assertions.assertThat(englishBaseWords("waitress", dictionary).map { it.from })
            .containsExactlyInAnyOrder("wait")

        assertions.assertThat(englishBaseWords("grievous", dictionary).map { it.from })
            .containsExactlyInAnyOrder("grieve")

        assertions.assertThat(englishBaseWords("injurious", dictionary).map { it.from })
            .isEmpty()

        assertions.assertThat(englishBaseWords("possess", dictionary).map { it.from })
            .isEmpty()

        assertions.assertThat(englishBaseWords("tedious", dictionary).map { it.from })
            .isEmpty()

        assertions.assertThat(englishBaseWords("portcullis", dictionary).map { it.from })
            .isEmpty()

        assertions.assertThat(englishBaseWords("flies", dictionary).map { it.from })
            .containsExactlyInAnyOrder("fly")

        assertions.assertAll()
    }


    @Test
    @DisplayName("englishBaseWords a-")
    fun test_englishBaseWords_startsWithA() {
        val dictionary = dictionary()

        val assertions = SoftAssertions()

        assertions.assertThat(englishBaseWords("amass", dictionary).map { it.from })
            .containsExactlyInAnyOrder("mass")

        assertions.assertThat(englishBaseWords("aside", dictionary).map { it.from })
            .containsExactlyInAnyOrder("side")

        assertions.assertThat(englishBaseWords("abide", dictionary).map { it.from })
            .containsExactlyInAnyOrder("bide")

        assertions.assertAll()
    }


    @Test
    @DisplayName("englishBaseWords re-")
    fun test_englishBaseWords_startsWithRe() {
        val dictionary = dictionary()

        val assertions = SoftAssertions()

        assertions.assertThat(englishBaseWords("replace", dictionary).map { it.from })
            .containsExactlyInAnyOrder("place")

        assertions.assertThat(englishBaseWords("regain", dictionary).map { it.from })
            .containsExactlyInAnyOrder("gain")

        assertions.assertThat(englishBaseWords("redress", dictionary).map { it.from })
            .containsExactlyInAnyOrder(
                "dress",
                "red", // unexpected word, but it would be difficult to filter it out :-)
            )

        assertions.assertAll()
    }


    @Test
    @DisplayName("englishBaseWords -ly")
    fun test_englishBaseWords_endsWithLy() {
        val dictionary = dictionary()

        val assertions = SoftAssertions()

        assertions.assertThat(englishBaseWords("directly", dictionary).map { it.from })
            .containsExactlyInAnyOrder("direct")

        assertions.assertAll()
    }


    @Test
    @DisplayName("englishBaseWords -ingly")
    fun test_englishBaseWords_endsWithIngly() {
        val dictionary = dictionary()

        val assertions = SoftAssertions()

        assertions.assertThat(englishBaseWords("boringly", dictionary).map { it.from })
            .containsExactlyInAnyOrder("boring", "bore")

        assertions.assertAll()
    }


    @Test
    @DisplayName("englishBaseWords -able")
    fun test_englishBaseWords_endsWithAble() {
        val dictionary = dictionary()

        val assertions = SoftAssertions()

        assertions.assertThat(englishBaseWords("acceptable", dictionary).map { it.from })
            .containsExactlyInAnyOrder("accept")

        assertions.assertThat(englishBaseWords("regretable", dictionary).map { it.from })
            .containsExactlyInAnyOrder("regret")

        assertions.assertThat(englishBaseWords("considerable", dictionary).map { it.from })
            .containsExactlyInAnyOrder("consider")

        //assertions.assertThat(englishBaseWords("abdicable", dictionary).map { it.from })
        //    .containsExactlyInAnyOrder("abdicate")

        assertions.assertThat(englishBaseWords("accruable", dictionary).map { it.from })
            .containsExactlyInAnyOrder("accrue")

        assertions.assertThat(englishBaseWords("arguable", dictionary).map { it.from })
            .containsExactlyInAnyOrder("argue")

        assertions.assertThat(englishBaseWords("debatable", dictionary).map { it.from })
            .containsExactlyInAnyOrder("debate")

        assertions.assertThat(englishBaseWords("vulnerable", dictionary).map { it.from })
            .isEmpty()

        assertions.assertThat(englishBaseWords("vocable", dictionary).map { it.from })
            .isEmpty()

        assertions.assertThat(englishBaseWords("probable", dictionary).map { it.from })
            .containsExactlyInAnyOrder("probe")

        assertions.assertThat(englishBaseWords("hospitable", dictionary).map { it.from })
            .isEmpty()

        assertions.assertAll()
    }


    @Test
    @DisplayName("englishBaseWords -ible")
    fun test_englishBaseWords_endsWithIble() {
        val dictionary = dictionary()

        val assertions = SoftAssertions()

        assertions.assertThat(englishBaseWords("audible", dictionary).map { it.from })
            .containsExactlyInAnyOrder("audio")

        assertions.assertThat(englishBaseWords("compatible", dictionary).map { it.from })
            .isEmpty()

        assertions.assertThat(englishBaseWords("eligible", dictionary).map { it.from })
            .isEmpty()

        assertions.assertThat(englishBaseWords("feasible", dictionary).map { it.from })
            .isEmpty()

        assertions.assertThat(englishBaseWords("terrible", dictionary).map { it.from })
            .isEmpty()

        assertions.assertAll()
    }


    @Test
    @DisplayName("englishBaseWords -ence")
    fun test_englishBaseWords_endsWithEnce() {
        val dictionary = dictionary()

        val assertions = SoftAssertions()

        assertions.assertThat(englishBaseWords("providence", dictionary).map { it.from })
            .containsExactlyInAnyOrder("provide", "provident")

        assertions.assertThat(englishBaseWords("residence", dictionary).map { it.from })
            .containsExactlyInAnyOrder("reside", "resident")

        assertions.assertThat(englishBaseWords("patience", dictionary).map { it.from })
            .containsExactlyInAnyOrder("patient")

        assertions.assertThat(englishBaseWords("conference", dictionary).map { it.from })
            .containsExactlyInAnyOrder("confer")

        assertions.assertThat(englishBaseWords("dependence", dictionary).map { it.from })
            .containsExactlyInAnyOrder("depend", "dependent")

        assertions.assertThat(englishBaseWords("difference", dictionary).map { it.from })
            .containsExactlyInAnyOrder("differ", "different")

        assertions.assertAll()
    }

}
