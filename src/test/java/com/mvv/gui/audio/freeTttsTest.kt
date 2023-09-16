@file:Suppress("unused")

package com.mvv.gui.audio

import com.sun.speech.freetts.*
import com.sun.speech.freetts.Gender
import com.sun.speech.freetts.en.us.CMULexicon
import com.sun.speech.freetts.jsapi.FreeTTSEngineCentral
import com.sun.speech.freetts.jsapi.FreeTTSVoice
import de.dfki.lt.freetts.en.us.MbrolaVoice
import de.dfki.lt.freetts.en.us.MbrolaVoiceValidator
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import javax.speech.Central
import javax.speech.EngineCreate
import javax.speech.synthesis.Synthesizer
import javax.speech.synthesis.SynthesizerModeDesc


// See also com.sun.speech.freetts.FreeTTS
// It also allows to dump audio file.
//

private val log = mu.KotlinLogging.logger {}


fun main() {
    //listMbrolaVoices()
    //test0()
    //test1()
    //dumpToFile_usingFreeTTSClass()
    test444()
}


private fun test1() {

    // System.setProperty("com.sun.speech.freetts.voice.defaultAudioPlayer", "com.sun.speech.freetts.audio.SingleFileAudioPlayer");
    System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory")
    Central.registerEngineCentral("com.sun.speech.freetts.jsapi.FreeTTSEngineCentral")

    val modeDesc = SynthesizerModeDesc(Locale.US) // SynthesizerModeDesc(null, "general", Locale.US, null, null)
    val synthesizer = Central.createSynthesizer(modeDesc)

    synthesizer.allocate()
    synthesizer.resume()

    synthesizer.speakPlainText("Welcome John!", null)

    synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY)

    val desc = synthesizer.engineModeDesc as SynthesizerModeDesc
    for (voice in desc.voices) {

        log.info { "voice: $voice" }

        if (voice is FreeTTSVoice) {
            voice.voice.waveDumpFile = "temp/freeTts.wav"
            voice.voice.audioPlayer = com.sun.speech.freetts.audio.SingleFileAudioPlayer()
        }

        synthesizer.synthesizerProperties.voice = voice

        synthesizer.speakPlainText("Hello world!", null)
    }

    synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY)

    synthesizer.deallocate()
}


@Suppress("unused")
private fun test0() {

    val synthesizer = createSynthesizer()

    synthesizer.allocate()
    synthesizer.resume()

    synthesizer.speakPlainText("Welcome John!", null)

    synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY)

    synthesizer.deallocate()
}


@Suppress("unused")
private fun test444() {

    // voices: [mbrola_us1, mbrola_us2, mbrola_us3]

    System.setProperty("mbrola.base", "/usr/share/mbrola")

    System.setProperty("freetts.voices", MbrolaVoiceDirectory22::class.java.name)
    val textaudio = VoiceManager.getInstance().getVoice("mbrola_us1")
    textaudio.allocate()
    //textaudio.setRate(150.0F)
    //textaudio.setPitch(150.0F)
    //textaudio.setVolume(6.0F)
    textaudio.speak("door")
    textaudio.speak("speak")
    textaudio.speak("apple")
}


fun createSynthesizer(): Synthesizer {

    // seems setting system property is obligatory :-(
    System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory")

    Central.registerEngineCentral("com.sun.speech.freetts.jsapi.FreeTTSEngineCentral")

    //val desc0 = SynthesizerModeDesc(
    //    null,
    //    //"time",  /* use "time" or "general" */
    //    "general",
    //    Locale.US,
    //    false,
    //    null,
    //)

    val desc = SynthesizerModeDesc(Locale.US)

    val central = FreeTTSEngineCentral()
    val list = central.createEngineList(desc)

    val synthesizer: Synthesizer? = if (list.size > 0)
            (list[0] as EngineCreate).createEngine() as Synthesizer
            else null

    return synthesizer ?: throw IllegalStateException("Cannot create synthesizer")
}


