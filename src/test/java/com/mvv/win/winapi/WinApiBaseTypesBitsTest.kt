@file:Suppress("SpellCheckingInspection")
package com.mvv.win.winapi

import com.mvv.gui.test.useAssertJSoftAssertions
import com.mvv.gui.util.toUHex
import org.junit.jupiter.api.Test


class WinApiBaseTypesBitsTest {

    @Test
    fun conversions() { useAssertJSoftAssertions {
        assertThat(0xFEABFFFF.toInt().toUHex()).isEqualTo("FEABFFFF")
    } }


    @Test
    fun `byte to unsigneds`() { useAssertJSoftAssertions {
        val b: Byte = -1 // 0xFF.toByte()
        assertThat(b.toWORD()  .toUHex()).isEqualTo("00FF")
        assertThat(b.toUSHORT().toUHex()).isEqualTo("00FF")
        assertThat(b.toDWORD() .toUHex()).isEqualTo("000000FF")
        assertThat(b.toULONG() .toUHex()).isEqualTo("000000FF")
        assertThat(b.toUInt64().toUHex()).isEqualTo("00000000000000FF")
    } }


    @Test
    fun `short to unsigneds`() { useAssertJSoftAssertions {
        val s: Short = -1 // 0xFFFF.toShort()
        assertThat(s.toWORD()  .toUHex()).isEqualTo("FFFF")
        assertThat(s.toUSHORT().toUHex()).isEqualTo("FFFF")
        assertThat(s.toDWORD() .toUHex()).isEqualTo("0000FFFF")
        assertThat(s.toULONG() .toUHex()).isEqualTo("0000FFFF")
        assertThat(s.toUInt64().toUHex()).isEqualTo("000000000000FFFF")
    } }


    @Test
    fun `int to unsigneds`() { useAssertJSoftAssertions {
        val i: Int = -1
        assertThat(i.toWORD()  .toUHex()).isEqualTo("FFFF")
        assertThat(i.toUSHORT().toUHex()).isEqualTo("FFFF")
        assertThat(i.toDWORD() .toUHex()).isEqualTo("FFFFFFFF")
        assertThat(i.toULONG() .toUHex()).isEqualTo("FFFFFFFF")
        assertThat(i.toUInt64().toUHex()).isEqualTo("00000000FFFFFFFF")
    } }


    @Test
    fun `long to unsigneds`() { useAssertJSoftAssertions {
        val l: Long = -1L
        assertThat(l.toWORD()  .toUHex()).isEqualTo("FFFF")
        assertThat(l.toUSHORT().toUHex()).isEqualTo("FFFF")
        assertThat(l.toDWORD() .toUHex()).isEqualTo("FFFFFFFF")
        assertThat(l.toULONG() .toUHex()).isEqualTo("FFFFFFFF")
        assertThat(l.toUInt64().toUHex()).isEqualTo("FFFFFFFFFFFFFFFF")
    } }


    @Test
    fun `long to unsigneds 2`() { useAssertJSoftAssertions {
        val l = 0x8090809L
        assertThat(l.toWORD()  .toUHex()).isEqualTo("0809")
        assertThat(l.toUSHORT().toUHex()).isEqualTo("0809")
        assertThat(l.toDWORD() .toUHex()).isEqualTo("08090809")
        assertThat(l.toULONG() .toUHex()).isEqualTo("08090809")
        assertThat(l.toUInt64().toUHex()).isEqualTo("0000000008090809")
    } }
}
