package com.mvv.gui.javafx

import com.mvv.gui.util.executeCommand
import com.mvv.win.programFiles
import javafx.application.Application
import javafx.scene.web.WebView
import javafx.stage.Stage
import org.apache.commons.lang3.SystemUtils.IS_OS_LINUX
import org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS
import kotlin.concurrent.thread


val WebView.selectedText: String get() =
    (this.engine.executeScript("window.getSelection().toString()") as String?) ?: ""

val WebView.textContent: String get() =
    (this.engine.executeScript("document.documentElement.outerText") as String?) ?: ""


private class TempAppStub : Application() {
    override fun start(primaryStage: Stage?) = throw IllegalStateException("It should ne be created.")
}


enum class BrowserType { Default, Opera }

fun openDefaultWebBrowser(url: String) = TempAppStub().hostServices.showDocument(url)

fun openWebBrowser(browser: BrowserType = BrowserType.Default, url: String) {
    when (browser) {
        BrowserType.Default -> openDefaultWebBrowser(url)
        BrowserType.Opera   -> {
            val exitCodeOfReusingExistentSession = 24
            thread (isDaemon = true, name = "web-browser $browser") {
                when {
                    IS_OS_LINUX   -> executeCommand(
                        listOf("opera", url),
                        listOf(0, exitCodeOfReusingExistentSession))
                    IS_OS_WINDOWS -> executeCommand(
                        listOf(programFiles.resolve("Opera/opera.exe").toString(), url),
                        listOf(0, exitCodeOfReusingExistentSession))
                }
            }
        }
    }
}
