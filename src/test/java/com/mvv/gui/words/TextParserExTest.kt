package com.mvv.gui.words

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


class TextParserExTest {

    @Test
    fun parse() {

        val parser = TextParserEx()

        val assertions = SoftAssertions()

        assertions.assertThat(parser.parse("")).hasSize(0)

        run {

            val sentences = parser.parse("Mama washed rama.")


            assertions.assertThat(sentences)
                .hasSize(1)

            assertions.assertThat(sentences[0].text).isEqualTo("Mama washed rama.")
            assertions.assertThat(sentences[0].allWords).containsExactly(
                wordEntry("mama", 0, "Mama washed rama."),
                wordEntry("washed", 1, "Mama washed rama."),
                wordEntry("rama", 2, "Mama washed rama."),
            )
            assertions.assertThat(sentences[0].translatableWords).containsExactly(
                wordEntry("mama", 0, "Mama washed rama."),
                wordEntry("washed", 1, "Mama washed rama."),
                wordEntry("rama", 2, "Mama washed rama."),
            )

            assertions.assertThat(sentences)
                .hasSize(1)
                .containsExactly(
                    Sentence(
                        "Mama washed rama.", listOf(
                            wordEntry("mama", 0, "Mama washed rama."),
                            wordEntry("washed", 1, "Mama washed rama."),
                            wordEntry("rama", 2, "Mama washed rama."),
                        )
                    ),
                )
        }

        run {

            assertions.assertThat(parser.parse("Mama washed rama. But papa didn't."))
                .containsExactly(
                    Sentence(
                        "Mama washed rama.", listOf(
                            wordEntry("mama", 0, "Mama washed rama."),
                            wordEntry("washed", 1, "Mama washed rama."),
                            wordEntry("rama", 2, "Mama washed rama."),
                        )
                    ),
                    Sentence(
                        "But papa didn't.", listOf(
                            wordEntry("but", 3, "But papa didn't."),
                            wordEntry("papa", 4, "But papa didn't."),
                            wordEntry("didn't", 5, "But papa didn't."),
                        )
                    ),
                )
        }

        assertions.assertAll()
    }

    @Test
    fun parseWithShortens_01() {

        val assertions = SoftAssertions()

        val sentences = TextParserEx(SentenceEndRule.ByEndingDot).parse("Mrs. Mama washed rama.  But Mr. Papa \n didn't. ")

        assertions.assertThat(sentences).hasSize(2)

        assertions.assertThat(sentences[0].text).isEqualTo("Mrs. Mama washed rama.")

        assertions.assertThat(sentences[0].allWords[1]).isEqualTo(wordEntry("mama", 1, "Mrs. Mama washed rama."))

        assertions.assertThat(sentences[0].allWords).containsExactly(
            wordEntry("mrs.", 0, "Mrs. Mama washed rama."),
            wordEntry("mama", 1, "Mrs. Mama washed rama."),
            wordEntry("washed", 2, "Mrs. Mama washed rama."),
            wordEntry("rama", 3, "Mrs. Mama washed rama."),
        )

        assertions.assertThat(sentences[1].text).isEqualTo("But Mr. Papa \n didn't.")

        assertions.assertThat(sentences[1].allWords).containsExactly(
            wordEntry("but", 4, "But Mr. Papa \n didn't."),
            wordEntry("mr.", 5, "But Mr. Papa \n didn't."),
            wordEntry("papa", 6, "But Mr. Papa \n didn't."),
            wordEntry("didn't", 7, "But Mr. Papa \n didn't."),
        )

        assertions.assertAll()
    }

    @Test
    fun parseWithShortens_byLineBreak() {

        val assertions = SoftAssertions()

        val sentences = TextParserEx(SentenceEndRule.ByLineBreak).parse("Mrs. Mama washed rama\n\nBut Mr. Papa didn't\n")

        assertions.assertThat(sentences.map { it.text }).containsExactly(
            "Mrs. Mama washed rama",
            "But Mr. Papa didn't",
        )

        assertions.assertThat(sentences[0].text).isEqualTo("Mrs. Mama washed rama")

        assertions.assertThat(sentences[0].allWords).containsExactly(
            wordEntry("mrs.", 0, "Mrs. Mama washed rama"),
            wordEntry("mama", 1, "Mrs. Mama washed rama"),
            wordEntry("washed", 2, "Mrs. Mama washed rama"),
            wordEntry("rama", 3, "Mrs. Mama washed rama"),
        )

        assertions.assertThat(sentences[1].text).isEqualTo("But Mr. Papa didn't")

        assertions.assertThat(sentences[1].allWords).containsExactly(
            wordEntry("but", 4, "But Mr. Papa didn't"),
            wordEntry("mr.", 5, "But Mr. Papa didn't"),
            wordEntry("papa", 6, "But Mr. Papa didn't"),
            wordEntry("didn't", 7, "But Mr. Papa didn't"),
        )

        assertions.assertAll()
    }

