package com.mvv.gui.words

import com.mvv.gui.audio.AudioSource
import com.mvv.gui.audio.JavaSoundPlayer
import com.mvv.gui.audio.PlayingMode
import java.net.URL
import java.net.URLEncoder


/*
VOICE_SELECTIONS=dfki-spike-hsmm%20en_GB%20male%20hmm&AUDIO_OUT=WAVE_FILE&LOCALE=en_GB&VOICE=dfki-spike-hsmm&AUDIO=WAVE_FILE
VOICE_SELECTIONS=dfki-spike%20en_GB%20male%20unitselection%20general&AUDIO_OUT=WAVE_FILE&LOCALE=en_GB&VOICE=dfki-spike&AUDIO=WAVE_FILE
VOICE_SELECTIONS=cmu-bdl-hsmm%20en_US%20male%20hmm&AUDIO_OUT=WAVE_FILE&LOCALE=en_US&VOICE=cmu-bdl-hsmm&AUDIO=WAVE_FILE
VOICE_SELECTIONS=dfki-spike%20en_GB%20male%20unitselection%20general&AUDIO_OUT=WAVE_FILE&LOCALE=en_GB&VOICE=dfki-spike&AUDIO=WAVE_FILE
VOICE_SELECTIONS=dfki-prudence%20en_GB%20female%20unitselection%20general&AUDIO_OUT=WAVE_FILE&LOCALE=en_GB&VOICE=dfki-prudence&AUDIO=WAVE_FILE
VOICE_SELECTIONS=cmu-slt-hsmm%20en_US%20female%20hmm&AUDIO_OUT=WAVE_FILE&LOCALE=en_US&VOICE=cmu-slt-hsmm&AUDIO=WAVE_FILE
VOICE_SELECTIONS=cmu-bdl-hsmm%20en_US%20male%20hmm&AUDIO_OUT=WAVE_FILE&LOCALE=en_US&VOICE=cmu-bdl-hsmm&AUDIO=WAVE_FILE
VOICE_SELECTIONS=dfki-obadiah-hsmm%20en_GB%20male%20hmm&AUDIO_OUT=WAVE_FILE&LOCALE=en_GB&VOICE=dfki-obadiah-hsmm&AUDIO=WAVE_FILE
VOICE_SELECTIONS=dfki-obadiah%20en_GB%20male%20unitselection%20general&AUDIO_OUT=WAVE_FILE&LOCALE=en_GB&VOICE=dfki-obadiah&AUDIO=WAVE_FILE
*/



// maryTts.setAudioEffects("Rate(durScale:1.5)");
// Add this then it will work. After test 0.5 is fast 1.0 is normal and 1.5 is slow.


@Suppress("PropertyName")
data class MarryTtsSpeechConfig (
    val port: Int = 59125,
    val voice: String = "cmu-slt-hsmm",
    val voice_Selections: String = "cmu-slt-hsmm en_US female hmm",
    val locale: String = "en_US",

    val effect_Volume_parameters: String = "amount:2.0;",

    val effect_TractScaler_selected: String = "",
    val effect_TractScaler_parameters: String = "amount:1.5;",
    val effect_F0Scale_selected: String = "",
    val effect_F0Scale_parameters: String = "f0Scale:2.0;",
    val effect_F0Add_selected: String = "",
    val effect_F0Add_parameters: String = "f0Add:50.0;",
    val effect_Rate_selected: String = "",
    val effect_Rate_parameters: String = "durScale:1.5;",
    val effect_Robot_selected: String = "",
    val effect_Robot_parameters: String = "amount:100.0;",
    val effect_Whisper_selected: String = "",
    val effect_Whisper_parameters: String = "amount:100.0;",
    val effect_Stadium_selected: String = "",
    val effect_Stadium_parameters: String = "amount:100.0;",
    val effect_Chorus_selected: String = "",
    val effect_Chorus_parameters: String = "delay1:466;amp1:0.54;delay2:600;amp2:-0.10;delay3:250;amp3:0.30",
    val effect_FIRFilter_selected: String = "",
    val effect_FIRFilter_parameters: String = "type:3;fc1:500.0;fc2:2000.0",
    val effect_JetPilot_selected: String = "",
    val effect_JetPilot_parameters: String = "",
)

