package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.javafx.runWithScrollKeeping
import com.mvv.gui.javafx.singleSelection
import com.mvv.gui.util.containsWhiteSpaceInMiddle
import com.mvv.gui.util.filterNotBlank
import com.mvv.gui.util.firstWord
import com.mvv.gui.util.removeCharSuffixesRepeatably
import com.mvv.gui.words.*
import com.mvv.gui.words.addBaseWordsInSet
import javafx.application.Platform


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
            val firstWordOfBase = baseWords.firstWord()?.removeCharSuffixesRepeatably("!.?…")?.toString() ?: ""
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
