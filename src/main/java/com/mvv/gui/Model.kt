package com.mvv.gui

import javafx.beans.property.SimpleStringProperty


class CardWordEntry {
    val fromProperty = SimpleStringProperty("from")
    val toProperty = SimpleStringProperty("to")

    var from: String
        get() = fromProperty.get()
        set(value) = fromProperty.set(value)
    var to: String
        get() = toProperty.get()
        set(value) = toProperty.set(value)

    constructor(from: String, to: String) {
        this.from = from
        this.to = to
    }
}


/*
class CardWordEntryJavaFxWrapper (
    var cardWordEntry: CardWordEntry
) {

    var from: String
        get() = cardWordEntry.from
        set(value) { cardWordEntry = cardWordEntry.copy(from = value) }

    var to: String
        get() = cardWordEntry.to
        set(value) { cardWordEntry = cardWordEntry.copy(to = value) }

    fun fromProperty(): StringProperty {
        if (firstName == null) firstName = SimpleStringProperty(this, "firstName")
        return firstName
    }

}
*/


@Suppress("unused")
val cardWordEntryComparator: Comparator<CardWordEntry> = Comparator.comparing({ it.from }, String.CASE_INSENSITIVE_ORDER)
