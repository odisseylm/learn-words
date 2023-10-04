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
import javafx.beans.value.WritableObjectValue


private val log = mu.KotlinLogging.logger {}


// TODO: try to remove them, since now NoBaseWordInSet and BaseWordDoesNotExist are (lazy) exclusive
//val CardWordEntry.noBaseWordInSet: Boolean get() = this.statuses.contains(NoBaseWordInSet)
//val CardWordEntry.ignoreNoBaseWordInSet: Boolean get() = this.statuses.contains(BaseWordDoesNotExist)
//val CardWordEntry.showNoBaseWordInSet: Boolean get() = !this.ignoreNoBaseWordInSet && this.noBaseWordInSet

//fun CardWordEntry.hasWarning(wordCardStatus: WordCardStatus): Boolean = this.statuses.contains(wordCardStatus)
fun CardWordEntry.hasOneOfWarning(statuses: Iterable<WordCardStatus>): Boolean = this.statuses.containsOneOf(statuses)

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


    fun <T> WritableObjectValue<Set<T>>.update(value: T, toSet: Boolean): Boolean =
        updateSetProperty(this, value, if (toSet) UpdateSet.Set else UpdateSet.Remove)
    fun <T> WritableObjectValue<Set<T>>.remove(value: T): Boolean =
        updateSetProperty(this, value, UpdateSet.Remove)

    wordCardsToVerify.forEach { card ->

        val from = card.from
        val fromLowerTrimmed = from.trim().lowercase()
        val to = card.to

        val statusesProperty = card.statusesProperty
        val statuses = card.statuses

        val hasDuplicate = (allWordCardsMap[fromLowerTrimmed]?.size ?: 0) >= 2
        statusesProperty.update(Duplicates, hasDuplicate)

        if (IgnoreExampleCardCandidates in statuses)
            statusesProperty.remove(TooManyExampleNewCardCandidates)
        else {
            val tooManyExampleCardCandidates = card.exampleNewCardCandidateCount > 5 // TODO: move 5 to settings
            statusesProperty.update(TooManyExampleNewCardCandidates, tooManyExampleCardCandidates)
        }

        val noTranslation = from.isNotBlank() && to.isBlank()
        statusesProperty.update(NoTranslation, noTranslation)

        val fromIsNotPrepared = from.isBlank()
                || from.containsOneOf(unneededPartsForLearning)
                || from.any { ! (it in " -'." || it.isEnglishLetter()) }
        val toIsNotPrepared   = to.isBlank()   || to.containsOneOf(unneededPartsForLearning)
        statusesProperty.update(TranslationIsNotPrepared, fromIsNotPrepared || toIsNotPrepared)


        if (BaseWordDoesNotExist in statuses || card.fromWordCount != 1)
            statusesProperty.remove(NoBaseWordInSet)
        else {

            val baseWordCards = englishBaseWords(fromLowerTrimmed, dictionary)
            val baseWords = baseWordCards.map { it.from }

            val cardsSetContainsOneOfBaseWords = allWordCardsMap.containsOneOfKeys(baseWords)
            val cardsSetContainsAllBaseWords   = allWordCardsMap.containsAllKeys(baseWords)

            val showWarningAboutMissedBaseWord = when (warnAboutMissedBaseWordsMode) {
                NotWarnWhenSomeBaseWordsPresent -> baseWords.isNotEmpty() && !cardsSetContainsOneOfBaseWords
                WarnWhenSomeBaseWordsMissed     -> baseWords.isNotEmpty() && !cardsSetContainsAllBaseWords
            }

            val missedBaseWords = baseWords.filterNot { allWordCardsMap.containsKey(it) }
            card.missedBaseWords = missedBaseWords

            log.debug("analyzeWordCards => '{}', cardsSetContainsOneOfBaseWords: {}, cardsSetContainsAllBaseWords: {}, baseWords: {}",
                from, cardsSetContainsOneOfBaseWords, cardsSetContainsAllBaseWords, baseWords)

            statusesProperty.update(NoBaseWordInSet, showWarningAboutMissedBaseWord)
        }

    }
    log.info("### analyzeWordCards took {}ms", System.currentTimeMillis() - started)
}
