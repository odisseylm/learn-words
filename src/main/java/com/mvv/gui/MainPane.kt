package com.mvv.gui

import com.mvv.gui.WordCardStatus.BaseWordDoesNotExist
import com.mvv.gui.WordCardStatus.NoBaseWordInSet
import com.mvv.gui.dictionary.*
import com.mvv.gui.dictionary.Dictionary
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.collections.transformation.SortedList
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.*
import javafx.scene.layout.*
import javafx.scene.text.Text
import javafx.stage.FileChooser
import javafx.util.Callback
import org.dict.kernel.DictEngine
import org.dict.kernel.IAnswer
import org.dict.kernel.IDatabase
import org.dict.kernel.IDictEngine
import org.dict.server.DatabaseFactory
import org.dict.server.DictHTMLPrinter
import java.io.File
import java.io.FileReader
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.*
import kotlin.text.Charsets.UTF_8


const val appTitle = "Words"

class MainWordsPane : BorderPane() /*GridPane()*/ {

    //private val projectDirectory = getProjectDirectory(this.javaClass)

    private val allDictionaries: List<Dictionary> = AutoDictionariesLoader().load() // HardcodedDictionariesLoader().load()
    private val dictionaryComposition = DictionaryComposition(allDictionaries)

    private val currentWords: ObservableList<CardWordEntry> = FXCollections.observableArrayList()
    //private val currentWordsSorted: SortedList<CardWordEntry> = SortedList(currentWords, cardWordEntryComparator)

    private val ignoredWords: ObservableList<String> = FXCollections.observableArrayList()
    private val ignoredWordsSorted: ObservableList<String> = SortedList(ignoredWords, String.CASE_INSENSITIVE_ORDER)

    private val allProcessedWords: ObservableList<String> = FXCollections.observableArrayList()

    private val ignoredWordsFile = dictDirectory.resolve(ignoredWordsFilename)
    private var currentWordsFile: Path? = null

    private val currentWordsList = TableView<CardWordEntry>()
    private val fromColumn = TableColumn<CardWordEntry, String>("English")
    private val wordCardStatusesColumn = TableColumn<CardWordEntry, Set<WordCardStatus>>("St")
    private val toColumn = TableColumn<CardWordEntry, String>("Russian")
    private val translationCountColumn = TableColumn<CardWordEntry, Int>("n")
    private val transcriptionColumn = TableColumn<CardWordEntry, String>("transcription")
    private val examplesColumn = TableColumn<CardWordEntry, String>("examples")

    private val ignoredWordsList = ListView<String>()
    private val allProcessedWordsList = ListView<String>()

