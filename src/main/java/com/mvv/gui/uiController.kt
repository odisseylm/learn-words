package com.mvv.gui

import com.mvv.gui.audio.*
import com.mvv.gui.dictionary.AutoDictionariesLoader
import com.mvv.gui.dictionary.CachedDictionary
import com.mvv.gui.dictionary.Dictionary
import com.mvv.gui.dictionary.DictionaryComposition
import com.mvv.gui.javafx.*
import com.mvv.gui.util.*
import com.mvv.gui.words.*
import com.mvv.gui.words.WordCardStatus.NoBaseWordInSet
import javafx.application.Platform
import javafx.beans.property.ReadOnlyProperty
import javafx.beans.property.StringPropertyBase
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.collections.transformation.SortedList
import javafx.event.EventHandler
import javafx.geometry.Point2D
import javafx.scene.control.*
import javafx.scene.input.Clipboard
import javafx.scene.input.MouseEvent
import javafx.stage.FileChooser
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.commons.text.StringEscapeUtils.escapeHtml4
import java.io.File
import java.io.Reader
import java.io.StringReader
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.io.path.*


private val log = mu.KotlinLogging.logger {}


class LearnWordsController (val isReadOnly: Boolean = false) {

    //private val projectDirectory = getProjectDirectory(this.javaClass)

    private val _allDictionaries: List<Dictionary> = if (isReadOnly) emptyList()
                                                     else AutoDictionariesLoader().load() // HardcodedDictionariesLoader().load()
    internal val dictionary = CachedDictionary(DictionaryComposition(_allDictionaries))

    internal val navigationHistory = NavigationHistory()

    internal val pane = MainWordsPane(this)

    internal val currentWordsList: WordCardsTable get() = pane.wordEntriesTable
    private val currentWords: ObservableList<CardWordEntry> get() = currentWordsList.items

    private var documentIsDirty: Boolean = false

    private val ignoredWords: ObservableList<String> = FXCollections.observableArrayList()
    private val ignoredWordsSorted: ObservableList<String> = SortedList(ignoredWords, String.CASE_INSENSITIVE_ORDER)

    private val allProcessedWords: ObservableList<String> = FXCollections.observableArrayList()

    private var currentWordsFile: Path? = null

    private val toolBarController2 = ToolBarControllerBig(this)
    private val settingsPane = SettingsPane()
    private val toolBar2 = ToolBar2(this).also {
        it.nextPrevWarningWord.addSelectedWarningsChangeListener { _, _, _ -> recalculateWarnedWordsCount() } }

    val cellEditorStates = WeakHashMap<Pair<TableColumn<CardWordEntry,*>, CardWordEntry>, CellEditorState>()

    private val allWordCardSetsManager = AllWordCardSetsManager()
    // we have to use lazy because creating popup before creating/showing main windows causes JavaFX hanging up :-)
    private val otherCardsViewPopup: OtherCardsViewPopup by lazy { OtherCardsViewPopup() }

    init {
        Timer("updateSpeechSynthesizersAvailabilityTimer", true)
            .also { it.schedule(timerTask { updateSpeechSynthesizerAvailability() }, 15000, 15000) }
    }

    init {
        pane.initThemeAndStyles()

        log.info("Used dictionaries\n" +
                _allDictionaries.mapIndexed { i, d -> "${i + 1} $d" }.joinToString("\n") +
                "\n---------------------------------------------------\n\n"
        )


        val currentWordsLabelText = "File/Clipboard (%d words)"
        currentWords.addListener(ListChangeListener { pane.wordEntriesLabel.text = currentWordsLabelText.format(it.list.size) })

        currentWords.addListener(ListChangeListener { markDocumentIsDirty(); reanalyzeAllWords() })

        val ignoredWordsLabelText = "Ignored words (%d)"
        ignoredWords.addListener(ListChangeListener { pane.ignoredWordsLabel.text = ignoredWordsLabelText.format(it.list.size) })


        pane.ignoredWordsList.items = ignoredWordsSorted

        val allProcessedWordsLabelText = "All processed words (%d)"
        allProcessedWords.addListener(ListChangeListener { pane.allProcessedWordsLabel.text = allProcessedWordsLabelText.format(it.list.size) })

        pane.allProcessedWordsList.items = SortedList(allProcessedWords, String.CASE_INSENSITIVE_ORDER)


        addGlobalKeyBindings(currentWordsList, copyKeyCombinations.associateWith { {
            if (!currentWordsList.isEditing) copySelectedWord() } })

        addKeyBindings()

        pane.warnAboutMissedBaseWordsModeDropDown.onAction = EventHandler { reanalyzeAllWords() }

        pane.topPane.children.add(0, MenuController(this).fillMenu())
        pane.topPane.children.add(toolBarController2.toolBar)
        pane.topPane.children.add(settingsPane)
        pane.topPane.children.add(toolBar2)

        val contextMenuController = ContextMenuController(this)
        currentWordsList.contextMenu = contextMenuController.contextMenu
        currentWordsList.onContextMenuRequested = EventHandler { contextMenuController.updateItemsVisibility() }

        currentWordsList.selectionModel.selectedItemProperty().addListener { _, _, _ ->
            Platform.runLater { if (toPlayWordOnSelect) playSelectedWord() } }

        currentWordsList.addEventHandler(MouseEvent.MOUSE_CLICKED) { ev ->
            val card = currentWordsSelection.selectedItem
            val tableColumn = currentWordsSelection.selectedCells?.getOrNull(0)?.tableColumn

            if (ev.clickCount >= 2 && card != null && tableColumn.isOneOf(currentWordsList.sourceSentencesColumn, currentWordsList.numberColumn))
                showSourceSentences(card)
        }

        settingsPane.goodVoices.setAll(
            // This voice is really the best for sentences, but sounds of single words are extremely ugly (too low).
            //VoiceChoice(VoiceConfigs.cmu_slt_hsmm_en_US_female_hmm),
            //
            VoiceChoice(PredefinedMarryTtsSpeechConfig.cmu_rms_hsmm_en_US_male_hmm),
            VoiceChoice(PredefinedMarryTtsSpeechConfig.cmu_bdl_hsmm_en_US_male_hmm),
            VoiceChoice(PredefinedMarryTtsSpeechConfig.dfki_spike_hsmm_en_GB_male_hmm),
            VoiceChoice(PredefinedMarryTtsSpeechConfig.dfki_obadiah_hsmm_en_GB_male_hmm),
        )

        loadExistentWords()

        pane.wordEntriesTable.fromColumn.addEventHandler(TableColumn.editCommitEvent<CardWordEntry,String>()) {
            val wordOrPhrase = it.newValue
            if (wordOrPhrase.isNotBlank()) Platform.runLater { onCardFromEdited(wordOrPhrase) }
        }

        pane.addIsShownHandler { CompletableFuture.runAsync { allWordCardSetsManager.reloadAllSets() } }

        installNavigationHistoryUpdates(currentWordsList, navigationHistory)
    }

