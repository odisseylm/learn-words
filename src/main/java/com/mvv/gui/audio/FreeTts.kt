package com.mvv.gui.audio

import com.mvv.gui.util.containsOneOf
import com.mvv.gui.util.isEven
import com.mvv.gui.util.nullIfNotExists
import com.mvv.gui.util.userHome
import com.sun.speech.freetts.Age
import com.sun.speech.freetts.Gender
import com.sun.speech.freetts.ValidationException
import com.sun.speech.freetts.Validator
import com.sun.speech.freetts.VoiceDirectory
import com.sun.speech.freetts.en.us.CMULexicon
import com.sun.speech.freetts.util.Utilities
import org.apache.commons.lang3.SystemUtils
import java.nio.file.Path
import java.util.*
import javax.speech.Central
import javax.speech.synthesis.Synthesizer
import javax.speech.synthesis.SynthesizerModeDesc
import kotlin.io.path.exists
import kotlin.io.path.notExists


private val log = mu.KotlinLogging.logger {}


fun initFreeTts() {
    initMbrolaBase()

    val voiceDirectoryClasses: List<Class<out VoiceDirectory>> = listOf(
        com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory::class.java,
        com.sun.speech.freetts.en.us.cmu_time_awb.AlanVoiceDirectory::class.java,
        MbrolaVoiceDirectory::class.java,
    )
    System.setProperty("freetts.voices", voiceDirectoryClasses.joinToString(",") { it.name })

    Central.registerEngineCentral(com.sun.speech.freetts.jsapi.FreeTTSEngineCentral::class.java.name)
    //Central.registerEngineCentral(edu.cmu.sphinx.jsapi.SphinxEngineCentral::class.java.name)
}

private fun initMbrolaBase() {

    if (!System.getProperty("mbrola.base").isNullOrBlank())
        return

    val mbrolaBaseDir = findMbrolaBaseDir()
    if (mbrolaBaseDir == null)
        throw IllegalStateException("Mbrola is not found in predefined places." +
                                    " Please install it or use system property 'mbrola.base'.")
    else
        System.setProperty("mbrola.base", mbrolaBaseDir.toString())
}

internal fun findMbrolaBaseDir(): Path? {
    val mbrolaBase = System.getProperty("mbrola.base")
    if (mbrolaBase != null && mbrolaBase.isNotBlank()) return Path.of(mbrolaBase)

    val mbrolaBaseDir = when {
        SystemUtils.IS_OS_LINUX ->
            Path.of("/usr/share/mbrola").nullIfNotExists

        SystemUtils.IS_OS_WINDOWS ->
            windowsProgramFilesDirs()
                .asSequence()
                .map { it.resolve("mbrola") }
                .find { it.exists() }

        else -> null
    }

    return mbrolaBaseDir
}


private fun windowsProgramFilesDirs(): List<Path> =
    sequenceOf(
            System.getenv("ProgramFiles"),
            System.getenv("ProgramFiles(x86)"),
            System.getenv("ProgramW6432"),
            "${userHome}/AppData/Local/Programs",
            "${userHome}/Tools",
            "$userHome",
        )
        .filterNotNull()
        .filter { it.isNotBlank() }
        .map { Path.of(it) }
        .filter { it.exists() }
        .toList()


