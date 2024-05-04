package com.mvv.gui.audio

import com.mvv.gui.dictionary.getProjectDirectory
import com.mvv.gui.util.*
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.fileSize


private val log = mu.KotlinLogging.logger {}


val defaultSpeechSynthesizerNetSettings = NetSettings(timeout = Duration.ofSeconds(5))
val availabilityTestNetSettings = NetSettings(timeout = Duration.ofSeconds(5))

abstract class CachingSpeechSynthesizer (
    private val audioPlayer: AudioPlayer,
    private val netSettings: NetSettings = defaultSpeechSynthesizerNetSettings,
    ) : SpeechSynthesizer {

    private fun cachedAudioFilePath(word: String): Path = cacheDir.resolve("${word.trim()}.$audioFileExt")

    abstract val voiceId: String
    abstract val audioFileExt: String
    protected open val cacheDir: Path get() = userHome.resolve("english/.cache/${javaClass.simpleName}/$voiceId")

    protected abstract val soundWordUrlTemplates: List<String>

    protected open fun prepareText(text: String) = text.trim()
    protected open fun adoptTextForUrl(text: String) = text

    override fun speak(text: String) {

        if (text.isBlank()) return

        val preparedText = prepareText(text)

        validateSupport(preparedText)

        val cachedAudioFilePath = cachedAudioFilePath(preparedText)
        if (!cachedAudioFilePath.exists()) {
            cachedAudioFilePath.parent.createDirectories()
            Files.write(cachedAudioFilePath, downloadAudioFile(preparedText))
        }

        validateFileContent(cachedAudioFilePath, preparedText)

        audioPlayer.play(AudioSource(cachedAudioFilePath))
    }

    protected open fun validateFileContent(audioFilePath: Path, word: String) {
        if (audioFilePath.fileSize() == 0L) throw SpeechSynthesizerException("Audio file for [$word] is corrupted.")
    }

    fun cleanCacheFor(text: String) = cachedAudioFilePath(text).deleteIfExists()

    private fun downloadAudioFile(text: String): ByteArray {
        val urlText = adoptTextForUrl(text)
        return soundWordUrlTemplates
            .asSequence()
            .mapNotNull { urlTemplate ->
                val url = urlTemplate.replace("\${word}", urlText)
                try {
                    downloadUrl(url, netSettings)
                } catch (ex: Exception) {
                    log.debug(ex) { "Error of loading [$url]" }; null
                }
            }
            .filter { it.isNotEmpty() }
            .firstOrThrow { IOException("Audio file for word [$text] is not loaded.") }
    }

}


fun validateTextIsOneWord(text: String, synthesizerName: String) {
    if (!isOneWordText(text))
        throw ExpressionIsNotSupportedException("$synthesizerName does not support long text.")
}

fun isOneWordText(text: String): Boolean = text.wordCount == 1


internal fun dumpTempFile(bytes: ByteArray, baseFilename: String) {
    val dumpDir = getProjectDirectory(HowJSayWebDownloadSpeechSynthesizer::class).resolve(".tmp/.dump")
    dumpDir.createDirectories()

    val tempFile = Files.createTempFile(dumpDir, "~", baseFilename)
    Files.write(tempFile, bytes)
}
