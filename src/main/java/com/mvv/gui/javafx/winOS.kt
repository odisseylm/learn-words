package com.mvv.gui.javafx

import com.mvv.gui.util.findReflectionField
import com.mvv.win.UseWindowDarkMode
import javafx.stage.Window
import org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS

/*
import com.sun.jna.Library
import com.sun.jna.PointerType
import com.sun.jna.platform.win32.WinDef
import javafx.stage.Window
import java.lang.annotation.Native


fun getNativeHandleForStage(stage: Stage): WinDef.HWND? {
    try {
        val getPeer: Method = Window::class.java.getDeclaredMethod("getPeer", null)
        getPeer.isAccessible = true
        val tkStage = getPeer.invoke(stage)
        val getRawHandle = tkStage.javaClass.getMethod("getRawHandle")
        getRawHandle.isAccessible = true
        val pointer: Pointer = Pointer(getRawHandle.invoke(tkStage) as Long)
        return HWND(pointer)
    } catch (ex: Exception) {
        System.err.println("Unable to determine native handle for window")
        return null
    }
}


fun setDarkMode(stage: Stage, darkMode: Boolean) {
    val hwnd: Any = FXWinUtil.getNativeHandleForStage(stage)
    val dwmapi: Any = Dwmapi.INSTANCE
    val darkModeRef: WinDef.BOOLByReference = BOOLByReference(BOOL(darkMode))

    dwmapi.DwmSetWindowAttribute(hwnd, 20, darkModeRef, Native.getNativeSize(WinDef.BOOLByReference::class.java))

    forceRedrawOfWindowTitleBar(stage)
}

private fun forceRedrawOfWindowTitleBar(stage: Stage) {
    val maximized = stage.isMaximized
    stage.isMaximized = !maximized
    stage.isMaximized = maximized
}


interface Dwmapi : Library {
    fun DwmSetWindowAttribute(hwnd: WinDef.HWND?, dwAttribute: Int, pvAttribute: PointerType?, cbAttribute: Int): Int

    companion object {
        val INSTANCE: Dwmapi = Native.load("dwmapi", Dwmapi::class.java)
    }
}

FXWinUtil.setDarkMode(stage, true);
*/

val Window.handle: Long? get() {
    val wnd = this

    // this (javafx.stage.Stage)
    //    -> peer (com.sun.javafx.tk.quantum.WindowStage)
    //        -> platformWindow (com.sun.glass.ui.win.WinWindow)
    //            -> private long ptr;

    // com.sun.javafx.tk.quantum.WindowStage
    val peer = wnd.findReflectionField("peer", "impl_peer", "peer_impl")?.get(wnd) ?: return null
    // com.sun.glass.ui.win.WinWindow
    val winWnd = peer.findReflectionField("platformWindow")?.get(peer) ?: return null
    // long
    //val hWnd = winWnd.findReflectionField("ptr", "handle", "hWnd")?.get(winWnd) as Long?
    val hWnd = (winWnd as com.sun.glass.ui.Window).rawHandle // .nativeHandle

    return hWnd
}

fun setDarkTitle(wnd: Window): Boolean {

    if (!IS_OS_WINDOWS) return false

    val hWnd = wnd.handle
    println("### hWnd: ${hWnd?.toString(16)}")

    if (hWnd == null) {
        return false
    }

    UseWindowDarkMode(hWnd)

    return true
}
