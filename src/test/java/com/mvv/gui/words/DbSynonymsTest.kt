package com.mvv.gui.words

import com.mvv.gui.test.useAssertJSoftAssertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Path


class DbSynonymsTest {

    @Test
    @DisplayName("parseSynonymsFromEnglishResultColumn")
    fun test_parseSynonymsFromEnglishResultColumn() {
        useAssertJSoftAssertions {
            val result = "<h4>kill</h4>" +
                    "<p>" +
                    "<strong>Definition:</strong>" +
                    "<br>" +
                    "Kill is defined as to cause to die or to destroy" +
                    "</p>" +
                    "<p>" +
                    "<strong>Example:</strong>" +
                    "<br>" +
                    "kill these lines in the President's speech" +
                    "</p>" +
                    "<p>" +
                    "<strong>Synonyms:</strong>" +
                    "<br>" +
                    " assimilate,  dispatch, massacre, murder, slaughter " +
                    "</p>" +
                    "<p>" +
                    "<strong>Antonyms:</strong>" +
                    "<br>create, produce, fashion, cause" +
                    "</p>"

            assertThat(result.parseSynonymsFromEnglishResultColumn()).containsExactly(
                "assimilate", "dispatch", "massacre", "murder", "slaughter",
            )

            assertThat("".parseSynonymsFromEnglishResultColumn()).isEmpty()
        }
    }


    @Test
    fun getSynonymsOf() { useAssertJSoftAssertions {
            val s = EnglishSynonyms()

            assertThat(s.getSynonymsOf("kill")).containsExactly(
                "assimilate", "dispatch", "massacre", "murder", "slaughter",
            )

            assertThat(s.getSynonymsOf("unknown word 456")).isEmpty()
    } }


    @Test
    @DisplayName("printAllSqlLiteTables")
    fun test_printAllSqlLiteTables() {
        printAllSqlLiteTables(Path.of("/home/vmelnykov/projects/words/learn-words/temp/russian-synonyms-newdatabase.db"))
    }

    @Test
    @DisplayName("printAllSqlLiteTables2")
    fun test_printAllSqlLiteTables2() {
        printAllSqlLiteTables(Path.of("/home/vmelnykov/projects/words/learn-words/temp/english-synonyms-newdatabase.db"))
    }
}


    /*
    @Test
    fun bb() {
        val result = "<h4>kill</h4>" +
                "<p>" +
                    "<strong>Definition:</strong>" +
                    "<br>" +
                        "Kill is defined as to cause to die or to destroy" +
                    "</p>" +
                "<p>" +
                    "<strong>Example:</strong>" +
                    "<br>" +
                    "kill these lines in the President's speech" +
                "</p>" +
                "<p>" +
                    "<strong>Synonyms:</strong>" +
                    "<br>assimilate, dispatch, massacre, murder, slaughter" +
                "</p>" +
                "<p>" +
                    "<strong>Antonyms:</strong>" +
                    "<br>create, produce, fashion, cause" +
                "</p>"

        val document: Document = result.parseAsHtml()
        //document.body().html()

        document.body().allElements
            .filter(NodeFilter { node, depth ->

                println("node.nodeName: ${node.nodeName()}")

                //if (node.nodeName() == "p" ) {
                //}
                if (node.nodeName() == "#text" &&
                    node.parentNode()?.nodeName() == "strong" &&
                    //node.parentNode()?.parentNode()?.nodeName() == "p" &&
                    node.outerHtml().trim() == "Synonyms:") {

                    // T O D O: use nextSibling()

                    val ff2 = node.parentNode()
                        ?.nextSiblings()
                        ?.filterNot { it.nodeName() == "br" }
                        ?.joinToString(" ") { it.outerHtml() }
                    println("Synonyms: $ff2")

                    NodeFilter.FilterResult.STOP
                }
                else
                    NodeFilter.FilterResult.CONTINUE
            })

        // Synonyms:

        val list: List<String> = Xsoup.compile("//p/strong").evaluate(document).list()
        println(list)

        //   https://devhints.io/xpath
        //   //button[text()="Submit"]
        //   //button[contains(text(),"Go")]
        //   //a | //div
        //   /bookstore/book/price[text()]
        //   /bookstore/book[price>35]/price
        //   /bookstore/book[price>35]/title
        //
        //val list22: Elements? = Xsoup.compile("//p/strong[text()=Synonyms]").evaluate(document).elements
        //val list22: Elements? = Xsoup.compile(".//*[@id='w3c_home_upcoming_events']/ul/li/div/p/a").evaluate(document).elements
        //println(list22)

        //val list: List<String> = Xsoup.compile("//tr/td/text()").evaluate(document).list()
        //val list3331: List<String> = Xsoup.compile("//p/strong[text()=\"Synonyms:\"]").evaluate(document).list()
        //val list3332: List<String> = Xsoup.compile("//p/strong[contains(normalize-space(), \"Berkeley\")]").evaluate(document).list()
        //val list3332: List<String> = Xsoup.compile("//p/strong[contains(normalize-space(), \"Berkeley\")]").evaluate(document).list()
        //val list3332: List<String> = Xsoup.compile("//p/strong[contains(text(), \"Berkeley\")]").evaluate(document).list()

        //val list3332: List<String> = Xsoup.compile("//button[contains(text(),\"Go\")]").evaluate(document).list()
        //val list3332: List<String> = Xsoup.compile("//p[contains(text(),\"Go\")]").evaluate(document).list()
        //println(list3332)

        / *
        run {
            val gggg = document.body().getElementsContainingOwnText("Synonyms:")
            println(gggg)
            println("\n\n---------------------------------------------------------")
            println(gggg[0].parent()?.ownText())
        }
        * /

        run {
            val gggg = document.body().getElementsContainingOwnText("synonyms")
            println(gggg)
            println("\n\n---------------------------------------------------------")
            println(gggg[0].parent()?.ownText())
        }

        //println(gggg[0].siblingElements().text())
        //println(gggg[0].siblingElements().size)
        //println(gggg[0].siblingElements()[0].siblingElements().text())
        //println(gggg[0].siblingElements())
        //println(gggg[0].siblingNodes())

        //println(gggg[0].ownText())
        //println(gggg[0].wholeOwnText())
        //println(gggg[0].wholeText())
        //println(gggg[0].selectXpath())
        //println(gggg[0].nextSiblings().map { it. })
    }
    */
}
*/
