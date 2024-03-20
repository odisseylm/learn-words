package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.javafx.showHtmlTextPreviewDialog
import com.mvv.gui.util.firstWord
import com.mvv.gui.words.CardWordEntry
import com.mvv.gui.words.from
import com.mvv.gui.words.sourceSentences
import org.apache.commons.text.StringEscapeUtils.escapeHtml4



fun LearnWordsController.showSourceSentences(card: CardWordEntry) {
    //showTextAreaPreviewDialog(pane, "Source sentences of '${card.from}'", card.sourceSentences)

    val htmlWithHighlighting =
        escapeHtml4(card.sourceSentences.trim())
            .highlightWords(escapeHtml4(card.from.trim()), "red")
            .replace("\n", "<br/>")

    showHtmlTextPreviewDialog(pane, "Source sentences of '${card.from}'", htmlWithHighlighting)
}


internal fun String.highlightWords(wordOrPhrase: String, color: String): String {

    val startTag = "<span style=\"color:$color; font-weight: bold;\"><strong><b><font color=\"$color\">"
    val endTag = "</font></b></strong></span>"

    var result = this
    var wordIndex = -1

    //val searchWord = if (!result.contains(wordTrimmed) && wordTrimmed.contains(' '))
    //                 wordTrimmed.substringBefore(' ') else wordTrimmed
    val searchWord = this.findWordToHighlight(wordOrPhrase)

    // T O D O: would be nice to rewrite using StringBuilder, but not now :-) (it is not used very often)
    do {
        wordIndex = result.indexOf(searchWord, wordIndex + 1, true)
        if (wordIndex != -1) {
            result = result.substring(0, wordIndex) +
                    startTag +
                    result.substring(wordIndex, wordIndex + searchWord.length) +
                    endTag +
                    result.substring(wordIndex + searchWord.length)
            wordIndex += startTag.length + endTag.length
        }
    } while (wordIndex != -1)

    return result
}


internal fun String.findWordToHighlight(tryToHighlightWord: String): String {
    val word = tryToHighlightWord.trim()

    val preparations: List<(String)->String?> = listOf(
        { it },
        { it.removePrefix("to ") },
        { it.removePrefix("to be ") },
        { it.removeSuffix("ing") },
        { it.removeSuffix("ling") },
        { it.removeSuffix("e") },
        { it.removeSuffix("le") },
        { it.removeSuffix("y") },
        { it.removeSuffix("ly") },
        { it.removePrefix("to ").removeSuffix("e") },
        { it.removePrefix("to be ").removeSuffix("e") },
        { it.removePrefix("to ").removeSuffix("s to") },
        { it.removePrefix("to ").removeSuffix(" to") },
        { it.removeSuffix(" to") },
        { it.removeSuffix(" to").removeSuffix("e") },
        { it.removeSuffix(" to").removeSuffix("le") },
        { it.removeSuffix(" to").removeSuffix("y") },
        { it.removeSuffix(" to").removeSuffix("ly") },

        { it.removePrefix("to ").firstWord() },
        { it.removePrefix("to be ").firstWord() },
        { it.removePrefix("to ").firstWord()?.removeSuffix("e") },
        { it.removePrefix("to ").firstWord()?.removeSuffix("le") },
        { it.removePrefix("to ").firstWord()?.removeSuffix("y") },
        { it.removePrefix("to ").firstWord()?.removeSuffix("ly") },
        { it.removePrefix("to be ").firstWord()?.removeSuffix("s") },
    )

    return preparations.firstNotNullOfOrNull { prep ->
        val preparedWord = prep(word)
        if (preparedWord != null && preparedWord in this) preparedWord else null
    } ?: word
}