    private var toPlayWordOnSelect: Boolean
        get() = settingsPane.playWordOnSelect
        set(value) { settingsPane.playWordOnSelect = value }


    private fun onCardFromEdited(wordOrPhrase: String) {
        if (settingsPane.warnAboutDuplicatesInOtherSets) {
            val foundInOtherSets = allWordCardSetsManager.findBy(wordOrPhrase)
            if (foundInOtherSets.isNotEmpty())
                showThisWordsInOtherSetsPopup(wordOrPhrase, foundInOtherSets)
        }
    }

    private fun showThisWordsInOtherSetsPopup(wordOrPhrase: String, cards: List<SearchEntry>) {
        val mainWnd = pane.scene.window

        otherCardsViewPopup.show(mainWnd, wordOrPhrase, cards) {
            val xOffset = 20.0;  val yOffset = 50.0
            Point2D(
                mainWnd.x + mainWnd.width - otherCardsViewPopup.width - xOffset,
                mainWnd.y + yOffset)
        }
    }

    internal fun showSourceSentences(card: CardWordEntry) {
        //showTextAreaPreviewDialog(pane, "Source sentences of '${card.from}'", card.sourceSentences)

        val htmlWithHighlighting =
            escapeHtml4(card.sourceSentences.trim())
                .highlightWords(escapeHtml4(card.from.trim()), "red")
                .replace("\n", "<br/>")

        showHtmlTextPreviewDialog(pane, "Source sentences of '${card.from}'", htmlWithHighlighting)
    }

    private val audioPlayer = JavaFxSoundPlayer(PlayingMode.Async)
    private val howJSayWebDownloadSpeechSynthesizer: HowJSayWebDownloadSpeechSynthesizer by lazy { HowJSayWebDownloadSpeechSynthesizer(audioPlayer) }

    private val voiceManager = VoiceManager()

    internal fun playSelectedWord() = currentWordsList.singleSelection?.let { playFrom(it) }

    private fun playFrom(card: CardWordEntry) {
        val voice = settingsPane.voice
        CompletableFuture.runAsync { playWord(card.from.trim(), voice) }
            .exceptionally {
                val rootCause = ExceptionUtils.getRootCause(it)
                //log.info { "Word [${card.from}] is not played => ${rootCause.javaClass.simpleName}: ${rootCause.message}" }
                log.info { "Word [${card.from}] is not played => ${rootCause.message}" }
                null
            }
    }

    private fun playWord(text: String, ignore: VoiceChoice) = voiceManager.speak(text)

    private fun playWord_Old(text: String, voice: VoiceChoice) {
        when (voice.synthesizer) {
            PredefSpeechSynthesizer.MarryTTS -> {

                val config: MarryTtsSpeechConfig? = PredefinedMarryTtsSpeechConfig.values()
                    .find { it.config.voice_Selections == voice.voice || it.config.voice == voice.voice }
                    ?.config

                requireNotNull(config) { "MarryTTS config for $voice is not found." }

                MarryTtsSpeechSynthesizer(config, audioPlayer).speak(text.trim())
            }
            PredefSpeechSynthesizer.Web -> {
                when (voice.voice) {
                    // TODO: refactor, use some Players list/map
                    "howjsay.com" -> howJSayWebDownloadSpeechSynthesizer.speak(text.trim())
                }
            }
        }
    }

    private fun updateSpeechSynthesizerAvailability() {

        val deadMarryVoices: List<VoiceChoice> = PredefinedMarryTtsSpeechConfig.values()
            .filter { it.config.locale.startsWith("en") }
            .filterNot { testSpeechSynthesizer(it) }
            .map { VoiceChoice(it) }

        log.debug { "updateSpeechSynthesizerAvailability deadMarryVoices: $deadMarryVoices" }

        Platform.runLater { settingsPane.deadVoices.clear(); settingsPane.deadVoices.addAll(deadMarryVoices) }
    }

