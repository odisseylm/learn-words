package com.mvv.gui.audio

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


// echo "testing festival" | festival --tts
// festival --pipe < festoval-data.txt

private val log = mu.KotlinLogging.logger {}


class FestivalTest {

    @Test
    fun availableVoices() {
        assertThat(FestivalVoiceManager().availableVoices).isNotEmpty
    }

    @Test
    fun predefinedVoices() {
        assertThat(FestivalVoiceManager().predefinedVoices).isNotEmpty
    }

    @Test
    fun goodVoices() {
        assertThat(FestivalVoiceManager().goodVoices).isNotEmpty
    }

    @Test
    fun say() {
        FestivalVoiceSpeechSynthesizer(FestivalVoiceManager().goodVoices.first()).speak("Hello Marina!")
    }

    @Test
    fun sayUsingGoodVoices() =
        FestivalVoiceManager().goodVoices.forEach {
            log.info("Testing voice '{}'", it.name)
            FestivalVoiceSpeechSynthesizer(it).speak("Hello Marina!")
        }

    @Test
    fun sayDifficultText1_UsingGoodVoices() =
        FestivalVoiceManager().goodVoices.forEach {
            log.info("Testing voice '{}'", it.name)
            FestivalVoiceSpeechSynthesizer(it).speak("6 new wireless networks found")
        }
}
