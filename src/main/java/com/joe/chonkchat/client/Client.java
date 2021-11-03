package com.joe.chonkchat.client;

import com.joe.chonkchat.data.FileTransfer;
import com.joe.chonkchat.data.Message;
import com.joe.chonkchat.data.MessageType;
import com.joe.chonkchat.data.User;
import com.joe.chonkchat.server.Server;
import com.joe.chonkchat.util.ResourceHandler;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

/**
 * ChonkChat Client.
 * 
 * @author Joseph Adamson
 *
 */
public class Client {
    
    private final Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private final User user;
    private ChatController chatController;
    
    public Client(Socket socket, User user, ChatController chatController) {
        this.socket = socket;
        this.user = user;
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

                            case FILE:
                                chatController.updateChatWindow(incomingMsg);
                                break;

                            case SERVER:
                                System.out.println("[SERVER]: " + incomingMsg.getTextData());
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
                    ResourceHandler.sendStackTrace(e, user, output);
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
            message.setSender(user);
            message.setTextData(text);
            message.setMessageType(MessageType.TEXT);
            message.setTimeSent(new Date());
            
            output.writeObject(message);
            output.flush();
            
        } catch (IOException e) {
            ResourceHandler.sendStackTrace(e, user, output);
            ResourceHandler.closeResources(socket, input, output);
        }
    }

    /**
     * Send a file (document or image) over server connection to other
     * clients.
     * 
     * @param file data to be sent to other users.
     */
    public void sendFile(File file) {
        
        try {
            byte[] content = Files.readAllBytes(Paths.get(file.getAbsolutePath()));

            String fileString = file.getName();
            int extIndex = fileString.lastIndexOf(".");
            String filename = fileString.substring(0, extIndex);
            String extension = fileString.substring(extIndex);


            FileTransfer fileTransfer = new FileTransfer();
            fileTransfer.setContent(content);
            fileTransfer.setName(filename);
            fileTransfer.setExtension(extension);

            Message message = new Message();
            message.setSender(user);
            message.setMessageType(MessageType.FILE);
            message.setTimeSent(new Date());
            message.setFile(fileTransfer);
            
            output.writeObject(message);
            output.flush();
            
        } catch (IOException e) {
            ResourceHandler.sendStackTrace(e, user, output);
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
            connectMsg.setSender(user);
            connectMsg.setTextData(user.getUsername() + " has entered the chat.");
            connectMsg.setMessageType(MessageType.CONNECTED);
            
            output.writeObject(connectMsg);
            output.flush();
            
        } catch (IOException e) {
            ResourceHandler.sendStackTrace(e, user, output);
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
            ResourceHandler.sendStackTrace(e, user, output);
        }
    }
}