class FreeTtsSpeechSynthesizer(private val voice: com.sun.speech.freetts.Voice) : SpeechSynthesizer {
    //@Synchronized (need to synchronize by voice but is it safe??)
    override fun speak(text: String) {

        //voice.audioPlayer = com.sun.speech.freetts.audio.JavaClipAudioPlayer()
        //voice.audioPlayer = com.sun.speech.freetts.audio.RawFileAudioPlayer()

        // It does not help
        //voice.audioPlayer = FixedForWindowsJavaStreamingAudioPlayer()

        // TODO: compare with format on Linux

        /*
        //voice.audioPlayer = JavaClipAudioPlayer()

        On Windows
        with mbrola voice error:
          Error: illegal request to write non-integral number of frames (57 bytes, frameSize = 2 bytes)

        result = {JavaStreamingAudioPlayer@3681} "JavaStreamingAudioPlayer"
        paused = false
        done = false
        cancelled = false
        line = null
        volume = 1.0
        timeOffset = 0
        timer = {BulkTimer@3682}
        defaultFormat = {AudioFormat@3683} "PCM_SIGNED 8000.0 Hz, 16 bit, mono, 2 bytes/frame, big-endian"
        currentFormat = {AudioFormat@3683} "PCM_SIGNED 8000.0 Hz, 16 bit, mono, 2 bytes/frame, big-endian"
        debug = false
        audioMetrics = false
        firstSample = true
        cancelDelay = 0
        drainDelay = 150
        openFailDelayMs = 0
        totalOpenFailDelayMs = 0
        */

        /*
        result = {JavaStreamingAudioPlayer@3802} "JavaStreamingAudioPlayer"
        paused = false
        done = false
        cancelled = false
        line = null
        volume = 1.0
        timeOffset = 0
        timer = {BulkTimer@3804}
        defaultFormat = {AudioFormat@3805} "PCM_SIGNED 8000.0 Hz, 16 bit, mono, 2 bytes/frame, big-endian"
        currentFormat = {AudioFormat@3805} "PCM_SIGNED 8000.0 Hz, 16 bit, mono, 2 bytes/frame, big-endian"
        debug = false
        audioMetrics = false
        firstSample = true
        cancelDelay = 0
        drainDelay = 150
        openFailDelayMs = 0
        totalOpenFailDelayMs = 0

        0 = "mbrola"
        1 = "-e"
        2 = "-R"
        3 = "V ah i iy I ih U uh { ae @ ax r= er A aa O ao u uw E eh EI ey AI ay OI oy aU aw @U ow j y h hh N ng S sh T th Z zh D dh tS ch dZ jh _ pau"
        4 = "/usr/share/mbrola/us1/us1"
        5 = "-"
        6 = "-.raw"


        mbrola -help

         USAGE: mbrola [COMMAND LINE OPTIONS] database pho_file+ output_file

        A - instead of pho_file or output_file means stdin or stdout
        Extension of output_file ( raw, au, wav, aiff ) tells the wanted audio format

        Options can be any of the following:
        -i    = display the database information if any
        -e    = IGNORE fatal errors on unkown diphone
        -c CC = set COMMENT char (escape sequence in pho files)
        -F FC = set FLUSH command name
        -v VR = VOLUME ratio, float ratio applied to ouput samples
        -f FR = FREQ ratio, float ratio applied to pitch points
        -t TR = TIME ratio, float ratio applied to phone durations
        -l VF = VOICE freq, target freq for voice quality
        -R RL = Phoneme RENAME list of the form a A b B ...
        -C CL = Phoneme CLONE list of the form a A b B ...

        -I IF = Initialization file containing one command per line
                CLONE, RENAME, VOICE, TIME, FREQ, VOLUME, FLUSH, COMMENT,
                and IGNORE are available
        -W store the datbase in ROM format
        -w the database in a ROM dump


        */

        voice.allocate()
        voice.speak(text)
        voice.deallocate()
    }
}



/* This impl (for fun) uses only pure java-speech API. */
class JavaSpeechSpeechSynthesizer(
    private val voice: javax.speech.synthesis.Voice,
    private val locale: Locale, // it is desired to set, otherwise it will be set automatically in default (probably undesired) value :-(
    ) : SpeechSynthesizer {

    constructor(voice: com.sun.speech.freetts.Voice) : this(voice.toJavaxSpeechVoice(), voice.locale)

    override fun speak(text: String) {

        val modeDesc = SynthesizerModeDesc(null, null, locale, null, arrayOf(voice))

        val synthesizer = Central.createSynthesizer(modeDesc)
        requireNotNull(synthesizer) { "java.speech.Synthesizer is not created." }
        cleanLastSynthesizerRef()

        synthesizer.allocate()
        synthesizer.synthesizerProperties.voice = voice

        synthesizer.resume()

        synthesizer.speakPlainText(text, null)
        synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY)

        synthesizer.cancelAll()
        synthesizer.deallocate()

        // Hack to avoid reusing last synthesizer and causing error if it is already deallocated
        // Or you can clean Central.lastSynthesizer using reflection even after immediate synthesizer creation.
        //
        //synthesizer.engineModeDesc.locale = Locale("rnd" + System.currentTimeMillis())
    }

    private fun cleanLastSynthesizerRef() =
        Central::class.java.getDeclaredField("lastSynthesizer")
            .also { lastSynthesizer -> lastSynthesizer.trySetAccessible(); lastSynthesizer.set(null, null) }
}



