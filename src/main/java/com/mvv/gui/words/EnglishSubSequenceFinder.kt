package com.mvv.gui.words

import com.mvv.gui.util.*


private val findMatchedPrefixOptions = SubSequenceFinderOptions(true)

fun SubSequenceFinder.findMatchedPrefix(phrase: String): String? {
    require(this.direction == Direction.Forward) {
        "findMatchedPrefix() should be used only with 'forward' SubSequenceFinder." }
    return this.findMatchedSubSequence(phrase, findMatchedPrefixOptions)
}


fun SubSequenceFinder.calculateBaseOfFromForSorting(phrase: String): String {
    val fixedPhrase = phrase.lowercase().trim()

    val prefix = findMatchedPrefix(fixedPhrase)
        ?: return fixedPhrase

    var base = fixedPhrase.removeRepeatableSpaces().substring(prefix.length).trimStart()
    if (base.isNotBlank()) return base

    base = fixedPhrase.removePrefix("to be ").removePrefix("to ")
    if (base.isNotBlank()) return base

    return fixedPhrase
}


fun englishPrefixFinder(ignoredWords: Collection<String> = emptySet()): SubSequenceFinder =
    SubSequenceFinder(englishPossibleNonRelevantForSortingPrefixTemplates, Direction.Forward, ignoredWords, englishLanguageRules)


private val englishPossibleNonRelevantForSortingPrefixTemplates: Alt<Seq<String>> = sequenceOf(
    "{verb}",
    "{verb} no",
    "{verb} no {art}",

    "{verb} {art}",
    "{verb} {prep} {art}",

    "{verb} to {art}",
    "{verb} to {prep} {art}",

    "to {verb}",
    "to {verb} no",
    "to {verb} no {art}",

    "to {verb} {art}",
    "to {verb} {prep} {art}",
    "not to {verb} {art}",
    "not to {verb} {prep} {art}",

    "to {verb} to",
    "to {verb} to {art}",
    "to {verb} to {prep} {art}",

    "{pp} {verb} {art}",
    "{pp} {verb} {prep} {art}",
    "{pp} {verb} to {art}",
    "{pp} {verb} to {prep} {art}",
    "{pp} {verb} not {art}",
    "{pp} {verb} not {prep} {art}",

    // for past perfect tense
    "{pp} {verb} {verb} {art}",
    "{pp} {verb} {verb} {prep} {art}",
    "{pp} {verb} {verb} to {art}",
    "{pp} {verb} {verb} to {prep} {art}",
    "{pp} {verb} {verb} not {art}",
    "{pp} {verb} {verb} not {prep} {art}",

    "{verb} {pp} to {prep} {art}",
    "{verb} {pp} {prep} {art}",
    "{verb} {pp} to {art}",
    "{verb} {pp} {art}",

    "{verb} {pp} {verb} {art}",
    "{verb} {pp} {verb} {prep} {art}",

    "let {pp}",
    "let {pp} {verb}",
    "let {pp} to {verb}",
    "let {pp} {verb} {art}",
    "let {pp} to {verb} {art}",
    "let {pp} {verb} {prep} {art}",
    "let {pp} to {verb} {prep} {art}",

    "not to be {art}",
    "not to {art}",
    "to {art}",

    "even with {art}",
    "{prep} {art}",
    "on no",

    "to {art}",
    "not to {art}",
    "not {prep} {art}",
    "not {art}",

    "by all {art}",
    "all {art}",
    "and {art}",
    "or {art}",

    "{art} no",
    "{art}",
    "no",

    "{art} few",
    "{art} many",

    "beyond all", "at short", "only",
    "all of a", "all on",

    "what a", "what the",
    "what's a", "what's the",
    "what is a", "what is the",
    "what is he",

    "new",
    "{pp}",
).splitToWords()


