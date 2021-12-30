package com.joe.chonkchat.client;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author Joseph Adamson
 * 
 * Handles audio playback of sound messages.
 */
public class AudioPlayback extends AudioUtil {

    private static Thread playbackThread;

    /**
     * Playback audio contained in a byte array.
     * 
     * @param message audio data
     */
    public static void playback(byte[] message) {
        
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(message);
            final AudioFormat audioFormat = getAudioFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

            long length = (long) message.length / audioFormat.getFrameSize();
            AudioInputStream audioInputStream =
                    new AudioInputStream(inputStream, audioFormat, length);

            setAudioOutputLine((SourceDataLine) AudioSystem.getLine(info));
            
            // prime mic output
            audioOutputLine.open();
            audioOutputLine.start();
            
            Runnable playback = new Runnable() {
                @Override
                public void run() {
                    byte[] buffer = new byte [1024];
                    int bytes;

                    System.out.println("Starting playback...");
                    try {
                        while ((bytes = audioInputStream.read(buffer, 0, buffer.length)) != -1) {
                            if (bytes > 0) {
                                audioOutputLine.write(buffer, 0, buffer.length);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    audioOutputLine.drain();
                    audioOutputLine.close();
                    System.out.println("Playback stopped.");
                }
            };
            playbackThread = new Thread(playback);
            playbackThread.start();
            
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stop audio playback by force-closing the audioOutputLine
     */
    public static void stopPlayback() {
        if(audioOutputLine.isActive()) {
            audioOutputLine.stop();
        }
    }

    public static Thread getPlaybackThread() {
        return playbackThread;
    }
}
