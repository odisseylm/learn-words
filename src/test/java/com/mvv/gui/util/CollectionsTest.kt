package com.mvv.gui.util

import com.mvv.gui.test.useAssertJSoftAssertions
import org.junit.jupiter.api.Test


class CollectionsTest {

    @Test
    fun ifIndexNotFound() { useAssertJSoftAssertions {
       assertThat((-1).ifIndexNotFound(5)).isEqualTo(5)
       assertThat(0.ifIndexNotFound(5)).isEqualTo(1)
       assertThat(1.ifIndexNotFound(5)).isEqualTo(1)
    } }

    @Test
    fun testIfIndexNotFound() { useAssertJSoftAssertions {
        assertThat((-1).ifIndexNotFound {5 }).isEqualTo(5)
        assertThat(0.ifIndexNotFound { 5 }).isEqualTo(1)
        assertThat(1.ifIndexNotFound { 5 }).isEqualTo(1)
    } }

    @Test
    fun minFoundIndex() { useAssertJSoftAssertions {
        assertThat(minFoundIndex(-1, -1)).isEqualTo(-1)

        assertThat(minFoundIndex(0, -1)).isEqualTo(0)
        assertThat(minFoundIndex(-1, 0)).isEqualTo(0)

        assertThat(minFoundIndex(1, -1)).isEqualTo(1)
        assertThat(minFoundIndex(-1, 1)).isEqualTo(1)

        assertThat(minFoundIndex(2, 5)).isEqualTo(2)
        assertThat(minFoundIndex(5, 2)).isEqualTo(2)
    } }
}
