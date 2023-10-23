package com.mvv.gui

import com.mvv.gui.audio.PredefinedMarryTtsSpeechConfig
import com.mvv.gui.util.firstOr
import com.mvv.gui.words.SentenceEndRule
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.collections.ObservableSet
import javafx.collections.SetChangeListener
import javafx.geometry.NodeOrientation
import javafx.scene.Node
import javafx.scene.control.*
import javafx.util.StringConverter
import org.apache.commons.lang3.NotImplementedException


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
    private val sentenceEndRuleDropDown = ComboBox<SentenceEndRule>()
        .also { it.converter = SentenceEndRuleToStringConverter() }
        .also { it.items.addAll(SentenceEndRule.values()) }
        .also { it.selectionModel.select(settings.sentenceEndRule) }
    private val autoRemoveIgnoredCheckBox = CheckBox("Auto remove ignored")
        .also { it.isSelected = settings.toAutoRemoveIgnored }
        .also { it.nodeOrientation = NodeOrientation.RIGHT_TO_LEFT }
    private val warnAboutDuplicatesInOtherSetsCheckBox = CheckBox("Warn about duplicates in other sets")
        .also { it.isSelected = settings.warnAboutDuplicatesInOtherSets }
        .also { it.nodeOrientation = NodeOrientation.RIGHT_TO_LEFT }

    init {
        items.addAll(
            Label("Split word count per file"),
            splitWordCountPerFileTextField,
            stub(),
            playWordOnSelectCheckBox,
            stub(),
            Label("Voice"),
            voiceChoicesDropDown,
            stub(),
            Label("Sentence end"),
            sentenceEndRuleDropDown,
            stub(),
            autoRemoveIgnoredCheckBox,
            stub(),
            warnAboutDuplicatesInOtherSetsCheckBox,
        )

        goodVoices.addListener( ListChangeListener { _ -> refreshDropDown(voiceChoicesDropDown) } )
        deadVoices.addListener( SetChangeListener  { _ -> refreshDropDown(voiceChoicesDropDown) } )

        playWordOnSelectCheckBox.isSelected = settings.autoPlay
    }

    private fun fillVoices(voiceChoicesDropDown: ComboBox<VoiceChoice>) {

        val allMarryTtsVoices = PredefinedMarryTtsSpeechConfig.values().asSequence()
            .sortedBy { it.config.voice_Selections.lowercase() }
            .filter { voiceCfg -> voiceCfg.config.locale.startsWith("en") }
            .map { voiceConf -> VoiceChoice(voiceConf) }
            .toList()

        val howjsayVoice = VoiceChoice(PredefSpeechSynthesizer.Web, "howjsay.com")

        val toSelect: VoiceChoice = goodVoices.firstOr { allMarryTtsVoices.first() }

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

    private fun stub(width: Double = 6.0): Node = Label(" ").also { it.prefWidth = width }

    val splitWordCountPerFile: Int get() = splitWordCountPerFileTextField.text.trim().toInt()
    val playWordOnSelect: Boolean get() = playWordOnSelectCheckBox.isSelected
    val voice: VoiceChoice get() = voiceChoicesDropDown.selectionModel.selectedItem!!
    val sentenceEndRule: SentenceEndRule get() = sentenceEndRuleDropDown.selectionModel.selectedItem
    val autoRemoveIgnoredWords: Boolean get() = autoRemoveIgnoredCheckBox.isSelected
    val warnAboutDuplicatesInOtherSets: Boolean get() = warnAboutDuplicatesInOtherSetsCheckBox.isSelected
}


private fun <T> refreshDropDown(comboBox: ComboBox<T>) {

    // If you know better way, please replace this peace of shi... with proper solution :-)
    val items = comboBox.items.toList()
    val selected = comboBox.selectionModel.selectedItem

    comboBox.items.clear()
    comboBox.items.setAll(items)
    comboBox.selectionModel.select(selected)
}

private class SentenceEndRuleToStringConverter : StringConverter<SentenceEndRule>() {
    override fun toString(value: SentenceEndRule?): String = when (value) {
        null -> ""
        SentenceEndRule.ByEndingDot -> ".!?"
        SentenceEndRule.ByEndingDotOrLineBreak -> ".!? or '\\n'"
        SentenceEndRule.ByLineBreak -> "\\n"
    }

    override fun fromString(string: String?): SentenceEndRule = throw NotImplementedException()
}