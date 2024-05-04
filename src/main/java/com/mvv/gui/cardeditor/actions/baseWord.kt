package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.dictionary.Dictionary
import com.mvv.gui.javafx.runWithScrollKeeping
import com.mvv.gui.javafx.singleSelection
import com.mvv.gui.util.containsWhiteSpaceInMiddle
import com.mvv.gui.util.filterNotBlank
import com.mvv.gui.util.firstWord
import com.mvv.gui.util.removeCharSuffixesRepeatably
import com.mvv.gui.words.*
import javafx.application.Platform
import javafx.collections.ObservableList



fun LearnWordsController.addAllBaseWordsInSet() = addAllBaseWordsInSetImpl(currentWords)
fun LearnWordsController.addBaseWordsInSetForSelected() = addAllBaseWordsInSetImpl(currentWordsSelection.selectedItems)

private fun LearnWordsController.addAllBaseWordsInSetImpl(wordCards: Iterable<CardWordEntry>) =
    currentWordsList.runWithScrollKeeping {

        val addedWordsMapping = addBaseWordsInSet(wordCards, currentWarnAboutMissedBaseWordsMode, currentWords, dictionary)

        if (addedWordsMapping.size == 1) {
            val newBaseWordCard = addedWordsMapping.values.asSequence().flatten().first()

            // select new base word to edit it immediately
            if (currentWordsSelection.selectedItems.size <= 1) {
                currentWordsSelection.clearSelection()
                currentWordsSelection.select(newBaseWordCard.adjustCard())
            }
        }
    }


fun LearnWordsController.selectByBaseWord() {
    val baseWordOfFrom = currentWordsList.singleSelection?.baseWordOfFrom ?: return

    val toSelect = currentWords.filtered { it.baseWordOfFrom.startsWith(baseWordOfFrom) }
    toSelect.forEach { currentWordsList.selectionModel.select(it) }
}


fun LearnWordsController.createBaseWordExtractor(): BaseWordExtractor {
    val englishVerbs = EnglishVerbs()
    return object : BaseWordExtractor {
        override fun extractBaseWord(phrase: String): String {
            val baseWords = prefixFinder.calculateBaseOfFromForSorting(phrase)
            val firstWordOfBase = baseWords.firstWord()?.removeCharSuffixesRepeatably("!.?…") ?: ""
            // In general, it would be nice to get infinitive for regular verbs,
            // but it is impossible for all cases, and now we can live with that
            // because these words in any cases will follow after base word because they have the same word root.
            return englishVerbs.getIrregularInfinitive(firstWordOfBase) ?: firstWordOfBase
        }
    }
}


internal fun LearnWordsController.rebuildPrefixFinder() {
    // only words without phrases
    val cards = currentWords.toList()
    val onlyPureWords = cards.map { it.from }
        .filterNotBlank()
        .filterNot { it.containsWhiteSpaceInMiddle() }
        .toSet()

    if (this.prefixFinder.ignoredWords != onlyPureWords) {
        // Rebuilding PrefixFinder_New is very fast - no need to use separate thread
        //CompletableFuture.runAsync { rebuildPrefixFinderImpl(onlyPureWords) }

        rebuildPrefixFinderImpl(onlyPureWords)
    }
}

fun LearnWordsController.rebuildPrefixFinderImpl(onlyPureWords: Set<String>) {
    this.prefixFinder = englishPrefixFinder(onlyPureWords)
    Platform.runLater { currentWords.toList().forEach { it.baseWordOfFromProperty.resetCachedValue() } }
}


internal fun addBaseWordsInSet(wordCardsToProcess: Iterable<CardWordEntry>,
                               warnAboutMissedBaseWordsMode: WarnAboutMissedBaseWordsMode,
                               allWordCards: ObservableList<CardWordEntry>,
                               dictionary: Dictionary
    ): Map<CardWordEntry, List<CardWordEntry>> {

    val allWordCardsMap: Map<String, CardWordEntry> = allWordCards.associateBy { it.from.trim().lowercase() }

    val withoutBaseWord = wordCardsToProcess
        .asSequence()
        .filter { WordCardStatus.NoBaseWordInSet in it.statuses }
        .toSortedSet(cardWordEntryComparator)

    val baseWordsToAddMap: Map<CardWordEntry, List<CardWordEntry>> = withoutBaseWord
        .asSequence()
        .map { card -> Pair(card, englishBaseWords(card.from, dictionary, card)) }
        .filter { it.second.isNotEmpty() }
        .associate { it }

    baseWordsToAddMap.forEach { (currentWordCard, baseWordCards) ->
        // T O D O: optimize this n*n
        val index = allWordCards.indexOf(currentWordCard)
        baseWordCards
            .filterNot { allWordCardsMap.containsKey(it.from) }
            .forEachIndexed { i, baseWordCard ->
                allWordCards.add(index + i, baseWordCard)
        }
    }
    analyzeWordCards(withoutBaseWord, warnAboutMissedBaseWordsMode, allWordCards, dictionary)

    return baseWordsToAddMap
}