    @Test
    fun parseWithShortens_byEndingDotOrLineBreak() {

        val assertions = SoftAssertions()

        val sentences = TextParserEx(SentenceEndRule.ByEndingDotOrLineBreak).parse("Mrs. Mama washed rama. But Mr. Papa didn't\nWhat about grandpa?")

        assertions.assertThat(sentences.map { it.text }).containsExactly(
            "Mrs. Mama washed rama.",
            "But Mr. Papa didn't",
            "What about grandpa?",
        )

        assertions.assertThat(sentences[0].text).isEqualTo("Mrs. Mama washed rama.")

        assertions.assertThat(sentences[0].allWords).containsExactly(
            wordEntry("mrs.", 0, "Mrs. Mama washed rama."),
            wordEntry("mama", 1, "Mrs. Mama washed rama."),
            wordEntry("washed", 2, "Mrs. Mama washed rama."),
            wordEntry("rama", 3, "Mrs. Mama washed rama."),
        )

        assertions.assertThat(sentences[1].text).isEqualTo("But Mr. Papa didn't")

        assertions.assertThat(sentences[1].allWords).containsExactly(
            wordEntry("but", 4, "But Mr. Papa didn't"),
            wordEntry("mr.", 5, "But Mr. Papa didn't"),
            wordEntry("papa", 6, "But Mr. Papa didn't"),
            wordEntry("didn't", 7, "But Mr. Papa didn't"),
        )

        assertions.assertThat(sentences[2].text).isEqualTo("What about grandpa?")

        assertions.assertThat(sentences[2].allWords).containsExactly(
            wordEntry("what", 8, "What about grandpa?"),
            wordEntry("about", 9, "What about grandpa?"),
            wordEntry("grandpa", 10, "What about grandpa?"),
        )

        assertions.assertAll()
    }

    @Test
    fun parseWithShortens_02() {

        val assertions = SoftAssertions()

        val sentences = TextParserEx().parse("Mrs. Mama washed rama. But Mr. Papa didn't.")

        assertions.assertThat(sentences).hasSize(2)

        assertions.assertThat(sentences[0].text).isEqualTo("Mrs. Mama washed rama.")
        assertions.assertThat(sentences[0].allWords).containsExactly(
            wordEntry("mrs.", 0, "Mrs. Mama washed rama."),
            wordEntry("mama", 1, "Mrs. Mama washed rama."),
            wordEntry("washed", 2, "Mrs. Mama washed rama."),
            wordEntry("rama", 3, "Mrs. Mama washed rama."),
        )

        assertions.assertThat(sentences[1].text).isEqualTo("But Mr. Papa didn't.")
        assertions.assertThat(sentences[1].allWords).containsExactly(
            wordEntry("but", 4, "But Mr. Papa didn't."),
            wordEntry("mr.", 5, "But Mr. Papa didn't."),
            wordEntry("papa", 6, "But Mr. Papa didn't."),
            wordEntry("didn't", 7, "But Mr. Papa didn't."),
        )

        // complex assert
        assertions.assertThat(sentences)
            .hasSize(2)
            .containsExactly(
                Sentence("Mrs. Mama washed rama.", listOf(
                    wordEntry("mrs.", 0, "Mrs. Mama washed rama."),
                    wordEntry("mama", 1, "Mrs. Mama washed rama."),
                    wordEntry("washed", 2, "Mrs. Mama washed rama."),
                    wordEntry("rama", 3, "Mrs. Mama washed rama."),
                )),
                Sentence("But Mr. Papa didn't.", listOf(
                    wordEntry("but", 4, "But Mr. Papa didn't."),
                    wordEntry("mr.", 5, "But Mr. Papa didn't."),
                    wordEntry("papa", 6, "But Mr. Papa didn't."),
                    wordEntry("didn't", 7, "But Mr. Papa didn't."),
                )),
            )

        assertions.assertAll()
    }


