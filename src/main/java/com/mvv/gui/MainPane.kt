package com.mvv.gui

import com.mvv.gui.words.WordCardStatus.BaseWordDoesNotExist
import com.mvv.gui.words.WordCardStatus.NoBaseWordInSet
import com.mvv.gui.dictionary.AutoDictionariesLoader
import com.mvv.gui.dictionary.Dictionary
import com.mvv.gui.dictionary.DictionaryComposition
import com.mvv.gui.dictionary.extractExamples
import com.mvv.gui.javafx.*
import com.mvv.gui.javafx.UpdateSet
import com.mvv.gui.util.trimToNull
import com.mvv.gui.javafx.updateSetProperty
import com.mvv.gui.util.useFileExt
import com.mvv.gui.words.*
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
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.FileChooser
import javafx.util.Callback
import mu.KotlinLogging
import java.io.FileReader
import java.nio.file.Files
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.*
import kotlin.io.path.*
import kotlin.streams.asSequence
import kotlin.text.Charsets.UTF_8


const val appTitle = "Words"

private val log = KotlinLogging.logger {}


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

        log.info(
            "Used dictionaries\n" +
            allDictionaries.mapIndexed { i, d -> "${i + 1} $d" }.joinToString("\n") +
            "\n---------------------------------------------------\n\n"
        )

        this.stylesheets.add("dark-theme.css")
        this.stylesheets.add("spreadsheet.css")

        val contentPane = GridPane()

        contentPane.alignment = Pos.CENTER
        contentPane.hgap = 10.0; contentPane.vgap = 10.0
        contentPane.padding = Insets(10.0, 10.0, 10.0, 10.0)

        val currentWordsLabelText = "File/Clipboard (%d words)"
        val currentWordsLabel = Text(currentWordsLabelText.format(0))
        contentPane.add(currentWordsLabel, 0, 0)

        currentWords.addListener(ListChangeListener { currentWordsLabel.text = currentWordsLabelText.format(it.list.size) })
        currentWords.addListener(ListChangeListener { analyzeWordCards(currentWords) })

        contentPane.add(currentWordsList, 0, 1, 1, 3)
        GridPane.setFillWidth(currentWordsList, true)
        GridPane.setHgrow(currentWordsList, Priority.ALWAYS)
        currentWordsList.selectionModel.selectionMode = SelectionMode.MULTIPLE


        val toolBar = ToolBar()
        this.top = toolBar

        val buttonsMiddleBar = VBox(5.0)
        buttonsMiddleBar.isFillWidth = true

        val removeIgnoredButton = newButton("Remove ignored") { removeIgnoredFromCurrentWords() }
        removeIgnoredButton.styleClass.add("middleBarButton")
        buttonsMiddleBar.children.add(removeIgnoredButton)

        contentPane.add(buttonsMiddleBar, 1, 1)


        val ignoredWordsLabelText = "Ignored words (%d)"
        val ignoredWordsLabel = Text(ignoredWordsLabelText.format(0))
        contentPane.add(ignoredWordsLabel, 2, 0)

        ignoredWords.addListener(ListChangeListener { ignoredWordsLabel.text = ignoredWordsLabelText.format(it.list.size) })

        ignoredWordsList.id = "ignoredWords"
        ignoredWordsList.items = ignoredWordsSorted
        contentPane.add(ignoredWordsList, 2, 1)
        //ignoredWordsList.maxWidth = maxListViewWidth
        GridPane.setFillWidth(ignoredWordsList, true)
        GridPane.setHgrow(ignoredWordsList, Priority.ALWAYS)
        ignoredWordsList.selectionModel.selectionMode = SelectionMode.MULTIPLE

        val allProcessedWordsLabelText = "All processed words (%d)"
        val allProcessedWordsLabel = Text(allProcessedWordsLabelText.format(0))
        contentPane.add(allProcessedWordsLabel, 2, 2)

        allProcessedWords.addListener(ListChangeListener { allProcessedWordsLabel.text = allProcessedWordsLabelText.format(it.list.size) })

        allProcessedWordsList.id = "allProcessedWords"
        allProcessedWordsList.items = SortedList(allProcessedWords, String.CASE_INSENSITIVE_ORDER)
        contentPane.add(allProcessedWordsList, 2, 3)
        GridPane.setFillWidth(allProcessedWordsList, true)
        GridPane.setHgrow(allProcessedWordsList, Priority.ALWAYS)

        currentWordsList.id = "currentWords"
        currentWordsList.isEditable = true

        fromColumn.id = "fromColumn"
        fromColumn.isEditable = true
        fromColumn.cellValueFactory = Callback { p -> p.value.fromProperty }
        //fromColumn.cellFactory = TextFieldTableCell.forTableColumn()
        fromColumn.cellFactory = ExTextFieldTableCell.forStringTableColumn(ExTextFieldTableCell.TextFieldType.TextField)

        toColumn.id = "toColumn"
        toColumn.isEditable = true
        toColumn.cellValueFactory = PropertyValueFactory("to")
        toColumn.cellValueFactory = Callback { p -> p.value.toProperty }
        toColumn.cellFactory = ExTextFieldTableCell.forStringTableColumn(ExTextFieldTableCell.TextFieldType.TextArea)

        // alternative approach
        //toColumn.cellValueFactory = PropertyValueFactory("to")
        //toColumn.cellFactory = MultilineTextFieldTableCell.forStringTableColumn { toText, card -> card.to = toText }

        translationCountColumn.id = "translationCountColumn"
        translationCountColumn.isEditable = false
        translationCountColumn.cellValueFactory = Callback { p -> p.value.translationCountProperty }

        translationCountColumn.cellFactory = LabelStatusTableCell.forTableColumn { cell, _, translationCount ->
            val translationCountStatus = translationCount?.toTranslationCountStatus ?: TranslationCountStatus.Ok
            cell.styleClass.removeAll(TranslationCountStatus.allCssClasses)
            cell.styleClass.add("TranslationCountStatus-${translationCountStatus.name}")
        }

        transcriptionColumn.id = "transcriptionColumn"
        transcriptionColumn.isEditable = true
        transcriptionColumn.cellValueFactory = PropertyValueFactory("transcription")
        transcriptionColumn.cellValueFactory = Callback { p -> p.value.transcriptionProperty }
        transcriptionColumn.cellFactory = ExTextFieldTableCell.forStringTableColumn(ExTextFieldTableCell.TextFieldType.TextField)

        examplesColumn.id = "examplesColumn"
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

        wordCardStatusesColumn.id = "wordCardStatusesColumn"
        wordCardStatusesColumn.isEditable = false
        wordCardStatusesColumn.cellValueFactory = PropertyValueFactory("wordCardStatuses")
        wordCardStatusesColumn.cellValueFactory = Callback { p -> p.value.wordCardStatusesProperty }

        wordCardStatusesColumn.cellFactory = LabelStatusTableCell.forTableColumn(EmptyTextStringConverter()) { cell, card, _ ->
            cell.styleClass.removeAll(WordCardStatus.allCssClasses)

            val toolTips = mutableListOf<String>()
            val showNoBaseWordInSet = !card.ignoreNoBaseWordInSet && card.noBaseWordInSet

            if (showNoBaseWordInSet) {
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

        // Impossible to move it to CSS ?!
        fromColumn.prefWidth = 200.0
        wordCardStatusesColumn.prefWidth = 50.0
        toColumn.prefWidth = 400.0
        translationCountColumn.prefWidth = 50.0
        transcriptionColumn.prefWidth = 150.0
        examplesColumn.prefWidth = 400.0


        currentWordsList.items = currentWords // Sorted
        //currentWordsList.setComparator(cardWordEntryComparator)
        currentWordsList.columns.setAll(fromColumn, wordCardStatusesColumn, toColumn, translationCountColumn, transcriptionColumn, examplesColumn)

        currentWordsList.sortOrder.add(fromColumn)


        addKeyBindings(currentWordsList, copyKeyCombinations.associateWith { {
            if (!currentWordsList.isEditing) copySelectedWord() } })

        fromColumn.isSortable = true
        fromColumn.sortType = TableColumn.SortType.ASCENDING
        fromColumn.comparator = String.CASE_INSENSITIVE_ORDER

        // It is needed if SortedList is used as TableView items
        // ??? just needed :-) (otherwise warning in console)
        //currentWordsSorted.comparatorProperty().bind(currentWordsList.comparatorProperty());

        fixSortingAfterCellEditCommit(fromColumn)


        this.center = contentPane

        this.sceneProperty().addListener { _, _, newScene -> newScene?.let { addKeyBindings(it) } }

        fillToolBar(toolBar)
        currentWordsList.contextMenu = fillContextMenu()

        loadExistentWords()
    }

    private fun addKeyBindings(newScene: Scene) {
        newScene.accelerators[KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN)] = Runnable { saveAll() }
        newScene.accelerators[KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN)] =
            Runnable { loadWordsFromFile() }
        newScene.accelerators[lowerCaseKeyCombination] = Runnable { toLowerCaseRow() }

        newScene.accelerators[KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.CONTROL_DOWN)] = Runnable { startEditingFrom() }
        newScene.accelerators[KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.CONTROL_DOWN)] = Runnable { startEditingTo() }
        newScene.accelerators[KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.CONTROL_DOWN)] = Runnable { startEditingTranscription() }
        newScene.accelerators[KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.CONTROL_DOWN)] = Runnable { startEditingRemarks() }
    }

    private fun fillToolBar(toolBar: ToolBar) {
        val controls = listOf(
            newButton("Load file", "Open internal or memo-word csv, or srt file",
                buttonIcon("/icons/open16x16.gif")) { loadWordsFromFile() },
            newButton("From clipboard", "Parse text from clipboard", buttonIcon("/icons/paste.gif" /*paste-3388622.png"*/, -1.0)) { loadFromClipboard() },
            newButton("Save All", "Save current file in internal and memo-word csv format and save ignored words",
                buttonIcon("/icons/disks.png", -1.0)) { saveAll() },
            newButton("Split", "Split current big set to several ones",
                buttonIcon("/icons/slidesstack.png")) { splitCurrentWords() },

            Label("  "),

            newButton("To ignore >>", "Move selected words to ignored",
                buttonIcon("icons/rem_all_co.png")) { moveToIgnored() }
                .also { it.styleClass.add("middleBarButton") },
            newButton("Translate", "Translate all words", buttonIcon("/icons/forward_nav.png")) { translateAll() },
            newButton("Remove words from other sets", "Remove words from other sets") { removeWordsFromOtherSetsFromCurrentWords() },

            Label("  "),

            newButton("Insert above", buttonIcon("/icons/insertAbove-01.png")) { insertWordCard(InsertPosition.Below) }
                .also { it.contentDisplay = ContentDisplay.RIGHT },
            newButton("Insert below", buttonIcon("/icons/insertBelow-01.png")) { insertWordCard(InsertPosition.Below) },

            Label("  "),

            newButton("No base word", "Ignore warning 'no base word in set'.", buttonIcon("/icons/skip_brkp.png")) {
                ignoreNoBaseWordInSet() },
            newButton("Add all missed base words", "Add all possible missed base words.", buttonIcon("/icons/toggleexpand.png")) {
                addAllBaseWordsInSet() },
            newButton("Add transcriptions", "Add missed transcription.", buttonIcon("/icons/transcription-1.png")) {
                addTranscriptions() },

            // For testing
            //
            //Label("  "),
            //newButton("Reanalyze") { reanalyzeWords() },
            //newButton("Refresh table") { currentWordsList.refresh() },
        )

        toolBar.items.addAll(controls)
    }

    private fun fillContextMenu(): ContextMenu {
        val ignoreNoBaseWordMenuItem = newMenuItem("Ignore 'No base word'",
            "Ignore warning 'no base word in set'", buttonIcon("/icons/skip_brkp.png")) {
            ignoreNoBaseWordInSet() }

        val addMissedBaseWordsMenuItem = newMenuItem("Add missed base word",
            buttonIcon("/icons/toggleexpand.png")) { addBaseWordsInSetForSelected() }
        val translateMenuItem = newMenuItem("Translate selected", buttonIcon("icons/forward_nav.png"),
            translateSelectedKeyCombination ) { translateSelected() }

        val menu = ContextMenu(
            newMenuItem("Insert above", buttonIcon("/icons/insertAbove-01.png")) { insertWordCard(InsertPosition.Above) },
            newMenuItem("Insert below", buttonIcon("/icons/insertBelow-01.png")) { insertWordCard(InsertPosition.Below) },
            newMenuItem("Lower case", buttonIcon("/icons/toLowerCase.png"), lowerCaseKeyCombination)   { toLowerCaseRow() },
            newMenuItem("To ignore >>", buttonIcon("icons/rem_all_co.png")) { moveToIgnored() },
            newMenuItem("Remove", buttonIcon("icons/cross-1.png")) { removeSelected() },
            translateMenuItem,
            ignoreNoBaseWordMenuItem,
            addMissedBaseWordsMenuItem,
        )

        menu.onShowing = EventHandler {
            updateIgnoreNoBaseWordMenuItem(ignoreNoBaseWordMenuItem)
            updateAddMissedBaseWordsMenuItem(addMissedBaseWordsMenuItem)
            updateTranslateMenuItem(translateMenuItem)
        }

        return menu
    }

    private fun updateIgnoreNoBaseWordMenuItem(menuItem: MenuItem) {
        val oneOfSelectedWordsHasNoBaseWord = isOneOfSelectedWordsHasNoBaseWord()
        menuItem.isVisible = oneOfSelectedWordsHasNoBaseWord

        val selectedCards = currentWordsList.selectionModel.selectedItems
        val menuItemText =
            if (oneOfSelectedWordsHasNoBaseWord && selectedCards.size == 1)
                "Ignore no base words [${possibleEnglishBaseWords(selectedCards[0].from).joinToString("|")}]"
            else "Ignore 'No base word'"
        menuItem.text = menuItemText
    }

    private fun updateAddMissedBaseWordsMenuItem(menuItem: MenuItem) {
        val oneOfSelectedWordsHasNoBaseWord = isOneOfSelectedWordsHasNoBaseWord()
        menuItem.isVisible = oneOfSelectedWordsHasNoBaseWord

        val selectedCards = currentWordsList.selectionModel.selectedItems
        val menuItemText =
            if (oneOfSelectedWordsHasNoBaseWord && selectedCards.size == 1) {
                val possibleBaseWord = possibleBestEnglishBaseWord(selectedCards[0].from)
                val showBaseWord = if (possibleBaseWord != null && !possibleBaseWord.endsWith('e'))
                    "${possibleBaseWord}(e)" else possibleBaseWord
                "Add base word '$showBaseWord'"
            }
            else "Add missed base word"
        menuItem.text = menuItemText
    }

    private fun updateTranslateMenuItem(menuItem: MenuItem) {
        val oneOfSelectedIsNotTranslated = currentWordsList.selectionModel.selectedItems.any { it.to.isBlank() }
        menuItem.isVisible = oneOfSelectedIsNotTranslated

        val selectedCards = currentWordsList.selectionModel.selectedItems
        val menuItemText =
            if (oneOfSelectedIsNotTranslated && selectedCards.size == 1)
                "Translate '${selectedCards[0].from}'"
            else "Translate selected"
        menuItem.text = menuItemText
    }

    private fun isOneOfSelectedWordsHasNoBaseWord(): Boolean =
        currentWordsList.selectionModel.selectedItems
            .any { !it.ignoreNoBaseWordInSet && it.noBaseWordInSet }

    private fun ignoreNoBaseWordInSet() =
        currentWordsList.selectionModel.selectedItems.forEach {
            updateSetProperty(it.wordCardStatusesProperty, BaseWordDoesNotExist, UpdateSet.Set) }

    private fun addAllBaseWordsInSet() = addAllBaseWordsInSetImpl(currentWordsList.items)
    private fun addBaseWordsInSetForSelected() = addAllBaseWordsInSetImpl(currentWordsList.selectionModel.selectedItems)

    private fun addAllBaseWordsInSetImpl(wordCards: Iterable<CardWordEntry>) {
        val withoutBaseWord = wordCards
            .asSequence()
            .filter { !it.ignoreNoBaseWordInSet && it.noBaseWordInSet }
            .toSortedSet(cardWordEntryComparator)

        val baseWordsToAddMap: Map<CardWordEntry, List<CardWordEntry>> = withoutBaseWord
            .asSequence()
            .map { card -> Pair(card, englishBaseWords(card.from)) }
            //.filterNotNullPairValue()
            .filter { it.second.isNotEmpty() }
            //.map { Pair(it.first, CardWordEntry(it.second, "")) }
            .associate { it }

        // Need to do it manually due to JavaFX bug (if a table has rows with different height)
        // This JavaFX bug appears if rows have different height.
        // See my comments in TableView.setViewPortAbsoluteOffsetImpl()
        currentWordsList.runWithScrollKeeping { // restoreScrollPosition ->

            // Ideally this approach should keep less/more scrolling (without any hacks) but...
            baseWordsToAddMap.forEach { (currentWordCard, baseWordCards) ->
                val index = currentWordsList.items.indexOf(currentWordCard)
                baseWordCards.forEachIndexed { i, baseWordCard ->
                    currentWordsList.items.add(index + i, baseWordCard)
                }
            }
            analyzeWordCards(withoutBaseWord, currentWordsList.items)


            if (baseWordsToAddMap.size == 1) {

                // Need to do it manually due to JavaFX bug (if a table has rows with different height)
                // !!! both call currentWordsList.viewPortAbsoluteOffset are needed !!!
                // restoreScrollPosition(SetViewPortAbsoluteOffsetMode.Immediately)

                val newBaseWordCard = baseWordsToAddMap.values.asSequence().flatten().first()

                // select new base word to edit it immediately
                if (currentWordsList.selectionModel.selectedItems.size <= 1) {
                    currentWordsList.selectionModel.clearSelection()
                    currentWordsList.selectionModel.select(newBaseWordCard)
                }
            }
        }
    }

    private fun englishBaseWords(word: String): List<CardWordEntry> {
        val foundWords: List<CardWordEntry> = possibleEnglishBaseWords(word)
            .asSequence()
            .map { baseWord ->
                try { translateWord(baseWord) }
                catch (ignore: Exception) { CardWordEntry(baseWord, "") } }
            .filter { it.to.isNotBlank() }
            .toList()

        val baseWords: List<CardWordEntry> = foundWords.ifEmpty {
             possibleBestEnglishBaseWord(word)?.let { listOf(CardWordEntry(it, "")) } ?: emptyList() }

        return baseWords.sortedBy { it.from.lowercase() }
    }

    private fun addTranscriptions() {
        val cardsWithoutTranscription = currentWordsList.items.filter { it.transcription.isEmpty() }
        cardsWithoutTranscription.forEach {
            try { it.transcription = translateWord(it.from).transcription }
            catch (ex: Exception) { log.warn("Error of getting transcription for [${it.from}].", ex) }
        }
    }

    private fun removeSelected() {
        val selectedSafeCopy = currentWordsList.selectionModel.selectedItems.toList()
        currentWordsList.selectionModel.clearSelection()
        currentWordsList.runWithScrollKeeping {
            currentWordsList.items.removeAll(selectedSafeCopy) }
    }

    @Suppress("unused")
    private fun reanalyzeWords() {
        analyzeWordCards(currentWordsList.items)
        currentWordsList.refresh()
    }

    private fun startEditingFrom() = startEditingColumnCell(fromColumn)
    private fun startEditingTo() = startEditingColumnCell(toColumn)
    private fun startEditingTranscription() = startEditingColumnCell(transcriptionColumn)
    private fun startEditingRemarks() = startEditingColumnCell(examplesColumn)

    private fun startEditingColumnCell(column: TableColumn<CardWordEntry, String>) {
        val selectedIndex = currentWordsList.selectionModel.selectedIndex
        if (selectedIndex != -1) currentWordsList.edit(selectedIndex, column)
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
                    translateWord(it)
                }
            }

    private fun translateWord(word: String): CardWordEntry=
        CardWordEntry(word, "").also { translateWord(it) }

    private fun translateWord(card: CardWordEntry) {
        val translation = dictionaryComposition.find(card.from.trim())
        card.to = translation.translations.joinToString("\n")
        card.transcription = translation.transcription ?: ""
        card.examples = extractExamples(translation)
    }


    private fun removeIgnoredFromCurrentWords() {
        val toRemove = currentWords.asSequence()
            .filter { word -> ignoredWordsSorted.contains(word.from) }
            .toList()
        currentWordsList.runWithScrollKeeping { currentWordsList.items.removeAll(toRemove) }
    }

    private fun removeWordsFromOtherSetsFromCurrentWords() {

        val skipFiles: Collection<Path> = this.currentWordsFile?.let { listOf(
                it,
                it.useFilenameSuffix(memoWordFileExt),
                it.useFilenameSuffix(internalWordCardsFileExt),
                it.useFilenameSuffix(plainWordsFileExt),
                ) }
            ?: emptyList()

        val toRemove = loadWordsFromAllExistentDictionaries(skipFiles)
        val toRemoveAsSet = toRemove.asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSortedSet(String.CASE_INSENSITIVE_ORDER)

        val currentToRemove = currentWordsList.items.filter { it.from.trim() in toRemoveAsSet }

        // perform removing as ONE operation to minimize change events
        currentWords.removeAll(currentToRemove)
    }

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

        val file = fc.showOpenDialog(this.scene.window)

        if (file != null) {
            val filePath = file.toPath()
            if (filePath == ignoredWordsFile) {
                showErrorAlert(this, "You cannot open [${ignoredWordsFile.name}].")
                return
            }

            val loadType = loadAction(filePath)

            when (loadType) {
                LoadType.Import -> { }
                LoadType.Open   ->
                    // !!! Only if success !!!
                    updateCurrentWordsFile(filePath)
            }
        }
    }

    private fun updateCurrentWordsFile(filePath: Path?) {
        this.currentWordsFile = filePath
        val windowTitle = if (filePath == null) appTitle else "$appTitle - ${filePath.name}"
        setWindowTitle(this, windowTitle)
    }

    private fun loadWordsFromFile() {
        doLoadAction { filePath ->
            val fileExt = filePath.extension.lowercase()
            val words: List<CardWordEntry> = when (fileExt) {
                "txt" ->
                    loadWords(filePath)
                        .map { CardWordEntry(it, "") }
                        .also { analyzeWordCards(it) }
                "csv", "words" ->
                    loadWordCards(filePath)
                        .also { analyzeWordCards(it) }
                "srt" ->
                    extractWordsFromFile(filePath)
                        .also { analyzeWordCards(it) }
                else ->
                    throw IllegalArgumentException("Unexpected file extension [${filePath}]")
            }

            currentWords.setAll(words)
            currentWordsList.sort()

            if (filePath.isInternalCsvFormat) LoadType.Open else LoadType.Import
        }
    }

    private fun extractWordsFromFile(filePath: Path): List<CardWordEntry> =
        FileReader(filePath.toFile(), UTF_8)
            .use { r -> extractWordsFromText(r.readText())
                .also { analyzeWordCards(it) } } // T O D O: would be better to pass lazy CharSequence instead of loading full text as String

    private fun loadFromClipboard() {

        val clipboard: Clipboard = Clipboard.getSystemClipboard()
        val content = clipboard.getContent(DataFormat.PLAIN_TEXT)

        log.info("clipboard content: [${content}]")

        if (content == null) return

        val words = extractWordsFromText(content.toString())
            .also { analyzeWordCards(it) }

        log.info("clipboard content as words: $words")

        currentWords.setAll(words)
        updateCurrentWordsFile(null)
    }

    private fun extractWordsFromText(content: CharSequence): List<CardWordEntry> =
        TextParser()
            .parse(content)
            .asSequence()
            .filter { !ignoredWordsSorted.contains(it) }
            .map { CardWordEntry(it, "") }
            .toList()

    private fun moveToIgnored() {

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

    private fun saveAll() {
        try {
            saveCurrentWords()
            saveIgnored()
        }
        catch (ex: Exception) {
            showErrorAlert(this, "Error of saving words\n${ex.message}")
        }
    }

    enum class InsertPosition { Above, Below }

    private fun insertWordCard(insertPosition: InsertPosition) {
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
                    currentWordsList.selectionModel.clearAndSelect(positionToInsert, fromColumn)
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
                    runLaterWithDelay(50) { currentWordsList.edit(positionToInsert, fromColumn) }
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

    @Suppress("SameParameterValue")
    private fun doSaveCurrentWords(saveAction:(Path)->Unit) {

        var filePath: Path? = this.currentWordsFile
        if (filePath == null) {
            filePath = showTextInputDialog(this, "Enter new words filename")
                .map { dictDirectory.resolve(useFileExt(it, internalWordCardsFileExt)) }
                .orElse(null)
        }

        if (filePath == null) {
            showErrorAlert(this, "Filename is not specified.")
            return
        }

        saveAction(filePath)

        // !!! ONLY if success !!!
        updateCurrentWordsFile(filePath)
    }

    private fun splitCurrentWords(): Unit = doSaveCurrentWords { filePath ->
        val words = currentWordsList.items
        val df = SimpleDateFormat("yyyyMMdd-HHmmss")
        val splitFilesDir = filePath.parent.resolve("split-${df.format(Date())}")

        val defaultSplitWordCountPerFile = 40
        val strSplitWordCountPerFile = showTextInputDialog(this,
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
                showErrorAlert(this, ex.message ?: "Unknown error", "Error of splitting.")
            }
        }
    }

    private fun saveIgnored() {
        val ignoredWords = this.ignoredWordsSorted
        if (ignoredWords.isNotEmpty()) {
            saveWordsToTxtFile(ignoredWordsFile, ignoredWords)
        }
    }

    private fun loadExistentWords() {
        loadIgnored()
        allProcessedWords.setAll(loadWordsFromAllExistentDictionaries())
    }

    private fun loadWordsFromAllExistentDictionaries(skipFiles: Collection<Path> = emptyList()): List<String> {

        if (dictDirectory.notExists()) return emptyList()

        val allWordsFilesExceptIgnored = Files.list(dictDirectory)
            .asSequence()
            .filter { it.isRegularFile() }
            .filter { it !in skipFiles && it != ignoredWordsFile }
            .filter { it.isInternalCsvFormat || it.isMemoWordFile }
            .sorted()
            .toList()

        return allWordsFilesExceptIgnored
            .asSequence()
            .map { loadWordCards(it) }
            .flatMap { it }
            .map { it.from }
            .distinctBy { it.lowercase() }
            .toList()
    }

    private fun loadIgnored() {
        if (ignoredWordsFile.exists()) {
            val ignoredWords = loadWords(ignoredWordsFile)
            this.ignoredWords.setAll(ignoredWords)
        }
    }

}


