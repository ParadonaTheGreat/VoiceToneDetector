# VoiceToneDetector

## How to use

The user starts by clicking the start recording button. They then record the audio they want to detect and then hit stop recording. After this, the program analyzes the speech and outputs  the emotions they had.  

## Project

This project was made in Android Studio. A hugging face model was used to detect the speech (https://huggingface.co/ehcalabres/wav2vec2-lg-xlsr-en-speech-emotion-recognition), called through curl commands in Java. To record and playback, this app uses the MediaRecorder and MediaPlayer Java classes. 

