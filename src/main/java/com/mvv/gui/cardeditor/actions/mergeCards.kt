package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.javafx.runWithScrollKeeping
import com.mvv.gui.util.filterNotBlank
import com.mvv.gui.util.filterNotEmpty
import com.mvv.gui.util.firstWord
import com.mvv.gui.util.ifNotBlank
import com.mvv.gui.words.*
import java.util.*



fun LearnWordsController.isSelectionMergingAllowed(): Boolean {
    val selectedCards = currentWordsSelection.selectedItems
    if (selectedCards.size != 2) return false

    val from1baseWord = selectedCards[0].baseWordOfFromProperty.value.firstWord()
    val from2baseWord = selectedCards[1].baseWordOfFromProperty.value.firstWord()

    return from1baseWord != null && from2baseWord != null &&
          (from1baseWord.startsWith(from2baseWord) || from2baseWord.startsWith(from1baseWord))
}


fun LearnWordsController.mergeSelected() {
    val selectedCards = currentWordsSelection.selectedItems.toList() // safe copy

    val merged = mergeCards(selectedCards).adjustCard()
    // currentWordsSelection.selectedIndex is not used because it returns the most recently selected item.
    val firstCardIndex = currentWords.indexOf(selectedCards[0])

    currentWordsList.runWithScrollKeeping {
        removeCards(selectedCards)
        currentWords.add(firstCardIndex, merged)

        reanalyzeOnlyWords(merged)
    }
}




fun mergeDuplicates(cards: Iterable<CardWordEntry>): List<CardWordEntry> {
    val grouped = cards.groupByTo(TreeMap(String.CASE_INSENSITIVE_ORDER)) { it.from }
    return grouped.entries.asSequence().map { mergeCards(it.value) }.toList()
}

fun mergeCards(cards: List<CardWordEntry>): CardWordEntry {

    if (cards.size == 1) return cards.first()

    fun Iterable<CardWordEntry>.merge(delimiter: String, getter: (CardWordEntry)->String): String =
        this.map(getter).distinct().filter { it.isNotBlank() }.joinToString(delimiter)

    fun <T> Iterable<CardWordEntry>.mergeList(getter: (CardWordEntry)->List<T>): List<T> =
        this.flatMap(getter).distinct().toList()

    fun <T> Iterable<CardWordEntry>.mergeSet(getter: (CardWordEntry)->Set<T>): Set<T> =
        this.flatMap(getter).toSet()


    val card = CardWordEntry(
        mergeFrom(cards.map { it.from }),
        cards.merge("\n") { it.to },
    )

    card.fromWithPreposition = cards.merge("  ") { it.fromWithPreposition }
    card.transcription    = cards.merge(" ")  { it.transcription }
    card.examples         = cards.flatMap { it.examples.splitExamples() }.filterNotBlank()
                                 .distinct()
                                 .joinToString("\n\n")
                                 .ifNotBlank { it + "\n\n" }
    card.statuses         = cards.mergeSet    { it.statuses }
    card.predefinedSets   = cards.mergeSet    { it.predefinedSets }
    card.sourcePositions  = cards.mergeList   { it.sourcePositions }
    card.sourceSentences  = cards.merge("\n") { it.sourceSentences }
    card.missedBaseWords  = cards.mergeList   { it.missedBaseWords }
    //card.file  = cards.map { it.file }.firstOrNull()

    return card
}



private fun mergeFrom(froms: List<String>): String {

    val suffixes = listOf("s", "d", "'s", "es", "ed", "ing")

    var merged = froms.map { it.lowercase() }.filterNotEmpty().distinct().joinToString(" ")

    if (froms.size == 2) {
        val first  = froms[0]
        val second = froms[1]

        if (first == second) return first

        // TODO: add support of 2nd/3rd form of irregular verbs

        for (suffix in suffixes) {
            if ((first + suffix).equals(second, ignoreCase = true)) merged = first
            if (first.equals(second + suffix, ignoreCase = true)) merged = second
        }
    }

    return merged
}
