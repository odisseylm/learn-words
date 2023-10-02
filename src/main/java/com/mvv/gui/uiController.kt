package com.mvv.gui

import com.mvv.gui.audio.*
import com.mvv.gui.dictionary.AutoDictionariesLoader
import com.mvv.gui.dictionary.CachedDictionary
import com.mvv.gui.dictionary.Dictionary
import com.mvv.gui.dictionary.DictionaryComposition
import com.mvv.gui.javafx.*
import com.mvv.gui.util.*
import com.mvv.gui.words.*
import javafx.application.Platform
import javafx.beans.property.ReadOnlyProperty
import javafx.beans.property.StringPropertyBase
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.collections.transformation.SortedList
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.input.Clipboard
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.input.MouseEvent
import javafx.stage.FileChooser
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


class LearnWordsController (
    private val pane: MainWordsPane,
) {
    init { pane.controller = this }

    //private val projectDirectory = getProjectDirectory(this.javaClass)

    private val allDictionaries: List<Dictionary> = AutoDictionariesLoader().load() // HardcodedDictionariesLoader().load()
    internal val dictionary = CachedDictionary(DictionaryComposition(allDictionaries))

    private val currentWords: ObservableList<CardWordEntry> = FXCollections.observableArrayList()
    private var documentIsDirty: Boolean = false

    private val ignoredWords: ObservableList<String> = FXCollections.observableArrayList()
    private val ignoredWordsSorted: ObservableList<String> = SortedList(ignoredWords, String.CASE_INSENSITIVE_ORDER)

    private val allProcessedWords: ObservableList<String> = FXCollections.observableArrayList()

    private var currentWordsFile: Path? = null

    //private val toolBarController = ToolBarController(this)
    private val toolBarController2 = ToolBarControllerBig(this)
    private val settingsPane = SettingsPane()
    private val toolBar2 = ToolBar2(this).also {
        it.nextPrevWarningWord.addSelectedWarningsChangeListener { _, _, _ -> recalculateWarnedWordsCount() } }

    val cellEditorStates = WeakHashMap<Pair<TableColumn<CardWordEntry,*>, CardWordEntry>, CellEditorState>()

    init {
        Timer("updateSpeechSynthesizersAvailabilityTimer", true)
            .also { it.schedule(timerTask { updateSpeechSynthesizerAvailability() }, 15000, 15000) }
    }

    init {

        initTheme()
        // after possible adding theme CSS
        pane.stylesheets.add("spreadsheet.css")


        log.info("Used dictionaries\n" +
                allDictionaries.mapIndexed { i, d -> "${i + 1} $d" }.joinToString("\n") +
                "\n---------------------------------------------------\n\n"
        )


        val currentWordsLabelText = "File/Clipboard (%d words)"
        currentWords.addListener(ListChangeListener { pane.currentWordsLabel.text = currentWordsLabelText.format(it.list.size) })

        currentWords.addListener(ListChangeListener { markDocumentIsDirty(); reanalyzeAllWords() })

        val ignoredWordsLabelText = "Ignored words (%d)"
        ignoredWords.addListener(ListChangeListener { pane.ignoredWordsLabel.text = ignoredWordsLabelText.format(it.list.size) })


        pane.ignoredWordsList.items = ignoredWordsSorted

        val allProcessedWordsLabelText = "All processed words (%d)"
        allProcessedWords.addListener(ListChangeListener { pane.allProcessedWordsLabel.text = allProcessedWordsLabelText.format(it.list.size) })

        pane.allProcessedWordsList.items = SortedList(allProcessedWords, String.CASE_INSENSITIVE_ORDER)


        pane.currentWordsList.items = currentWords // Sorted
        //currentWordsList.setComparator(cardWordEntryComparator)

        addKeyBindings(currentWordsList, copyKeyCombinations.associateWith { {
            if (!currentWordsList.isEditing) copySelectedWord() } })

        pane.sceneProperty().addListener { _, _, newScene -> newScene?.let { addKeyBindings(it) } }

        pane.removeIgnoredButton.onAction = EventHandler { removeIgnoredFromCurrentWords() }


        pane.fromColumn.isSortable = true
        pane.fromColumn.sortType = TableColumn.SortType.ASCENDING
        pane.fromColumn.comparator = String.CASE_INSENSITIVE_ORDER

        currentWordsList.sortOrder.add(pane.fromColumn)

        // Platform.runLater is used to perform analysis AFTER word card changing
        // It would be nice to find better/proper event (with already changed underlying model after edit commit)
        val doOnWordChanging: (card: CardWordEntry)->Unit = { markDocumentIsDirty(); Platform.runLater { reanalyzeOnlyWords(it) } }

        pane.fromColumn.addEventHandler(TableColumn.editCommitEvent<CardWordEntry,String>()) { doOnWordChanging(it.rowValue) }
        pane.toColumn.addEventHandler(TableColumn.editCommitEvent<CardWordEntry,String>())   { doOnWordChanging(it.rowValue) }

        pane.warnAboutMissedBaseWordsModeDropDown.onAction = EventHandler { reanalyzeAllWords() }

        // It is needed if SortedList is used as TableView items
        // ??? just needed :-) (otherwise warning in console)
        //currentWordsSorted.comparatorProperty().bind(currentWordsList.comparatorProperty());

        fixSortingAfterCellEditCommit(pane.fromColumn)


        //toolBarController.fillToolBar(pane.toolBar)

        pane.topPane.children.add(0, MenuController(this).fillMenu())
        pane.topPane.children.add(toolBarController2.toolBar)
        pane.topPane.children.add(settingsPane)
        pane.topPane.children.add(toolBar2)

        val contextMenuController = ContextMenuController(this)
        currentWordsList.contextMenu = contextMenuController.contextMenu
        currentWordsList.onContextMenuRequested = EventHandler { contextMenuController.updateItemsVisibility() }

        currentWordsList.selectionModel.selectedItemProperty().addListener { _, _, _ ->
            Platform.runLater { if (settingsPane.playWordOnSelect) playSelectedWord() } }

        currentWordsList.addEventHandler(MouseEvent.MOUSE_CLICKED) { ev ->
            val card = currentWordsSelection.selectedItem
            val clickedOnSourceSentence = currentWordsSelection.selectedCells?.getOrNull(0)?.tableColumn == pane.sourceSentencesColumn

            if (ev.clickCount >= 2 && card != null && clickedOnSourceSentence)
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

    internal fun playSelectedWord() = currentWordsList.singleSelection?.let { playFrom(it) }

    private fun playFrom(card: CardWordEntry) {
        val voice = settingsPane.voice
        CompletableFuture.runAsync { playWord(card.from.trim(), voice) }
    }

    private fun playWord(text: String, voice: VoiceChoice) {
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

    internal val currentWordsList: TableView<CardWordEntry> get() = pane.currentWordsList
    //internal val currentWords: ObservableList<CardWordEntry> get() = pane.currentWordsList.items
    private val currentWordsSelection: TableView.TableViewSelectionModel<CardWordEntry> get() = currentWordsList.selectionModel
    private val currentWarnAboutMissedBaseWordsMode: WarnAboutMissedBaseWordsMode get() = pane.warnAboutMissedBaseWordsModeDropDown.value

    private fun markDocumentIsDirty() { this.documentIsDirty = true; Platform.runLater { updateTitle() } }
    private fun resetDocumentIsDirty() { this.documentIsDirty = false; Platform.runLater { updateTitle() } }


    private fun addKeyBindings(newScene: Scene) {
        newScene.accelerators[openDocumentKeyCodeCombination] = Runnable { loadWordsFromFile() }
        newScene.accelerators[saveDocumentKeyCodeCombination] = Runnable { saveAll() }

        newScene.accelerators[lowerCaseKeyCombination] = Runnable { toLowerCaseRow() }

        newScene.accelerators[KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.CONTROL_DOWN)] = Runnable { startEditingFrom() }
        newScene.accelerators[KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.CONTROL_DOWN)] = Runnable { startEditingTo() }
        newScene.accelerators[KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.CONTROL_DOWN)] = Runnable { startEditingTranscription() }
        newScene.accelerators[KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.CONTROL_DOWN)] = Runnable { startEditingRemarks() }
    }

    private fun initTheme() {
        when (settings.theme) {
            Theme.System -> { }
            Theme.SystemDark -> pane.style = "-fx-base:black" // standard JavaFX dark theme
            Theme.Dark -> pane.stylesheets.add("dark-theme.css")
        }
    }

    fun isOneOfSelectedWordsHasNoBaseWord(): Boolean =
        currentWordsSelection.selectedItems.isOneOfSelectedWordsHasNoBaseWord

    fun ignoreNoBaseWordInSet() =
        currentWordsSelection.selectedItems.forEach {
                    updateSetProperty(it.wordCardStatusesProperty, WordCardStatus.BaseWordDoesNotExist, UpdateSet.Set) }

    fun ignoreTooManyExampleCardCandidates() =
        currentWordsSelection.selectedItems.forEach {
            updateSetProperty(it.wordCardStatusesProperty, WordCardStatus.IgnoreExampleCardCandidates, UpdateSet.Set) }

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
            .doIfTrue { markDocumentIsDirty() }


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
    private fun reanalyzeOnlyWords(vararg words: CardWordEntry) = reanalyzeOnlyWords(words.asIterable())

    private fun recalculateWarnedWordsCount() {
        val warnings = toolBar2.nextPrevWarningWord.selectedWarnings
        val wordCountWithWarning = currentWords.count { it.hasOneOfWarning(warnings) }
        pane.updateWarnWordCount(wordCountWithWarning)
    }

    private fun startEditingFrom() = startEditingColumnCell(pane.fromColumn)
    private fun startEditingTo() = startEditingColumnCell(pane.toColumn)
    private fun startEditingTranscription() = startEditingColumnCell(pane.transcriptionColumn)
    private fun startEditingRemarks() = startEditingColumnCell(pane.examplesColumn)

    private fun startEditingColumnCell(column: TableColumn<CardWordEntry, String>) {
        val selectedIndex = currentWordsSelection.selectedIndex
        if (selectedIndex != -1) currentWordsList.edit(selectedIndex, column)
    }

    fun toLowerCaseRow() {
        if (currentWordsList.isEditing) return

        wordCardsToLowerCaseRow(currentWordsSelection.selectedItems)
        markDocumentIsDirty()

        //currentWordsList.sort()
        //currentWordsList.refresh()
    }

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

    private fun doLoadAction(openFile: Path?, loadAction: (Path)->LoadType) {

        validateCurrentDocumentIsSaved("Open file")

        val file: File? = if (openFile == null) {
            val fc = FileChooser()
            fc.title = "Select words file"
            fc.initialDirectory = dictDirectory.toFile()
            fc.extensionFilters.add(FileChooser.ExtensionFilter("Words file", "*.csv", "*.words", "*.txt", "*.srt"))

            fc.showOpenDialog(pane.scene.window)
        }
        else openFile.toFile()

        if (file == null) return

        val filePath = file.toPath()
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
        card.wordCardStatusesProperty.addListener(changeCardListener)
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

        currentWords.setAll(words) // or add all ??
        reanalyzeAllWords()
        updateCurrentWordsFile(null)
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
                    currentWordsSelection.clearAndSelect(positionToInsert, pane.fromColumn)
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
                    runLaterWithDelay(50) { currentWordsList.edit(positionToInsert, pane.fromColumn) }
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

        saveWordCards(filePath.useFilenameSuffix(memoWordFileExt), CsvFormat.MemoWord, words)

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

                val maxMemoCardWordCount = 300
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

    internal fun moveSelectedToSeparateCard(editor: TextInputControl, item: CardWordEntry, tableColumn: TableColumn<CardWordEntry, String>) {
        createCardFromSelection(editor, tableColumn, item, currentWordsList)
            ?.also {
                reanalyzeOnlyWords(it)
                addChangeCardListener(it)
            }
    }

    private fun createCardFromSelection(
        textInput: TextInputControl,
        tableColumn: TableColumn<CardWordEntry, String>,
        currentCard: CardWordEntry,
        currentWords: TableView<CardWordEntry>,
    ): CardWordEntry? {

        val selection = textInput.selectedText
        if (selection.isBlank()) return null

        val newCard: CardWordEntry = selection.parseToCard() ?: return null
        if (!newCard.isGoodLearnCardCandidate()) return null

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

        currentWords.runWithScrollKeeping(
            {
                // TODO: initially insert in proper place
                currentWords.items.add(currentCardIndex + 1, newCard)
            },
            {
                println("# Storing state in createCardFromSelection ${CellEditorState(scrollLeft, scrollTop, caretPosition)}")
                cellEditorStates[Pair(pane.examplesColumn, currentCard)] = CellEditorState(scrollLeft, scrollTop, caretPosition)

                currentWords.selectItem(currentCard)
                currentWords.edit(currentWords.items.indexOf(currentCard), tableColumn)

            })

        return newCard
    }

}


