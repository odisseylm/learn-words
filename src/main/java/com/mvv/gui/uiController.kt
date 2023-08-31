package com.mvv.gui

import com.mvv.gui.dictionary.AutoDictionariesLoader
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
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.*
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.name


private val log = mu.KotlinLogging.logger {}


class LearnWordsController (
    private val mainPane: MainWordsPane,
) {

    //private val projectDirectory = getProjectDirectory(this.javaClass)

    private val allDictionaries: List<Dictionary> = AutoDictionariesLoader().load() // HardcodedDictionariesLoader().load()
    private val dictionaryComposition = DictionaryComposition(allDictionaries)

    private val currentWords: ObservableList<CardWordEntry> = FXCollections.observableArrayList()
    //private val currentWordsSorted: SortedList<CardWordEntry> = SortedList(currentWords, cardWordEntryComparator)

    private val ignoredWords: ObservableList<String> = FXCollections.observableArrayList()
    private val ignoredWordsSorted: ObservableList<String> = SortedList(ignoredWords, String.CASE_INSENSITIVE_ORDER)

    private val allProcessedWords: ObservableList<String> = FXCollections.observableArrayList()

    private var currentWordsFile: Path? = null

    init {

        log.info("Used dictionaries\n" +
                allDictionaries.mapIndexed { i, d -> "${i + 1} $d" }.joinToString("\n") +
                "\n---------------------------------------------------\n\n"
        )

        val currentWordsLabelText = "File/Clipboard (%d words)"
        currentWords.addListener(ListChangeListener { mainPane.currentWordsLabel.text = currentWordsLabelText.format(it.list.size) })

        currentWords.addListener(ListChangeListener { analyzeWordCards(currentWords) })

        val ignoredWordsLabelText = "Ignored words (%d)"
        ignoredWords.addListener(ListChangeListener { mainPane.ignoredWordsLabel.text = ignoredWordsLabelText.format(it.list.size) })


        mainPane.ignoredWordsList.items = ignoredWordsSorted

        val allProcessedWordsLabelText = "All processed words (%d)"
        allProcessedWords.addListener(ListChangeListener { mainPane.allProcessedWordsLabel.text = allProcessedWordsLabelText.format(it.list.size) })

        mainPane.allProcessedWordsList.items = SortedList(allProcessedWords, String.CASE_INSENSITIVE_ORDER)


        mainPane.currentWordsList.items = currentWords // Sorted
        //currentWordsList.setComparator(cardWordEntryComparator)

        addKeyBindings(currentWordsList, copyKeyCombinations.associateWith { {
            if (!currentWordsList.isEditing) copySelectedWord() } })

        mainPane.sceneProperty().addListener { _, _, newScene -> newScene?.let { addKeyBindings(it) } }

        mainPane.removeIgnoredButton.onAction = EventHandler { removeIgnoredFromCurrentWords() }


        mainPane.fromColumn.isSortable = true
        mainPane.fromColumn.sortType = TableColumn.SortType.ASCENDING
        mainPane.fromColumn.comparator = String.CASE_INSENSITIVE_ORDER

        currentWordsList.sortOrder.add(mainPane.fromColumn)

        // It is needed if SortedList is used as TableView items
        // ??? just needed :-) (otherwise warning in console)
        //currentWordsSorted.comparatorProperty().bind(currentWordsList.comparatorProperty());

        fixSortingAfterCellEditCommit(mainPane.fromColumn)


        ToolBarController(this).fillToolBar(mainPane.toolBar)

        currentWordsList.contextMenu = ContextMenu()
        ContextMenuController(this).fillContextMenu(currentWordsList.contextMenu)


        loadExistentWords()
    }

    internal val currentWordsList: TableView<CardWordEntry> get() = mainPane.currentWordsList


    private fun addKeyBindings(newScene: Scene) {
        newScene.accelerators[KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN)] = Runnable { saveAll() }
        newScene.accelerators[KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN)] = Runnable { loadWordsFromFile() }
        newScene.accelerators[lowerCaseKeyCombination] = Runnable { toLowerCaseRow() }

        newScene.accelerators[KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.CONTROL_DOWN)] = Runnable { startEditingFrom() }
        newScene.accelerators[KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.CONTROL_DOWN)] = Runnable { startEditingTo() }
        newScene.accelerators[KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.CONTROL_DOWN)] = Runnable { startEditingTranscription() }
        newScene.accelerators[KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.CONTROL_DOWN)] = Runnable { startEditingRemarks() }
    }


    fun isOneOfSelectedWordsHasNoBaseWord(): Boolean =
        currentWordsList.selectionModel.selectedItems.isOneOfSelectedWordsHasNoBaseWord

    fun ignoreNoBaseWordInSet() = ignoreNoBaseWordInSet(currentWordsList.selectionModel.selectedItems)

    fun addAllBaseWordsInSet() = addAllBaseWordsInSetImpl(currentWordsList.items)
    fun addBaseWordsInSetForSelected() = addAllBaseWordsInSetImpl(currentWordsList.selectionModel.selectedItems)

    private fun addAllBaseWordsInSetImpl(wordCards: Iterable<CardWordEntry>) {

        currentWordsList.runWithScrollKeeping {

            val addedWordsMapping = addBaseWordsInSet(wordCards, currentWordsList.items, dictionaryComposition)

            if (addedWordsMapping.size == 1) {
                val newBaseWordCard = addedWordsMapping.values.asSequence().flatten().first()

                // select new base word to edit it immediately
                if (currentWordsList.selectionModel.selectedItems.size <= 1) {
                    currentWordsList.selectionModel.clearSelection()
                    currentWordsList.selectionModel.select(newBaseWordCard)
                }
            }
        }
    }

    fun addTranscriptions() {
        addTranscriptions(currentWordsList.items, dictionaryComposition)
    }

    fun removeSelected() {
        val selectedSafeCopy = currentWordsList.selectionModel.selectedItems.toList()
        currentWordsList.selectionModel.clearSelection()
        currentWordsList.runWithScrollKeeping {
            currentWordsList.items.removeAll(selectedSafeCopy) }
    }

    @Suppress("unused")
    private fun reanalyzeWords() {
        analyzeWordCards(currentWordsList.items)
        //currentWordsList.refresh()
    }

    private fun startEditingFrom() = startEditingColumnCell(mainPane.fromColumn)
    private fun startEditingTo() = startEditingColumnCell(mainPane.toColumn)
    private fun startEditingTranscription() = startEditingColumnCell(mainPane.transcriptionColumn)
    private fun startEditingRemarks() = startEditingColumnCell(mainPane.examplesColumn)

    private fun startEditingColumnCell(column: TableColumn<CardWordEntry, String>) {
        val selectedIndex = currentWordsList.selectionModel.selectedIndex
        if (selectedIndex != -1) currentWordsList.edit(selectedIndex, column)
    }

    fun toLowerCaseRow() {
        if (currentWordsList.isEditing) return

        wordCardsToLowerCaseRow(currentWordsList.selectionModel.selectedItems)

        //currentWordsList.sort()
        //currentWordsList.refresh()
    }

    private fun copySelectedWord() = copyWordsToClipboard(currentWordsList.selectionModel.selectedItems)


    fun translateSelected() {
        dictionaryComposition.translateWords(currentWordsList.selectionModel.selectedItems)
        currentWordsList.refresh()
    }


    fun translateAll() {
        dictionaryComposition.translateWords(currentWords)
        currentWordsList.refresh()
    }

    private fun removeIgnoredFromCurrentWords() {
        val toRemove = currentWords.asSequence()
            .filter { word -> ignoredWordsSorted.contains(word.from) }
            .toList()
        currentWordsList.runWithScrollKeeping { currentWordsList.items.removeAll(toRemove) }
    }

    fun removeWordsFromOtherSetsFromCurrentWords() =
        removeWordsFromOtherSetsFromCurrentWords(currentWordsList.items, this.currentWordsFile)

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

        val file = fc.showOpenDialog(mainPane.scene.window)

        if (file != null) {
            val filePath = file.toPath()
            if (filePath == ignoredWordsFile) {
                showErrorAlert(mainPane, "You cannot open [${ignoredWordsFile.name}].")
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
        setWindowTitle(mainPane, windowTitle)
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
                .also { analyzeWordCards(it) }

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

        val selectedWords = currentWordsList.selectionModel.selectedItems.toList()

        log.debug("selectedWords: {} moved to IGNORED.", selectedWords)

        val newIgnoredWordEntries = selectedWords.filter { !ignoredWordsSorted.contains(it.from) }
        val newIgnoredWords = newIgnoredWordEntries.map { it.from }
        log.debug("newIgnored: {}", newIgnoredWords)
        ignoredWords.addAll(newIgnoredWords)

        currentWordsList.runWithScrollKeeping {
            currentWordsList.selectionModel.clearSelection()
            currentWordsList.items.removeAll(selectedWords)
        }
    }

    fun saveAll() {
        try {
            saveCurrentWords()
            saveIgnored()
        }
        catch (ex: Exception) {
            showErrorAlert(mainPane, "Error of saving words\n${ex.message}")
        }
    }

    enum class InsertPosition { Above, Below }

    fun insertWordCard(insertPosition: InsertPosition) {
        val isWordCardsSetEmpty = currentWordsList.items.isEmpty()
        val currentlySelectedIndex = currentWordsList.selectionModel.selectedIndex

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

                currentWordsList.items.add(positionToInsert, newCardWordEntry)
                currentWordsList.selectionModel.clearAndSelect(positionToInsert, mainPane.fromColumn)
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
                    runLaterWithDelay(50) { currentWordsList.edit(positionToInsert, mainPane.fromColumn) }
                }
            )
        }
    }

    // enum class WordsOrder { ORIGINAL, SORTED }

    private fun saveCurrentWords() = doSaveCurrentWords { filePath ->
        val words = currentWordsList.items
        saveWordCards(filePath.useFilenameSuffix(internalWordCardsFileExt), CsvFormat.Internal, words)
        saveWordCards(filePath.useFilenameSuffix(memoWordFileExt), CsvFormat.MemoWord, words)
    }

    private fun doSaveCurrentWords(saveAction:(Path)->Unit) {

        var filePath: Path? = this.currentWordsFile
        if (filePath == null) {
            filePath = showTextInputDialog(mainPane, "Enter new words filename")
                .map { dictDirectory.resolve(useFileExt(it, internalWordCardsFileExt)) }
                .orElse(null)
        }

        if (filePath == null) {
            showErrorAlert(mainPane, "Filename is not specified.")
            return
        }

        saveAction(filePath)

        // !!! ONLY if success !!!
        updateCurrentWordsFile(filePath)
    }

    fun splitCurrentWords(): Unit = doSaveCurrentWords { filePath ->
        val words = currentWordsList.items
        val df = SimpleDateFormat("yyyyMMdd-HHmmss")
        val splitFilesDir = filePath.parent.resolve("split-${df.format(Date())}")

        val defaultSplitWordCountPerFile = 40
        val strSplitWordCountPerFile = showTextInputDialog(mainPane,
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
                showErrorAlert(mainPane, ex.message ?: "Unknown error", "Error of splitting.")
            }
        }
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
