package com.mvv.gui.audio

import com.mpatric.mp3agic.Mp3File
import com.mvv.gui.util.downloadUrl
import com.mvv.gui.util.timerTask
import javafx.scene.media.AudioClip
import java.nio.file.Files
import java.nio.file.Path
import java.util.Timer
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.function.Consumer
import kotlin.io.path.deleteIfExists
import kotlin.io.path.toPath


//private val log = mu.KotlinLogging.logger {}


class JavaFxSoundPlayer(val playingMode: PlayingMode) : InterruptableAudioPlayer {

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

    override fun interrupt() = stopPreviousClips()


    companion object {
        val stopperTimer: Timer by lazy { Timer("java-fx-audio-player-stopper", true) }
    }
}


private fun getClipLengthMs(audioSource: AudioSource): Long {
    return try {
        //val format = audioSourceToJavaSoundAudioFormat(audioSource)
        //log.debug("getClipLengthMs audio-format {}", format)

        javax.sound.sampled.AudioSystem.getClip().use {
            it.open(audioSourceToJavaSoundAudioInput(audioSource))
            it.microsecondLength / 1000
        }
    }
    catch (_: Exception) {
        val mp3File = mp3File(audioSource)
        mp3File.lengthInMilliseconds
    }
}

private fun mp3File(audioSource: AudioSource): Mp3File =
    when (audioSource) {
        is AudioSource.PathAudioSource  -> Mp3File(audioSource.source)
        is AudioSource.UriAudioSource   -> {
            if (audioSource.source.scheme == "file") Mp3File(audioSource.source.toPath())
            else mp3File(AudioSource(downloadUrl(audioSource.source.toString())))
        }
        is AudioSource.BytesAudioSource -> {
            // This is bad approach with saving bytes to file but now this approach (with mp3) is not used.
            val tempFile = Files.createTempFile("~temp.mp3.javafxsoundplayer~", ".mp3")
            try {
                Files.write(tempFile, audioSource.source)
                Mp3File(tempFile)
            }
            finally {
                tempFile.deleteIfExists()
            }
        }
    }


private fun writeToTempFile(bytes: ByteArray): Path {
    val file = Files.createTempFile("~javaFxSound-", ".tmp.wav")
    Files.write(file, bytes)
    return file
}
