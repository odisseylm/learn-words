package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.dictionary.Dictionary
import com.mvv.gui.util.doIfSuccess
import com.mvv.gui.util.trimToNull
import com.mvv.gui.words.CardWordEntry
import com.mvv.gui.words.from
import com.mvv.gui.words.transcription
import java.util.*


private val log = mu.KotlinLogging.logger {}


fun LearnWordsController.addTranscriptions() =
    addTranscriptions(currentWords, dictionary)
        .doIfSuccess { markDocumentIsDirty() }




/** Returns true if any transcription is added. */
internal fun addTranscriptions(wordCards: MutableList<CardWordEntry>, dictionary: Dictionary): Boolean {
    val cardsWithoutTranscription = wordCards.filter { it.transcription.isBlank() }

    cardsWithoutTranscription.forEach { card ->
        getTranscription(card.from, dictionary)
            .ifPresent { card.transcription = it }
    }

    return cardsWithoutTranscription.any { it.transcription.isNotBlank() }
}


private fun getTranscription(word: String, dictionary: Dictionary): Optional<String> =
    try { Optional.ofNullable(
        dictionary.translateWord(word).transcription.trimToNull()) }
    catch (ex: Exception) {
        log.warn(ex) { "Error of getting transcription for [${word}]." }
        Optional.empty() }

