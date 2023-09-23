package com.mvv.gui.audio

import com.mvv.gui.util.downloadUrl
import com.mvv.gui.util.firstOrThrow
import com.mvv.gui.util.safeSubstring
import com.mvv.gui.util.userHome
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.text.Charsets.UTF_8


private val log = mu.KotlinLogging.logger {}


// https://dictionary.cambridge.org/media/english/us_pron/s/sou/sound/sound.mp3
// https://dictionary.cambridge.org/media/english/uk_pron/u/uks/uksor/uksorel026.mp3
// https://d1qx7pbj0dvboc.cloudfront.net/"+e+".mp3


// https://howjsay.com/how-to-pronounce-wear
class HowJSayWebDownloadSpeechSynthesizer(private val audioPlayer: AudioPlayer) : SpeechSynthesizer {

    private val soundWordUrlTemplates: List<String> by lazy { collectSoundWordUrlTemplates() }
    private val cacheDir = userHome.resolve("english/.cache/web/howjsay.com")

    private fun cachedAudioFilePath(word: String): Path = cacheDir.resolve("${word}.mp3")

    private fun collectSoundWordUrlTemplates(): List<String> =
        parseSoundWordUrlTemplates(String(downloadUrl("https://howjsay.com/js/script-min.js"), UTF_8))

    override fun speak(text: String) {

        if (text.isBlank()) return

        val word = text.trim()
        require(word.wordCount <= 3) { "${this.javaClass.simpleName} does not support long text." }

        val cachedAudioFilePath = cachedAudioFilePath(word)
        if (!cachedAudioFilePath.exists()) {
            cachedAudioFilePath.parent.createDirectories()
            Files.write(cachedAudioFilePath, downloadAudioFile(word))
        }

        audioPlayer.play(AudioSource(cachedAudioFilePath))
    }

    private fun downloadAudioFile(word: String): ByteArray =
        soundWordUrlTemplates
            .asSequence()
            .mapNotNull { urlTemplate ->
                val url = urlTemplate.replace("\${word}", word)
                try { downloadUrl(url) }
                catch (ex: Exception) { log.debug(ex) { "Error of loading [$url]" }; null }
            }
            .filter { it.isNotEmpty() }
            .firstOrThrow { IOException("Audio file for word [$word] is not loaded.") }
}


private val String.wordCount: Int get() = this.trim().split(" \t\n").size


private const val defaultSoundWordUrlTemplate = "https://d1qx7pbj0dvboc.cloudfront.net/\${word}.mp3"


internal fun parseSoundWordUrlTemplates(minJs: String): List<String> =
    sequenceOf("\"#audio\"", "\"#audioQuick\"")
        .flatMap { parseSoundWordUrlTemplates(minJs, it) }
        .distinct()
        .toList()
        .ifEmpty { listOf(defaultSoundWordUrlTemplate) }


private fun parseSoundWordUrlTemplates(minJs: String, startTag: String): List<String> =
    minJs.indexesOf(startTag).mapNotNull { index ->
            val maxBlockSize = 200
            val block = minJs.safeSubstring(index, index + maxBlockSize)

            val urlPrefix = ".attr({src:\""
            val urlSuffix = "\"+e+\".mp3\""

            var wordUrlTemplate: String? = null

            val startAttrIndex = block.indexOf(urlPrefix)
            if (startAttrIndex != -1) {
                val endIndex = block.indexOf(urlSuffix, startAttrIndex)
                if (endIndex != -1) {
                    wordUrlTemplate = block.substring(startAttrIndex + urlPrefix.length, endIndex) + "\${word}.mp3"
                }
            }

            wordUrlTemplate
        }
        .distinct()


private fun String.indexesOf(subString: String): List<Int> {

    val indexes = mutableListOf<Int>()
    var index = -1

    do {
        index = this.indexOf(subString, index + 1)
        if (index != -1) {
            indexes.add(index)
        }
    } while (index != -1)

    return indexes
}