    private fun testSpeechSynthesizer(voiceConfig: PredefinedMarryTtsSpeechConfig): Boolean {
        val playerStub = object : AudioPlayer { override fun play(audioSource: AudioSource) { } }
        return try {
            MarryTtsSpeechSynthesizer(voiceConfig, playerStub).speak("apple")
            log.debug { "MarryTTS ${voiceConfig.config.voice_Selections} is OK" }
            true
        }
        catch (ex: Exception) {
            log.debug("MarryTTS {} is failed", voiceConfig.config.voice_Selections, ex)
            false
        }
    }

    private val currentWordsSelection: TableView.TableViewSelectionModel<CardWordEntry> get() = currentWordsList.selectionModel
    private val currentWarnAboutMissedBaseWordsMode: WarnAboutMissedBaseWordsMode get() = pane.warnAboutMissedBaseWordsModeDropDown.value

    internal fun markDocumentIsDirty() { this.documentIsDirty = true; Platform.runLater { updateTitle() } }
    private fun resetDocumentIsDirty() { this.documentIsDirty = false; Platform.runLater { updateTitle() } }


    private fun addKeyBindings() {
        if (isReadOnly) return

        addGlobalKeyBinding(pane, openDocumentKeyCodeCombination) { loadWordsFromFile() }
        addGlobalKeyBinding(pane, saveDocumentKeyCodeCombination) { saveAll() }

        addGlobalKeyBinding(pane, previousNavigationKeyCodeCombination) { navigateToCard(NavigationDirection.Back, navigationHistory, currentWordsList) }
        addGlobalKeyBinding(pane, nextNavigationKeyCodeCombination)     { navigateToCard(NavigationDirection.Forward, navigationHistory, currentWordsList) }
    }

    fun isOneOfSelectedWordsHasNoBaseWord(): Boolean =
        currentWordsSelection.selectedItems.any { NoBaseWordInSet in it.statuses }

    fun ignoreNoBaseWordInSet() =
        currentWordsSelection.selectedItems.forEach {
                    updateSetProperty(it.statusesProperty, WordCardStatus.BaseWordDoesNotExist, UpdateSet.Set) }

    fun ignoreTooManyExampleCardCandidates() =
        currentWordsSelection.selectedItems.forEach {
            updateSetProperty(it.statusesProperty, WordCardStatus.IgnoreExampleCardCandidates, UpdateSet.Set) }

    fun addAllBaseWordsInSet() = addAllBaseWordsInSetImpl(currentWords)
    fun addBaseWordsInSetForSelected() = addAllBaseWordsInSetImpl(currentWordsSelection.selectedItems)

    private fun addAllBaseWordsInSetImpl(wordCards: Iterable<CardWordEntry>) =
        currentWordsList.runWithScrollKeeping {

            val addedWordsMapping = addBaseWordsInSet(wordCards, currentWarnAboutMissedBaseWordsMode, currentWords, dictionary)

            if (addedWordsMapping.size == 1) {
                val newBaseWordCard = addedWordsMapping.values.asSequence().flatten().first()

                // select new base word to edit it immediately
                if (currentWordsSelection.selectedItems.size <= 1) {
                    currentWordsSelection.clearSelection()
                    currentWordsSelection.select(newBaseWordCard.also { addChangeCardListener(it) })
                }
            }
        }

    fun addTranscriptions() =
        addTranscriptions(currentWords, dictionary)
            .doIfSuccess { markDocumentIsDirty() }


    fun removeSelected() {
        val selectedSafeCopy = currentWordsSelection.selectedItems.toList()
        currentWordsSelection.clearSelection()
        currentWordsList.runWithScrollKeeping {
            currentWords.removeAll(selectedSafeCopy) }
    }

    private fun analyzeAllWords(allWords: Iterable<CardWordEntry>) =
        analyzeWordCards(allWords, currentWarnAboutMissedBaseWordsMode, allWords, dictionary)
            .also { recalculateWarnedWordsCount() }
    internal fun reanalyzeAllWords() =
        analyzeAllWords(currentWords)
            .also { recalculateWarnedWordsCount() }
    private fun reanalyzeOnlyWords(words: Iterable<CardWordEntry>) =
        analyzeWordCards(words, currentWarnAboutMissedBaseWordsMode, currentWords, dictionary)
            .also { recalculateWarnedWordsCount() }
    internal fun reanalyzeOnlyWords(vararg words: CardWordEntry) = reanalyzeOnlyWords(words.asIterable())

    private fun recalculateWarnedWordsCount() {
        val warnings = toolBar2.nextPrevWarningWord.selectedWarnings
        val wordCountWithWarning = currentWords.count { it.hasOneOfWarnings(warnings) }
        pane.updateWarnWordCount(wordCountWithWarning)
    }

    fun toggleTextSelectionCaseOrLowerCaseRow() = currentWordsList.toggleTextSelectionCaseOrLowerCaseRow()

    private fun copySelectedWord() = copyWordsToClipboard(currentWordsSelection.selectedItems)


    fun translateSelected() =
        currentWordsSelection.selectedItems.doIfNotEmpty {
            dictionary.translateWords(it)
            markDocumentIsDirty()
            reanalyzeOnlyWords(it)
            currentWordsList.refresh()
        }


    fun translateAll() {
        dictionary.translateWords(currentWords)
        markDocumentIsDirty()
        reanalyzeAllWords()
        currentWordsList.refresh()
    }

    internal fun removeIgnoredFromCurrentWords() {
        val toRemove = currentWords.asSequence()
            .filter { word -> ignoredWordsSorted.contains(word.from) }
            .toList()
        currentWordsList.runWithScrollKeeping { currentWords.removeAll(toRemove) }
    }

    fun removeWordsFromOtherSetsFromCurrentWords() =
        removeWordsFromOtherSetsFromCurrentWords(currentWords, this.currentWordsFile)

