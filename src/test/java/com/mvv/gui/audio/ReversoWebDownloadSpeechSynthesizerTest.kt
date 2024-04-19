package com.mvv.gui.audio

import org.junit.jupiter.api.Test


class ReversoWebDownloadSpeechSynthesizerTest {

    @Test
    fun speak() {
        val speechSynthesizer = ReversoWebDownloadSpeechSynthesizer(JavaFxSoundPlayer(PlayingMode.Sync))

        val text = " Beijing \n "
        speechSynthesizer.cleanCacheFor(text)
        speechSynthesizer.speak(text)
    }
}
