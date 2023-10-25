package com.mvv.gui.audio

import com.mvv.gui.util.addParam
import com.mvv.gui.util.commandLine
import com.mvv.gui.util.executeCommand
import com.mvv.gui.util.trimToNull
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.ExecuteException
import org.apache.commons.exec.PumpStreamHandler
import java.io.ByteArrayOutputStream
import java.util.regex.Pattern
import kotlin.text.Charsets.UTF_8


data class ESpeakVoice(
    val pty: Int, val language: String, val gender: Gender?,
    /** !! Name be not unique !! */
    val name: String,
    /** !! File seems to be unique !! */
    val file: String,
    val otherLanguages: String? = null,
    // -p <integer>  Pitch adjustment, 0 to 99, default is 50
    val pitch: Int? = null,
    //-s <integer>   Speed in words per minute, 80 to 450, default is 175
    val wordsPerMinute: Int? = null,
    //-a <integer>   Amplitude, 0 to 200, default is 100
    val amplitude: Int? = null,
    // -g <integer>  Word gap. Pause between words, units of 10mS at the default speed
    val wordGap: Int? = null,
    //-k <integer>   Indicate capital letters with: 1=sound, 2=the word "capitals",
    //               higher values indicate a pitch increase (try -k20).
    val indicateCapitalLetters: Int? = null,
    //-l <integer>   Line length. If not zero (which is the default), consider
    //               lines less than this length as end-of-clause
    val lineLength: Int? = null,
) {

    companion object {
        // Strings:
        //    "2   en-gb          M  english              en            (en-uk 2)(en 2)"
        //    " 2  en-gb          M  english              en            (en-uk 2)(en 2)"
        //    " 5  en             M  default              default"
        //    " 5  ru             M  russian              europe/ru"
        private val pattern = Pattern.compile("\\s*(\\d+)" +
                "[\\s\\t]+([a-zA-Z0-9\\-_]+)" +  // Ptty
                "[\\s\\t]+([FfMmNn-])" +           // Gender (female/male/unknown)
                "[\\s\\t]+([a-zA-Z0-9\\-_]+)" +  // Voice Name
                "[\\s\\t]+([a-zA-Z0-9\\-/_]+)" + // File
                "[\\s\\t]*(.*)"                  // Other Languages
                //".*"
            )

        operator fun invoke(string: String): ESpeakVoice {
            val matcher = pattern.matcher(string)
            val isMatched = matcher.matches()

            val items: List<String> = if (isMatched) (1..matcher.groupCount()).map { matcher.group(it) }
                                      else throw IllegalArgumentException("[$string] cannot be interpreted as eSpeak voice.")

            require(items.size >= 5) { "Not enough params in [$string] for eSpeak voice." }

            return ESpeakVoice(items[0].toInt(), items[1], items[2].toGender(), items[3], items[4], items.getOrNull(5).trimToNull())
        }
    }
}


private fun String.toGender(): Gender? = when (this.trim()) {
    "M", "m" -> Gender.Male
    "F", "f" -> Gender.Female
    "N", "n" -> Gender.Neutral
    "-" -> null
    else -> throw IllegalArgumentException("Cannot parse gender from [$this].")
}


internal val fixedMbrolaVoiceDefinitions: Map<String, ESpeakVoice> = listOf(
    ESpeakVoice(3, "en-uk", Gender.Male, "english-mb-en1", "mb/mb-en1", "(en 2)",
        /*pitch = 20,*/ wordsPerMinute = 140),
    // ??? mbrola-us1 should be woman ???
    ESpeakVoice(5, "en-us", Gender.Male, "us-mbrola-1", "mb/mb-us1", "(en 8)",
        /*pitch = 20,*/ wordsPerMinute = 130),
    ESpeakVoice(5, "en-us", Gender.Male, "us-mbrola-2", "mb/mb-us2", "(en 7)",
        /*pitch = 20,*/ wordsPerMinute = 130),
    ESpeakVoice(5, "en-us", Gender.Male, "us-mbrola-3", "mb/mb-us3", "(en 8)",
        /*pitch = 20,*/ wordsPerMinute = 130),

    // with German accent
    ESpeakVoice(9, "en", Gender.Male, "en-german", "mb/mb-de4-en",
        /*pitch = 20,*/ wordsPerMinute = 130),
    ESpeakVoice(9, "en", Gender.Male, "en-german-5", "mb/mb-de5-en",
        /*pitch = 20,*/ wordsPerMinute = 130),

    // with French accent
    ESpeakVoice(10, "en", Gender.Male, "en-french", "mb/mb-fr1-en",
        /*pitch = 20,*/ wordsPerMinute = 130),
    ESpeakVoice(10, "en", Gender.Female, "en-french", "mb/mb-fr4-en",
        /*pitch = 20,*/ wordsPerMinute = 130),

    // if you need you can properly adjust also the following ones
    // en-afrikaans    mb/mb-af1-en
    // en-french       mb/mb-fr1-en
    // en-french       mb/mb-fr4-en
    // en-hungarian    mb/mb-hu1-en
    // en-dutch        mb/mb-nl2-en
    // en-polish       mb/mb-pl1-en
    // en-swedish      mb/mb-sw1-en
    // en-swedish-f    mb/mb-sw2-en
).associateBy { it.file }