@Suppress("unused")
private fun dumpToFile_usingFreeTTSClass() {
    //System.setProperty("com.sun.speech.freetts.voice.defaultAudioPlayer", "com.sun.speech.freetts.audio.SingleFileAudioPlayer");
    System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory")
    Central.registerEngineCentral("com.sun.speech.freetts.jsapi.FreeTTSEngineCentral")

    val vm = VoiceManager.getInstance()
    val voice = vm.getVoice("kevin16") // or "kelvin
    voice.allocate()

    val freeTts = FreeTTS(voice)

    Files.createDirectories(Path.of("temp"))
    freeTts.setAudioFile("temp/aaa.wav")

    freeTts.startup() // it is mandatory to dump to file (otherwise it will be spoken)
    freeTts.textToSpeech("Good bye!")
    freeTts.shutdown()

    voice.deallocate()
}

/*
    desc = (SynthesizerModeDesc) synthesizer.getEngineModeDesc();
    javax.speech.synthesis.Voice[] jsapiVoices = desc.getVoices();
    javax.speech.synthesis.Voice jsapiVoice = voices[0];

    /* Non-JSAPI modification of voice audio player
     */
    if (jsapiVoice instanceof com.sun.speech.freetts.jsapi.FreeTTSVoice) {
        com.sun.speech.freetts.Voice freettsVoice =
            ((com.sun.speech.freetts.jsapi.FreeTTSVoice) jsapiVoice).getVoice();
        freettsVoice.setAudioPlayer(new SingleFileAudioPlayer());
    }
*/


/*
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

public class TextToSpeech {
    public  void Speak(String text) {
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        Voice textaudio = VoiceManager.getInstance().getVoice("kevin16");
        textaudio.allocate();
        textaudio.setRate(150);
        textaudio.setPitch(150);
        textaudio.setVolume(6);
        textaudio.speak(text);
    }
}
*/



/*
/usr/lib/x86_64-linux-gnu/espeak-ng-data/mbrola_ph
/usr/lib/x86_64-linux-gnu/espeak-data/mbrola_ph
/usr/bin/mbrola
/usr/share/mbrola
/usr/share/festival/mbrola.scm
/usr/share/man/man1/mbrola.1.gz
/usr/share/doc/mbrola-en1
/usr/share/doc/mbrola-en1/examples/mbrolamr.pho
/usr/share/doc/mbrola-en1/examples/mbrola.pho
/usr/share/doc/mbrola
/usr/share/doc/mbrola/examples/mbrola-voice-package-generate.gz
/usr/share/doc/espeak/docs/mbrola.html
*/
private fun listMbrolaVoices() {
    System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory")
    System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_time_awb.AlanVoiceDirectory")

    //System.setProperty("mbrola.base", "/home/vmelnykov/.m2/repository/org/mobicents/external/freetts/mbrola/1.2.2")
    //System.setProperty("mbrola.base", "/usr/lib/x86_64-linux-gnu/espeak-ng-data/mbrola_ph")
    //System.setProperty("mbrola.base", "/usr/lib/x86_64-linux-gnu/espeak-ng-data")
    System.setProperty("mbrola.base", "/usr/share/mbrola")
    //System.setProperty("freetts.voices", "de.dfki.lt.freetts.en.us.MbrolaVoiceDirectory")
    System.setProperty("freetts.voices", MbrolaVoiceDirectory22::class.java.name)

    val voiceManager = VoiceManager.getInstance()
    val voices = voiceManager.voices
    println("voices: ${voices.map { it.name }}")
}

/*
mbrolaBase = "/usr/lib/x86_64-linux-gnu/espeak-ng-data/mbrola_ph"
mbrolaBinary = {File@2830} "/usr/lib/x86_64-linux-gnu/espeak-ng-data/mbrola_ph/mbrola"
mbrolaVoiceDB = {File@2835} "/usr/lib/x86_64-linux-gnu/espeak-ng-data/mbrola_ph/us1/us1"

mbrolaBase =    "/usr/lib/x86_64-linux-gnu/espeak-ng-data/mbrola_ph"
mbrolaBinary =  "/usr/lib/x86_64-linux-gnu/espeak-ng-data/mbrola_ph/mbrola"
mbrolaVoiceDB = "/usr/lib/x86_64-linux-gnu/espeak-ng-data/mbrola_ph/us2/us2"

mbrolaBase =    "/usr/lib/x86_64-linux-gnu/espeak-ng-data/mbrola_ph"
mbrolaBinary =  "/usr/lib/x86_64-linux-gnu/espeak-ng-data/mbrola_ph/mbrola"
mbrolaVoiceDB = "/usr/lib/x86_64-linux-gnu/espeak-ng-data/mbrola_ph/us3/us3"


- mbrolaBase =   "/usr/share/mbrola"
+ mbrolaBinary = "/usr/share/mbrola/mbrola"
mbrolaVoiceDB =  "/usr/share/mbrola/us1/us1"


/usr/lib/x86_64-linux-gnu/espeak-ng-data/mbrola_ph
/usr/lib/x86_64-linux-gnu/espeak-data/mbrola_ph
*/

