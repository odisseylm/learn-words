package com.mvv.gui.cardeditor

import com.mvv.gui.cardeditor.actions.*
import com.mvv.gui.dictionary.*
import com.mvv.gui.dictionary.Dictionary
import com.mvv.gui.javafx.*
import com.mvv.gui.task.TaskManager
import com.mvv.gui.task.addTask
import com.mvv.gui.task.createFxProgressBar
import com.mvv.gui.task.javaFxRunner
import com.mvv.gui.util.*
import com.mvv.gui.words.*
import javafx.application.Platform
import javafx.beans.property.ReadOnlyProperty
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.collections.transformation.SortedList
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.input.MouseEvent
import javafx.stage.Stage
import javafx.stage.Window
import javafx.stage.WindowEvent
import java.nio.file.Path
import java.util.*
import java.util.concurrent.Executors
import kotlin.io.path.*


//private val log = mu.KotlinLogging.logger {}


class LearnWordsController (val isReadOnly: Boolean = false): AutoCloseable {

    internal val log = mu.KotlinLogging.logger {}

    //private val projectDirectory = getProjectDirectory(this.javaClass)

    private val defaultTaskExecutor = Executors.newSingleThreadExecutor()
    private val taskManager = TaskManager(javaFxRunner)

    internal val dictionary: Dictionary by lazy { CachedDictionary( DictionaryComposition(loadDictionaries()) ) }

    internal val navigationHistory = NavigationHistory()

    internal val pane = MainWordsPane(this)

    internal val currentWordsList: WordCardsTable get() = pane.wordEntriesTable
    internal val currentWords: ObservableList<CardWordEntry> get() = currentWordsList.items

    internal val ignoredWords: ObservableList<String> = FXCollections.observableArrayList()
    internal val ignoredWordsSorted: ObservableList<String> = SortedList(ignoredWords, String.CASE_INSENSITIVE_ORDER)

    private val allProcessedWords: ObservableList<String> = FXCollections.observableArrayList()

    internal var currentWordsFile: Path? = null

    internal val cellEditorStates = WeakHashMap<Pair<TableColumn<CardWordEntry,*>, CardWordEntry>, CellEditorState>()

    internal val settingsPane = SettingsPane()
    private val toolBarController = ToolBarControllerBig(this)
    private val toolBar = ToolBar2(this).also {
        it.nextPrevWarningWord.addSelectedWarningsChangeListener { _, _, _ -> recalculateWarnedWordsCount() } }

    internal val allWordCardSetsManager: AllWordCardSetsManager by lazy { AllWordCardSetsManager() }

    // we have to use lazy because creating popup before creating/showing main windows causes JavaFX hanging up :-)
    internal val otherCardsViewPopup: OtherCardsViewPopup by lazy { OtherCardsViewPopup() }
    internal val lightOtherCardsViewPopup: LightOtherCardsViewPopup by lazy { LightOtherCardsViewPopup() }
    internal val foundCardsViewPopup: OtherCardsViewPopup by lazy { OtherCardsViewPopup().also {
        it.contentComponent.maxWidth  = 650.0
        it.contentComponent.maxHeight = 500.0
    } }

    internal val synonymsPopup: SynonymsPopup by lazy { SynonymsPopup(this) }

    private val showSynonymInEditorTimer = Timer("showSynonymInEditorTimer", true).also {
        if (settings.showSynonyms && !isReadOnly)
            it.schedule(uiTimerTask { showSynonymOfCurrentWord() }, 5000, 1500)
    }

    @Volatile
    internal var prefixFinder = englishPrefixFinder(emptyList())
    private val baseWordExtractor: BaseWordExtractor = createBaseWordExtractor()

    override fun close() {
        taskManager.close()
        defaultTaskExecutor.shutdown()
        allWordCardSetsManager.close()
        showSynonymInEditorTimer.cancel()

        runInFxEdtNowOrLater { pane.scene.window.hide() }
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
        pane.topPane.children.add(toolBarController.toolBar)
        pane.topPane.children.add(settingsPane)
        pane.topPane.children.add(toolBar)

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

        currentWordsList.selectionModel.selectedItemProperty().addListener { _, _, _ ->
            Platform.runLater { if (toPlayWordOnSelect) playSelectedWord() }
            Platform.runLater { showWarningAboutSelectedCardExistInOtherSet() }
        }

        pane.wordEntriesTable.fromColumn.addEventHandler(TableColumn.editCommitEvent<CardWordEntry,String>()) {
            val wordOrPhrase = it.newValue
            if (wordOrPhrase.isNotBlank()) Platform.runLater { onCardFromEdited(wordOrPhrase) }
        }

        addGlobalKeyBindings(currentWordsList, copyKeyCombinations.associateWith { {
            if (!currentWordsList.isEditing) copySelectedWord() } })

        addNonReadOnlyKeyBindings()

        pane.statusPane.right = taskManager.createFxProgressBar().also {
            it.padding = Insets(2.0, 10.0, 4.0, 0.0)
        }

        pane.addIsShownHandler {
            val window = pane.scene.window

            window.addEventHandler(WindowEvent.WINDOW_HIDDEN) { hidePopups() }
            if (window is Stage)
                window.iconifiedProperty().addListener { _, _, minimized ->
                    if (minimized) hidePopups()
                }

            window.focusedProperty().addListener { _,_,_ -> Platform.runLater { hidePopups() } }

            // We use delay to get frame be shown before CPU will be busy with background tasks.
            runLaterWithDelay(1000) {

                runAsyncTask("Rebuilding prefix")    { rebuildPrefixFinderImpl(emptySet()) }

                runAsyncTask("Loading dictionaries") { dictionary.find("apple")  } // load dictionaries lazy

                allWordCardSetsManager.reloadAllSetsAsync(taskManager)
            }
        }

        installNavigationHistoryUpdates(currentWordsList, navigationHistory)

        loadExistentWords()
    }

