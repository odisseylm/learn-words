@file:Suppress("SpellCheckingInspection", "TestFunctionName")

package com.mvv.win.winapi

import com.mvv.gui.test.useAssertJSoftAssertions
import com.mvv.gui.util.findReflectionField
import com.mvv.gui.util.reflectionField
import com.mvv.win.RedrawNCWindow
import com.mvv.win.winapi.dwm.GetWindowCompositionAttribute
import com.mvv.win.winapi.dwm.SetWindowCompositionAttribute
import com.mvv.win.winapi.dwm.WINDOWCOMPOSITIONATTRIB
import com.mvv.win.winapi.window.IsWindow
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS


@EnabledOnOs(OS.WINDOWS)
class DwmapiTest {

    companion object {
        private val window = TestWindow()

        @BeforeAll @JvmStatic
        fun createWindow()  {
            assertThat(window.hWnd)
                .describedAs("Testing window is not created ")
                .isNotNull()
        }

        @AfterAll @JvmStatic
        fun destroyWindow()  {
            //Thread.sleep(30_000)
            window.destroy()
        }
    }

    @Test
    fun IsWindow() { useAssertJSoftAssertions {
        assertThat(IsWindow(window.hWnd))
            .isTrue
    } }

    @Test
    fun `GetWindowCompositionAttribute for bool attribute`() { useAssertJSoftAssertions {
        // It is ONLY one docummneted attribute.
        // See https://learn.microsoft.com/en-us/windows/win32/dwm/windowcompositionattrib
        val excludedFromDDA = GetWindowCompositionAttribute(window.hWnd, WINDOWCOMPOSITIONATTRIB.WCA_EXCLUDED_FROM_DDA)
        assertThat(excludedFromDDA)
            .isNotNull

        // These attributes undocummenetd and functin may return null depending on Windows version/build
        //
        //
        val useDarkModeColors = GetWindowCompositionAttribute(window.hWnd, WINDOWCOMPOSITIONATTRIB.WCA_USEDARKMODECOLORS)?.asBool
        assertThat(useDarkModeColors)
            .isNotNull

        assertThat(GetWindowCompositionAttribute(window.hWnd, WINDOWCOMPOSITIONATTRIB.WCA_PASSIVEUPDATEMODE)?.asBool)
            .isNotNull

        assertThat(GetWindowCompositionAttribute(window.hWnd, WINDOWCOMPOSITIONATTRIB.WCA_HOLOGRAPHIC)?.asBool)
            .isNotNull

        //assertThat(GetWindowCompositionAttribute(window.hWnd, WINDOWCOMPOSITIONATTRIB.WCA_VISUAL_OWNER)?.asBool)
        //    .isNotNull

        assertThat(GetWindowCompositionAttribute(window.hWnd, WINDOWCOMPOSITIONATTRIB.WCA_EVER_UNCLOAKED)?.asBool)
            .isNotNull

        assertThat(GetWindowCompositionAttribute(window.hWnd, WINDOWCOMPOSITIONATTRIB.WCA_FREEZE_REPRESENTATION)?.asBool)
            .isNotNull

        assertThat(GetWindowCompositionAttribute(window.hWnd, WINDOWCOMPOSITIONATTRIB.WCA_CLOAKED)?.asBool)
            .isNotNull

        //assertThat(GetWindowCompositionAttribute(window.hWnd, WINDOWCOMPOSITIONATTRIB.WCA_DISALLOW_PEEK)?.asBool)
        //    .isNotNull

        //assertThat(GetWindowCompositionAttribute(window.hWnd, WINDOWCOMPOSITIONATTRIB.WCA_FORCE_ACTIVEWINDOW_APPEARANCE)?.asBool)
        //    .isNotNull

        //assertThat(GetWindowCompositionAttribute(window.hWnd, WINDOWCOMPOSITIONATTRIB.WCA_VIDEO_OVERLAY_ACTIVE)?.asBool)
        //    .isNotNull

        //assertThat(GetWindowCompositionAttribute(window.hWnd, WINDOWCOMPOSITIONATTRIB.WCA_EXCLUDED_FROM_LIVEPREVIEW)?.asBool)
        //    .isNotNull

        //assertThat(GetWindowCompositionAttribute(window.hWnd, WINDOWCOMPOSITIONATTRIB.WCA_NCRENDERING_EXILED)?.asBool)
        //    .isNotNull

        //assertThat(GetWindowCompositionAttribute(window.hWnd, WINDOWCOMPOSITIONATTRIB.WCA_HAS_ICONIC_BITMAP)?.asBool)
        //    .isNotNull

        //assertThat(GetWindowCompositionAttribute(window.hWnd, WINDOWCOMPOSITIONATTRIB.WCA_FORCE_ICONIC_REPRESENTATION)?.asBool)
        //    .isNotNull

        //assertThat(GetWindowCompositionAttribute(window.hWnd, WINDOWCOMPOSITIONATTRIB.WCA_NONCLIENT_RTL_LAYOUT)?.asBool)
        //    .isNotNull

        //assertThat(GetWindowCompositionAttribute(window.hWnd, WINDOWCOMPOSITIONATTRIB.WCA_NONCLIENT_RTL_LAYOUT)?.asBool)
        //    .isNotNull

        //assertThat(GetWindowCompositionAttribute(window.hWnd, WINDOWCOMPOSITIONATTRIB.WCA_ALLOW_NCPAINT)?.asBool)
        //    .isNotNull

        //assertThat(GetWindowCompositionAttribute(window.hWnd, WINDOWCOMPOSITIONATTRIB.WCA_TRANSITIONS_FORCEDISABLED)?.asBool)
        //    .isNotNull

        assertThat(GetWindowCompositionAttribute(window.hWnd, WINDOWCOMPOSITIONATTRIB.WCA_NCRENDERING_ENABLED)?.asBool)
            .isNotNull

        // And for other attributes it does not reurn anything
        // However setter may work
        /*
        val ncRenderingPolicy = GetWindowCompositionAttribute(window.hWnd, WINDOWCOMPOSITIONATTRIB.WCA_NCRENDERING_POLICY)
        assertThat(ncRenderingPolicy)
            .isNotNull
            //.isNotEqualTo(0)

        val ncRenderingPolicy2 = GetWindowCompositionAttribute(window.hWnd, WINDOWCOMPOSITIONATTRIB.WCA_NCRENDERING_POLICY.nativeValue)
        assertThat(ncRenderingPolicy2)
            .isNotNull
            //.isNotEqualTo(0)

        val unknowAttribute = 9999
        val unknowAttributeValue = GetWindowCompositionAttribute(window.hWnd, unknowAttribute)
        assertThat(unknowAttributeValue)
            .isNull()
        */
    } }

