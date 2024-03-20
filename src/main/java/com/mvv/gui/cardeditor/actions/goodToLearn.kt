package com.mvv.gui.cardeditor.actions

import com.mvv.gui.words.CardWordEntry
import com.mvv.gui.words.from
import com.mvv.gui.words.parseSentence



private val ignoreWordsForGoodLearnCardCandidate = listOf("a", "the", "to", "be")

fun CardWordEntry.isGoodLearnCardCandidate(): Boolean {
    val sentence = parseSentence(this.from, 0)
    val wordCount = sentence.allWords.asSequence()
        .filter { it.word !in ignoreWordsForGoodLearnCardCandidate }
        .count()
    return wordCount <= 4 && sentence.allWords
        // ??? Why I've added it? ???
        .all { !it.word.first().isUpperCase() }
}
