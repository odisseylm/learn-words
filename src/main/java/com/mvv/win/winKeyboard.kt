package com.mvv.win


// https://learn.microsoft.com/en-us/windows/win32/inputdev/keyboard-input
// https://learn.microsoft.com/en-us/windows/win32/intl/language-names
// https://learn.microsoft.com/en-us/windows/win32/intl/locale-names
// https://learn.microsoft.com/en-us/windows/win32/intl/locales-and-languages
// https://learn.microsoft.com/en-us/windows/win32/intl/language-identifiers
// https://learn.microsoft.com/en-us/windows/win32/intl/language-identifier-constants-and-strings
//
// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-activatekeyboardlayout
// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-getkeyboardlayout
// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-getkeyboardlayoutnamew
// https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-getkeyboardlayoutlist
//
//
// https://sheroz.com/pages/blog/programmatically-switching-keyboard-layouts.html
// https://stackoverflow.com/questions/9685560/how-can-change-language-keyboard-layout-in-windows-c

// The next command changes the system keyboard layout to English:
// PostMessage(GetForegroundWindow(), WM_INPUTLANGCHANGEREQUEST, 1, 0x04090409);
//
// to Russian:
// PostMessage(GetForegroundWindow(), WM_INPUTLANGCHANGEREQUEST, 1, 0x04190419);

