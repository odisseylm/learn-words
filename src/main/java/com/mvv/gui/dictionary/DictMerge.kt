package com.mvv.gui.dictionary

import com.mvv.gui.trimToNull


fun mergeDictionaryEntries(word: String, dictionaryEntries: Iterable<DictionaryEntry>): DictionaryEntry {

    val uniqueTranscriptions = dictionaryEntries
        .asSequence()
        .map { it.transcription.trimToEmpty() }
        .filter { it.isNotBlank() }
        .map { normalizeTranscription(it) }
        .distinct()
        .joinToString("")
        .trimToNull()

    val uniqueTranslations: List<String> = dictionaryEntries
        .asSequence()
        .flatMap { it.translations }
        .distinctBy {
            if (partOfSpeechSynonyms.containsKey(it)) partOfSpeechSynonyms[it]
            else it.removePrefixNumber().lowercase()
        }
        .toList()

    return DictionaryEntry(word, uniqueTranscriptions, uniqueTranslations)
}

private fun String?.trimToEmpty(): String = this?.trim() ?: ""

// probably using array instead of map will be faster (but it is not critical now)
private val transcriptionChars = mapOf(
    'ɪ' to 'ı',
    'a' to 'ɑ',
    720.toChar() to 58.toChar(), // or 'ː' to ':',
)


fun normalizeTranscription(transcription: String): String {
    val withoutUnneededPrefix = if (transcription.startsWith("[01(") && transcription.endsWith(")]")) {
        val pureTranscription = transcription.removePrefix("[01(").removeSuffix(")]")
        "[$pureTranscription]"
    } else transcription

    return withoutUnneededPrefix
        .replace("ːː", "ː")
        .replace("ʧ", "tʃ")
        .replace("ʤ", "dʒ")
        .asSequence()
        .map { transcriptionChars.getOrDefault(it, it) }
        .joinToString("")
}



// 8 parts of speech: noun, pronoun, adjective, verb, adverb, preposition, conjunction, article, ?interjection, ?determiner
//
// [02noun]  [02pronoun] [02adjective] [02verb]  [02adverb]  [02preposition] [02conjunction] [02interjection] [02article]  ?? [02name]
// [02n.]    [02pron.]   [02adj.]      [02v.]    [02adv.]    [02prep.]       [02conj.]       [02interj.]      [02article]  ??
// [02 (n.)] [02 (p.)]   [02 (a.)]     [02 (v.)] [02 (adv.)] [02 (prep.)]    [02 (conj.)]    [02 (interj.)]   ??           ?? [02 (p-p.)]  [02 (pl.)]  [02 (attr.)]  [02 (pres-p.)]  [02 (predic.)]  [02 (pass.)]  [02 (pref.)]

// gainsaid	[01(geınˈseıd)]\n [02 (p.)] и [02 (p-p.)] от gainsay
// gainst	[01(geınst)]\n [02 (prep.)] [02 (поэт.)] см. against


private val partOfSpeechSynonyms: Map<String, String> = mapOf(
    // noun
    "[02n.]"    to "[02noun]",
    "[02 (n.)]" to "[02noun]",
    "[02noun]"  to "[02noun]",

    // pronoun
    "[02pron.]"   to "[02pronoun]",
    "[02 (p.)]"   to "[02pronoun]",
    "[02pronoun]" to "[02pronoun]",

    // adjective
    "[02adj.]"      to "[02adjective]",
    "[02 (a.)]"     to "[02adjective]",
    "[02adjective]" to "[02adjective]",

    // verb
    "[02v.]"    to "[02verb]",
    "[02 (v.)]" to "[02verb]",
    "[02verb]"  to "[02verb]",

    // adverb
    "[02adv.]"    to "[02adverb]",
    "[02 (adv.)]" to "[02adverb]",
    "[02adverb]"  to "[02adverb]",

    // preposition
    "[02prep.]"       to "[02preposition]",
    "[02 (prep.)]"    to "[02preposition]",
    "[02preposition]" to "[02preposition]",

    // conjunction
    "[02conj.]"       to "[02conjunction]",
    "[02 (conj.)]"    to "[02conjunction]",
    "[02conjunction]" to "[02conjunction]",

    // interjection
    "[02interj.]"      to "[02interjection]",
    "[02 (interj.)]"   to "[02interjection]",
    "[02interjection]" to "[02interjection]",

    // no sense to add for 'article'
)


private const val maxPrefixNumber = 30
private val prefixNumberRange = IntRange(1, maxPrefixNumber)
private val possiblePrefixes: List<String> = listOf(
    prefixNumberRange.map { "[$it) " },
    prefixNumberRange.map { "[$it. " },
    prefixNumberRange.map { "[$it "  },
    prefixNumberRange.map { "$it "   },
    prefixNumberRange.map { "$it) "  },
    prefixNumberRange.map { "$it. "  },

    prefixNumberRange.map { "[%02d) ".format(it) },
    prefixNumberRange.map { "[%02d. ".format(it) },
    prefixNumberRange.map { "[%02d " .format(it) },
    prefixNumberRange.map { "%02d "  .format(it) },
    prefixNumberRange.map { "%02d) " .format(it) },
    prefixNumberRange.map { "%02d. " .format(it) },
    // if leading ' ' is missed
    prefixNumberRange.map { "[%02d)".format(it) },
    prefixNumberRange.map { "[%02d.".format(it) },
    prefixNumberRange.map { "[%02d" .format(it) },
)
    .flatten()
    .toList()


// It is not optimized at all, but it is not needed.
private fun String.removePrefixNumber(): String =
    possiblePrefixes
        .find { this.startsWith(it) }
        ?.let { this.removePrefix(it) }
        ?: this


//private fun removePrefixNumber(translation: String): String {
//    val possiblyPrefixNumber = translation.substringBefore(" ", "")
//        .removeSuffix(")") // if number looks like "1) translation"
//        .removeSuffix(".") // if number looks like "1. translation"
//    return if (possiblyPrefixNumber.isNumber) translation.substringAfter(' ').trim()
//           else translation
//}
//
//
//private val String.isNumber: Boolean get() {
//    return try { this.toInt(); true }
//    catch (ignore: NumberFormatException) { false }
//}




fun extractExamples(translation: DictionaryEntry): String {
    val examplesFromDictFormat: List<String> = translation.translations
        .filter { it.startsWith("*)") }
        //.map { it.removePrefix("*)").trim() }
        .map { it.trim() }

    val omegaWikiExamples = translation.translations
        .filter { it.startsWith("[0") && it.endsWith("]") && it.contains(translation.word) && !it.containsRussianChars() }
        .map { it.removePrefixNumber().trim() }

    return (examplesFromDictFormat + omegaWikiExamples).joinToString("\n")
}


private const val russianChars = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ"

internal fun String.containsRussianChars(): Boolean =
    this.any { russianChars.contains(it) }
