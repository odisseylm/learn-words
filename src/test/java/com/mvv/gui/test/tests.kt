package com.mvv.gui.test

import org.assertj.core.api.SoftAssertions


fun SoftAssertions.runTests(block: SoftAssertions.() -> Unit) {
    block()
    this.assertAll()
}

fun useAssertJSoftAssertions(block: SoftAssertions.() -> Unit) {
    val a = SoftAssertions()
    a.block()
    a.assertAll()
}
