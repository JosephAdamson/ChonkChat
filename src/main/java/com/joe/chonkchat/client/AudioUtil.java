package com.joe.chonkchat.client;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;

/**
 * Parent class for audio recording and playback
 * 
 * @author Joseph Adamson
 */
public class AudioUtil {
    
    protected static boolean isRecording = false;
    protected static boolean isPlaying = false;
    protected static SourceDataLine audioOutputLine;

    public static boolean isRecording() {
        return isRecording;
    }

    public static void setIsRecording(boolean isRecording) {
        AudioUtil.isRecording = isRecording;
    }

    public static boolean isPlaying() {
        return isPlaying;
    }

    public static void setIsPlaying(boolean isPlaying) {
        AudioUtil.isPlaying = isPlaying;
    }

    public static SourceDataLine getAudioOutputLine() {
        return audioOutputLine;
    }

    public static void setAudioOutputLine(SourceDataLine audioOutputLine) {
        AudioUtil.audioOutputLine = audioOutputLine;
    }

    /**
     * @return audio file format (configure for .wav)
     */
    public static AudioFormat getAudioFormat() {
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                44100,
                16,
                2,
                4,
                44100,
                false
        );
    }
}
