package com.mvv.gui.audio

import com.mvv.gui.util.commandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.Executor
import org.apache.commons.exec.PumpStreamHandler
import kotlin.text.Charsets.UTF_8


private val log = mu.KotlinLogging.logger {}


// TODO: add other attributes like gender, pitch, so on
//
// https://stackoverflow.com/questions/38572860/can-festival-ttss-speed-of-speech-be-changed
// (Parameter.set 'Audio_Method 'Audio_Command)
// (Parameter.set 'Audio_Command "aplay -Dplug:default -f S16_LE -r 15000 $FILE")
//
// Consider using the Festival utility text2wave to write the audio as a file, then play the file using sox with
// the speed and pitch effects. To slow the audio down you will need a speed value less than one, and compensate
// for the effect on pitch with a positive value in pitch.
//
// TODO: to see festival songs
//       http://festvox.org/docs/manual-1.4.3/festival_29.html
//
// https://metacpan.org/pod/eGuideDog::Festival
//


data class FestivalVoice (
    val name: String
)


private val predefinedVoicesList: List<FestivalVoice> = listOf(
    FestivalVoice("ked_diphone"),
    FestivalVoice("kal_diphone"),
    FestivalVoice("rab_diphone"),
    FestivalVoice("don_diphone"),
    // mbrola
    // FestivalVoice("en1_mbrola"), broken
    FestivalVoice("us1_mbrola"),
    FestivalVoice("us2_mbrola"),
    FestivalVoice("us3_mbrola"),

    FestivalVoice("cmu_us_slp_cg"),
    FestivalVoice("cmu_us_eey_cg"),
    FestivalVoice("cmu_us_jmk_cg"),
    FestivalVoice("cmu_us_aew_cg"),
    FestivalVoice("cmu_us_awb_cg"),
    FestivalVoice("cmu_us_gka_cg"),
    //FestivalVoice("cmu_us_rms_cg"), // broken
    FestivalVoice("cmu_us_lnh_cg"),
    FestivalVoice("cmu_us_aup_cg"),
    FestivalVoice("cmu_us_ljm_cg"),
    FestivalVoice("cmu_us_ahw_cg"),
    FestivalVoice("cmu_us_clb_cg"),
    FestivalVoice("cmu_us_bdl_cg"),
    FestivalVoice("cmu_us_rxr_cg"),
    FestivalVoice("cmu_us_ksp_cg"),
    FestivalVoice("cmu_us_fem_cg"),
    FestivalVoice("cmu_us_axb_cg"),
    FestivalVoice("cmu_us_slt_c"),
    // all 'arctic' voices are broken (probably because they were designed for 2.0.1 but current festival version is 2.5)
    //FestivalVoice("nitech_us_jmk_arctic_hts"),
    //FestivalVoice("nitech_us_slt_arctic_hts"),
    //FestivalVoice("nitech_us_awb_arctic_hts"),
    //FestivalVoice("nitech_us_bdl_arctic_hts"),
    //FestivalVoice("nitech_us_rms_arctic_hts"),
    //FestivalVoice("nitech_us_clb_arctic_hts"),
)

private val goodVoicesList: List<FestivalVoice> = listOf(
    FestivalVoice("cmu_us_ljm_cg"), // ++
    FestivalVoice("cmu_us_ahw_cg"), // ++ (a bit slow by default)
    FestivalVoice("cmu_us_clb_cg"), // ++
    //
    FestivalVoice("cmu_us_eey_cg"),
    FestivalVoice("cmu_us_jmk_cg"), // (a bit slow by default)
    FestivalVoice("cmu_us_aew_cg"), // (a bit slow by default)
    FestivalVoice("cmu_us_awb_cg"), // (a bit slow by default)
    FestivalVoice("cmu_us_gka_cg"), // (a bit slow by default)
    FestivalVoice("cmu_us_lnh_cg"), // (a bit slow by default)
    FestivalVoice("cmu_us_aup_cg"), //
    FestivalVoice("cmu_us_bdl_cg"),
    FestivalVoice("cmu_us_rxr_cg"),
    FestivalVoice("cmu_us_slt_cg"),
    // mbrola
    // FestivalVoice("en1_mbrola"),// broken
    FestivalVoice("us1_mbrola"),
    FestivalVoice("us2_mbrola"),
    FestivalVoice("us3_mbrola"),
)

class FestivalVoiceManager {

    val predefinedVoices: List<FestivalVoice> get() = predefinedVoicesList

    val goodVoices: List<FestivalVoice> get() = goodVoicesList

    val availableVoices: List<FestivalVoice> get() =
        // TODO: impl, but I don't know how :-)
        predefinedVoices
}


class FestivalVoiceSpeechSynthesizer (private val voice: FestivalVoice) : SpeechSynthesizer {

    override fun speak(text: String) {
        val inputPipedString =
                "(voice_${voice.name})\n" +
                "(SayText \"${text.escapeText()}\")\n" +
                "(quit)\n"

        log.debug { "Festival input: \n$inputPipedString" }

        val executor = createExecutor(inputPipedString)
        val exitCode = executor.execute(commandLine("festival", "--pipe"))

        // TODO: in case of error inside inputPipedString,
        //       we just have 'SIOD ERROR: end of file inside list' but exit-code is 0/success.
        //       Need to determine such errors.
        require(exitCode == 0) { "Error of speaking by festival." }
    }
}


fun String.escapeText(): String =
    this
        .replace("\"", "＂") // "”")
        .replace("'", "’")


private fun createExecutor(inputPipedString: String): Executor =
    DefaultExecutor().also {
        it.streamHandler = PumpStreamHandler(System.out, System.err, inputPipedString.byteInputStream(UTF_8))
    }
