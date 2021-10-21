package com.joe.chonkchat.util;

import com.joe.chonkchat.data.Message;
import com.joe.chonkchat.data.MessageType;

import java.io.*;
import java.net.Socket;
import java.util.Date;

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

    /**
     * Produce stack trace that can be viewed in terminal GUI.
     * Designed to broadcast any errors to the terminal that
     * occur whilst the client is connected.
     * 
     * @param exception: error
     */
    public static void sendStackTrace(Exception exception, String username, ObjectOutputStream output) {
        
        try {

            Message msg = new Message();
            msg.setSender(username);
            msg.setTimeSent(new Date());
            msg.setException(exception);
            msg.setMessageType(MessageType.ERROR);
            
            output.writeObject(msg);
            output.flush();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
