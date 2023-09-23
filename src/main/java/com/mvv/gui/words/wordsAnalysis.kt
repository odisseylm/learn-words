package com.mvv.gui.words

import com.mvv.gui.dictionary.Dictionary
import com.mvv.gui.javafx.UpdateSet
import com.mvv.gui.javafx.updateSetProperty
import com.mvv.gui.util.containsAllKeys
import com.mvv.gui.util.containsOneOf
import com.mvv.gui.util.containsOneOfKeys
import com.mvv.gui.words.WarnAboutMissedBaseWordsMode.NotWarnWhenSomeBaseWordsPresent
import com.mvv.gui.words.WarnAboutMissedBaseWordsMode.WarnWhenSomeBaseWordsMissed
import com.mvv.gui.words.WordCardStatus.*


private val log = mu.KotlinLogging.logger {}


val CardWordEntry.noBaseWordInSet: Boolean get() = this.wordCardStatuses.contains(NoBaseWordInSet)
val CardWordEntry.ignoreNoBaseWordInSet: Boolean get() = this.wordCardStatuses.contains(BaseWordDoesNotExist)
val CardWordEntry.showNoBaseWordInSet: Boolean get() = !this.ignoreNoBaseWordInSet && this.noBaseWordInSet

val CardWordEntry.hasWarning: Boolean get() = this.showNoBaseWordInSet ||
        this.wordCardStatuses.containsOneOf(NoTranslation, TranslationIsNotPrepared)

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


enum class WarnAboutMissedBaseWordsMode { // WarnWhenSomeWordsMissed NotWarnWhenSomeBaseWordsPresent
    WarnWhenSomeBaseWordsMissed,
    NotWarnWhenSomeBaseWordsPresent,
}


@Suppress("unused")
fun analyzeWordCards(allWordCards: Iterable<CardWordEntry>, warnAboutMissedBaseWordsMode: WarnAboutMissedBaseWordsMode, dictionary: Dictionary) =
    analyzeWordCards(allWordCards, warnAboutMissedBaseWordsMode, allWordCards, dictionary)

fun analyzeWordCards(wordCardsToVerify: Iterable<CardWordEntry>,
                     warnAboutMissedBaseWordsMode: WarnAboutMissedBaseWordsMode,
                     allWordCards: Iterable<CardWordEntry>,
                     dictionary: Dictionary) {

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

            val showWarningAboutMissedBaseWord = when (warnAboutMissedBaseWordsMode) {
                NotWarnWhenSomeBaseWordsPresent -> baseWords.isNotEmpty() && !cardsSetContainsOneOfBaseWords
                WarnWhenSomeBaseWordsMissed     -> baseWords.isNotEmpty() && !cardsSetContainsAllBaseWords
            }

            val missedBaseWords = baseWords.filterNot { allWordCardsMap.containsKey(it) }
            card.missedBaseWords = missedBaseWords

            log.debug("analyzeWordCards => '{}', cardsSetContainsOneOfBaseWords: {}, cardsSetContainsAllBaseWords: {}, baseWords: {}",
                englishWord, cardsSetContainsOneOfBaseWords, cardsSetContainsAllBaseWords, baseWords)

            val noBaseWordStatusUpdateAction = if (showWarningAboutMissedBaseWord) UpdateSet.Set else UpdateSet.Remove
            updateSetProperty(card.wordCardStatusesProperty, NoBaseWordInSet, noBaseWordStatusUpdateAction)
        }

    }
    log.info("### analyzeWordCards took {}ms", System.currentTimeMillis() - started)
}
