package com.mvv.gui.javafx

import com.mvv.gui.util.ActionCanceledException
import javafx.application.Platform


private val log = mu.KotlinLogging.logger {}

fun installJavaFxLogger() {
    if (Platform.isFxApplicationThread()) {
        Thread.currentThread().setUncaughtExceptionHandler { _, ex ->
            if (ex is ActionCanceledException) log.debug("Action is [{}] canceled. ({})", ex.action, ex.toString(), ex)
            else log.error("Error in JavaFX EDT [{}]", ex.toString(), ex)
        }
    }
    else Platform.runLater { installJavaFxLogger() }
}
