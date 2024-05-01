@file:Suppress("FunctionName", "SpellCheckingInspection", "unused", "PackageDirectoryMismatch")
package com.mvv.win.winapi.dwm

import com.mvv.win.*
import com.mvv.win.winapi.*
import com.mvv.win.winapi.error.HR_SUCCEEDED
import com.mvv.win.winapi.window.HWND
import com.mvv.win.winapi.window.ValueLayout_HWND
//import com.mvv.foreign.MemoryLayout
//import com.mvv.foreign.MemorySegment
//import com.mvv.foreign.StructLayout


// https://learn.microsoft.com/en-us/windows/win32/api/dwmapi/nf-dwmapi-dwmgetcolorizationcolor
//
fun DwmGetColorizationColor(): DWORD? = nativeContext {
    // HRESULT DwmGetColorizationColor( [out] DWORD *pcrColorization, [out] BOOL *pfOpaqueBlend )

    val pcrColorization = allocateDWORD(0)
    val pfOpaqueBlend = allocateBOOL(false)

    val hResult = functionHandle(
        WinModule.Dwmapi, "DwmGetColorizationColor",
        ValueLayout_HRESULT, ValueLayout_PTR.withTargetLayout(ValueLayout_DWORD), ValueLayout_PTR.withTargetLayout(
            ValueLayout_BOOL
        ))
        .call<HRESULT>(pcrColorization, pfOpaqueBlend)

    println(pfOpaqueBlend.getAtIndex(ValueLayout_DWORD, 0))
    if (HR_SUCCEEDED(hResult)) pcrColorization.getAtIndex(ValueLayout_DWORD, 0) else null
}


fun DwmGetWindowAttribute(hWnd: HWND, dwAttribute: DWMWINDOWATTRIBUTE): DWORD? =
    DwmGetWindowAttribute(hWnd, dwAttribute.nativeValue)

// https://learn.microsoft.com/en-us/windows/win32/api/dwmapi/nf-dwmapi-dwmgetwindowattribute
//
fun DwmGetWindowAttribute(hWnd: HWND, dwAttribute: DWORD): DWORD? = nativeContext {
    // Dwmapi.dll / Uxtheme.dll / Dwmapi.lib / dwmapi.h
    //
    // HRESULT DwmGetWindowAttribute(
    //  [in]  HWND  hwnd,
    //  [in]  DWORD dwAttribute,
    //  [out] PVOID pvAttribute,
    //  [in]  DWORD cbAttribute
    // )

    val dwBuffer = allocateDWORD(0)

    val hr = functionHandle(
        WinModule.Dwmapi, "DwmGetWindowAttribute",
            ValueLayout_HRESULT,
            ValueLayout_HWND, ValueLayout_DWORD, ValueLayout_PTR, ValueLayout_DWORD
        )
        .call<HRESULT>(hWnd, dwAttribute, dwBuffer, ValueLayout_DWORD.dwByteSize)

    if (HR_SUCCEEDED(hr)) dwBuffer.getAtIndex(ValueLayout_DWORD, 0) else null
}


fun DwmSetWindowAttribute(hWnd: HWND, dwAttribute: DWMWINDOWATTRIBUTE, bValue: Boolean): HRESULT =
    DwmSetWindowAttribute(hWnd, dwAttribute.nativeValue, bValue.asBOOL)
fun DwmSetWindowAttribute(hWnd: HWND, dwAttribute: DWMWINDOWATTRIBUTE, dwValue: DWORD): HRESULT =
    DwmSetWindowAttribute(hWnd, dwAttribute.nativeValue, dwValue)

fun DwmSetWindowAttribute(hWnd: HWND, dwAttribute: DWORD, dwValue: DWORD): HRESULT = nativeContext {
    // Dwmapi.dll / Uxtheme.dll / Dwmapi.lib / dwmapi.h
    // HRESULT WINAPI DwmSetWindowAttribute (
    //   HWND hWnd,
    //   DWORD dwAttribute,
    //   LPCVOID pvAttribute,
    //   DWORD cbAttribute
    // )
    functionHandle(
        WinModule.Dwmapi, "DwmSetWindowAttribute",
            ValueLayout_HRESULT,
            ValueLayout_HWND, ValueLayout_DWORD, ValueLayout_PTR, ValueLayout_DWORD
        )
        .call<HRESULT>(hWnd, dwAttribute, allocateDWORD(dwValue), ValueLayout_DWORD.dwByteSize)
}


@Suppress("UNUSED_PARAMETER")
fun SetWindowCompositionAttribute(hWnd: HWND, attribute: WINDOWCOMPOSITIONATTRIB, dwValue: DWORD): Boolean = false
@Suppress("UNUSED_PARAMETER")
fun SetWindowCompositionAttribute(hWnd: HWND, attribute: WINDOWCOMPOSITIONATTRIB, value: Boolean): Boolean = false
@Suppress("UNUSED_PARAMETER")
fun GetWindowCompositionAttribute(hWnd: HWND, dwAttribute: WINDOWCOMPOSITIONATTRIB): DWORD? = null

