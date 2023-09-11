package com.mvv.gui.audio

import com.mvv.gui.util.timerTask
import javafx.scene.media.AudioClip
import java.nio.file.Files
import java.nio.file.Path
import java.util.Timer
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.function.Consumer


class JavaFxSoundPlayer(val playingMode: PlayingMode) : AudioPlayer {

    // Actually there is absolutely not important which type of synchronized list is used
    // (you can replace it by java.util.Collections.synchronizedList() or Vector)
    private val clips = CopyOnWriteArrayList<AudioClip>()

    override fun play(audioSource: AudioSource) =
        when (audioSource) {
            is AudioSource.PathAudioSource -> playImpl(audioSource.source.toUri().toString(), audioSource)
            is AudioSource.BytesAudioSource -> {
                val tempFile = writeToTempFile(audioSource.source)

                playImpl(tempFile.toUri().toString(), audioSource) { Files.deleteIfExists(tempFile) }
            }
            is AudioSource.UriAudioSource -> playImpl(audioSource.source.toString(), audioSource)
        }

    private fun playImpl(sourceStr: String, audioSource: AudioSource, stopAction: ()->Unit = { }) {

        val syncLatch = CountDownLatch(1)

        val clip = AudioClip(sourceStr)

        stopperTimer.schedule(
            timerTask {
                syncLatch.countDown()
                clips.remove(clip)
                stopAction()
            },
            getClipLengthMs(audioSource),
        )

        stopPreviousClips()
        clips.add(clip)

        clip.play()

        if (playingMode == PlayingMode.Sync)
            // since there are no onStop events we cannot wait using proper approach
            syncLatch.await()
    }

    private fun stopPreviousClips() =
        // !!! thread-safe java forEach !!!
        clips.forEach(Consumer { it.stop() })


    companion object {
        val stopperTimer: Timer by lazy { Timer("java-fx-audio-player-stopper", true) }
    }
}


private fun getClipLengthMs(audioSource: AudioSource): Long =
    javax.sound.sampled.AudioSystem.getClip().use {
        it.open(audioSourceToJavaSoundAudioInput(audioSource))
        it.microsecondLength / 1000
    }

private fun writeToTempFile(bytes: ByteArray): Path {
    val file = Files.createTempFile("~javaFxSound-", ".tmp.wav")
    Files.write(file, bytes)
    return file
}
