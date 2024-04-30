@file:Suppress("unused", "SpellCheckingInspection", "ClassName", "PackageDirectoryMismatch")
package com.mvv.win.winapi.dwm

import com.mvv.win.winapi.DWORD
import com.mvv.win.winapi.HANDLE
import com.mvv.win.winapi.PTR


// See dwmapi.h
//   C:\msys64\mingw64\include\dwmapi.h
//   C:\msys64\ucrt64\include\dwmapi.h
//   C:\msys64\usr\include\w32api\dwmapi.h


//typealias ULONGLONG = Long128

// typedef ULONGLONG DWM_FRAME_COUNT;
// typealias DWM_FRAME_COUNT = ULONGLONG

// typedef ULONGLONG QPC_TIME;
// typealias QPC_TIME = ULONGLONG

// typedef ULONGLONG QPC_TIME;
// typealias QPC_TIME = ULONGLONG

// typedef HANDLE HTHUMBNAIL;
typealias HTHUMBNAIL = HANDLE

// typedef HTHUMBNAIL *PHTHUMBNAIL;
typealias PHTHUMBNAIL = PTR


enum class DWMWINDOWATTRIBUTE (val nativeValue: DWORD) {
    DWMWA_NCRENDERING_ENABLED         (0),
    DWMWA_NCRENDERING_POLICY          (1),
    DWMWA_TRANSITIONS_FORCEDISABLED   (2),
    DWMWA_ALLOW_NCPAINT               (3),
    DWMWA_CAPTION_BUTTON_BOUNDS       (4),
    DWMWA_NONCLIENT_RTL_LAYOUT        (5),
    DWMWA_FORCE_ICONIC_REPRESENTATION (6),
    DWMWA_FLIP3D_POLICY               (7),
    DWMWA_EXTENDED_FRAME_BOUNDS       (8),
    DWMWA_HAS_ICONIC_BITMAP           (9),
    DWMWA_DISALLOW_PEEK               (10),
    DWMWA_EXCLUDED_FROM_PEEK          (11),
    DWMWA_CLOAK                       (12),
    DWMWA_CLOAKED                     (13),
    DWMWA_FREEZE_REPRESENTATION       (14),
    DWMWA_PASSIVE_UPDATE_MODE         (15),

    DWMWA_USE_HOSTBACKDROPBRUSH       (16),
    // !!! This value is supported starting with Windows 11 Build 22000. !!!
    DWMWA_USE_IMMERSIVE_DARK_MODE     (20),
    DWMWA_WINDOW_CORNER_PREFERENCE    (33),
    DWMWA_BORDER_COLOR                (34),
    DWMWA_CAPTION_COLOR               (35),
    DWMWA_TEXT_COLOR                  (36),
    DWMWA_VISIBLE_FRAME_BORDER_THICKNESS (37),
    DWMWA_SYSTEMBACKDROP_TYPE         (38),
    //DWMWA_LAST,
}


// https://learn.microsoft.com/en-us/windows/apps/desktop/modernize/apply-rounded-corners
//
// The DWM_WINDOW_CORNER_PREFERENCE enum for DwmSetWindowAttribute's third parameter, which tells the function
// what value of the enum to set.
// Copied from dwmapi.h
enum class DWM_WINDOW_CORNER_PREFERENCE(val nativeValue: Int) {
    // Let the system decide when to round window corners.
    DWMWCP_DEFAULT      (0),
    DWMWCP_DONOTROUND   (1),
    // Round the corners, if appropriate.
    DWMWCP_ROUND        (2),
    // Round the corners if appropriate, with a small radius.
    DWMWCP_ROUNDSMALL   (3),
}


enum class DWMFLIP3DWINDOWPOLICYE (val nativeValue: Int) {
    DWMFLIP3D_DEFAULT      (0),
    DWMFLIP3D_EXCLUDEBELOW (1),
    DWMFLIP3D_EXCLUDEABOVE (2),
    DWMFLIP3D_LAST         (3),
}


enum class DWMNCRENDERINGPOLICY (val nativeValue: Int) {
    DWMNCRP_USEWINDOWSTYLE (0),
    DWMNCRP_DISABLED       (1),
    DWMNCRP_ENABLED        (2),
    DWMNCRP_LAST           (3),
}


//#if NTDDI_VERSION >= NTDDI_WIN8
enum class GESTURE_TYPE  (val nativeValue: Int) {
    GT_PEN_TAP                 (0),
    GT_PEN_DOUBLETAP           (1),
    GT_PEN_RIGHTTAP            (2),
    GT_PEN_PRESSANDHOLD        (3),
    GT_PEN_PRESSANDHOLDABORT   (4),
    GT_TOUCH_TAP               (5),
    GT_TOUCH_DOUBLETAP         (6),
    GT_TOUCH_RIGHTTAP          (7),
    GT_TOUCH_PRESSANDHOLD      (8),
    GT_TOUCH_PRESSANDHOLDABORT (9),
    GT_TOUCH_PRESSANDTAP       (10),
}

