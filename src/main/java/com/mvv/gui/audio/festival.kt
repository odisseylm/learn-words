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
    val name: String,
    override val gender: Gender? = null,
) : Voice {
    override val shortDescription: String = name
}


private val predefinedVoicesList: List<FestivalVoice> = listOf(
    FestivalVoice("ked_diphone", Gender.Male),
    FestivalVoice("kal_diphone", Gender.Male),
    FestivalVoice("rab_diphone", Gender.Male),
    FestivalVoice("don_diphone", Gender.Male),
    // mbrola
    // FestivalVoice("en1_mbrola"), broken
    FestivalVoice("us1_mbrola", Gender.Female),
    FestivalVoice("us2_mbrola", Gender.Male),
    FestivalVoice("us3_mbrola", Gender.Male),

    FestivalVoice("cmu_us_slp_cg", Gender.Female),
    FestivalVoice("cmu_us_eey_cg", Gender.Female),
    FestivalVoice("cmu_us_jmk_cg", Gender.Male),
    FestivalVoice("cmu_us_aew_cg", Gender.Male),
    FestivalVoice("cmu_us_awb_cg", Gender.Male),
    FestivalVoice("cmu_us_gka_cg", Gender.Male),
    //FestivalVoice("cmu_us_rms_cg"), // broken
    FestivalVoice("cmu_us_lnh_cg", Gender.Female),
    FestivalVoice("cmu_us_aup_cg", Gender.Neutral), // It seems for me like gender-neutral :-)
    FestivalVoice("cmu_us_ljm_cg", Gender.Female),
    FestivalVoice("cmu_us_ahw_cg", Gender.Male),
    FestivalVoice("cmu_us_clb_cg", Gender.Female),
    FestivalVoice("cmu_us_bdl_cg", Gender.Male),
    FestivalVoice("cmu_us_rxr_cg", Gender.Neutral), // It seems for me like gender-neutral :-)  It is like 'female' but too low.
    FestivalVoice("cmu_us_slt_cg", Gender.Female),
    FestivalVoice("cmu_us_ksp_cg", Gender.Male),
    FestivalVoice("cmu_us_fem_cg", Gender.Male),
    FestivalVoice("cmu_us_axb_cg", Gender.Female),
    FestivalVoice("cmu_us_slt_c"), // ??? broken
    // all 'arctic' voices are broken (probably because they were designed for 2.0.1 but current festival version is 2.5)
    //FestivalVoice("nitech_us_jmk_arctic_hts"),
    //FestivalVoice("nitech_us_slt_arctic_hts"),
    //FestivalVoice("nitech_us_awb_arctic_hts"),
    //FestivalVoice("nitech_us_bdl_arctic_hts"),
    //FestivalVoice("nitech_us_rms_arctic_hts"),
    //FestivalVoice("nitech_us_clb_arctic_hts"),
)

private val goodVoicesList: List<FestivalVoice> = listOf(
    FestivalVoice("cmu_us_ljm_cg", Gender.Female), // ++
    FestivalVoice("cmu_us_ahw_cg", Gender.Male), // ++ (a bit slow by default)
    FestivalVoice("cmu_us_clb_cg", Gender.Female), // ++
    //
    FestivalVoice("cmu_us_eey_cg", Gender.Female),
    FestivalVoice("cmu_us_jmk_cg", Gender.Male),    // (a bit slow by default)
    FestivalVoice("cmu_us_aew_cg", Gender.Male),    // (a bit slow by default)
    FestivalVoice("cmu_us_awb_cg", Gender.Male),    // (a bit slow by default)
    FestivalVoice("cmu_us_gka_cg", Gender.Male),    // (a bit slow by default)
    FestivalVoice("cmu_us_lnh_cg", Gender.Female),  // (a bit slow by default)
    FestivalVoice("cmu_us_aup_cg", Gender.Neutral), // It seems for me like gender-neutral :-)
    FestivalVoice("cmu_us_bdl_cg", Gender.Male),
    FestivalVoice("cmu_us_rxr_cg", Gender.Neutral), // It seems for me like gender-neutral :-)  It is like 'female' but too low.
    FestivalVoice("cmu_us_slt_cg", Gender.Female),
    // mbrola
    // FestivalVoice("en1_mbrola"),// broken
    FestivalVoice("us1_mbrola", Gender.Female),
    FestivalVoice("us2_mbrola", Gender.Male),
    FestivalVoice("us3_mbrola", Gender.Male),
)

class FestivalVoiceManager {

    val predefinedVoices: List<FestivalVoice> get() = predefinedVoicesList

    val goodVoices: List<FestivalVoice> get() = goodVoicesList

    val availableVoices: List<FestivalVoice> get() =
        // T O D O: impl, but I don't know how :-)
        predefinedVoices
}


class FestivalVoiceSpeechSynthesizer (override val voice: FestivalVoice) : SpeechSynthesizer {

    override val shortDescription: String = "Festival ${voice.name}"

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
        //.replace("'", "’") // Why do I need it? We can  use such replacement only as they are wrapping word.


private fun createExecutor(inputPipedString: String): Executor =
    DefaultExecutor().also {
        it.streamHandler = PumpStreamHandler(System.out, System.err, inputPipedString.byteInputStream(UTF_8))
    }