/** Fixed version. Original version requires 'mbrola' executable in directory with mbrola dictionaries (what is unusual for unix). */
class MbrolaVoiceDirectory(private val mbrolaBaseDir: Path? = null) : VoiceDirectory() {

    override fun getVoices(): Array<MbrolaVoice> {

        val mbrolaBinary = findMbrolaBinary()
        val mbrolaBaseDir = mbrolaBaseDir ?: getMbrolaBaseDir()

        val predefinedVoices = predefinedVoices(mbrolaBinary, mbrolaBaseDir)

        val validVoices = predefinedVoices
            // de.dfki.lt.freetts.en.us.MbrolaVoiceValidator requires absolute path of mbrolaBinary.
            // I don't want to search real full path of 'mbrola'.
            //.filter { try { MbrolaVoiceValidator(it).validate(); true } catch (ex: Exception) { false } }
            //
            .filter { try { validateVoice(it); true } catch (ex: Exception) { false } }

        if (validVoices.isEmpty()) {
            log.error { "\nCould not validate any MBROLA voices at $mbrolaBaseDir\n" }
            if ('~' in mbrolaBaseDir.toString())
                log.error { "DO NOT USE ~ as part of the path name to specify the 'mbrola.base' property." }

            log.error { "Make sure you FULLY specify the path to the MBROLA directory using the 'mbrola.base' system property.\n" }
        }

        return validVoices.toTypedArray()
    }

    private fun predefinedVoices(mbrolaBinary: String?, mbrolaBaseDir: Path?): List<MbrolaVoice> {
        val lexicon = CMULexicon("cmulex")
        return listOf(
            MbrolaVoice(
                mbrolaBinary, mbrolaBaseDir, "en1",
                "en1", //pitch 82 117
                150.0f, 100.0f, 12.0f, // TODO: it may be wrong!? Need to validate in some way.
                "mbrola_en1",
                Gender.MALE, Age.YOUNGER_ADULT, "MBROLA Voice en1", Locale.ENGLISH, "general", "mbrola",
                lexicon,
            ),
            MbrolaVoice(
                mbrolaBinary, mbrolaBaseDir, "us1",
                "us1",
                150.0f, 180.0f, 22.0f,
                "mbrola_us1",
                Gender.FEMALE, Age.YOUNGER_ADULT, "MBROLA Voice us1", Locale.US, "general", "mbrola",
                lexicon,
            ),
            MbrolaVoice(
                mbrolaBinary, mbrolaBaseDir, "us2",
                "us2",
                150.0f, 115.0f, 12.0f,
                "mbrola_us2",
                Gender.MALE, Age.YOUNGER_ADULT, "MBROLA Voice us2", Locale.US, "general", "mbrola",
                lexicon,
            ),
            MbrolaVoice(
                mbrolaBinary, mbrolaBaseDir, "us3",
                "us3",
                150.0f, 125.0f, 12.0f,
                "mbrola_us3",
                Gender.MALE, Age.YOUNGER_ADULT, "MBROLA Voice us3", Locale.US, "general", "mbrola",
                lexicon,
            ),
        )
    }

    private fun getMbrolaBaseDir(): Path = this.mbrolaBaseDir ?: Path.of(Utilities.getProperty("mbrola.base", "."))

    private fun findMbrolaBinary(): String {
        val exeFilename = if (SystemUtils.IS_OS_WINDOWS) "mbrola.exe" else "mbrola"
        val asPath = getMbrolaBaseDir().resolve(exeFilename)
        return if (asPath.exists()) asPath.toString() else exeFilename
    }

