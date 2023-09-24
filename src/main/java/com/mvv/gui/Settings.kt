package com.mvv.gui

import com.mvv.gui.dictionary.getProjectDirectory
import com.mvv.gui.util.userHome
import com.mvv.gui.words.SentenceEndRule
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
)

val settings: Settings by lazy { loadSettings() }


private const val defaultSplitWordCountPerFile: Int = 50
private val defaultTheme: Theme = Theme.System


private fun loadSettings(): Settings {
    val possiblePaths = listOf(
        getProjectDirectory(Settings::class).resolve(".config.properties"),
        userHome.resolve(".config.properties"),
    )

    val props = Properties()

    possiblePaths
        .firstOrNull { it.exists() }
        ?.let { configFile -> FileReader(configFile.toFile(), UTF_8).use { r -> props.load(r) } }

    return Settings(
        splitWordCountPerFile = props.getProperty("splitWordCountPerFile", defaultSplitWordCountPerFile.toString()).toInt(),
        theme = Theme.valueOf(props.getProperty("theme", defaultTheme.name)),
        isMaximized = props.getProperty("isWindowMaximized", "false").toBoolean(),
        sentenceEndRule = props.getProperty("sentenceEndRule", "ByEndingDotOrLineBreak")
            .let { SentenceEndRule.valueOf(it) },
        toAutoRemoveIgnored = props.getProperty("toAutoRemoveIgnored", "true").toBoolean(),
    )
}


private val recentsFile = userHome.resolve(".learn-words/recents.txt")
private val recentCount = 5

class RecentDocuments {

    val recents: List<Path> get() =
        if (recentsFile.exists())
            recentsFile.readText(UTF_8).split('\n').map { it.trim() }.distinct().take(recentCount).map { Path.of(it) }
            else emptyList()

    fun addRecent(file: Path) {
        val newRecents = (listOf(file) + recents).map { it.toString() }.distinct().take(recentCount)

        recentsFile.parent.createDirectories()
        recentsFile.writeText(newRecents.joinToString("\n"))
    }
}
