package com.mvv.gui.cardeditor

import com.mvv.gui.cardeditor.actions.*
import com.mvv.gui.dictionary.*
import com.mvv.gui.dictionary.Dictionary
import com.mvv.gui.javafx.*
import com.mvv.gui.memoword.MemoWordSession
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
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.io.path.*


private val log = mu.KotlinLogging.logger {}


/**
 * Application global/shared state objects to reuse with several windows.
 */
class AppContext : AutoCloseable {

    val voiceManager = VoiceManager()

    val dictionary: Dictionary by lazy { CachedDictionary( DictionaryComposition(loadDictionaries()) ) }

    val allWordCardSetsManager: AllWordCardSetsManager by lazy { AllWordCardSetsManager() }

    private val defaultTaskExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    val taskManager = TaskManager(javaFxRunner)

    val ignoredWords: ObservableList<String> = FXCollections.observableArrayList()
    val ignoredWordsSorted: ObservableList<String> = SortedList(ignoredWords, String.CASE_INSENSITIVE_ORDER)

    val allProcessedWords: ObservableList<String> = FXCollections.observableArrayList()

    val openEditors: MutableList<LearnWordsController> = mutableListOf()

    fun runAsyncTask(name: String, task: ()->Unit) = taskManager.addTask(name, defaultTaskExecutor, task)

    private fun loadDictionaries() = AutoDictionariesLoader().load()
        .also { dictionaries -> // HardcodedDictionariesLoader().load()
                log.info(
                    "Used dictionaries\n" +
                            dictionaries.mapIndexed { i, d -> "${i + 1} $d" }.joinToString("\n") +
                            "\n---------------------------------------------------\n\n"
                )
            }

    private fun loadIgnored() {
        if (ignoredWordsFile.exists()) {
            val ignoredWords = loadWords(ignoredWordsFile)
            runInFxEdtNowOrLater { this.ignoredWords.setAll(ignoredWords) }
        }
    }

    internal fun loadExistentWords() {
        runAsyncTask("Loading ignored") { loadIgnored() }

        runAsyncTask("Loading existent words") {
            // TODO: use AllWordCardSetsManager
            val fromAllExistentDictionaries = loadWordsFromAllExistentDictionaries(null)
            runInFxEdtNowOrLater { allProcessedWords.setAll(fromAllExistentDictionaries) }
        }
    }

    override fun close() {
        allWordCardSetsManager.close()
        taskManager.close()
        defaultTaskExecutor.shutdown()
    }

    fun quit() {
        val editors = openEditors.toList() // safe unchangeable list
        val appContext = editors.first().appContext

        val unsaved = editors.filter { it.documentIsDirty }

        if (unsaved.isEmpty()) {
            editors.forEach { it.close(); openEditors.remove(it) }
            appContext.close()
        }
        else {
            val active = editors.find { it.pane.scene.window.isActive } ?: editors.first()
            active.pane.activateWindow()

            showInfoAlert(active.pane, "Some documents are unsaved.\nPlease close them manually.", appTitle)

            if (!active.documentIsDirty)
                unsaved.first().pane.activateWindow()
        }
    }
}



