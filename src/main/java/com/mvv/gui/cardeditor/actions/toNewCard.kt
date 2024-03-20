package com.mvv.gui.cardeditor.actions

import com.mvv.gui.words.CardWordEntry
import com.mvv.gui.words.isRussianLetter



fun CharSequence.parseToCard(): CardWordEntry? {

    val text = this.trim().removePrefix(";").removeSuffix(";").trim()

    val indexOfRussianCharOrSpecial = text.indexOfFirst { it.isRussianLetter() || it == '_' }
    if (indexOfRussianCharOrSpecial == -1 || indexOfRussianCharOrSpecial == 0) return null

    val startOfTranslationCountStatus = if (text[indexOfRussianCharOrSpecial - 1] == '(')
        indexOfRussianCharOrSpecial - 1
        else indexOfRussianCharOrSpecial

    val from = text.substring(0, startOfTranslationCountStatus).trim()
    val to = text.substring(startOfTranslationCountStatus).trim().removeSuffix(";").trim()

    return CardWordEntry(from, to)
}
