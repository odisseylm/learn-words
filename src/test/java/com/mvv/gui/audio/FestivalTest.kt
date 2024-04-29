package com.mvv.gui.audio

import com.mvv.gui.test.useAssertJSoftAssertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS


// echo "testing festival" | festival --tts
// festival --pipe < festoval-data.txt

private val log = mu.KotlinLogging.logger {}


@EnabledOnOs(OS.LINUX)
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
    @Disabled("for manual testing")
    fun sayUsingGoodVoices() =
        FestivalVoiceManager().goodVoices.forEach {
            log.info("Testing voice '{}'", it.name)
            FestivalVoiceSpeechSynthesizer(it).speak("Hello Marina!")
        }

    @Test
    @Disabled("for manual testing")
    fun sayDifficultText1_UsingGoodVoices() =
        FestivalVoiceManager().goodVoices.forEach {
            log.info("Testing voice '{}'", it.name)
            FestivalVoiceSpeechSynthesizer(it).speak("6 new wireless networks found")
        }

    @Test
    @Disabled("for manual launching")
    fun playWithSpecificVoice() =
        FestivalVoiceManager().predefinedVoices
            .filter { it.name == "don_diphone" }
            .forEach {
                log.info("Testing voice '{}'", it.name)
                FestivalVoiceSpeechSynthesizer(it).speak("6 new wireless networks found")
            }

    @Test
    @Disabled("for manual launching")
    fun playWithBadVoice() {
        useAssertJSoftAssertions {
            assertThatCode {
                log.info("Testing voice 'unknown_voice'")
                FestivalVoiceSpeechSynthesizer(FestivalVoice("unknown_voice")).speak("welcome")
            }
            .hasMessageContaining("Error of speaking by festival. (SIOD ERROR: unbound variable : voice_unknown_voice)")
        }
    }
}
