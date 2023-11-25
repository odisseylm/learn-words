package com.mvv.gui

import com.mvv.gui.audio.*
import com.mvv.gui.util.doTry
import com.mvv.gui.util.treeStringCaseInsensitiveSet
import com.mvv.gui.words.fixFrom
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.atomic.AtomicReference


private val log = mu.KotlinLogging.logger {}


class VoiceManager {
    private val audioPlayer = JavaFxSoundPlayer(PlayingMode.Async)

    private val englishLocales = treeStringCaseInsensitiveSet("en", "en_GB", "en_US")

    // available only in Windows OS
    private fun getMsVoices(): List<SpeechSynthesizer> =
        WindowsVoiceManager().availableVoices
            .filter { it.culture.startsWith("en") }
            .map { WindowsSpeechSynthesizer(it) }
            .filter { it.isAvailable }

    private val theBestMarryVoiceNames: Set<String> = listOf(
        // It sounds very fine with some phrases, and very bad with some others...
        //PredefinedMarryTtsSpeechConfig.cmu_slt_hsmm_en_US_female_hmm,

        // It is stable enough
        PredefinedMarryTtsSpeechConfig.cmu_rms_hsmm_en_US_male_hmm,
    )
        .map { it.config.voice }
        .toSet()

    private fun getMarryVoices(): List<SpeechSynthesizer> =
        MarryTtsVoiceManager().predefinedVoices
            .filter { it.locale in englishLocales }
            .filter { it.voice in theBestMarryVoiceNames }
            .map { MarryTtsSpeechSynthesizer(it, audioPlayer) }
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

    private val lastPlayer = AtomicReference<SpeechSynthesizer>()

    val bestVoices: List<SpeechSynthesizer> = listOf(
            HowJSayWebDownloadSpeechSynthesizer(audioPlayer),
            AbbyLingvoWebDownloadSpeechSynthesizer(audioPlayer),
        ) + getMsVoices() + getMarryVoices() + getFestivalVoices() + getFreeTtsVoices()

    // Ideally it would be nice/better to use BlockingQueue with size = 1 and with the policy to keep only the latest item.
    private val playTasksQueue: BlockingQueue<Runnable> = ArrayBlockingQueue(100)
    private val executorThread = Thread {
            while (true)
                playTasksQueue.take()
                    .also { doTry { it.run() } }
        }
        .also { it.isDaemon = true; it.name = "Speak words executor" }

    @Synchronized
    private fun makeSureExecutorIsStarted() {
        if (!executorThread.isAlive) executorThread.start()
    }

    fun speak(text: String, voiceGender: Gender?) {

        makeSureExecutorIsStarted()

        // let's try to stop previous playing... but it may not be supported
        lastPlayer.get()?.interrupt()

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

        playTasksQueue.clear()
        playTasksQueue.put { playImpl(suitableVoices, fixedText) }
    }

    private fun playImpl(suitableVoices: List<SpeechSynthesizer>, fixedText: String) {
        for (voice in suitableVoices)
            if (doTry { voice.speak(fixedText) }) break
    }
}


private fun prepareText(text: String): String {
    // at six o'clock sharp
    text.replace('\'', '\'')
    return fixFrom(text.trim())
}
