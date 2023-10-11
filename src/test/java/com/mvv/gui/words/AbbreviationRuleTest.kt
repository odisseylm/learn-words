package com.mvv.gui.words

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class AbbreviationRuleTest {

    @Test
    fun simple() {
        val ab = AbbreviationRule("n.")

        assertThat(ab.thisDotIsPartOfTheAbbreviation("n.", 1)).isTrue()
        assertThat(ab.thisDotIsPartOfTheAbbreviation(" n.", 2)).isTrue()
        assertThat(ab.thisDotIsPartOfTheAbbreviation(" n. ", 2)).isTrue()

        assertThat(ab.thisDotIsPartOfTheAbbreviation("nn.", 2)).isFalse()
        assertThat(ab.thisDotIsPartOfTheAbbreviation("nn. ", 2)).isFalse()
    }


    @Test
    fun multiDotAbbreviation() {
        val ab = AbbreviationRule("P.P.S.")

        assertThat(ab.thisDotIsPartOfTheAbbreviation("P.P.S.", 1)).isTrue()
        assertThat(ab.thisDotIsPartOfTheAbbreviation("P.P.S.", 3)).isTrue()
        assertThat(ab.thisDotIsPartOfTheAbbreviation("P.P.S.", 5)).isTrue()
        assertThat(ab.thisDotIsPartOfTheAbbreviation("P.P.S. ", 5)).isTrue()

        assertThat(ab.thisDotIsPartOfTheAbbreviation("P.other.", 1)).isFalse()
        assertThat(ab.thisDotIsPartOfTheAbbreviation("P. other.", 1)).isFalse()

        assertThat(ab.thisDotIsPartOfTheAbbreviation("P.P.other", 3)).isFalse()
        assertThat(ab.thisDotIsPartOfTheAbbreviation("P.P. other", 3)).isFalse()

        assertThat(ab.thisDotIsPartOfTheAbbreviation("otherP.P.S.", 6)).isFalse()
        assertThat(ab.thisDotIsPartOfTheAbbreviation("otherP.P.S.", 8)).isFalse()
        assertThat(ab.thisDotIsPartOfTheAbbreviation("otherP.P.S.", 10)).isFalse()
    }
}
