package com.mvv.gui.words

import com.mvv.gui.audio.JavaSoundPlayer
import com.mvv.gui.audio.MarryTtsSpeechSynthesizer
import com.mvv.gui.audio.PlayingMode
import com.mvv.gui.audio.VoiceConfigs
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test


private val log = mu.KotlinLogging.logger {}


@Disabled("for manual testing")
class MarryTtsSpeechSynthesizerTest {

    @Test
    fun speak() {

        // good:
        //  * VoiceConfigs.cmu_slt_hsmm_en_US_female_hmm, // ++

        VoiceConfigs.values().filter { it.config.locale.startsWith("en") }.forEach {
            val started = System.currentTimeMillis()
            try {
                log.info { "Testing voice ${it.name}" }

                val speechSynthesizer = MarryTtsSpeechSynthesizer(it, audioPlayer())
                speechSynthesizer.speak("Hello and goodbye my sweetie!")

                log.info { "Voice ${it.name} is tested successfully (took ${System.currentTimeMillis() - started})." }
            }
            catch (ex: Exception) {
                log.error("error of using ${it.config.voice}/${it.config.voice_Selections}" +
                        " (took ${System.currentTimeMillis() - started}).")
            }
        }

        log.info { "All voices are tested." }
    }

    @Test
    fun speakRussian() {
        val speechSynthesizer = MarryTtsSpeechSynthesizer(VoiceConfigs.ac_irina_hsmm_ru_female_hmm, audioPlayer())
        speechSynthesizer.speak("Привет, моя сладкая!")
    }

    @Test
    fun speak_working() {
        val speechSynthesizer = MarryTtsSpeechSynthesizer(VoiceConfigs.dfki_spike_hsmm_en_GB_male_hmm, audioPlayer())
        speechSynthesizer.speak("Hello and goodbye my sweetie!")
    }

    @Test
    fun speak_theBestVoice() {
        val speechSynthesizer = MarryTtsSpeechSynthesizer(VoiceConfigs.cmu_slt_hsmm_en_US_female_hmm, audioPlayer())
        speechSynthesizer.speak("Hello and goodbye my sweetie!")
    }

    private fun audioPlayer() = JavaSoundPlayer(PlayingMode.Sync)
}
