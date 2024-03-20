package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.util.doIfSuccess
import com.mvv.gui.words.addTranscriptions


fun LearnWordsController.addTranscriptions() =
    addTranscriptions(currentWords, dictionary)
        .doIfSuccess { markDocumentIsDirty() }
