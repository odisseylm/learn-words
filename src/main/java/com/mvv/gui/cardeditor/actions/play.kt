package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.javafx.singleSelection
import com.mvv.gui.words.CardWordEntry
import com.mvv.gui.words.from
import org.apache.commons.lang3.exception.ExceptionUtils
import java.util.concurrent.CompletableFuture


internal fun LearnWordsController.playSelectedWord() = currentWordsList.singleSelection?.let { playFrom(it) }


private fun LearnWordsController.playFrom(card: CardWordEntry) {
    //val voice = settingsPane.voice

    CompletableFuture.runAsync { playWord(card.from.trim()) }
        .exceptionally {
            val rootCause = ExceptionUtils.getRootCause(it)
            //log.info { "Word [${card.from}] is not played => ${rootCause.javaClass.simpleName}: ${rootCause.message}" }
            log.info { "Word [${card.from}] is not played => ${rootCause.message}" }
            null
        }
}


private fun LearnWordsController.playWord(text: String) = voiceManager.speak(text, settingsPane.playVoiceGender)
