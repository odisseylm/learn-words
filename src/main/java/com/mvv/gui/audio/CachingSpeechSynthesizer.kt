package com.mvv.gui.audio

import com.mvv.gui.util.*
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.fileSize


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

        validateFileContent(cachedAudioFilePath, word)

        audioPlayer.play(AudioSource(cachedAudioFilePath))
    }

    protected open fun validateFileContent(audioFilePath: Path, word: String) {
        if (audioFilePath.fileSize() == 0L) throw SpeechSynthesizerException("Audio file for [$word] is corrupted.")
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

fun isOneWordText(text: String): Boolean = text.wordCount == 1
