package com.mvv.gui.cardeditor

import com.mvv.gui.dictionary.getProjectDirectory
import com.mvv.gui.util.trimToNull
import com.mvv.gui.util.userHome
import com.mvv.gui.words.SentenceEndRule
import com.mvv.gui.words.isInternalCsvFormat
import javafx.scene.Parent
import java.io.FileReader
import java.nio.file.Path
import java.util.Properties
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.text.Charsets.UTF_8


enum class Theme {
    System,
    SystemDark,
    Dark,
}

class Settings (
    val splitWordCountPerFile: Int = defaultSplitWordCountPerFile,
    val theme: Theme = defaultTheme,
    val isMaximized: Boolean = false,
    val sentenceEndRule: SentenceEndRule = SentenceEndRule.ByEndingDotOrLineBreak,
    val toAutoRemoveIgnored: Boolean = true,
    val autoPlay: Boolean = true,
    val warnAboutDuplicatesInOtherSets: Boolean = true,
    // means examples which can (recommended) be turned into cards (for learning)
    val tooManyExampleCardCandidatesCount: Int = 5,
    val recentDocumentsCount: Int = 10,
    val showSynonyms: Boolean = true,
    val openInNewWindow: Boolean = false,

    val memoLogin: String?    = null,
    val memoPassword: String? = null,
    val memoWordLanguageProfileId: String?   = null, // for ru-en it can be 665ebd51-66cb-43d7-9ad0-ee3f0b489710
    val memoWordLanguageProfileName: String? = null, // "Ru-En"
    val memoWordAuthor: String? = null, // "Ru-En"
)

val settings: Settings by lazy { loadSettings() }


private const val defaultSplitWordCountPerFile: Int = 50
private val defaultTheme: Theme = Theme.System


data class MemoSettings (
    val login: String,                   // email
    val password: String,
    val languageProfileId: String,   // for ru-en it can be 665ebd51-66cb-43d7-9ad0-ee3f0b489710
    val languageProfileName: String, // "Ru-En"
    val author: String, // "Ru-En"
)

val Settings.memoSettings: MemoSettings? get() {
    if (memoLogin == null) return null

    return MemoSettings(
        login = memoLogin,
        password = requireNotNull(memoPassword),
        languageProfileId = requireNotNull(memoWordLanguageProfileId),
        languageProfileName = requireNotNull(memoWordLanguageProfileName),
        author = requireNotNull(memoWordAuthor),
    )
}

private fun loadSettings(): Settings {
    val possiblePaths = listOf(
        getProjectDirectory(Settings::class).resolve(".config.properties"),
        userHome.resolve(".learnEditorConfig.properties"),
        // overriding value from local non-git files
        getProjectDirectory(Settings::class).resolve(".local.config.properties"),
    )

    val props = Properties()

    possiblePaths
        .filter { it.exists() }
        .forEach { configFile -> FileReader(configFile.toFile(), UTF_8).use { r -> props.load(r) } }

    val defSet = Settings()

    return Settings(
        splitWordCountPerFile = props.getInt("splitWordCountPerFile") ?: defSet.splitWordCountPerFile,
        theme = Theme.valueOf(props.getProperty("theme", defaultTheme.name)),
        isMaximized = props.getBool("isWindowMaximized") ?: defSet.isMaximized,
        sentenceEndRule = props.getProperty("sentenceEndRule", defSet.sentenceEndRule.name).let { SentenceEndRule.valueOf(it) },
        toAutoRemoveIgnored = props.getBool("toAutoRemoveIgnored") ?: defSet.toAutoRemoveIgnored,
        autoPlay = props.getBool("autoPlay") ?: defSet.autoPlay,
        warnAboutDuplicatesInOtherSets = props.getBool("warnAboutDuplicatesInOtherSets") ?: defSet.warnAboutDuplicatesInOtherSets,
        tooManyExampleCardCandidatesCount = props.getInt("tooManyExampleCardCandidatesCount") ?: defSet.tooManyExampleCardCandidatesCount,
        recentDocumentsCount = props.getInt("recentDocumentsCount") ?: defSet.recentDocumentsCount,
        showSynonyms = props.getBool("showSynonyms") ?: defSet.showSynonyms,
        openInNewWindow = props.getBool("openInNewWindow") ?: defSet.openInNewWindow,

        memoLogin = props.getProperty("memoLogin"),
        memoPassword = props.getProperty("memoPassword"),
        memoWordLanguageProfileId = props.getProperty("memoWordLanguageProfileId"),
        memoWordLanguageProfileName = props.getProperty("memoWordLanguageProfileName"),
        memoWordAuthor = props.getProperty("memoWordAuthor"),
    )
}


private fun Properties.getBool(propName: String): Boolean? = this.getProperty(propName)?.trimToNull()?.toBoolean()
private fun Properties.getInt(propName: String): Int? = this.getProperty(propName)?.trimToNull()?.toInt()


private val recentFilesFile = userHome.resolve(".learn-words/recents.txt")
private val recentDirectoriesFile = userHome.resolve(".learn-words/recentDirs.txt")

class RecentDocuments {

    private val recentCount = settings.recentDocumentsCount

    val recentFiles: List<Path> get() =
        if (recentFilesFile.exists())
            recentFilesFile.readText(UTF_8).split('\n').map { it.trim() }.distinct().take(recentCount).map { Path.of(it) }
            else emptyList()

    val recentDirectories: List<Path> get() =
        if (recentDirectoriesFile.exists())
            recentDirectoriesFile.readText(UTF_8).split('\n').map { it.trim() }.distinct().take(recentCount).map { Path.of(it) }
            else emptyList()

    fun addRecent(file: Path) {
        if (file.isInternalCsvFormat) {
            val newRecentFiles = (listOf(file) + recentFiles).map { it.toString() }.distinct().take(recentCount)
            recentFilesFile.parent.createDirectories()
            recentFilesFile.writeText(newRecentFiles.joinToString("\n"))
        }

        val newRecentDirs = (listOf(file.parent) + recentDirectories).map { it.toString() }.distinct().take(recentCount)
        recentDirectoriesFile.parent.createDirectories()
        recentDirectoriesFile.writeText(newRecentDirs.joinToString("\n"))
    }
}


fun Parent.initThemeAndStyles() {
    val pane = this
    when (settings.theme) {
        Theme.System -> { }
        Theme.SystemDark -> pane.style = "-fx-base:black" // standard JavaFX dark theme
        Theme.Dark -> pane.stylesheets.add("dark-theme.css")
    }

    // after possible adding theme CSS
    pane.stylesheets.add("spreadsheet.css")
}