class LearnWordsController (
    val appContext: AppContext,
    val isReadOnly: Boolean,
    ): AutoCloseable {

    internal val log = mu.KotlinLogging.logger {}

    //private val projectDirectory = getProjectDirectory(this.javaClass)

    internal val dictionary: Dictionary get() = appContext.dictionary
    internal val voiceManager: VoiceManager get() = appContext.voiceManager
    internal val allWordCardSetsManager: AllWordCardSetsManager get() = appContext.allWordCardSetsManager
    internal val ignoredWords: ObservableList<String> get() = appContext.ignoredWords
    internal val ignoredWordsSorted: ObservableList<String> get() = appContext.ignoredWordsSorted


    internal val navigationHistory = NavigationHistory()

    internal val pane = MainWordsPane(this)

    internal val currentWordsList: WordCardsTable get() = pane.wordEntriesTable
    internal val currentWords: ObservableList<CardWordEntry> get() = currentWordsList.items

    internal var currentWordsFile: Path? = null

    internal val cellEditorStates = WeakHashMap<Pair<TableColumn<CardWordEntry,*>, CardWordEntry>, CellEditorState>()

    internal val settingsPane = SettingsPane()
    private val toolBarController = ToolBarControllerBig(this)
    private val toolBar = ToolBar2(this).also {
        it.nextPrevWarningWord.addSelectedWarningsChangeListener { _, _, _ -> recalculateWarnedWordsCount() } }

    // we have to use lazy because creating popup before creating/showing main windows causes JavaFX hanging up :-)
    internal val otherCardsViewPopup: OtherCardsViewPopup by lazy { OtherCardsViewPopup(appContext) }
    internal val lightOtherCardsViewPopup: LightOtherCardsViewPopup by lazy { LightOtherCardsViewPopup() }
    internal val foundCardsViewPopup: OtherCardsViewPopup by lazy { OtherCardsViewPopup(appContext).also {
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

    internal val memoWord: MemoWordSession by lazy { MemoWordSession() }

    override fun close() {
        showSynonymInEditorTimer.cancel()

        runInFxEdtNowOrLater { pane.hideWindow() }
    }


    init {
        pane.initThemeAndStyles()

        val currentWordsLabelText = "File/Clipboard (%d words)"
        currentWords.addListener(ListChangeListener { pane.wordEntriesLabel.text = currentWordsLabelText.format(it.list.size) })

        val ignoredWordsLabelText = "Ignored words (%d)"
        appContext.ignoredWords.addListener(ListChangeListener { pane.ignoredWordsLabel.text = ignoredWordsLabelText.format(it.list.size) })

        pane.ignoredWordsList.items = appContext.ignoredWordsSorted

        val allProcessedWordsLabelText = "All processed words (%d)"
        appContext.allProcessedWords.addListener(ListChangeListener { pane.allProcessedWordsLabel.text = allProcessedWordsLabelText.format(it.list.size) })

        pane.allProcessedWordsList.items = SortedList(appContext.allProcessedWords, String.CASE_INSENSITIVE_ORDER)

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

        val cardAnyCellOnEditEventHandler = EventHandler<TableColumn.CellEditEvent<CardWordEntry, Any>> { onCardEdited(it.rowValue) }
        pane.wordEntriesTable.columns.forEach { it.addEventHandler(TableColumn.editCommitEvent(), cardAnyCellOnEditEventHandler) }

        addGlobalKeyBindings(currentWordsList, copyKeyCombinations.associateWith { {
            if (!currentWordsList.isEditing) copySelectedWord() } })

        addNonReadOnlyKeyBindings()

        pane.statusPane.right = appContext.taskManager.createFxProgressBar().also {
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

                runAsyncTask("Rebuilding prefix", ShowError.No) { rebuildPrefixFinderImpl(emptySet()) }

                // load dictionaries lazy
                runAsyncTask("Loading dictionaries", ShowError.Message) { appContext.dictionary.find("apple")  }

                if (appContext.allWordCardSetsManager.isEmpty())
                    appContext.allWordCardSetsManager.reloadAllSetsAsync(appContext.taskManager)
            }
        }

        installNavigationHistoryUpdates(currentWordsList, navigationHistory)

        if (appContext.ignoredWords.isEmpty())
            appContext.loadExistentWords()
    }

    private fun addNonReadOnlyKeyBindings() {
        addGlobalKeyBinding(pane, openDocumentKeyCodeCombination) { loadWordsFromFile(OpenDialogType.Standard) }
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
            val foundInOtherSets = appContext.allWordCardSetsManager.findBy(fixedWordOrPhrase, MatchMode.Exact)
            if (foundInOtherSets.isNotEmpty())
                showThisWordsInOtherSetsPopup(wordOrPhrase, foundInOtherSets)
        }

        rebuildPrefixFinder()
    }

    private fun onCardEdited(card: CardWordEntry) {
        markDocumentIsDirty()

        val now = ZonedDateTime.now()
        card.updatedAt = now
        if (card.createdAt.isUnset()) card.createdAt = now
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

    internal val currentWordsSelection: TableView.TableViewSelectionModel<CardWordEntry> get() = currentWordsList.selectionModel
    internal val currentWarnAboutMissedBaseWordsMode: WarnAboutMissedBaseWordsMode get() = pane.warnAboutMissedBaseWordsModeDropDown.value


    @Suppress("PrivatePropertyName")
    private var documentIsDirty_: Boolean = false

    internal val documentIsDirty: Boolean get() = this.documentIsDirty_
    internal fun markDocumentIsDirty() { this.documentIsDirty_ = true; Platform.runLater { updateTitle() } }
    internal fun resetDocumentIsDirty() { this.documentIsDirty_ = false; Platform.runLater { updateTitle() } }

    internal fun doIsCurrentDocumentSaved(currentAction: String = ""): Boolean =
        try { validateCurrentDocumentIsSaved(currentAction); true } catch (_: Exception) { false }


    internal val changeCardListener = ChangeListener<Any> { prop,_,_ ->
        markDocumentIsDirty()

        // in general, it is bad idea to use so many 'instanceOf' but it is not critical there
        if (prop is ReadOnlyProperty<*>) {
            val bean = prop.bean
            if (bean is CardWordEntry) reanalyzeOnlyWords(bean)
        }
    }

    fun runAsyncTask(name: String, showError: ShowError, task: ()->Unit) = appContext.runAsyncTask(name, wrapTask(name, task, showError))

    private fun wrapTask(taskName: String, task: ()->Unit, showError: ShowError): ()->Unit = {
        try { task() }
        catch (ex: Exception) {
            log.error(ex) { "Error of executing task '$taskName'" }
            when (showError) {
                ShowError.Message -> Platform.runLater {
                    showErrorAlert(pane, "${ex.javaClass.simpleName}\n${ex.message}", "$taskName - Error") }
                ShowError.No -> { }
            }
        }
    }
}

enum class ShowError { No, Message }

private val Window.isActive: Boolean get() =
    this.isShowing && this.isFocused
