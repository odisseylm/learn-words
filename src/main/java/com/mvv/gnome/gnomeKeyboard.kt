package com.mvv.gnome

import com.mvv.gnome.shell.keyboard.isShellEvalAvailable
import com.mvv.gnome.shell.keyboard.isShyriiwookExtensionAvailable
import com.mvv.gnome.shell.keyboard.selectInputSource as selectGnomeInputSourceByShellEval
import com.mvv.gnome.shell.keyboard.selectInputSourceByShyriiwookExtension
import java.util.*



fun selectKeyboardLayout(locale: Locale) {
    when {
        // Custom Gnome extension
        isShyriiwookExtensionAvailable -> selectInputSourceByShyriiwookExtension(locale)

        // ShellEval is removed/disabled since Ubuntu 22.0 due to security reasons.
        //
        isShellEvalAvailable -> selectGnomeInputSourceByShellEval(locale)

        // Using THIS gSettings approach is bad idea,
        // because it requires full gnome session update
        //
        // else -> selectGSettingsInputSource(locale)
    }
}
