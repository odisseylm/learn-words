package com.mvv.gui.audio

import com.sun.speech.freetts.VoiceDirectory
import com.sun.speech.freetts.VoiceManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Path



// TODO: add conditional annotations for 'mbrola' tests
class FreeTtsTest {

    @BeforeEach
    fun cleanUp() {
        System.clearProperty("mbrola.base")
        System.clearProperty("freetts.voices")
    }


    @Test
    fun getAlanVoice() {
        System.setProperty("freetts.voices", com.sun.speech.freetts.en.us.cmu_time_awb.AlanVoiceDirectory::class.java.name)
        assertThat(VoiceManager.getInstance().voices.map { it.name }).containsExactly("alan")
    }


    @Test
    fun getKevinVoice() {
        System.setProperty("freetts.voices", com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory::class.java.name)
        assertThat(VoiceManager.getInstance().voices.map { it.name }).containsExactlyInAnyOrder("kevin", "kevin16")
    }


    // TODO: add tests (and probably impl) for alternative directories like
    //   * /usr/lib/x86_64-linux-gnu/espeak-ng-data/mbrola_ph
    //   * /usr/lib/x86_64-linux-gnu/espeak-data/mbrola_ph
    @Test
    fun getMbrolaVoices() {
        System.setProperty("mbrola.base", "/usr/share/mbrola")

        val accessibleVoices = MbrolaVoiceDirectory().voices
        assertThat(accessibleVoices).isNotEmpty

        System.setProperty("freetts.voices", MbrolaVoiceDirectory::class.java.name)
        assertThat(VoiceManager.getInstance().voices.map { it.name }).containsExactlyInAnyOrder(
            "mbrola_en1",
            "mbrola_us1",
            "mbrola_us2",
            "mbrola_us3",
        )
    }


    @Test
    fun getMbrolaVoices_alt() {
        System.setProperty("mbrola.base", "")

        val accessibleVoices = MbrolaVoiceDirectory(Path.of("/usr/share/mbrola")).voices
        assertThat(accessibleVoices).isNotEmpty

        //System.setProperty("freetts.voices", MbrolaVoiceDirectory::class.java.name)
        //assertThat(VoiceManager.getInstance().voices).isNotEmpty
    }


    @Test
    fun getAllVoices() {
        System.setProperty("mbrola.base", "/usr/share/mbrola")

        val accessibleVoices = MbrolaVoiceDirectory().voices
        assertThat(accessibleVoices).isNotEmpty

        val voiceDirectoryClasses: List<Class<out VoiceDirectory>> = listOf(
            com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory::class.java,
            com.sun.speech.freetts.en.us.cmu_time_awb.AlanVoiceDirectory::class.java,
            MbrolaVoiceDirectory::class.java,
        )
        System.setProperty("freetts.voices", voiceDirectoryClasses.joinToString(",") { it.name })

        assertThat(VoiceManager.getInstance().voices.map { it.name }).containsExactlyInAnyOrder(
            "kevin",
            "kevin16",
            "alan",
            "mbrola_en1",
            "mbrola_us1",
            "mbrola_us2",
            "mbrola_us3",
        )
    }


    @Test
    fun playVia_JavaSpeechSpeechSynthesizer() {
        System.clearProperty("mbrola.base")
        System.clearProperty("freetts.voices")

        initFreeTts()
        val voiceManager = VoiceManager.getInstance()

        voiceManager.getVoice("kevin")
            .let { JavaSpeechSpeechSynthesizer(it.toJavaxSpeechVoice(), it.locale) }
            .speak("Welcome John!")
        JavaSpeechSpeechSynthesizer(voiceManager.getVoice("kevin16"))
            .speak("Welcome John!")

        // 'alan' does not work
        JavaSpeechSpeechSynthesizer(voiceManager.getVoice("alan"))
            .speak("Welcome John!")

        // Or does not work good with java FreeTTS or need to configure properly.
        //JavaSpeechSpeechSynthesizer(voiceManager.getVoice("mbrola_en1"))
        //    .speak("Welcome John!")

        JavaSpeechSpeechSynthesizer(voiceManager.getVoice("mbrola_us1"))
            .speak("Welcome John!")
        JavaSpeechSpeechSynthesizer(voiceManager.getVoice("mbrola_us2"))
            .speak("Welcome John!")
        JavaSpeechSpeechSynthesizer(voiceManager.getVoice("mbrola_us3"))
            .speak("Welcome John!")
    }


    @Test
    fun playVia_FreeTtsSpeechSynthesizer() {
        System.clearProperty("mbrola.base")
        System.clearProperty("freetts.voices")

        initFreeTts()
        val voiceManager = VoiceManager.getInstance()

        FreeTtsSpeechSynthesizer(voiceManager.getVoice("kevin")).speak("Welcome John!")
        FreeTtsSpeechSynthesizer(voiceManager.getVoice("kevin16")).speak("Welcome John!")

        // seems 'alan' quietly does not work
        //FreeTtsSpeechSynthesizer(voiceManager.getVoice("alan")).speak("Welcome John!")

        // Or does not work good with java FreeTTS or need to configure properly.
        //FreeTtsSpeechSynthesizer(voiceManager.getVoice("mbrola_en1")).speak("Welcome John!")

        FreeTtsSpeechSynthesizer(voiceManager.getVoice("mbrola_us1")).speak("Welcome John!")
        FreeTtsSpeechSynthesizer(voiceManager.getVoice("mbrola_us2")).speak("Welcome John!")
        FreeTtsSpeechSynthesizer(voiceManager.getVoice("mbrola_us3")).speak("Welcome John!")
    }
}
