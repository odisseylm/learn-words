package com.mvv.gui.audio

import com.mvv.gui.util.doTry
import com.mvv.gui.util.downloadUrl
import com.mvv.gui.util.userHome
import java.nio.file.Path
import java.util.*


class ReversoWebDownloadSpeechSynthesizer (
    audioPlayer: AudioPlayer,
) : CachingSpeechSynthesizer(audioPlayer) {

    override val voiceId: String = "reverso.net"
    override val voice: Voice = SimpleVoice("reverso.net", null)
    override val shortDescription: String = "reverso.net"

    override val audioFileExt: String = "mp3"
    override val soundWordUrlTemplates: List<String> = listOf("https://voice.reverso.net/RestPronunciation.svc/v1/output=json/GetVoiceStream/voiceName=Heather22k?voiceSpeed=100&inputText=\${word}")
    override val cacheDir: Path get() = userHome.resolve("english/.cache/web/reverso.net")

    override fun adoptTextForUrl(text: String): String = Base64.getEncoder().encodeToString(text.trim().lowercase().toByteArray(Charsets.UTF_8))

    override fun toString(): String = javaClass.simpleName

    override val isAvailable: Boolean get() = doTry( {
        downloadUrl("https://context.reverso.net/translation/english-russian/", availabilityTestNetSettings).isNotEmpty() },
        false)
}