    private fun validateVoice(voice: MbrolaVoice) {

        val isPath = voice.mbrolaBinary.containsOneOf("/\\")
        if (isPath && Path.of(voice.mbrolaBinary).notExists())
            throw ValidationException("No MBROLA binary at: ${voice.mbrolaBinary}.")

        if (Path.of(voice.database).notExists())
            throw ValidationException("No voice database for ${voice.name} at: ${voice.database}.")

        // throw ValidationException("System property \"mbrola.base\" is undefined. You might need to set the MBROLA_DIR environment variable.")
    }
}


class MbrolaVoice(
    private val mbrolaBinary: String?,
    private val mbrolaBaseDir: Path?,
    databaseDirectory: String, database: String,
    rate: Float, pitch: Float, range: Float,
    name: String, gender: Gender, age: Age,
    description: String, locale: Locale,
    domain: String, organization: String, lexicon: CMULexicon
) : de.dfki.lt.freetts.en.us.MbrolaVoice(databaseDirectory, database,
    rate, pitch, range, name, gender, age, description, locale,
    domain, organization, lexicon) {

    override fun getMbrolaBase(): String =
        this.mbrolaBaseDir?.toString() ?: super.getMbrolaBase()

    override fun getMbrolaBinary(): String =
        this.mbrolaBinary ?: super.getMbrolaBinary()
}

class NoValidator : Validator { override fun validate() { } }

fun com.sun.speech.freetts.Voice.toJavaxSpeechVoice(): javax.speech.synthesis.Voice =
    com.sun.speech.freetts.jsapi.FreeTTSVoice(this, NoValidator::class.java.name)

/*
fun com.sun.speech.freetts.Voice.toJavaxSpeechVoice(): javax.speech.synthesis.Voice =
    javax.speech.synthesis.Voice(this.name, this.gender.toJavaSpeechVoiceGender(),
                                 this.age.toJavaSpeechVoiceAge(), this.style)

private fun Gender?.toJavaSpeechVoiceGender(): Int = when (this) {
    null -> javax.speech.synthesis.Voice.GENDER_DONT_CARE
    Gender.FEMALE    -> javax.speech.synthesis.Voice.GENDER_FEMALE
    Gender.MALE      -> javax.speech.synthesis.Voice.GENDER_MALE
    Gender.NEUTRAL   -> javax.speech.synthesis.Voice.GENDER_NEUTRAL
    Gender.DONT_CARE -> javax.speech.synthesis.Voice.GENDER_DONT_CARE
    else -> javax.speech.synthesis.Voice.GENDER_DONT_CARE
}

private fun Age?.toJavaSpeechVoiceAge(): Int = when (this) {
    null -> javax.speech.synthesis.Voice.AGE_DONT_CARE
    Age.CHILD         -> javax.speech.synthesis.Voice.AGE_CHILD
    Age.TEENAGER      -> javax.speech.synthesis.Voice.AGE_TEENAGER
    Age.YOUNGER_ADULT -> javax.speech.synthesis.Voice.AGE_YOUNGER_ADULT
    Age.MIDDLE_ADULT  -> javax.speech.synthesis.Voice.AGE_MIDDLE_ADULT
    Age.OLDER_ADULT   -> javax.speech.synthesis.Voice.AGE_OLDER_ADULT
    Age.NEUTRAL       -> javax.speech.synthesis.Voice.AGE_NEUTRAL
    Age.DONT_CARE     -> javax.speech.synthesis.Voice.AGE_DONT_CARE
    else -> javax.speech.synthesis.Voice.AGE_DONT_CARE
}
*/

@Suppress("unused")
class FixedForWindowsJavaStreamingAudioPlayer : com.sun.speech.freetts.audio.JavaStreamingAudioPlayer() {

    override fun write(bytes: ByteArray?, offset: Int, size: Int): Boolean {
        return super.write(bytes, offset, size)
    }

    override fun write(audioData: ByteArray): Boolean =
        if (audioFormat.frameSize.isEven && !audioData.size.isEven)
            super.write(audioData, 0, audioData.size  - 1)
        else
            super.write(audioData)

}
