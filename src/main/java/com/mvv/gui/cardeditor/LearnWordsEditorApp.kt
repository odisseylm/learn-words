package com.mvv.gui.cardeditor

import com.mvv.gui.javafx.installJavaFxLogger
import com.mvv.gui.javafx.setDarkTitle
import com.mvv.gui.util.isOneOf
import javafx.application.Application
import javafx.application.Preloader
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.Window
import javafx.stage.WindowEvent
import org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS


internal class SetApplicationNamePreloader : Preloader() {
    override fun start(stage: Stage) {

        // SetThemeAppProperties(STAP_VALIDBITS)

        // this code does not work if just to call it from main()
        com.sun.glass.ui.Application.GetApplication().setName("Learn Words Editor")
    }
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

    // To avoid short white-black blinking we initially show very small window
    // and change it to big size only after fixing background (see fixDark)
    stage.width  = 5.0
    stage.height = 5.0
    stage.centerOnScreen()

    val scene = Scene(mainWordsPane)

    stage.title = appTitle
    stage.scene = scene
    scene.stylesheets.addAll(mainWordsPane.stylesheets)

    val screens = Screen.getScreens()
    val minScreenWidth  = screens.minOf { it.bounds.width  }
    val minScreenHeight = screens.minOf { it.bounds.height }

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

    stage.addEventHandler(WindowEvent.WINDOW_SHOWN) {
        // T O D O: We need to do it after creating native window but BEFORE showing that window,
        //          however JavaFX (in contrast to AWT with it addNotify()) does not have such explicit or implicit event.
        fixDarkTheme(stage)
    }

    stage.icons.addAll(
        //Image(LearnWordsEditorApp::class.java.getResourceAsStream("/icons/app/book-01-64.svg")),
        Image(LearnWordsEditorApp::class.java.getResourceAsStream("/icons/app/book-01-256.png")),
        //
        //Image(LearnWordsEditorApp::class.java.getResourceAsStream("/icons/app/book-03-512.png")),
        //Image(LearnWordsEditorApp::class.java.getResourceAsStream("/icons/app/book-03-256.png")),
        //Image(LearnWordsEditorApp::class.java.getResourceAsStream("/icons/app/book-03-128.png")),
        //Image(LearnWordsEditorApp::class.java.getResourceAsStream("/icons/app/book-03-64.png")),
        //Image(LearnWordsEditorApp::class.java.getResourceAsStream("/icons/app/book-03-48.png")),
        //Image(LearnWordsEditorApp::class.java.getResourceAsStream("/icons/app/book-03-32.png")),
    )

    stage.show()

    stage.width  = minScreenWidth  * 0.8
    stage.height = minScreenHeight * 0.8

    stage.isMaximized = settings.isMaximized
    if (!stage.isMaximized)
        stage.centerOnScreen()

    return stage
}

fun fixDarkTheme(wnd: Window) {
    if (IS_OS_WINDOWS && settings.theme.isOneOf(Theme.Dark, Theme.SystemDark))
        setDarkTitle(wnd)
}
