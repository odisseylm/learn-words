package com.mvv.gui

import com.mvv.gui.audio.Gender
import com.mvv.gui.words.SentenceEndRule
import javafx.geometry.NodeOrientation
import javafx.scene.Node
import javafx.scene.control.*
import javafx.util.StringConverter
import org.apache.commons.lang3.NotImplementedException



class SettingsPane : ToolBar() {

    private val splitWordCountPerFileTextField = TextField("${settings.splitWordCountPerFile}")
        .also { it.prefColumnCount = 3 }
    private val playWordOnSelectCheckBox = CheckBox("Auto play word")
        .also { it.nodeOrientation = NodeOrientation.RIGHT_TO_LEFT }
    private val voiceGenderDropDown = ComboBox<Gender?>()
        .also { it.items.setAll(listOf(null) + Gender.values()) }
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
            voiceGenderDropDown,
            stub(),
            stub(),
            Label("Sentence end"),
            sentenceEndRuleDropDown,
            stub(),
            autoRemoveIgnoredCheckBox,
            stub(),
            warnAboutDuplicatesInOtherSetsCheckBox,
        )

        playWordOnSelectCheckBox.isSelected = settings.autoPlay
    }


    private fun stub(width: Double = 6.0): Node = Label(" ").also { it.prefWidth = width }

    val splitWordCountPerFile: Int get() = splitWordCountPerFileTextField.text.trim().toInt()
    var playWordOnSelect: Boolean
        get() = playWordOnSelectCheckBox.isSelected
        set(value) { playWordOnSelectCheckBox.isSelected = value }
    val playVoiceGender: Gender? get() = voiceGenderDropDown.selectionModel.selectedItem
    //val voice: VoiceChoice get() = voiceChoicesDropDown.selectionModel.selectedItem!!
    val sentenceEndRule: SentenceEndRule get() = sentenceEndRuleDropDown.selectionModel.selectedItem
    val autoRemoveIgnoredWords: Boolean get() = autoRemoveIgnoredCheckBox.isSelected
    val warnAboutDuplicatesInOtherSets: Boolean get() = warnAboutDuplicatesInOtherSetsCheckBox.isSelected
}


@Suppress("unused")
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