package com.mvv.gui.words

import com.mvv.gui.words.WordCardStatus.BaseWordDoesNotExist
import com.mvv.gui.words.WordCardStatus.NoBaseWordInSet
import com.mvv.gui.javafx.UpdateSet
import com.mvv.gui.javafx.updateSetProperty
import com.mvv.gui.util.containsOneOfKeys
import mu.KotlinLogging


private val log = KotlinLogging.logger {}


val CardWordEntry.noBaseWordInSet: Boolean get() = this.wordCardStatuses.contains(NoBaseWordInSet)
val CardWordEntry.ignoreNoBaseWordInSet: Boolean get() = this.wordCardStatuses.contains(BaseWordDoesNotExist)
val CardWordEntry.showNoBaseWordInSet: Boolean get() = !this.ignoreNoBaseWordInSet && this.noBaseWordInSet


fun analyzeWordCards(allWordCards: Iterable<CardWordEntry>) = analyzeWordCards(allWordCards, allWordCards)

fun analyzeWordCards(wordCardsToVerify: Iterable<CardWordEntry>, allWordCards: Iterable<CardWordEntry>) {

    log.info("### analyzeWordCards")

    val allWordCardsMap: Map<String, CardWordEntry> = allWordCards.associateBy { it.from.trim().lowercase() }

    wordCardsToVerify.forEach { card ->
        val englishWord = card.from.trim().lowercase()

        if (!card.ignoreNoBaseWordInSet && englishWord.mayBeDerivedWord) {

            val baseWords = possibleEnglishBaseWords(englishWord)
            val cardsSetContainsBaseWord = allWordCardsMap.containsOneOfKeys(baseWords)

            val noBaseWordStatusUpdateAction = if (cardsSetContainsBaseWord) UpdateSet.Remove else UpdateSet.Set
            updateSetProperty(card.wordCardStatusesProperty, NoBaseWordInSet, noBaseWordStatusUpdateAction)
        }
    }
}
