package com.mvv.gui

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Screen
import javafx.stage.Stage


class LearnWordsEditorApp : Application() {

    override fun start(stage: Stage) = showMain(stage)

    private fun showMain(primaryStage: Stage) {

        val mainWordsPane = MainWordsPane()
        LearnWordsController(mainWordsPane)

        val scene = Scene(mainWordsPane)
        primaryStage.setScene(scene)
        primaryStage.title = appTitle

        primaryStage.scene = scene

        val screens = Screen.getScreens()
        val minScreenWidth = screens.minOf { it.bounds.width }
        val minScreenHeight = screens.minOf { it.bounds.height }

        mainWordsPane.prefWidth = minScreenWidth * 0.8
        mainWordsPane.prefHeight = minScreenHeight * 0.8

        primaryStage.isMaximized = settings.isMaximized
        if (!primaryStage.isMaximized)
            primaryStage.centerOnScreen()

        primaryStage.show()
    }

    companion object {
        // for running with good java class name LearnWordsEditorApp
        @JvmStatic fun main(args: Array<String>) = launch(LearnWordsEditorApp::class.java)
    }
}


fun main() = Application.launch(LearnWordsEditorApp::class.java)
