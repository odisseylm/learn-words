package com.mvv.gui.words

import com.mvv.gui.dictionary.Dictionary
import com.mvv.gui.javafx.UpdateSet
import com.mvv.gui.javafx.updateSetProperty
import com.mvv.gui.cardeditor.settings
import com.mvv.gui.util.*
import com.mvv.gui.words.WarnAboutMissedBaseWordsMode.NotWarnWhenSomeBaseWordsPresent
import com.mvv.gui.words.WarnAboutMissedBaseWordsMode.WarnWhenSomeBaseWordsMissed
import com.mvv.gui.words.WordCardStatus.*
import javafx.beans.value.WritableObjectValue
import java.util.EnumSet


private val log = mu.KotlinLogging.logger {}


fun CardWordEntry.hasOneOfWarnings(statuses: Iterable<WordCardStatus>): Boolean = this.statuses.containsOneOf(statuses)

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


private fun <T> WritableObjectValue<Set<T>>.update(value: T, toSet: Boolean): Boolean =
    updateSetProperty(this, value, if (toSet) UpdateSet.Set else UpdateSet.Remove)
private fun <T> WritableObjectValue<Set<T>>.remove(value: T): Boolean =
    updateSetProperty(this, value, UpdateSet.Remove)


fun analyzeWordCards(wordCardsToVerify: Iterable<CardWordEntry>,
                     warnAboutMissedBaseWordsMode: WarnAboutMissedBaseWordsMode,
                     allWordCards: Iterable<CardWordEntry>,
                     dictionary: Dictionary) {

    val started = System.currentTimeMillis()
    log.debug("### analyzeWordCards")

    val allWordCardsMap: Map<String, List<CardWordEntry>> = allWordCards
        .flatMap {
            val fromLCTrimmed = it.from.trim().lowercase()
            listOf(Pair(fromLCTrimmed, it), Pair(fromLCTrimmed.removePrefix("to "), it))
        }
        .groupBy({ it.first }, { it.second })
        .mapValues { it.value.distinct() }

    wordCardsToVerify.forEach { card ->

        val from = card.from
        val fromLowerTrimmed = from.trim().lowercase()
        val to = card.to

        val statusesProperty = card.statusesProperty
        val statuses = card.statuses

        val noTranslation = from.isNotBlank() && to.isBlank()
        statusesProperty.update(NoTranslation, noTranslation)

        val fromOrToPreparedStatus = validateFromOrToPreparedStatus(card)
        statusesProperty.update(TranslationIsNotPrepared, !fromOrToPreparedStatus.isOk)


        if (IgnoreExampleCardCandidates in statuses)
            statusesProperty.remove(TooManyExampleNewCardCandidates) // need to remove if 'ignore' (IgnoreExampleCardCandidates) is added by user
        else {
            val tooManyExampleCardCandidates = card.exampleNewCardCandidateCount > settings.tooManyExampleCardCandidatesCount
            statusesProperty.update(TooManyExampleNewCardCandidates, tooManyExampleCardCandidates)
        }


        if (BaseWordDoesNotExist in statuses || card.fromWordCount != 1)
            statusesProperty.remove(NoBaseWordInSet) // need to remove if 'ignore' (BaseWordDoesNotExist) is added by user
        else {

            val baseWordCards = englishBaseWords(fromLowerTrimmed, dictionary)
            val baseWords = baseWordCards.map { it.from }

            val cardsSetContainsOneOfBaseWords = allWordCardsMap.containsOneOfKeys(baseWords)
            val cardsSetContainsAllBaseWords = allWordCardsMap.containsAllKeys(baseWords)

            val showWarningAboutMissedBaseWord = when (warnAboutMissedBaseWordsMode) {
                NotWarnWhenSomeBaseWordsPresent -> baseWords.isNotEmpty() && !cardsSetContainsOneOfBaseWords
                WarnWhenSomeBaseWordsMissed -> baseWords.isNotEmpty() && !cardsSetContainsAllBaseWords
            }

            val missedBaseWords = baseWords.filterNot { allWordCardsMap.containsKey(it) }
            card.missedBaseWords = missedBaseWords

            log.debug(
                "analyzeWordCards => '{}', cardsSetContainsOneOfBaseWords: {}, cardsSetContainsAllBaseWords: {}, baseWords: {}",
                from, cardsSetContainsOneOfBaseWords, cardsSetContainsAllBaseWords, baseWords
            )

            statusesProperty.update(NoBaseWordInSet, showWarningAboutMissedBaseWord)
        }

    }

    analyzeWordCardsDuplicates(allWordCards)

    log.debug("### analyzeWordCards took {}ms", System.currentTimeMillis() - started)
}


enum class FormatProblem { IsBlank, ForbiddenChars, UnpairedBrackets }

data class FormatStatus (
    val isOk: Boolean,
    val problems: Set<FormatProblem> = emptySet(),
    val problemIndex: Int = -1
)


