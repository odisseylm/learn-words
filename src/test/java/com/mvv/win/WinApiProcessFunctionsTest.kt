package com.mvv.win

import com.mvv.gui.test.useAssertJSoftAssertions
import com.mvv.win.winapi.processthreadsapi.GetCurrentThreadId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS


@EnabledOnOs(OS.WINDOWS)
class WinApiProcessFunctionsTest {

    @Test
    fun getCurrentThreadId() { useAssertJSoftAssertions {
        assertThat(GetCurrentThreadId())
            .isNotNull().isNotIn(-1, 0, 1)
    } }
}
