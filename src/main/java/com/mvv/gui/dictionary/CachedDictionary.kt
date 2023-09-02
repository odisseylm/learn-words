package com.mvv.gui.dictionary

import com.mvv.gui.util.Cache
import com.mvv.gui.util.weakHashMapCache


//private val log = mu.KotlinLogging.logger {}


class CachedDictionary (private val dictionary: Dictionary) : Dictionary {

    private val cache: Cache<String, DictionaryEntry> = weakHashMapCache()

    override fun find(word: String): DictionaryEntry =
        cache.get(word) { dictionary.find(it) }
}
