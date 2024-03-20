package com.mvv.gui.words

import com.mvv.gui.cardeditor.actions.mergeCards
import com.mvv.gui.cardeditor.actions.mergeDuplicates
import com.mvv.gui.cardeditor.actions.splitExamples
import com.mvv.gui.test.useAssertJSoftAssertions
import com.mvv.gui.util.enumSetOf
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Path


class WordActionsTest {

    @Test
    fun extractWordsFromText() {
        val cards = extractWordsFromText_New("John has come in the room.", SentenceEndRule.ByEndingDot, emptySet())

        assertThat(cards.map { it.from }).containsExactly(
            "John", "has",
            "come",
            "come in", // with preposition
            "in", "the", "room",
        )
    }

    @Test
    fun extractWordsFromText_withMultiWordsPreposition() {

        val cards = extractWordsFromText_New("Businesses have costs to cover in addition to your salary.", SentenceEndRule.ByEndingDot, emptySet())
        assertThat(cards.map { it.from }).containsExactly(
            "Businesses", "have",
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
            "Businesses", "have",
            "costs",
            "costs to", // with preposition
            "to",
            "cover",
            "cover in addition to", // with preposition
            "in", "addition",
            "addition to", // would be nice to exclude it in some way?!
            "to", "your", "salary",
            "John", "promised", "promised to", "to", "cover", "trade", "code", "code with", "with", "tests"
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
            "Businesses", "have",
            "costs",
            "costs to", // with preposition
            "to",
            "cover",
            "cover in addition to", // with preposition
            "in", "addition",
            "addition to", // would be nice to exclude it in some way?!
            "to", "your", "salary",
            "John", "promised", "promised to", "to", "cover", "trade", "code", "code with", "with", "tests"
        )

        val mergedCards = mergeDuplicates(cards)
        assertThat(mergedCards
            .sortedBy { it.sourcePositions.first() }
            .map { it.from }).containsExactly(
            "Businesses", "have",
            "costs",
            "costs to", // with preposition
            "to",
            "cover",
            "cover in addition to", // with preposition
            "in", "addition",
            "addition to", // would be nice to exclude it in some way?!
            //"to",    // duplicate
            "your", "salary",
            "John", "promised", "promised to",
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

    @Test
    fun mergeCardsTest() {
        val card1 = CardWordEntry("angel", "translated1\ntranslated2\ntranslated3\n").also {
            it.examples = "Example 01\nExample 02\nExample 03\n"
            it.predefinedSets = enumSetOf(PredefinedSet.DifficultToListen)
            it.transcription = "[transcr1]"
            it.statuses = setOf(WordCardStatus.NoBaseWordInSet, WordCardStatus.Duplicates)
            //it.file = Path.of("/dir1/file1.csv")
            it.fromWithPreposition = "angel of"
            it.sourcePositions = listOf(101, 102, 103)
            it.sourceSentences = "senetence 01\nsenetence 02"
        }

        val card2 = CardWordEntry("Angeles", "translated1\ntranslated2\ntranslated4\n").also {
            it.examples = "Example 01\nExample 02\nExample 04\n"
            it.predefinedSets = enumSetOf(PredefinedSet.DifficultSense)
            it.transcription = "[transcr2]"
            it.statuses = setOf(WordCardStatus.NoBaseWordInSet, WordCardStatus.TooManyExampleNewCardCandidates)
            //it.file = Path.of("/dir1/file2.csv")
            it.fromWithPreposition = "Angeles from"
            it.sourcePositions = listOf(101, 102, 104)
            it.sourceSentences = "senetence 01\nsenetence 04"
        }

        val merged = mergeCards(listOf(card1, card2))

        val a = SoftAssertions()

        a.assertThat(merged.from).isEqualTo("angel")
        a.assertThat(merged.to).isEqualTo(card1.to + "\n" + card2.to)
        a.assertThat(merged.transcription).isEqualTo("[transcr1] [transcr2]")
        a.assertThat(merged.examples).isEqualTo("Example 01\n\nExample 02\n\nExample 03\n\nExample 04\n\n")
        a.assertThat(merged.predefinedSets).isEqualTo(enumSetOf(PredefinedSet.DifficultSense, PredefinedSet.DifficultToListen))
        a.assertThat(merged.statuses).isEqualTo(enumSetOf(WordCardStatus.NoBaseWordInSet, WordCardStatus.Duplicates, WordCardStatus.TooManyExampleNewCardCandidates))
        //a.assertThat(merged.file).isEqualTo(Path.of("/dir1/file1.csv"))
        a.assertThat(merged.fromWithPreposition).isEqualTo("angel of  Angeles from")
        a.assertThat(merged.sourcePositions).isEqualTo(listOf(101, 102, 103, 104))
        a.assertThat(merged.sourceSentences).isEqualTo(card1.sourceSentences + "\n" + card2.sourceSentences)


        fun cards(vararg froms: String) = froms.map { CardWordEntry(it, "") }

        a.assertThat(mergeCards(cards("angel", "")).from).isEqualTo("angel")

        a.assertThat(mergeCards(cards("angel", "angel")).from).isEqualTo("angel")

        a.assertThat(mergeCards(cards("angel", "angels")).from).isEqualTo("angel")
        a.assertThat(mergeCards(cards("angels", "angel")).from).isEqualTo("angel")

        a.assertThat(mergeCards(cards("Angel", "angels")).from).isEqualTo("Angel")
        a.assertThat(mergeCards(cards("Angels", "angel")).from).isEqualTo("angel")

        a.assertThat(mergeCards(cards("eat", "eating")).from).isEqualTo("eat")
        a.assertThat(mergeCards(cards("eating", "eat")).from).isEqualTo("eat")

        a.assertAll()
    }

    @Test
    fun mergeCardsTest_withTheSameValues() { useAssertJSoftAssertions {

        val card1 = CardWordEntry("angel", "translated1\ntranslated2\ntranslated3\n").also {
            it.examples = "Example 01\nExa\n  mple 02\nExample 03\n"
            it.predefinedSets = enumSetOf(PredefinedSet.DifficultToListen)
            it.transcription = "[transcr1]"
            it.statuses = setOf(WordCardStatus.NoBaseWordInSet, WordCardStatus.Duplicates)
            //it.file = Path.of("/dir1/file1.csv")
            it.fromWithPreposition = "angel of"
            it.sourcePositions = listOf(101, 102, 103)
            it.sourceSentences = "senetence 01\nsenetence 02"
        }

        val card2 = CardWordEntry("Angeles", "translated1\ntranslated2\ntranslated4\n").also {
            it.examples = "Example 01\nExa\n  mple 02\nExample 04\n"
            it.predefinedSets = enumSetOf(PredefinedSet.DifficultSense)
            it.transcription = "[transcr2]"
            it.statuses = setOf(WordCardStatus.NoBaseWordInSet, WordCardStatus.TooManyExampleNewCardCandidates)
            //it.file = Path.of("/dir1/file2.csv")
            it.fromWithPreposition = "Angeles from"
            it.sourcePositions = listOf(101, 102, 104)
            it.sourceSentences = "senetence 01\nsenetence 04"
        }

        val card3 = CardWordEntry("angel", "translated1\ntranslated2\ntranslated3\n").also {
            it.examples = "Example 01\n\nExa\n  mple 02\n\n\nExample 06\n"
            it.predefinedSets = enumSetOf(PredefinedSet.DifficultToListen)
            it.transcription = "[transcr1]"
            it.statuses = setOf(WordCardStatus.NoBaseWordInSet, WordCardStatus.Duplicates)
            //it.file = Path.of("/dir1/file1.csv")
            it.fromWithPreposition = "angel of"
            it.sourcePositions = listOf(101, 102, 103)
            it.sourceSentences = "senetence 01\nsenetence 02"
        }

        val merged = mergeCards(listOf(card1, card2, card3))

        assertThat(merged.from).isEqualTo("angel angeles")
        assertThat(merged.to).isEqualTo(card1.to + "\n" + card2.to)
        assertThat(merged.transcription).isEqualTo("[transcr1] [transcr2]")
        assertThat(merged.examples).isEqualTo(
            """
                Example 01
                
                Exa
                  mple 02
                
                Example 03
                
                Example 04
                
                Example 06
                
                
                """.trimIndent())
        assertThat(merged.predefinedSets).isEqualTo(enumSetOf(PredefinedSet.DifficultSense, PredefinedSet.DifficultToListen))
        assertThat(merged.statuses).isEqualTo(enumSetOf(WordCardStatus.NoBaseWordInSet, WordCardStatus.Duplicates, WordCardStatus.TooManyExampleNewCardCandidates))
        //assertThat(merged.file).isEqualTo(Path.of("/dir1/file1.csv"))
        assertThat(merged.fromWithPreposition).isEqualTo("angel of  Angeles from")
        assertThat(merged.sourcePositions).isEqualTo(listOf(101, 102, 103, 104))
        assertThat(merged.sourceSentences).isEqualTo(card1.sourceSentences + "\n" + card2.sourceSentences)


        fun cards(vararg froms: String) = froms.map { CardWordEntry(it, "") }

        assertThat(mergeCards(cards("angel", "")).from).isEqualTo("angel")

        assertThat(mergeCards(cards("angel", "angel")).from).isEqualTo("angel")

        assertThat(mergeCards(cards("angel", "angels")).from).isEqualTo("angel")
        assertThat(mergeCards(cards("angels", "angel")).from).isEqualTo("angel")

        assertThat(mergeCards(cards("Angel", "angels")).from).isEqualTo("Angel")
        assertThat(mergeCards(cards("Angels", "angel")).from).isEqualTo("angel")

        assertThat(mergeCards(cards("eat", "eating")).from).isEqualTo("eat")
        assertThat(mergeCards(cards("eating", "eat")).from).isEqualTo("eat")
    } }

    @Test
    @DisplayName("splitExamples")
    fun test_splitExamples() {
        useAssertJSoftAssertions {

            assertThat(
                """
                    Example 01

                    Exa
                      mple 02

                    Example 03

                    Example 04
                    Example 05

                    Example 06
                """.trimIndent()
                .splitExamples()
            )
            .containsExactly(
                "Example 01",
                "Exa\n  mple 02",
                "Example 03",
                "Example 04",
                "Example 05",
                "Example 06",
            )
        }
    }

    @Test
    @Disabled("for manual debug")
    fun fromSrt() {
        val a = SoftAssertions()

        val cards = extractWordsFromSrtFileAndMerge(Path.of("/home/vmelnykov/english/src/Thursday.English-WWW.MY-SUBS.CO.srt"), emptySet())

        a.assertThat(cards).isNotEmpty

        a.assertAll()
    }
}
