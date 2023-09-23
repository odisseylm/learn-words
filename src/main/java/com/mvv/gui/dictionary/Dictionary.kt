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

    /**
     * In case if translation is not found, 'to' (translation) will be empty.
     * In this case we could return null value (or Optional<DictionaryEntry>)
     * but it would make merging more complicated.
     *
     * (But probably in the future I'll change it to this way, but not now. Need to investigate.)
     */
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
                log.error(ex) { "Error of finding word [$word] in [$dictionary]" }
                DictionaryEntry.empty(word)
            }
            else { throw ex }
        }
}
