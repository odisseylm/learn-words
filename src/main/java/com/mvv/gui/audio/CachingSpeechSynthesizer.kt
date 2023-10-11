package com.mvv.gui.audio

import com.mvv.gui.util.NetSettings
import com.mvv.gui.util.downloadUrl
import com.mvv.gui.util.firstOrThrow
import com.mvv.gui.util.userHome
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists


private val log = mu.KotlinLogging.logger {}


val defaultSpeechSynthesizerNetSettings = NetSettings()

abstract class CachingSpeechSynthesizer (
    protected val audioPlayer: AudioPlayer,
    private   val netSettings: NetSettings = defaultSpeechSynthesizerNetSettings,
    ) : SpeechSynthesizer {

    private fun cachedAudioFilePath(word: String): Path = cacheDir.resolve("${word.trim()}.$audioFileExt")

    abstract val voiceId: String
    abstract val audioFileExt: String
    protected open val cacheDir: Path get() = userHome.resolve("english/.cache/${javaClass.simpleName}/$voiceId")

    protected abstract val soundWordUrlTemplates: List<String>


    override fun speak(text: String) {

        if (text.isBlank()) return

        val word = text.trim()

        validateSupport(word)

        val cachedAudioFilePath = cachedAudioFilePath(word)
        if (!cachedAudioFilePath.exists()) {
            cachedAudioFilePath.parent.createDirectories()
            Files.write(cachedAudioFilePath, downloadAudioFile(word))
        }

        audioPlayer.play(AudioSource(cachedAudioFilePath))
    }

    fun cleanCacheFor(text: String) = cachedAudioFilePath(text).deleteIfExists()

    private fun downloadAudioFile(word: String): ByteArray =
        soundWordUrlTemplates
            .asSequence()
            .mapNotNull { urlTemplate ->
                val url = urlTemplate.replace("\${word}", word)
                try { downloadUrl(url, netSettings) }
                catch (ex: Exception) { log.debug(ex) { "Error of loading [$url]" }; null }
            }
            .filter { it.isNotEmpty() }
            .firstOrThrow { IOException("Audio file for word [$word] is not loaded.") }

}


fun validateTextIsOneWord(text: String, synthesizerName: String) {
    if (!isOneWordText(text))
        throw ExpressionIsNotSupportedException("$synthesizerName does not support long text.")
}

fun isOneWordText(text: String): Boolean = text.trim().wordCount == 1

private val String.wordCount: Int get() = this.trim().split(" ", "\t", "\n").size
