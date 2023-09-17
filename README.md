


JavaFX application to create words dictionaries by extracting words from clipboard text.
It has 'ignored words' list to ignore easy words.
It automatically saves/exports words cards/translations to MemoWorld format (simple-simple CSV format with ';' separator)



***
Currently used dictionaries (en-ru):
- Mueller
  - dicts/mueller-dict-3.1.1
    - https://sourceforge.net/projects/mueller-dict/files/dict/mueller-dict-3.1.1.tar.gz/download (unpack it)
    - https://sourceforge.net/projects/mueller-dict/files/dict/
- Slovnyk
  - dicts/slovnyk/slovnyk_en-gb_ru-ru.csv.gz
  - dicts/slovnyk/slovnyk_en-us_ru-ru.csv.gz
  - [Downlaod](http://www.slovnyk.org/fcgi-bin/dic.fcgi?hn=dwn&ul=en-us&il=&ol=en-us&iw=)
    - [Downloads](http://www.slovnyk.org/fcgi-bin/dic.fcgi?hn=dwn&ul=en-us&il=&ol=en-us&iw=)
      - [English - USA => ru-ru Russian - Russia](http://www.slovnyk.org/csv/slovnyk_en-us_ru-ru.csv.gz)
      - [English - United Kingdom => ru-ru Russian - Russia](http://www.slovnyk.org/csv/slovnyk_en-gb_ru-ru.csv.gz)
      - Others
        - [Russian - Russia => en-us English - USA](http://www.slovnyk.org/csv/slovnyk_ru-ru_en-us.csv.gz)
        - [Russian - Russia => en-gb English - United Kingdom](http://www.slovnyk.org/csv/slovnyk_ru-ru_en-gb.csv.gz)
- [DictionaryForMIDs](https://dictionarymid.sourceforge.net/) (Dictionaries packed for MID (old java platform for mobile phones))
  - !!! Remember to extract JAR files from archives !!!
  - Used now
    - dicts/DfM_OmegaWiki_Eng_3.5.9.jar
    - dicts/DfM_OmegaWiki_EngRus_3.5.9.jar
    - dicts/DictionaryForMIDs_EngRus_Mueller.jar
  - [Dictionaries for Download](https://dictionarymid.sourceforge.net/dict.html)
    - Other Bilingual Dictionaries / Russian Bilinguals
      - [DictionaryForMIDs_3.4.0_EngRus_Mueller.zip](http://prdownloads.sourceforge.net/dictionarymid/DictionaryForMIDs_3.4.0_EngRus_Mueller.zip?download)
       - It also Mueller dictionary, you can skip it if you already added mueller dictionary.
    - [Bilingual Dictionaries from OmegaWiki](https://dictionarymid.sourceforge.net/dictionaries/dictsBinlingualsOmegaWiki.html)
      - [English <> Russian](http://prdownloads.sourceforge.net/dictionarymid/DfM_OmegaWiki_EngRus_3.5.9_08.May.2014.zip?download)
 - Search other dictionaries ))




***
Other references
- [MemoWord application to learn words](https://memoword.online/)
- [Cписок словарей на dict.dvo.ru](http://wiki.dvo.ru/wiki/C%D0%BF%D0%B8%D1%81%D0%BE%D0%BA_%D1%81%D0%BB%D0%BE%D0%B2%D0%B0%D1%80%D0%B5%D0%B9_%D0%BD%D0%B0_dict.dvo.ru)
- http://www.linuxlib.ru/text/Mueller.htm
- http://www.slovnyk.org/fcgi-bin/dic.fcgi?hn=dwn&ul=en-us&il=&ol=en-us&iw=
- [Mueller dict](https://sourceforge.net/projects/mueller-dict/)
  - [Mueller dict downloads](https://sourceforge.net/projects/mueller-dict/files/)
- https://en.wikipedia.org/wiki/DICT
- [dictsBinlingualsOmegaWiki](https://dictionarymid.sourceforge.net/dictionaries/dictsBinlingualsOmegaWiki.html)
  - https://dictionarymid.sourceforge.net/dictionaries/dictsIDP.html
  - https://dictionarymid.sourceforge.net/dictionaries/dictsOtherBilinguals.html#Russian


MemoWorld format (described in [Download section](https://memowordapp.com/panel/import/index/665ebd51-66cb-43d7-9ad0-ee3f0b489710))
 - CSV or XLSX format
 - A set of cards in the table should be formed in the first two columns of the table - a word or phrase 
   in the base language in 1st column, the translation - in 2nd column.
 - Optionally, the name of the part of speech can be added to the 3rd column, a hint or transcription can be added to the 4th.
 - One set should not exceed 300 entries


***

Run application
 - `mvn exec:java`


***

Speech synthesizers
 - MaryTTS
   - https://github.com/marytts/marytts
   - http://mary.dfki.de/
   - Configuration
     - https://projectnaomi.com/plugins/ttss/marytts/
   - Forks
     - synesthesiam/marytts
       - https://hub.docker.com/r/synesthesiam/marytts
       - https://github.com/synesthesiam/docker-marytts
       - `docker pull synesthesiam/marytts`
       - `docker run -it -p 59125:59125 synesthesiam/marytts:5.2 --voice cmu-slt-hsmm`
     - MaryTTS 5.2 with unit selection and HSMM Voices
       - `docker pull andreibosco/marytts`
       - https://hub.docker.com/r/andreibosco/marytts 
       - To run: `docker run -it -p 59125:59125 synesthesiam/marytts:5.2`
       - To run: `docker run -it -p 59125:59125 synesthesiam/marytts:5.2 --voice cmu-slt-hsmm --voice cmu-bdl-hsmm --voice cmu-rms-hsmm --voice dfki-obadiah-hsmm --voice dfki-poppy-hsmm --voice dfki-prudence-hsmm --voice dfki-spike-hsmm`
       - A list of voices can be obtained with: `docker run -it synesthesiam/marytts:5.2 --voices`
     - Mary-TTS Server
       - https://hub.docker.com/r/sepia/marytts
       - `docker pull sepia/marytts`
       - `?sudo? docker run --rm --name=marytts -p 59125:80 -it sepia/marytts:latest`
 - eSpeak
   - https://espeak.sourceforge.net/
   - https://espeak.sourceforge.net/commands.html
 - ?? kaldi
   - https://github.com/kaldi-asr/kaldi
 - Mozilla TTS
   - https://github.com/mozilla/TTS
 - ?? https://github.com/therealvasanth/online-tts
 - ?? FestVox / Festival
   - http://festvox.org/festival/index.html
   - http://festvox.org/packed/festival/2.5/voices/
   - http://festvox.org/cmu_arctic/
     - http://festvox.org/cmu_arctic/dbs_bdl.html
     - http://festvox.org/cmu_arctic/dbs_slt.html
 - Coqui TTS
   - ?? Can be used in free software ??
   - https://github.com/coqui-ai/TTS
   - http://erogol.com/ddc-samples/
   - https://docs.coqui.ai/docs
   - Docker
     - docker run --rm -it -p 5002:5002 --entrypoint /bin/bash ghcr.io/coqui-ai/tts-cpu
     - python3 TTS/server/server.py --list_models #To get the list of available models
     - python3 TTS/server/server.py --model_name tts_models/en/vctk/vits # To start a server
 - Ila
   - https://sourceforge.net/projects/ila-voice-assistant/
 - FreeTTS
   - https://freetts.sourceforge.io
   - https://freetts.sourceforge.io/docs/index.php
   - https://freetts.sourceforge.io/mbrola/README.html
   - https://sourceforge.net/projects/freetts/
   - Mbrola
     - MBROLA binaries and voice databases can be downloaded free for non-commercial, non-military
     - https://github.com/numediart/MBROLA
     - https://espeak.sourceforge.net/mbrola.html
     - https://chromium.googlesource.com/chromiumos/third_party/espeak-ng/+/HEAD/docs/mbrola.md
     - https://espeak.sourceforge.net/mbrola.html
     - Install on Ubuntu (https://robots.uc3m.es/installation-guides/install-espeak-mbrola.html)
       - `sudo apt install mbrola-en1`
       - `sudo apt install mbrola-us1`
       - `sudo apt install mbrola-us2`
       - `sudo apt install mbrola-us3`
     - Arch linux (you can as separate files)
       - https://wiki.archlinux.org/title/Mbrola
       - https://aur.archlinux.org/packages?K=mbrola-voices
 - Google Speech
   - Not free
   - https://cloud.google.com/text-to-speech/docs/libraries
 - Owner avatar
 - jAdapterForNativeTTS
   - https://github.com/jonelo/jAdapterForNativeTTS


***

Links:
 - 15 Open-source Text To Speech TTS Apps and Libraries[https://medevel.com/14-os-text-to-speech/]


***

Licenses:

Some icons can have [Attribution 3.0 Unported] license(https://creativecommons.org/licenses/by/3.0/)
 - Yusuke Kamiyamane
   - https://iconbird.com/search/?q=iconset:Fugue%20Icons

Dark theme
- [JavaFX-Dark-Theme](https://github.com/antoniopelusi/JavaFX-Dark-Theme) - Creative Commons Legal Code (CC0 1.0 Universal) license
    - https://github.com/antoniopelusi/JavaFX-Dark-Theme/blob/main/style.css (with some my local fixes)
