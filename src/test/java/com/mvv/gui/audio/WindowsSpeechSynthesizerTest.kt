package com.mvv.gui.audio

import com.mvv.gui.test.useAssertJSoftAssertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS


private val log = mu.KotlinLogging.logger {}


@EnabledOnOs(OS.WINDOWS)
class WindowsSpeechSynthesizerTest {

    @Test
    @DisplayName("extractChcpCodePageNumber")
    fun test_extractChcpCodePageNumber() { useAssertJSoftAssertions {

        // real examples
        assertThat("Active code page: 437".extractChcpCodePageNumber()).isEqualTo(437)
        assertThat("Текущая кодовая страница: 437".extractChcpCodePageNumber()).isEqualTo(437)

        assertThat("aaa: 852".extractChcpCodePageNumber()).isEqualTo(852)
        assertThat("851 852 853".extractChcpCodePageNumber()).isEqualTo(853)
    } }

    @Test
    @DisplayName("getWindowsConsoleCharset")
    fun test_getWindowsConsoleCharset() {
        assertThat(getWindowsConsoleCharsetImpl()).isNotNull
    }

    @Test
    fun predefinedVoices() {
        assertThat(WindowsVoiceManager().predefinedVoices).isNotEmpty
    }

    @Test
    fun availableVoices() {
        assertThat(WindowsVoiceManager().availableVoices).isNotEmpty
    }

    @Test
    fun speak() {
        WindowsSpeechSynthesizer(WindowsVoiceManager().predefinedVoices.first()).speak("Hello Marina!")
    }

    @Test
    fun speakAllVoices() {
        WindowsVoiceManager().predefinedVoices
            .filter { it.culture.startsWith("en") }
            .forEach {
                log.info("speak by voice ${it.name}/${it.culture} (${it.gender} ${it.age})")
                WindowsSpeechSynthesizer(it).speak("Hello Marina!")
            }
    }

    @Test
    fun speakByRussian() { // mvn test -Dtest=com.mvv.gui.audio.WindowsSpeechSynthesizerTest#speakByRussian
        val russianVoices = WindowsVoiceManager().predefinedVoices
            .filter { it.culture.startsWith("ru") }

        assertThat(russianVoices).isNotEmpty

        //val russianText = "Когда человек сознательно или интуитивно выбирает себе в жизни какую-то цель," +
        //        " жизненную задачу, он невольно дает себе оценку. По тому, ради чего человек живет," +
        //        " можно судить и о его самооценке - низкой или высокой."

        val russianText = "Привет пацаны!"

        log.info("speakByRussian() => Say [$russianText]")

        russianVoices.forEach {
            log.info("speak by voice ${it.name}/${it.culture} (${it.gender} ${it.age})")
            WindowsSpeechSynthesizer(it).speak(russianText)
        }
    }
}