class VoiceConfigs {

    companion object {

        val dfki_spike_hsmm_en_GB_male_hmm = MarryTtsSpeechConfig(
            voice = "dfki-spike-hsmm",
            voice_Selections = "dfki-spike-hsmm en_GB male hmm",
            locale = "en_GB",
        )

        val dfki_spike_en_GB_male_unitselection_general = MarryTtsSpeechConfig(
            voice = "dfki-spike",
            voice_Selections = "dfki-spike en_GB male unitselection general",
            locale = "en_GB",
            effect_Volume_parameters = "amount:3.0;",
        )

        val cmu_bdl_hsmm_en_US_male_hmm = MarryTtsSpeechConfig(
            voice = "cmu-bdl-hsmm",
            voice_Selections = "cmu-bdl-hsmm en_US male hmm",
            locale = "en_US",
        )

        val dfki_spike_en_GB_male_unitselection = MarryTtsSpeechConfig(
            voice = "dfki-spike",
            voice_Selections = "dfki-spike en_GB male unitselection",
            locale = "en_us",
            effect_Volume_parameters = "amount:3.0;",
        )

        val dfki_prudence_en_GB_female_unitselection_general = MarryTtsSpeechConfig(
            voice = "dfki-prudence",
            voice_Selections = "dfki-prudence en_GB female unitselection general",
            locale = "en_GB",
        )

        val cmu_slt_hsmm_en_US_female_hmm = MarryTtsSpeechConfig(
            voice = "cmu-slt-hsmm",
            voice_Selections = "cmu-slt-hsmm en_US female hmm",
            locale = "en_US",
        )

        val dfki_obadiah_hsmm_en_GB_male_hmm = MarryTtsSpeechConfig(
            voice = "dfki-obadiah-hsmm",
            voice_Selections = "dfki-obadiah-hsmm en_GB male hmm",
            locale = "en_GB",
        )

        val dfki_obadiah_en_GB_male_unitselection_general = MarryTtsSpeechConfig(
            voice = "dfki-obadiah",
            voice_Selections = "dfki-obadiah en_GB male unitselection general",
            locale = "en_GB",
        )
    }
}

class MarryTtsSpeechSynthesizer (val config: MarryTtsSpeechConfig) : SpeechSynthesizer {