enum class DWM_SHOWCONTACT (val nativeValue: Int) {
    DWMSC_DOWN      (0x1),
    DWMSC_UP        (0x2),
    DWMSC_DRAG      (0x4),
    DWMSC_HOLD      (0x8),
    DWMSC_PENBARREL (0x10),
    DWMSC_NONE      (0x0),
    DWMSC_ALL       (0xfffffff),
}

enum class DWM_TAB_WINDOW_REQUIREMENTS (val nativeValue: Int) {
    DWMTWR_NONE                  (0x0000),
    DWMTWR_IMPLEMENTED_BY_SYSTEM (0x0001),
    DWMTWR_WINDOW_RELATIONSHIP   (0x0002),
    DWMTWR_WINDOW_STYLES         (0x0004),
    DWMTWR_WINDOW_REGION         (0x0008),
    DWMTWR_WINDOW_DWM_ATTRIBUTES (0x0010),
    DWMTWR_WINDOW_MARGINS        (0x0020),
    DWMTWR_TABBING_ENABLED       (0x0040),
    DWMTWR_USER_POLICY           (0x0080),
    DWMTWR_GROUP_POLICY          (0x0100),
    DWMTWR_APP_COMPAT            (0x0200),
}

enum class DWM_SOURCE_FRAME_SAMPLING (val nativeValue: Int) {
    DWM_SOURCE_FRAME_SAMPLING_POINT    (0),
    DWM_SOURCE_FRAME_SAMPLING_COVERAGE (1),
    DWM_SOURCE_FRAME_SAMPLING_LAST     (2),
}

enum class DWMTRANSITION_OWNEDWINDOW_TARGET (val nativeValue: Int) {
    DWMTRANSITION_OWNEDWINDOW_NULL       (-1),
    DWMTRANSITION_OWNEDWINDOW_REPOSITION (0),
}


// https://stackoverflow.com/questions/39261826/change-the-color-of-the-title-bar-caption-of-a-win32-application
// https://gist.github.com/sylveon/9c199bb6684fe7dffcba1e3d383fb609
//
enum class WINDOWCOMPOSITIONATTRIB (val nativeValue: Int) {
    WCA_UNDEFINED (0),
    WCA_NCRENDERING_ENABLED (1),
    WCA_NCRENDERING_POLICY (2),
    WCA_TRANSITIONS_FORCEDISABLED (3),
    WCA_ALLOW_NCPAINT (4),
    WCA_CAPTION_BUTTON_BOUNDS (5),
    WCA_NONCLIENT_RTL_LAYOUT (6),
    WCA_FORCE_ICONIC_REPRESENTATION (7),
    WCA_EXTENDED_FRAME_BOUNDS (8),
    WCA_HAS_ICONIC_BITMAP (9),
    WCA_THEME_ATTRIBUTES (10),
    // Causes Classic light appearance, like Windows 7
    WCA_NCRENDERING_EXILED (11),
    WCA_NCADORNMENTINFO (12),
    WCA_EXCLUDED_FROM_LIVEPREVIEW (13),
    WCA_VIDEO_OVERLAY_ACTIVE (14),
    WCA_FORCE_ACTIVEWINDOW_APPEARANCE (15),
    WCA_DISALLOW_PEEK (16),
    WCA_CLOAK (17),
    WCA_CLOAKED (18),
    WCA_ACCENT_POLICY (19), // 0x13
    WCA_FREEZE_REPRESENTATION (20),
    WCA_EVER_UNCLOAKED (21),
    WCA_VISUAL_OWNER (22),
    WCA_HOLOGRAPHIC (23),
    WCA_EXCLUDED_FROM_DDA (24),
    WCA_PASSIVEUPDATEMODE (25),
    WCA_USEDARKMODECOLORS (26),
    WCA_LAST (27),
}