    enum class LoadType {
        /** In case of open action this file will/can/should be overwritten.  */
        Open,
        /** In case of import action during saving filename will/should be requested.  */
        Import,
    }

    private fun doLoadAction(fileOrDir: Path?, loadAction: (Path)->LoadType) {

        validateCurrentDocumentIsSaved("Open file")

        val filePath = if (fileOrDir == null || fileOrDir.isDirectory())
            showOpenDialog(dir = fileOrDir)?.toPath()
            else fileOrDir

        if (filePath == null) return

        if (filePath == ignoredWordsFile) {
            showErrorAlert(pane, "You cannot open [${ignoredWordsFile.name}].")
            return
        }

        val loadType = loadAction(filePath)

        when (loadType) {
            LoadType.Import -> markDocumentIsDirty()
            LoadType.Open   -> {
                // !!! Only if success !!!
                updateCurrentWordsFile(filePath)
                resetDocumentIsDirty()
            }
        }
    }

    private fun showOpenDialog(dir: Path? = null, extensions: List<String> = emptyList()): File? {

        val allExtensions = listOf("*.csv", "*.words", "*.txt", "*.srt")
        val ext = extensions.ifEmpty { allExtensions }

        val fc = FileChooser()
        fc.title = "Select words file"
        fc.initialDirectory = (dir ?: dictDirectory).toFile()
        fc.extensionFilters.add(FileChooser.ExtensionFilter("Words file", ext))

        return fc.showOpenDialog(pane.scene.window)
    }

    internal fun doIsCurrentDocumentIsSaved(currentAction: String = ""): Boolean =
        try { validateCurrentDocumentIsSaved(currentAction); true } catch (_: Exception) { false }


    /**
     * Throws exception if document is not saved and user would cancel saving.
     *
     * Actually there exception is used for (less/more) flow what is an anti-pattern...
     * but in this case it is make sense in my opinion because you do not need to write 'if'-s
     * and can use it as assert/require flows.
     * If you want to use boolean function you just can use doIsCurrentDocumentIsSaved().
     */
    private fun validateCurrentDocumentIsSaved(currentAction: String = "") {

        fun throwCanceled(msg: String = "Action [$currentAction] was cancelled."): Nothing { throw ActionCanceledException(currentAction, msg)}

        if (documentIsDirty) {
            val selectedButton = showConfirmation(pane,
                "Current words are not saved and last changes can be lost.\n" +
                    "Do you want save current words?",
                currentAction, ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)

            selectedButton.ifPresentOrElse(
                { btn ->
                    when (btn) {
                        ButtonType.NO  -> { /* Nothing to do */ }
                        ButtonType.YES -> {
                            saveCurrentWords()
                            if (documentIsDirty) throwCanceled("Seems save action was cancelled.")
                        }
                        ButtonType.CANCEL -> throwCanceled()
                        ButtonType.CLOSE  -> throwCanceled()
                        else -> throw IllegalStateException("Unexpected user choice [$btn].")
                    }
                },
                { throwCanceled() }
            )
        }
    }

    private fun updateCurrentWordsFile(filePath: Path?) {
        this.currentWordsFile = filePath
        updateTitle()

        allWordCardSetsManager.ignoredFile = filePath
        CompletableFuture.runAsync { allWordCardSetsManager.reloadAllSets() }
    }

    private fun updateTitle() {
        val filePath: Path? = this.currentWordsFile
        val documentNameTitle = if (filePath == null) appTitle else "$appTitle - ${filePath.name}"
        val documentIsDirtySuffix = if (documentIsDirty) " *" else ""
        val windowTitle = "$documentNameTitle$documentIsDirtySuffix"
        setWindowTitle(pane, windowTitle)
    }

    fun newDocument() {
        validateCurrentDocumentIsSaved("New document")

        currentWords.clear()
        updateCurrentWordsFile(null)
        resetDocumentIsDirty()
    }

    fun loadWordsFromFile() = loadWordsFromFile(null)

    fun loadWordsFromFile(file: Path?) {
        doLoadAction(file) { filePath ->
            val fileExt = filePath.extension.lowercase()
            val words: List<CardWordEntry> = when (fileExt) {
                "txt"          -> loadWords(filePath).map { CardWordEntry(it, "") }
                "csv", "words" -> loadWordCards(filePath)
                "srt"          -> loadFromSrt(filePath)
                else           -> throw IllegalArgumentException("Unexpected file extension [${filePath}]")
            }
            // T O D O: make it async, but it is not easy because there are change listeners which also call analyzeAllWords()
            // We need to do ANY/EVERY call to analyzeAllWords() async
            .also { analyzeAllWords(it) }

            currentWords.setAll(words)
            currentWordsList.sort()

            addChangeCardListener(words)

            RecentDocuments().addRecent(filePath)

            if (filePath.isInternalCsvFormat) LoadType.Open else LoadType.Import
        }
    }

    private val changeCardListener = ChangeListener<Any> { prop,_,_ ->
        markDocumentIsDirty()

        // in general, it is bad idea to use so many 'instanceOf' but it is not critical there
        if (prop is ReadOnlyProperty<*>) {
            val bean = prop.bean
            if (bean is CardWordEntry) reanalyzeOnlyWords(bean)
        }
    }

    private fun addChangeCardListener(cards: Iterable<CardWordEntry>) =
        cards.forEach { addChangeCardListener(it) }

