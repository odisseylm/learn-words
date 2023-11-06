package com.mvv.gui.audio

import com.mvv.gui.util.commandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.PumpStreamHandler
import org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset


private val log = mu.KotlinLogging.logger {}


// see System.Speech.Synthesis.VoiceInfo https://learn.microsoft.com/en-us/dotnet/api/system.speech.synthesis.voiceinfo?view=netframework-4.8.1
//
data class WindowsVoice (
    val id: String,
    val name: String,
    val description: String,
    override val gender: Gender,
    val age: Age,
    val culture: String, // corresponds to Java locale
) : Voice {

    private val shortName: String = name.trim().removePrefix("Microsoft").removeSuffix("Desktop").trim()
    override val shortDescription: String = shortName

    companion object {
        @Suppress("unused")
        enum class Age { Adult, Child, Senior, Teen, NotSet }
    }
}


private val predefinedVoicesList: List<WindowsVoice> = listOf(
    WindowsVoice("TTS_MS_EN-US_DAVID_11.0", "Microsoft David Desktop", "Microsoft David Desktop - English (United States)",
        Gender.Male, WindowsVoice.Companion.Age.Adult, "en-US"),
    WindowsVoice("MSTTS_V110_enUS_MarkM", "Microsoft Mark", "Microsoft Mark - English (United States)",
        Gender.Male, WindowsVoice.Companion.Age.Adult, "en-US"),
    WindowsVoice("MSTTS_V110_enUS_ZiraM", "Microsoft Zira", "Microsoft Zira - English (United States)",
        Gender.Female, WindowsVoice.Companion.Age.Adult, "en-US"),
    WindowsVoice("MSTTS_V110_enUS_DavidM", "Microsoft David", "Microsoft David - English (United States)",
        Gender.Male, WindowsVoice.Companion.Age.Adult, "en-US"),
    // !!! Real working name is 'Microsoft Zira', see Windows Registry
    // however script prints 'Microsoft Zira Desktop'
    WindowsVoice("TTS_MS_EN-US_ZIRA_11.0", "Microsoft Zira", "Microsoft Zira Desktop - English (United States)",
        Gender.Female, WindowsVoice.Companion.Age.Adult, "en-US"),

    WindowsVoice("MSTTS_V110_ruRU_IrinaM", "Microsoft Irina", "Microsoft Irina - Russian (Russia)",
        Gender.Female, WindowsVoice.Companion.Age.Adult, "ru-RU"),
    WindowsVoice("MSTTS_V110_ruRU_PavelM", "Microsoft Pavel", "Microsoft Pavel - Russian (Russia)",
        Gender.Male, WindowsVoice.Companion.Age.Adult, "ru-RU"),
    // !!! Real working name is 'Microsoft Irina', see Windows Registry
    // however script prints 'Microsoft Irina Desktop'
    WindowsVoice("TTS_MS_RU-RU_IRINA_11.0", "Microsoft Irina", "Microsoft Irina Desktop - Russian",
        Gender.Female, WindowsVoice.Companion.Age.Adult, "ru-RU"),
)


class WindowsVoiceManager {
    val predefinedVoices: List<WindowsVoice> get() = predefinedVoicesList

    val availableVoices: List<WindowsVoice> get() =
        // TODO: impl parsing result of
        //  `PowerShell -Command "Add-Type –AssemblyName System.Speech; (New-Object System.Speech.Synthesis.SpeechSynthesizer).GetInstalledVoices().VoiceInfo;"`
        // or in PowerShell console
        // Add-Type -AssemblyName System.Speech
        // $speak = New-Object System.Speech.Synthesis.SpeechSynthesizer
        // # print voices
        // $speak.GetInstalledVoices()
        // $speak.GetInstalledVoices().VoiceInfo
        predefinedVoicesList
}


class WindowsSpeechSynthesizer (override val voice: WindowsVoice) : SpeechSynthesizer {

    override val shortDescription: String = "MS TTS ${voice.name.trim().removePrefix("Microsoft").removeSuffix("Desktop").trim()}"

    override fun speak(text: String) {

        val pumpStdOut = if (log.isDebugEnabled) System.out else null
        //val pumpStdOut = System.out

        val ioCharset = if (IS_OS_WINDOWS) windowsConsoleCharset else Charset.defaultCharset()

        val powerShellScript =
            "Add-Type -AssemblyName System.Speech;\r\n" +
            "\$speak = New-Object System.Speech.Synthesis.SpeechSynthesizer;\r\n" +
            "\$speak.SelectVoice(\"${voice.name}\");\r\n" +
            "\$speak.Speak(\"${text.escapeText()}\");\r\n" +
            "exit\r\n"

        val exitCode = DefaultExecutor()
            .also { it.streamHandler = PumpStreamHandler(pumpStdOut, System.err, powerShellScript.byteInputStream(ioCharset)) }
            .execute(commandLine("PowerShell")) // , "-Encoding", "UTF8"))
        require(exitCode == 0) { "Failed to launch windows speech synthesizer (exitCode = $exitCode)." }
    }

    // In general it is not enough, because it may no be enabled in Windows Registry for any applications.
    // See enable instructions in README.md
    override val isAvailable: Boolean get() = IS_OS_WINDOWS
}


private val windowsConsoleCharset: Charset by lazy { getWindowsConsoleCharsetImpl() }

