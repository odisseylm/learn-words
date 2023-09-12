package com.mvv.gui

import com.mvv.gui.audio.*
import com.mvv.gui.dictionary.AutoDictionariesLoader
import com.mvv.gui.dictionary.CachedDictionary
import com.mvv.gui.dictionary.Dictionary
import com.mvv.gui.dictionary.DictionaryComposition
import com.mvv.gui.javafx.*
import com.mvv.gui.util.ActionCanceledException
import com.mvv.gui.util.doIfNotEmpty
import com.mvv.gui.util.doIfTrue
import com.mvv.gui.util.withFileExt
import com.mvv.gui.words.*
import javafx.application.Platform
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
import javafx.stage.FileChooser
import java.io.File
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.*
import kotlin.io.path.*


private val log = mu.KotlinLogging.logger {}


class LearnWordsController (
    private val pane: MainWordsPane,
) {

    //private val projectDirectory = getProjectDirectory(this.javaClass)

    private val allDictionaries: List<Dictionary> = AutoDictionariesLoader().load() // HardcodedDictionariesLoader().load()
    internal val dictionary = CachedDictionary(DictionaryComposition(allDictionaries))

    private val currentWords: ObservableList<CardWordEntry> = FXCollections.observableArrayList()
    private var documentIsDirty: Boolean = false

    private val ignoredWords: ObservableList<String> = FXCollections.observableArrayList()
    private val ignoredWordsSorted: ObservableList<String> = SortedList(ignoredWords, String.CASE_INSENSITIVE_ORDER)

    private val allProcessedWords: ObservableList<String> = FXCollections.observableArrayList()

    private var currentWordsFile: Path? = null

    private val toolBarController = ToolBarController(this)
    private val settingsPane = SettingsPane()

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
        val doOnWordChanging: (card: CardWordEntry)->Unit = { markDocumentIsDirty(); Platform.runLater { reanalyzeOnlyWords(listOf(it)) } }

        pane.fromColumn.addEventHandler(TableColumn.editCommitEvent<CardWordEntry,String>()) { doOnWordChanging(it.rowValue) }
        pane.toColumn.addEventHandler(TableColumn.editCommitEvent<CardWordEntry,String>())   { doOnWordChanging(it.rowValue) }

        pane.warnAboutMissedBaseWordsModeDropDown.onAction = EventHandler { reanalyzeAllWords() }

        // It is needed if SortedList is used as TableView items
        // ??? just needed :-) (otherwise warning in console)
        //currentWordsSorted.comparatorProperty().bind(currentWordsList.comparatorProperty());

        fixSortingAfterCellEditCommit(pane.fromColumn)


        toolBarController.fillToolBar(pane.toolBar)

        pane.topPane.children.add(0, MenuController(this).fillMenu())
        pane.topPane.children.add(settingsPane)
        currentWordsList.contextMenu = ContextMenuController(this).fillContextMenu()

        currentWordsList.selectionModel.selectedItemProperty().addListener { _, _, _ ->
            Platform.runLater { if (settingsPane.playWordOnSelect) playSelectedWord() } }


        loadExistentWords()
    }

    private val audioPlayer = JavaFxSoundPlayer(PlayingMode.Async)

    internal fun playSelectedWord() = currentWordsList.singleSelection?.let { playFrom(it) }

    private fun playFrom(card: CardWordEntry) {
        val voice = settingsPane.voice
        if (voice.synthesizer == PredefSpeechSynthesizer.MarryTTS) {

            val config: MarryTtsSpeechConfig? = PredefinedMarryTtsSpeechConfig.values()
                .find { it.config.voice_Selections == voice.voice || it.config.voice == voice.voice }
                ?.config

            requireNotNull(config) { "MarryTTS config for $voice is not found." }

            MarryTtsSpeechSynthesizer(config, audioPlayer).speak(card.from.trim())
        }
    }

    internal val currentWordsList: TableView<CardWordEntry> get() = pane.currentWordsList
    //internal val currentWords: ObservableList<CardWordEntry> get() = pane.currentWordsList.items
    private val currentWordsSelection: TableView.TableViewSelectionModel<CardWordEntry> get() = currentWordsList.selectionModel
    private val currentWarnAboutMissedBaseWordsMode: WarnAboutMissedBaseWordsMode get() = pane.warnAboutMissedBaseWordsModeDropDown.value

    private fun markDocumentIsDirty() { this.documentIsDirty = true }


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
        currentWordsSelection.selectedItems
            .doIfNotEmpty { cards ->
                ignoreNoBaseWordInSet(cards)
                recalculateWarnedWordsCount()
                markDocumentIsDirty()
            }

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
                    currentWordsSelection.select(newBaseWordCard)
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

    private fun recalculateWarnedWordsCount() {
        val wordCountWithWarning = currentWords.count { it.hasWarning }
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

    private fun doLoadAction(loadAction: (Path)->LoadType) {

        validateCurrentDocumentIsSaved("Open file")

        val fc = FileChooser()
        fc.title = "Select words file"
        fc.initialDirectory = dictDirectory.toFile()
        fc.extensionFilters.add(FileChooser.ExtensionFilter("Words file", "*.csv", "*.words", "*.txt", "*.srt"))

        val file = fc.showOpenDialog(pane.scene.window)

        if (file != null) {
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
                    this.documentIsDirty = false
                }
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
        val windowTitle = if (filePath == null) appTitle else "$appTitle - ${filePath.name}"
        setWindowTitle(pane, windowTitle)
    }

    fun newDocument() {
        validateCurrentDocumentIsSaved("New document")
        currentWords.clear()
    }

    fun loadWordsFromFile() {
        doLoadAction { filePath ->
            val fileExt = filePath.extension.lowercase()
            val words: List<CardWordEntry> = when (fileExt) {
                "txt"          -> loadWords(filePath).map { CardWordEntry(it, "") }
                "csv", "words" -> loadWordCards(filePath)
                "srt"          -> extractWordsFromFile(filePath, ignoredWords)
                else           -> throw IllegalArgumentException("Unexpected file extension [${filePath}]")
            }
            // T O D O: make it async, but it is not easy because there are change listeners which also call analyzeAllWords()
            // We need to do ANY/EVERY call to analyzeAllWords() async
            .also { analyzeAllWords(it) }

            currentWords.setAll(words)
            currentWordsList.sort()

            if (filePath.isInternalCsvFormat) LoadType.Open else LoadType.Import
        }
    }

    fun loadFromClipboard() {
        val words = extractWordsFromClipboard(Clipboard.getSystemClipboard(), ignoredWords)

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
            val newCardWordEntry = CardWordEntry("", "")

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
        saveWordCards(filePath.useFilenameSuffix(internalWordCardsFileExt), CsvFormat.Internal, words)
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

        this.documentIsDirty = false
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
                log.error("${ex.message}", ex)
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
