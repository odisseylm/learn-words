package com.mvv.gui.audio

import com.mvv.gui.util.*
import java.nio.file.Path
import kotlin.text.Charsets.ISO_8859_1
import kotlin.text.Charsets.UTF_8


//private val log = mu.KotlinLogging.logger {}


// https://dictionary.cambridge.org/media/english/us_pron/s/sou/sound/sound.mp3
// https://dictionary.cambridge.org/media/english/uk_pron/u/uks/uksor/uksorel026.mp3
// https://d1qx7pbj0dvboc.cloudfront.net/"+e+".mp3


// https://howjsay.com/how-to-pronounce-wear
class HowJSayWebDownloadSpeechSynthesizer(
    audioPlayer: AudioPlayer,
    internal val debugDump: Boolean = false,
    ) : CachingSpeechSynthesizer(audioPlayer) {

    override val voiceId: String = "howjsay.com"
    override val audioFileExt: String = "mp3"
    override val soundWordUrlTemplates: List<String> by lazy { collectSoundWordUrlTemplates() }
    override val cacheDir: Path get() = userHome.resolve("english/.cache/web/howjsay.com")

    override fun adoptTextForUrl(text: String): String = text.trim().lowercase()

    private fun collectSoundWordUrlTemplates(): List<String> {
        val bytes = downloadUrl("https://howjsay.com/js/script-min.js")
        if (debugDump) dumpTempFile(bytes, "script-min.js")
        return parseHowJSaySoundWordUrlTemplates(String(bytes, UTF_8))
    }

    override fun isSupported(text: String): Boolean = text.isBlank() || isOneWordText(text)
    override fun validateSupport(text: String) = validateTextIsOneWord(text, this.javaClass.simpleName)

    override val shortDescription: String = "howjsay.com"
    override val voice: Voice = SimpleVoice("howjsay.com", null)

    override fun toString(): String = javaClass.simpleName

    override fun validateFileContent(audioFilePath: Path, word: String) {
        super.validateFileContent(audioFilePath, word)

        val bytes = audioFilePath.readBytes(200U)
        val asString = String(bytes, ISO_8859_1)
        if (asString.startsWith("<?xml") && asString.contains("<Error>"))
            throw SpeechSynthesizerException("No data for the word [$word] at howjsay.com.")
    }

    override val isAvailable: Boolean get() = doTry( {
            downloadUrl("https://howjsay.com", availabilityTestNetSettings).isNotEmpty() },
            false)
}


private const val defaultSoundWordUrlTemplate = "https://d1qx7pbj0dvboc.cloudfront.net/\${word}.mp3"


internal fun parseHowJSaySoundWordUrlTemplates(minJs: String): List<String> =
    sequenceOf("\"#audio\"", "\"#audioQuick\"")
        .flatMap { parseHowJSaySoundWordUrlTemplates(minJs, it) }
        .distinct()
        .toList()
        .ifEmpty { listOf(defaultSoundWordUrlTemplate) }


private fun parseHowJSaySoundWordUrlTemplates(minJs: String, startTag: String): List<String> =
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
