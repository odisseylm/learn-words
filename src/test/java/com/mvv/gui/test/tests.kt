package com.mvv.gui.test

import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail


fun SoftAssertions.runTests(block: SoftAssertions.() -> Unit) {
    block()
    this.assertAll()
}

fun useAssertJSoftAssertions(block: SoftAssertions.() -> Unit) {
    val a = SoftAssertions()
    a.block()
    a.assertAll()
}


class TestUtilsTest {

    @Test
    @DisplayName("runTests - makeSure assertAll is called")
    fun test_runTests() {

        try {
            SoftAssertions().runTests {
                assertThat(1).isEqualTo(2)
            }

            fail("SoftAssertions.assertAll() is not called.")
        }
        catch (ex: AssertionError) {
            assertTrue(ex.message?.contains("Multiple Failures (1 failure)") ?: false)
            assertTrue(ex.message?.contains("expected: 2\n but was: 1") ?: false)
        }
    }

    @Test
    @DisplayName("useAssertJSoftAssertions - makeSure assertAll is called")
    fun test_useAssertJSoftAssertions() {

        try {
            useAssertJSoftAssertions {
                assertThat(1).isEqualTo(2)
            }

            fail("SoftAssertions.assertAll() is not called.")
        }
        catch (ex: AssertionError) {
            assertTrue(ex.message?.contains("Multiple Failures (1 failure)") ?: false)
            assertTrue(ex.message?.contains("expected: 2\n but was: 1") ?: false)
        }
    }

}
