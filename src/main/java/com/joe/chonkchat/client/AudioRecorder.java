package com.joe.chonkchat.client;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Joseph Adamson
 * 
 * Allows user to record sound messages that can be sent ot other clients.
 */
public class AudioRecorder extends AudioUtil {

    /**
     * Record mic input and format to .wav audio before sending the data via client 
     * connection to the server.
     * 
     * @param client connection to the chat server.
     */
    public static void record(Client client) {
        try {
            final AudioFormat audioFormat = getAudioFormat();
            
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
            // check is this line is supported by the hardware 
            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("line not supported");
            }

            final TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
            line.open();
            line.start();
            
            Runnable recorder = new Runnable() {

                byte[] buffer = new byte[1024];
                ByteArrayOutputStream output = new ByteArrayOutputStream();

                @Override
                public void run() {

                    System.out.println("Starting recording....");
                    try {
                        while (isRecording) {
                            int availableBytes = line.read(buffer, 0, buffer.length);
                            if (availableBytes >= 0) {
                                output.write(buffer, 0, availableBytes);
                            }
                        }
                        System.out.println("Recording stopped");
                        output.flush();
                        output.close();
                        line.flush();
                        line.close();

                        // kick recording along
                        client.sendAudio(output.toByteArray());

                    } catch (IOException e) {
                        System.err.println("IO error");
                    }
                }
            };
            Thread captureThread = new Thread(recorder);
            captureThread.start();

        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
