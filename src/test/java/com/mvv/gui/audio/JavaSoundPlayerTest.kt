package com.mvv.gui.audio

import com.mvv.gui.dictionary.getProjectDirectory
import com.mvv.gui.util.logTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Disabled
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import kotlin.io.path.readBytes


private val log = mu.KotlinLogging.logger {}


@Disabled("for manual testing")
class JavaSoundPlayerTest {
    // codec: WAV, Channels: Mono, Sample rate: 48000 Hz, Bit rate: 768 kbps
    private val difficultWavPath  = getProjectDirectory().resolve("src/test/resources/goodQualityButInterrupted.wav")
    private val difficultWav2Path = getProjectDirectory().resolve("src/test/resources/cmu-bdl-hsmm - cmu-bdl-hsmm en_US male hmm.wav")

    // codec: WAV, Channels: Mono, Sample rate: 16000 Hz, Bit rate: 256 kbps
    private val easyWavPath = getProjectDirectory().resolve("src/test/resources/badQualityButNonInterrupted.wav")

    @Test
    fun playDifficult01_byFilePath_Sync() {
        JavaSoundPlayer(PlayingMode.Sync).play(AudioSource(difficultWavPath))

        JavaSoundPlayer(PlayingMode.Sync).play(AudioSource(difficultWavPath))
        Thread.sleep(2000) // to hear artifacts of unexpected sound
    }

    @Test
    fun playDifficult02_byFilePath_Sync() {
        JavaSoundPlayer(PlayingMode.Sync).play(AudioSource(difficultWav2Path))

        JavaSoundPlayer(PlayingMode.Sync).play(AudioSource(difficultWav2Path))
        Thread.sleep(2000) // to hear artifacts of unexpected sound
    }

    @Test
    fun playEasyFile_byFilePath_Sync() {
        JavaSoundPlayer(PlayingMode.Sync).play(AudioSource(easyWavPath))

        JavaSoundPlayer(PlayingMode.Sync).play(AudioSource(easyWavPath))
        Thread.sleep(2000) // to hear artifacts of unexpected sound
    }

    @Test
    fun play_byFilePath_Sync_withReusing() {
        val player = JavaSoundPlayer(PlayingMode.Sync)
        player.play(AudioSource(difficultWavPath))

        player.play(AudioSource(difficultWavPath))
        Thread.sleep(2000) // to hear artifacts of unexpected sound

        // just to avoid 'could be private' warning
        assertThat(player.playingMode).isEqualTo(PlayingMode.Sync)
    }

    @Test
    fun play_byFilePath_Async() {
        JavaSoundPlayer(PlayingMode.Async).play(AudioSource(difficultWavPath))
        Thread.sleep(3000)

        JavaSoundPlayer(PlayingMode.Async).play(AudioSource(difficultWavPath))
        Thread.sleep(3000)
    }

    @Test
    fun play_byFilePath_Async_withReusing() {
        val player = JavaSoundPlayer(PlayingMode.Async)
        player.play(AudioSource(difficultWavPath))

        Thread.sleep(3000)

        player.play(AudioSource(difficultWavPath))
        Thread.sleep(3000) // to hear artifacts of unexpected sound
    }

    @Test
    fun play_byFilePath_Async_withSoundIntersection_expected2parallelSounds() {
        logTime(log, "after launching 1st sound") {
            JavaSoundPlayer(PlayingMode.Async).play(AudioSource(difficultWavPath))
        }

        Thread.sleep(500)

        JavaSoundPlayer(PlayingMode.Async).play(AudioSource(difficultWavPath))
        Thread.sleep(3000)
    }

    @Test
    fun play_byFilePath_Async_withReusing_withSoundIntersection_expected1stClipInterruption() {
        val player = JavaSoundPlayer(PlayingMode.Async)

        logTime(log, "after launching 1st sound") {
            player.play(AudioSource(difficultWavPath))
        }

        Thread.sleep(500)

        player.play(AudioSource(difficultWavPath))
        Thread.sleep(3000) // to hear artifacts of unexpected sound
    }

    @Test
    fun play_byUri() =
        JavaSoundPlayer(PlayingMode.Sync).play(AudioSource(difficultWavPath.toUri()))

    @Test
    fun play_fileInputStream() =
        JavaSoundPlayer(PlayingMode.Sync).play(
            AudioSource(FileInputStream(difficultWavPath.toFile())))

    @Test
    fun play_bytes() =
        JavaSoundPlayer(PlayingMode.Sync).play(AudioSource(difficultWavPath.readBytes()))

    @Test
    fun play_bytesArrayInputStream() =
        JavaSoundPlayer(PlayingMode.Sync).play(
            AudioSource(ByteArrayInputStream(difficultWavPath.readBytes())))

}