    private fun addChangeCardListener(card: CardWordEntry) {
        card.fromProperty.addListener(changeCardListener)
        card.fromWithPrepositionProperty.addListener(changeCardListener)
        card.toProperty.addListener(changeCardListener)
        card.transcriptionProperty.addListener(changeCardListener)
        card.examplesProperty.addListener(changeCardListener)
        card.statusesProperty.addListener(changeCardListener)
        card.predefinedSetsProperty.addListener(changeCardListener)
        card.sourcePositionsProperty.addListener(changeCardListener)
        card.sourceSentencesProperty.addListener(changeCardListener)
    }

    private fun loadFromSrt(filePath: Path) =
        extractWordsFromFile(filePath, SentenceEndRule.ByEndingDot) { StringReader(loadOnlyTextFromSrt(it)) }

    @Suppress("SameParameterValue")
    private fun extractWordsFromFile(filePath: Path, sentenceEndRule: SentenceEndRule, preProcessor: (Reader)->Reader = { it }): List<CardWordEntry> {
        val toIgnoreWords = if (settingsPane.autoRemoveIgnoredWords) this.ignoredWords else emptySet()
        return mergeDuplicates(extractWordsFromFile(filePath, sentenceEndRule, toIgnoreWords, preProcessor))
    }

    fun loadFromClipboard() {
        val toIgnoreWords = if (settingsPane.autoRemoveIgnoredWords) ignoredWords else emptySet()
        val words = extractWordsFromClipboard(Clipboard.getSystemClipboard(), settingsPane.sentenceEndRule, toIgnoreWords)
            .also { addChangeCardListener(it) }

        currentWords.addAll(words)
        reanalyzeAllWords()
    }


    fun moveSelectedToIgnored() {

        val selectedWords = currentWordsSelection.selectedItems.toList()

        log.debug("selectedWords: {} moved to IGNORED.", selectedWords)

        val newIgnoredWordEntries = selectedWords.filter { !ignoredWordsSorted.contains(it.from) }
        val newIgnoredWords = newIgnoredWordEntries.map { it.from }
        log.debug("newIgnored: {}", newIgnoredWords)
        ignoredWords.addAll(newIgnoredWords)

        currentWordsList.runWithScrollKeeping {
            currentWordsSelection.clearSelection()
            currentWords.removeAll(selectedWords)
        }
    }

    /*
    fun moveSelectedToIgnored() {

        val sw = startStopWatch("moveSelectedToIgnored")

        val selectedWords = currentWordsSelection.selectedItems.toList()

        log.debug("selectedWords: {} moved to IGNORED.", selectedWords)

        val newIgnoredWordEntries = selectedWords.filter { !ignoredWordsSorted.contains(it.from) }
        sw.logInfo(log, "filter newIgnoredWordEntries")

        val newIgnoredWords = newIgnoredWordEntries.map { it.from }
        log.debug("newIgnored: {}", newIgnoredWords)
        sw.logInfo(log, "creating newIgnoredWords")

        ignoredWords.addAll(newIgnoredWords)
        sw.logInfo(log, "adding newIgnoredWords")

        currentWordsList.runWithScrollKeeping {
            currentWordsSelection.clearSelection()
            sw.logInfo(log, "clearSelection")

            currentWords.removeAll(selectedWords)
            sw.logInfo(log, "removeAll(selectedWords)")
        }
    }
    */

    fun saveAll() {
        try {
            saveCurrentWords()
            saveIgnored()
        }
        catch (ex: Exception) {
            showErrorAlert(pane, "Error of saving words\n${ex.message}")
        }
    }

    enum class InsertPosition { Above, Below }

    fun cloneWordCard() {
        val selected: CardWordEntry? = currentWordsList.singleSelection

        selected?.also {
            val pos = currentWordsList.selectionModel.selectedIndex
            val cloned: CardWordEntry = selected.copy().also { addChangeCardListener(it) }

            currentWordsList.runWithScrollKeeping { currentWordsList.items.add(pos, cloned) }
        }
    }

    fun insertWordCard(insertPosition: InsertPosition) {
        val isWordCardsSetEmpty = currentWords.isEmpty()
        val currentlySelectedIndex = currentWordsSelection.selectedIndex

        val positionToInsert = when {
            isWordCardsSetEmpty -> 0

            currentlySelectedIndex != -1 -> when (insertPosition) {
                InsertPosition.Above -> currentlySelectedIndex
                InsertPosition.Below -> currentlySelectedIndex + 1
            }

            else -> -1
        }

        if (positionToInsert != -1) {
            val newCardWordEntry = CardWordEntry("", "").also { addChangeCardListener(it) }

            if (currentWordsList.editingCell?.row != -1)
                currentWordsList.edit(-1, null)

            currentWordsList.runWithScrollKeeping( {

                    currentWords.add(positionToInsert, newCardWordEntry)
                    currentWordsSelection.clearAndSelect(positionToInsert, currentWordsList.fromColumn)
                },
                {
                    // JavaFX bug.
                    // Without this call at the end of view editing cell appears twice (in right position and in wrong some below position)
                    // and erases data in wrong (below) position !!!
                    // Probably such behavior happens due to my hack in TableView.fixEstimatedContentHeight() (which uses scrolling over all rows)
                    // but now I have no better idea how to fix EstimatedContentHeight.
                    //
                    currentWordsList.refresh()

                    // JavaFX bug: without runLaterWithDelay() editing cell does not get focus (you need to click on it or use Tab key)
                    // if column cells were not present before (if TableView did not have content yet).
                    // Platform.runLater() also does not help.
                    //
                    runLaterWithDelay(50) { currentWordsList.edit(positionToInsert, currentWordsList.fromColumn) }
                }
            )
        }
    }