    @Test
    fun parseWithShortens_03() {

        val assertions = SoftAssertions()

        val sentences = TextParserEx().parse("The number PI 3.1415926 is a magic! But number 2.2 is not one.")

        assertions.assertThat(sentences).hasSize(2)

        assertions.assertThat(sentences[0].text).isEqualTo("The number PI 3.1415926 is a magic!")
        assertions.assertThat(sentences[0].allWords).containsExactly(
            wordEntry("the", 0, "The number PI 3.1415926 is a magic!"),
            wordEntry("number", 1, "The number PI 3.1415926 is a magic!"),
            wordEntry("pi", 2, "The number PI 3.1415926 is a magic!"),
            wordEntry("3.1415926", 3, "The number PI 3.1415926 is a magic!"),
            wordEntry("is", 4, "The number PI 3.1415926 is a magic!"),
            wordEntry("a", 5, "The number PI 3.1415926 is a magic!"),
            wordEntry("magic", 6, "The number PI 3.1415926 is a magic!"),
        )

        assertions.assertThat(sentences[1].text).isEqualTo("But number 2.2 is not one.")
        assertions.assertThat(sentences[1].allWords).containsExactly(
            wordEntry("but", 7, "But number 2.2 is not one."),
            wordEntry("number", 8, "But number 2.2 is not one."),
            wordEntry("2.2", 9, "But number 2.2 is not one."),
            wordEntry("is", 10, "But number 2.2 is not one."),
            wordEntry("not", 11, "But number 2.2 is not one."),
            wordEntry("one", 12, "But number 2.2 is not one."),
        )

        assertions.assertAll()
    }


    @Test
    fun parseWithShortens_04() {

        val assertions = SoftAssertions()

        val sentences = TextParserEx().parse("The number PI 3.1415926 is a magic!!! But number 2.2 is not one... Oops!")

        assertions.assertThat(sentences.map { it.text }).containsExactly(
            "The number PI 3.1415926 is a magic!!!",
            "But number 2.2 is not one...",
            "Oops!",
        )

        assertions.assertThat(sentences[0].text).isEqualTo("The number PI 3.1415926 is a magic!!!")
        assertions.assertThat(sentences[0].allWords).containsExactly(
            wordEntry("the", 0, "The number PI 3.1415926 is a magic!!!"),
            wordEntry("number", 1, "The number PI 3.1415926 is a magic!!!"),
            wordEntry("pi", 2, "The number PI 3.1415926 is a magic!!!"),
            wordEntry("3.1415926", 3, "The number PI 3.1415926 is a magic!!!"),
            wordEntry("is", 4, "The number PI 3.1415926 is a magic!!!"),
            wordEntry("a", 5, "The number PI 3.1415926 is a magic!!!"),
            wordEntry("magic", 6, "The number PI 3.1415926 is a magic!!!"),
        )

        assertions.assertThat(sentences[1].text).isEqualTo("But number 2.2 is not one...")
        assertions.assertThat(sentences[1].allWords).containsExactly(
            wordEntry("but", 7, "But number 2.2 is not one..."),
            wordEntry("number", 8, "But number 2.2 is not one..."),
            wordEntry("2.2", 9, "But number 2.2 is not one..."),
            wordEntry("is", 10, "But number 2.2 is not one..."),
            wordEntry("not", 11, "But number 2.2 is not one..."),
            wordEntry("one", 12, "But number 2.2 is not one..."),
        )

        assertions.assertAll()
    }

    @Test
    fun parseRealText() {
        val text =
            """
            Charles Stross
            EQUOID
            “Bob! Are you busy right now? I’d like a moment of your time.”
            Those thirteen words never bode well—although coming from my new manager, Iris, they’re less doom-laden than if they were falling from the lips of some others I could name. In the two months I’ve been working for her Iris has turned out to be the sanest and most sensible manager I’ve had in the past five years. Which is saying quite a lot, really, and I’m eager to keep her happy while I’ve got her.
            “Be with you in ten minutes,” I call through the open door of my office; “got a query from HR to answer first.” Human Resources have teeth, here in the secretive branch of the British government known to its inmates as the Laundry; so when HR ask you to do their homework—ahem, provide one’s opinion of an applicant’s suitability for a job opening—you give them priority over your regular work load. Even when it’s pretty obvious that they’re taking the piss. 
            """

        val sentences: List<Sentence> = TextParserEx(SentenceEndRule.ByEndingDotOrLineBreak).parse(text)

        assertThat(sentences.map { it.text }).containsExactly(
            "Charles Stross",
            "EQUOID",
            "Bob!",
            "Are you busy right now?",
            "I’d like a moment of your time.",
            "Those thirteen words never bode well—although coming from my new manager, Iris," +
                    " they’re less doom-laden than if they were falling from the lips of some others I could name.",
            "In the two months I’ve been working for her Iris has turned out to be the sanest" +
                    " and most sensible manager I’ve had in the past five years.",
            "Which is saying quite a lot, really, and I’m eager to keep her happy while I’ve got her.",
            "“Be with you in ten minutes,” I call through the open door of my office; “got a query from HR to answer first.”",
            "Human Resources have teeth, here in the secretive branch of the British government known to its inmates" +
                    " as the Laundry; so when HR ask you to do their homework—ahem, provide one’s opinion" +
                    " of an applicant’s suitability for a job opening—you give them priority over your regular work load.",
            "Even when it’s pretty obvious that they’re taking the piss."
        )
    }


