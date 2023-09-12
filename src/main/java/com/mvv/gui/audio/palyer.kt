package com.mvv.gui.audio

import java.io.InputStream
import java.net.URI
import java.nio.file.Path



sealed class AudioSource {
    data class PathAudioSource (val source: Path) : AudioSource()
    data class UriAudioSource (val source: URI) : AudioSource()
    class BytesAudioSource (val source: ByteArray) : AudioSource() {
        override fun toString(): String = "BytesAudioSource { ${source.size} bytes }"
    }

    companion object {
        operator fun invoke(source: Path): PathAudioSource = PathAudioSource(source)
        operator fun invoke(source: URI): UriAudioSource = UriAudioSource(source)
        operator fun invoke(source: ByteArray): BytesAudioSource = BytesAudioSource(source)
        // Do not pass big file as stream!!! Please, pass a file directly (as Path)!
        operator fun invoke(source: InputStream): BytesAudioSource = BytesAudioSource(source.use { it.readAllBytes() })
    }
}


enum class PlayingMode {
    Sync,
    Async,
}


interface AudioPlayer {
    fun play(audioSource: AudioSource)
}
