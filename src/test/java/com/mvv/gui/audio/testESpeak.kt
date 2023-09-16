package com.mvv.gui.audio

import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor


/*
-v <voice name>
-p <integer>  Pitch adjustment, 0 to 99, default is 50
-a <integer>  Amplitude, 0 to 200, default is 100


voices:
 5  en             M  default              default
 2  en-gb          M  english              en            (en-uk 2)(en 2)
 5  en-sc          M  en-scottish          other/en-sc   (en 4)
 5  en-uk-north    M  english-north        other/en-n    (en-uk 3)(en 5)
 5  en-uk-rp       M  english_rp           other/en-rp   (en-uk 4)(en 5)
 5  en-uk-wmids    M  english_wmids        other/en-wm   (en-uk 9)(en 9)
 2  en-us          M  english-us           en-us         (en-r 5)(en 3)
 5  en-wi          M  en-westindies        other/en-wi   (en-uk 4)(en 10)
 5  ru             M  russian              europe/ru
*/


fun main() {
    /*
    -a <integer>         Amplitude, 0 to 200, default is 100
    -p <integer>         Pitch adjustment, 0 to 99, default is 50
    -s <integer>         Speed in words per minute, 80 to 450, default is 175
    -v <voice name>      Use voice file of this name from espeak-data/voices
    -w <wave file name>  Write speech to this WAV file, rather than speaking it directly
    */

    //execute("espeak", "apple")
    //execute("espeak", "door")

    /*
    // Not bad for separate words (but for long sentences so-so-so)
    testVoice("mb-en1")
    testVoice("mb-us1")
    testVoice("mb-us2")
    testVoice("mb-us3")

    Thread.sleep(5000)
    testVoice("mb-en1")
    testVoice("default")
    testVoice("english")
    testVoice("en-scottish")
    testVoice("english-north")
    testVoice("english_rp")
    testVoice("english_wmids")
    testVoice("english-us")
    testVoice("en-westindies")
    */

    //testVoice("mb/mb-en1")
    //testVoice("mb-en1")
    //testVoice("english-mb-en1")

    testVoice("en-german-5")

    // russian voice is bad
    //testVoice("russian", "привет")
}


@Suppress("SameParameterValue")
private fun testVoice(voice: String) {
    execute("espeak", "-v", voice, "door")
    execute("espeak", "-v", voice, "apple")
    execute("espeak", "-v", voice, "boredom")
    execute("espeak", "-v", voice, "bore")
    execute("espeak", "-v", voice, "boring")
    execute("espeak", "-v", voice, "exit")
    Thread.sleep(1000)
}


private fun execute(vararg args: String) {
    DefaultExecutor()
        .execute(
            CommandLine(args.first())
                .also { cl -> args.asSequence().drop(1).forEach { cl.addArguments(it) } }
    )
}

/*
/usr/lib/x86_64-linux-gnu/espeak-data
/usr/lib/x86_64-linux-gnu/espeak-ng-data

/usr/lib/x86_64-linux-gnu/espeak-ng-data/voices/mb/mb-en1
/usr/lib/x86_64-linux-gnu/espeak-ng-data/voices/mb/mb-us1
/usr/lib/x86_64-linux-gnu/espeak-ng-data/voices/mb/mb-us2
/usr/lib/x86_64-linux-gnu/espeak-ng-data/voices/mb/mb-us3
*/



/*
espeak --help

eSpeak text-to-speech: 1.48.03  04.Mar.14  Data at: /usr/lib/x86_64-linux-gnu/espeak-data

espeak [options] ["<words>"]

-f <text file>   Text file to speak
--stdin    Read text input from stdin instead of a file

If neither -f nor --stdin, then <words> are spoken, or if none then text
is spoken from stdin, each line separately.

-a <integer>
	   Amplitude, 0 to 200, default is 100
-g <integer>
	   Word gap. Pause between words, units of 10mS at the default speed
-k <integer>
	   Indicate capital letters with: 1=sound, 2=the word "capitals",
	   higher values indicate a pitch increase (try -k20).
-l <integer>
	   Line length. If not zero (which is the default), consider
	   lines less than this length as end-of-clause
-p <integer>
	   Pitch adjustment, 0 to 99, default is 50
-s <integer>
	   Speed in words per minute, 80 to 450, default is 175
-v <voice name>
	   Use voice file of this name from espeak-data/voices
-w <wave file name>
	   Write speech to this WAV file, rather than speaking it directly
-b	   Input text encoding, 1=UTF8, 2=8 bit, 4=16 bit
-m	   Interpret SSML markup, and ignore other < > tags
-q	   Quiet, don't produce any speech (may be useful with -x)
-x	   Write phoneme mnemonics to stdout
-X	   Write phonemes mnemonics and translation trace to stdout
-z	   No final sentence pause at the end of the text
--compile=<voice name>
	   Compile pronunciation rules and dictionary from the current
	   directory. <voice name> specifies the language
--ipa      Write phonemes to stdout using International Phonetic Alphabet
	         --ipa=1 Use ties, --ipa=2 Use ZWJ, --ipa=3 Separate with _
--path="<path>"
	   Specifies the directory containing the espeak-data directory
--pho      Write mbrola phoneme data (.pho) to stdout or to the file in --phonout
--phonout="<filename>"
	   Write phoneme output from -x -X --ipa and --pho to this file
--punct="<characters>"
	   Speak the names of punctuation characters during speaking.  If
	   =<characters> is omitted, all punctuation is spoken.
--split="<minutes>"
	   Starts a new WAV file every <minutes>.  Used with -w
--stdout   Write speech output to stdout
--version  Shows version number and date, and location of espeak-data
--voices=<language>
	   List the available voices for the specified language.
	   If <language> is omitted, then list all voices.
*/


/*
espeak --voice=en
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


espeak --voice=ru
Pty Language Age/Gender VoiceName          File          Other Languages
 5  ru             M  russian              europe/ru

*/
