
On Linux
 - It is recommended to start Mary TTS (the best Mary voice is better than best )
   - `docker run -it -p 59125:59125 synesthesiam/marytts:5.2 --voice cmu-slt-hsmm --voice cmu-bdl-hsmm --voice cmu-rms-hsmm --voice dfki-obadiah-hsmm --voice dfki-poppy-hsmm --voice dfki-prudence-hsmm --voice dfki-spike-hsmm`
   - Or you can download (and probably build). As for me 'docker' approach is 
 - Or install festival

On Windows
 - It is recommended to enable Windows native TTS. See instruction is README.md.
 - Or you also can use Mary TTS (directly or by docker)
