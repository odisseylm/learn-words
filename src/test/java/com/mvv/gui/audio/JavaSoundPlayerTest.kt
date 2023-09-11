package com.mvv.gui.audio

import com.mvv.gui.util.logTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Disabled
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.nio.file.Path
import kotlin.io.path.readBytes


private val log = mu.KotlinLogging.logger {}


@Disabled("for manual testing")
class JavaSoundPlayerTest {
    // codec: WAV, Channels: Mono, Sample rate: 48000 Hz, Bit rate: 768 kbps
    private val difficultWavPath = "/home/vmelnykov/projects/words/learn-words/src/test/resources/goodQualityButInterrupted.wav"

    // codec: WAV, Channels: Mono, Sample rate: 16000 Hz, Bit rate: 256 kbps
    private val easyWavPath = "/home/vmelnykov/projects/words/learn-words/src/test/resources/badQualityButNonInterrupted.wav"

    @Test
    fun play_byFilePath_Sync() {
        JavaSoundPlayer(PlayingMode.Sync).play(AudioSource(Path.of(difficultWavPath)))

        JavaSoundPlayer(PlayingMode.Sync).play(AudioSource(Path.of(difficultWavPath)))
        Thread.sleep(2000) // to hear artifacts of unexpected sound
    }

    @Test
    fun playEasyFile_byFilePath_Sync() {
        JavaSoundPlayer(PlayingMode.Sync).play(AudioSource(Path.of(easyWavPath)))

        JavaSoundPlayer(PlayingMode.Sync).play(AudioSource(Path.of(easyWavPath)))
        Thread.sleep(2000) // to hear artifacts of unexpected sound
    }

    @Test
    fun play_byFilePath_Sync_withReusing() {
        val player = JavaSoundPlayer(PlayingMode.Sync)
        player.play(AudioSource(Path.of(difficultWavPath)))

        player.play(AudioSource(Path.of(difficultWavPath)))
        Thread.sleep(2000) // to hear artifacts of unexpected sound

        // just to avoid 'could be private' warning
        assertThat(player.playingMode).isEqualTo(PlayingMode.Sync)
    }

    @Test
    fun play_byFilePath_Async() {
        JavaSoundPlayer(PlayingMode.Async).play(AudioSource(Path.of(difficultWavPath)))
        Thread.sleep(3000)

        JavaSoundPlayer(PlayingMode.Async).play(AudioSource(Path.of(difficultWavPath)))
        Thread.sleep(3000)
    }

    @Test
    fun play_byFilePath_Async_withReusing() {
        val player = JavaSoundPlayer(PlayingMode.Async)
        player.play(AudioSource(Path.of(difficultWavPath)))

        Thread.sleep(3000)

        player.play(AudioSource(Path.of(difficultWavPath)))
        Thread.sleep(3000) // to hear artifacts of unexpected sound
    }

    @Test
    fun play_byFilePath_Async_withSoundIntersection_expected2parallelSounds() {
        logTime(log, "after launching 1st sound") {
            JavaSoundPlayer(PlayingMode.Async).play(AudioSource(Path.of(difficultWavPath)))
        }

        Thread.sleep(500)

        JavaSoundPlayer(PlayingMode.Async).play(AudioSource(Path.of(difficultWavPath)))
        Thread.sleep(3000)
    }

    @Test
    fun play_byFilePath_Async_withReusing_withSoundIntersection_expected1stClipInterruption() {
        val player = JavaSoundPlayer(PlayingMode.Async)

        logTime(log, "after launching 1st sound") {
            player.play(AudioSource(Path.of(difficultWavPath)))
        }

        Thread.sleep(500)

        player.play(AudioSource(Path.of(difficultWavPath)))
        Thread.sleep(3000) // to hear artifacts of unexpected sound
    }

    @Test
    fun play_byUri() =
        JavaSoundPlayer(PlayingMode.Sync).play(AudioSource(Path.of(difficultWavPath).toUri()))

    @Test
    fun play_fileInputStream() =
        JavaSoundPlayer(PlayingMode.Sync).play(
            AudioSource(FileInputStream(difficultWavPath)))

    @Test
    fun play_bytes() =
        JavaSoundPlayer(PlayingMode.Sync).play(AudioSource(Path.of(difficultWavPath).readBytes()))

    @Test
    fun play_bytesArrayInputStream() =
        JavaSoundPlayer(PlayingMode.Sync).play(
            AudioSource(ByteArrayInputStream(Path.of(difficultWavPath).readBytes())))

}
