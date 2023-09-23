package com.mvv.gui.javafx

import com.mvv.gui.util.ActionCanceledException
import javafx.application.Platform


private val log = mu.KotlinLogging.logger {}

fun installJavaFxLogger() {
    if (Platform.isFxApplicationThread()) {
        Thread.currentThread().setUncaughtExceptionHandler { _, ex ->
            if (ex is ActionCanceledException) log.debug(ex) { "Action is [${ex.action}] canceled. ($ex)" }
            // seems mu.KotlinLogging does not print exception if it is passed as last param !?!
            // I would say that it is bug!
            //else log.error("Error in JavaFX EDT [{}]", ex.toString(), ex)
            else log.error(ex) { "Error in JavaFX EDT [$ex]" }
        }
    }
    else Platform.runLater { installJavaFxLogger() }
}
