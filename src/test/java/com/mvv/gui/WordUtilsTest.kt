package com.mvv.gui

import com.mvv.gui.dictionary.containsRussianChars
import com.mvv.gui.dictionary.normalizeTranscription
import com.mvv.gui.words.translationCount
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Condition
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


class WordUtilsTest {

    @Test
    @DisplayName("translationCount")
    fun test_translationCount() {

        val assertions = SoftAssertions()

        assertions.assertThat("".translationCount).isEqualTo(0)
        assertions.assertThat(",".translationCount).isEqualTo(0)
        assertions.assertThat(";".translationCount).isEqualTo(0)
        assertions.assertThat(",;".translationCount).isEqualTo(0)
        assertions.assertThat(";,".translationCount).isEqualTo(0)

        assertions.assertThat("word".translationCount).isEqualTo(1)
        assertions.assertThat("word;,".translationCount).isEqualTo(1)
        assertions.assertThat(";,word".translationCount).isEqualTo(1)
        assertions.assertThat(";,word,;".translationCount).isEqualTo(1)

        assertions.assertThat(";,word,; apple".translationCount).isEqualTo(2)
        assertions.assertThat(";,word,; apple,;".translationCount).isEqualTo(2)

        assertions.assertAll()
    }

    @Test
    fun aa() {
        //Assertions.assertThat("")
        val s1 = "[ədˈvaıs]"
        val s2 = "[ədˈvaɪs]"
        assertEquals(normalizeTranscription(s1), normalizeTranscription(s2))

        //val arr1 = s1.codePoints().toArray()
        //val arr2 = s2.codePoints().toArray()
        //assertArrayEquals(arr1, arr2)

        //assertEquals(s1, s2)
        //assertNotEquals("[ədˈvaıs]", "[ədˈvaɪs]")
    }


    @Test
    fun testNormalizedTranscriptions() {

        testNormalizedTranscriptionsAreEqual("[ədˈvaıs][ədˈvaɪs]")
        testNormalizedTranscriptionsAreEqual("[ˈɑːftə][ˈa:ftə]")
        testNormalizedTranscriptionsAreEqual("[ˈɔːltə][ˈɔ:ltə]")
        testNormalizedTranscriptionsAreEqual("[ɔːlˈðəu][ɔ:lˈðəu]")
        testNormalizedTranscriptionsAreEqual("[ˈɔːlwəz][ˈɔ:lwəz]")
        //testNormalizedTranscriptionsAreEqual("[ˈenıwʌn][ˈenıwʌn]")
        testNormalizedTranscriptionsAreEqual("[ɑːːnt][a:nt]")
        testNormalizedTranscriptionsAreEqual("[tʃɑːns][ʧa:ns]")
        testNormalizedTranscriptionsAreEqual("[tʃeındʒ][ʧeınʤ]")
        testNormalizedTranscriptionsAreEqual("[tʃiːp][ʧi:p]")
        testNormalizedTranscriptionsAreEqual("[kliːn][kli:n]")
        testNormalizedTranscriptionsAreEqual("[ˈkliːnıŋ][ˈkli:nıŋ]")
        testNormalizedTranscriptionsAreEqual("[kəˈmiːdjən][kəˈmi:djən]")
        testNormalizedTranscriptionsAreEqual("[kɔːs][kɔ:s]")
        testNormalizedTranscriptionsAreEqual("[ˈdɔːtə][ˈdɔ:tə]")
        testNormalizedTranscriptionsAreEqual("[ˈdaınəusɔː][ˈdaınəusɔ:]")
        testNormalizedTranscriptionsAreEqual("[ınˈgeıdʒmənt][ınˈgeıʤmənt]")
        testNormalizedTranscriptionsAreEqual("[ınˈdʒɔıəbl][ınˈʤɔıəbl]")
        //testNormalizedTranscriptionsAreEqual("[ˈepısəud][ˈepısəud]")
        testNormalizedTranscriptionsAreEqual("[fiːl][fi:l]")
        testNormalizedTranscriptionsAreEqual("[fuːd][fu:d]")
        //testNormalizedTranscriptionsAreEqual("[fəˈget][fəˈget]")
        testNormalizedTranscriptionsAreEqual("[gəːl][gə:l]")
        //testNormalizedTranscriptionsAreEqual("[gaı][gaı]")
        testNormalizedTranscriptionsAreEqual("[hɑːt][ha:t]")
        testNormalizedTranscriptionsAreEqual("[hiːt][hi:t]")
        //testNormalizedTranscriptionsAreEqual("[haı][haı]")
        testNormalizedTranscriptionsAreEqual("[ˈhʌnımuːn][ˈhʌnımu:n]")
        testNormalizedTranscriptionsAreEqual("[həːt][hə:t]")
        testNormalizedTranscriptionsAreEqual("[ıˈmædʒınərı][ıˈmæʤınərı]")
        testNormalizedTranscriptionsAreEqual("[ˈkıtʃın][ˈkıʧın]")
        testNormalizedTranscriptionsAreEqual("[liːst][li:st]")
        //testNormalizedTranscriptionsAreEqual("[ˈlısn][ˈlısn]")
        //testNormalizedTranscriptionsAreEqual("[ˈmæstədɔn][ˈmæstədɔn]")
        testNormalizedTranscriptionsAreEqual("[miːt][mi:t]")
        testNormalizedTranscriptionsAreEqual("[ˈnɔːsjə][ˈnɔ:sjə]")
        testNormalizedTranscriptionsAreEqual("[ˈpiːpl][ˈpi:pl]")
        testNormalizedTranscriptionsAreEqual("[prıˈfəːd][prıˈfə:d]")
        testNormalizedTranscriptionsAreEqual("[rıˈtəːn][rıˈtə:n]")
        testNormalizedTranscriptionsAreEqual("[ˈsiːıŋ][ˈsi:ıŋ]")
        testNormalizedTranscriptionsAreEqual("[ʃuːt][ʃu:t]")
        testNormalizedTranscriptionsAreEqual("[ˈtʌtʃıŋ][ˈtʌʧıŋ]")
        testNormalizedTranscriptionsAreEqual("[wəːs][wə:s]")
        //testNormalizedTranscriptionsAreEqual("[rəut][rəut]")
        testNormalizedTranscriptionsAreEqual("[njuːz][nju:z]")
        testNormalizedTranscriptionsAreEqual("[pɑːt][pa:t]")
        testNormalizedTranscriptionsAreEqual("[ˈpiːpl][ˈpi:pl]")
        testNormalizedTranscriptionsAreEqual("[ˈfiːbı][ˈfi:bı]")
        testNormalizedTranscriptionsAreEqual("[pliːz][pli:z]")
        testNormalizedTranscriptionsAreEqual("[prıˈfəːd][prıˈfə:d]")
        testNormalizedTranscriptionsAreEqual("[ˈriːsntlı][ˈri:sntlı]")
        testNormalizedTranscriptionsAreEqual("[ˈrestərɔːŋ][ˈrestərɔ:ŋ]")
        testNormalizedTranscriptionsAreEqual("[rıˈtəːn][rıˈtə:n]")
        testNormalizedTranscriptionsAreEqual("[ˈsiːıŋ][ˈsi:ıŋ]")
        testNormalizedTranscriptionsAreEqual("[siːm][si:m]")
        testNormalizedTranscriptionsAreEqual("[ʃuːt][ʃu:t]")
        testNormalizedTranscriptionsAreEqual("[spiːk][spi:k]")
        testNormalizedTranscriptionsAreEqual("[tʌtʃ][tʌʧ]")
        testNormalizedTranscriptionsAreEqual("[ˈtʌtʃıŋ][ˈtʌʧıŋ]")
        testNormalizedTranscriptionsAreEqual("[wıtʃ][wıʧ]")
        testNormalizedTranscriptionsAreEqual("[huː][hu:]")
        testNormalizedTranscriptionsAreEqual("[wəːks][wə:ks]")
        testNormalizedTranscriptionsAreEqual("[wəːs][wə:s]")
        testNormalizedTranscriptionsAreEqual("[jɔːˈself][jɔ:ˈself]")
        testNormalizedTranscriptionsAreEqual("[ıˌljuːmıˈneıʃən][ıˌlju:mıˈneıʃən]")
    }