    // enum class WordsOrder { ORIGINAL, SORTED }

    private fun saveCurrentWords() = doSaveCurrentWords { filePath ->
        val words = currentWords

        val internalFormatFile = filePath.useFilenameSuffix(internalWordCardsFileExt)
        saveWordCards(internalFormatFile, CsvFormat.Internal, words)
        RecentDocuments().addRecent(internalFormatFile)

        val memoWordFile = filePath.useFilenameSuffix(memoWordFileExt)
        if (words.size <= maxMemoCardWordCount)
            saveWordCards(memoWordFile, CsvFormat.MemoWord, words)
        else {
            memoWordFile.deleteIfExists()
            log.info { "Saving MemoWord file ${memoWordFile.name} is skipped since it has too many entries ${words.size} (> $maxMemoCardWordCount)." }
        }

        val splitFilesDir = filePath.parent.resolve(filePath.baseWordsFilename)
        if (splitFilesDir.exists()) {
            val oldSplitFiles: List<Path> = splitFilesDir.listDirectoryEntries("*${filePath.baseWordsFilename}*.csv").sorted()
            if (oldSplitFiles.isNotEmpty())
                log.debug { "### Removing old split files: \n  ${oldSplitFiles.joinToString("\n  ")}" }
            oldSplitFiles.forEach { it.deleteIfExists() }
        }

        splitFilesDir.createDirectories()
        saveSplitWordCards(filePath, currentWords, splitFilesDir, settingsPane.splitWordCountPerFile)

        resetDocumentIsDirty()
    }

    private fun doSaveCurrentWords(saveAction:(Path)->Unit) {

        var filePath: Path? = this.currentWordsFile
        if (filePath == null) {
            filePath = showTextInputDialog(pane, "Enter new words filename")
                .map { wordsFilename -> dictDirectory.resolve(wordsFilename.withFileExt(internalWordCardsFileExt)) }
                .orElse(null)
        }

        if (filePath == null) {
            //showErrorAlert(pane, "Filename is not specified.")
            return
        }

        saveAction(filePath)

        // !!! ONLY if success !!!
        updateCurrentWordsFile(filePath)
    }

    fun splitCurrentWords(): Unit = doSaveCurrentWords { filePath ->
        val words = currentWords
        val df = SimpleDateFormat("yyyyMMdd-HHmmss")
        val splitFilesDir = filePath.parent.resolve("split-${df.format(Date())}")

        val defaultSplitWordCountPerFile = settingsPane.splitWordCountPerFile
        val strSplitWordCountPerFile = showTextInputDialog(pane,
            "Current words will be split into several files and put into directory $splitFilesDir.\n" +
                    "Please, enter word count for every file.", "Splitting current words",
            "$defaultSplitWordCountPerFile")

        strSplitWordCountPerFile.ifPresent {
            try {
                val wordCountPerFile: Int = it.toInt()
                require(wordCountPerFile >= 1) { "Word count should be positive value." }

                require(wordCountPerFile <= maxMemoCardWordCount) {
                    "Word count should be less than 300 since memo-word supports only $maxMemoCardWordCount sized word sets." }

                saveSplitWordCards(filePath, words, splitFilesDir, wordCountPerFile)
            }
            catch (ex: Exception) {
                log.error(ex) { "Splitting words error: ${ex.message}" }
                showErrorAlert(pane, ex.message ?: "Unknown error", "Error of splitting.")
            }
        }
    }

    internal fun joinWords() {

        val fc = FileChooser()
        fc.title = "Select words files"
        fc.initialDirectory = dictDirectory.toFile()
        fc.extensionFilters.add(FileChooser.ExtensionFilter("Words file", "*.csv"))
        val selectedFiles: List<File>? = fc.showOpenMultipleDialog(pane.scene.window)?.sorted()

        if (selectedFiles.isNullOrEmpty()) {
            //showInfoAlert(pane, "No files to join.")
            return
        }

        if (selectedFiles.size == 1) {
            showInfoAlert(pane, "No sense to use join only one file. Just use load/open action.")
            return
        }

        val words: List<CardWordEntry> = selectedFiles
            .flatMap { loadWordCards(it.toPath()) }
            .also { analyzeAllWords(it) }

        if (words.isEmpty()) {
            showInfoAlert(pane, "Files ${selectedFiles.map { it.name }} do not contain words.")
            return
        }

        updateCurrentWordsFile(null)
        currentWords.setAll(words)
    }

    private fun saveIgnored() {
        val ignoredWords = this.ignoredWordsSorted
        if (ignoredWords.isNotEmpty())
            saveWordsToTxtFile(ignoredWordsFile, ignoredWords)
    }

    private fun loadExistentWords() {
        loadIgnored()
        allProcessedWords.setAll(loadWordsFromAllExistentDictionaries(null))
    }


    private fun loadIgnored() {
        if (ignoredWordsFile.exists()) {
            val ignoredWords = loadWords(ignoredWordsFile)
            this.ignoredWords.setAll(ignoredWords)
        }
    }

    fun addToDifficultSet() =
        currentWordsSelection.selectedItems.forEach { it.predefinedSets += PredefinedSet.DifficultSense }

    fun addToListenSet() =
        currentWordsSelection.selectedItems.forEach { it.predefinedSets += PredefinedSet.DifficultToListen }

