package com.mvv.gui.cardeditor.actions

import com.mvv.gui.words.CardWordEntry
import com.mvv.gui.words.examples
import com.mvv.gui.words.from
import com.mvv.gui.words.to
import com.mvv.gui.words.transcription


internal class VerifyDuplicatesResult (
    // they should be ignored
    val fullDuplicates: Set<CardWordEntry>,
    // they should be skipped or merged
    val duplicatedByOnlyFrom: Set<CardWordEntry>,
)


internal fun verifyDuplicates(existentCards: List<CardWordEntry>, newCards: List<CardWordEntry>): VerifyDuplicatesResult {

    val existentCardsAsMap = existentCards.groupBy { it.from.lowercase() }

    val fullDuplicates = newCards.filter { newCard ->
        val existentCardsWithThisFrom = existentCardsAsMap[newCard.from.lowercase()] ?: return@filter false

        existentCardsWithThisFrom.any {
                    it.to == newCard.to && it.transcription == newCard.transcription && it.examples == newCard.examples
                    // old/new predefinedSets should/desired be merged
                    //
                    // We can ignore 'statuses', 'sourcePositions', 'sourceSentences'.
        }
    }.toSet()

    val partialDuplicates = newCards
        .filterNot { it in fullDuplicates }
        .filter { newCard -> existentCardsAsMap[newCard.from.lowercase()] != null }
        .toSet()

    return VerifyDuplicatesResult(fullDuplicates, partialDuplicates)
}