    private fun addNonReadOnlyKeyBindings() {
        addGlobalKeyBinding(pane, openDocumentKeyCodeCombination) { loadWordsFromFile() }
        addGlobalKeyBinding(pane, saveDocumentKeyCodeCombination) { saveAll() }

        addGlobalKeyBinding(pane, previousNavigationKeyCodeCombination) { navigateToCard(NavigationDirection.Back, navigationHistory, currentWordsList) }
        addGlobalKeyBinding(pane, nextNavigationKeyCodeCombination)     { navigateToCard(NavigationDirection.Forward, navigationHistory, currentWordsList) }
    }

    internal val toWarnAbout: Set<WordCardStatus> get() = toolBar.nextPrevWarningWord.selectedWarnings

    internal var toPlayWordOnSelect: Boolean
        get() = settingsPane.playWordOnSelect
        set(value) { settingsPane.playWordOnSelect = value }

    /** Adds change listeners and other controller features. */
    internal fun CardWordEntry.adjustCard(): CardWordEntry =
        this.copy(this@LearnWordsController.baseWordExtractor).also { addChangeCardListener(it) }

    private fun String.removeEnglishTrailingPronoun(): String =
        englishOptionalTrailingPronounsFinder.removeMatchedSubSequence(this, SubSequenceFinderOptions(false))

    private fun onCardFromEdited(wordOrPhrase: String) {
        if (settingsPane.warnAboutDuplicatesInOtherSets) {
            val fixedWordOrPhrase = wordOrPhrase.removeEnglishTrailingPronoun()
            val foundInOtherSets = allWordCardSetsManager.findBy(fixedWordOrPhrase, MatchMode.Exact)
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

    internal val voiceManager = VoiceManager()

    internal val currentWordsSelection: TableView.TableViewSelectionModel<CardWordEntry> get() = currentWordsList.selectionModel
    internal val currentWarnAboutMissedBaseWordsMode: WarnAboutMissedBaseWordsMode get() = pane.warnAboutMissedBaseWordsModeDropDown.value


    @Suppress("PrivatePropertyName")
    private var documentIsDirty_: Boolean = false

    internal val documentIsDirty: Boolean get() = this.documentIsDirty_
    internal fun markDocumentIsDirty() { this.documentIsDirty_ = true; Platform.runLater { updateTitle() } }
    internal fun resetDocumentIsDirty() { this.documentIsDirty_ = false; Platform.runLater { updateTitle() } }

    internal fun doIsCurrentDocumentIsSaved(currentAction: String = ""): Boolean =
        try { validateCurrentDocumentIsSaved(currentAction); true } catch (_: Exception) { false }


    internal val changeCardListener = ChangeListener<Any> { prop,_,_ ->
        markDocumentIsDirty()

        // in general, it is bad idea to use so many 'instanceOf' but it is not critical there
        if (prop is ReadOnlyProperty<*>) {
            val bean = prop.bean
            if (bean is CardWordEntry) reanalyzeOnlyWords(bean)
        }
    }


    private fun runAsyncTask(name: String, task: ()->Unit) = taskManager.addTask(name, defaultTaskExecutor, task)


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

    private fun loadExistentWords() {
        runAsyncTask("Loading ignored") { loadIgnored() }

        runAsyncTask("Loading existent words") {
            val fromAllExistentDictionaries = loadWordsFromAllExistentDictionaries(null)
            runInFxEdtNowOrLater { allProcessedWords.setAll(fromAllExistentDictionaries) }
        }
    }

    private fun loadIgnored() {
        if (ignoredWordsFile.exists()) {
            val ignoredWords = loadWords(ignoredWordsFile)
            runInFxEdtNowOrLater { this.ignoredWords.setAll(ignoredWords) }
        }
    }

}


private val Window.isActive: Boolean get() =
    this.isShowing && this.isFocused

