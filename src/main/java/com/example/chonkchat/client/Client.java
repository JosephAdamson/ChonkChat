package com.example.chonkchat.client;

import com.example.chonkchat.data.Message;
import com.example.chonkchat.data.MessageType;
import com.example.chonkchat.server.Server;
import com.example.chonkchat.util.ResourceHandler;
import javafx.application.Platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;

/**
 * ChonkChat Client.
 * 
 * @author Joseph Adamson
 * 
 * TODO: methods for sending files and images.
 */
public class Client {
    
    private final Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private String username;
    private ChatController chatController;
    
    public Client(Socket socket, String username, ChatController chatController) {
        this.socket = socket;
        this.username = username;
        this.chatController = chatController;
    }

    /**
     * Client needs to be able to listen for incoming messages 
     * on a separate thread, as while loop is a blocking operation
     * and would otherwise lock up the main client thread.
     */
    public void listenForIncomingMessages() {
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    output = new ObjectOutputStream(socket.getOutputStream());
                    input = new ObjectInputStream(socket.getInputStream());

                    connectToServer();

                    while (socket.isConnected()) {

                        Message incomingMsg = (Message) input.readObject();

                        switch (incomingMsg.getMessageType()) {

                            case TEXT:
                                chatController.updateChatWindow(incomingMsg);
                                break;

                            case SERVER:
                                System.out.println("[SERVER]: " + incomingMsg.getMessage());
                                break;

                            case SHUTDOWN:
                                System.out.println("Server has shut down");
                                ResourceHandler.closeResources(socket, input, output);
                                break;
                                
                            case CONNECTED:
                                Platform.runLater(
                                        () -> chatController.refreshOnlineUserList(incomingMsg)
                                );
                                break;

                            default:
                                System.out.println("Whoa there...");
                        }
                    }

                } catch (SocketException e) {
                    System.err.println("[SERVER @ port " + Server.PORT + "]: Socket no longer available");

                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Exception in run() method for client");

                } finally {
                    ResourceHandler.closeResources(socket, input, output);
                }
            }
        }).start();
    }

    /**
     * Sends standard text message.
     * 
     * @param text string text.
     */
    public void sendMessage(String text) {
        try {
            
            Message message = new Message();
            message.setSender(username);
            message.setMessage(text);
            message.setMessageType(MessageType.TEXT);
            message.setTimeSent(new Date());
            
            output.writeObject(message);
            output.flush();
            
        } catch (IOException e) {
            e.printStackTrace();
            ResourceHandler.closeResources(socket, input, output);
        }
    }

    /**
     * Send initial communication to server so that information can
     * be checked and current session can be added to list of active clients.
     */
    public void connectToServer() {
        
        try {
            Message connectMsg = new Message();
            connectMsg.setSender(username);
            connectMsg.setMessage(username + " has entered the chat.");
            connectMsg.setMessageType(MessageType.CONNECTED);
            
            output.writeObject(connectMsg);
            output.flush();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Disconnect client.
     */
    public void disconnect() {
        
        try {
            Message disconnectMsg = new Message();
            disconnectMsg.setMessageType(MessageType.DISCONNECTED);
            
            output.writeObject(disconnectMsg);
            output.flush();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
