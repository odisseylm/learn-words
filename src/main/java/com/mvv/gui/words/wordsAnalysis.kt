package com.mvv.gui.words

import com.mvv.gui.javafx.UpdateSet
import com.mvv.gui.javafx.updateSetProperty
import com.mvv.gui.util.containsOneOf
import com.mvv.gui.util.containsOneOfKeys
import com.mvv.gui.words.WordCardStatus.*
import mu.KotlinLogging


private val log = KotlinLogging.logger {}


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


fun analyzeWordCards(allWordCards: Iterable<CardWordEntry>) = analyzeWordCards(allWordCards, allWordCards)

fun analyzeWordCards(wordCardsToVerify: Iterable<CardWordEntry>, allWordCards: Iterable<CardWordEntry>) {

    log.info("### analyzeWordCards")

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
        if (!card.ignoreNoBaseWordInSet && englishWord.mayBeDerivedWord) {

            val baseWords = possibleEnglishBaseWords(englishWord)
            val cardsSetContainsBaseWord = allWordCardsMap.containsOneOfKeys(baseWords)

            val noBaseWordStatusUpdateAction = if (cardsSetContainsBaseWord) UpdateSet.Remove else UpdateSet.Set
            updateSetProperty(card.wordCardStatusesProperty, NoBaseWordInSet, noBaseWordStatusUpdateAction)
        }
    }
}
