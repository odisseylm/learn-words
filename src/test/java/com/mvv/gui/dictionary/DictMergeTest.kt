package com.mvv.gui.dictionary

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


class DictMergeTest {

    @Test
    @DisplayName("containsUniqueTranscriptionChars")
    fun test_containsUniqueTranscriptionChars() {
        assertThat("[ˈteıkən]".containsUniqueTranscriptionChars()).isTrue()
    }

}
