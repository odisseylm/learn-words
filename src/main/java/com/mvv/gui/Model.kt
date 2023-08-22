package com.mvv.gui

import com.sun.javafx.binding.ExpressionHelper
import javafx.beans.InvalidationListener
import javafx.beans.property.ReadOnlyProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.paint.Color


class CardWordEntry {
    val fromProperty = SimpleStringProperty(this, "from", "")
    val toProperty = SimpleStringProperty(this, "to", "")
    val transcriptionProperty = SimpleStringProperty(this, "transcription", "")
    val translationCountProperty = AroundReadOnlyIntegerProperty<String>(this, "translationCount", toProperty) {
        to -> to?.translationCount ?: 0 }
    val examplesProperty = SimpleStringProperty(this, "examples", "")

    var from: String
        get() = fromProperty.get()
        set(value) = fromProperty.set(value)
    var to: String
        get() = toProperty.get()
        set(value) = toProperty.set(value)
    var transcription: String
        get() = transcriptionProperty.get()
        set(value) = transcriptionProperty.set(value)
    var examples: String
        get() = examplesProperty.get()
        set(value) = examplesProperty.set(value)

    @Suppress("MemberVisibilityCanBePrivate")
    var translationCount: Int = 0
        private set

    constructor(from: String, to: String) {
        toProperty.addListener { _, _, newValue -> translationCount = newValue?.translationCount ?: 0 }

        this.from = from
        this.to = to
    }

    override fun toString(): String {
        return "CardWordEntry(from='$from', to='${to.take(20)}...', translationCount=$translationCount, transcription='$transcription', examples='${examples.take(10)}...')"
    }
}


@Suppress("unused")
val cardWordEntryComparator: Comparator<CardWordEntry> = Comparator.comparing({ it.from }, String.CASE_INSENSITIVE_ORDER)



enum class TranslationCountStatus(val color: Color) {
    Ok(Color.TRANSPARENT),
    NotBad(Color.valueOf("#fffac5")),
    Warn(Color.valueOf("#ffdcc0")),
    ToMany(Color.valueOf("#ffbbbb")),
    ;
}

val Int.toTranslationCountStatus: TranslationCountStatus get() = when (this) {
    in 0..3 -> TranslationCountStatus.Ok
    in 4..5 -> TranslationCountStatus.NotBad
    in 6..7 -> TranslationCountStatus.Warn
    else -> TranslationCountStatus.ToMany
}


class AroundReadOnlyIntegerProperty<OtherPropertyType>(
    private val bean: Any,
    private val name: String,
    private val baseProperty: ObservableValue<OtherPropertyType>,
    val convertFunction: (OtherPropertyType?)->Int,
    ) : ReadOnlyProperty<Int>, ObservableValue<Int> {

    private var helper: ExpressionHelper<Int>? = null

    init {
        baseProperty.addListener { _, _, _ -> fireValueChangedEvent() } // <= Do I need this??
        baseProperty.addListener { _ -> fireValueChangedEvent() }
    }

    override fun addListener(listener: InvalidationListener) {
        helper = ExpressionHelper.addListener(helper, this, listener)
    }

    override fun removeListener(listener: InvalidationListener) {
        helper = ExpressionHelper.removeListener(helper, listener)
    }

    override fun getValue(): Int = convertFunction(baseProperty.value)

    override fun addListener(listener: ChangeListener<in Int>) {
        helper = ExpressionHelper.addListener(helper, this, listener)
    }

    override fun removeListener(listener: ChangeListener<in Int>) {
        helper = ExpressionHelper.removeListener(helper, listener)
    }

    private fun fireValueChangedEvent() = ExpressionHelper.fireValueChangedEvent(helper)

    override fun getBean(): Any = bean

    override fun getName(): String = name
}
