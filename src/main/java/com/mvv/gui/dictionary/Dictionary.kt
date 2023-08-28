package com.mvv.gui.dictionary

import mu.KotlinLogging


private val log = KotlinLogging.logger {}


data class DictionaryEntry (
    /** It may contain phrase to if dictionary supports it and can find phrase. */
    val word: String,
    val transcription: String?,
    val translations: List<String>,
) {
    companion object {
        fun empty(word: String) = DictionaryEntry(word, null, emptyList())
    }
}


interface Dictionary {
    fun find(word: String): DictionaryEntry
}


class DictionaryComposition (private val dictionaries: List<Dictionary>) : Dictionary {

    override fun find(word: String): DictionaryEntry = find(word, true)

    fun find(word: String, ignoreSubDirectoryErrors: Boolean): DictionaryEntry =
        dictionaries
            .map { findInDictionaryImpl(it, word, ignoreSubDirectoryErrors) }
            .let { mergeDictionaryEntries(word, it) }

    private fun findInDictionaryImpl(dictionary: Dictionary, word: String, ignoreErrors: Boolean) =
        try { dictionary.find(word) }
        catch (ex: Exception) {
            if (ignoreErrors) {
                log.error("Error of finding word [{}] in [{}]", word, dictionary, ex)
                DictionaryEntry.empty(word)
            }
            else { throw ex }
        }
}