    @Test
    fun parseRealText_altQuotes() {
        val text =
            """
            Charles Stross
            EQUOID
            «Bob! Are you busy right now? I’d like a moment of your time.»
            Those thirteen words never bode well—although coming from my new manager, Iris, they’re less doom-laden than if they were falling from the lips of some others I could name. In the two months I’ve been working for her Iris has turned out to be the sanest and most sensible manager I’ve had in the past five years. Which is saying quite a lot, really, and I’m eager to keep her happy while I’ve got her.
            «Be with you in ten minutes,» I call through the open door of my office; «got a query from HR to answer first.» Human Resources have teeth, here in the secretive branch of the British government known to its inmates as the Laundry; so when HR ask you to do their homework—ahem, provide one’s opinion of an applicant’s suitability for a job opening—you give them priority over your regular work load. Even when it’s pretty obvious that they’re taking the piss. 
            """

        val sentences: List<Sentence> = TextParserEx(SentenceEndRule.ByEndingDotOrLineBreak).parse(text)

        assertThat(sentences.map { it.text }).containsExactly(
            "Charles Stross",
            "EQUOID",
            "Bob!",
            "Are you busy right now?",
            "I’d like a moment of your time.",
            "Those thirteen words never bode well—although coming from my new manager, Iris," +
                    " they’re less doom-laden than if they were falling from the lips of some others I could name.",
            "In the two months I’ve been working for her Iris has turned out to be the sanest" +
                    " and most sensible manager I’ve had in the past five years.",
            "Which is saying quite a lot, really, and I’m eager to keep her happy while I’ve got her.",
            "«Be with you in ten minutes,» I call through the open door of my office; «got a query from HR to answer first.»",
            "Human Resources have teeth, here in the secretive branch of the British government known to its inmates" +
                    " as the Laundry; so when HR ask you to do their homework—ahem, provide one’s opinion" +
                    " of an applicant’s suitability for a job opening—you give them priority over your regular work load.",
            "Even when it’s pretty obvious that they’re taking the piss."
        )
    }


    @Test
    fun parseSrt() {
        val text =
            """
            305
            00:23:03,281 --> 00:23:05,681
            What are we supposed to be
            seeing here?
            """

        val sentences: List<Sentence> = TextParserEx(SentenceEndRule.ByEndingDotOrLineBreak).parse(text)

        assertThat(sentences.map { it.text }).containsExactly(
            "305",
            "00:23:03,281 --> 00:23:05,681",
            "What are we supposed to be",
            "seeing here?",
        )
    }


    @Test
    fun parseSrt_expectedFlow() {

        val text =
            """
            305
            00:23:03,281 --> 00:23:05,681
            What are we supposed to be
            seeing here?
            """.trimIndent()

        val sentences: List<Sentence> = TextParserEx(SentenceEndRule.ByEndingDot).parse(loadOnlyTextFromSrt(text))

        assertThat(sentences.map { it.text }).containsExactly(
            "What are we supposed to be\nseeing here?"
        )
    }


    @Test
    @DisplayName("isAbbreviation")
    fun test_isAbbreviation() {
        val assertions = SoftAssertions()

        val abbreviation = AbbreviationRule("Mr.")

        assertions.assertThat(abbreviation.thisDotIsPartOfTheAbbreviation("Mr.", 2)).isTrue
        assertions.assertThat(abbreviation.thisDotIsPartOfTheAbbreviation(" Mr.", 3)).isTrue
        assertions.assertThat(abbreviation.thisDotIsPartOfTheAbbreviation(" Mr. ", 3)).isTrue

        assertions.assertThat(abbreviation.thisDotIsPartOfTheAbbreviation("Mr.", 0)).isFalse
        assertions.assertThat(abbreviation.thisDotIsPartOfTheAbbreviation("Mr.", 1)).isFalse
        assertions.assertThat(abbreviation.thisDotIsPartOfTheAbbreviation("Mr. ", 3)).isFalse
        assertions.assertThat(abbreviation.thisDotIsPartOfTheAbbreviation("Mr.  ", 4)).isFalse

        assertions.assertAll()
    }


