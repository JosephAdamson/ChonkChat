package com.example.chonkchat.util;

import java.io.*;
import java.net.Socket;

/**
 * Helper method(s) to manage ClientHandler/Client resources.
 * 
 * @author Joseph Adamson.
 */
public class ResourceHandler {

    /**
     * Close all outgoings for ClientHandler thread and/or client connection
     * 
     * @param socket: socket (belongs to either ClientHandler or Client)
     * @param objIn: input stream
     * @param objOut: output stream
     */
    public static synchronized void closeResources(Socket socket, ObjectInputStream objIn,
                                      ObjectOutputStream objOut) {
        try {
            if (socket != null) {
                socket.close();
            }
            
            if (objIn != null) {
                objIn.close();
            }
            
            if (objOut != null) {
                objOut.close();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
