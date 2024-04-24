package com.mvv.gnome.gsettings

import com.mvv.gui.test.useAssertJSoftAssertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.*


class GSettingsInputSourceKeysTest {

    @Test
    fun parsePair() { useAssertJSoftAssertions {
        assertThat(parsePair("('a','b')")).isEqualTo(Pair("'a'", "'b'"))
        assertThat(parsePair("'a','b')")).isEqualTo(Pair("'a'", "'b'"))
        assertThat(parsePair("('a','b'")).isEqualTo(Pair("'a'", "'b'"))
        assertThat(parsePair("'a','b'")).isEqualTo(Pair("'a'", "'b'"))
    } }

    @Test
    fun splitByComma() { useAssertJSoftAssertions {
        assertThat("a,b".splitByComma()).containsExactly("a", "b")
        assertThat("a, b".splitByComma()).containsExactly("a", "b")
        assertThat(" a , b ".splitByComma()).containsExactly("a", "b")

        assertThat(",a,b,,c,".splitByComma()).containsExactly("", "a", "b", "", "c", "")
        assertThat(" , a , b , , c , ".splitByComma()).containsExactly("", "a", "b", "", "c", "")
        assertThat("\t,\ta\t,\tb\t,\t,\tc\t,\t".splitByComma()).containsExactly("", "a", "b", "", "c", "")
    } }

    @Test
    fun `splitByComma with quotes`() { useAssertJSoftAssertions {
        assertThat("'a','b'".splitByComma()).containsExactly("'a'", "'b'")
        assertThat("'a', 'b'".splitByComma()).containsExactly("'a'", "'b'")
        assertThat(" 'a' , 'b' ".splitByComma()).containsExactly("'a'", "'b'")

        assertThat("'','a',b,'',c,".splitByComma()).containsExactly("''", "'a'", "b", "''", "c", "")
        assertThat(" , a , 'b' , '' , 'c' , ''".splitByComma()).containsExactly("", "a", "'b'", "''", "'c'", "''")
        assertThat("\t,\ta\t,\tb\t,\t,\t'c'\t,''\t".splitByComma()).containsExactly("", "a", "b", "", "'c'", "''")
    } }

    @Test
    fun `splitByComma with comma inside quotes`() { useAssertJSoftAssertions {
        assertThat(" 'a' , 'b,c' , d ".splitByComma()).containsExactly("'a'", "'b,c'", "d")
    } }

    @Test
    fun `splitByComma multi-level `() { useAssertJSoftAssertions {
        assertThat(" ((a, ' b(( ', c)) , ( 'p' , 'r') ".splitByComma()).containsExactly("((a, ' b(( ', c))", "( 'p' , 'r')")
    } }

    @Test
    fun parseInputSourceKeys() { useAssertJSoftAssertions {
        assertThat("[('xkb', 'us'), ('xkb', 'ru'), ('xkb', 'ua')]".parseInputSourceKeys())
            .containsExactly(InputSourceKey("xkb", "us"), InputSourceKey("xkb", "ru"), InputSourceKey("xkb", "ua"))

        assertThat("[(xkb, us) , (xkb, 'ru') , ('xkb', ua)]".parseInputSourceKeys())
            .containsExactly(InputSourceKey("xkb", "us"), InputSourceKey("xkb", "ru"), InputSourceKey("xkb", "ua"))
    } }


    @Test
    @Disabled("For manual testing/debugging")
    fun findInputSourceKey() { useAssertJSoftAssertions {
        // [('xkb', 'us'), ('xkb', 'gb'), ('xkb', 'au'), ('xkb', 'ru'), ('xkb', 'ge+ru'), ('xkb', 'ua')]

        // Locale.getAvailableLocales().forEach { println(it) }

        val inputSourceKeys = listOf(
            InputSourceKey("us"), InputSourceKey("gb"), InputSourceKey("au"),
            InputSourceKey("ru"), InputSourceKey("ge+ru"), InputSourceKey("ua"),
        )

        assertThat(inputSourceKeys.findInputSourceKey(Locale.ENGLISH))
            .isEqualTo(InputSourceKey("us"))

        assertThat(inputSourceKeys.findInputSourceKey(Locale("en", "AU")))
            .isEqualTo(InputSourceKey("au"))

        assertThat(inputSourceKeys.findInputSourceKey(Locale("en", "US")))
            .isEqualTo(InputSourceKey("us"))

        assertThat(inputSourceKeys.findInputSourceKey(Locale("en", "GB")))
            .isEqualTo(InputSourceKey("gb"))

        assertThat(inputSourceKeys.findInputSourceKey(Locale("ru")))
            .isEqualTo(InputSourceKey("ru"))

        assertThat(inputSourceKeys.findInputSourceKey(Locale("ru", "GE")))
            .isEqualTo(InputSourceKey("ge+ru"))

        assertThat(listOf(InputSourceKey("ge+ru")).findInputSourceKey(Locale("ru")))
            .isEqualTo(InputSourceKey("ge+ru"))

        assertThat(listOf(InputSourceKey("au")).findInputSourceKey(Locale.ENGLISH))
            .isEqualTo(InputSourceKey("au"))

        assertThat(listOf(InputSourceKey("au")).findInputSourceKey(Locale("en", "US")))
            .isEqualTo(InputSourceKey("au"))

        assertThat(listOf(InputSourceKey("us")).findInputSourceKey(Locale("en", "AU")))
            .isEqualTo(InputSourceKey("us"))

        assertThat(listOf(InputSourceKey("gb")).findInputSourceKey(Locale("en", "AU")))
            .isEqualTo(InputSourceKey("gb"))
    } }

    @Test
    @Disabled("For manual testing/debugging")
    fun `load all xkb InputSources`() { useAssertJSoftAssertions {
        val all = loadAllXkbInputSources()
        assertThat(all).hasSizeGreaterThan(10)
    } }
}
