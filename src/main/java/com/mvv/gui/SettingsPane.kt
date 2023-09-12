package com.mvv.gui

import com.mvv.gui.audio.PredefinedMarryTtsSpeechConfig
import javafx.geometry.NodeOrientation
import javafx.scene.control.*
import javafx.util.StringConverter


enum class PredefSpeechSynthesizer {
    MarryTTS,
}

data class VoiceChoice (
    val synthesizer: PredefSpeechSynthesizer,
    val voice: String,
) {
    constructor(marryTtsConf: PredefinedMarryTtsSpeechConfig) : this(PredefSpeechSynthesizer.MarryTTS, marryTtsConf.config.voice_Selections)
}


class SettingsPane : ToolBar() {

    private val splitWordCountPerFileTextField = TextField("${settings.splitWordCountPerFile}")
        .also { it.prefColumnCount = 3 }
    private val playWordOnSelectCheckBox = CheckBox("Auto play word")
        .also { it.nodeOrientation = NodeOrientation.RIGHT_TO_LEFT }
    private val voiceChoicesDropDown = ComboBox<VoiceChoice>()
        .also { it.prefWidth = 200.0; fillVoices(it) }

    init {
        items.addAll(
            Label("Split word count per file"),
            splitWordCountPerFileTextField,
            Label(" ").also { it.prefWidth = 10.0 },
            playWordOnSelectCheckBox,
            Label(" Voice"),
            voiceChoicesDropDown
        )
    }

    private fun fillVoices(voiceChoicesDropDown: ComboBox<VoiceChoice>) {
        val goodOnes = listOf(
            // This voice is really the best for sentences, but sounds of single words are extremely ugly (too low).
            //VoiceChoice(VoiceConfigs.cmu_slt_hsmm_en_US_female_hmm),
            //
            VoiceChoice(PredefinedMarryTtsSpeechConfig.cmu_rms_hsmm_en_US_male_hmm),
            VoiceChoice(PredefinedMarryTtsSpeechConfig.cmu_bdl_hsmm_en_US_male_hmm),
            VoiceChoice(PredefinedMarryTtsSpeechConfig.dfki_spike_hsmm_en_GB_male_hmm),
            VoiceChoice(PredefinedMarryTtsSpeechConfig.dfki_obadiah_hsmm_en_GB_male_hmm),
        )

        val allMarryTtsVoices = PredefinedMarryTtsSpeechConfig.values().asSequence()
            .sortedBy { it.config.voice_Selections.lowercase() }
            .filter { voiceCfg -> voiceCfg.config.locale.startsWith("en") }
            .map { voiceConf -> VoiceChoice(voiceConf) }
            .toList()

        voiceChoicesDropDown.items.setAll(allMarryTtsVoices)
        voiceChoicesDropDown.selectionModel.select(goodOnes.first())

        voiceChoicesDropDown.converter = object : StringConverter<VoiceChoice>() {
            override fun toString(value: VoiceChoice?): String {
                val prefix = if (goodOnes.contains(value)) "+" else "  "
                return if (value == null) "" else "$prefix${value.synthesizer.name} - ${value.voice}"
            }
            override fun fromString(string: String?): VoiceChoice = throw IllegalStateException("fromString should not be used.")
        }
    }

    val splitWordCountPerFile: Int get() = splitWordCountPerFileTextField.text.trim().toInt()
    val playWordOnSelect: Boolean get() = playWordOnSelectCheckBox.isSelected
    val voice: VoiceChoice get() = voiceChoicesDropDown.selectionModel.selectedItem!!
}