    init {

        println("Used dictionaries")
        allDictionaries.forEachIndexed { i, d -> println("${i + 1} $d") }
        println("---------------------------------------------------\n\n")

        this.stylesheets.add("spreadsheet.css")

        val contentPane = GridPane()

        val maxListViewWidth = 5000.0 // TODO: move to CSS
        val maxButtonWidth = 500.0 // TODO: move to CSS

        contentPane.alignment = Pos.CENTER
        contentPane.hgap = 10.0; contentPane.vgap = 10.0
        contentPane.padding = Insets(10.0, 10.0, 10.0, 10.0)

        val currentWordsLabelText = "File/Clipboard (%d words)"
        val currentWordsLabel = Text(currentWordsLabelText.format(0))
        contentPane.add(currentWordsLabel, 0, 0)

        currentWords.addListener(ListChangeListener { currentWordsLabel.text = currentWordsLabelText.format(it.list.size) })
        currentWords.addListener(ListChangeListener { analyzeWordCards(currentWords) })

        contentPane.add(currentWordsList, 0, 1, 1, 3)
        currentWordsList.maxWidth = maxListViewWidth
        GridPane.setFillWidth(currentWordsList, true)
        GridPane.setHgrow(currentWordsList, Priority.ALWAYS)
        currentWordsList.selectionModel.selectionMode = SelectionMode.MULTIPLE


        val toolBar = ToolBar()
        this.top = toolBar

        val buttonsMiddleBar = VBox(5.0)
        buttonsMiddleBar.isFillWidth = true


        val ignoreButton = newButton("To ignore >>", buttonIcon("icons/rem_all_co.png")) { moveToIgnored() }
        ignoreButton.maxWidth = maxButtonWidth
        buttonsMiddleBar.children.add(ignoreButton)

        val removeIgnoredButton = newButton("Remove ignored") { removeIgnoredFromCurrentWords() }
        removeIgnoredButton.maxWidth = maxButtonWidth
        buttonsMiddleBar.children.add(removeIgnoredButton)

        contentPane.add(buttonsMiddleBar, 1, 1)


        val ignoredWordsLabelText = "Ignored words (%d)"
        val ignoredWordsLabel = Text(ignoredWordsLabelText.format(0))
        contentPane.add(ignoredWordsLabel, 2, 0)

        ignoredWords.addListener(ListChangeListener { ignoredWordsLabel.text = ignoredWordsLabelText.format(it.list.size) })

        ignoredWordsList.items = ignoredWordsSorted
        contentPane.add(ignoredWordsList, 2, 1)
        ignoredWordsList.maxWidth = maxListViewWidth
        GridPane.setFillWidth(ignoredWordsList, true)
        GridPane.setHgrow(ignoredWordsList, Priority.ALWAYS)
        ignoredWordsList.selectionModel.selectionMode = SelectionMode.MULTIPLE

        val allProcessedWordsLabelText = "All processed words (%d)"
        val allProcessedWordsLabel = Text(allProcessedWordsLabelText.format(0))
        contentPane.add(allProcessedWordsLabel, 2, 2)

        allProcessedWords.addListener(ListChangeListener { allProcessedWordsLabel.text = allProcessedWordsLabelText.format(it.list.size) })

        allProcessedWordsList.items = SortedList(allProcessedWords, String.CASE_INSENSITIVE_ORDER)
        contentPane.add(allProcessedWordsList, 2, 3)
        allProcessedWordsList.maxWidth = maxListViewWidth
        GridPane.setFillWidth(allProcessedWordsList, true)
        GridPane.setHgrow(allProcessedWordsList, Priority.ALWAYS)


        currentWordsList.isEditable = true

        fromColumn.isEditable = true
        fromColumn.cellValueFactory = Callback { p -> p.value.fromProperty }
        //fromColumn.cellFactory = TextFieldTableCell.forTableColumn()
        fromColumn.cellFactory = ExTextFieldTableCell.forStringTableColumn(ExTextFieldTableCell.TextFieldType.TextField)

        toColumn.isEditable = true
        toColumn.cellValueFactory = PropertyValueFactory("to")
        toColumn.cellValueFactory = Callback { p -> p.value.toProperty }
        toColumn.cellFactory = ExTextFieldTableCell.forStringTableColumn(ExTextFieldTableCell.TextFieldType.TextArea)

        // alternative approach
        //toColumn.cellValueFactory = PropertyValueFactory("to")
        //toColumn.cellFactory = MultilineTextFieldTableCell.forStringTableColumn { toText, card -> card.to = toText }

        translationCountColumn.isEditable = false
        translationCountColumn.cellValueFactory = Callback { p -> p.value.translationCountProperty }

        translationCountColumn.cellFactory = LabelStatusTableCell.forTableColumn { cell, _, translationCount ->
            val translationCountStatus = translationCount?.toTranslationCountStatus ?: TranslationCountStatus.Ok
            cell.styleClass.removeAll(TranslationCountStatus.allCssClasses)
            cell.styleClass.add("TranslationCountStatus-${translationCountStatus.name}")
        }

        transcriptionColumn.isEditable = true
        transcriptionColumn.cellValueFactory = PropertyValueFactory("transcription")
        transcriptionColumn.cellValueFactory = Callback { p -> p.value.transcriptionProperty }
        transcriptionColumn.cellFactory = ExTextFieldTableCell.forStringTableColumn(ExTextFieldTableCell.TextFieldType.TextField)

        examplesColumn.isEditable = true
        examplesColumn.cellValueFactory = PropertyValueFactory("examples")
        examplesColumn.cellValueFactory = Callback { p -> p.value.examplesProperty }
        examplesColumn.cellFactory = ExTextFieldTableCell.forStringTableColumn(ExTextFieldTableCell.TextFieldType.TextArea)


        val icon = Image("icons/exclamation-1.png")

        // Seems it is not allowed to share ImageView instance (between different cells rendering)
        // It causes disappearing/erasing icons in table view during scrolling
        // Most probably it is a bug or probably feature :-) */
        //
        // val iconView = ImageView(icon)

        wordCardStatusesColumn.isEditable = false
        wordCardStatusesColumn.cellValueFactory = PropertyValueFactory("wordCardStatuses")
        wordCardStatusesColumn.cellValueFactory = Callback { p -> p.value.wordCardStatusesProperty }

        wordCardStatusesColumn.cellFactory = LabelStatusTableCell.forTableColumn(EmptyTextStringConverter()) { cell, card, value ->
            cell.styleClass.removeAll(WordCardStatus.allCssClasses)

            val toolTips = mutableListOf<String>()
            val wordCardStatuses = value ?: emptySet()

            if (NoBaseWordInSet in wordCardStatuses && BaseWordDoesNotExist !in wordCardStatuses) {
                toolTips.add(NoBaseWordInSet.toolTipF(card))
                cell.styleClass.add(NoBaseWordInSet.cssClass)

                // Setting icon in CSS does not work. See my other comments regarding it.
                cell.graphic = ImageView(icon)
            }
            else {
                cell.graphic = null
            }

            val toolTipText = toolTips.joinToString("\n").trimToNull()
            cell.toolTipText = toolTipText
        }

        // TODO: move to CSS
        fromColumn.prefWidth = 200.0
        wordCardStatusesColumn.prefWidth = 50.0
        toColumn.prefWidth = 400.0
        translationCountColumn.prefWidth = 50.0
        transcriptionColumn.prefWidth = 200.0
        examplesColumn.prefWidth = 200.0


        currentWordsList.items = currentWords // Sorted
        //currentWordsList.setComparator(cardWordEntryComparator)
        currentWordsList.columns.setAll(fromColumn, wordCardStatusesColumn, toColumn, translationCountColumn, transcriptionColumn, examplesColumn)
        currentWordsList.sortOrder.add(fromColumn)

        currentWordsList.id = "currentWords"

        addKeyBindings(currentWordsList, copyKeyCombinations.associateWith { {
            if (!currentWordsList.isEditing) copySelectedWord() } })

        fromColumn.isSortable = true
        fromColumn.sortType = TableColumn.SortType.ASCENDING
        fromColumn.comparator = String.CASE_INSENSITIVE_ORDER

        // It is needed if SortedList is used as TableView items
        // ??? just needed :-) (otherwise warning in console) TODO: ???
        //currentWordsSorted.comparatorProperty().bind(currentWordsList.comparatorProperty());

        fixSortingAfterCellEditCommit(fromColumn)


        this.center = contentPane

        this.sceneProperty().addListener { _, _, newScene -> newScene?.let { addKeyBindings(it) } }

        fillToolBar(toolBar)
        addContextMenu()

        loadExistentWords()
    }

