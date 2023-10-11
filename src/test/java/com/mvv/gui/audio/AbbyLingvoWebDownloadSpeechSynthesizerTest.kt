package com.mvv.gui.audio

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Disabled


class AbbyLingvoWebDownloadSpeechSynthesizerTest {

    @Test
    @Disabled("for manual testing")
    fun speak() {
        val s = AbbyLingvoWebDownloadSpeechSynthesizer(JavaFxSoundPlayer(PlayingMode.Sync))

        s.cleanCacheFor("apple")
        s.speak("apple")
    }
}
