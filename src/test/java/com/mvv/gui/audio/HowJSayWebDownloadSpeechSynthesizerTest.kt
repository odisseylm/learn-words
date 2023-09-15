package com.mvv.gui.audio

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.text.Charsets.UTF_8


class HowJSayWebDownloadSpeechSynthesizerTest {

    @Test
    fun parseSoundWordUrlTemplates() {

        val minJs = javaClass.getResourceAsStream("/script-min-formatted.js")
            .use { stream -> String(stream!!.readAllBytes(), UTF_8) }

        val templates = parseSoundWordUrlTemplates(minJs)

        assertThat(templates).containsExactlyInAnyOrder(
            "https://d1qx7pbj0dvboc_1.cloudfront.net/\${word}.mp3",
            "https://d1qx7pbj0dvboc_2.cloudfront.net/\${word}.mp3",
            "https://d1qx7pbj0dvboc_3.cloudfront.net/\${word}.mp3",
            "https://d1qx7pbj0dvboc_4.cloudfront.net/\${word}.mp3",
            "https://d1qx7pbj0dvboc_5.cloudfront.net/\${word}.mp3",
            "https://d1qx7pbj0dvboc_6.cloudfront.net/\${word}.mp3",
            "https://d1qx7pbj0dvboc_7.cloudfront.net/\${word}.mp3",
        )
    }


    @Test
    @Disabled("for manual testing")
    fun speak() {
        val speechSynthesizer = HowJSayWebDownloadSpeechSynthesizer(JavaFxSoundPlayer(PlayingMode.Sync))
        speechSynthesizer.speak(" door \n ")
    }
}
