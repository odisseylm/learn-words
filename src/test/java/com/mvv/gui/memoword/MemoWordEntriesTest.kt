package com.mvv.gui.memoword

import com.mvv.gui.test.useAssertJSoftAssertions
import com.mvv.gui.words.*
import org.junit.jupiter.api.Test


class MemoWordEntriesTest {

    @Test
    fun asSinglePartOfSpeech_byFrom() { useAssertJSoftAssertions {

        assertThat(cardWordEntry { from = "to advertise" }.asSinglePartOfSpeech())
            .isEqualTo(PartOfSpeech.Verb)

        assertThat(cardWordEntry { from = "to advertise for smth." }.asSinglePartOfSpeech())
            .isEqualTo(PartOfSpeech.Verb)

        assertThat(cardWordEntry { from = "angle" }.asSinglePartOfSpeech())
            .isEqualTo(PartOfSpeech.Word)

        assertThat(cardWordEntry { from = "angle of sight" }.asSinglePartOfSpeech())
            .isEqualTo(PartOfSpeech.Phrase)
    } }

    @Test
    fun asSinglePartOfSpeech_fixedByTo() { useAssertJSoftAssertions {

        assertThat(cardWordEntry {
                    from = "advertise"
                    to = """ помещать ОБЪЯВЛение
                             рекламировать
                             искать ПО ОБЪЯВЛЕНИЮ
                             (уст.) извещать, ОБЪЯВЛЯТЬ
                         """
                    }.asSinglePartOfSpeech())
            .isEqualTo(PartOfSpeech.Verb)

    } }

    @Test
    fun `asSinglePartOfSpeech when SetExpression and something else`() { useAssertJSoftAssertions {

        assertThat(cardWordEntry {
            from = "to make one's bread"
            to = "зарабатывать на жизнь"
            partsOfSpeech = setOf(PartOfSpeech.Verb, PartOfSpeech.SetExpression)
        }.asSinglePartOfSpeech())
        .isEqualTo(PartOfSpeech.SetExpression)

    } }
}
