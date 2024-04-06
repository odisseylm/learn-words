package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.cardeditor.ShowError
import com.mvv.gui.words.baseWordsFilename
import com.mvv.gui.words.isInternalCsvFormat
import java.nio.file.Path



fun LearnWordsController.saveAllAndExportToMemoWord() {

     val processedFiles = this.saveAll()
     //val processedMemoFiles = processedFiles.filter { it.file.isMemoWordFile && it.file.extension == "xlsx" } // for direct upload
     val processedInternalFiles = processedFiles.filter { it.file.isInternalCsvFormat }

     runAsyncTask("Upload to MemoWord", ShowError.Message) {

          /*
          val setNamesToDelete = processedMemoFiles
               .filter { it.operation == FileSaveResult.Operation.Deleted }
               .map { it.file.asMemoWordSetName }

          val filesToSave = processedMemoFiles
               .filter { it.operation == FileSaveResult.Operation.Saved }
               .map { it.file }

          memoWord.connect()

          memoWord.deleteExistentMemoLists(setNamesToDelete)

          // This approach creates duplicates.
          filesToSave.forEach { f ->
               memoWord.uploadMemoList(f.asMemoWordSetName, f, rewrite = true)
          }
          */

          val filesToSave = processedInternalFiles
               .filter { it.operation == FileSaveResult.Operation.Saved }
               .map { it.file }

          val setNamesToDelete = processedInternalFiles
               .filter { it.operation == FileSaveResult.Operation.Deleted }
               .map { it.file }
               .filterNot { it in filesToSave } // to make sure
               .map { it.asMemoWordSetName }

          memoWord.connect()

          filesToSave.forEach { f ->
               memoWord.saveMemoList(f)
          }
          memoWord.deleteExistentMemoLists(setNamesToDelete)
          memoWord.deleteTempMemoLists() // in case of success
     }
}

private val Path.asMemoWordSetName: String get() =
     this.baseWordsFilename
          .replace("_0", " 0")
          .replace("_OnlyWords", " OnlyWords")
