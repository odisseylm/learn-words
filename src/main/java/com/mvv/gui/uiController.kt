package com.mvv.gui

import com.mvv.gui.dictionary.AutoDictionariesLoader
import com.mvv.gui.dictionary.CachedDictionary
import com.mvv.gui.dictionary.Dictionary
import com.mvv.gui.dictionary.DictionaryComposition
import com.mvv.gui.javafx.*
import com.mvv.gui.util.useFileExt
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
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.name


private val log = mu.KotlinLogging.logger {}


class LearnWordsController (
    private val pane: MainWordsPane,
) {

    //private val projectDirectory = getProjectDirectory(this.javaClass)

    private val allDictionaries: List<Dictionary> = AutoDictionariesLoader().load() // HardcodedDictionariesLoader().load()
    internal val dictionary = CachedDictionary(DictionaryComposition(allDictionaries))

    private val currentWords: ObservableList<CardWordEntry> = FXCollections.observableArrayList()
    //private val currentWordsSorted: SortedList<CardWordEntry> = SortedList(currentWords, cardWordEntryComparator)

    private val ignoredWords: ObservableList<String> = FXCollections.observableArrayList()
    private val ignoredWordsSorted: ObservableList<String> = SortedList(ignoredWords, String.CASE_INSENSITIVE_ORDER)

    private val allProcessedWords: ObservableList<String> = FXCollections.observableArrayList()

    private var currentWordsFile: Path? = null

    init {

        initTheme()
        // after possible adding theme CSS
        pane.stylesheets.add("spreadsheet.css")


        log.info("Used dictionaries\n" +
                allDictionaries.mapIndexed { i, d -> "${i + 1} $d" }.joinToString("\n") +
                "\n---------------------------------------------------\n\n"
        )

        pane.dictionary = dictionary

        val currentWordsLabelText = "File/Clipboard (%d words)"
        currentWords.addListener(ListChangeListener { pane.currentWordsLabel.text = currentWordsLabelText.format(it.list.size) })

        currentWords.addListener(ListChangeListener { analyzeWordCards(currentWords, dictionary) })

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
        val reanalyzeChangedWord: (card: CardWordEntry)->Unit = { Platform.runLater {
            analyzeWordCards(listOf(it), currentWords, dictionary) } }

        pane.fromColumn.addEventHandler(TableColumn.editCommitEvent<CardWordEntry,String>()) { reanalyzeChangedWord(it.rowValue) }
        pane.toColumn.addEventHandler(TableColumn.editCommitEvent<CardWordEntry,String>())   { reanalyzeChangedWord(it.rowValue) }

        // It is needed if SortedList is used as TableView items
        // ??? just needed :-) (otherwise warning in console)
        //currentWordsSorted.comparatorProperty().bind(currentWordsList.comparatorProperty());

        fixSortingAfterCellEditCommit(pane.fromColumn)


        ToolBarController(this).fillToolBar(pane.toolBar)

        pane.topPane.children.add(0, MenuController(this).fillMenu())
        currentWordsList.contextMenu = ContextMenuController(this).fillContextMenu()


        loadExistentWords()
    }

    internal val currentWordsList: TableView<CardWordEntry> get() = pane.currentWordsList
    //internal val currentWords: ObservableList<CardWordEntry> get() = pane.currentWordsList.items
    private val currentWordsSelection: TableView.TableViewSelectionModel<CardWordEntry> get() = currentWordsList.selectionModel


    private fun addKeyBindings(newScene: Scene) {
        newScene.accelerators[openKeyCodeCombination] = Runnable { loadWordsFromFile() }
        newScene.accelerators[saveKeyCodeCombination] = Runnable { saveAll() }

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

    fun ignoreNoBaseWordInSet() = ignoreNoBaseWordInSet(currentWordsSelection.selectedItems)

    fun addAllBaseWordsInSet() = addAllBaseWordsInSetImpl(currentWords)
    fun addBaseWordsInSetForSelected() = addAllBaseWordsInSetImpl(currentWordsSelection.selectedItems)

    private fun addAllBaseWordsInSetImpl(wordCards: Iterable<CardWordEntry>) {

        currentWordsList.runWithScrollKeeping {

            val addedWordsMapping = addBaseWordsInSet(wordCards, currentWords, dictionary)

            if (addedWordsMapping.size == 1) {
                val newBaseWordCard = addedWordsMapping.values.asSequence().flatten().first()

                // select new base word to edit it immediately
                if (currentWordsSelection.selectedItems.size <= 1) {
                    currentWordsSelection.clearSelection()
                    currentWordsSelection.select(newBaseWordCard)
                }
            }
        }
    }

    fun addTranscriptions() {
        addTranscriptions(currentWords, dictionary)
    }

    fun removeSelected() {
        val selectedSafeCopy = currentWordsSelection.selectedItems.toList()
        currentWordsSelection.clearSelection()
        currentWordsList.runWithScrollKeeping {
            currentWords.removeAll(selectedSafeCopy) }
    }

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    internal fun reanalyzeWords() {
        analyzeWordCards(currentWords, dictionary)
        //currentWordsList.refresh()
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

        //currentWordsList.sort()
        //currentWordsList.refresh()
    }

    private fun copySelectedWord() = copyWordsToClipboard(currentWordsSelection.selectedItems)


    fun translateSelected() {
        dictionary.translateWords(currentWordsSelection.selectedItems)
        currentWordsList.refresh()
    }


    fun translateAll() {
        dictionary.translateWords(currentWords)
        currentWordsList.refresh()
    }

    private fun removeIgnoredFromCurrentWords() {
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
                LoadType.Import -> { } // or reset current words file ??
                LoadType.Open   ->
                    // !!! Only if success !!!
                    updateCurrentWordsFile(filePath)
            }
        }
    }

    private fun updateCurrentWordsFile(filePath: Path?) {
        this.currentWordsFile = filePath
        val windowTitle = if (filePath == null) appTitle else "$appTitle - ${filePath.name}"
        setWindowTitle(pane, windowTitle)
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
                .also { analyzeWordCards(it, dictionary) }

            currentWords.setAll(words)
            currentWordsList.sort()

            if (filePath.isInternalCsvFormat) LoadType.Open else LoadType.Import
        }
    }

    fun loadFromClipboard() {
        val words = extractWordsFromClipboard(Clipboard.getSystemClipboard(), ignoredWords)

        currentWords.setAll(words) // or add all ??
        reanalyzeWords()
        updateCurrentWordsFile(null)
    }


    fun moveToIgnored() {

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
    }

    private fun doSaveCurrentWords(saveAction:(Path)->Unit) {

        var filePath: Path? = this.currentWordsFile
        if (filePath == null) {
            filePath = showTextInputDialog(pane, "Enter new words filename")
                .map { dictDirectory.resolve(useFileExt(it, internalWordCardsFileExt)) }
                .orElse(null)
        }

        if (filePath == null) {
            showErrorAlert(pane, "Filename is not specified.")
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

        val defaultSplitWordCountPerFile = settings.splitWordCountPerFile
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
            showInfoAlert(pane, "No files to join.")
            return
        }

        if (selectedFiles.size == 1) {
            showInfoAlert(pane, "No sense to use join only one file. Just use load/open action.")
            return
        }

        val words: List<CardWordEntry> = selectedFiles
            .flatMap { loadWordCards(it.toPath()) }
            .also { analyzeWordCards(it, dictionary) }

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
        allProcessedWords.setAll(loadWordsFromAllExistentDictionaries())
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