    @Test
    @DisplayName("containsRussianChars")
    fun test_containsRussianChars() {

        val containsRussianCharsCondition = Condition<String>({ it.containsRussianChars() }, "containsRussianChars")

        assertThat("dsds").isNot(containsRussianCharsCondition)
        assertThat("dsds".containsRussianChars()).isFalse()

        assertThat("ds Чебуран ds").`is`(containsRussianCharsCondition)
        assertThat("ds Чебуран ds".containsRussianChars()).isTrue()

        assertThat("Чебуран").`is`(containsRussianCharsCondition)
        assertThat("Чебуран".containsRussianChars()).isTrue()
    }
}


private fun testNormalizedTranscriptionsAreEqual(transcriptions: String) {
    val splitPos = transcriptions.indexOf("][")
    val tr1 = transcriptions.substring(0, splitPos + 1)
    val tr2 = transcriptions.substring(splitPos + 1)

    assertThat(tr1).isNotEqualTo(tr2)

    val ntr1 = normalizeTranscription(tr1)
    val ntr2 = normalizeTranscription(tr2)


    val arr1 = ntr1.codePoints().toArray()
    val arr2 = ntr2.codePoints().toArray()

    assertThat(ntr1).hasSameSizeAs(ntr2)
    //assertThat(ntr1).hasSize(ntr2.length)

    //for (i=0 ;i<ntr1.length; i++) {
    for (i in ntr1.indices) {
        val ch1 = ntr1[i]
        val ch2 = ntr2[i]

        val cp1 = ch1.code
        val cp2 = ch2.code

        if ((ch1 != ch2) || (cp1 != cp2)) {
            Assertions.fail<String>("$ntr1 differs from $ntr2 in position $i ($ch1 != $ch2 / $cp1 != $cp2).")
        }
    }

    assertArrayEquals(arr1, arr2)
    assertThat(normalizeTranscription(tr1)).isEqualTo(normalizeTranscription(tr2))
}