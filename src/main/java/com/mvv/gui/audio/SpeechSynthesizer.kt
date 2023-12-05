package com.mvv.gui.audio



interface Voice {
    // val name: String // I do not use 'name' since I do not know wht should be contained in it for different TTS.
    val shortDescription: String // it is not unique
    val gender: Gender?
}


internal data class SimpleVoice (
    override val shortDescription: String,
    override val gender: Gender?,
) : Voice

interface SpeechSynthesizer {
    @Throws(SpeechSynthesizerException::class)
    fun speak(text: String)

    /** Optional. Not implemnted/supported by default. */
    fun interrupt() { }

    fun isSupported(text: String): Boolean = true
    @Throws(SpeechSynthesizerException::class)
    fun validateSupport(text: String) {
        if (!isSupported(text)) throw SpeechSynthesizerException(
            "SpeechSynthesizer ${this.javaClass.simpleName} does not support text [$text].")
    }

    val voice: Voice
    val shortDescription: String
    val isAvailable: Boolean
}


enum class Gender { Female, Male, Neutral /*, NotSet */ }


open class SpeechSynthesizerException(message: String, cause: Throwable? = null) : RuntimeException(message) {
    init { cause?.let { initCause(cause) } }
}

/** If text language is not supported. Some implementations can even hang up in such case. */
@Suppress("unused")
class LanguageIsNotSupportedException(message: String, cause: Throwable? = null) : SpeechSynthesizerException(message, cause)

/** For synthesizers which supports only separate words.
  * For example if only stored audio files are used (or is words dictionary like AbbyLingvo).
  */
class ExpressionIsNotSupportedException(message: String = "Only single words are supported.", cause: Throwable? = null) : SpeechSynthesizerException(message, cause)


