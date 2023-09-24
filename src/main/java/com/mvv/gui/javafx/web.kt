package com.mvv.gui.javafx

import javafx.application.Application
import javafx.scene.web.WebView
import javafx.stage.Stage


val WebView.selectedText: String get() =
    (this.engine.executeScript("window.getSelection().toString()") as String?) ?: ""

val WebView.textContent: String get() =
    (this.engine.executeScript("document.documentElement.outerText") as String?) ?: ""


private class TempAppStub : Application() {
    override fun start(primaryStage: Stage?) = throw IllegalStateException("It should ne be created.")
}

fun openWebBrowser(url: String) = TempAppStub().hostServices.showDocument(url)
