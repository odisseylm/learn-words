package com.mvv.gui.audio

import com.mvv.gui.util.timerTask
import java.io.ByteArrayInputStream
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.function.Consumer
import javax.sound.sampled.*


private val log = mu.KotlinLogging.logger {}

class JavaSoundPlayer (val playingMode: PlayingMode) : AudioPlayer /*, AutoCloseable*/ {

    // Actually there is absolutely not important which type of synchronized list is used
    // (you can replace it by java.util.Collections.synchronizedList() or Vector)
    private val clips = CopyOnWriteArrayList<Clip>()

    private val cleanUpListener = LineListener { event ->
        when (event.type) {
            LineEvent.Type.STOP  -> event.line.close()
            LineEvent.Type.CLOSE -> clips.remove(event.line as Clip)
        }
    }

    override fun play(audioSource: AudioSource) = playImpl(audioSourceToJavaSoundAudioInput(audioSource))

    private fun playImpl(audioStream: AudioInputStream) {

        val syncLatch = CountDownLatch(1)

        // Reusing 'clip' may not work - depends on OS/hardware
        // for that reason we always create new Clip
        //
        val clip = AudioSystem.getClip()
        clip.addLineListener(logListener)
        clip.addLineListener(cleanUpListener)

        clip.addLineListener { event -> if (event.type == LineEvent.Type.STOP) syncLatch.countDown() }

        stopPreviousClips()
        clips.add(clip)

        clip.open(audioStream)

        // I cannot understand where it works properly using just Clip.start()
        //if (audioStream.format.sampleRate <= 16000.0)
        //    clip.start()
        //else {
            // JavaSound bug!
            // Standard approach does not work for such 'wav' files: Sample rate: 48000 Hz, Bit rate: 768 kbps
            playByLoopAndManualStopOnSecondPlaying(clip)
            //playByLoopPoints(clip)
        //}

        if (playingMode == PlayingMode.Sync)
            syncLatch.await()
    }

    private fun stopPreviousClips() =
        // !!! thread-safe java forEach !!!
        clips.forEach(Consumer { it.stop(); it.close() })

    // The public official standard Clip.start() approach does not work for audio some audios.
    //  * codec: WAV, Channels: Mono, Sample rate: 48000 Hz, Bit rate: 768 kbps
    //  * and some others...
    //
    // Reliable workaround but if CPU is busy user can hear beginning of audio clip again.
    private fun playByLoopAndManualStopOnSecondPlaying(clip: Clip) {
        val lengthMs = clip.microsecondLength / 1000
        clip.loop(1)
        when (playingMode) {
            PlayingMode.Sync  -> {
                Thread.sleep(lengthMs)
                clip.stop()
            }
            PlayingMode.Async -> { stopperTimer.schedule(timerTask { clip.stop() }, lengthMs) }
        }
    }

    // The public official standard Clip.start() approach does not work for audio some audios.
    //  * codec: WAV, Channels: Mono, Sample rate: 48000 Hz, Bit rate: 768 kbps
    //  * and some others...
    //
    // This simple workaround not reliable... Clip can be interrupted in 0.5 sec.
    @Suppress("unused")
    private fun playByLoopPoints(clip: Clip) {
        clip.setLoopPoints((clip.frameLength / 500) * 500, clip.frameLength - 1)
        clip.loop(1)
    }

    companion object {
        val stopperTimer: Timer by lazy { Timer("java-sound-audio-player-stopper", true) }
    }
}


fun audioSourceToJavaSoundAudioInput(source: AudioSource): AudioInputStream = when (source) {
    is AudioSource.BytesAudioSource -> AudioSystem.getAudioInputStream(ByteArrayInputStream(source.source))
    is AudioSource.PathAudioSource  -> AudioSystem.getAudioInputStream(source.source.toFile())
    is AudioSource.UriAudioSource   -> AudioSystem.getAudioInputStream(source.source.toURL())
}


private val logListener: LineListener = object : LineListener {
    @Volatile var started = 0L
    override fun update(event: LineEvent) =
        when (event.type) {
            LineEvent.Type.OPEN  -> log.debug {  "${event.type}" }
            LineEvent.Type.CLOSE -> log.debug {  "${event.type}" }
            LineEvent.Type.START -> {
                val clip = event.source as Clip
                log.debug { "${event.type}" +
                        ", microsecondLength: ${clip.microsecondLength}" +
                        ", bufferSize: ${clip.bufferSize}" +
                        ", frameLength: ${clip.frameLength}"
                }
                started = System.currentTimeMillis()
            }
            LineEvent.Type.STOP  -> {
                val playedMs = System.currentTimeMillis() - started
                log.debug { "${event.type}, stopped after ${playedMs}ms" }
            }
            else -> log.debug { "${event.type} $event" }
        }
}
