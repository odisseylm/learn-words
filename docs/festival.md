

Links:
 - https://wiki.archlinux.org/title/Festival
 - http://festvox.org/festival/index.html
 - https://github.com/festvox/festival/
 - http://festvox.org/docs/manual-2.4.0/



Commands:
 - exit: (quit)
 - available voices: (voice.list)
 - select voice: (voice_XXX) , for example (voice_us2_mbrola)
 - Say: (SayText "Hello from Ubuntu")
 - (tts "story.txt" nil)
 - Say intro text using current voice: (intro)
 - `echo "This is a test." | text2wave -o output.wav`



Voice files
- from http://festvox.org/packed/festival/%LATEST_VERSION%/voices/
    - For 2.5 http://festvox.org/packed/festival/2.5/voices/
        - festvox_cmu_us_aew_cg.tar.gz
        - festvox_cmu_us_ahw_cg.tar.gz
        - festvox_cmu_us_aup_cg.tar.gz
        - festvox_cmu_us_awb_cg.tar.gz
        - festvox_cmu_us_axb_cg.tar.gz
        - festvox_cmu_us_bdl_cg.tar.gz
        - festvox_cmu_us_clb_cg.tar.gz
        - festvox_cmu_us_eey_cg.tar.gz
        - festvox_cmu_us_fem_cg.tar.gz
        - festvox_cmu_us_gka_cg.tar.gz
        - festvox_cmu_us_jmk_cg.tar.gz
        - festvox_cmu_us_ksp_cg.tar.gz
        - festvox_cmu_us_ljm_cg.tar.gz
        - festvox_cmu_us_lnh_cg.tar.gz
        - festvox_cmu_us_rms_cg.tar.gz
        - festvox_cmu_us_rxr_cg.tar.gz
        - festvox_cmu_us_slp_cg.tar.gz
        - festvox_cmu_us_slt_cg.tar.gz



Good voices:
 - cmu_us_eey_cg
 - cmu_us_jmk_cg (a bit slow by default)
 - cmu_us_aew_cg (a bit slow by default)
 - cmu_us_awb_cg (a bit slow by default)
 - cmu_us_gka_cg (a bit slow by default)
 - cmu_us_lnh_cg (a bit slow by default)
 - cmu_us_aup_cg
 - cmu_us_ljm_cg ++
 - cmu_us_ahw_cg ++ (a bit slow by default)
 - cmu_us_clb_cg ++
 - cmu_us_bdl_cg
 - cmu_us_rxr_cg
 - cmu_us_slt_cg
 - mbrola_us1
 - mbrola_us2
 - mbrola_us3

Broken voices:
 - cmu_us_rms_cg - (is broken, hangs up)
 - all 'arctic' voices (probably because they were designed for 2.0.1 but current festival version is 2.5)

So-so voices:
 - cmu_us_slp_cg (so-so with accent)
 - cmu_us_ksp_cg (with accent)
 - cmu_us_fem_cg (with accent)
 - cmu_us_axb_cg (with accent)



Set enhanced voices
 - https://ubuntuforums.org/showthread.php?t=751169
 - http://teknologisuara.blogspot.com/2011/11/howto-make-festival-tts-use-better.html
 - https://ubuntuforums.org/archive/index.php/t-751169.html



Install mbrola voices (seems something wrong with 'en1')

 `unpack files to current (temp) dir`

 `unzip -x en1-980910.zip`
 `unzip -x us1-980512.zip`
 `unzip -x us2-980812.zip`
 `unzip -x us3-990208.zip`

 `tar xvf festvox_en1.tar.gz`
 `tar xvf festvox_us1.tar.gz`
 `tar xvf festvox_us2.tar.gz`
 `tar xvf festvox_us3.tar.gz`

 `sudo mkdir -p /usr/share/festival/voices/english/en1_mbrola/`
 `sudo mkdir -p /usr/share/festival/voices/english/us1_mbrola/`
 `sudo mkdir -p /usr/share/festival/voices/english/us2_mbrola/`
 `sudo mkdir -p /usr/share/festival/voices/english/us3_mbrola/`

 `sudo cp en1 /usr/share/festival/voices/english/en1_mbrola/ -r`
 `sudo cp us1 /usr/share/festival/voices/english/us1_mbrola/ -r`
 `sudo cp us2 /usr/share/festival/voices/english/us2_mbrola/ -r`
 `sudo cp us3 /usr/share/festival/voices/english/us3_mbrola/ -r`

 `sudo cp festival/lib/voices/english/en1_mbrola/* /usr/share/festival/voices/english/en1_mbrola/ -r`
 `sudo cp festival/lib/voices/english/us1_mbrola/* /usr/share/festival/voices/english/us1_mbrola/ -r`
 `sudo cp festival/lib/voices/english/us2_mbrola/* /usr/share/festival/voices/english/us2_mbrola/ -r`
 `sudo cp festival/lib/voices/english/us3_mbrola/* /usr/share/festival/voices/english/us3_mbrola/ -r`



