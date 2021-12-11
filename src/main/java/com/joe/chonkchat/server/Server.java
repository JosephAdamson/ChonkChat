package com.joe.chonkchat.server;

import com.joe.chonkchat.data.Message;
import com.joe.chonkchat.data.MessageType;
import com.joe.chonkchat.data.Status;
import com.joe.chonkchat.data.User;
import com.joe.chonkchat.util.ResourceHandler;
import javafx.application.Platform;
import javafx.fxml.FXML;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ChonkChat server.
 * 
 * @author Joseph Adamson
 */
public class Server {

    public final static int PORT = 49200;
    private final String serverID = "[server @ port " + PORT + "]:";
    private ServerSocket serverSocket;
    private boolean serverActive = false;
    TerminalController serverController;
    
    private final HashMap<String, ObjectOutputStream> activeConnections = new HashMap<>();
    private final HashMap<String, User> activeUserRoster = new HashMap<>();
    
    public Server(TerminalController serverController) {
        this.serverController = serverController;
    }
    
    /**
     * Creates main server thread which in turn generates client connection threads.
     */
    @FXML
    public void startSever() {
        
        serverActive = true;
        
        // Server runs on its own thread away from the main UI (terminal) thread.
        new Thread(new Runnable() {
            @Override
            public void run() {
                
                // For now keep at 50 clients max
                ExecutorService clientThreads = Executors.newFixedThreadPool(50);
                
                try {
                    serverSocket = new ServerSocket(PORT);
                    
                    Platform.runLater(
                            () -> serverController.addTerminalMessage(serverID + " now online!")
                    );
                    
                    // listen for incoming connections and start new thread for each connection
                    while (!serverSocket.isClosed()) {
                        
                        Socket clientConnection = serverSocket.accept();

                        ClientHandler clientHandler = new ClientHandler(clientConnection);
                        clientThreads.execute(clientHandler);
                        
                        Platform.runLater(
                                () -> serverController
                                        .addTerminalMessage(serverID + " a new client has connected.")
                        );
                    }

                } catch (SocketException e) {
                    clientThreads.shutdownNow();

                } catch (IOException e) {
                    System.err.println("Server connection couldn't be established.");
                    clientThreads.shutdownNow();
                    shutdownServer();
                }

            }
        }).start();
    }