    override fun speak(text: String) {

        fun encode(s: String) = URLEncoder.encode(s, Charsets.UTF_8)

        val url = "http://localhost:${config.port}/process?" +
                "&INPUT_TEXT=${encode(text)}" +
                "&VOICE_SELECTIONS=${encode(config.voice_Selections)}" +
                "&LOCALE=${encode(config.locale)}" +
                "&VOICE=${encode(config.voice)}" +
                "&INPUT_TYPE=TEXT" +
                "&OUTPUT_TYPE=AUDIO" +
                "&OUTPUT_TEXT=" +
                "&AUDIO_OUT=WAVE_FILE" +
                "&AUDIO=WAVE_FILE" +
                //------------------------------------------------------------------------------------
                "&effect_Volume_selected=" +
                "&effect_Volume_parameters=${encode(config.effect_Volume_parameters)}" +
                "&effect_TractScaler_selected=${encode(config.effect_TractScaler_selected)}" +
                "&effect_TractScaler_parameters=${encode(config.effect_TractScaler_parameters)}" +
                "&effect_F0Scale_selected=${encode(config.effect_F0Scale_selected)}" +
                "&effect_F0Scale_parameters=${encode(config.effect_F0Scale_parameters)}" +
                "&effect_F0Add_selected=${encode(config.effect_F0Add_selected)}" +
                "&effect_F0Add_parameters=${encode(config.effect_F0Add_parameters)}" +
                "&effect_Rate_selected=${encode(config.effect_Rate_selected)}" +
                "&effect_Rate_parameters=${encode(config.effect_Rate_parameters)}" +
                "&effect_Robot_selected=${encode(config.effect_Robot_selected)}" +
                "&effect_Robot_parameters=${encode(config.effect_Robot_parameters)}" +
                "&effect_Whisper_selected=${encode(config.effect_Whisper_selected)}" +
                "&effect_Whisper_parameters=${encode(config.effect_Whisper_parameters)}" +
                "&effect_Stadium_selected=${encode(config.effect_Stadium_selected)}" +
                "&effect_Stadium_parameters=${encode(config.effect_Stadium_parameters)}" +
                "&effect_Chorus_selected=${encode(config.effect_Chorus_selected)}" +
                "&effect_Chorus_parameters=${encode(config.effect_Chorus_parameters)}" +
                "&effect_FIRFilter_selected=${encode(config.effect_FIRFilter_selected)}" +
                "&effect_FIRFilter_parameters=${encode(config.effect_FIRFilter_parameters)}" +
                "&effect_JetPilot_selected=${encode(config.effect_JetPilot_selected)}" +
                "&effect_JetPilot_parameters=${encode(config.effect_JetPilot_parameters)}"

        /*
        val url3 = "http://localhost:59125/process?INPUT_TYPE=TEXT&OUTPUT_TYPE=AUDIO&INPUT_TEXT=Welcome%20my%20sweetie!&OUTPUT_TEXT=&effect_Volume_selected=on&effect_Volume_parameters=amount%3A1.0%3B&effect_Volume_default=Default&effect_Volume_help=Help&effect_TractScaler_selected=&effect_TractScaler_parameters=amount%3A1.5%3B&effect_TractScaler_default=Default&effect_TractScaler_help=Help&effect_F0Scale_selected=&effect_F0Scale_parameters=f0Scale%3A2.0%3B&effect_F0Scale_default=Default&effect_F0Scale_help=Help&effect_F0Add_selected=&effect_F0Add_parameters=f0Add%3A50.0%3B&effect_F0Add_default=Default&effect_F0Add_help=Help&effect_Rate_selected=&effect_Rate_parameters=durScale%3A1.5%3B&effect_Rate_default=Default&effect_Rate_help=Help&effect_Robot_selected=&effect_Robot_parameters=amount%3A100.0%3B&effect_Robot_default=Default&effect_Robot_help=Help&effect_Whisper_selected=&effect_Whisper_parameters=amount%3A100.0%3B&effect_Whisper_default=Default&effect_Whisper_help=Help&effect_Stadium_selected=&effect_Stadium_parameters=amount%3A100.0&effect_Stadium_default=Default&effect_Stadium_help=Help&effect_Chorus_selected=&effect_Chorus_parameters=delay1%3A466%3Bamp1%3A0.54%3Bdelay2%3A600%3Bamp2%3A-0.10%3Bdelay3%3A250%3Bamp3%3A0.30&effect_Chorus_default=Default&effect_Chorus_help=Help&effect_FIRFilter_selected=&effect_FIRFilter_parameters=type%3A3%3Bfc1%3A500.0%3Bfc2%3A2000.0&effect_FIRFilter_default=Default&effect_FIRFilter_help=Help&effect_JetPilot_selected=&effect_JetPilot_parameters=&effect_JetPilot_default=Default&effect_JetPilot_help=Help&HELP_TEXT=&exampleTexts=&VOICE_SELECTIONS=cmu-bdl-hsmm%20en_US%20male%20hmm&AUDIO_OUT=WAVE_FILE&LOCALE=en_US&VOICE=cmu-bdl-hsmm&AUDIO=WAVE_FILE"
        val url4 = "http://localhost:59125/process?" +
                "&INPUT_TYPE=TEXT" +
                "&OUTPUT_TYPE=AUDIO" +
                "&INPUT_TEXT=Hello+and+goodbye+my+sweetie!" +
                "&OUTPUT_TEXT=" +
                "&effect_Volume_selected=on" +
                "&effect_Volume_parameters=amount%3A1.0%3B" +
                "&effect_Volume_default=Default" +
                "&effect_Volume_help=Help" +
                "&effect_TractScaler_selected=" +
                "&effect_TractScaler_parameters=amount%3A1.5%3B" +
                "&effect_TractScaler_default=Default" +
                "&effect_TractScaler_help=Help" +
                "&effect_F0Scale_selected=" +
                "&effect_F0Scale_parameters=f0Scale%3A2.0%3B" +
                "&effect_F0Scale_default=Default" +
                "&effect_F0Scale_help=Help" +
                "&effect_F0Add_selected=" +
                "&effect_F0Add_parameters=f0Add%3A50.0%3B" +
                "&effect_F0Add_default=Default&effect_F0Add_help=Help" +
                "&effect_Rate_selected=" +
                "&effect_Rate_parameters=durScale%3A1.5%3B" +
                "&effect_Rate_default=Default" +
                "&effect_Rate_help=Help" +
                "&effect_Robot_selected=" +
                "&effect_Robot_parameters=amount%3A100.0%3B" +
                "&effect_Robot_default=Default" +
                "&effect_Robot_help=Help" +
                "&effect_Whisper_selected=" +
                "&effect_Whisper_parameters=amount%3A100.0%3B" +
                "&effect_Whisper_default=Default" +
                "&effect_Whisper_help=Help" +
                "&effect_Stadium_selected=" +
                "&effect_Stadium_parameters=amount%3A100.0" +
                "&effect_Stadium_default=Default" +
                "&effect_Stadium_help=Help" +
                "&effect_Chorus_selected=" +
                "&effect_Chorus_parameters=delay1%3A466%3Bamp1%3A0.54%3Bdelay2%3A600%3Bamp2%3A-0.10%3Bdelay3%3A250%3Bamp3%3A0.30" +
                "&effect_Chorus_default=Default&effect_Chorus_help=Help" +
                "&effect_FIRFilter_selected=" +
                "&effect_FIRFilter_parameters=type%3A3%3Bfc1%3A500.0%3Bfc2%3A2000.0" +
                "&effect_FIRFilter_default=Default&effect_FIRFilter_help=Help" +
                "&effect_JetPilot_selected=&effect_JetPilot_parameters=" +
                "&effect_JetPilot_default=Default" +
                "&effect_JetPilot_help=Help" +
                "&HELP_TEXT=" +
                "&exampleTexts=" +
                "&VOICE_SELECTIONS=cmu-bdl-hsmm+en_US+male+hmm" +
                "&AUDIO_OUT=WAVE_FILE" +
                "&LOCALE=en_US" +
                "&VOICE=cmu-bdl-hsmm" +
                "&AUDIO=WAVE_FILE"
        */

        val soundBytes = URL(url).openStream().readAllBytes()

        val audioPlayer = JavaSoundPlayer(PlayingMode.Sync)
        audioPlayer.play(AudioSource(soundBytes))
    }
}


