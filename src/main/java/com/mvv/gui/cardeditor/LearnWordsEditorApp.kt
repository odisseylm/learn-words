package com.mvv.gui.cardeditor

import com.mvv.gui.javafx.installJavaFxLogger
import javafx.application.Application
import javafx.application.Preloader
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.stage.Screen
import javafx.stage.Stage


internal class SetApplicationNamePreloader : Preloader() {
    override fun start(stage: Stage) =
        // this code does not work if just to call it from main()
        com.sun.glass.ui.Application.GetApplication().setName("Learn Words Editor")
}

class LearnWordsEditorApp : Application() {

    override fun start(stage: Stage) = showMain(stage)

    private fun showMain(primaryStage: Stage) {

        installJavaFxLogger()

        val controller = LearnWordsController()
        val mainWordsPane = controller.pane

        val scene = Scene(mainWordsPane)
        primaryStage.setScene(scene)
        primaryStage.title = appTitle

        primaryStage.scene = scene

        scene.stylesheets.addAll(mainWordsPane.stylesheets)

        val screens = Screen.getScreens()
        val minScreenWidth = screens.minOf { it.bounds.width }
        val minScreenHeight = screens.minOf { it.bounds.height }

        mainWordsPane.prefWidth = minScreenWidth * 0.8
        mainWordsPane.prefHeight = minScreenHeight * 0.8

        primaryStage.isMaximized = settings.isMaximized
        if (!primaryStage.isMaximized)
            primaryStage.centerOnScreen()

        primaryStage.onCloseRequest = EventHandler { closeRequestEvent ->
            val canQuit = controller.doIsCurrentDocumentIsSaved("Close application")
            if (canQuit)
                controller.close()
            else
                closeRequestEvent.consume()
        }

        primaryStage.show()
    }

    companion object {
        // for running with good java class name LearnWordsEditorApp
        @JvmStatic fun main(args: Array<String>) = launch(LearnWordsEditorApp::class.java)
    }
}


fun main() {
    System.setProperty("javafx.preloader", SetApplicationNamePreloader::class.java.name)
    Application.launch(LearnWordsEditorApp::class.java)
}
