@file:Suppress("FunctionName", "unused")
package com.mvv.win

import com.mvv.win.winapi.*
import com.mvv.win.winapi.dwm.*
import com.mvv.win.winapi.error.HR_SUCCEEDED
import com.mvv.win.winapi.gdi.COLORREF
import com.mvv.win.winapi.gdi.CreateSolidBrush
import com.mvv.win.winapi.window.*


// See also
//  https://learn.microsoft.com/en-us/windows/apps/desktop/modernize/apply-windows-themes
//
//  https://github.com/mimoguz/custom_window
//  https://medium.com/swlh/customizing-the-title-bar-of-an-application-window-50a4ac3ed27e
//  https://github.com/kalibetre/CustomDecoratedJFrame
//  https://github.com/dukke/FXThemes
//
//  https://superuser.com/questions/1002847/how-to-change-default-window-background-color-in-windows-10
//
fun UseWindowDarkMode(
    hWnd: HWND,
    backgroundColor: COLORREF? = null,
    captionColor: COLORREF? = 0X00FF00FF, // null,
    borderColor: COLORREF? = null,
): Boolean = nativeContext {

    //IsAppThemed()
    //IsThemeActive()

    //var hTheme = GetWindowTheme(hWnd)
    //if (hTheme == 0L) {
    //    // TODO: test under Windows 11
    //    //AllowDarkModeForWindow(hWnd, true)
    //    //DarkModeEnabled(hWnd)
    //    //SetPreferredAppMode(SPDAM_FORCEDARK)
    //    //SendMessage(hWnd, WM_THEMECHANGED, 0, 0)
    //    //SetWindowTheme(hWnd, "DarkMode")
    //    //SetWindowTheme(hWnd, "DarkMode_Explorer")
    //    //hTheme = OpenThemeData(hWnd, "EXPLORERBAR")
    //    //hTheme = OpenThemeData(hWnd, "TOOLBAR")
    //    hTheme = OpenThemeData(hWnd, "WINDOW")
    //    //hTheme = GetWindowTheme(hWnd)
    //}
    //
    //
    //val iColorId = TMT_WINDOWFRAME // TMT_ACTIVECAPTION TMT_INACTIVECAPTION
    //val sysColor = GetThemeSysColor(hTheme, iColorId)
    //println("### sysColor: ${sysColor.toUInt().toString(16)}")
    //val brush = GetThemeSysColorBrush(hTheme, iColorId)


    val brush = CreateSolidBrush(backgroundColor ?: 0x00000000) // or darkgray 0x00505050

    // Seems system colors for GetSysColorBrush are not supported now
    //   https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-getsyscolor
    //    => "Windows 10 or greater: This value is not supported."
    //
    //val brush = GetSysColorBrush(COLOR_WINDOWFRAME) // similar to 0x00505050

    SetClassLongPtr(hWnd, GCLP_HBRBACKGROUND, brush)

    // https://learn.microsoft.com/en-us/windows/win32/api/dwmapi/nf-dwmapi-dwmsetwindowattribute
    // https://learn.microsoft.com/en-us/windows/win32/api/dwmapi/ne-dwmapi-dwmwindowattribute

    // Dwmapi.dll / Uxtheme.dll / Dwmapi.lib / dwmapi.h
    // HRESULT WINAPI DwmSetWindowAttribute (HWND hwnd, DWORD dwAttribute, LPCVOID pvAttribute, DWORD cbAttribute);
    //
    //SetWindowTheme(hWnd, "DarkMode_Explorer", null)
    //SetWindowTheme(hWnd, "Explorer", null)
    //SetWindowTheme(hWnd, "Explorer", null)
    //SetWindowTheme(hWnd, "aero", null)

    //val cornerPref33 = DwmGetWindowAttribute(hWnd, DWMWINDOWATTRIBUTE.DWMWA_WINDOW_CORNER_PREFERENCE)
    //val freezeRepresentation = DwmGetWindowAttribute(hWnd, DWMWINDOWATTRIBUTE.DWMWA_FREEZE_REPRESENTATION)
    //val ncRenderingEnabled = DwmGetWindowAttribute(hWnd, DWMWINDOWATTRIBUTE.DWMWA_NCRENDERING_ENABLED)
    //val useImmersiveDarkMode = DwmGetWindowAttribute(hWnd, DWMWINDOWATTRIBUTE.DWMWA_USE_IMMERSIVE_DARK_MODE)
    //val captionColor33 = DwmGetWindowAttribute(hWnd, DWMWINDOWATTRIBUTE.DWMWA_CAPTION_COLOR)
    //val borderColor33 = DwmGetWindowAttribute(hWnd, DWMWINDOWATTRIBUTE.DWMWA_BORDER_COLOR)
    //val flip3dPolicy = DwmGetWindowAttribute(hWnd, DWMWINDOWATTRIBUTE.DWMWA_FLIP3D_POLICY)
    //val ncRenderingPolicy = DwmGetWindowAttribute(hWnd, DWMWINDOWATTRIBUTE.DWMWA_NCRENDERING_POLICY)
    //val systemBackDropType = DwmGetWindowAttribute(hWnd, DWMWINDOWATTRIBUTE.DWMWA_SYSTEMBACKDROP_TYPE)

    // !!! DWMWA_USE_IMMERSIVE_DARK_MODE is supported starting with Windows 11 Build 22000 !!!
    val hrImmersive = DwmSetWindowAttribute(hWnd, DWMWINDOWATTRIBUTE.DWMWA_USE_IMMERSIVE_DARK_MODE, true)
    val successImmersive = HR_SUCCEEDED(hrImmersive)

    val successUseDarkModeColors = SetWindowCompositionAttribute(hWnd, WINDOWCOMPOSITIONATTRIB.WCA_USEDARKMODECOLORS, true)

    val darkModeIsSet = successImmersive || successUseDarkModeColors

    // !!!
    // https://learn.microsoft.com/en-us/windows/win32/controls/cookbook-overview?redirectedfrom=MSDN
    //
    // HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows\CurrentVersion\Themes


    // only in Windows 11 SDK
    if (captionColor != null)
        DwmSetWindowAttribute(hWnd, DWMWINDOWATTRIBUTE.DWMWA_CAPTION_COLOR, captionColor)
    if (borderColor != null)
        DwmSetWindowAttribute(hWnd, DWMWINDOWATTRIBUTE.DWMWA_BORDER_COLOR, borderColor)

    //var hTheme = GetWindowTheme(hWnd)
    //if (hTheme == 0L) {
        // TODO: test under Windows 11
        //AllowDarkModeForWindow(hWnd, true)
        //DarkModeEnabled(hWnd)
        //SetPreferredAppMode(SPDAM_FORCEDARK)
        //SendMessage(hWnd, WM_THEMECHANGED, 0, 0)
        //SetWindowTheme(hWnd, "DarkMode")
        //SetWindowTheme(hWnd, "DarkMode_Explorer")
        //hTheme = OpenThemeData(hWnd, "EXPLORERBAR")
        //hTheme = OpenThemeData(hWnd, "TOOLBAR")
        //hTheme = OpenThemeData(hWnd, "WINDOW")
        //hTheme = OpenThemeData(hWnd, "Explorer::WINDOW")
        //hTheme = OpenThemeData(hWnd, "aa4545")
        //hTheme = GetWindowTheme(hWnd)

        //println("GetThemeFilename: " + GetThemeFilename(hTheme, 1, 1, 0)) // hResult = unsupported
        //println("GetThemeFilename: " + GetThemeFilename(hTheme, 1, 1, 1)) // hResult = unsupported
        //for (i in 0..100)
        //    println("GetThemeFilename: " + GetThemeFilename(hTheme, 1, 1, i))

        //val color333 = DwmGetColorizationColor()
        //println("color333: ${color333?.toUInt()?.toString(16)}")
    //}

    //println("### sysColor ???: ${GetThemeSysColor(hTheme, 99999).toUInt().toString(16)}")
    //println("### sysColor TMT_ACTIVECAPTION: ${GetThemeSysColor(hTheme, TMT_ACTIVECAPTION).toUInt().toString(16)}")
    //println("### sysColor TMT_INACTIVECAPTION: ${GetThemeSysColor(hTheme, TMT_INACTIVECAPTION).toUInt().toString(16)}")
    //println("### sysColor TMT_WINDOWFRAME: ${GetThemeSysColor(hTheme, TMT_WINDOWFRAME).toUInt().toString(16)}")
    //println("### sysColor TMT_WINDOW: ${GetThemeSysColor(hTheme, TMT_WINDOW).toUInt().toString(16)}")
    //println("### sysColor TMT_ACTIVEBORDER: ${GetThemeSysColor(hTheme, TMT_ACTIVEBORDER).toUInt().toString(16)}")
    //println("### sysColor TMT_INACTIVEBORDER: ${GetThemeSysColor(hTheme, TMT_INACTIVEBORDER).toUInt().toString(16)}")
    //println("### sysColor TMT_MENUBAR: ${GetThemeSysColor(hTheme, TMT_MENUBAR).toUInt().toString(16)}")
    //println("### sysColor TMT_BORDERCOLOR: ${GetThemeSysColor(hTheme, TMT_BORDERCOLOR).toUInt().toString(16)}")
    //println("### sysColor TMT_FILLCOLOR: ${GetThemeSysColor(hTheme, TMT_FILLCOLOR).toUInt().toString(16)}")

    //RedrawNCWindow(hWnd)

    //val color333 = DwmGetColorizationColor()
    //println("color333: ${color333?.toUInt()?.toString(16)}")

    //println("### sysColor TMT_ACTIVECAPTION: ${GetThemeSysColor(hTheme, TMT_ACTIVECAPTION).toUInt().toString(16)}")
    //println("### sysColor TMT_INACTIVECAPTION: ${GetThemeSysColor(hTheme, TMT_INACTIVECAPTION).toUInt().toString(16)}")
    //println("### sysColor TMT_WINDOWFRAME: ${GetThemeSysColor(hTheme, TMT_WINDOWFRAME).toUInt().toString(16)}")
    //println("### sysColor TMT_WINDOW: ${GetThemeSysColor(hTheme, TMT_WINDOW).toUInt().toString(16)}")
    //println("### sysColor TMT_ACTIVEBORDER: ${GetThemeSysColor(hTheme, TMT_ACTIVEBORDER).toUInt().toString(16)}")
    //println("### sysColor TMT_INACTIVEBORDER: ${GetThemeSysColor(hTheme, TMT_INACTIVEBORDER).toUInt().toString(16)}")
    //println("### sysColor TMT_MENUBAR: ${GetThemeSysColor(hTheme, TMT_MENUBAR).toUInt().toString(16)}")
    //println("### sysColor TMT_BORDERCOLOR: ${GetThemeSysColor(hTheme, TMT_BORDERCOLOR).toUInt().toString(16)}")
    //println("### sysColor TMT_FILLCOLOR: ${GetThemeSysColor(hTheme, TMT_FILLCOLOR).toUInt().toString(16)}")

    if (darkModeIsSet) {
        RedrawNCWindow(hWnd)
    }

    darkModeIsSet
}


