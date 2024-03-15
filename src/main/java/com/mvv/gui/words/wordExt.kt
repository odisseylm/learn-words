package com.mvv.gui.words


var CardWordEntry.from: String
    get()      = fromProperty.valueSafe
    set(value) = fromProperty.set(value)
var CardWordEntry.fromWithPreposition: String
    get()      = fromWithPrepositionProperty.valueSafe
    set(value) = fromWithPrepositionProperty.set(value)
val CardWordEntry.fromWordCount: Int
    get() = fromWordCountProperty.value
val CardWordEntry.baseWordOfFrom: String
    get() = baseWordOfFromProperty.value ?: ""
var CardWordEntry.to: String
    get()      = toProperty.get()
    set(value) = toProperty.set(value)
var CardWordEntry.transcription: String
    get()      = transcriptionProperty.get()
    set(value) = transcriptionProperty.set(value)
var CardWordEntry.examples: String
    get()      = examplesProperty.get()
    set(value) = examplesProperty.set(value)
val CardWordEntry.exampleNewCardCandidateCount: Int get() = exampleNewCardCandidateCountProperty.value

val CardWordEntry.translationCount: Int
    get() = translationCountProperty.value

var CardWordEntry.statuses: Set<WordCardStatus>
    get()      = statusesProperty.get()
    set(value) = statusesProperty.set(value)

var CardWordEntry.predefinedSets: Set<PredefinedSet>
    get()      = predefinedSetsProperty.get()
    set(value) = predefinedSetsProperty.set(value)

var CardWordEntry.sourcePositions: List<Int>
    get()      = sourcePositionsProperty.get()
    set(value) = sourcePositionsProperty.set(value)

var CardWordEntry.sourceSentences: String
    get()      = sourceSentencesProperty.get()
    set(value) = sourceSentencesProperty.set(value)

var CardWordEntry.missedBaseWords: List<String>
    get()      = missedBaseWordsProperty.get()
    set(value) = missedBaseWordsProperty.set(value)
