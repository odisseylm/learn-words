package com.mvv.gui.words

import com.mvv.gui.dictionary.Dictionary
import com.mvv.gui.javafx.UpdateSet
import com.mvv.gui.javafx.updateSetProperty
import com.mvv.gui.util.containsAllKeys
import com.mvv.gui.util.containsOneOf
import com.mvv.gui.util.containsOneOfKeys
import com.mvv.gui.util.isEnglishLetter
import com.mvv.gui.words.WarnAboutMissedBaseWordsMode.NotWarnWhenSomeBaseWordsPresent
import com.mvv.gui.words.WarnAboutMissedBaseWordsMode.WarnWhenSomeBaseWordsMissed
import com.mvv.gui.words.WordCardStatus.*


private val log = mu.KotlinLogging.logger {}


// TODO: try to remove them, since now NoBaseWordInSet and BaseWordDoesNotExist are (lazy) exclusive
val CardWordEntry.noBaseWordInSet: Boolean get() = this.wordCardStatuses.contains(NoBaseWordInSet)
val CardWordEntry.ignoreNoBaseWordInSet: Boolean get() = this.wordCardStatuses.contains(BaseWordDoesNotExist)
val CardWordEntry.showNoBaseWordInSet: Boolean get() = !this.ignoreNoBaseWordInSet && this.noBaseWordInSet

//fun CardWordEntry.hasWarning(wordCardStatus: WordCardStatus): Boolean = this.wordCardStatuses.contains(wordCardStatus)
fun CardWordEntry.hasOneOfWarning(wordCardStatuses: Iterable<WordCardStatus>): Boolean = this.wordCardStatuses.containsOneOf(wordCardStatuses)

// These parts are usually present after getting translation from dictionary
// but them are useless (even boring/garbage) during learning,
// and it is desirable to remove this garbage
// (and remove unneeded translations to keep the shortest translation to learn it by heart).
private val unneededPartsForLearning = listOf(
    "<", ">",
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

    val allWordCardsMap: Map<String, List<CardWordEntry>> = allWordCards.groupBy { it.from.trim().lowercase() }

    wordCardsToVerify.forEach { card ->

        val from = card.from
        val to = card.to

        val hasDuplicate = (allWordCardsMap[from.trim().lowercase()]?.size ?: 0) >= 2
        val hasDuplicateStatusUpdateAction = if (hasDuplicate) UpdateSet.Set else UpdateSet.Remove
        updateSetProperty(card.wordCardStatusesProperty, Duplicates, hasDuplicateStatusUpdateAction)

        if (IgnoreExampleCardCandidates !in card.wordCardStatuses) {
            val tooManyExampleCardCandidates = card.exampleNewCardCandidateCount > 5 // TODO: move 5 to settings
            val tooManyExamplesStatusUpdateAction = if (tooManyExampleCardCandidates) UpdateSet.Set else UpdateSet.Remove
            updateSetProperty(card.wordCardStatusesProperty, TooManyExampleCardCandidates, tooManyExamplesStatusUpdateAction)
        }
        else {
            updateSetProperty(card.wordCardStatusesProperty, TooManyExampleCardCandidates, UpdateSet.Remove)
        }

        val noTranslation = from.isNotBlank() && to.isBlank()
        val noTranslationStatusUpdateAction = if (noTranslation) UpdateSet.Set else UpdateSet.Remove
        updateSetProperty(card.wordCardStatusesProperty, NoTranslation, noTranslationStatusUpdateAction)

        val fromIsNotPrepared = from.isBlank() || from.containsOneOf(unneededPartsForLearning)
                || !from.all { it.isEnglishLetter() || it in " -'." }
        val toIsNotPrepared   = to.isBlank()   || to.containsOneOf(unneededPartsForLearning)

        val translationIsNotPreparedStatusUpdateAction =
            if (fromIsNotPrepared || toIsNotPrepared) UpdateSet.Set else UpdateSet.Remove
        updateSetProperty(card.wordCardStatusesProperty, TranslationIsNotPrepared, translationIsNotPreparedStatusUpdateAction)

        val englishWord = from.trim().lowercase()
        if (!card.ignoreNoBaseWordInSet && card.fromWordCount == 1) {

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
        else {
            updateSetProperty(card.wordCardStatusesProperty, NoBaseWordInSet, UpdateSet.Remove)
        }

    }
    log.info("### analyzeWordCards took {}ms", System.currentTimeMillis() - started)
}