fun validateFromOrToPreparedStatus(card: CardWordEntry): FormatStatus {
    val fromStatus = validateFromOrToPreparedStatusImpl(card.from, unneededPartsForLearning,
        unneededCharF = { !it.isEnglishLetter() && it !in " -'.!?" })
    val toStatus   = validateFromOrToPreparedStatusImpl(card.to, unneededPartsForLearning)

    return FormatStatus(
        isOk         = fromStatus.isOk && toStatus.isOk,
        problems     = fromStatus.problems + toStatus.problems,
        problemIndex = minFoundIndex(fromStatus.problemIndex, toStatus.problemIndex),
    )
}


private fun validateFromOrToPreparedStatusImpl(
    fromOrTo: String,
    unneededSubStrings: List<String>,
    unneededCharF: ((Char)->Boolean)? = null,
    ): FormatStatus {
    if (fromOrTo.isBlank())
        return FormatStatus(isOk = false, problems = setOf(FormatProblem.IsBlank))

    val unpairedBracketIndex = fromOrTo.getUnpairedBracketsIndex()
    val unneededPartsForLearningIndex: Int = minFoundIndex(
        fromOrTo.indexOfAnyOfOneOf(unneededSubStrings),
        if (unneededCharF == null) -1 else fromOrTo.indexOfFirst { unneededCharF(it) },
    )

    if (unpairedBracketIndex == -1 && unneededPartsForLearningIndex == -1)
        return FormatStatus(isOk = true)

    // T O D O: try to write in more elegant way
    val problems = EnumSet.noneOf(FormatProblem::class.java)
    if (unpairedBracketIndex != -1) problems.add(FormatProblem.UnpairedBrackets)
    if (unneededPartsForLearningIndex != -1) problems.add(FormatProblem.ForbiddenChars)

    return FormatStatus(
        isOk = problems.isEmpty(),
        problems = problems,
        problemIndex = minFoundIndex(unpairedBracketIndex, unneededPartsForLearningIndex),
    )
}


private fun analyzeWordCardsDuplicates(allWordCards: Iterable<CardWordEntry>) {

    val removeOptionalTrailingPronounOptions = SubSequenceFinderOptions(false)

    fun String.removeOptionalTrailingPronoun(): String =
        englishOptionalTrailingPronounsFinder.removeMatchedSubSequence(this, removeOptionalTrailingPronounOptions)

    fun String.asMinimizedFrom(): String {
        val keyToVerifyOnDuplicate = this.trim().lowercase().removePrefix("to ").let {
            val withoutTrailingPronoun = it.removeOptionalTrailingPronoun()
            if (withoutTrailingPronoun.wordCount == 1) it else withoutTrailingPronoun
        }
        return keyToVerifyOnDuplicate
    }


    val allWordCardsMap: Map<String, List<CardWordEntry>> = allWordCards
        .flatMap {
            val fromLCTrimmed = it.from.trim().lowercase()
            listOf(Pair(fromLCTrimmed, it), Pair(fromLCTrimmed.asMinimizedFrom(), it))
        }
        .groupBy({ it.first }, { it.second })
        .mapValues { it.value.distinct() }


    allWordCards.forEach { card ->

        val fromLowerTrimmed = card.from.trim().lowercase()

        val keyToVerifyOnDuplicate = fromLowerTrimmed.asMinimizedFrom()

        val possibleCardWordEntries = allWordCardsMap[keyToVerifyOnDuplicate]
        val hasDuplicate: Boolean = when {
            possibleCardWordEntries == null || possibleCardWordEntries.size <= 1 -> false
            else -> {
                val normalizedFroms = possibleCardWordEntries.map { it.from.trim().lowercase().removePrefix("to ").trim() }.distinct()
                if (normalizedFroms.size != 1) false
                else {
                    if (possibleCardWordEntries.size == 2) {
                        // if it is so-so logic because we cannot surely say is translation is verb or no
                        val verbCard = possibleCardWordEntries.find { it.from.trim().startsWith("to ") }
                        val probablyNonVerbCard = possibleCardWordEntries.find { !it.from.trim().startsWith("to ") }

                        val cardsAreVerbAndNotVerb = (verbCard != null && probablyNonVerbCard != null &&
                                                      verbCard.to.areAllVerbs && !probablyNonVerbCard.to.areAllVerbs)
                        !cardsAreVerbAndNotVerb
                    }
                    else true
                }
            }
        }

        card.statusesProperty.update(Duplicates, hasDuplicate)
    }
}


internal fun CharSequence.hasUnpairedBrackets(): Boolean =
    getUnpairedBracketsIndex() != -1

internal fun CharSequence.getUnpairedBracketsIndex(): Int {

    var bracketLevel = 0
    var bracketPos = -1

    for (i in this.indices) {
        val ch = this[i]

        if (ch == '(') {
            bracketLevel++
            bracketPos = i
        }
        if (ch == ')') {
            bracketLevel--
            if (bracketLevel < 0) return i
        }
    }

    return if (bracketLevel != 0) bracketPos else -1
}


private val String.areAllVerbs: Boolean get() {
    val translations = this.splitToToTranslations()
    return translations.filterNotBlank()
        .flatMap { it.splitTranslationToIndexed() }
        .map { it.splitToWords()[0] }
        .all { it.isVerb }
}
