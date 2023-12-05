

Flite
 - https://github.com/festvox/flite


flite.exe -lv
Voices available: kal awb_time kal16 awb rms slt

kal -t is really kal ((

flite -voice kal -t "Hello Marina!"


Build under Windows
Use cygwin (with make, automake, wget)! Do NOT waste time om VisualStudio - files are outdated and not working!!!
SAPI is not build, but there is no sense to build so bad TTS as Windows SAPI.

sln file for SAPI almost working, only one file is missed )) and need to change config for Windows SDK and other small changes.


TODO: test all voices from http://festvox.org/flite/packed/flite-2.3/voices/

also play with it on Linux

play with downloaded voices

./bin/flite -voice C:\Users\Volod\Downloads\cmu_us_ahw.flitevox -t "Other similar platforms should just work, we have also cross compiled on a Linux machine for various ARM and MIPS processors."
./bin/flite -voice C:\Users\Volod\Downloads\cmu_us_clb.flitevox -t "Other similar platforms should just work, we have also cross compiled on a Linux machine for various ARM and MIPS processors."
./bin/flite -voice C:\Users\Volod\Downloads\cmu_us_fem.flitevox -t "Other similar platforms should just work, we have also cross compiled on a Linux machine for various ARM and MIPS processors."
+ ./bin/flite -voice C:\Users\Volod\Downloads\cmu_us_ljm.flitevox -t "Other similar platforms should just work, we have also cross compiled on a Linux machine for various ARM and MIPS processors."
./bin/flite -voice C:\Users\Volod\Downloads\cmu_us_slp.flitevox -t "Other similar platforms should just work, we have also cross compiled on a Linux machine for various ARM and MIPS processors."
./bin/flite -voice C:\Users\Volod\Downloads\cmu_us_ahw.flitevox -t "Other similar platforms should just work, we have also cross compiled on a Linux machine for various ARM and MIPS processors."



In theory flite can load voice from http but really it does not work 
./bin/flite -voice http://festvox.org/flite/packed/flite-2.1/voices/cmu_us_aew.flitevox -f doc/alice

Error load voice: http://festvox.org/flite/packed/flite-2.3/voices/cmu_us_clb.flitevox does not have expected header
Error load voice: failed to load voice from http://festvox.org/flite/packed/flite-2.3/voices/cmu_us_clb.flitevox


You can download the available voices into voices/
./bin/get_voices us_voices
