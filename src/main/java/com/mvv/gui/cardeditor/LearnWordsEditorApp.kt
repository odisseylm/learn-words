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

        val appContext = AppContext()

        showLearnEditor(appContext, primaryStage)
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


fun showLearnEditor(appContext: AppContext, stage: Stage? = null): Stage =
    showLearnEditorImpl(null, appContext, stage)
fun showLearnEditor(controller: LearnWordsController, stage: Stage? = null): Stage =
    showLearnEditorImpl(controller, controller.appContext, stage)

private fun showLearnEditorImpl(controller0: LearnWordsController?, appContext: AppContext, stage0: Stage? = null): Stage {

    val controller: LearnWordsController = controller0 ?: LearnWordsController(appContext, isReadOnly = false)
    val mainWordsPane = controller.pane
    val stage = stage0 ?: Stage()

    val scene = Scene(mainWordsPane)

    stage.title = appTitle
    stage.scene = scene
    scene.stylesheets.addAll(mainWordsPane.stylesheets)

    val screens = Screen.getScreens()
    val minScreenWidth  = screens.minOf { it.bounds.width  }
    val minScreenHeight = screens.minOf { it.bounds.height }

    mainWordsPane.prefWidth  = minScreenWidth  * 0.8
    mainWordsPane.prefHeight = minScreenHeight * 0.8

    stage.isMaximized = settings.isMaximized
    if (!stage.isMaximized)
        stage.centerOnScreen()

    stage.onCloseRequest = EventHandler { closeRequestEvent ->
        val canQuit = controller.doIsCurrentDocumentSaved("Close application")
        if (canQuit) {
            controller.close()

            appContext.openEditors.remove(controller)

            if (appContext.openEditors.isEmpty())
                // shutdown all threads to complete application
                appContext.close()
        }
        else
            closeRequestEvent.consume()
    }

    appContext.openEditors.add(controller)

    stage.show()

    return stage
}