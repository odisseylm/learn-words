package com.mvv.gui.cardeditor.actions

import com.mvv.gui.cardeditor.LearnWordsController
import com.mvv.gui.cardeditor.ShowError
import com.mvv.gui.util.containsOneOf
import com.mvv.gui.words.CardsGroup
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
               .filter { it.containsOneOf("_MemoWord", ".MemoWord") }

          val setNamesToDelete = processedInternalFiles
               .filter { it.operation == FileSaveResult.Operation.Deleted }
               .map { it.file }
               .filterNot { it in filesToSave } // to make sure
               .map { it.asMemoListName }

          memoWord.connect()

          filesToSave.forEach { f ->
               memoWord.saveMemoList(f.asMemoListName, f)
          }
          memoWord.deleteExistentMemoLists(setNamesToDelete)
          memoWord.deleteTempMemoLists() // in case of success
     }
}

private val Path.asMemoListName: String get() {

     val group = CardsGroup.values()
          //.find { it.fileBelongsToGroup(this) }
          .filter { it != CardsGroup.Root }
          .find { this.startsWith(it.directory) }

     val baseName = this.baseWordsFilename
          // Let's improve it a bit.
          .replace("_0", " 0")
          .replace("_OnlyWords", " OnlyWords")

     return if (group != null) "${group.groupName} - $baseName"
            else baseName
}
