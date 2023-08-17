


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



***

Run application
 - `mvn exec:java`
