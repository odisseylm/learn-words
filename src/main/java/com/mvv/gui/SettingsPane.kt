package com.mvv.gui

import com.mvv.gui.audio.PredefinedMarryTtsSpeechConfig
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.collections.ObservableSet
import javafx.collections.SetChangeListener
import javafx.geometry.NodeOrientation
import javafx.scene.control.*
import javafx.util.StringConverter


enum class PredefSpeechSynthesizer {
    MarryTTS,
    Web,
}

data class VoiceChoice (
    val synthesizer: PredefSpeechSynthesizer,
    val voice: String,
) {
    constructor(marryTtsConf: PredefinedMarryTtsSpeechConfig) : this(PredefSpeechSynthesizer.MarryTTS, marryTtsConf.config.voice_Selections)
}


class SettingsPane : ToolBar() {

    internal val goodVoices: ObservableList<VoiceChoice> = FXCollections.observableArrayList()
    internal val deadVoices: ObservableSet<VoiceChoice> = FXCollections.observableSet()

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

        fun <T> refreshDropDown(comboBox: ComboBox<T>) {

            // If you know better way, please replace this peace of shi... with proper solution :-)
            val items = comboBox.items.toList()
            val selected = comboBox.selectionModel.selectedItem

            comboBox.items.clear()
            comboBox.items.setAll(items)
            comboBox.selectionModel.select(selected)
        }

        goodVoices.addListener( ListChangeListener { _ -> refreshDropDown(voiceChoicesDropDown) } )
        deadVoices.addListener( SetChangeListener  { _ -> refreshDropDown(voiceChoicesDropDown) } )
    }

    private fun fillVoices(voiceChoicesDropDown: ComboBox<VoiceChoice>) {

        val allMarryTtsVoices = PredefinedMarryTtsSpeechConfig.values().asSequence()
            .sortedBy { it.config.voice_Selections.lowercase() }
            .filter { voiceCfg -> voiceCfg.config.locale.startsWith("en") }
            .map { voiceConf -> VoiceChoice(voiceConf) }
            .toList()

        val howjsayVoice = VoiceChoice(PredefSpeechSynthesizer.Web, "howjsay.com")

        val toSelect: VoiceChoice = goodVoices.firstOrNull() ?: allMarryTtsVoices.first()

        voiceChoicesDropDown.items.add(howjsayVoice)
        voiceChoicesDropDown.items.addAll(allMarryTtsVoices)
        voiceChoicesDropDown.selectionModel.select(toSelect)

        voiceChoicesDropDown.converter = object : StringConverter<VoiceChoice>() {
            override fun toString(value: VoiceChoice?): String {
                val prefix = when (value) {
                    in deadVoices -> "-"
                    in goodVoices -> "+"
                    else -> "  "
                }
                return if (value == null) "" else "$prefix${value.synthesizer.name} - ${value.voice}"
            }
            override fun fromString(string: String?): VoiceChoice = throw IllegalStateException("fromString should not be used.")
        }
    }

    val splitWordCountPerFile: Int get() = splitWordCountPerFileTextField.text.trim().toInt()
    val playWordOnSelect: Boolean get() = playWordOnSelectCheckBox.isSelected
    val voice: VoiceChoice get() = voiceChoicesDropDown.selectionModel.selectedItem!!
}
