package com.mvv.gui.words

import com.mvv.gui.dictionary.Dictionary
import com.mvv.gui.javafx.UpdateSet
import com.mvv.gui.javafx.updateSetProperty
import com.mvv.gui.util.containsAllKeys
import com.mvv.gui.util.containsOneOf
import com.mvv.gui.util.containsOneOfKeys
import com.mvv.gui.words.WordCardStatus.*


private val log = mu.KotlinLogging.logger {}


val CardWordEntry.noBaseWordInSet: Boolean get() = this.wordCardStatuses.contains(NoBaseWordInSet)
val CardWordEntry.ignoreNoBaseWordInSet: Boolean get() = this.wordCardStatuses.contains(BaseWordDoesNotExist)
val CardWordEntry.showNoBaseWordInSet: Boolean get() = !this.ignoreNoBaseWordInSet && this.noBaseWordInSet


// These parts are usually present after getting translation from dictionary
// but them are useless (even boring/garbage) during learning,
// and it is desirable to remove this garbage
// (and remove unneeded translations to keep the shortest translation to learn it by heart).
private val unneededPartsForLearning = listOf(
    "[", "]",
    "1.", "2.", "3.",
    "1)", "2)", "3)",
    "1).", "2).", "3).",
    "1.)", "2.)", "3).",
    "*)"
)


fun analyzeWordCards(allWordCards: Iterable<CardWordEntry>, dictionary: Dictionary) = analyzeWordCards(allWordCards, allWordCards, dictionary)

fun analyzeWordCards(wordCardsToVerify: Iterable<CardWordEntry>, allWordCards: Iterable<CardWordEntry>, dictionary: Dictionary) {

    val started = System.currentTimeMillis()
    log.debug("### analyzeWordCards")

    val allWordCardsMap: Map<String, CardWordEntry> = allWordCards.associateBy { it.from.trim().lowercase() }

    wordCardsToVerify.forEach { card ->

        val from = card.from
        val to = card.to

        val noTranslationStatusUpdateAction = if (from.isNotBlank() && to.isBlank()) UpdateSet.Set else UpdateSet.Remove
        updateSetProperty(card.wordCardStatusesProperty, NoTranslation, noTranslationStatusUpdateAction)

        val translationIsNotPreparedStatusUpdateAction =
            if (from.isNotBlank() && to.containsOneOf(unneededPartsForLearning)) UpdateSet.Set else UpdateSet.Remove
        updateSetProperty(card.wordCardStatusesProperty, TranslationIsNotPrepared, translationIsNotPreparedStatusUpdateAction)

        val englishWord = from.trim().lowercase()
        if (!card.ignoreNoBaseWordInSet) {

            val baseWordCards = englishBaseWords(englishWord, dictionary)
            val baseWords = baseWordCards.map { it.from }

            val cardsSetContainsOneOfBaseWords = allWordCardsMap.containsOneOfKeys(baseWords)
            val cardsSetContainsAllBaseWords = allWordCardsMap.containsAllKeys(baseWords)
            // TODO: move to UI to use cardsSetContainsOneOfBaseWords or cardsSetContainsAllBaseWords
            val showWarningAboutMissedBaseWord = baseWords.isNotEmpty() && !cardsSetContainsOneOfBaseWords

            val missedBaseWords = baseWords.filterNot { allWordCardsMap.containsKey(it) }
            card.missedBaseWords = missedBaseWords

            log.debug("analyzeWordCards => '{}', cardsSetContainsOneOfBaseWords: {}, cardsSetContainsAllBaseWords: {}, baseWords: {}",
                englishWord, cardsSetContainsOneOfBaseWords, cardsSetContainsAllBaseWords, baseWords)

            val noBaseWordStatusUpdateAction = if (showWarningAboutMissedBaseWord) UpdateSet.Set else UpdateSet.Remove
            updateSetProperty(card.wordCardStatusesProperty, NoBaseWordInSet, noBaseWordStatusUpdateAction)
        }

        log.info("### analyzeWordCards took ${System.currentTimeMillis() - started}ms")
    }
}
