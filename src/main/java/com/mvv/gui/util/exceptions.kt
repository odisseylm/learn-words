package com.mvv.gui.util


class ActionCanceledException(val action: String, message: String, cause: Throwable? = null) : RuntimeException(message) {
    init { cause?.let { initCause(cause) } }
}
