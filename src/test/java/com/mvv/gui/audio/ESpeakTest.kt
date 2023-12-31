package com.mvv.gui.audio

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test


private val log = mu.KotlinLogging.logger {}


class ESpeakTest {

    @Test
    fun parseESpeakVoice() {
        val a = SoftAssertions()

        ESpeakVoice("2  en-gb          M  english              en            (en-uk 2)(en 2)").also {
            a.assertThat(it.pty).isEqualTo(2)
            a.assertThat(it.language).isEqualTo("en-gb")
            a.assertThat(it.gender).isEqualTo(Gender.Male)
            a.assertThat(it.name).isEqualTo("english")
            a.assertThat(it.file).isEqualTo("en")
            a.assertThat(it.otherLanguages).isEqualTo("(en-uk 2)(en 2)")
        }

        ESpeakVoice(" 2  en-gb          M  english              en            (en-uk 2)(en 2)").also {
            a.assertThat(it.pty).isEqualTo(2)
            a.assertThat(it.language).isEqualTo("en-gb")
            a.assertThat(it.gender).isEqualTo(Gender.Male)
            a.assertThat(it.name).isEqualTo("english")
            a.assertThat(it.file).isEqualTo("en")
            a.assertThat(it.otherLanguages).isEqualTo("(en-uk 2)(en 2)")
        }

        ESpeakVoice(" 2  en-gb          N  english              en            (en-uk 2)(en 2)").also {
            a.assertThat(it.pty).isEqualTo(2)
            a.assertThat(it.language).isEqualTo("en-gb")
            a.assertThat(it.gender).isEqualTo(Gender.Neutral)
            a.assertThat(it.name).isEqualTo("english")
            a.assertThat(it.file).isEqualTo("en")
            a.assertThat(it.otherLanguages).isEqualTo("(en-uk 2)(en 2)")
        }

        ESpeakVoice(" 5  en             M  default              default").also {
            a.assertThat(it.pty).isEqualTo(5)
            a.assertThat(it.language).isEqualTo("en")
            a.assertThat(it.gender).isEqualTo(Gender.Male)
            a.assertThat(it.name).isEqualTo("default")
            a.assertThat(it.file).isEqualTo("default")
            a.assertThat(it.otherLanguages).isNull()
        }

        ESpeakVoice(" 5  ru             M  russian              europe/ru").also {
            a.assertThat(it.pty).isEqualTo(5)
            a.assertThat(it.language).isEqualTo("ru")
            a.assertThat(it.gender).isEqualTo(Gender.Male)
            a.assertThat(it.name).isEqualTo("russian")
            a.assertThat(it.file).isEqualTo("europe/ru")
            a.assertThat(it.otherLanguages).isNull()
        }

        ESpeakVoice(" 5  bg             -  bulgarian            europe/bg     ").also {
            a.assertThat(it.pty).isEqualTo(5)
            a.assertThat(it.language).isEqualTo("bg")
            a.assertThat(it.gender).isNull()
            a.assertThat(it.name).isEqualTo("bulgarian")
            a.assertThat(it.file).isEqualTo("europe/bg")
            a.assertThat(it.otherLanguages).isNull()
        }

        ESpeakVoice(" 5  en-uk-rp       M  english_rp           other/en-rp   (en-uk 4)(en 5)").also {
            a.assertThat(it.pty).isEqualTo(5)
            a.assertThat(it.language).isEqualTo("en-uk-rp")
            a.assertThat(it.gender).isEqualTo(Gender.Male)
            a.assertThat(it.name).isEqualTo("english_rp")
            a.assertThat(it.file).isEqualTo("other/en-rp")
            a.assertThat(it.otherLanguages).isEqualTo("(en-uk 4)(en 5)")
        }

        ESpeakVoice(" \t 5 \t en-uk-rp \t  F  english_rp \t     other/en-rp \t (en-uk 4)(en 5)").also {
            a.assertThat(it.pty).isEqualTo(5)
            a.assertThat(it.language).isEqualTo("en-uk-rp")
            a.assertThat(it.gender).isEqualTo(Gender.Female)
            a.assertThat(it.name).isEqualTo("english_rp")
            a.assertThat(it.file).isEqualTo("other/en-rp")
            a.assertThat(it.otherLanguages).isEqualTo("(en-uk 4)(en 5)")
        }

        ESpeakVoice("\t5\ten-uk-rp\tf\tenglish_rp\tother/en-rp\t(en-uk\t4)(en\t5)").also {
            a.assertThat(it.pty).isEqualTo(5)
            a.assertThat(it.language).isEqualTo("en-uk-rp")
            a.assertThat(it.gender).isEqualTo(Gender.Female)
            a.assertThat(it.name).isEqualTo("english_rp")
            a.assertThat(it.file).isEqualTo("other/en-rp")
            a.assertThat(it.otherLanguages).isEqualTo("(en-uk\t4)(en\t5)")
        }

        a.assertAll()
    }


