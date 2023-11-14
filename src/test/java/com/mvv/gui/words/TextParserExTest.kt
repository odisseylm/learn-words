package com.mvv.gui.words

import com.mvv.gui.util.splitByCharSeparatorsAndPreserveAllTokens
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
                wordEntry("Mama", 0, "Mama washed rama."),
                wordEntry("washed", 1, "Mama washed rama."),
                wordEntry("rama", 2, "Mama washed rama."),
            )
            assertions.assertThat(sentences[0].translatableWords).containsExactly(
                wordEntry("Mama", 0, "Mama washed rama."),
                wordEntry("washed", 1, "Mama washed rama."),
                wordEntry("rama", 2, "Mama washed rama."),
            )

            assertions.assertThat(sentences)
                .hasSize(1)
                .containsExactly(
                    Sentence(
                        "Mama washed rama.", listOf(
                            wordEntry("Mama", 0, "Mama washed rama."),
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
                            wordEntry("Mama", 0, "Mama washed rama."),
                            wordEntry("washed", 1, "Mama washed rama."),
                            wordEntry("rama", 2, "Mama washed rama."),
                        )
                    ),
                    Sentence(
                        "But papa didn't.", listOf(
                            wordEntry("But", 3, "But papa didn't."),
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

        assertions.assertThat(sentences[0].allWords[1]).isEqualTo(wordEntry("Mama", 1, "Mrs. Mama washed rama."))

        assertions.assertThat(sentences[0].allWords).containsExactly(
            wordEntry("Mrs.", 0, "Mrs. Mama washed rama."),
            wordEntry("Mama", 1, "Mrs. Mama washed rama."),
            wordEntry("washed", 2, "Mrs. Mama washed rama."),
            wordEntry("rama", 3, "Mrs. Mama washed rama."),
        )

        assertions.assertThat(sentences[1].text).isEqualTo("But Mr. Papa \n didn't.")

        assertions.assertThat(sentences[1].allWords).containsExactly(
            wordEntry("But", 4, "But Mr. Papa \n didn't."),
            wordEntry("Mr.", 5, "But Mr. Papa \n didn't."),
            wordEntry("Papa", 6, "But Mr. Papa \n didn't."),
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
            wordEntry("Mrs.", 0, "Mrs. Mama washed rama"),
            wordEntry("Mama", 1, "Mrs. Mama washed rama"),
            wordEntry("washed", 2, "Mrs. Mama washed rama"),
            wordEntry("rama", 3, "Mrs. Mama washed rama"),
        )

        assertions.assertThat(sentences[1].text).isEqualTo("But Mr. Papa didn't")

        assertions.assertThat(sentences[1].allWords).containsExactly(
            wordEntry("But", 4, "But Mr. Papa didn't"),
            wordEntry("Mr.", 5, "But Mr. Papa didn't"),
            wordEntry("Papa", 6, "But Mr. Papa didn't"),
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
            wordEntry("Mrs.", 0, "Mrs. Mama washed rama."),
            wordEntry("Mama", 1, "Mrs. Mama washed rama."),
            wordEntry("washed", 2, "Mrs. Mama washed rama."),
            wordEntry("rama", 3, "Mrs. Mama washed rama."),
        )

        assertions.assertThat(sentences[1].text).isEqualTo("But Mr. Papa didn't")

        assertions.assertThat(sentences[1].allWords).containsExactly(
            wordEntry("But", 4, "But Mr. Papa didn't"),
            wordEntry("Mr.", 5, "But Mr. Papa didn't"),
            wordEntry("Papa", 6, "But Mr. Papa didn't"),
            wordEntry("didn't", 7, "But Mr. Papa didn't"),
        )

        assertions.assertThat(sentences[2].text).isEqualTo("What about grandpa?")

        assertions.assertThat(sentences[2].allWords).containsExactly(
            wordEntry("What", 8, "What about grandpa?"),
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
            wordEntry("Mrs.", 0, "Mrs. Mama washed rama."),
            wordEntry("Mama", 1, "Mrs. Mama washed rama."),
            wordEntry("washed", 2, "Mrs. Mama washed rama."),
            wordEntry("rama", 3, "Mrs. Mama washed rama."),
        )

        assertions.assertThat(sentences[1].text).isEqualTo("But Mr. Papa didn't.")
        assertions.assertThat(sentences[1].allWords).containsExactly(
            wordEntry("But", 4, "But Mr. Papa didn't."),
            wordEntry("Mr.", 5, "But Mr. Papa didn't."),
            wordEntry("Papa", 6, "But Mr. Papa didn't."),
            wordEntry("didn't", 7, "But Mr. Papa didn't."),
        )

        // complex assert
        assertions.assertThat(sentences)
            .hasSize(2)
            .containsExactly(
                Sentence("Mrs. Mama washed rama.", listOf(
                    wordEntry("Mrs.", 0, "Mrs. Mama washed rama."),
                    wordEntry("Mama", 1, "Mrs. Mama washed rama."),
                    wordEntry("washed", 2, "Mrs. Mama washed rama."),
                    wordEntry("rama", 3, "Mrs. Mama washed rama."),
                )),
                Sentence("But Mr. Papa didn't.", listOf(
                    wordEntry("But", 4, "But Mr. Papa didn't."),
                    wordEntry("Mr.", 5, "But Mr. Papa didn't."),
                    wordEntry("Papa", 6, "But Mr. Papa didn't."),
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
            wordEntry("The", 0, "The number PI 3.1415926 is a magic!"),
            wordEntry("number", 1, "The number PI 3.1415926 is a magic!"),
            wordEntry("PI", 2, "The number PI 3.1415926 is a magic!"),
            wordEntry("3.1415926", 3, "The number PI 3.1415926 is a magic!"),
            wordEntry("is", 4, "The number PI 3.1415926 is a magic!"),
            wordEntry("a", 5, "The number PI 3.1415926 is a magic!"),
            wordEntry("magic", 6, "The number PI 3.1415926 is a magic!"),
        )

        assertions.assertThat(sentences[1].text).isEqualTo("But number 2.2 is not one.")
        assertions.assertThat(sentences[1].allWords).containsExactly(
            wordEntry("But", 7, "But number 2.2 is not one."),
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
            wordEntry("The", 0, "The number PI 3.1415926 is a magic!!!"),
            wordEntry("number", 1, "The number PI 3.1415926 is a magic!!!"),
            wordEntry("PI", 2, "The number PI 3.1415926 is a magic!!!"),
            wordEntry("3.1415926", 3, "The number PI 3.1415926 is a magic!!!"),
            wordEntry("is", 4, "The number PI 3.1415926 is a magic!!!"),
            wordEntry("a", 5, "The number PI 3.1415926 is a magic!!!"),
            wordEntry("magic", 6, "The number PI 3.1415926 is a magic!!!"),
        )

        assertions.assertThat(sentences[1].text).isEqualTo("But number 2.2 is not one...")
        assertions.assertThat(sentences[1].allWords).containsExactly(
            wordEntry("But", 7, "But number 2.2 is not one..."),
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
    fun parseRealText_04() {
        val text =
            """
            Talk about your...
            ...big lizards.
             """

        val sentences: List<Sentence> = TextParserEx(SentenceEndRule.ByEndingDot).parse(text)

        val expectedSentence = "Talk about your...\n            ...big lizards."
        assertThat(sentences.map { it.text }).containsExactly(
            expectedSentence
        )

        assertThat(sentences.first().allWords).containsExactly(
            wordEntry("Talk", 0, expectedSentence),
            wordEntry("about", 1, expectedSentence),
            wordEntry("your", 2, expectedSentence),
            wordEntry("big", 3, expectedSentence),
            wordEntry("lizards", 4, expectedSentence),
        )
    }


    @Test
    fun parseRealText_05() {
        val text =
            """
            Talk about your...
            ...big lizards.
            Anyway, if you don't feel
            like being alone tonight...
            ...Joey and Chandler are
            helping me with my furniture.
            """

        val sentences: List<Sentence> = TextParserEx(SentenceEndRule.ByEndingDot).parse(text)

        val expectedSentence = "Talk about your...\n            ...big lizards."
        assertThat(sentences.map { it.text }).containsExactly(
            "Talk about your...\n            ...big lizards.",
            "Anyway, if you don't feel\n            like being alone tonight...",
            "...", // T O D O: Ideally it should be part of some sentence... probably...
            "Joey and Chandler are\n            helping me with my furniture."
        )

        assertThat(sentences.first().allWords).containsExactly(
            wordEntry("Talk", 0, expectedSentence),
            wordEntry("about", 1, expectedSentence),
            wordEntry("your", 2, expectedSentence),
            wordEntry("big", 3, expectedSentence),
            wordEntry("lizards", 4, expectedSentence),
        )
    }


    @Test
    fun parseRealText_06_0() {

        //assertEquals(".", ".")

        val text =
            //"She always drank it out of the can. I should have known. Hey. Ross, let me ask you a question."
            "She always drank it out of the can. I should have known. Hey. Ross, let me ask you a question."

        val sentences: List<Sentence> = TextParserEx(SentenceEndRule.ByEndingDot).parse(text)

        val expectedSentence1 = "She always drank it out of the can."
        val expectedSentence2 = "I should have known."

        assertThat(sentences.map { it.text }).containsExactly(
            "She always drank it out of the can.",
            "I should have known.",
            "Hey.",
            "Ross, let me ask you a question."
        )

        assertThat(sentences[0].allWords).containsExactly(
            wordEntry("She", 0, expectedSentence1),
            wordEntry("always", 1, expectedSentence1),
            wordEntry("drank", 2, expectedSentence1),
            wordEntry("it", 3, expectedSentence1),
            wordEntry("out", 4, expectedSentence1),
            wordEntry("of", 5, expectedSentence1),
            wordEntry("the", 6, expectedSentence1),
            wordEntry("can", 7, expectedSentence1),
        )

        assertThat(sentences[1].allWords).containsExactly(
            wordEntry("I", 8, expectedSentence2),
            wordEntry("should", 9, expectedSentence2),
            wordEntry("have", 10, expectedSentence2),
            wordEntry("known", 11, expectedSentence2),
        )
    }


    @Test
    fun parseRealText_06() {

        //assertEquals(".", ".")

        val text =
            """
            She always drank it out of the can.
            I should have known.
            Hey. Ross, let me ask you a question.
            """

        val sentences: List<Sentence> = TextParserEx(SentenceEndRule.ByEndingDot).parse(text)


        val expectedSentence1 = "She always drank it out of the can."
        val expectedSentence2 = "I should have known."

        assertThat(sentences.map { it.text }).containsExactly(
            "She always drank it out of the can.",
            "I should have known.",
            "Hey.",
            "Ross, let me ask you a question."
        )

        assertThat(sentences[0].allWords).containsExactly(
            wordEntry("She", 0, expectedSentence1),
            wordEntry("always", 1, expectedSentence1),
            wordEntry("drank", 2, expectedSentence1),
            wordEntry("it", 3, expectedSentence1),
            wordEntry("out", 4, expectedSentence1),
            wordEntry("of", 5, expectedSentence1),
            wordEntry("the", 6, expectedSentence1),
            wordEntry("can", 7, expectedSentence1),
        )

        assertThat(sentences[1].allWords).containsExactly(
            wordEntry("I", 8, expectedSentence2),
            wordEntry("should", 9, expectedSentence2),
            wordEntry("have", 10, expectedSentence2),
            wordEntry("known", 11, expectedSentence2),
        )
    }


    @Test
    fun parseRealText_07() {

        //assertEquals(".", ".")

        val text =
            """
            One woman. That's like saying there's only one flavor of ice cream for you.
            """

        val sentences: List<Sentence> = TextParserEx(SentenceEndRule.ByEndingDot).parse(text)

        assertThat(sentences.map { it.text }).containsExactly(
            "One woman.",
            "That's like saying there's only one flavor of ice cream for you.",
        )

        assertThat(sentences.first().allWords).contains(
            wordEntry("One", 0, "One woman."),
            wordEntry("woman", 1, "One woman."), // word woman should be without '.'
            // ...
        )
    }


    @Test
    fun extractWordsFromText_New_IgnorePrepositionAfterComa() {
        val text =
            "The colors, white, grey, blue, orange, in the wall, in the furniture, in the sky beyond the window, were . . . were . . . ."

        val sentences: List<Sentence> = TextParserEx(SentenceEndRule.ByEndingDot).parse(text)

        val resultingSentence = "The colors, white, grey, blue, orange, in the wall, in the furniture, in the sky beyond the window, were . . . were . . . ."

        assertThat(sentences.map { it.text }).containsExactly(resultingSentence)

        assertThat(sentences.first().allWords).containsExactly(
            wordEntry("The", 0, resultingSentence),
            wordEntry("colors", 1, resultingSentence),
            wordEntry("white", 2, resultingSentence),
            wordEntry("grey", 3, resultingSentence),
            wordEntry("blue", 4, resultingSentence),
            wordEntry("orange", 5, resultingSentence),
            wordEntry("in", 6, resultingSentence),
            wordEntry("the", 7, resultingSentence),
            wordEntry("wall", 8, resultingSentence),
            wordEntry("in", 9, resultingSentence),
            wordEntry("the", 10, resultingSentence),
            wordEntry("furniture", 11, resultingSentence),
            wordEntry("in", 12, resultingSentence),
            wordEntry("the", 13, resultingSentence),
            wordEntry("sky", 14, resultingSentence),
            wordEntry("beyond", 15, resultingSentence),
            wordEntry("the", 16, resultingSentence),
            wordEntry("window", 17, resultingSentence),
            wordEntry("were", 18, resultingSentence),
            wordEntry(".", 19, resultingSentence),
            wordEntry(".", 20, resultingSentence),
            wordEntry(".", 21, resultingSentence),
            wordEntry("were", 22, resultingSentence),
            wordEntry(".", 23, resultingSentence),
            wordEntry(".", 24, resultingSentence),
            wordEntry(".", 25, resultingSentence),
        )

        val cards: List<CardWordEntry> = extractWordsFromText_New(text, SentenceEndRule.ByEndingDot, emptySet())

        assertThat(cards.map { it.from })
            .containsExactly(
                "The",
                "colors",
                "white",
                "grey",
                "blue",
                "orange",
                //"orange in", // should not be present because they separated by coma
                "in",
                "the",
                "wall",
                //"wall in",   // should not be present because they separated by coma
                "in",
                "the",
                "furniture",
                //"furniture in", // should not be present because they separated by coma
                "in",
                "the",
                "sky",
                "sky beyond",
                "beyond",
                "the",
                "window",
                "were",
                "were",
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

        assertions.assertThatCode { abbreviation.thisDotIsPartOfTheAbbreviation("Mr.", 0) }
            .hasMessage("Char [M] at textDotPosition (Mr.) should point '.'.")
        assertions.assertThatCode { abbreviation.thisDotIsPartOfTheAbbreviation("Mr.", 1) }
            .hasMessage("Char [r] at textDotPosition (Mr.) should point '.'.")
        assertions.assertThatCode { abbreviation.thisDotIsPartOfTheAbbreviation("Mr. ", 3) }
            .hasMessage("Char [ ] at textDotPosition (Mr. ) should point '.'.")
        assertions.assertThatCode { abbreviation.thisDotIsPartOfTheAbbreviation("Mr.  ", 4) }
            .hasMessage("Char [ ] at textDotPosition (Mr.  ) should point '.'.")

        assertions.assertAll()
    }


    @Test
    fun temp5545() {
        val abbreviation = AbbreviationRule("e.g.")
        abbreviation.thisDotIsPartOfTheAbbreviation("ee.g.", 2)
    }


    @Test
    @DisplayName("isAbbreviation 02")
    fun test_isAbbreviation_02() {
        val assertions = SoftAssertions()

        val abbreviation = AbbreviationRule("e.g.")

        assertions.assertThatCode { abbreviation.thisDotIsPartOfTheAbbreviation("e.g.", 0) }
            .hasMessage("Char [e] at textDotPosition (e.g.) should point '.'.")

        assertions.assertThat(abbreviation.thisDotIsPartOfTheAbbreviation("e.g.", 1)).isTrue

        assertions.assertThat(abbreviation.thisDotIsPartOfTheAbbreviation(" e.g.", 2)).isTrue

        assertions.assertThat(abbreviation.thisDotIsPartOfTheAbbreviation("ee.g.", 2)).isFalse
        assertions.assertThat(abbreviation.thisDotIsPartOfTheAbbreviation("Фe.g.", 2)).isTrue

        assertions.assertThat(abbreviation.thisDotIsPartOfTheAbbreviation(" e.g. ", 2)).isTrue
        assertions.assertThat(abbreviation.thisDotIsPartOfTheAbbreviation(" e.g.\t", 2)).isTrue
        assertions.assertThat(abbreviation.thisDotIsPartOfTheAbbreviation(" e.g.\n", 2)).isTrue
        assertions.assertThat(abbreviation.thisDotIsPartOfTheAbbreviation(" e.g.\"", 2)).isTrue
        assertions.assertThat(abbreviation.thisDotIsPartOfTheAbbreviation(" e.g.\'", 2)).isTrue

        // ??? Should it be true ot false
        assertions.assertThat(abbreviation.thisDotIsPartOfTheAbbreviation(" e.g.otherEnglishChars", 2)).isTrue

        assertions.assertThatCode { abbreviation.thisDotIsPartOfTheAbbreviation("e.g.", 2) }
            .hasMessage("Char [g] at textDotPosition (e.g.) should point '.'.")

        assertions.assertThat(abbreviation.thisDotIsPartOfTheAbbreviation("e.g.", 3)).isTrue

        assertions.assertThatCode { abbreviation.thisDotIsPartOfTheAbbreviation("e.g. ", 4) }
            .hasMessage("Char [ ] at textDotPosition (e.g. ) should point '.'.")

        assertions.assertThatCode { abbreviation.thisDotIsPartOfTheAbbreviation(" e.g.", 0) }
            .hasMessage("Char [ ] at textDotPosition ( e.g.) should point '.'.")
        assertions.assertThatCode { abbreviation.thisDotIsPartOfTheAbbreviation(" e.g.", 1) }
            .hasMessage("Char [e] at textDotPosition ( e.g.) should point '.'.")

        assertions.assertThat(abbreviation.thisDotIsPartOfTheAbbreviation(" e.g.", 2)).isTrue

        assertions.assertThatCode { abbreviation.thisDotIsPartOfTheAbbreviation(" e.g.", 3) }
            .hasMessage("Char [g] at textDotPosition ( e.g.) should point '.'.")

        assertions.assertThat(abbreviation.thisDotIsPartOfTheAbbreviation(" e.g.", 4)).isTrue

        assertions.assertThatCode { abbreviation.thisDotIsPartOfTheAbbreviation(" e.g.", 5) }
            .hasMessage("String index out of range: 5")
        assertions.assertThatCode { abbreviation.thisDotIsPartOfTheAbbreviation(" e.g.", 6) }
            .hasMessage("String index out of range: 6")

        assertions.assertAll()
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


    @Test
    fun test_splitByCharSeparatorsAndPreserveAllTokens() {
        val tokens = "   \n \t Are you busy right now?  \n \t  "
            .splitByCharSeparatorsAndPreserveAllTokens(",\n\t \"")

        // hm
        assertThat(tokens).containsExactly(
            " ", " ", " ", "\n", " ", "\t", " ",
            "Are", " ", "you", " ", "busy", " ", "right", " ", "now?",
            " ", " ", "\n", " ", "\t", " ", " ",
        )
    }
}


private fun wordEntry(word: String, position: Int, sentenceText: CharSequence): WordEntry = WordEntry(word, position, Sentence(sentenceText, emptyList()))
