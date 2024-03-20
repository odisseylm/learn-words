package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.javafx.UpdateSet
import com.mvv.gui.javafx.updateSetProperty
import com.mvv.gui.words.*


fun LearnWordsController.isOneOfSelectedWordsHasNoBaseWord(): Boolean =
    currentWordsSelection.selectedItems.any { WordCardStatus.NoBaseWordInSet in it.statuses }

fun LearnWordsController.ignoreNoBaseWordInSet() =
    currentWordsSelection.selectedItems.forEach {
                updateSetProperty(it.statusesProperty, WordCardStatus.BaseWordDoesNotExist, UpdateSet.Set) }

fun LearnWordsController.ignoreTooManyExampleCardCandidates() =
    currentWordsSelection.selectedItems.forEach {
        updateSetProperty(it.statusesProperty, WordCardStatus.IgnoreExampleCardCandidates, UpdateSet.Set) }



fun LearnWordsController.analyzeAllWords(allWords: Iterable<CardWordEntry>) =
    analyzeWordCards(allWords, currentWarnAboutMissedBaseWordsMode, allWords, dictionary)
        .also { recalculateWarnedWordsCount() }
internal fun LearnWordsController.reanalyzeAllWords() =
    analyzeAllWords(currentWords)
        .also { recalculateWarnedWordsCount() }
fun LearnWordsController.reanalyzeOnlyWords(words: Iterable<CardWordEntry>) =
    analyzeWordCards(words, currentWarnAboutMissedBaseWordsMode, currentWords, dictionary)
        .also { recalculateWarnedWordsCount() }
internal fun LearnWordsController.reanalyzeOnlyWords(vararg words: CardWordEntry) = reanalyzeOnlyWords(words.asIterable())

fun LearnWordsController.recalculateWarnedWordsCount() {
    val wordCountWithWarning = currentWords.count { it.hasOneOfWarnings(toWarnAbout) }
    pane.updateWarnWordCount(wordCountWithWarning)
}
