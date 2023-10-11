package com.mvv.gui.audio

import com.mvv.gui.util.userHome
import java.nio.file.Path


// https://api.lingvolive.com/sounds?uri=LingvoUniversal%20(En-Ru)%2Fapple.wav

private val abbyLingvoNetSettings = defaultSpeechSynthesizerNetSettings.copy(ignoreCertificate = true)


class AbbyLingvoWebDownloadSpeechSynthesizer(audioPlayer: AudioPlayer) : CachingSpeechSynthesizer(audioPlayer, abbyLingvoNetSettings) {
    override val voiceId: String = "lingvolive.com"
    override val audioFileExt: String = "wav"
    override val soundWordUrlTemplates: List<String> =
        listOf("https://api.lingvolive.com/sounds?uri=LingvoUniversal%20(En-Ru)%2F\${word}.wav")
    override val cacheDir: Path get() = userHome.resolve("english/.cache/web/lingvolive.com")

    //private fun collectSoundWordUrlTemplates(): List<String> =
    //    parseSoundWordUrlTemplates(String(downloadUrl("https://howjsay.com/js/script-min.js"), Charsets.UTF_8))
    //
    // html view-source:https://www.lingvolive.com/en-us/translate/en-ru/apple can be parsed
    // to find

    override fun isSupported(text: String): Boolean = text.isBlank() || isOneWordText(text)
    override fun validateSupport(text: String) = validateTextIsOneWord(text, this.javaClass.simpleName)
}