/*
public Voice[] getVoices() {
    String base = Utilities.getProperty("mbrola.base", (String)null);
    if (base != null && base.trim().length() != 0) {
        CMULexicon lexicon = new CMULexicon("cmulex");
        Voice mbrola1 = new MbrolaVoice("us1", "us1", 150.0F, 180.0F, 22.0F, "mbrola_us1", Gender.FEMALE, Age.YOUNGER_ADULT, "MBROLA Voice us1", Locale.US, "general", "mbrola", lexicon);
        Voice mbrola2 = new MbrolaVoice("us2", "us2", 150.0F, 115.0F, 12.0F, "mbrola_us2", Gender.MALE, Age.YOUNGER_ADULT, "MBROLA Voice us2", Locale.US, "general", "mbrola", lexicon);
        Voice mbrola3 = new MbrolaVoice("us3", "us3", 150.0F, 125.0F, 12.0F, "mbrola_us3", Gender.MALE, Age.YOUNGER_ADULT, "MBROLA Voice us3", Locale.US, "general", "mbrola", lexicon);

/usr/lib/x86_64-linux-gnu/espeak-data
/usr/lib/x86_64-linux-gnu/espeak-ng-data
*/



class MbrolaVoiceDirectory22 : VoiceDirectory() {
    override fun getVoices(): Array<Voice> {
        //val base = Utilities.getProperty("mbrola.base", null as String?)
        val base = "/usr/share/mbrola"
        return if (base != null && base.trim { it <= ' ' }.length != 0) {

            val lexicon = CMULexicon("cmulex")

            // TODO: use fixed MbrolaVoice with fixed method getMbrolaBinary
            val mbrola1 = MbrolaVoice(
                "us1",
                "us1",
                150.0f,
                180.0f,
                22.0f,
                "mbrola_us1",
                Gender.FEMALE,
                Age.YOUNGER_ADULT,
                "MBROLA Voice us1",
                Locale.US,
                "general",
                "mbrola",
                lexicon
            )
            val mbrola2 = MbrolaVoice(
                "us2",
                "us2",
                150.0f,
                115.0f,
                12.0f,
                "mbrola_us2",
                Gender.MALE,
                Age.YOUNGER_ADULT,
                "MBROLA Voice us2",
                Locale.US,
                "general",
                "mbrola",
                lexicon
            )
            val mbrola3 = MbrolaVoice(
                "us3",
                "us3",
                150.0f,
                125.0f,
                12.0f,
                "mbrola_us3",
                Gender.MALE,
                Age.YOUNGER_ADULT,
                "MBROLA Voice us3",
                Locale.US,
                "general",
                "mbrola",
                lexicon
            )

            val voices = arrayOf<Voice>(mbrola1, mbrola2, mbrola3)
            val validVoices = ArrayList<Voice>()
            var count = 0

            for (i in voices.indices) {
                val voice = voices[i] as MbrolaVoice
                val validator = MbrolaVoiceValidator(voice)
                try {
                    //validator.validate()
                    //require(File(base, voice.database).exists())
                    validVoices.add(voices[i])
                    ++count
                //} catch (_: ValidationException) { }
                } catch (_: Exception) { }
            }

            if (count == 0) {
                System.err.println("\nCould not validate any MBROLA voices at\n\n  $base\n")
                if (base.indexOf(126.toChar()) != -1) {
                    System.err.println("DO NOT USE ~ as part of the path name\nto specify the mbrola.base property.")
                }
                System.err.println("Make sure you FULLY specify the path to\nthe MBROLA directory using the mbrola.base\nsystem property.\n")
                arrayOf()
            } else {
                validVoices.toTypedArray()
            }
        } else {
            println("System property \"mbrola.base\" is undefined.  Will not use MBROLA voices.")
            arrayOf()
        }
    }

    //companion object {
    //    @JvmStatic
    //    fun main(args: Array<String>) {
    //        println(MbrolaVoiceDirectory22().toString())
    //    }
    //}
}