Other links:
 - Voices: /usr/share/festival/voices/
   - http://festvox.org/docs/manual-2.4.0/festival_24.html#Current-voices
   - https://archlinux.org/packages/extra/any/festival-us/
   - https://archlinux.org/packages/extra/any/festival-english/ (default)
   - http://festvox.org/festival/downloads.html
   - !oudated! https://ubuntuforums.org/showthread.php?t=751169
   - ? !oudated! http://teknologisuara.blogspot.com/2011/11/howto-make-festival-tts-use-better.html
   - ? +/- https://ubuntuforums.org/archive/index.php/t-751169.html
   - ? https://www.cstr.ed.ac.uk/projects/festival/mbrola.html
   - https://github.com/pettarin/setup-festival-mbrola/blob/master/patches/festival/lib/voices/english/us1_mbrola/festvox/us1_mbrola.scm
   - + https://www.cstr.ed.ac.uk/downloads/festival/1.95/?C=N;O=D
   - https://github.com/pettarin/setup-festival-mbrola/blob/master/setup_festival_mbrola.sh
   - Mbrola
     - Files:
       - http://kobyla.org/soft/distfiles/mbrolavox/
         - en1-980910.zip us1-980512.zip us2-980812.zip us3-990208.zip
       - ? us2-980812.zip https://www.os2site.com/sw/mmedia/speech/index.html
         - https://www.cstr.ed.ac.uk/downloads/festival/1.95/
           - festvox_en1.tar.gz festvox_us1.tar.gz festvox_us2.tar.gz festvox_us3.tar.gz
         - http://www.sisyphus.ru/ru/srpm/Sisyphus/
           - en1-980910.zip http://www.sisyphus.ru/ru/srpm/Sisyphus/mbrola-voice-en1/sources
           - us1-980512.zip http://www.sisyphus.ru/ru/srpm/Sisyphus/mbrola-voice-us1/sources
           - us2-980812.zip http://www.sisyphus.ru/ru/srpm/Sisyphus/mbrola-voice-us2/sources
           - us3-990208.zip http://www.sisyphus.ru/ru/srpm/Sisyphus/mbrola-voice-us3/sources
         - http://www.sisyphus.ru/ru/srpm/Sisyphus/mbrola-voice-us1/sources/0
           - us2-980812.zip https://www.os2site.com/sw/mmedia/speech/index.html 
   - Arctic voices
     - Seems they are outdated and do not work any more (I have only 'arctic' voices of 2.01, and they do not work with festival 2.5.0 )
     - https://aur.archlinux.org/packages/festival-hts-voices-patched
       - festvox_nitech_us_awb_arctic_hts-2.1.tar.bz2
       - festvox_nitech_us_bdl_arctic_hts-2.1.tar.bz2
       - festvox_nitech_us_clb_arctic_hts-2.1.tar.bz2
       - festvox_nitech_us_jmk_arctic_hts-2.1.tar.bz2
       - festvox_nitech_us_rms_arctic_hts-2.1.tar.bz2
       - festvox_nitech_us_slt_arctic_hts-2.1.tar.bz2
     - https://forums.linuxmint.com/viewtopic.php?t=21902 (or https://ubuntuforums.org/archive/index.php/t-677277.html)
       - http://hts.sp.nitech.ac.jp/archives/2.0.1/festvox_nitech_us_awb_arctic_hts-2.0.1.tar.bz2
       - http://hts.sp.nitech.ac.jp/archives/2.0.1/festvox_nitech_us_bdl_arctic_hts-2.0.1.tar.bz2
       - http://hts.sp.nitech.ac.jp/archives/2.0.1/festvox_nitech_us_clb_arctic_hts-2.0.1.tar.bz2
       - http://hts.sp.nitech.ac.jp/archives/2.0.1/festvox_nitech_us_rms_arctic_hts-2.0.1.tar.bz2
       - http://hts.sp.nitech.ac.jp/archives/2.0.1/festvox_nitech_us_slt_arctic_hts-2.0.1.tar.bz2
       - http://hts.sp.nitech.ac.jp/archives/2.0.1/festvox_nitech_us_jmk_arctic_hts-2.0.1.tar.bz2
     - Probably more outdated (I think) https://src.fedoraproject.org/repo/pkgs/festival/
         - See 'arctic' sub-directories
             - https://src.fedoraproject.org/repo/pkgs/festival/festvox_nitech_us_awb_arctic_hts.tar.bz2/
             - https://src.fedoraproject.org/repo/pkgs/festival/festvox_nitech_us_bdl_arctic_hts.tar.bz2/
             - https://src.fedoraproject.org/repo/pkgs/festival/festvox_nitech_us_clb_arctic_hts.tar.bz2/
             - https://src.fedoraproject.org/repo/pkgs/festival/festvox_nitech_us_jmk_arctic_hts.tar.bz2/
             - https://src.fedoraproject.org/repo/pkgs/festival/festvox_nitech_us_rms_arctic_hts.tar.bz2/
             - https://src.fedoraproject.org/repo/pkgs/festival/festvox_nitech_us_slt_arctic_hts.tar.bz2/