fun RedrawNCWindow(hWnd: HWND): Boolean {

    if (hWnd == 0L) return false

    //DwmSetWindowAttribute(hWnd, DWMWINDOWATTRIBUTE.DWMWA_WINDOW_CORNER_PREFERENCE, DWM_WINDOW_CORNER_PREFERENCE.DWMWCP_ROUNDSMALL.nativeValue)
    //val cornerPref = DwmGetWindowAttribute(hWnd, DWMWINDOWATTRIBUTE.DWMWA_WINDOW_CORNER_PREFERENCE)

    //val freezeRepresentation = DwmGetWindowAttribute(hWnd, DWMWINDOWATTRIBUTE.DWMWA_FREEZE_REPRESENTATION)
    //val ncRenderingEnabled = DwmGetWindowAttribute(hWnd, DWMWINDOWATTRIBUTE.DWMWA_NCRENDERING_ENABLED)
    //val useImmersiveDarkMode = DwmGetWindowAttribute(hWnd, DWMWINDOWATTRIBUTE.DWMWA_USE_IMMERSIVE_DARK_MODE)
    //val captionColor = DwmGetWindowAttribute(hWnd, DWMWINDOWATTRIBUTE.DWMWA_CAPTION_COLOR)
    //val borderColor = DwmGetWindowAttribute(hWnd, DWMWINDOWATTRIBUTE.DWMWA_BORDER_COLOR)
    //val flip3dPolicy = DwmGetWindowAttribute(hWnd, DWMWINDOWATTRIBUTE.DWMWA_FLIP3D_POLICY)
    //val currentNcRenderingPolicy = DwmGetWindowAttribute(hWnd, DWMWINDOWATTRIBUTE.DWMWA_NCRENDERING_POLICY)
    //val systemBackDropType = DwmGetWindowAttribute(hWnd, DWMWINDOWATTRIBUTE.DWMWA_SYSTEMBACKDROP_TYPE)

    // It does NOT help!
    //if (freezeRepresentation != null) {
    //    DwmSetWindowAttribute(hWnd, DWMWINDOWATTRIBUTE.DWMWA_FREEZE_REPRESENTATION, true)
    //    DwmSetWindowAttribute(hWnd, DWMWINDOWATTRIBUTE.DWMWA_FREEZE_REPRESENTATION, false)
    //    DwmSetWindowAttribute(hWnd, DWMWINDOWATTRIBUTE.DWMWA_FREEZE_REPRESENTATION, freezeRepresentation)
    //}

    // It does NOT help!
    //if (currentNcRenderingPolicy != null) {
    //    //DwmSetWindowAttribute(hWnd, DWMWINDOWATTRIBUTE.DWMWA_NCRENDERING_POLICY, DWMNCRENDERINGPOLICY.DWMNCRP_USEWINDOWSTYLE.nativeValue)
    //    //DwmSetWindowAttribute(hWnd, DWMWINDOWATTRIBUTE.DWMWA_NCRENDERING_POLICY, DWMNCRENDERINGPOLICY.DWMNCRP_DISABLED.nativeValue)
    //    DwmSetWindowAttribute(hWnd, DWMWINDOWATTRIBUTE.DWMWA_NCRENDERING_POLICY, DWMNCRENDERINGPOLICY.DWMNCRP_ENABLED.nativeValue)
    //
    //    DwmSetWindowAttribute(hWnd, DWMWINDOWATTRIBUTE.DWMWA_NCRENDERING_POLICY, currentNcRenderingPolicy)
    //}

    val currentForceActiveWindowAppearance = GetWindowCompositionAttribute(hWnd, WINDOWCOMPOSITIONATTRIB.WCA_FORCE_ACTIVEWINDOW_APPEARANCE)?.asBool

    SetWindowCompositionAttribute(hWnd, WINDOWCOMPOSITIONATTRIB.WCA_FORCE_ACTIVEWINDOW_APPEARANCE, false)
    SetWindowCompositionAttribute(hWnd, WINDOWCOMPOSITIONATTRIB.WCA_FORCE_ACTIVEWINDOW_APPEARANCE, true)
    SetWindowCompositionAttribute(hWnd, WINDOWCOMPOSITIONATTRIB.WCA_FORCE_ACTIVEWINDOW_APPEARANCE, false)

    if (currentForceActiveWindowAppearance != null)
        SetWindowCompositionAttribute(hWnd, WINDOWCOMPOSITIONATTRIB.WCA_FORCE_ACTIVEWINDOW_APPEARANCE, currentForceActiveWindowAppearance)

    //val currentForceActiveWindowAppearance33 = GetWindowCompositionAttribute(hWnd, WINDOWCOMPOSITIONATTRIB.WCA_FORCE_ACTIVEWINDOW_APPEARANCE)

    //var hDwp = BeginDeferWindowPos(2)
    ////hDwp = DeferWindowPos(hDwp, hWnd, 0L, 0, 0, 0, 0, SWP_NOSIZE or SWP_NOMOVE or SWP_FRAMECHANGED or SWP_SHOWWINDOW)
    //hDwp = DeferWindowPos(hDwp, hWnd, hWnd, 0, 0, 0, 0, SWP_NOSIZE or SWP_NOMOVE or SWP_FRAMECHANGED or SWP_SHOWWINDOW or RDW_INVALIDATE or RDW_UPDATENOW)
    //EndDeferWindowPos(hDwp)

    //PostMessage(hWnd, WM_DWMCOLORIZATIONCOLORCHANGED, 0, 0)
    //PostMessage(hWnd, WM_DWMWINDOWMAXIMIZEDCHANGE, 0, 0)
    //PostMessage(hWnd, WM_DWMNCRENDERINGCHANGED, 0, 0)
    //PostMessage(hWnd, WM_DWMNCRENDERINGCHANGED, 1, 0)
    //PostMessage(hWnd, WM_DWMCOMPOSITIONCHANGED, 1, 0)

    //SetWindowCompositionAttribute(hWnd, WINDOWCOMPOSITIONATTRIB.WCA_FORCE_ACTIVEWINDOW_APPEARANCE, true)
    //SetWindowCompositionAttribute(hWnd, WINDOWCOMPOSITIONATTRIB.WCA_FORCE_ACTIVEWINDOW_APPEARANCE, false)
    //val originalForceActiveWindowAppearance22 = GetWindowCompositionAttribute(hWnd, WINDOWCOMPOSITIONATTRIB.WCA_FORCE_ACTIVEWINDOW_APPEARANCE)?.asBool

    // Official, but non-working solution :-(
    //RedrawWindow(hWnd, RDW_FRAME or RDW_INVALIDATE or RDW_UPDATENOW or RDW_NOCHILDREN)
    RedrawWindow(hWnd, RDW_FRAME or RDW_INVALIDATE or RDW_UPDATENOW)
    //RedrawWindow(hWnd, RDW_FRAME or RDW_UPDATENOW)
    //RedrawWindow(hWnd, RDW_FRAME)

    // Official, but non-working solution :-(
    // SetWindowPos(hWnd, GetWindowRect(hWnd), SWP_FRAMECHANGED)
    // SetWindowPos(hWnd, null, SWP_FRAMECHANGED)
    // SetWindowPos(hWnd, null, SWP_FRAMECHANGED or SWP_NOOWNERZORDER or SWP_NOACTIVATE or SWP_NOOWNERZORDER or SWP_NOZORDER or SWP_SHOWWINDOW)

    // Also does not work.
    // Sending WM_NCPAINT, WM_NCCALCSIZE, WM_NCHITTEST, WM_THEMECHANGED, WM_STYLECHANGED (with -20 or -16) does not work.

    // !!! Really WORKING hack/approach for repainting caption/border !!!
    //PostMessage(hWnd, WM_NCACTIVATE, 1, 0)
    //PostMessage(hWnd, WM_NCACTIVATE, 0, 0)
    //PostMessage(hWnd, WM_NCACTIVATE, 1, 0)

    //val rc = GetWindowRect(hWnd)
    //if (rc != null) {
    //    // T O D O: It works only if not maximized
    //    SetWindowPos(hWnd, 0, rc.copy(bottom = rc.bottom - 1))
    //    SetWindowPos(hWnd, 0, rc, SWP_DRAWFRAME)
    //}

    //val currentStyle = GetWindowStyle(hWnd)
    //// It works with WS_CAPTION (of course) but causes layout blinking (of course)
    //val toRemoveTemporary = WS_CAPTION
    //ModifyWindowStyle(hWnd, dwRemove = toRemoveTemporary, dwAdd = 0, swpFlags = SWP_DRAWFRAME)
    //ModifyWindowStyle(hWnd, dwRemove = 0, dwAdd = toRemoveTemporary, swpFlags = SWP_DRAWFRAME)
    //SetWindowStyle(hWnd, currentStyle)
    //SetWindowPos(hWnd, null, SWP_DRAWFRAME)

    //val currentExStyle = GetWindowExStyle(hWnd)
    //val exToRemoveTemporary = WS_EX_STATICEDGE
    //ModifyWindowExStyle(hWnd, dwRemove = exToRemoveTemporary, swpFlags = SWP_DRAWFRAME)
    //ModifyWindowExStyle(hWnd, dwAdd = exToRemoveTemporary, swpFlags = SWP_DRAWFRAME)
    //SetWindowExStyle(hWnd, currentExStyle)
    //SetWindowPos(hWnd, 0, null, SWP_DRAWFRAME)

    //val currentExStyle = GetWindowExStyle(hWnd)
    //ModifyWindowExStyle(hWnd, dwRemove = WS_EX_CLIENTEDGE or WS_EX_STATICEDGE or WS_EX_WINDOWEDGE, dwAdd = 0, swpFlags = SWP_DRAWFRAME)
    //ModifyWindowExStyle(hWnd, dwAdd = WS_EX_STATICEDGE, swpFlags = SWP_DRAWFRAME)
    //SetWindowExStyle(hWnd, currentExStyle)
    //SetWindowPos(hWnd, 0, null, SWP_DRAWFRAME)

    //ModifyWindowExStyle(hWnd, dwAdd = WS_EX_LAYOUTRTL, swpFlags = SWP_DRAWFRAME)

    //SetWindowPos(hWnd, 0, GetWindowRect(hWnd), SWP_FRAMECHANGED)

    // The best hack
    //SetWindowPos(hWnd, 0, 0, 0, 0, 0,
    //    //SWP_FRAMECHANGED or SWP_NOMOVE or SWP_NOSIZE or SWP_NOREPOSITION or SWP_NOZORDER);
    //    SWP_NOACTIVATE or SWP_FRAMECHANGED or SWP_NOMOVE or SWP_NOSIZE or SWP_NOOWNERZORDER or SWP_NOZORDER or SWP_NOREPOSITION or SWP_NOREDRAW);


    //SetWindowCompositionAttribute22(hWnd)

    /*
    Public Declare Function SetWindowCompositionAttribute Lib "user32" (ByVal hWnd As Long, wca As WINDOWCOMPOSITIONATTRIBDATA) As Long
    Public Type WINDOWCOMPOSITIONATTRIBDATA
        Attrib As WINDOWCOMPOSITIONATTRIB
        pvData As Long
        cbData As Long
    End Type

    Const WCA_USEDARKMODECOLORS=26
    Dim tWCAD As WINDOWCOMPOSITIONATTRIBDATA
    Dim bValue As Long: bValue = 1
    tWCAD.Attrib = WCA_USEDARKMODECOLORS
    tWCAD.pvData = VarPtr(bValue)
    tWCAD.cbData = LenB(bValue)
    SetWindowCompositionAttribute hWnd, tWCAD
    */

    return true
}


// See https://habr.com/ru/companies/vk/articles/735716/

/*
    // !!! Working approach !!!
    //windowCompositionAttribData.set(attr, attrOffset, WCA_FORCE_ACTIVEWINDOW_APPEARANCE)
    //boolBuffer.set(ValueLayout_BOOL, 0, true.asBOOL)
    //fh.call<BOOL>(hWnd, windowCompositionAttribData)
    //boolBuffer.set(ValueLayout_BOOL, 0, false.asBOOL)
    //fh.call<BOOL>(hWnd, windowCompositionAttribData)

    //// !!! Seems also working approach !!!
    //windowCompositionAttribData.set(attr, attrOffset, WCA_FREEZE_REPRESENTATION)
    //boolBuffer.set(ValueLayout_BOOL, 0, false.asBOOL)
    //fh.call<BOOL>(hWnd, windowCompositionAttribData)
    //boolBuffer.set(ValueLayout_BOOL, 0, true.asBOOL)
    //fh.call<BOOL>(hWnd, windowCompositionAttribData)
*/