/*
typedef struct _DWM_BLURBEHIND {
    DWORD dwFlags;
    WINBOOL fEnable;
    HRGN hRgnBlur;
    WINBOOL fTransitionOnMaximized;
} DWM_BLURBEHIND, *PDWM_BLURBEHIND;

typedef struct _DWM_THUMBNAIL_PROPERTIES {
    DWORD dwFlags;
    RECT rcDestination;
    RECT rcSource;
    BYTE opacity;
    WINBOOL fVisible;
    WINBOOL fSourceClientAreaOnly;
} DWM_THUMBNAIL_PROPERTIES, *PDWM_THUMBNAIL_PROPERTIES;

typedef struct _UNSIGNED_RATIO {
    UINT32 uiNumerator;
    UINT32 uiDenominator;
} UNSIGNED_RATIO;

typedef struct _DWM_TIMING_INFO {
    UINT32 cbSize;
    UNSIGNED_RATIO rateRefresh;
    QPC_TIME qpcRefreshPeriod;
    UNSIGNED_RATIO rateCompose;
    QPC_TIME qpcVBlank;
    DWM_FRAME_COUNT cRefresh;
    UINT cDXRefresh;
    QPC_TIME qpcCompose;
    DWM_FRAME_COUNT cFrame;
    UINT cDXPresent;
    DWM_FRAME_COUNT cRefreshFrame;
    DWM_FRAME_COUNT cFrameSubmitted;
    UINT cDXPresentSubmitted;
    DWM_FRAME_COUNT cFrameConfirmed;
    UINT cDXPresentConfirmed;
    DWM_FRAME_COUNT cRefreshConfirmed;
    UINT cDXRefreshConfirmed;
    DWM_FRAME_COUNT cFramesLate;
    UINT cFramesOutstanding;
    DWM_FRAME_COUNT cFrameDisplayed;
    QPC_TIME qpcFrameDisplayed;
    DWM_FRAME_COUNT cRefreshFrameDisplayed;
    DWM_FRAME_COUNT cFrameComplete;
    QPC_TIME qpcFrameComplete;
    DWM_FRAME_COUNT cFramePending;
    QPC_TIME qpcFramePending;
    DWM_FRAME_COUNT cFramesDisplayed;
    DWM_FRAME_COUNT cFramesComplete;
    DWM_FRAME_COUNT cFramesPending;
    DWM_FRAME_COUNT cFramesAvailable;
    DWM_FRAME_COUNT cFramesDropped;
    DWM_FRAME_COUNT cFramesMissed;
    DWM_FRAME_COUNT cRefreshNextDisplayed;
    DWM_FRAME_COUNT cRefreshNextPresented;
    DWM_FRAME_COUNT cRefreshesDisplayed;
    DWM_FRAME_COUNT cRefreshesPresented;
    DWM_FRAME_COUNT cRefreshStarted;
    ULONGLONG cPixelsReceived;
    ULONGLONG cPixelsDrawn;
    DWM_FRAME_COUNT cBuffersEmpty;
} DWM_TIMING_INFO;

typedef struct _DWM_PRESENT_PARAMETERS {
    UINT32 cbSize;
    WINBOOL fQueue;
    DWM_FRAME_COUNT cRefreshStart;
    UINT cBuffer;
    WINBOOL fUseSourceRate;
    UNSIGNED_RATIO rateSource;
    UINT cRefreshesPerFrame;
    DWM_SOURCE_FRAME_SAMPLING eSampling;
} DWM_PRESENT_PARAMETERS;

#ifndef _MIL_MATRIX3X2D_DEFINED
#define _MIL_MATRIX3X2D_DEFINED

typedef struct _MilMatrix3x2D {
    DOUBLE S_11;
    DOUBLE S_12;
    DOUBLE S_21;
    DOUBLE S_22;
    DOUBLE DX;
    DOUBLE DY;
} MilMatrix3x2D;
#endif

#ifndef MILCORE_MIL_MATRIX3X2D_COMPAT_TYPEDEF
#define MILCORE_MIL_MATRIX3X2D_COMPAT_TYPEDEF
typedef MilMatrix3x2D MIL_MATRIX3X2D;
#endif

#include <poppack.h>
*/

const val DWM_BB_ENABLE                : Int = 0x1
const val DWM_BB_BLURREGION            : Int = 0x2
const val DWM_BB_TRANSITIONONMAXIMIZED : Int = 0x4

const val DWM_CLOAKED_APP         : Int =  0x1
const val DWM_CLOAKED_SHELL       : Int =  0x2
const val DWM_CLOAKED_INHERITED   : Int =  0x4

const val DWM_TNP_RECTDESTINATION : Int = 0x1
const val DWM_TNP_RECTSOURCE      : Int = 0x2
const val DWM_TNP_OPACITY         : Int = 0x4
const val DWM_TNP_VISIBLE         : Int = 0x8
const val DWM_TNP_SOURCECLIENTAREAONLY : Int = 0x10

const val DWM_FRAME_DURATION_DEFAULT : Int =  -1

const val c_DwmMaxQueuedBuffers     : Int = 8
const val c_DwmMaxMonitors          : Int = 16
const val c_DwmMaxAdapters          : Int = 16

const val DWM_EC_DISABLECOMPOSITION : Int = 0
const val DWM_EC_ENABLECOMPOSITION  : Int = 1