    /**
     * Close server socket to force shutdown. Communicate shutdown to active clients.
     */
    public void shutdownServer() {
        
        serverActive = false;
        
        Platform.runLater(
                () -> serverController.addTerminalMessage(serverID + " shutting down...")
        );
        
        try {

            // Issue shutdown command to safely stop handler and client.
            Message shutdownCmd = new Message();
            shutdownCmd.setMessageType(MessageType.SHUTDOWN);
            
            for (Map.Entry<String, ObjectOutputStream> entry : activeConnections.entrySet()) {
                entry.getValue().writeObject(shutdownCmd);
            }
            
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks active clients against parameter user.
     * 
     * @param user: prospective connection
     * @return true if user with same username already exists, false otherwise.
     */
    public boolean duplicateUsername(User user) {
        return activeConnections.containsKey(user.getUsername());
    }

    /**
     * @return active clients on the server.
     */
    public HashMap<String, ObjectOutputStream> getActiveConnections() {
        return activeConnections;
    }

    /**
     * Manages client connection to the server.
     */
    public class ClientHandler implements Runnable {

        private final Socket socket;
        private ObjectInputStream input;
        private ObjectOutputStream output;
        private String username;
        
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        /**
         * Listens for incoming messages from corresponding client connection.
         * Executed in a separate thread.
         */
        @Override
        public void run() {

            try {
                output = new ObjectOutputStream(socket.getOutputStream());
                input = new ObjectInputStream(socket.getInputStream());

                while (socket.isConnected() && serverActive) {
                    
                    Message msg = (Message) input.readObject();
                    
                    switch (msg.getMessageType()) {
                        
                        case TEXT:
                            String text = msg.getTextData();
                            
                            // For now we'll keep track of conversations.
                            Platform.runLater(
                                    () -> serverController.addTerminalMessage(
                                            msg.getSender().getUsername() + ": " + text
                                    )
                            );
                            broadcastMessage(msg);
                            break;
                            
                        case FILE:
                            String file = (msg.getFile().getName() + msg.getFile().getExtension());
                            Platform.runLater(
                                    () -> serverController.addTerminalMessage(
                                            msg.getSender().getUsername() + " sent file: " + file
                                    )
                            );
                            broadcastMessage(msg);
                            break;
                            
                        case CONNECTED:
                            Platform.runLater(
                                    () -> serverController.addTerminalMessage((msg.getTextData()))
                            );
                            addToActiveClients(msg);
                            break;
                            
                        case DISCONNECTED:
                            removeClient();
                            msg.setMessageType(MessageType.STATUS_UPDATE);
                            msg.setActiveUsers(new ArrayList<>(activeUserRoster.values()));
                            broadcastMessage(msg);
                            ResourceHandler.closeResources(socket, input, output);
                            break;

                        case STATUS_UPDATE:
                            Platform.runLater(
                                    () -> serverController.addTerminalMessage(
                                            msg.getSender().getUsername() + " has updated their status.")
                            );
                            System.out.println(msg.getSender());
                            updateActiveUserStatus(msg.getSender(), msg.getStatusUpdate());
                            broadcastMessage(msg);
                            break;
                            
                        case ERROR:
                            handleException(msg.getSender().getUsername(),
                                    msg.getTimeSent(), msg.getException());
                            break;
                            
                        default:
                            System.out.println("Whoa there...");
                            
                    }
                }
                ResourceHandler.closeResources(socket, input, output);
                
            } catch (IOException | ClassNotFoundException e) {
                removeClient();
                ResourceHandler.closeResources(socket, input, output);
            }
        }

        /**
         * Ping new client with a welcome message to confirm connection.
         */
        public void addToActiveClients(Message initialMsg) {

            this.username = initialMsg.getSender().getUsername();
            
            // add output stream active clients, so it can be written to later.
            activeConnections.put(username, output);
            activeUserRoster.put(username, initialMsg.getSender());

            initialMsg.setActiveUsers(new ArrayList<>(activeUserRoster.values()));

            broadcastMessage(initialMsg);
        }

        /**
         * Replaces user-key with an updated version.
         * 
         * @param user user-key with update status.
         */
        public void updateActiveUserStatus(User user, Status update) {
            user.setStatus(update);
            activeUserRoster.replace(user.getUsername(), user);
        }

        /**
         * Remove client form active client list.
         */
        public void removeClient() {
            activeConnections.remove(username);
            activeUserRoster.remove(username);
            Platform.runLater(
                    () -> serverController.addTerminalMessage(username + 
                            " has left the chat.")
            );
        }

        /**
         * Send Message packet to all active users in the chat.
         * 
         * @param message (text file etc.)
         */
        public void broadcastMessage(Message message) {
            
            // active users at the time the message is sent.
            message.setActiveUsers(new ArrayList<>(activeUserRoster.values()));
            
            try {
                for (Map.Entry<String, ObjectOutputStream> activeClient : activeConnections.entrySet()) {
                    activeClient.getValue().writeObject(message);
                    activeClient.getValue().flush();
                }
                
            } catch (IOException e) {
                e.printStackTrace();
                ResourceHandler.closeResources(socket, input, output);
            }
        }

        /**
         * Print stacktrace caused by client exception to terminal GUI
         * 
         * @param sender: source of exception
         * @param timeSent: time of exception's occurrence
         * @param exception: exception
         */
        public void handleException(String sender, String timeSent, Exception exception) {

            try {
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                
                exception.printStackTrace(printWriter);

                String stacktrace = "---Exception---\n" +
                        timeSent + "\n" +
                        sender + "\n" +
                        stringWriter.toString();
                
                stringWriter.close();
                printWriter.close();
                
                Platform.runLater(
                        () -> serverController.addTerminalMessage(stacktrace)
                );
            
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
    }
    
    }