/*
fun main() {

    val dictRootDir = getProjectDirectory(MainWordsPane::class).resolve("dicts/04/mueller-dict-3.1.1").toFile()

    val props = Properties()
    val dbFilename = "mueller-base"
    @SuppressWarnings("UnnecessaryVariable")
    val dbId = dbFilename

    props.setProperty("${dbFilename}.data", File(dictRootDir, "dict/${dbFilename}.dict.dz").absolutePath)
    props.setProperty("${dbFilename}.index", File(dictRootDir, "dict/${dbFilename}.index").absolutePath)
    props.setProperty("${dbFilename}.encoding", "UTF-8")

    val db = DatabaseFactory.createDatabase(
        dbId,
        dictRootDir,
        props
    )

    log.info("{}", db)

    val fEngine = DictEngine()
    fEngine.databases = arrayOf(db)

    //db.search()

    val word = "apple"

    //val answers: Array<IAnswer?>? = fEngine.defineMatch(dbId, word, pos, true, IDatabase.STRATEGY_NEAR)
    //log.info("{}", answers)

    //val strategy = IDatabase.STRATEGY_EXACT // IDatabase.STRATEGY_NEAR

    //log.info("{}", "\n-----------------------------------------------------------------------------------------")
    //val answers2: Array<IAnswer> = fEngine.defineMatch(dbId, word, "2026", false, strategy)
    //printAnswers(fEngine, answers2, word)


    log.info("{}", "\n--------------------------- match STRATEGY_EXACT ----------------------------------------")
    val answersMatchedByStrategyExact: Array<IAnswer> = fEngine.match(dbId, word, IDatabase.STRATEGY_EXACT)
    printAnswers(fEngine, answersMatchedByStrategyExact, word)

    log.info("{}", "\n--------------------------- match STRATEGY_NONE -----------------------------------------")
    val answersMatchedByStrategyNone: Array<IAnswer> = fEngine.match(dbId, word, IDatabase.STRATEGY_NONE)
    printAnswers(fEngine, answersMatchedByStrategyNone, word)


    log.info("{}", "\n--------------------------- defineMatch define=false STRATEGY_EXACT ---------------------")
    val answersByDefineMatchByStrategyExactWithDefineFalse: Array<IAnswer> = fEngine.defineMatch(
        dbId, word, null, false, IDatabase.STRATEGY_EXACT)
    printAnswers(fEngine, answersByDefineMatchByStrategyExactWithDefineFalse, word)

    log.info("{}", "\n--------------------------- defineMatch define=false STRATEGY_NONE ---------------------")
    val answersByDefineMatchByStrategyNoneWithDefineFalse: Array<IAnswer> = fEngine.defineMatch(
        dbId, word, null, false, IDatabase.STRATEGY_NONE)
    printAnswers(fEngine, answersByDefineMatchByStrategyNoneWithDefineFalse, word)

    log.info("{}", "\n--------------------------- defineMatch define=true STRATEGY_EXACT ---------------------")
    val answersByDefineMatchByStrategyExactWithDefineTrue: Array<IAnswer> = fEngine.defineMatch(
        dbId, word, null, true, IDatabase.STRATEGY_EXACT)
    printAnswers(fEngine, answersByDefineMatchByStrategyExactWithDefineTrue, word)

    log.info("{}", "\n--------------------------- defineMatch define=true STRATEGY_NONE ---------------------")
    val answersByDefineMatchByStrategyNoneWithDefineTrue: Array<IAnswer> = fEngine.defineMatch(
        dbId, word, null, true, IDatabase.STRATEGY_NONE)
    printAnswers(fEngine, answersByDefineMatchByStrategyNoneWithDefineTrue, word)


    log.info("{}", "\n--------------------------- define (default) ------------------------------------------")
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
*/



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
