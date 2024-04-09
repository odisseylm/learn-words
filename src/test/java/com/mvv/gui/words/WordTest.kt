package com.mvv.gui.words

import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.DisplayName

class WordTest {

    @Test
    @DisplayName("translationCount")
    fun test_splitToToTranslations_Old() {
        val a = SoftAssertions()

        //a.assertThat("word1, , ,,, word2".splitToToTranslations_Old().size).isEqualTo(2)

        a.assertThat("word1, , ,,, word2".translationCount).isEqualTo(2)
        a.assertThat(" word1 ,  , ,,,  word2 (comment1,  , ,,, comment2, comment3, ) , \t \n word3 \t".translationCount).isEqualTo(3)

        a.assertAll()
    }

    @Test
    fun splitToToTranslations() {
        val a = SoftAssertions()

        //a.assertThat("word1, word2".splitToToTranslations_Old()).containsExactly("word1", " word2")

        a.assertThat("word1, word2".splitToToTranslations()).containsExactly("word1", "word2")

        // compact string
        a.assertThat("word1,word2(comment1,comment2,comment3,),word3".splitToToTranslations())
            .containsExactly("word1", "word2(comment1,comment2,comment3,)", "word3")

        // too many spaces
        a.assertThat(" word1 ,  word2 (comment1, comment2, comment3, ) , \t \n word3 \t".splitToToTranslations())
            .containsExactly("word1", "word2 (comment1, comment2, comment3, )", "word3")

        // with word after bracket
        a.assertThat(" word1 ,  word2 (comment1, comment2, comment3, ) bla-bla, \t \n word3 \t".splitToToTranslations())
            .containsExactly("word1", "word2 (comment1, comment2, comment3, ) bla-bla", "word3")

        a.assertAll()
    }

    @Test
    fun splitToToTranslations_withWrongString_shouldNotFail() {
        val a = SoftAssertions()

        a.assertThat(" word1 ,  word2  )   (comment1 )) ) , \t \n word3 \t".splitToToTranslations())
            .containsExactly("word1", "word2  )   (comment1 )) )", "word3")

        a.assertAll()
    }

    @Test
    @DisplayName("toTranslationCountStatus")
    fun test_toTranslationCountStatus() {
        val a = SoftAssertions()

        // ideally we should throw IllegalArgumentException
        a.assertThat((-2).toTranslationCountStatus).isEqualTo(TranslationCountStatus.ToMany)
        a.assertThat((-1).toTranslationCountStatus).isEqualTo(TranslationCountStatus.ToMany)

        a.assertThat(0.toTranslationCountStatus).isEqualTo(TranslationCountStatus.Ok)
        a.assertThat(1.toTranslationCountStatus).isEqualTo(TranslationCountStatus.Ok)
        a.assertThat(2.toTranslationCountStatus).isEqualTo(TranslationCountStatus.Ok)
        a.assertThat(3.toTranslationCountStatus).isEqualTo(TranslationCountStatus.Ok)
        a.assertThat(4.toTranslationCountStatus).isEqualTo(TranslationCountStatus.NotBad)
        a.assertThat(5.toTranslationCountStatus).isEqualTo(TranslationCountStatus.NotBad)
        a.assertThat(6.toTranslationCountStatus).isEqualTo(TranslationCountStatus.Warn)
        a.assertThat(7.toTranslationCountStatus).isEqualTo(TranslationCountStatus.Warn)
        a.assertThat(8.toTranslationCountStatus).isEqualTo(TranslationCountStatus.ToMany)
        a.assertThat(9.toTranslationCountStatus).isEqualTo(TranslationCountStatus.ToMany)
        a.assertThat(10.toTranslationCountStatus).isEqualTo(TranslationCountStatus.ToMany)
        a.assertThat(11.toTranslationCountStatus).isEqualTo(TranslationCountStatus.ToMany)
        a.assertThat(12.toTranslationCountStatus).isEqualTo(TranslationCountStatus.ToMany)
        a.assertThat(13.toTranslationCountStatus).isEqualTo(TranslationCountStatus.ToMany)
        a.assertThat(14.toTranslationCountStatus).isEqualTo(TranslationCountStatus.ToMany)
        a.assertThat(15.toTranslationCountStatus).isEqualTo(TranslationCountStatus.ToMany)
        a.assertThat(16.toTranslationCountStatus).isEqualTo(TranslationCountStatus.ToMany)
        a.assertThat(17.toTranslationCountStatus).isEqualTo(TranslationCountStatus.ToMany)
        a.assertThat(18.toTranslationCountStatus).isEqualTo(TranslationCountStatus.ToMany)
        a.assertThat(19.toTranslationCountStatus).isEqualTo(TranslationCountStatus.ToMany)
        a.assertThat(20.toTranslationCountStatus).isEqualTo(TranslationCountStatus.ToMany)
        a.assertThat(21.toTranslationCountStatus).isEqualTo(TranslationCountStatus.ToMany)

        a.assertAll()
    }
}