package com.mvv.gui.words

import com.mvv.gui.test.useAssertJSoftAssertions
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.DisplayName

class WordIOTest {

    @Test
    @DisplayName("fileNameEnding")
    fun test_fileNameEnding() { useAssertJSoftAssertions {
        assertThat(CsvFormat.Internal.fileNameEnding).isEqualTo(".csv")
        assertThat(CsvFormat.MemoWord.fileNameEnding).isEqualTo(" - RuEn-MemoWord.csv")
    } }
}