//#if _WIN32_WINNT >= 0x0601
const val DWM_SIT_DISPLAYFRAME      : Int = 0x1
//#endif

// https://learn.microsoft.com/en-us/windows/win32/api/dwmapi/nf-dwmapi-dwmsetwindowattribute
//HRESULT WINAPI DwmSetWindowAttribute (HWND hwnd, DWORD dwAttribute, LPCVOID pvAttribute, DWORD cbAttribute);



//WINBOOL WINAPI DwmDefWindowProc (HWND hWnd, UINT msg, WPARAM wParam, LPARAM lParam, LRESULT *plResult);
//HRESULT WINAPI DwmEnableBlurBehindWindow (HWND hWnd, const DWM_BLURBEHIND *pBlurBehind);
//HRESULT WINAPI DwmEnableComposition (UINT uCompositionAction);
//HRESULT WINAPI DwmEnableMMCSS (WINBOOL fEnableMMCSS);
//HRESULT WINAPI DwmExtendFrameIntoClientArea (HWND hWnd, const MARGINS *pMarInset);
//HRESULT WINAPI DwmGetColorizationColor (DWORD *pcrColorization, WINBOOL *pfOpaqueBlend);
//HRESULT WINAPI DwmGetCompositionTimingInfo (HWND hwnd, DWM_TIMING_INFO *pTimingInfo);
//HRESULT WINAPI DwmGetWindowAttribute (HWND hwnd, DWORD dwAttribute, PVOID pvAttribute, DWORD cbAttribute);
//HRESULT WINAPI DwmIsCompositionEnabled (WINBOOL *pfEnabled);
//HRESULT WINAPI DwmModifyPreviousDxFrameDuration (HWND hwnd, INT cRefreshes, WINBOOL fRelative);
//HRESULT WINAPI DwmQueryThumbnailSourceSize (HTHUMBNAIL hThumbnail, PSIZE pSize);
//HRESULT WINAPI DwmRegisterThumbnail (HWND hwndDestination, HWND hwndSource, PHTHUMBNAIL phThumbnailId);
//HRESULT WINAPI DwmSetDxFrameDuration (HWND hwnd, INT cRefreshes);
//HRESULT WINAPI DwmSetPresentParameters (HWND hwnd, DWM_PRESENT_PARAMETERS *pPresentParams);
//HRESULT WINAPI DwmSetWindowAttribute (HWND hwnd, DWORD dwAttribute, LPCVOID pvAttribute, DWORD cbAttribute);
//HRESULT WINAPI DwmUnregisterThumbnail (HTHUMBNAIL hThumbnailId);
//HRESULT WINAPI DwmUpdateThumbnailProperties (HTHUMBNAIL hThumbnailId, const DWM_THUMBNAIL_PROPERTIES *ptnProperties);
//HRESULT WINAPI DwmAttachMilContent (HWND hwnd);
//HRESULT WINAPI DwmDetachMilContent (HWND hwnd);
//HRESULT WINAPI DwmFlush ();
//HRESULT WINAPI DwmGetGraphicsStreamTransformHint (UINT uIndex, MilMatrix3x2D *pTransform);
//HRESULT WINAPI DwmGetGraphicsStreamClient (UINT uIndex, UUID *pClientUuid);
//HRESULT WINAPI DwmGetTransportAttributes (WINBOOL *pfIsRemoting, WINBOOL *pfIsConnected, DWORD *pDwGeneration);
//HRESULT WINAPI DwmTransitionOwnedWindow (HWND hwnd, enum DWMTRANSITION_OWNEDWINDOW_TARGET target);
//#if _WIN32_WINNT >= 0x0601
//HRESULT WINAPI DwmSetIconicThumbnail (HWND hwnd, HBITMAP hbmp, DWORD dwSITFlags);
//HRESULT WINAPI DwmSetIconicLivePreviewBitmap (HWND hwnd, HBITMAP hbmp, POINT *pptClient, DWORD dwSITFlags);
//HRESULT WINAPI DwmInvalidateIconicBitmaps (HWND hwnd);
//#endif
//#if NTDDI_VERSION >= NTDDI_WIN8
//HRESULT WINAPI DwmRenderGesture (enum GESTURE_TYPE gt, UINT cContacts, const DWORD *pdwPointerID, const POINT *pPoints);
//HRESULT WINAPI DwmTetherContact (DWORD dwPointerID, WINBOOL fEnable, POINT ptTether);
//HRESULT WINAPI DwmShowContact (DWORD dwPointerID, enum DWM_SHOWCONTACT eShowContact);
//#endif
//#if NTDDI_VERSION >= NTDDI_WIN10_RS4
//HRESULT WINAPI DwmGetUnmetTabRequirements (HWND appWindow, enum DWM_TAB_WINDOW_REQUIREMENTS *value);
//#endif
