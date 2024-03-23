package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.cardeditor.ShowError
import com.mvv.gui.words.baseWordsFilename
import com.mvv.gui.words.isMemoWordFile
import java.nio.file.Path


fun LearnWordsController.saveAllAndExportToMemoWord() {

     val processedFiles = this.saveAll()
     val processedMemoFiles = processedFiles.filter { it.file.isMemoWordFile }

     runAsyncTask("Upload to MemoWord", ShowError.Message) {

          val setNamesToDelete = processedMemoFiles
               .filter { it.operation == FileSaveResult.Operation.Deleted }
               .map { it.file.asMemoWordSetName }

          val filesToSave = processedMemoFiles
               .filter { it.operation == FileSaveResult.Operation.Saved }
               .map { it.file }

          memoWord.connect()

          memoWord.deleteExistentCardSets(setNamesToDelete)

          filesToSave.forEach { f ->
               memoWord.uploadCardSet(f.asMemoWordSetName, f, rewrite = true)
          }
     }
}

private val Path.asMemoWordSetName: String get() =
     this.baseWordsFilename
          .replace("_0", " 0")
          .replace("_OnlyWords", " OnlyWords")