    internal fun moveSubTextToSeparateCard() {
        val focusOwner = pane.scene.focusOwner
        val editingCard = currentWordsList.editingItem
        val editingTableColumn: TableColumn<CardWordEntry, *>? = currentWordsList.editingCell?.column?.let { currentWordsList.columns[it] }

        if (editingCard != null && (editingTableColumn === currentWordsList.toColumn || editingTableColumn === currentWordsList.examplesColumn)
            && focusOwner is TextArea && focusOwner.belongsToParent(currentWordsList)) {
            moveSubTextToSeparateCard(focusOwner, editingCard, editingTableColumn)
        }
    }

    internal fun moveSubTextToExamplesAndSeparateCard() {
        val focusOwner = pane.scene.focusOwner
        val editingCard = currentWordsList.editingItem
        val editingTableColumn: TableColumn<CardWordEntry, *>? = currentWordsList.editingCell?.column?.let { currentWordsList.columns[it] }

        if (editingCard != null && (editingTableColumn === currentWordsList.toColumn)
            && focusOwner is TextArea && focusOwner.belongsToParent(currentWordsList)) {
            moveSubTextToExamplesAndSeparateCard(focusOwner, editingCard, editingTableColumn)
        }
    }

    internal fun moveSubTextToExamples() {
        val focusOwner = pane.scene.focusOwner
        val editingCard = currentWordsList.editingItem
        val editingTableColumn: TableColumn<CardWordEntry, *>? = currentWordsList.editingCell?.column?.let { currentWordsList.columns[it] }

        if (editingCard != null && editingTableColumn === currentWordsList.toColumn
            && focusOwner is TextArea && focusOwner.belongsToParent(currentWordsList)) {
            moveSubTextToExamples(editingCard, focusOwner)
        }
    }

    internal fun moveSubTextToSeparateCard(editor: TextInputControl, item: CardWordEntry, tableColumn: TableColumn<CardWordEntry, String>) {
        createCardFromSelectionOrCurrentLine(editor, tableColumn, item, currentWordsList)
            ?.also {
                reanalyzeOnlyWords(it)
                addChangeCardListener(it)
            }
    }

    internal fun moveSubTextToExamplesAndSeparateCard(editor: TextInputControl, item: CardWordEntry, tableColumn: TableColumn<CardWordEntry, String>) {
        tryToAdToExamples(editor.selectionOrCurrentLine, item)
        moveSubTextToSeparateCard(editor, item, tableColumn)
    }

    private fun createCardFromSelectionOrCurrentLine(
        textInput: TextInputControl,
        tableColumn: TableColumn<CardWordEntry, String>,
        currentCard: CardWordEntry,
        currentWords: TableView<CardWordEntry>,
    ): CardWordEntry? {

        val selectionOrCurrentLine = textInput.selectionOrCurrentLine
        if (selectionOrCurrentLine.isBlank()) return null

        val newCard: CardWordEntry = selectionOrCurrentLine.parseToCard() ?: return null

        if (textInput.selectedText.isEmpty()) {
            textInput.selectRange(textInput.selectionOrCurrentLineRange)
        }

        // replace/remove also ending '\n'
        if (textInput.selection.end < textInput.text.length && textInput.text[textInput.selection.end] == '\n')
            textInput.selectRange(textInput.selection.start, textInput.selection.end + 1)
        // verify again (in case of end "\n\n")
        if (textInput.selection.end < textInput.text.length && textInput.text[textInput.selection.end] == '\n')
            textInput.selectRange(textInput.selection.start, textInput.selection.end + 1)

        textInput.replaceSelection("")

        val caretPosition = textInput.caretPosition
        val scrollLeft = if (textInput is TextArea) textInput.scrollLeft else 0.0
        val scrollTop = if (textInput is TextArea) textInput.scrollTop else 0.0

        // After adding new card editor loses focus, and we will lose our changes
        // I do not know how to avoid this behaviour...
        // And for that reason I emulate cell commit and reopening it again.

        val prop: StringPropertyBase = tableColumn.cellValueFactory.call(
            TableColumn.CellDataFeatures(currentWords, tableColumn, currentCard)) as StringPropertyBase

        // emulate cell commit to avoid losing changes on focus lost
        prop.set(textInput.text)

        val currentCardIndex = currentWords.items.indexOf(currentCard)

        // We need this workaround because after adding new card current selected one looses focus, and we need to reselect it again...
        // And to avoid unneeded word's autoplaying we disable playing and re-enable it again after re-selecting card.
        val currentToPlayWordOnSelect = toPlayWordOnSelect
        toPlayWordOnSelect = false

        currentWords.runWithScrollKeeping(
            {
                // TODO: initially insert in proper place
                currentWords.items.add(currentCardIndex + 1, newCard)
            },
            {
                cellEditorStates[Pair(currentWordsList.examplesColumn, currentCard)] = CellEditorState(scrollLeft, scrollTop, caretPosition)

                currentWords.selectItem(currentCard)
                currentWords.edit(currentWords.items.indexOf(currentCard), tableColumn)

                toPlayWordOnSelect = currentToPlayWordOnSelect
            })

        return newCard
    }


    fun removeWordsFromOtherSet() {
        val file = showOpenDialog(extensions = listOf("*.csv")) ?: return

        if (file == currentWordsFile) {
            showInfoAlert(pane, "You cannot remove themself :-)")
            return
        }

        val words: List<String> = loadWords(file.toPath())
        val wordsSet: Set<String> = words.toSortedSet(String.CASE_INSENSITIVE_ORDER)

        val cardsToRemove = currentWords.filter { it.from in wordsSet }
        // Let's try to remove as one bulk operation to minimize notifications hell.
        currentWords.removeAll(cardsToRemove)
    }

}