    private fun addKeyBindings(newScene: Scene) {
        newScene.accelerators[KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN)] = Runnable { saveAll() }
        newScene.accelerators[KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN)] =
            Runnable { loadWordsFromFile() }
        newScene.accelerators[lowerCaseKeyCombination] = Runnable { toLowerCaseRow() }

        newScene.accelerators[KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.CONTROL_DOWN)] = Runnable { startEditingFrom() }
        newScene.accelerators[KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.CONTROL_DOWN)] = Runnable { startEditingTo() }
    }

    private fun addContextMenu() {
        val ignoreNoBaseWordMenuItem = newMenuItem("Ignore 'No base word'", "Ignore warning 'no base word in set'", buttonIcon("/icons/skip_brkp.png")) { ignoreNoBaseWordInSet() }

        val menu = ContextMenu(
            newMenuItem("Insert above", buttonIcon("/icons/insertAbove-01.png")) { insertWordCard(InsertPosition.Above) },
            newMenuItem("Insert below", buttonIcon("/icons/insertBelow-01.png")) { insertWordCard(InsertPosition.Below) },
            newMenuItem("Lower case", buttonIcon("/icons/toLowerCase.png"), lowerCaseKeyCombination)   { toLowerCaseRow() },
            newMenuItem("To ignore >>", buttonIcon("icons/rem_all_co.png")) { moveToIgnored() },
            newMenuItem("Translate selected", buttonIcon("icons/forward_nav.png"), translateSelectedKeyCombination) { translateSelected() },
            ignoreNoBaseWordMenuItem,
        )

        menu.onShowing = EventHandler {
            updateIgnoreNoBaseWordMenuItem(ignoreNoBaseWordMenuItem)
        }

        currentWordsList.contextMenu = menu
    }

    private fun fillToolBar(toolBar: ToolBar) {
        val controls = listOf(
            newButton("Load file", buttonIcon("/icons/open16x16.gif")) { loadWordsFromFile() },
            newButton("Parse text from clipboard", buttonIcon("/icons/paste.gif" /*paste-3388622.png"*/, -1.0)) { loadFromClipboard() },
            newButton("Save All", buttonIcon("/icons/disks.png", -1.0)) { saveAll() },
            newButton("Translate", buttonIcon("/icons/forward_nav.png")) { translateAll() },
            newButton("Split", buttonIcon("/icons/slidesstack.png")) { splitCurrentWords() },
            Label("  "),
            newButton("Insert above", buttonIcon("/icons/insertAbove-01.png")) { insertWordCard(InsertPosition.Below) },
            newButton("Insert below", buttonIcon("/icons/insertBelow-01.png")) { insertWordCard(InsertPosition.Below) },
            Label("  "),
            newButton("No base word", "Ignore warning 'no base word in set'", buttonIcon("/icons/skip_brkp.png")) {
                ignoreNoBaseWordInSet() }

            // For testing
            //
            //Label("  "),
            //newButton("Reanalyze") { reanalyzeWords() },
            //newButton("Refresh table") { currentWordsList.refresh() },
        )

        toolBar.items.addAll(controls)
    }

    private fun updateIgnoreNoBaseWordMenuItem(ignoreNoBaseWordMenuItem: MenuItem) {
        val oneOfSelectedWordsHasNoBaseWord = isOneOfSelectedWordsHasNoBaseWord()
        ignoreNoBaseWordMenuItem.isVisible = oneOfSelectedWordsHasNoBaseWord

        val selectedCards = currentWordsList.selectionModel.selectedItems
        val ignoreNoBaseWordMenuItemText =
            if (oneOfSelectedWordsHasNoBaseWord && selectedCards.size == 1)
                "Ignore no base words [${possibleEnglishBaseWords(selectedCards[0].from).joinToString("|")}]"
            else "Ignore 'No base word'"
        ignoreNoBaseWordMenuItem.text = ignoreNoBaseWordMenuItemText
    }

    private fun isOneOfSelectedWordsHasNoBaseWord(): Boolean =
        currentWordsList.selectionModel.selectedItems
            .any { !it.wordCardStatuses.contains(BaseWordDoesNotExist) && it.wordCardStatuses.contains(NoBaseWordInSet) }

    private fun ignoreNoBaseWordInSet() =
        currentWordsList.selectionModel.selectedItems.forEach {
            updateSetProperty(it.wordCardStatusesProperty, BaseWordDoesNotExist, UpdateSet.Set) }

    @Suppress("unused")
    private fun reanalyzeWords() {
        analyzeWordCards(currentWordsList.items)
        currentWordsList.refresh()
    }

    private fun startEditingFrom() {
        val selectedIndex = currentWordsList.selectionModel.selectedIndex
        if (selectedIndex != -1) currentWordsList.edit(selectedIndex, fromColumn)
    }

    private fun startEditingTo() {
        val selectedIndex = currentWordsList.selectionModel.selectedIndex
        if (selectedIndex != -1) currentWordsList.edit(selectedIndex, toColumn)
    }

    private fun toLowerCaseRow() {
        if (currentWordsList.isEditing) return

        currentWordsList.selectionModel.selectedItems.forEach {
            it.from = it.from.lowercase()
            it.to = it.to.lowercase()
        }
        currentWordsList.sort()
        currentWordsList.refresh()
    }

    private fun copySelectedWord() {

        val wordCardsAsString = currentWordsList.selectionModel.selectedItems
            .joinToString("\n") { "${it.from}  ${it.to}" }

        val clipboardContent = ClipboardContent()
        clipboardContent.putString(wordCardsAsString)
        Clipboard.getSystemClipboard().setContent(clipboardContent)
    }


    private fun translateSelected() {
        translateImpl(currentWordsList.selectionModel.selectedItems)
        currentWordsList.refresh()
    }


    private fun translateAll() {
        translateImpl(currentWords)
        currentWordsList.refresh()
    }


    private fun translateImpl(words: Iterable<CardWordEntry>) =
        words
            .filter  { it.from.isNotBlank() }
            .forEach {
                if (it.to.isBlank()) {
                    val translation = dictionaryComposition.find(it.from.trim())
                    it.to = translation.translations.joinToString("\n")
                    it.transcription = translation.transcription ?: ""
                    it.examples = extractExamples(translation)
                }
            }



    private fun removeIgnoredFromCurrentWords() {
        val toRemove = currentWords.asSequence()
            .filter { word -> ignoredWordsSorted.contains(word.from) }
            .toList()
        currentWords.removeAll(toRemove)
    }

    private fun loadWordsFromFile() {

        val fc = FileChooser()
        fc.title = "Select words file"
        fc.initialDirectory = dictDirectory.toFile()
        fc.extensionFilters.add(FileChooser.ExtensionFilter("Words file", "*.csv", "*.words", "*.txt", "*.srt"))

        val file = fc.showOpenDialog(this.scene.window)

        if (file != null) {
            val filePath = file.toPath()

            if (filePath == ignoredWordsFile) {
                showErrorAlert(this, "You cannot open [${ignoredWordsFile.name}].")
                return
            }

            val fileExt = filePath.extension.lowercase()
            val words: List<CardWordEntry> = when (fileExt) {
                "txt" ->
                    loadWords(filePath)
                        .map { CardWordEntry(it, "") }
                        .also { analyzeWordCards(it) }
                "csv", "words" ->
                    loadWordEntries(filePath)
                        .also { analyzeWordCards(it) }
                "srt" ->
                    extractWordsFromFile(filePath)
                        .also { analyzeWordCards(it) }
                else ->
                    throw IllegalArgumentException("Unexpected file extension [${filePath}]")
            }

            currentWords.setAll(words)
            //currentWordsList.refresh()
            currentWordsList.sort()

            if (fileExt == "csv") {
                // TODO: separate to 2 functions, this 1st branch should be called as Open
                // !!! Only if success !!!
                this.currentWordsFile = filePath
                setWindowTitle(this, "$appTitle - ${filePath.name}")
            }
            else {
                // TODO: separate to 2 functions, this 2nd branch should be called as Import
            }
        }
    }

    private fun extractWordsFromFile(filePath: Path): List<CardWordEntry> {
        val r = FileReader(filePath.toFile(), UTF_8)
        return r.use { extractWordsFromText(r.readText()).also { analyzeWordCards(it) } } // T O D O: would be better to pass lazy CharSequence instead of loading full text as String
    }

    private fun loadFromClipboard() {

        val clipboard: Clipboard = Clipboard.getSystemClipboard()
        val content = clipboard.getContent(DataFormat.PLAIN_TEXT)

        println("clipboard content: [${content}]")

        if (content == null) return

        val words = extractWordsFromText(content.toString())
            .also { analyzeWordCards(it) }

        println("clipboard content as words: $words")

        currentWords.setAll(words)
        currentWordsFile = null
        setWindowTitle(this, appTitle)
    }

    private fun extractWordsFromText(content: CharSequence): List<CardWordEntry> =
        TextParser()
            .parse(content)
            .asSequence()
            .filter { !ignoredWordsSorted.contains(it) }
            .map { CardWordEntry(it, "") }
            .toList()

    private fun moveToIgnored() {

        val selectedWords = currentWordsList.selectionModel.selectedItems
            .stream().toList()

        println("selectedWords: $selectedWords")

        val newIgnoredWordEntries = selectedWords.filter { !ignoredWordsSorted.contains(it.from) }
        val newIgnoredWords = newIgnoredWordEntries.map { it.from }
        println("newIgnored: $newIgnoredWords")
        ignoredWords.addAll(newIgnoredWords)

        // T O D O: in case of big words count it may be inefficient!!! It would be nice to improve it!
        currentWords.removeAll(newIgnoredWordEntries)
        currentWordsList.selectionModel.clearSelection()
    }

    private fun saveAll() {
        try {
            saveCurrentWords(WordsOrder.SORTED)
            saveIgnored()
        }
        catch (ex: Exception) {
            showErrorAlert(this, "Error of saving words\n${ex.message}")
        }
    }

    enum class InsertPosition { Above, Below }

    private fun insertWordCard(insertPosition: InsertPosition) {
        val noWordCards = currentWordsList.items.isEmpty()
        val currentlySelectedIndex = currentWordsList.selectionModel.selectedIndex

        val positionToInsert = when {
            noWordCards -> 0

            currentlySelectedIndex != -1 -> when (insertPosition) {
                    InsertPosition.Above -> currentlySelectedIndex
                    InsertPosition.Below -> currentlySelectedIndex + 1
                }

            else -> -1
        }

        if (positionToInsert != -1) {
            val newCardWordEntry = CardWordEntry("", "")

            // TODO: keep current scroll position

            currentWordsList.items.add(positionToInsert, newCardWordEntry)

            if (noWordCards) currentWordsList.selectionModel.select(newCardWordEntry)

            // Bug in JavaFX: without runLaterWithDelay() editing cell does not get focus (you need to click on it or use Tab key)
            // if column cells were not present before (if TableView did not have content yet).
            // Platform.runLater() also does not help.
            //
            runLaterWithDelay(50) {
                if (!currentWordsList.isFocusVisible) currentWordsList.scrollTo(newCardWordEntry)
                currentWordsList.edit(positionToInsert, fromColumn)
            }
        }
    }

    private fun splitCurrentWords() {

        // TODO: refactor, extract duplicated code

        val words = currentWordsList.items

        var currentWordsFile: Path? = this.currentWordsFile // TODO: remove shading var name

        if (currentWordsFile == null) {
            currentWordsFile = showTextInputDialog(this, "Enter new words filename")
                .map { dictDirectory.resolve(addWordsFileExtIfNeeded(it, cardWordsFileExt)) }
                .orElse(null)
        }

        if (currentWordsFile == null) {
            showErrorAlert(this, "Filename is not specified.")
            return
        }

        saveSplitWords(currentWordsFile, words, 30)

        // !!! ONLY if success !!!
        this.currentWordsFile = currentWordsFile
        setWindowTitle(this, "$appTitle - ${currentWordsFile.name}")
    }

    enum class WordsOrder { ORIGINAL, SORTED }

    @Suppress("SameParameterValue")
    private fun saveCurrentWords(wordsOrder: WordsOrder) {
        //val words = if (wordsOrder == WordsOrder.SORTED) currentWordsSorted else currentWords
        //val words = if (wordsOrder == WordsOrder.SORTED) currentWordsList.items else currentWordsList.items
        val words = currentWordsList.items

        var currentWordsFile: Path? = this.currentWordsFile // TODO: remove shading var name

        if (currentWordsFile == null) {
            currentWordsFile = showTextInputDialog(this, "Enter new words filename")
                .map { dictDirectory.resolve(addWordsFileExtIfNeeded(it, cardWordsFileExt)) }
                .orElse(null)
            //val newFileNameDialog = TextInputDialog("")
            //newFileNameDialog.headerText = "Enter new words filename"
            //currentWordsFile = newFileNameDialog.showAndWait()
            //    .map { dictDirectory.resolve(addWordsFileExtIfNeeded(it)) }
            //    .orElse(null)
        }

        if (currentWordsFile == null) {
            showErrorAlert(this, "Filename is not specified.")
            return
        }

        saveWordEntries(currentWordsFile.useFilenameSuffix(cardWordsFileExt), CsvFormat.Standard, words)
        saveWordEntries(currentWordsFile.useFilenameSuffix(memoWorldCardWordsFileExt), CsvFormat.MemoWorld, words)

        // !!! ONLY if success !!!
        this.currentWordsFile = currentWordsFile
        setWindowTitle(this, "$appTitle - ${currentWordsFile.name}")
    }

    @Suppress("SameParameterValue")
    private fun addWordsFileExtIfNeeded(wordsFilename: String, wordsFileExt: String): String =
        if (wordsFilename.endsWith(wordsFileExt)) wordsFilename else wordsFilename + wordsFileExt

    private fun saveIgnored() {
        val ignoredWords = this.ignoredWordsSorted
        if (ignoredWords.isNotEmpty()) {
            saveWordsToTxtFile(ignoredWordsFile, ignoredWords)
        }
    }

    private fun loadExistentWords() {
        loadIgnored()
        loadAllExistentDictionaries()
    }

    private fun loadAllExistentDictionaries() {

        if (dictDirectory.notExists()) return

        val allWordsFilesExceptIgnored = Files.list(dictDirectory)
            .filter { it.isRegularFile() }
            //.filter { it != ignoredWordsFile }
            .filter { it.name.endsWith(cardWordsFileExt) }
            .toList()

        val allWords = allWordsFilesExceptIgnored
            .asSequence()
            .map { loadWordEntries(it) }
            .flatMap { it }
            .map { it.from }
            .distinctBy { it.lowercase() }
            .toList()

        this.allProcessedWords.setAll(allWords)
    }

    private fun loadIgnored() {
        if (ignoredWordsFile.exists()) {
            val ignoredWords = loadWords(ignoredWordsFile)
            this.ignoredWords.setAll(ignoredWords)
        }
    }

}