    @Test
    fun predefinedVoices() {
        assertThat(ESpeakVoiceManager().predefinedVoices).isNotEmpty
    }


    @Test
    @Disabled("for manual testing")
    fun allVoices() {
        assertThat(ESpeakVoiceManager().availableVoices).isNotEmpty
    }


    @Test
    @Disabled("for manual testing")
    fun play() {
        val voiceManager = ESpeakVoiceManager()

        val someVoice = voiceManager.predefinedVoices.first()
        ESpeakSpeechSynthesizer(someVoice).speak("Hello Marina!")
        ESpeakSpeechSynthesizer(someVoice).speak("Hello John!")

        val someMbrolaVoiceCustomizedToBeSlow = voiceManager.predefinedVoices
            .find { it.name == "us-mbrola-2" }
            ?.copy(pitch = 15, amplitude = 200, wordsPerMinute = 120) // , wordGap = 50)

        assertThat(someMbrolaVoiceCustomizedToBeSlow).describedAs("Mbrola voice is not found").isNotNull

        ESpeakSpeechSynthesizer(someMbrolaVoiceCustomizedToBeSlow!!).speak("Hello John!")
    }


    @Test
    @Disabled("for manual testing")
    fun play_usingMbrolaVoice() {
        val mbrolaEnVoices = fixedMbrolaVoiceDefinitions.values
        mbrolaEnVoices.forEach { voice ->
            ESpeakSpeechSynthesizer(voice).speak("Hello Marina!")
            ESpeakSpeechSynthesizer(voice).speak("Hello John!")
            ESpeakSpeechSynthesizer(voice).speak("6 new wireless networks found")
        }
    }


    @Test
    @Disabled("for manual testing")
    fun play_usingMbrolaVoices() {
        val mbrolaVoices = ESpeakVoiceManager().availableVoices
            .filter { it.file.startsWith("mb/") }
            //.filter { it.name == "en-german-5" }
            //.filter { it.name.contains("french") }
            //.filter { it.name.contains("english-mb-en1") }
        assertThat(mbrolaVoices).describedAs("No mbrola voices.").isNotEmpty

        mbrolaVoices.forEach { voice ->
            log.info("Voice: {}", voice.name)
            ESpeakSpeechSynthesizer(voice).speak("Hello Marina!")
            ESpeakSpeechSynthesizer(voice).speak("Hello John!")
        }
    }


    @Test
    @Disabled("for manual testing")
    fun play_usingCoreUsMbrolaVoices() {
        val mbrolaVoices = ESpeakVoiceManager().availableVoices
            .filter { it.file.startsWith("mb/") }
            .filter { it.name.startsWith("us-mbrola-") || it.name == "english-mb-en1" }
        assertThat(mbrolaVoices).describedAs("No mbrola core US voices.").isNotEmpty

        mbrolaVoices.forEach { voice ->
            log.info("Voice: {}", voice.name)
            ESpeakSpeechSynthesizer(voice).speak("Hello Marina!")
            ESpeakSpeechSynthesizer(voice).speak("Hello John!")
        }
    }
}