    @Test
    fun SetWindowCompositionAttribute() { useAssertJSoftAssertions {

        var useDarkModeColors = GetWindowCompositionAttribute(window.hWnd, WINDOWCOMPOSITIONATTRIB.WCA_USEDARKMODECOLORS)?.asBool
        assertThat(useDarkModeColors)
            .isNotNull
            .isFalse

        window.show()

        val success = SetWindowCompositionAttribute(window.hWnd, WINDOWCOMPOSITIONATTRIB.WCA_USEDARKMODECOLORS, true)
        assertThat(success)
            .isTrue

        RedrawNCWindow(window.hWnd)

        useDarkModeColors = GetWindowCompositionAttribute(window.hWnd, WINDOWCOMPOSITIONATTRIB.WCA_USEDARKMODECOLORS)?.asBool
        assertThat(useDarkModeColors)
            .isNotNull
            .isTrue

        window.hide()
    } }
}


class TestWindow {

    //private val frame = java.awt.Frame()
    private val frame = javax.swing.JFrame()
        .also {
            it.isEnabled = false
            it.pack() // it creates hidden window
        }

    // Now it needs permissions
    //   --add-modules jdk.incubator.foreign
    //   --enable-native-access=ALL-UNNAMED
    //   --add-opens java.desktop/java.awt=ALL-UNNAMED
    //   --add-opens java.desktop/sun.awt.windows=ALL-UNNAMED
    //
    val hWnd: Long get() {
        val peer = (frame.reflectionField("peer")
            .also { it.trySetAccessible() }
            .get(frame)) // as java.awt.peer.ComponentPeer
        val hWnd = peer.findReflectionField("hwnd", "hWnd", "handle")
            ?.also { it.trySetAccessible() }
            ?.get(peer) as Long
        return hWnd
    }

    fun show() {
        frame.isEnabled = true
        frame.setBounds(100, 100, 100, 100)
        frame.defaultCloseOperation = javax.swing.WindowConstants.DISPOSE_ON_CLOSE
        frame.isVisible = true
    }
    fun hide() {
        // hide, but not destroy
        frame.isVisible = false
    }
    fun destroy() = frame.dispose()
}

/*
fun main() {
    println("DwmapiTest main")
    TestWindow().show()
}
*/
