package com.mvv.gui

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
import javafx.beans.value.WritableValue
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.collections.transformation.SortedList
import javafx.event.EventHandler
import javafx.geometry.Point2D
import javafx.scene.control.*
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.input.Clipboard
import javafx.scene.input.MouseEvent
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.stage.Window
import javafx.stage.WindowEvent
import javafx.util.StringConverter
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.commons.text.StringEscapeUtils.escapeHtml4
import java.io.File
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.io.path.*


private val log = mu.KotlinLogging.logger {}


class LearnWordsController (val isReadOnly: Boolean = false) {

    //private val projectDirectory = getProjectDirectory(this.javaClass)

    internal val dictionary: Dictionary by lazy { CachedDictionary( DictionaryComposition(loadDictionaries()) ) }

    internal val navigationHistory = NavigationHistory()

    internal val pane = MainWordsPane(this)

    internal val currentWordsList: WordCardsTable get() = pane.wordEntriesTable
    private val currentWords: ObservableList<CardWordEntry> get() = currentWordsList.items

    private var documentIsDirty: Boolean = false

    private val ignoredWords: ObservableList<String> = FXCollections.observableArrayList()
    private val ignoredWordsSorted: ObservableList<String> = SortedList(ignoredWords, String.CASE_INSENSITIVE_ORDER)

    private val allProcessedWords: ObservableList<String> = FXCollections.observableArrayList()

    private var currentWordsFile: Path? = null

    val cellEditorStates = WeakHashMap<Pair<TableColumn<CardWordEntry,*>, CardWordEntry>, CellEditorState>()

    private val toolBarController2 = ToolBarControllerBig(this)
    private val settingsPane = SettingsPane()
    private val toolBar2 = ToolBar2(this).also {
        it.nextPrevWarningWord.addSelectedWarningsChangeListener { _, _, _ -> recalculateWarnedWordsCount() } }

    internal val allWordCardSetsManager: AllWordCardSetsManager by lazy { AllWordCardSetsManager() }
    // we have to use lazy because creating popup before creating/showing main windows causes JavaFX hanging up :-)
    private val otherCardsViewPopup: OtherCardsViewPopup by lazy { OtherCardsViewPopup() }
    private val lightOtherCardsViewPopup: LightOtherCardsViewPopup by lazy { LightOtherCardsViewPopup() }
    private val foundCardsViewPopup: OtherCardsViewPopup by lazy { OtherCardsViewPopup().also {
        it.contentComponent.maxWidth  = 650.0
        it.contentComponent.maxHeight = 500.0
    } }

    private val englishVerbs = EnglishVerbs()
    @Volatile
    private var prefixFinder = PrefixFinder(emptyList())
    private val baseWordExtractor: BaseWordExtractor = object : BaseWordExtractor {
        override fun extractBaseWord(phrase: String): String {
            val baseWords = prefixFinder.calculateBaseOfFromForSorting(phrase)
            val firstWordOfBase = baseWords.firstWord.removeSuffixesRepeatably("!", ".", "?", "…").toString()
            // TODO: use EnglishVerbs.getInfinitive() when it is implemented properly
            return englishVerbs.getIrregularInfinitive(firstWordOfBase) ?: firstWordOfBase
        }
    }


    private fun rebuildPrefixFinder() {
        // only words without phrases
        val cards = currentWords.toList()
        val onlyPureWords = cards.map { it.from }
            .filterNotBlank()
            .filterNot { it.containsWhiteSpaceInMiddle() }
            .toSet()

        if (this.prefixFinder.ignoredWords != onlyPureWords) {
            // Rebuilding PrefixFinder_New is very fast - no need to use separate thread
            //CompletableFuture.runAsync { rebuildPrefixFinderImpl(onlyPureWords) }

            rebuildPrefixFinderImpl(onlyPureWords)
        }
    }

    private fun rebuildPrefixFinderImpl(onlyPureWords: Set<String>) {
        this.prefixFinder = PrefixFinder(onlyPureWords)
        Platform.runLater { currentWords.toList().forEach { it.baseWordOfFromProperty.resetCachedValue() } }
    }


