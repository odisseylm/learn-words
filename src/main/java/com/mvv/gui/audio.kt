package com.mvv.gui

import com.mvv.gui.audio.*
import com.mvv.gui.util.doTry
import com.mvv.gui.words.fixFrom


private val log = mu.KotlinLogging.logger {}


class VoiceManager {
    private val audioPlayer = JavaFxSoundPlayer(PlayingMode.Async)

    val bestVoices: List<SpeechSynthesizer> = listOf(
            HowJSayWebDownloadSpeechSynthesizer(audioPlayer),
            AbbyLingvoWebDownloadSpeechSynthesizer(audioPlayer),
        ) + getMsVoices() + getFestivalVoices() + getFreeTtsVoices()

    // available only in Windows OS
    private fun getMsVoices(): List<SpeechSynthesizer> =
        WindowsVoiceManager().availableVoices
            .filter { it.culture.startsWith("en") }
            .map { WindowsSpeechSynthesizer(it) }
            .filter { it.isAvailable }

    // Some (the best) Festival voices have enough good quality.
    // Available only in Linux OS.
    private fun getFestivalVoices(): List<SpeechSynthesizer> =
        FestivalVoiceManager().goodVoices
            .map { FestivalVoiceSpeechSynthesizer(it) }
            .filter { it.isAvailable }

    // Reserved, not good for learning English at all, but can be installed on both Windows and Linux and free for personal usage.
    private fun getFreeTtsVoices(): List<SpeechSynthesizer> =
        try {
            if (System.getProperty("mbrola.base").isNullOrBlank()) initFreeTts()

            val goodVoiceNames = listOf("mbrola_us1", "mbrola_us2", "mbrola_us3")
            MbrolaVoiceDirectory().voices
                .filter { it.name in goodVoiceNames }
                .sortedBy { it.name }
                //.map { FreeTtsSpeechSynthesizer(it) } // it fails if reuse the same FreeTtsSpeechSynthesizer or Voice instance
                .map { JavaSpeechSpeechSynthesizer(it) }
                .filter { it.isAvailable }
        }
        catch (ex: Exception) {
            log.warn(ex) { "Free TTS is not initialized" }
            emptyList()
        }


    fun speak(text: String, voiceGender: Gender?) {

        // I think that it would be better to use excluding surely unsuitable voices (black list instead of white list)
        // because not all voices have gender property.
        val toExcludeVoiceGender = when (voiceGender) {
            Gender.Male    -> setOf(Gender.Female, Gender.Neutral)
            Gender.Female  -> setOf(Gender.Male, Gender.Neutral)
            Gender.Neutral -> setOf(Gender.Male, Gender.Female)
            else -> emptySet() // nothing to exclude
        }

        val fixedText = prepareText(text)
        val suitableVoices = bestVoices
            .filter { it.isSupported(fixedText) }
            .filter { it.voice.gender !in toExcludeVoiceGender }

        for (voice in suitableVoices) {
            if (doTry { voice.speak(fixedText) }) break
        }
    }
}


private fun prepareText(text: String): String {
    // at six o'clock sharp
    text.replace('\'', '\'')
    return fixFrom(text.trim())
}