/*
fun SetWindowCompositionAttribute(hWnd: HWND, attribute: WINDOWCOMPOSITIONATTRIB, dwValue: DWORD): Boolean =
    SetWindowCompositionAttribute(hWnd, attribute.nativeValue, dwValue)
fun SetWindowCompositionAttribute(hWnd: HWND, attribute: WINDOWCOMPOSITIONATTRIB, value: Boolean): Boolean =
    SetWindowCompositionAttribute(hWnd, attribute, value.asBOOL)

// https://learn.microsoft.com/en-us/windows/win32/dwm/setwindowcompositionattribute
//
fun SetWindowCompositionAttribute(hWnd: HWND, dwAttribute: DWORD, dwValue: DWORD): Boolean = nativeContext {
    // BOOL SetWindowCompositionAttribute( HWND hwnd, [IN] const WINDOWCOMPOSITIONATTRIBDATA* pwcad )

    val attrData = WINDOWCOMPOSITIONATTRIBDATA(this)
    attrData.attribute = dwAttribute
    attrData.attributeValue = dwValue

    val fh = functionHandle(
        WinModule.User, "SetWindowCompositionAttribute",
        ValueLayout_BOOL, ValueLayout_HWND, ValueLayout_PTR.withTargetLayout(attrData.structLayout))

    fh.call<BOOL>(hWnd, attrData.memorySegment).asBool

    // Causes Classic light appearance, like Windows 7
    //boolBuffer.set(ValueLayout_BOOL, 0, true.asBOOL)
    //windowCompositionAttribData.set(attr, attrOffset, WCA_NCRENDERING_EXILED)
    //fh.call<BOOL>(hWnd, windowCompositionAttribData)

    //// !!! Seems also working approach !!!
    //windowCompositionAttribData.set(attr, attrOffset, WCA_FREEZE_REPRESENTATION)
    //boolBuffer.set(ValueLayout_BOOL, 0, false.asBOOL)
    //fh.call<BOOL>(hWnd, windowCompositionAttribData)
    //boolBuffer.set(ValueLayout_BOOL, 0, true.asBOOL)
    //fh.call<BOOL>(hWnd, windowCompositionAttribData)
}


// Semms it always fails ((
//
fun GetWindowCompositionAttribute(hWnd: HWND, dwAttribute: WINDOWCOMPOSITIONATTRIB): DWORD? =
    GetWindowCompositionAttribute(hWnd, dwAttribute.nativeValue)

// https://learn.microsoft.com/en-us/windows/win32/dwm/getwindowcompositionattribute
//
fun GetWindowCompositionAttribute(hWnd: HWND, dwAttribute: DWORD): DWORD? = nativeContext {
    // BOOL GetWindowCompositionAttribute( HWND hwnd, [INOUT] WINDOWCOMPOSITIONATTRIBDATA* pwcad )

    val attrData = WINDOWCOMPOSITIONATTRIBDATA(this)
    attrData.attribute = dwAttribute

    val fh = functionHandle(
        WinModule.User, "GetWindowCompositionAttribute",
        ValueLayout_BOOL, ValueLayout_HWND, ValueLayout_PTR.withTargetLayout(attrData.structLayout))

    val success = fh.call<BOOL>(hWnd, attrData.memorySegment).asBool

    if (success) attrData.attributeValue else null
}


// https://learn.microsoft.com/en-us/windows/win32/dwm/windowcompositionattribdata
//
// typedef struct tagWINDOWCOMPOSITIONATTRIBDATA {
//    WINDOWCOMPOSITIONATTRIB Attrib;
//    void* pvData;
//    UINT cbData;
// } WINDOWCOMPOSITIONATTRIBDATA;
//
private class WINDOWCOMPOSITIONATTRIBDATA (nativeContext: NativeContext) {
    private val attr = ValueLayout_DWORD.withName("Attrib").withByteAlignment(8)
    private val pvData = ValueLayout_PTR.withName("pvData").withByteAlignment(8)
    private val cbData = ValueLayout_UINT.withName("cbData").withByteAlignment(8)

    val structLayout: StructLayout = MemoryLayout.structLayout(
        attr,
        MemoryLayout.paddingLayout(4), // T O D O: how to avoid it?
        pvData,
        cbData,
        MemoryLayout.paddingLayout(4),
    ).withName("WINDOWCOMPOSITIONATTRIBDATA")

    private val attrOffset = structLayout.byteOffset(MemoryLayout.PathElement.groupElement(attr.name().get()))
    private val pvDataOffset = structLayout.byteOffset(MemoryLayout.PathElement.groupElement(pvData.name().get()))
    private val cbDataOffset = structLayout.byteOffset(MemoryLayout.PathElement.groupElement(cbData.name().get()))

    private val dwValueBuffer = nativeContext.allocateDWORD(0)
    val memorySegment: MemorySegment = nativeContext.arena.allocate(structLayout)

    init {
        memorySegment.set(attr, attrOffset, 0)
        memorySegment.set(pvData, pvDataOffset, dwValueBuffer)
        memorySegment.set(cbData, cbDataOffset, structLayout.dwByteSize)
    }

    var attribute: DWORD
        get() = memorySegment.get(attr, attrOffset)
        set(value) = memorySegment.set(attr, attrOffset, value)

    var attributeValue: DWORD
        get() = dwValueBuffer.getAtIndex(ValueLayout_DWORD, 0)
        set(value) = dwValueBuffer.setAtIndex(ValueLayout_DWORD, 0, value)
}
*/
