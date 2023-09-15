package com.mvv.gui.audio

import com.sun.speech.freetts.jsapi.FreeTTSEngineCentral
import com.sun.speech.freetts.jsapi.FreeTTSVoice
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
    //test0()
    test1()
    //dumpToFile_usingFreeTTSClass()
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

    val vm = com.sun.speech.freetts.VoiceManager.getInstance()
    val voice = vm.getVoice("kevin16") // or "kelvin
    voice.allocate()

    val freeTts = com.sun.speech.freetts.FreeTTS(voice)

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
