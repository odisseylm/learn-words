package com.mvv.gui.words

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test


class WordActionsTest {

    @Test
    fun extractWordsFromText() {
        val cards = extractWordsFromText_New("John has come in the room.", SentenceEndRule.ByEndingDot, emptySet())

        assertThat(cards.map { it.from }).containsExactly(
            "john", "has",
            "come",
            "come in", // with preposition
            "in", "the", "room",
        )
    }

    @Test
    fun extractWordsFromText_withMultiWordsPreposition() {

        val cards = extractWordsFromText_New("Businesses have costs to cover in addition to your salary.", SentenceEndRule.ByEndingDot, emptySet())
        assertThat(cards.map { it.from }).containsExactly(
            "businesses", "have",
            "costs",
            "costs to", // with preposition
            "to",
            "cover",
            "cover in addition to", // with preposition
            "in", "addition",
            "addition to", // would be nice to exclude it in some way?!
            "to", "your", "salary",
        )

        cards.first { it.from == "costs" }.also {
            val assertions = SoftAssertions()

            assertions.assertThat(it.from).isEqualTo("costs")
            assertions.assertThat(it.fromWithPreposition).isEqualTo("costs to")
            assertions.assertThat(it.sourcePositions).containsExactlyInAnyOrder(2)
            assertions.assertThat(it.sourceSentences).isEqualTo(
                "Businesses have costs to cover in addition to your salary.")

            // just to make sure lets test them too at least once
            assertions.assertThat(it.to).isEqualTo("")
            assertions.assertThat(it.transcription).isEqualTo("")
            assertions.assertThat(it.examples).isEqualTo("")
            assertions.assertThat(it.translationCount).isEqualTo(0)
            assertions.assertThat(it.statuses).isEmpty()
            assertions.assertThat(it.predefinedSets).isEmpty()
            assertions.assertThat(it.missedBaseWords).isEmpty()

            assertions.assertAll()
        }

        cards.first { it.from == "costs to" }.also {
            val assertions = SoftAssertions()

            assertions.assertThat(it.from).isEqualTo("costs to")
            assertions.assertThat(it.fromWithPreposition).isEqualTo("costs to")
            assertions.assertThat(it.sourcePositions).containsExactlyInAnyOrder(2)
            assertions.assertThat(it.sourceSentences).isEqualTo(
                "Businesses have costs to cover in addition to your salary.")

            assertions.assertAll()
        }
    }

    @Test
    fun extractWordsFromText_multipleSentences() {

        val content = """
             Businesses have costs to cover in addition to your salary.
             John promised to cover trade code with tests.
            """

        val cards = extractWordsFromText_New(content, SentenceEndRule.ByEndingDot, emptySet())
        assertThat(cards.map { it.from }).containsExactly(
            "businesses", "have",
            "costs",
            "costs to", // with preposition
            "to",
            "cover",
            "cover in addition to", // with preposition
            "in", "addition",
            "addition to", // would be nice to exclude it in some way?!
            "to", "your", "salary",
            "john", "promised", "promised to", "to", "cover", "trade", "code", "code with", "with", "tests"
        )
    }

    @Test
    fun extractWordsFromText_mergeDuplicates() {

        val content = """
             Businesses have costs to cover in addition to your salary.
             John promised to cover trade code with tests.
            """

        val cards = extractWordsFromText_New(content, SentenceEndRule.ByEndingDot, emptySet())
        assertThat(cards.map { it.from }).containsExactly(
            "businesses", "have",
            "costs",
            "costs to", // with preposition
            "to",
            "cover",
            "cover in addition to", // with preposition
            "in", "addition",
            "addition to", // would be nice to exclude it in some way?!
            "to", "your", "salary",
            "john", "promised", "promised to", "to", "cover", "trade", "code", "code with", "with", "tests"
        )

        val mergedCards = mergeDuplicates(cards)
        assertThat(mergedCards.map { it.from }).containsExactly(
            "businesses", "have",
            "costs",
            "costs to", // with preposition
            "to",
            "cover",
            "cover in addition to", // with preposition
            "in", "addition",
            "addition to", // would be nice to exclude it in some way?!
            //"to",    // duplicate
            "your", "salary",
            "john", "promised", "promised to",
            //"to",    // duplicate
            //"cover", // duplicate
            "trade", "code", "code with", "with", "tests"
        )

        val assertions = SoftAssertions()

        mergedCards.first { it.from == "cover" }.also {
            assertions.assertThat(it.from).isEqualTo("cover")
            assertions.assertThat(it.fromWithPreposition).isEqualTo("cover in addition to")
            assertions.assertThat(it.sourcePositions).containsExactly(4, 13)
            assertions.assertThat(it.sourceSentences).isEqualTo(
                "Businesses have costs to cover in addition to your salary.\n" +
                "John promised to cover trade code with tests.",
            )

            // Just to make sure lets test them too at least once.
            // TODO: add tests
            //assertions.assertThat(it.to).isEqualTo("")
            //assertions.assertThat(it.transcription).isEqualTo("")
            //assertions.assertThat(it.examples).isEqualTo("")
            //assertions.assertThat(it.translationCount).isEqualTo(0)
            //assertions.assertThat(it.statuses).isEmpty()
            //assertions.assertThat(it.predefinedSets).isEmpty()
            //assertions.assertThat(it.missedBaseWords).isEmpty()
        }

        mergedCards.first { it.from == "to" }.also {
            assertions.assertThat(it.from).isEqualTo("to")
            assertions.assertThat(it.fromWithPreposition).isEqualTo("")
            assertions.assertThat(it.sourcePositions).containsExactly(3, 7, 12)
            assertions.assertThat(it.sourceSentences).isEqualTo(
                "Businesses have costs to cover in addition to your salary.\n" +
                "John promised to cover trade code with tests."
            )
        }

        assertions.assertAll()
    }
}
