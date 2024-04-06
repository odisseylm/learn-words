package com.mvv.gui.words

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class SrtTest {

    @Test
    fun test_isSrtTimeStampsLine() {
        assertThat("00:24:19,379 --> 00:24:29,379".isSrtTimeStampsLine).isTrue()

        assertThat("000:24:19,379 --> 00:24:29,379".isSrtTimeStampsLine).isFalse()
        assertThat("00:24:19,379 --> 000:24:29,379".isSrtTimeStampsLine).isFalse()
        assertThat(" 00:24:19,379 --> 00:24:29,379".isSrtTimeStampsLine).isFalse()
        assertThat("00:24:19,379 --> 00:24:29,379 ".isSrtTimeStampsLine).isFalse()
    }

    @Test
    fun test_isSrtTimestamp() {
        assertThat("00:23:03,281".isSrtTimestamp).isTrue()

        assertThat("000:023:003,0281".isSrtTimestamp).isFalse()
        assertThat("000:23:03,281".isSrtTimestamp).isFalse()
        assertThat("00:023:03,281".isSrtTimestamp).isFalse()
        assertThat("00:23:003,281".isSrtTimestamp).isFalse()
        assertThat("00:23:03,0281".isSrtTimestamp).isFalse()

        assertThat("00:23:03".isSrtTimestamp).isFalse()
        assertThat("00:23".isSrtTimestamp).isFalse()
        assertThat("00".isSrtTimestamp).isFalse()
        assertThat("".isSrtTimestamp).isFalse()
        assertThat("john".isSrtTimestamp).isFalse()
    }
}
