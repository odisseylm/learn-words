package com.mvv.gui.words

import org.junit.jupiter.api.Test



class MarryTtsSpeechSynthesizerTest {

    @Test
    fun speak() {

        val configs = listOf(
            VoiceConfigs.dfki_spike_hsmm_en_GB_male_hmm,
            VoiceConfigs.dfki_spike_en_GB_male_unitselection_general,
            VoiceConfigs.cmu_bdl_hsmm_en_US_male_hmm,
            VoiceConfigs.dfki_spike_en_GB_male_unitselection, // +-
            VoiceConfigs.dfki_prudence_en_GB_female_unitselection_general,
            VoiceConfigs.cmu_slt_hsmm_en_US_female_hmm, // ++
            VoiceConfigs.dfki_obadiah_hsmm_en_GB_male_hmm,
            VoiceConfigs.dfki_obadiah_en_GB_male_unitselection_general,
        )

        configs.forEach {
            val speechSynthesizer = MarryTtsSpeechSynthesizer(it)
            speechSynthesizer.speak("Hello and goodbye my sweetie!")
            Thread.sleep(5_000)
        }
    }

    @Test
    fun speak_working() {
        val speechSynthesizer = MarryTtsSpeechSynthesizer(VoiceConfigs.dfki_spike_hsmm_en_GB_male_hmm)
        speechSynthesizer.speak("Hello and goodbye my sweetie!")
    }

    @Test
    fun speak_theBestVoice() {
        val speechSynthesizer = MarryTtsSpeechSynthesizer(VoiceConfigs.cmu_slt_hsmm_en_US_female_hmm)
        speechSynthesizer.speak("Hello and goodbye my sweetie!")
    }
}
