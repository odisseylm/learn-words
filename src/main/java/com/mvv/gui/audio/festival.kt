package com.mvv.gui.audio

import com.mvv.gui.util.*
import org.apache.commons.exec.*
import org.apache.commons.lang3.SystemUtils.IS_OS_LINUX
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.io.UncheckedIOException
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import kotlin.text.Charsets.UTF_8


private val log = mu.KotlinLogging.logger {}


// https://stackoverflow.com/questions/38572860/can-festival-ttss-speed-of-speech-be-changed
// (Parameter.set 'Audio_Method 'Audio_Command)
// (Parameter.set 'Audio_Command "aplay -Dplug:default -f S16_LE -r 15000 $FILE")
//
// Consider using the Festival utility text2wave to write the audio as a file, then play the file using sox with
// the speed and pitch effects. To slow the audio down you will need a speed value less than one, and compensate
// for the effect on pitch with a positive value in pitch.
//
// Old Festival has interesting feature - festival songs
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
        // It would be nice to impl, but I don't know how :-)
        predefinedVoices
}


private val isFestivalCommandPresent: Boolean by lazy {
    IS_OS_LINUX && (
               doTry({ executeCommandWithOutput("which", "festival") }, "")        .trim().endsWith("/festival")
            || doTry({ executeCommandWithOutput("command", "-v", "festival") }, "").trim().endsWith("/festival")
    )
}

class FestivalVoiceSpeechSynthesizer (override val voice: FestivalVoice) : SpeechSynthesizer {

    override val shortDescription: String = "Festival ${voice.name}"

    override fun toString(): String = shortDescription

    private val processDestroyer = LastProcessDestroyer()

    override fun interrupt() = processDestroyer.destroyAllProcesses()

    override fun speak(text: String) {

        log.debug { "Speak by ${this.shortDescription} ${this.hashCode()}, prevProcesses: ${processDestroyer.size()}" }

        processDestroyer.destroyAllProcesses()

        val inputPipedString =
                "(voice_${voice.name})\n" +
                "(SayText \"${text.escapeText()}\")\n" +
                "(quit)\n"

        log.debug { "Festival input: \n$inputPipedString" }

        val bout = ByteArrayOutputStream()

        val executor = createExecutorWithInput(inputPipedString, bout)

        //executor.watchdog = ExecuteWatchdog(15_000L)
        executor.processDestroyer = processDestroyer

        val exitCode = executor.execute(commandLine("festival", "--pipe"))
        val resultOutput = bout.toString(UTF_8)

        val failed = (exitCode != 0) || resultOutput.contains("SIOD ERROR")

        if (failed) {
            log.error(resultOutput)

            val errStr = resultOutput.substringStartingFrom("SIOD ERROR", "\n", 200)
            throw UncheckedIOException(IOException("Error of speaking by festival. ($errStr)"))
        }
    }

    override val isAvailable: Boolean get() = isFestivalCommandPresent
}


fun String.escapeText(): String =
    this
        .replace("\"", "＂") // "”")
        //.replace("'", "’") // Why do I need it? We can  use such replacement only as they are wrapping word.


private fun createExecutorWithInput(inputPipedString: String, outputStream: OutputStream): Executor =
    DefaultExecutor().also {
        //it.streamHandler = PumpStreamHandler(System.out, System.err, inputPipedString.byteInputStream(UTF_8))
        it.streamHandler = PumpStreamHandler(outputStream, outputStream, inputPipedString.byteInputStream(UTF_8))
    }

private class LastProcessDestroyer : ProcessDestroyer {
    private val processes: MutableList<Process> = CopyOnWriteArrayList() // really does not matter which kind of thread-safe list is used

    override fun add(process: Process): Boolean = this.processes.add(process)
    override fun remove(process: Process): Boolean = this.processes.remove(process)
    override fun size(): Int = this.processes.size

    fun destroyAllProcesses() {
        val safeSubProcessesRef = processes.toList()

        safeSubProcessesRef.forEach {
            it.descendants().forEach { subProcess -> subProcess.destroy() }
            it.destroy()
        }

        Thread.sleep(50)
        // forcibly
        safeSubProcessesRef.forEach {
            it.descendants().forEach { subProcess ->
                subProcess.destroyForcibly()
                subProcess.onExit().get(5, TimeUnit.SECONDS)
            }

            it.destroyForcibly()
            it.onExit().get(5, TimeUnit.SECONDS)
        }
    }
}