fun main() {

    //val aa = DictLoadFileProcessor()
    //aa.add(FileObjectImpl())
    //aa.process();

    //val aa = org.dict.kernel.DictEngine()
    //val dbF = DatabaseFactory()
    //dbF.



    val dictRootDir = getProjectDirectory(MainWordsPane::class).resolve("dicts/04/mueller-dict-3.1.1").toFile()

    val props = Properties()
    val dbFilename = "mueller-base"
    val dbId = dbFilename

    props.setProperty("${dbFilename}.data", File(dictRootDir, "dict/${dbFilename}.dict.dz").absolutePath)
    props.setProperty("${dbFilename}.index", File(dictRootDir, "dict/${dbFilename}.index").absolutePath)
    props.setProperty("${dbFilename}.encoding", "UTF-8")

    val db = DatabaseFactory.createDatabase(
        dbId,
        dictRootDir,
        props
    )

    println(db)

    val fEngine = DictEngine()
    fEngine.databases = arrayOf(db)

    //db.search()

    val word = "apple"

    //val answers: Array<IAnswer?>? = fEngine.defineMatch(dbId, word, pos, true, IDatabase.STRATEGY_NEAR)
    //println(answers)

    //val strategy = IDatabase.STRATEGY_EXACT // IDatabase.STRATEGY_NEAR

    //println("\n-----------------------------------------------------------------------------------------")
    //val answers2: Array<IAnswer> = fEngine.defineMatch(dbId, word, "2026", false, strategy)
    //printAnswers(fEngine, answers2, word)


    println("\n--------------------------- match STRATEGY_EXACT ----------------------------------------")
    val answersMatchedByStrategyExact: Array<IAnswer> = fEngine.match(dbId, word, IDatabase.STRATEGY_EXACT)
    printAnswers(fEngine, answersMatchedByStrategyExact, word)

    println("\n--------------------------- match STRATEGY_NONE -----------------------------------------")
    val answersMatchedByStrategyNone: Array<IAnswer> = fEngine.match(dbId, word, IDatabase.STRATEGY_NONE)
    printAnswers(fEngine, answersMatchedByStrategyNone, word)


    println("\n--------------------------- defineMatch define=false STRATEGY_EXACT ---------------------")
    val answersByDefineMatchByStrategyExactWithDefineFalse: Array<IAnswer> = fEngine.defineMatch(
        dbId, word, null, false, IDatabase.STRATEGY_EXACT)
    printAnswers(fEngine, answersByDefineMatchByStrategyExactWithDefineFalse, word)

    println("\n--------------------------- defineMatch define=false STRATEGY_NONE ---------------------")
    val answersByDefineMatchByStrategyNoneWithDefineFalse: Array<IAnswer> = fEngine.defineMatch(
        dbId, word, null, false, IDatabase.STRATEGY_NONE)
    printAnswers(fEngine, answersByDefineMatchByStrategyNoneWithDefineFalse, word)

    println("\n--------------------------- defineMatch define=true STRATEGY_EXACT ---------------------")
    val answersByDefineMatchByStrategyExactWithDefineTrue: Array<IAnswer> = fEngine.defineMatch(
        dbId, word, null, true, IDatabase.STRATEGY_EXACT)
    printAnswers(fEngine, answersByDefineMatchByStrategyExactWithDefineTrue, word)

    println("\n--------------------------- defineMatch define=true STRATEGY_NONE ---------------------")
    val answersByDefineMatchByStrategyNoneWithDefineTrue: Array<IAnswer> = fEngine.defineMatch(
        dbId, word, null, true, IDatabase.STRATEGY_NONE)
    printAnswers(fEngine, answersByDefineMatchByStrategyNoneWithDefineTrue, word)


    println("\n--------------------------- define (default) ------------------------------------------")
    val answers3: Array<IAnswer> = fEngine.define(dbId, word)
    printAnswers(fEngine, answers3, word)
}


@Suppress("SameParameterValue")
private fun printAnswers(fEngine: IDictEngine, answers: Array<IAnswer>, word: String) {
    val printer = DictHTMLPrinter.getInstance()
    val outAsWriter = PrintWriter(System.out)
    try { printer.printAnswers(fEngine, answers, outAsWriter, word, "") } catch (ignore: Exception) { }
    outAsWriter.flush()
}



fun <S,T> fixSortingAfterCellEditCommit(column: TableColumn<S,T>) {

    column.addEventHandler(TableColumn.CellEditEvent.ANY) {

        if (it.eventType == TableColumn.editCommitEvent<CardWordEntry,String>()) {

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


@Suppress("unused") // Do not use it during cell rendering (during rendering runtime selection state is not accessible )
private val <S, T> TableCell<S, T>.isRowSelected: Boolean get() =
    this.isSelected || this.tableRow.isSelected
    //   || this.tableView.selectionModel.selectedItems.contains(this.tableRow.item)