    init {
        pane.initThemeAndStyles()

        val currentWordsLabelText = "File/Clipboard (%d words)"
        currentWords.addListener(ListChangeListener { pane.wordEntriesLabel.text = currentWordsLabelText.format(it.list.size) })

        val ignoredWordsLabelText = "Ignored words (%d)"
        ignoredWords.addListener(ListChangeListener { pane.ignoredWordsLabel.text = ignoredWordsLabelText.format(it.list.size) })

        pane.ignoredWordsList.items = ignoredWordsSorted

        val allProcessedWordsLabelText = "All processed words (%d)"
        allProcessedWords.addListener(ListChangeListener { pane.allProcessedWordsLabel.text = allProcessedWordsLabelText.format(it.list.size) })

        pane.allProcessedWordsList.items = SortedList(allProcessedWords, String.CASE_INSENSITIVE_ORDER)

        pane.topPane.children.add(0, MenuController(this).fillMenu())
        pane.topPane.children.add(toolBarController2.toolBar)
        pane.topPane.children.add(settingsPane)
        pane.topPane.children.add(toolBar2)

        currentWordsList.addEventHandler(MouseEvent.MOUSE_CLICKED) { ev ->
            val card = currentWordsSelection.selectedItem
            val tableColumn = currentWordsSelection.selectedCells?.getOrNull(0)?.tableColumn

            if (ev.clickCount >= 2 && card != null && tableColumn.isOneOf(currentWordsList.sourceSentencesColumn, currentWordsList.numberColumn))
                showSourceSentences(card)
        }

        if (!isReadOnly) initNonReadOnly()
    }

    private fun initNonReadOnly() {
        currentWords.addListener(ListChangeListener { markDocumentIsDirty(); reanalyzeAllWords() })

        pane.warnAboutMissedBaseWordsModeDropDown.onAction = EventHandler { reanalyzeAllWords() }

        val contextMenuController = ContextMenuController(this)
        currentWordsList.contextMenu = contextMenuController.contextMenu
        currentWordsList.onContextMenuRequested = EventHandler { contextMenuController.updateItemsVisibility() }

        currentWordsList.selectionModel.selectedItemProperty().addListener { _, _, card ->
            Platform.runLater { if (toPlayWordOnSelect) playSelectedWord() }
            Platform.runLater { showWarningAboutSelectedCardExistInOtherSet() }
        }

        pane.wordEntriesTable.fromColumn.addEventHandler(TableColumn.editCommitEvent<CardWordEntry,String>()) {
            val wordOrPhrase = it.newValue
            if (wordOrPhrase.isNotBlank()) Platform.runLater { onCardFromEdited(wordOrPhrase) }
        }

        addGlobalKeyBindings(currentWordsList, copyKeyCombinations.associateWith { {
            if (!currentWordsList.isEditing) copySelectedWord() } })

        addKeyBindings()

        pane.addIsShownHandler {
            val window = pane.scene.window

            window.addEventHandler(WindowEvent.WINDOW_HIDDEN) { hidePopups() }
            if (window is Stage)
                window.iconifiedProperty().addListener { _, _, minimized ->
                    if (minimized) hidePopups() }

            window.focusedProperty().addListener { _,_,_ -> Platform.runLater { hidePopups() } }
        }

        pane.addIsShownHandler {
            // We use delay to get frame be shown before CPU will be busy with background tasks.
            runLaterWithDelay(1000) { CompletableFuture.runAsync {

                // TODO: use some background task indicator

                rebuildPrefixFinderImpl(emptySet())

                this.dictionary.find("apple") // load dictionaries lazy

                allWordCardSetsManager.reloadAllSetsAsync()
            } } }

        installNavigationHistoryUpdates(currentWordsList, navigationHistory)

        loadExistentWords()
    }

    private fun showWarningAboutSelectedCardExistInOtherSet() {
        val selectedWordOrPhrase = currentWordsList.singleSelection?.from ?: ""

        val foundInOtherSets = allWordCardSetsManager.findBy(selectedWordOrPhrase, MatchMode.Exact)
        if (foundInOtherSets.isNotEmpty())
            showThisWordsInLightOtherSetsPopup(selectedWordOrPhrase, foundInOtherSets)
        else
            lightOtherCardsViewPopup.hide()
    }

