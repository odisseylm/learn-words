package com.mvv.gui

import com.mvv.gui.dictionary.getProjectDirectory
import com.mvv.gui.util.userHome
import java.io.FileReader
import java.util.Properties
import kotlin.io.path.exists
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
    )
}