internal fun getWindowsConsoleCharsetImpl(): Charset {

    val contentStream = ByteArrayOutputStream()

    DefaultExecutor()
            .also { it.streamHandler = PumpStreamHandler(contentStream) }
            .execute(commandLine("cmd", "/C", "chcp"))

    val content = contentStream.toString(Charsets.ISO_8859_1).trim()
            .removePrefix("Active code page:")

    // TODO: use regex to get last digits
    val codePageNumberIndex = content.lastIndexOfAny(charArrayOf(' ', '\t', ':')) + 1 // or proper index or 0 (- 1  + 1)

    val codePageNumberStr = content.substring(codePageNumberIndex).trim().removePrefix(":").trim()

    val codePageNumber = try { codePageNumberStr.toInt() }
                         catch (_: Exception) {
                             log.warn { "Error of parsing windows console charset from [$content]." }
                             return Charset.defaultCharset()
                         }

    log.info { "Windows console code page: $codePageNumber." }

    val possibleWindowsCharsetNames = listOf("cp$codePageNumber", "cp-$codePageNumber", "windows-$codePageNumber")
    val consoleCharset: Charset? = possibleWindowsCharsetNames.asSequence()
            .map { try { Charset.forName(it) } catch (_: Exception) { null } }
            .filterNotNull()
            .firstOrNull()

    if (consoleCharset == null) {
        log.warn { "Charset for codepage $codePageNumber is not found." }
    }

    return consoleCharset ?: Charset.defaultCharset()
}


/*
VoiceAge
Adult 	30   Indicates an adult voice (age 30).
Child 	10   Indicates a child voice (age 10).
NotSet 	0    Indicates that no voice age is specified.
Senior 	65   Indicates a senior voice (age 65).
Teen 	15   Indicates a teenage voice (age 15).

VoiceGender
Female  2   Indicates a female voice.
Male    1 	Indicates a male voice.
Neutral 3   Indicates a gender-neutral voice.
NotSet  0   Indicates no voice gender specification.
*/

/*
Add-Type -AssemblyName System.Speech;
$speak = New-Object System.Speech.Synthesis.SpeechSynthesizer;
$speak.SelectVoice("Microsoft Irina");
$speak.Speak("Привет пацаны!");
*/

/*
Gender                : Male
Age                   : Adult
Name                  : Microsoft David Desktop
Culture               : en-US
Id                    : TTS_MS_EN-US_DAVID_11.0
Description           : Microsoft David Desktop - English (United States)
SupportedAudioFormats : {}
AdditionalInfo        : {[Age, Adult], [Gender, Male], [Language, 409], [Name, Microsoft David Desktop]...}

Gender                : Male
Age                   : Adult
Name                  : Microsoft Mark
Culture               : en-US
Id                    : MSTTS_V110_enUS_MarkM
Description           : Microsoft Mark - English (United States)
SupportedAudioFormats : {}
AdditionalInfo        : {[Age, Adult], [DataVersion, 11.0.2013.1022], [Gender, Male], [Language, 409]...}

Gender                : Female
Age                   : Adult
Name                  : Microsoft Zira
Culture               : en-US
Id                    : MSTTS_V110_enUS_ZiraM
Description           : Microsoft Zira - English (United States)
SupportedAudioFormats : {}
AdditionalInfo        : {[Age, Adult], [DataVersion, 11.0.2013.1022], [Gender, Female], [Language, 409]...}

Gender                : Female
Age                   : Adult
Name                  : Microsoft Irina
Culture               : ru-RU
Id                    : MSTTS_V110_ruRU_IrinaM
Description           : Microsoft Irina - Russian (Russia)
SupportedAudioFormats : {}
AdditionalInfo        : {[Age, Adult], [DataVersion, 11.0.2013.1022], [Gender, Female], [Language, 419]...}

Gender                : Male
Age                   : Adult
Name                  : Microsoft Pavel
Culture               : ru-RU
Id                    : MSTTS_V110_ruRU_PavelM
Description           : Microsoft Pavel - Russian (Russia)
SupportedAudioFormats : {}
AdditionalInfo        : {[Age, Adult], [DataVersion, 11.0.2013.1022], [Gender, Male], [Language, 419]...}

Gender                : Male
Age                   : Adult
Name                  : Microsoft David
Culture               : en-US
Id                    : MSTTS_V110_enUS_DavidM
Description           : Microsoft David - English (United States)
SupportedAudioFormats : {}
AdditionalInfo        : {[Age, Adult], [DataVersion, 11.0.2016.0129], [Gender, Male], [Language, 409]...}

Gender                : Female
Age                   : Adult
Name                  : Microsoft Zira Desktop
Culture               : en-US
Id                    : TTS_MS_EN-US_ZIRA_11.0
Description           : Microsoft Zira Desktop - English (United States)
SupportedAudioFormats : {}
AdditionalInfo        : {[Age, Adult], [Gender, Female], [Language, 409], [Name, Microsoft Zira Desktop]...}

Gender                : Female
Age                   : Adult
Name                  : Microsoft Irina Desktop
Culture               : ru-RU
Id                    : TTS_MS_RU-RU_IRINA_11.0
Description           : Microsoft Irina Desktop - Russian
SupportedAudioFormats : {}
AdditionalInfo        : {[Age, Adult], [Gender, Female], [Language, 419], [Name, Microsoft Irina Desktop]...}
*/
