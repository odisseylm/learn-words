package com.mvv.gnome.shell.keyboard

import com.mvv.gui.test.useAssertJSoftAssertions
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Disabled


class ShyriiwookInputSourceExtensionTest {

    @Test
    @Disabled("For manual testing/debugging.")
    fun `get current InputSource by Shyriiwook gnome extension`() { useAssertJSoftAssertions {
        assertThat(getCurrentInputSourceByShyriiwookExt())
            .isNotNull()
            .isNotBlank
            .hasSizeBetween(2, 5)
    } }

    @Test
    @Disabled("For manual testing/debugging.")
    fun `is Shyriiwook gnome extension available`() { useAssertJSoftAssertions {
        assertThat(isShyriiwookExtensionAvailableImpl()).isTrue
    } }

    @Test
    fun parseCurrentInputSourceByShyriiwookExt() { useAssertJSoftAssertions {

        val output = """
            wook --only-properties
            node /me/madhead/Shyriiwook {
              interface me.madhead.Shyriiwook {
                properties:
                  readonly as availableLayouts = ['us', 'gb', 'au', 'ru', 'ge+ru', 'ua'];
                  readonly s currentLayout = 'us';
              };
            };
            """

        assertThat(parseCurrentInputSourceByShyriiwookExt(output))
            .isEqualTo("us")
    } }
}
