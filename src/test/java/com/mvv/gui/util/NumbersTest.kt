package com.mvv.gui.util

import com.mvv.gui.test.useAssertJSoftAssertions
import org.junit.jupiter.api.Test


class NumbersTest {

    @Test
    @Suppress("SpellCheckingInspection")
    fun `toUHex of negative numbers`() { useAssertJSoftAssertions {
        assertThat(0x3.toByte().toUHex()).isEqualTo("03")
        assertThat(0xF.toByte().toUHex()).isEqualTo("0F")

        assertThat(0xFF.toByte().toUHex()).isEqualTo("FF")
        assertThat((-3).toByte().toUHex()).isEqualTo("FD")
        assertThat((-3).toByte().toString(16)).isEqualTo("-3")


        assertThat(0x3.toShort().toUHex()).isEqualTo("0003")
        assertThat(0xF.toShort().toUHex()).isEqualTo("000F")

        assertThat(0xFFFF.toShort().toUHex()).isEqualTo("FFFF")
        assertThat((-3).toShort().toUHex()).isEqualTo("FFFD")
        assertThat((-3).toShort().toString(16)).isEqualTo("-3")


        assertThat(4.toUHex()).isEqualTo("00000004")

        assertThat((-1).toUHex()).isEqualTo("FFFFFFFF")
        assertThat(0xFFFFFFFF.toInt().toUHex()).isEqualTo("FFFFFFFF")
        assertThat(0xFFFFFFFFL.toInt().toUHex()).isEqualTo("FFFFFFFF")

        assertThat((-1L).toUHex()).isEqualTo("FFFFFFFFFFFFFFFF")
    } }
}