    @Test
    @DisplayName("isAbbreviation 02")
    fun test_isAbbreviation_02() {
        val assertions = SoftAssertions()

        val abbreviation = AbbreviationRule("e.g.")

        assertions.assertThat(abbreviation.thisDotIsPartOfTheAbbreviation("e.g.", 0)).isFalse
        assertions.assertThat(abbreviation.thisDotIsPartOfTheAbbreviation("e.g.", 1)).isTrue
        assertions.assertThat(abbreviation.thisDotIsPartOfTheAbbreviation("e.g.", 2)).isFalse
        assertions.assertThat(abbreviation.thisDotIsPartOfTheAbbreviation("e.g.", 3)).isTrue
        assertions.assertThat(abbreviation.thisDotIsPartOfTheAbbreviation("e.g.", 4)).isFalse

        assertions.assertThat(abbreviation.thisDotIsPartOfTheAbbreviation(" e.g.", 0)).isFalse
        assertions.assertThat(abbreviation.thisDotIsPartOfTheAbbreviation(" e.g.", 1)).isFalse
        assertions.assertThat(abbreviation.thisDotIsPartOfTheAbbreviation(" e.g.", 2)).isTrue
        assertions.assertThat(abbreviation.thisDotIsPartOfTheAbbreviation(" e.g.", 3)).isFalse
        assertions.assertThat(abbreviation.thisDotIsPartOfTheAbbreviation(" e.g.", 4)).isTrue
        assertions.assertThat(abbreviation.thisDotIsPartOfTheAbbreviation(" e.g.", 5)).isFalse
        assertions.assertThat(abbreviation.thisDotIsPartOfTheAbbreviation(" e.g.", 6)).isFalse

        assertions.assertAll()
    }


    @Test
    @DisplayName("isEndOf 02")
    fun test_isEndOf_02() {
        val abbreviation = AbbreviationRule("Mr.")

        assertThat(abbreviation.thisDotIsPartOfTheAbbreviation("Mr.", 2)).isTrue
        assertThat(abbreviation.thisDotIsPartOfTheAbbreviation(" Mr.", 3)).isTrue
        assertThat(abbreviation.thisDotIsPartOfTheAbbreviation(" Mr. ", 3)).isTrue

        assertThat(abbreviation.thisDotIsPartOfTheAbbreviation("Mr.", 0)).isFalse
        assertThat(abbreviation.thisDotIsPartOfTheAbbreviation("Mr.", 1)).isFalse
        assertThat(abbreviation.thisDotIsPartOfTheAbbreviation("Mr. ", 3)).isFalse
        assertThat(abbreviation.thisDotIsPartOfTheAbbreviation("Mr.  ", 4)).isFalse
    }


    @Test
    @DisplayName("removeUnpairedStartingQuote")
    fun test_removeUnpairedStartingQuote() {
        assertThat(removeUnpairedStartingQuote("“Bob!")).isEqualTo("Bob!")
        assertThat(removeUnpairedStartingQuote("«Bob!")).isEqualTo("Bob!")
        assertThat(removeUnpairedStartingQuote("\"Bob!")).isEqualTo("Bob!")
        assertThat(removeUnpairedStartingQuote("'Bob!")).isEqualTo("Bob!")

        assertThat(removeUnpairedStartingQuote("Bob!”")).isEqualTo("Bob!”")
        assertThat(removeUnpairedStartingQuote("Bob!»")).isEqualTo("Bob!»")
        assertThat(removeUnpairedStartingQuote("Bob!\"")).isEqualTo("Bob!\"")
        assertThat(removeUnpairedStartingQuote("Bob!'")).isEqualTo("Bob!'")
    }


    @Test
    @DisplayName("removeUnpairedEndingQuote")
    fun test_removeUnpairedEndingQuote() {
        assertThat(removeUnpairedEndingQuote("“Bob!")).isEqualTo("“Bob!")
        assertThat(removeUnpairedEndingQuote("«Bob!")).isEqualTo("«Bob!")
        assertThat(removeUnpairedEndingQuote("\"Bob!")).isEqualTo("\"Bob!")
        assertThat(removeUnpairedEndingQuote("'Bob!")).isEqualTo("'Bob!")

        assertThat(removeUnpairedEndingQuote("Bob!”")).isEqualTo("Bob!")
        assertThat(removeUnpairedEndingQuote("Bob!»")).isEqualTo("Bob!")
        assertThat(removeUnpairedEndingQuote("Bob!\"")).isEqualTo("Bob!")
        assertThat(removeUnpairedEndingQuote("Bob!'")).isEqualTo("Bob!")
    }

}


private fun wordEntry(word: String, position: Int, sentenceText: CharSequence): WordEntry = WordEntry(word, position, Sentence(sentenceText, emptyList()))