/*
class MarryTtsSpeechSynthesizer : SpeechSynthesizer {

    override fun speak(text: String) {

        fun encode(s: String) = URLEncoder.encode(s, UTF_8)


        "http://localhost:59125/process?INPUT_TYPE=TEXT&OUTPUT_TYPE=AUDIO&OUTPUT_TEXT=" +
                "&effect_Volume_selected=" +
                //"&effect_Volume_parameters=amount%3A2.0%3B" +
                "&effect_Volume_parameters=${encode("amount:2.0;")}" +
                "&effect_Volume_default=Default" +
                "&effect_Volume_help=Help" +
                "&effect_TractScaler_selected=" +
                //"&effect_TractScaler_parameters=amount%3A1.5%3B" +
                "&effect_TractScaler_parameters=${encode("amount:1.5;")}" +
                "&effect_TractScaler_default=Default" +
                "&effect_TractScaler_help=Help" +
                "&effect_F0Scale_selected=" +
                //"&effect_F0Scale_parameters=f0Scale%3A2.0%3B" +
                "&effect_F0Scale_parameters=${encode("f0Scale:2.0;")}" +
                "&effect_F0Scale_default=Default" +
                "&effect_F0Scale_help=Help" +
                "&effect_F0Add_selected=" +
                //"&effect_F0Add_parameters=f0Add%3A50.0%3B" +
                "&effect_F0Add_parameters=${encode("f0Add:50.0;")}" +
                "&effect_F0Add_default=Default" +
                "&effect_F0Add_help=Help" +
                "&effect_Rate_selected=" +
                //"&effect_Rate_parameters=durScale%3A1.5%3B" +
                "&effect_Rate_parameters=${encode("durScale:1.5;")}" +
                "&effect_Rate_default=Default" +
                "&effect_Rate_help=Help" +
                "&effect_Robot_selected=" +
                //"&effect_Robot_parameters=amount%3A100.0%3B" +
                "&effect_Robot_parameters=${encode("amount:100.0;")}" +
                "&effect_Robot_default=Default" +
                "&effect_Robot_help=Help" +
                "&effect_Whisper_selected=" +
                //"&effect_Whisper_parameters=amount%3A100.0%3B" +
                "&effect_Whisper_parameters=${encode("amount:100.0;")}" +
                "&effect_Whisper_default=Default" +
                "&effect_Whisper_help=Help" +
                "&effect_Stadium_selected=" +
                //"&effect_Stadium_parameters=amount%3A100.0" +
                "&effect_Stadium_parameters=${encode("amount:100.0;")}" +
                "&effect_Stadium_default=Default" +
                "&effect_Stadium_help=Help" +
                "&effect_Chorus_selected=" +
                //"&effect_Chorus_parameters=delay1%3A466%3Bamp1%3A0.54%3Bdelay2%3A600%3Bamp2%3A-0.10%3Bdelay3%3A250%3Bamp3%3A0.30" +
                "&effect_Chorus_parameters=${encode("delay1:466;amp1:0.54;delay2:600;amp2:-0.10;delay3:250;amp3:0.30")}" +
                "&effect_Chorus_default=Default" +
                "&effect_Chorus_help=Help" +
                "&effect_FIRFilter_selected=" +
                //"&effect_FIRFilter_parameters=type%3A3%3Bfc1%3A500.0%3Bfc2%3A2000.0" +
                "&effect_FIRFilter_parameters=${encode("type:3;fc1:500.0;fc2:2000.0")}" +
                "&effect_FIRFilter_default=Default" +
                "&effect_FIRFilter_help=Help" +
                "&effect_JetPilot_selected=" +
                "&effect_JetPilot_parameters=" +
                "&effect_JetPilot_default=Default" +
                "&effect_JetPilot_help=Help" +
                "&HELP_TEXT=" +
                "&exampleTexts=" +
                //"&VOICE_SELECTIONS=cmu-slt-hsmm%20en_US%20female%20hmm" +
                "&VOICE_SELECTIONS=${encode("cmu-slt-hsmm en_US female hmm")}" +
                "&AUDIO_OUT=WAVE_FILE" +
                "&LOCALE=en_US" +
                "&VOICE=${encode("cmu-slt-hsmm")}" +
                "&AUDIO=WAVE_FILE" +
                "&INPUT_TEXT=${URLEncoder.encode(text, UTF_8)}"

        T O D O("Not yet implemented")
    }
}
*/



