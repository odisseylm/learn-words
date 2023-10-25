package com.mvv.gui.audio

import com.mvv.gui.util.executeCommand
import com.sun.speech.freetts.VoiceDirectory
import com.sun.speech.freetts.VoiceManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIf
import java.io.IOException



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


    @Test
    fun getMbrolaVoices() {
        System.setProperty("mbrola.base", findMbrolaBaseDir().toString())

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

        val accessibleVoices = MbrolaVoiceDirectory(findMbrolaBaseDir()).voices
        assertThat(accessibleVoices).isNotEmpty

        //System.setProperty("freetts.voices", MbrolaVoiceDirectory::class.java.name)
        //assertThat(VoiceManager.getInstance().voices).isNotEmpty
    }


    @Test
    fun getBaseVoicesInsideAll() {
        System.setProperty("mbrola.base", findMbrolaBaseDir().toString())

        val accessibleVoices = MbrolaVoiceDirectory().voices
        assertThat(accessibleVoices).isNotEmpty

        val voiceDirectoryClasses: List<Class<out VoiceDirectory>> = listOf(
            com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory::class.java,
            com.sun.speech.freetts.en.us.cmu_time_awb.AlanVoiceDirectory::class.java,
            MbrolaVoiceDirectory::class.java,
        )
        System.setProperty("freetts.voices", voiceDirectoryClasses.joinToString(",") { it.name })

        assertThat(VoiceManager.getInstance().voices.map { it.name }).contains(
            "kevin",
            "kevin16",
            "alan",
        )
    }


    @Test
    @EnabledIf("isMbrolaExecutablePresent")
    fun getMbrolaVoicesInsideAll() {
        System.setProperty("mbrola.base", findMbrolaBaseDir().toString())

        val accessibleVoices = MbrolaVoiceDirectory().voices
        assertThat(accessibleVoices).isNotEmpty

        val voiceDirectoryClasses: List<Class<out VoiceDirectory>> = listOf(
            com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory::class.java,
            com.sun.speech.freetts.en.us.cmu_time_awb.AlanVoiceDirectory::class.java,
            MbrolaVoiceDirectory::class.java,
        )
        System.setProperty("freetts.voices", voiceDirectoryClasses.joinToString(",") { it.name })

        assertThat(VoiceManager.getInstance().voices.map { it.name }).contains(
            "mbrola_en1",
            "mbrola_us1",
            "mbrola_us2",
            "mbrola_us3",
        )
    }


    @Test
    fun playStandardVoicesBy_JavaSpeechSpeechSynthesizer() {
        System.clearProperty("mbrola.base")
        System.clearProperty("freetts.voices")

        initFreeTts()
        val voiceManager = VoiceManager.getInstance()

        // using alt constructor
        voiceManager.getVoice("kevin")
            .let { JavaSpeechSpeechSynthesizer(it.toJavaxSpeechVoice(), it.locale) }
            .speak("Welcome John!")

        JavaSpeechSpeechSynthesizer(voiceManager.getVoice("kevin"))
            .speak("Welcome John!")
        JavaSpeechSpeechSynthesizer(voiceManager.getVoice("kevin16"))
            .speak("Welcome John!")

        // 'alan' does not work
        JavaSpeechSpeechSynthesizer(voiceManager.getVoice("alan"))
            .speak("Welcome John!")
    }


    @Test
    @EnabledIf("isMbrolaExecutablePresent")
    fun playMbrolaVoicesBy_JavaSpeechSpeechSynthesizer() {
        System.clearProperty("mbrola.base")
        System.clearProperty("freetts.voices")

        initFreeTts()
        val voiceManager = VoiceManager.getInstance()

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
    @EnabledIf("isMbrolaExecutablePresent")
    fun playMbrolaVoicesBy_sharingTheSameInstance_JavaSpeechSpeechSynthesizer() {
        System.clearProperty("mbrola.base")
        System.clearProperty("freetts.voices")

        initFreeTts()
        val voiceManager = VoiceManager.getInstance()

        // Or does not work good with java FreeTTS or need to configure properly.
        //JavaSpeechSpeechSynthesizer(voiceManager.getVoice("mbrola_en1"))
        //    .speak("Welcome John!")

        val javaSpeechSpeechSynthesizer = JavaSpeechSpeechSynthesizer(voiceManager.getVoice("mbrola_us2"))
        javaSpeechSpeechSynthesizer.speak("Welcome John!")
        javaSpeechSpeechSynthesizer.speak("Welcome John!")
        javaSpeechSpeechSynthesizer.speak("Welcome John!")
    }


    @Test
    fun playBaseVoicesBy_FreeTtsSpeechSynthesizer() {
        System.clearProperty("mbrola.base")
        System.clearProperty("freetts.voices")

        initFreeTts()
        val voiceManager = VoiceManager.getInstance()

        FreeTtsSpeechSynthesizer(voiceManager.getVoice("kevin")).speak("Welcome John!")
        FreeTtsSpeechSynthesizer(voiceManager.getVoice("kevin16")).speak("Welcome John!")

        // seems 'alan' quietly does not work
        //FreeTtsSpeechSynthesizer(voiceManager.getVoice("alan")).speak("Welcome John!")
    }


    @Test
    @EnabledIf("isMbrolaExecutablePresent")
    fun playMbrolaVoicesBy_FreeTtsSpeechSynthesizer() {
        System.clearProperty("mbrola.base")
        System.clearProperty("freetts.voices")

        initFreeTts()
        val voiceManager = VoiceManager.getInstance()

        // Or does not work good with java FreeTTS or need to configure properly.
        //FreeTtsSpeechSynthesizer(voiceManager.getVoice("mbrola_en1")).speak("Welcome John!")

        FreeTtsSpeechSynthesizer(voiceManager.getVoice("mbrola_us1")).speak("Welcome John!")
        FreeTtsSpeechSynthesizer(voiceManager.getVoice("mbrola_us2")).speak("Welcome John!")
        FreeTtsSpeechSynthesizer(voiceManager.getVoice("mbrola_us3")).speak("Welcome John!")
    }


    @Test
    @EnabledIf("isMbrolaExecutablePresent")
    fun playMbrolaVoicesBy_repeatablyUsingNewInstanceEveryTime_FreeTtsSpeechSynthesizer() {
        System.clearProperty("mbrola.base")
        System.clearProperty("freetts.voices")

        initFreeTts()
        val voiceManager = VoiceManager.getInstance()

        // Or does not work good with java FreeTTS or need to configure properly.
        //FreeTtsSpeechSynthesizer(voiceManager.getVoice("mbrola_en1")).speak("Welcome John!")

        FreeTtsSpeechSynthesizer(voiceManager.getVoice("mbrola_us1")).speak("Welcome John!")
        FreeTtsSpeechSynthesizer(voiceManager.getVoice("mbrola_us1")).speak("Welcome John!")
        FreeTtsSpeechSynthesizer(voiceManager.getVoice("mbrola_us1")).speak("Welcome John!")
    }


    // T O D O: fails
    @Test
    @EnabledIf("isMbrolaExecutablePresent")
    fun playMbrolaVoicesBy_repeatablySharingTheSameSynthesizerInstance_FreeTtsSpeechSynthesizer() {
        System.clearProperty("mbrola.base")
        System.clearProperty("freetts.voices")

        initFreeTts()
        val voiceManager = VoiceManager.getInstance()

        // Or does not work good with java FreeTTS or need to configure properly.
        //FreeTtsSpeechSynthesizer(voiceManager.getVoice("mbrola_en1")).speak("Welcome John!")

        val voice = voiceManager.getVoice("mbrola_us1")
        FreeTtsSpeechSynthesizer(voice).speak("Welcome John!")
        FreeTtsSpeechSynthesizer(voice).speak("Welcome John!")
        FreeTtsSpeechSynthesizer(voice).speak("Welcome John!")
    }


    // T O D O: fails
    @Test
    @EnabledIf("isMbrolaExecutablePresent")
    fun playMbrolaVoicesBy_repeatablySharingTheSameVoiceInstance_FreeTtsSpeechSynthesizer() {
        System.clearProperty("mbrola.base")
        System.clearProperty("freetts.voices")

        initFreeTts()
        val voiceManager = VoiceManager.getInstance()

        // Or does not work good with java FreeTTS or need to configure properly.
        //FreeTtsSpeechSynthesizer(voiceManager.getVoice("mbrola_en1")).speak("Welcome John!")

        val freeTtsSpeechSynthesizer = FreeTtsSpeechSynthesizer(voiceManager.getVoice("mbrola_us1"))
        freeTtsSpeechSynthesizer.speak("Welcome John!")
        freeTtsSpeechSynthesizer.speak("Welcome John!")
        freeTtsSpeechSynthesizer.speak("Welcome John!")
    }

    companion object {
        @JvmStatic
        fun isMbrolaExecutablePresent(): Boolean =
                try { executeCommand(findMbrolaBinary(), "-help") == 0 }
                catch (_: IOException) { false }
    }
}
