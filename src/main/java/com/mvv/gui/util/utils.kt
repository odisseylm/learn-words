package com.mvv.gui.util


fun Boolean.doIfTrue(action: ()->Unit) {
    if (this) action()
}