internal fun String.highlightWords(wordOrPhrase: String, color: String): String {

    val startTag = "<span style=\"color:$color; font-weight: bold;\"><strong><b><font color=\"$color\">"
    val endTag = "</font></b></strong></span>"

    var result = this
    var wordIndex = -1

    //val searchWord = if (!result.contains(wordTrimmed) && wordTrimmed.contains(' '))
    //                 wordTrimmed.substringBefore(' ') else wordTrimmed
    val searchWord = this.findWordToHighlight(wordOrPhrase)

    // T O D O: would be nice to rewrite using StringBuilder, but not now :-) (it is not used very often)
    do {
        wordIndex = result.indexOf(searchWord, wordIndex + 1, true)
        if (wordIndex != -1) {
            result = result.substring(0, wordIndex) +
                    startTag +
                    result.substring(wordIndex, wordIndex + searchWord.length) +
                    endTag +
                    result.substring(wordIndex + searchWord.length)
            wordIndex += startTag.length + endTag.length
        }
    } while (wordIndex != -1)

    return result
}

internal fun String.findWordToHighlight(tryToHighlightWord: String): String {
    val word = tryToHighlightWord.trim()

    val preparations: List<(String)->String> = listOf(
        { it },
        { it.removePrefix("to ") },
        { it.removePrefix("to be ") },
        { it.removeSuffix("ing") },
        { it.removeSuffix("ling") },
        { it.removeSuffix("e") },
        { it.removeSuffix("le") },
        { it.removeSuffix("y") },
        { it.removeSuffix("ly") },
        { it.removePrefix("to ").removeSuffix("e") },
        { it.removePrefix("to be ").removeSuffix("e") },
        { it.removePrefix("to ").removeSuffix("s to") },
        { it.removePrefix("to ").removeSuffix(" to") },
        { it.removeSuffix(" to") },
        { it.removeSuffix(" to").removeSuffix("e") },
        { it.removeSuffix(" to").removeSuffix("le") },
        { it.removeSuffix(" to").removeSuffix("y") },
        { it.removeSuffix(" to").removeSuffix("ly") },

        { it.removePrefix("to ").firstWord() },
        { it.removePrefix("to be ").firstWord() },
        { it.removePrefix("to ").firstWord().removeSuffix("e") },
        { it.removePrefix("to ").firstWord().removeSuffix("le") },
        { it.removePrefix("to ").firstWord().removeSuffix("y") },
        { it.removePrefix("to ").firstWord().removeSuffix("ly") },
        { it.removePrefix("to be ").firstWord().removeSuffix("s") },
    )

    return preparations.firstNotNullOfOrNull { prep ->
        val preparedWord = prep(word)
        if (preparedWord in this) preparedWord else null
    } ?: word
}

private fun String.firstWord(): String {
    val s = this.trim()
    val endIndex = s.indexOfOneOfChars(" \n\t")
    return if (endIndex == -1) s else s.substring(0, endIndex)
}

internal fun moveSubTextToExamples(card: CardWordEntry, textInput: TextInputControl) {
    val textRange = textInput.selectionOrCurrentLineRange

    if (tryToAdToExamples(textInput.selectionOrCurrentLine, card)) {
        //textInput.selectRange(textInput.selectionOrCurrentLineRange)
        textInput.replaceText(textRange, "")
    }
}


fun tryToAdToExamples(example: String, card: CardWordEntry): Boolean {
    if (example.isBlank()) return false

    val examples = card.examples
    val fixedExample = example
        .trim().removePrefix(";").removeSuffix(";")
    val preparedExamplesToMove = fixedExample
        .trim().replace(';', '\n')

    // TODO: use all possible combination of this replacements (the easiest way use numbers as binary number)
    //val possibleReplacements: List<Map<String,String>> = listOf(
    //    mapOf(";" to "\n"),
    //    mapOf("а>" to "а)", "б>" to "б)", "в>" to "в)", "г>" to "г)", "д>" to "д)", "е>" to "е)", ),
    //    mapOf("а>" to "\n", "б>" to "\n", "в>" to "\n", "г>" to "\n", "д>" to "\n", "е>" to "\n", ),
    //    mapOf("а>" to "", "б>" to "", "в>" to "", "г>" to "", "д>" to "", "е>" to "", ),
    //    mapOf(
    //      юр юрид уст спорт амер полит жарг театр воен ист фон посл парл австрал шутл полигр
    //      "(_юр.)" to "(юр.)", "_юр." to "(юр.)",
    //      ),
    //)

    if (examples.containsOneOf(example, fixedExample, preparedExamplesToMove)) return false

    val separator = when {
        examples.isBlank() -> ""
        examples.endsWith("\n\n") -> ""
        examples.endsWith("\n") -> "\n"
        else -> "\n\n"
    }
    val endLine = if (preparedExamplesToMove.endsWith('\n')) "" else "\n"

    card.examples += "$separator$preparedExamplesToMove$endLine"

    return true
}


private val ignoreWordsForGoodLearnCardCandidate = listOf("a", "the", "to", "be")

fun CardWordEntry.isGoodLearnCardCandidate(): Boolean {
    val sentence = parseSentence(this.from, 0)
    val wordCount = sentence.allWords.asSequence()
        .filter { it.word !in ignoreWordsForGoodLearnCardCandidate }
        .count()
    return wordCount <= 4 && sentence.allWords
        // ??? Why I've added it? ???
        .all { !it.word.first().isUpperCase() }
}


fun String.parseToCard(): CardWordEntry? {

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


data class CellEditorState (
    val scrollLeft: Double,
    val scrollTop: Double,
    val caretPosition: Int,
)