private val englishArticlesAndSimilar: Alt<Seq<String>> = sequenceOf(
    "one's", "ones", "one",

    "somebody's", "somebody", "smb's.", "smb.'s", "smb's", "smbs'", "smbs", "smb.", "smb",
    "somebody's a", "somebody a", "smb's. a", "smb's a", "smbs' a", "smbs a", "smb. a", "smb a",
    "somebody's the", "somebody the", "smb's. the", "smb's the", "smbs' the", "smbs the", "smb. the", "smb the",

    "smth.'s", "smth's.", "smt.'s", "smt's.", "something's",
    "smth.", "smth", "smt.", "smt", "something",

    "every", "one", "your", "mine", "one's own",
    "daily",
    "forbidden",
    "slightly",
    "front", "the next", "next",
    "a short", "short",
    "a long", "long",
    "a high", "high",
    "a low", "low",
    "a full", "full",
    "a big", "big", "a large", "large",
    "a small", "small", "a tiny", "tiny", "a little", "little",
    "a tall", "tall",
    "a thick", "thick", "a thin", "thin",
    "a bad", "bad", "worse", "the worst",
    "a good", "good", "better", "the best",
    "a hard", "hard", "harder",
    "a heavy", "heavy",
    "a great", "great",
    "a half", "half",
    "the public","a public", "public",
    "enough", "a common", "common",

    // Personal pronouns
    // Subject Pronouns
    "i", "you", "he", "she", "it", "we", "you", "they",
    // Object pronouns
    "me", "you", "him", "her", "it", "us", "you", "them",
    // Possessive pronouns
    "mine", "yours", "his", "hers", "its", "ours", "yours", "theirs",

    "my", "his", "her", "their", "its",

    // Demonstrative pronouns
    "this", "these", "that", "those",
    // Interrogative pronouns
    "who", "whom", "which", "what",
    // Relative pronouns
    "who", "whom", "that", "which", "whoever", "whichever", "whomever",
    // Indefinite pronouns
    "all", "another", "any", "anybody", "anyone", "anything", "each", "everybody", "everyone", "everything",
    "few", "many", "nobody", "none", "one", "several", "some", "somebody", "and someone",
    // Reflexive pronouns
    "myself", "yourself", "himself", "herself", "ourselves", "yourselves", "themselves",

    // articles
    "the", "an", "a",
).splitToWords()


private val englishPronouns: Alt<Seq<String>> = sequenceOf(
    // Personal pronouns
    // Subject Pronouns
    "i", "you", "he", "she", "it", "we", "you", "they",
    // Object pronouns
    "me", "you", "him", "her", "it", "us", "you", "them",
    // Possessive pronouns
    "mine", "yours", "his", "hers", "its", "ours", "yours", "theirs",

    // Demonstrative pronouns
    "this", "these", "that", "those",
    // Interrogative pronouns
    "who", "whom", "which", "what",
    // Relative pronouns
    "who", "whom", "that", "which", "whoever", "whichever", "whomever",
    // Indefinite pronouns
    "all", "another", "any", "anybody", "anyone", "anything", "each", "everybody", "everyone", "everything",
    "few", "many", "nobody", "none", "one", "several", "some", "somebody", "and someone",
    // Reflexive pronouns
    "myself", "yourself", "himself", "herself", "ourselves", "yourselves", "themselves",
).splitToWords()

//private val maxExpectedNodesCount = max(verbs.size, articlesAndSimilar.size, prepositions.size)

private val englishLanguageRules = SubSequenceLanguageRules(
    englishArticlesAndSimilar,
    prepositions,
    englishCommonVerbs,
    englishPronouns,
)


fun englishOptionalTrailingPronounsFinder(): SubSequenceFinder =
    SubSequenceFinder(englishOptionalEndingsPronounsTemplates, Direction.Backward, emptyList(), englishLanguageRules)

private val englishOptionalEndingsPronounsTemplates: Alt<Seq<String>> = sequenceOf(
    "{pp}",
    "{art}",
).splitToWords()

val englishOptionalTrailingPronounsFinder: SubSequenceFinder = englishOptionalTrailingPronounsFinder()
