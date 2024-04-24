package com.mvv.gui.javafx

import com.mvv.gnome.shell.keyboard.isShellEvalAvailable
import com.mvv.gnome.shell.keyboard.isShyriiwookExtensionAvailable
import com.mvv.gnome.shell.keyboard.selectInputSourceByShyriiwookExtension
import com.mvv.gui.util.measureTime
import com.mvv.gnome.shell.keyboard.selectInputSource as selectGnomeInputSourceByShellEval
import org.apache.commons.lang3.SystemUtils.IS_OS_LINUX
import java.util.Locale


private val log = mu.KotlinLogging.logger {}


// See
//  https://askubuntu.com/questions/1039950/ubuntu-18-04-how-to-change-keyboard-layout-from-a-script
//  https://unix.stackexchange.com/questions/316998/how-to-change-keyboard-layout-in-gnome-3-from-command-line
//  https://discourse.gnome.org/t/how-to-set-gnome-keyboard-layout-programatically/9459

//  !!! Dangerous! Kills icon of language in system bar !!!
//  !!! gsettings set org.gnome.desktop.input-sources sources "[('xkb', 'ru')]"

// Other useful command:
//   `gsettings get org.gnome.desktop.input-sources sources`
//
// Output:
//   [('xkb', 'us'), ('xkb', 'ua'), ('xkb', 'ru')]
//
// If works properly, "org.gnome.desktop.input-sources.mru-sources" (most-recently-used sources)
// looks like what you want, and can be monitored by (like listening event)
//  `gsettings monitor org.gnome.desktop.input-sources mru-sources`


fun selectKeyboardLayout(locale: Locale) { measureTime("select keyboard for $locale", log) {
    if (IS_OS_LINUX)
        when {
            // Custom Gnome extension
            isShyriiwookExtensionAvailable -> selectInputSourceByShyriiwookExtension(locale)

            // ShellEval is removed since Ubuntu 22.0 due to security reasons.
            //
            isShellEvalAvailable           -> selectGnomeInputSourceByShellEval(locale)

            // Using THIS gSettings approach is bad idea,
            // because it requires full gnome session update
            //
            // else                        -> selectGSettingsInputSource(locale)
        }

        //selectGnomeInputSourceByShellEval(locale)
        //selectGSettingsInputSource(locale)
        //selectInputSourceByShyriiwookExtension(locale)
} }
