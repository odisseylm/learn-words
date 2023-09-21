
 - Mbrola
   - MBROLA binaries and voice databases can be downloaded free for non-commercial, non-military
   - https://github.com/numediart/MBROLA
   - https://github.com/numediart/MBROLA-voices/
   - https://espeak.sourceforge.net/mbrola.html
   - https://chromium.googlesource.com/chromiumos/third_party/espeak-ng/+/HEAD/docs/mbrola.md
   - https://wiki.archlinux.org/title/Mbrola
   - https://espeak.sourceforge.net/mbrola.html
   - for eSpeak https://robots.uc3m.es/installation-guides/install-espeak-mbrola.html
   - Install
     - Binary
       - on Ubuntu (https://robots.uc3m.es/installation-guides/install-espeak-mbrola.html)
         - `sudo apt install mbrola`
         - compile/build from sources
           - latest release sources https://github.com/numediart/MBROLA/releases
     - Voices
       - on Ubuntu (https://robots.uc3m.es/installation-guides/install-espeak-mbrola.html)
         - `sudo apt install mbrola-en1`
         - `sudo apt install mbrola-us1`
         - `sudo apt install mbrola-us2`
         - `sudo apt install mbrola-us3`
         - `sudo apt install mbrola-de{1..7}`
         - `sudo apt install mbrola-xxx` etc
       - github
         - https://github.com/numediart/MBROLA-voices/
           - https://github.com/numediart/MBROLA-voices/tree/master/data
             - https://github.com/numediart/MBROLA-voices/tree/master/data/en1
             - https://github.com/numediart/MBROLA-voices/tree/master/data/us1
             - https://github.com/numediart/MBROLA-voices/tree/master/data/us2
             - https://github.com/numediart/MBROLA-voices/tree/master/data/us3
         - Arch linux (you can as separate files) (really links to git :-) )
           - https://aur.archlinux.org/packages?K=mbrola-voices
     - http://kobyla.org/soft/distfiles/mbrolavox/ (!!! ancient versions !!!)
       - Files (probably very old): en1-980910.zip us1-980512.zip us2-980812.zip us3-990208.zip


***

Building mbrola on Windows

Building with new Visual Studio generates non-working exe file (it executes properly but generates incorrect data).

Use Cygwin or MinGW/msys64 with proper configuration

Installing required packages on cygwin
- Use UI, relaunch `cygwin-setup-x86_64.exe` again

 - Run cygwin terminal `C:\cygwin64\bin\mintty.exe -i /Cygwin-Terminal.ico -` (or choose in Windows/Start menu) 
 - `cd cygdrive/c/Users/Volod/Projects/temp/MBROLA/`
 - `make`
 - copy resulting 'mbrola.exe' to your mbrola.base dir (C:\Program Files (x86)\mbrola)  
 - copy resulting 'cygwin1.dll' (from C:\cygwin64\bin\) to your mbrola.base dir (C:\Program Files (x86)\mbrola)
   - I tried to compile mbrola.exe without dependency to cygwin1.dll (or msys-2.0.dll) but such exe files worked improperly.



- https://www.mingw-w64.org/
- https://www.msys2.org/
- Go to project dir `cd /c/Users/Volod/Projects/temp/MBROLA`
- Installing required packages on msys64
    - `
      cd /c/Users/Volod/Projects/temp/MBROLA
      pacman -S gcc
      pacman -S make
      #pacman -S mingw-w64-x86_64-gcc
      `
- Packages https://packages.msys2.org/updates?repo=msys
