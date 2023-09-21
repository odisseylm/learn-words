package com.mvv.gui.audio


interface SpeechSynthesizer {
    fun speak(text: String)
}


enum class Gender { Female, Male, Neutral /*, NotSet */ }