class ESpeakVoiceManager {
    private val predefinedVoicesString =
        // `espeak --voice=en`
        """
        Pty Language Age/Gender VoiceName          File          Other Languages

         2  en-gb          M  english              en            (en-uk 2)(en 2)
         3  en-uk          M  english-mb-en1       mb/mb-en1     (en 2)
         2  en-us          M  english-us           en-us         (en-r 5)(en 3)
         5  en-sc          M  en-scottish          other/en-sc   (en 4)
         5  en             M  default              default
         5  en-uk-north    M  english-north        other/en-n    (en-uk 3)(en 5)
         5  en-uk-rp       M  english_rp           other/en-rp   (en-uk 4)(en 5)
         5  en-us          M  us-mbrola-2          mb/mb-us2     (en 7)
         5  en-us          F  us-mbrola-1          mb/mb-us1     (en 8)
         5  en-us          M  us-mbrola-3          mb/mb-us3     (en 8)
         9  en             M  en-german            mb/mb-de4-en
         9  en             F  en-german-5          mb/mb-de5-en
         9  en             M  en-greek             mb/mb-gr2-en
         9  en             M  en-romanian          mb/mb-ro1-en
         5  en-uk-wmids    M  english_wmids        other/en-wm   (en-uk 9)(en 9)
        10  en             M  en-dutch             mb/mb-nl2-en
        10  en             M  en-french            mb/mb-fr1-en
        10  en             F  en-french            mb/mb-fr4-en
        10  en             F  en-hungarian         mb/mb-hu1-en
        10  en             F  en-swedish-f         mb/mb-sw2-en
         5  en-wi          M  en-westindies        other/en-wi   (en-uk 4)(en 10)
        11  en             M  en-afrikaans         mb/mb-af1-en
        11  en             F  en-polish            mb/mb-pl1-en
        11  en             M  en-swedish           mb/mb-sw1-en
        
        // espeak --voice=ru
         5  ru             M  russian              europe/ru
        """.trimIndent()

    val predefinedVoices: List<ESpeakVoice> get() = parseVoices(predefinedVoicesString)

    val availableVoices: List<ESpeakVoice> get() {
        val voicesString: String = executeAndReturnContent("espeak", "--voices")
        // strange but if we specify language espeak also shows mbrola voices, otherwise it does not.
        val enVoicesString: String = executeAndReturnContent("espeak", "--voices=en")
        return parseVoices(voicesString + "\n" + enVoicesString)
    }

    private fun parseVoices(voicesString: String) = voicesString
        .splitToSequence("\n")
        .distinct()
        .filter { it.isNotBlank() }
        .filterNot { it.startsWith("//") }
        .filterNot { it.startsWith("Pty") } // real header
        .map { ESpeakVoice(it) }
        .map { fixedMbrolaVoiceDefinitions.getOrDefault(it.file, it) }
        .sortedBy { it.name }
        .toList()
}


private fun executeAndReturnContent(vararg args: String): String {
    val executor = DefaultExecutor()

    val contentStream = ByteArrayOutputStream()
    executor.streamHandler = PumpStreamHandler(contentStream, System.err)

    val res = executor.execute(commandLine(*args))
    if (res != 0) throw ExecuteException("[espeak --voices] failed with code $res.", res)

    return contentStream.toString(UTF_8)
}


class ESpeakSpeechSynthesizer(private val voice: ESpeakVoice) : SpeechSynthesizer {

    // eSpeak voice file is unique and name is not unique and in some cases is too general (for example, "en-french mb/mb-fr4-en")
    override val shortDescription: String = "eSpeak ${voice.file.removePrefix("mb/")}"

    override fun speak(text: String) {
        val args = mutableListOf("espeak")
            .addParam("-v", voice.file) // name is not unique ?!
            .addParam("-p", voice.pitch)
            .addParam("-s", voice.wordsPerMinute)
            .addParam("-a", voice.amplitude)
            .addParam("-g", voice.wordGap)
            .addParam("-k", voice.indicateCapitalLetters)
            .addParam("-l", voice.lineLength)
            .addParam(text)

        executeCommand(args)
    }
}

/*
Mbrola en voices:

 9  en             M  en-german            mb/mb-de4-en
 9  en             F  en-german-5          mb/mb-de5-en
 9  en             M  en-greek             mb/mb-gr2-en
 9  en             M  en-romanian          mb/mb-ro1-en
10  en             M  en-dutch             mb/mb-nl2-en
10  en             M  en-french            mb/mb-fr1-en
10  en             F  en-french            mb/mb-fr4-en
10  en             F  en-hungarian         mb/mb-hu1-en
10  en             F  en-swedish-f         mb/mb-sw2-en
11  en             M  en-afrikaans         mb/mb-af1-en
11  en             F  en-polish            mb/mb-pl1-en
11  en             M  en-swedish           mb/mb-sw1-en
*/
