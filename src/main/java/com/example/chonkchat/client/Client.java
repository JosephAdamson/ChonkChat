package com.example.chonkchat.client;

import com.example.chonkchat.data.Message;
import com.example.chonkchat.data.MessageType;
import com.example.chonkchat.server.Server;
import com.example.chonkchat.util.ResourceHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

/**
 * ChonkChat Client.
 * 
 * @author Joseph Adamson
 */
public class Client implements Runnable{
    
    private final Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private final String username;
    
    public Client(Socket socket, String username) {
        this.socket = socket;
        this.username = username;
    }

    /**
     * Client needs to be able to listen for incoming messages 
     * on a separate thread, as while loop is a blocking operation
     * and would otherwise lock up the main client thread.
     */
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
                        System.out.println(incomingMsg.getMessage());
                        break;

                    case SERVER:
                        System.out.println("[SERVER]: " + incomingMsg.getMessage());
                        break;

                    case SHUTDOWN:
                        System.out.println("Server has shut down");
                        ResourceHandler.closeResources(socket, input, output);
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

    /**
     * NOTE: For now we'll test the client from the terminal, this
     * will have to be changed later so it can be integrated
     * will the Chat UI
     */
    /*public void sendMessage() {
        
        try {
            
            Scanner scanner = new Scanner(System.in);
            
            while (socket.isConnected()) {

                String text = scanner.nextLine();
                Message msgToSend = new Message();
                if (text.trim().equals("bye")) {
                    msgToSend.setMessageType(MessageType.DISCONNECTED);
                } else {
                    msgToSend.setMessage(text + "\n");
                    msgToSend.setMessageType(MessageType.TEXT);
                }

                // use the stream
                output.writeObject(msgToSend);
                output.flush();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

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
     * Temporary way to interface with server.
     */
    /*public static void main(String[] args) throws IOException {
        // retrieve user name
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter a username: ");
        String username = scanner.nextLine();

        // launch connection
        Socket socket = new Socket("localhost", Server.PORT);
        Client client = new Client(socket, username);

        // listen comes first as it will be on a separate thread
        client.listenForMessage();
        //client.sendMessage();
    }*/
}
