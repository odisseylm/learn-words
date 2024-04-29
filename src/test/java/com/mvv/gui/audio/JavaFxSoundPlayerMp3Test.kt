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
class JavaFxSoundPlayerMp3Test {
    private val audioFile = getProjectDirectory().resolve("src/test/resources/door.mp3")

    @Test
    fun play_byFilePath_Sync() {
        JavaFxSoundPlayer(PlayingMode.Sync).play(AudioSource(audioFile))

        JavaFxSoundPlayer(PlayingMode.Sync).play(AudioSource(audioFile))
        Thread.sleep(2000) // to hear artifacts of unexpected sound
    }

    @Test
    fun play_byFilePath_Sync_withReusing() {
        val player = JavaFxSoundPlayer(PlayingMode.Sync)
        player.play(AudioSource(audioFile))

        player.play(AudioSource(audioFile))
        Thread.sleep(2000) // to hear artifacts of unexpected sound

        // just to avoid 'could be private' warning
        assertThat(player.playingMode).isEqualTo(PlayingMode.Sync)
    }

    @Test
    fun play_byFilePath_Async() {
        JavaFxSoundPlayer(PlayingMode.Async).play(AudioSource(audioFile))
        Thread.sleep(3000)

        JavaFxSoundPlayer(PlayingMode.Async).play(AudioSource(audioFile))
        Thread.sleep(3000)
    }

    @Test
    fun play_byFilePath_Async_withReusing() {
        val player = JavaFxSoundPlayer(PlayingMode.Async)
        player.play(AudioSource(audioFile))

        Thread.sleep(3000)

        player.play(AudioSource(audioFile))
        Thread.sleep(3000) // to hear artifacts of unexpected sound
    }

    @Test
    fun play_byFilePath_Async_withSoundIntersection_expected2parallelSounds() {
        logTime(log, "after launching 1st sound") {
            JavaFxSoundPlayer(PlayingMode.Async).play(AudioSource(audioFile))
        }

        Thread.sleep(500)

        JavaFxSoundPlayer(PlayingMode.Async).play(AudioSource(audioFile))
        Thread.sleep(3000)
    }

    @Test
    fun play_byFilePath_Async_withReusing_withSoundIntersection_expected1stClipInterruption() {
        val player = JavaFxSoundPlayer(PlayingMode.Async)

        logTime(log, "after launching 1st sound") {
            player.play(AudioSource(audioFile))
        }

        Thread.sleep(500)

        player.play(AudioSource(audioFile))
        Thread.sleep(3000) // to hear artifacts of unexpected sound
    }

    @Test
    fun play_byUri() =
        JavaFxSoundPlayer(PlayingMode.Sync).play(AudioSource(audioFile.toUri()))

    @Test
    fun play_fileInputStream() =
        JavaFxSoundPlayer(PlayingMode.Sync).play(
            AudioSource(FileInputStream(audioFile.toFile())))

    @Test
    fun play_bytes() =
        JavaFxSoundPlayer(PlayingMode.Sync).play(AudioSource(audioFile.readBytes()))

    @Test
    fun play_bytesArrayInputStream() =
        JavaFxSoundPlayer(PlayingMode.Sync).play(
            AudioSource(ByteArrayInputStream(audioFile.readBytes())))
}
