package com.mvv.win

import com.mvv.gui.test.useAssertJSoftAssertions
import com.mvv.win.winapi.gdi.CreateSolidBrush
import com.mvv.win.winapi.gdi.DeleteObject
import com.mvv.win.winapi.window.GetForegroundWindow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS


@EnabledOnOs(OS.WINDOWS)
class WinApiWindowFunctionsKtTest {

    @Test
    fun getForegroundWindow() { useAssertJSoftAssertions {
        assertThat(GetForegroundWindow())
            .isNotIn(-1, 0, 1)
    } }

    @Test
    fun createSolidBrush() { useAssertJSoftAssertions {
        val brush = CreateSolidBrush(0xFF00FF00L.toInt())
        assertThat(brush).isNotIn(0, -1)

        DeleteObject(brush)
    } }
}