    private var toPlayWordOnSelect: Boolean
        get() = settingsPane.playWordOnSelect
        set(value) { settingsPane.playWordOnSelect = value }

    /** Adds change listeners and other controller features. */
    private fun CardWordEntry.adjustCard(): CardWordEntry =
        this.copy(this@LearnWordsController.baseWordExtractor).also { addChangeCardListener(it) }

    private fun loadDictionaries() =
        if (isReadOnly) emptyList()
        else {
            AutoDictionariesLoader().load().also { dicts -> // HardcodedDictionariesLoader().load()
                log.info(
                    "Used dictionaries\n" +
                            dicts.mapIndexed { i, d -> "${i + 1} $d" }.joinToString("\n") +
                            "\n---------------------------------------------------\n\n"
                )
            }
        }

    private fun onCardFromEdited(wordOrPhrase: String) {
        if (settingsPane.warnAboutDuplicatesInOtherSets) {
            // TODO: remove ending 'something', 'smt', so on to avoid duplicates with and without smt, smb, etc.
            val foundInOtherSets = allWordCardSetsManager.findBy(wordOrPhrase, MatchMode.Exact)
            if (foundInOtherSets.isNotEmpty())
                showThisWordsInOtherSetsPopup(wordOrPhrase, foundInOtherSets)
        }

        rebuildPrefixFinder()
    }

    // timeout is needed because JavaFX window takes focus with some delay
    private fun hidePopups() = runLaterWithDelay(250) { hidePopupsImpl() }

    private fun hidePopupsImpl() {
        val isOneOfAppWindowActive = pane.scene.window.isActive || otherCardsViewPopup.isActive || lightOtherCardsViewPopup.isActive

        if (!isOneOfAppWindowActive) {
            lightOtherCardsViewPopup.hide()
            otherCardsViewPopup.hide()
        }
    }

    private fun showThisWordsInOtherSetsPopup(wordOrPhrase: String, cards: List<SearchEntry>) {
        val mainWnd = pane.scene.window

        lightOtherCardsViewPopup.hide()
        otherCardsViewPopup.show(mainWnd, "Word '$wordOrPhrase' already exists in other sets", cards) {
            val xOffset = 20.0;  val yOffset = 50.0
            Point2D(
                mainWnd.x + mainWnd.width - otherCardsViewPopup.width - xOffset,
                mainWnd.y + yOffset)
        }
    }

    internal fun showSpecifiedWordsInOtherSetsPopup(wordOrPhrase: String, cards: List<SearchEntry>) {
        val mainWnd = pane.scene.window

        foundCardsViewPopup.show(mainWnd, "'$wordOrPhrase' in other sets", cards) {
            val xOffset = 20.0;  val yOffset = 50.0
            Point2D(
                mainWnd.x + mainWnd.width - foundCardsViewPopup.width - xOffset,
                mainWnd.y + mainWnd.height - foundCardsViewPopup.height - yOffset)
        }
    }

    private fun showThisWordsInLightOtherSetsPopup(wordOrPhrase: String, cards: List<SearchEntry>) {
        val mainWnd = pane.scene.window

        lightOtherCardsViewPopup.show(mainWnd, wordOrPhrase, cards) {
            val xOffset = 20.0;  val yOffset = 80.0
            Point2D(
                mainWnd.x + mainWnd.width - lightOtherCardsViewPopup.width - xOffset,
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

    private val voiceManager = VoiceManager()

    internal fun playSelectedWord() = currentWordsList.singleSelection?.let { playFrom(it) }

    private fun playFrom(card: CardWordEntry) {
        //val voice = settingsPane.voice

        CompletableFuture.runAsync { playWord(card.from.trim()) }
            .exceptionally {
                val rootCause = ExceptionUtils.getRootCause(it)
                //log.info { "Word [${card.from}] is not played => ${rootCause.javaClass.simpleName}: ${rootCause.message}" }
                log.info { "Word [${card.from}] is not played => ${rootCause.message}" }
                null
            }
    }

    private fun playWord(text: String) = voiceManager.speak(text, settingsPane.playVoiceGender)

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
                    currentWordsSelection.select(newBaseWordCard.adjustCard())
                }
            }
        }