fun <S,T> fixSortingAfterCellEditCommit(column: TableColumn<S,T>) {

    column.addEventHandler(TableColumn.CellEditEvent.ANY) {

        if (it.eventType == TableColumn.editCommitEvent<S,T>()) {

            //val index = column.tableView?.editingCell?.row ?: -1
            //val editedEntry: S? = if (index >= 0) column.tableView.items[index] else null

            Platform.runLater {
                column.tableView.sort()

                //if (editedEntry != null) column.tableView.selectionModel.select(editedEntry)

                // seems bug in JavaFx, after cell edition complete table view looses focus
                if (!column.tableView.isFocused) column.tableView.requestFocus()
            }
        }
    }
}


internal fun String.highlightWords(word: String, color: String): String {

    val startTag = "<span style=\"color:$color; font-weight: bold;\"><strong><b><font color=\"$color\">"
    val endTag = "</font></b></strong></span>"

    val wordTrimmed = word.trim()
    var result = this
    var wordIndex = -1

    val searchWord = if (!result.contains(wordTrimmed) && wordTrimmed.contains(' '))
                     wordTrimmed.substringBefore(' ') else wordTrimmed

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


internal fun moveSelectedTextToExamples(card: CardWordEntry, textInput: TextInputControl) {
    val rawExamplesToMove = textInput.selectedText
    if (rawExamplesToMove.isBlank()) return

    val preparedExamplesToMove = rawExamplesToMove.trim().replace(';', '\n')
    if (card.examples.containsOneOf(rawExamplesToMove, preparedExamplesToMove)) return

    val separator = when {
        card.examples.isBlank() -> ""
        card.examples.endsWith("\n\n") -> ""
        card.examples.endsWith("\n") -> "\n"
        else -> "\n\n"
    }
    val endLine = if (preparedExamplesToMove.endsWith('\n')) "" else "\n"

    card.examples += "$separator$preparedExamplesToMove$endLine"

    //val newCaretPos = min(textInput.selection.start, textInput.selection.end)
    //textInput.text = textInput.text.substring(0, textInput.selection.start) + textInput.text.substring(textInput.selection.end)
    //textInput.selectRange(newCaretPos, newCaretPos)

    textInput.deleteText(textInput.selection.start, textInput.selection.end)
}



private val ignoreWordsForGoodLearnCardCandidate = listOf("a", "the", "to", "be")

fun CardWordEntry.isGoodLearnCardCandidate(): Boolean {
    val sentence = parseSentence(this.from, 0)
    val wordCount = sentence.allWords.asSequence()
        .filter { it.word !in ignoreWordsForGoodLearnCardCandidate }
        .count()
    return wordCount <= 4 && sentence.allWords.all { !it.word.first().isUpperCase() }
}


fun String.parseToCard(): CardWordEntry? {

    val indexOfRussianCharOrSpecial = this.indexOfFirst { it.isRussianLetter() || it == '_' }
    if (indexOfRussianCharOrSpecial == -1 || indexOfRussianCharOrSpecial == 0) return null

    val startOfTranslationCountStatus = if (this[indexOfRussianCharOrSpecial - 1] == '(')
        indexOfRussianCharOrSpecial - 1
        else indexOfRussianCharOrSpecial

    val from = this.substring(0, startOfTranslationCountStatus).trim()
    val to = this.substring(startOfTranslationCountStatus).trim()

    return CardWordEntry(from, to)
}


data class CellEditorState (
    val scrollLeft: Double,
    val scrollTop: Double,
    val caretPosition: Int,
)