    fun addTranscriptions() =
        addTranscriptions(currentWords, dictionary)
            .doIfSuccess { markDocumentIsDirty() }


    fun removeSelected() {
        val toRemoveSafeCopy = currentWordsSelection.selectedItems.toList()
        currentWordsSelection.clearSelection()
        currentWordsList.runWithScrollKeeping { removeCards(toRemoveSafeCopy) }
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

    fun selectByBaseWord() {
        val baseWordOfFrom = currentWordsList.singleSelection?.baseWordOfFrom ?: return

        val toSelect = currentWords.filtered { it.baseWordOfFrom.startsWith(baseWordOfFrom) }
        toSelect.forEach { currentWordsList.selectionModel.select(it) }
    }

    fun copySelectToOtherSet() {
        try { copySelectToOtherSetImpl() }
        catch (ex: Exception) {
            log.error(ex) { "Error of copying selected cards to other set file." }
            showInfoAlert(currentWordsList, "Error of copying selected cards to other set file.\n\n${ex.message}")
        }
    }

    private fun copySelectToOtherSetImpl() {

        val selected = currentWordsSelection.selectedItems
        if (selected.isEmpty()) return

        val currentWordsFile = this.currentWordsFile // local safe ref
        val currentWordsFileParent = currentWordsFile?.parent ?: dictDirectory

        val otherSetsFiles = allWordCardSetsManager.allCardSets.sortedWith(PathCaseInsensitiveComparator())
        val allOtherSetsParents = otherSetsFiles.map { it.parent }.distinct()
        val commonAllOtherSetsSubParent = if (allOtherSetsParents.size == 1) allOtherSetsParents[0]
                                          else allOtherSetsParents.minByOrNull { it.nameCount } ?: currentWordsFileParent

        val allSetsParentsAreParentOfCurrent = (allOtherSetsParents.size == 1) && (allOtherSetsParents[0] == currentWordsFileParent)

        val pathToSetNameConv = object : StringConverter<Path>() {
            override fun toString(value: Path?): String = when {
                allSetsParentsAreParentOfCurrent -> value?.baseWordsFilename
                else -> value?.toString()?.removePrefix(commonAllOtherSetsSubParent.toString())?.removePrefix("/")
            } ?: ""
            override fun fromString(string: String?): Path? = if (string.isNullOrBlank()) null else Path.of(string.trim())
        }

        val destFileOrSetName = showDropDownDialog(currentWordsList, "Select cards' set to copy cards to", otherSetsFiles, pathToSetNameConv, true)
            ?: return

        val destFilePath =
            if (destFileOrSetName.exists()) destFileOrSetName
            else {
                if (destFileOrSetName.isInternalCsvFormat)
                    commonAllOtherSetsSubParent.resolve(destFileOrSetName)
                else {
                    val setName = destFileOrSetName.toString()
                    require(!setName.endsWithOneOf(internalWordCardsFileExt, plainWordsFileExt)) {
                        "Please, specify set name or full absolute path to set file."}

                    currentWordsFileParent.resolve(setName.withFileExt(internalWordCardsFileExt))
                }
            }

        require(destFilePath.isInternalCsvFormat && !destFilePath.isMemoWordFile) {
            "It looks strange to load/save data in memo-word format [$destFilePath]." }

        val existentCards = if (destFilePath.exists()) loadWordCards(destFilePath) else emptyList()

        val duplicates = verifyDuplicates(existentCards, selected)

        val cardToSave: List<CardWordEntry> =
            if (duplicates.duplicatedByOnlyFrom.isNotEmpty()) {
                val similarFrom = duplicates.duplicatedByOnlyFrom.map { it.from }.sorted()
                val showDuplicateCount = 2 //7
                val showDuplicatePadding = "  "
                val similarFromStr = similarFrom
                    .joinToString("\n", "", "", showDuplicateCount, "$showDuplicatePadding...") {
                        "$showDuplicatePadding'$it'" }

                val mergeButton = ButtonType("Merge", ButtonData.OTHER)
                val skipButton  = ButtonType("Skip", ButtonData.OTHER)

                val res = showConfirmation(currentWordsList,
                    "This set already contains similar ${duplicates.duplicatedByOnlyFrom.size} cards:\n" +
                        "${similarFromStr}\n\n" +
                        "Do you want to merge them or skip?",
                    "Copy cards to other set",
                    ButtonType.CANCEL, mergeButton, skipButton,
                )

                if (res.isEmpty || res.get() == ButtonType.CANCEL) return

                when (res.get()) {
                    skipButton  -> existentCards + selected.skipCards(duplicates.fullDuplicates, duplicates.duplicatedByOnlyFrom)
                    mergeButton -> mergeCards(existentCards, selected.skipCards(duplicates.fullDuplicates))
                    else        -> throw IllegalStateException("Unexpected button [${res.get()}]")
                }
            }
            else existentCards + selected.skipCards(duplicates.fullDuplicates)

        saveWordsImpl(cardToSave, destFilePath)
    }

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

    fun showInitialTranslationOfSelected() {
        val card = currentWordsList.singleSelection ?: return

        val translated = dictionary.translateWord(card.from.trim())
        showTextAreaPreviewDialog(currentWordsList, "Translation of '${card.from}'",
            translated.to + "\n" + translated.examples, wrapText = true, width = 400.0, height = 400.0)
    }

    fun addInitialTranslationOfSelected() {
        val card = currentWordsList.singleSelection ?: return

        val translated = dictionary.translateWord(card.from.trim())

        addToCardProp(card, translated) { it.toProperty }
        addToCardProp(card, translated) { it.examplesProperty }
        addToCardProp(card, translated) { it.transcriptionProperty }

        reanalyzeOnlyWords(card)
    }

    private fun addToCardProp(card: CardWordEntry, addFromCard: CardWordEntry, prop: (CardWordEntry)->WritableValue<String>) {
        val cardProp = prop(card)
        val cardPropValue = cardProp.value
        val valueToAdd    = prop(addFromCard).value

        if (cardPropValue == valueToAdd) return

        if (cardPropValue.isNotBlank()) cardProp.value += "\n\n"
        cardProp.value += valueToAdd
    }

    internal fun removeIgnoredFromCurrentWords() {
        val toRemove = currentWords.asSequence()
            .filter { word -> ignoredWordsSorted.contains(word.from) }
            .toList()
        currentWordsList.runWithScrollKeeping { removeCards(toRemove) }
    }

    fun removeWordsFromOtherSetsFromCurrentWords() =
        removeWordsFromOtherSetsFromCurrentWords(currentWords, this.currentWordsFile)
            .also { removeChangeCardListener(it) }

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
        allWordCardSetsManager.reloadAllSetsAsync()
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
        navigationHistory.clear()
        cellEditorStates.clear()
        rebuildPrefixFinder()
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
            // Probably we need to do ANY/EVERY call to analyzeAllWords() async
            .also { analyzeAllWords(it) }
            .map { it.adjustCard() }

            currentWords.setAll(words)

            rebuildPrefixFinder()
            currentWordsList.sort()

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

    private fun removeChangeCardListener(cards: Iterable<CardWordEntry>) =
        cards.forEach { removeChangeCardListener(it) }

    private fun removeChangeCardListener(card: CardWordEntry) {
        card.fromProperty.removeListener(changeCardListener)
        card.fromWithPrepositionProperty.removeListener(changeCardListener)
        card.toProperty.removeListener(changeCardListener)
        card.transcriptionProperty.removeListener(changeCardListener)
        card.examplesProperty.removeListener(changeCardListener)
        card.statusesProperty.removeListener(changeCardListener)
        card.predefinedSetsProperty.removeListener(changeCardListener)
        card.sourcePositionsProperty.removeListener(changeCardListener)
        card.sourceSentencesProperty.removeListener(changeCardListener)
    }

    private fun loadFromSrt(filePath: Path): List<CardWordEntry> {
        val toIgnoreWords = if (settingsPane.autoRemoveIgnoredWords) this.ignoredWords.toIgnoreCaseSet() else emptySet()
        return extractWordsFromSrtFileAndMerge(filePath, toIgnoreWords)
    }

    //@Suppress("SameParameterValue")
    //private fun extractWordsFromFile(filePath: Path, sentenceEndRule: SentenceEndRule, preProcessor: (Reader)->Reader = { it }): List<CardWordEntry> {
    //    val toIgnoreWords = if (settingsPane.autoRemoveIgnoredWords) this.ignoredWords.toIgnoreCaseSet() else emptySet()
    //    return extractWordsFromFileAndMerge(filePath, sentenceEndRule, toIgnoreWords, preProcessor)
    //}

    fun loadFromClipboard() {
        val toIgnoreWords = if (settingsPane.autoRemoveIgnoredWords) ignoredWords.toIgnoreCaseSet() else emptySet()
        val words = extractWordsFromClipboard(Clipboard.getSystemClipboard(), settingsPane.sentenceEndRule, toIgnoreWords)
            .map { it.adjustCard() }

        currentWords.addAll(words)
        rebuildPrefixFinder()

        reanalyzeAllWords()
    }


    fun moveSelectedToIgnored() {

        val selectedWords = currentWordsSelection.selectedItems.toList() // safe copy

        log.debug("selectedWords: {} moved to IGNORED.", selectedWords)

        val newIgnoredWordEntries = selectedWords.filter { !ignoredWordsSorted.contains(it.from) }
        val newIgnoredWords = newIgnoredWordEntries.map { it.from }
        log.debug("newIgnored: {}", newIgnoredWords)
        ignoredWords.addAll(newIgnoredWords)

        currentWordsList.runWithScrollKeeping {
            currentWordsSelection.clearSelection()
            removeCards(selectedWords)
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

            removeCards(selectedWords)
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
            val cloned: CardWordEntry = selected.copy().adjustCard()

            currentWordsList.runWithScrollKeeping { currentWords.add(pos, cloned) }
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
            val newCardWordEntry = CardWordEntry("", "").adjustCard()

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
        RecentDocuments().addRecent(filePath.useFilenameSuffix(internalWordCardsFileExt))
        saveWordsImpl(currentWords, filePath)
        resetDocumentIsDirty()
    }

    private fun saveWordsImpl(words: List<CardWordEntry>, filePath: Path) {
        val internalFormatFile = filePath.useFilenameSuffix(internalWordCardsFileExt)
        saveWordCards(internalFormatFile, CsvFormat.Internal, words)

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
        saveSplitWordCards(filePath, words, splitFilesDir, settingsPane.splitWordCountPerFile, CsvFormat.MemoWord)

        // without phrases
        val onlyWords = words.filterNot { it.from.containsWhiteSpaceInMiddle() }
        saveSplitWordCards(filePath.parent.resolve(filePath.baseWordsFilename + "_OnlyWords.csv"), onlyWords, splitFilesDir, settingsPane.splitWordCountPerFile, CsvFormat.MemoWord)
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

        //val defaultSplitWordCountPerFile = settingsPane.splitWordCountPerFile
        val strSplitWordCountPerFile = showTextInputDialog(pane,
            "Current words will be split into several files and put into directory $splitFilesDir.\n" +
                    "Please, enter word count for every file.", "Splitting current words",
            //"$defaultSplitWordCountPerFile")
            "")

        strSplitWordCountPerFile.ifPresent {
            try {
                val wordCountPerFile: Int = it.toInt()
                require(wordCountPerFile >= 20) { "Word count should be positive value." }

                //require(wordCountPerFile <= maxMemoCardWordCount) {
                //    "Word count should be less than 300 since memo-word supports only $maxMemoCardWordCount sized word sets." }

                saveSplitWordCards(filePath, words, splitFilesDir, wordCountPerFile, CsvFormat.Internal)
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

    // TODO: do it async and use some task indicator
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

    fun addToOrRemoveFromPredefinedSet(set: PredefinedSet) {
        val cards = currentWordsSelection.selectedItems
        if (cards.isEmpty()) return

        val someCardsAreNotAddedToPredefSet = cards.any { set !in it.predefinedSets }

        // For me this logic is more expected/desired.
        // We can use just simple inversion logic.
        val addOrRemoveAction = if (someCardsAreNotAddedToPredefSet) UpdateSet.Set else UpdateSet.Remove

        cards.forEach {
            updateSetProperty(it.predefinedSetsProperty, set, addOrRemoveAction) }
    }

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
            ?.also { reanalyzeOnlyWords(it) }
            ?.adjustCard()
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

        val newCard: CardWordEntry = selectionOrCurrentLine.parseToCard()?.adjustCard() ?: return null

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
        removeCards(cardsToRemove)
    }

    private fun removeCards(toRemove: List<CardWordEntry>) {
        currentWords.removeAll(toRemove)
        removeChangeCardListener(toRemove)
    }

    fun isSelectionMergingAllowed(): Boolean {
        val selectedCards = currentWordsSelection.selectedItems
        if (selectedCards.size != 2) return false

        val from1baseWord = selectedCards[0].baseWordOfFromProperty.value.firstWord
        val from2baseWord = selectedCards[1].baseWordOfFromProperty.value.firstWord

        return from1baseWord.startsWith(from2baseWord) || from2baseWord.startsWith(from1baseWord)
    }

    fun mergeSelected() {
        val selectedCards = currentWordsSelection.selectedItems.toList() // safe copy

        val merged = mergeCards(selectedCards).adjustCard()
        // currentWordsSelection.selectedIndex is not used because it returns the most recently selected item.
        val firstCardIndex = currentWords.indexOf(selectedCards[0])

        currentWordsList.runWithScrollKeeping {
            removeCards(selectedCards)
            currentWords.add(firstCardIndex, merged)

            reanalyzeOnlyWords(merged)
        }
    }

}

private fun List<CardWordEntry>.skipCards(toSkip: Set<CardWordEntry>): List<CardWordEntry> =
    this.filterNot { it in toSkip }
private fun List<CardWordEntry>.skipCards(toSkip1: Set<CardWordEntry>, toSkip2: Set<CardWordEntry>): List<CardWordEntry> =
    this.filterNot { it in toSkip1 || it in toSkip2 }
private fun mergeCards(existentCards: List<CardWordEntry>, newCards: List<CardWordEntry>): List<CardWordEntry> {
    val all = (existentCards + newCards).groupBy { it.from.lowercase() }
    return all.values.map { cardsWitTheSameFrom -> mergeCards(cardsWitTheSameFrom).also { it.from = cardsWitTheSameFrom.first().from } }
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

        { it.removePrefix("to ").firstWord },
        { it.removePrefix("to be ").firstWord },
        { it.removePrefix("to ").firstWord.removeSuffix("e") },
        { it.removePrefix("to ").firstWord.removeSuffix("le") },
        { it.removePrefix("to ").firstWord.removeSuffix("y") },
        { it.removePrefix("to ").firstWord.removeSuffix("ly") },
        { it.removePrefix("to be ").firstWord.removeSuffix("s") },
    )

    return preparations.firstNotNullOfOrNull { prep ->
        val preparedWord = prep(word)
        if (preparedWord in this) preparedWord else null
    } ?: word
}

private val String.firstWord: String get() {
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


private val Window.isActive: Boolean get() =
    this.isShowing && this.isFocused

data class CellEditorState (
    val scrollLeft: Double,
    val scrollTop: Double,
    val caretPosition: Int,
)



internal class VerifyDuplicatesResult (
    // they should be ignored
    val fullDuplicates: Set<CardWordEntry>,
    // they should be skipped or merged
    val duplicatedByOnlyFrom: Set<CardWordEntry>,
)

internal fun verifyDuplicates(existentCards: List<CardWordEntry>, newCards: List<CardWordEntry>): VerifyDuplicatesResult {

    val existentCardsAsMap = existentCards.groupBy { it.from.lowercase() }

    val fullDuplicates = newCards.filter { newCard ->
        val existentCardsWithThisFrom = existentCardsAsMap[newCard.from.lowercase()] ?: return@filter false

        existentCardsWithThisFrom.any {
                    it.to == newCard.to && it.transcription == newCard.transcription && it.examples == newCard.examples
                    // old/new predefinedSets should/desired be merged
                    //
                    // We can ignore 'statuses', 'sourcePositions', 'sourceSentences'.
        }
    }.toSet()

    val partialDuplicates = newCards
        .filterNot { it in fullDuplicates }
        .filter { newCard -> existentCardsAsMap[newCard.from.lowercase()] != null }
        .toSet()

    return VerifyDuplicatesResult(fullDuplicates, partialDuplicates)
}

