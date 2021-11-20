package com.joe.chonkchat.server;

import com.joe.chonkchat.util.CustomWindowBaseController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.*;
import java.util.ResourceBundle;

/**
 * Controller for the chonk-terminal which is used to set up
 * and monitor the server.
 * 
 * @author Joseph Adamson
 */
public class TerminalController extends CustomWindowBaseController implements Initializable {

    private boolean serverActive = false;
    private Server server;
    
    @FXML
    public BorderPane basePane;
    
    @FXML
    public TextArea terminalInput;

    @FXML
    private ListView<HBox> terminalMsgNodes;

    /**
     * Upon boot of the terminal
     * 
     * @param url relative path for the root object.
     * @param resourceBundle resources to localize root object.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        
        server = new Server(this);
        
        try {
            InetAddress IP = InetAddress.getLocalHost();
            addTerminalMessage(IP.toString());
            addTerminalMessage("");
            
            String welcomeMsg = "Welcome to ChonkChat!\n" +
                    "If you have no idea what to do next use command 'chonk help'";
            
            addTerminalMessage(welcomeMsg);
            
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * Print message to the chonk-terminal.
     * 
     * @param message description of current activity.
     */
    public void addTerminalMessage(String message) {
        
        // create message
        Label msg = new Label(message);
        msg.setStyle("-fx-text-fill: #00FF41");
        msg.setWrapText(true);
        
        // create container
        HBox container = new HBox();
        container.getChildren().add(msg);
        container.setPrefWidth(450);
        
        // add container to listview
        terminalMsgNodes.getItems().add(container);
        int end = terminalMsgNodes.getItems().size();
        terminalMsgNodes.scrollTo(end);
    }

    /**
     * Parser for all chonk terminal commands.
     * 
     * @param cmd chonk-terminal command.
     */
    public void commandParser(String cmd) {
        
        String[] tokens = cmd.split(" ");
        if (tokens[0].equals("chonk") && tokens.length >= 2) {
            switch(tokens[1]) {
                case "help":
                    openHelpDoc();
                    break;

                case "start":
                    serverActive = true;
                    server.startSever();
                    break;

                case "stop":
                    serverActive = false;
                    server.shutdownServer();
                    break;

                case "clients":
                    getClients();
                    break;

                default:
                    addTerminalMessage("chonk: command not found: " + cmd);
                    break;
            }
        }  else {
            addTerminalMessage("chonk: command not found: " + cmd);
        }
    }

    /**
     * Print helpful commands to the chonk-terminal.
     */
    private void openHelpDoc() {
        
        try {
            // read help.log
            BufferedReader br = new BufferedReader(
                    new FileReader(new File(getClass()
                            .getResource("/com/joe/logs/help.log").getFile()))
            );
            
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            
            addTerminalMessage("");
            addTerminalMessage(sb.toString());
            addTerminalMessage("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieve list of active clients.
     */
    public void getClients() {
        
        if (serverActive) {
            
            if (!server.getActiveConnections().isEmpty()) {
                
                // loop through active clients and print usernames
                // to terminal
                addTerminalMessage("--- active clients ---");
                
                StringBuilder clientList = new StringBuilder();
                server.getActiveConnections().forEach(
                        (username, connection) -> {
                            clientList.append("\t").append(username).append("\n");
                        });
                addTerminalMessage(clientList.toString());
                
            } else {
                addTerminalMessage("[status]: no active clients");
            }
        } else {
            addTerminalMessage("[status]: server is currently offline");
        }
    }
    
    /**
     * User presses 'enter' to execute chonk-command
     *
     * @param keyEvent enter
     */
    @FXML
    public void terminalInputEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            String input = terminalInput.getText().trim();
            commandParser(input);
            terminalInput.clear();
        }
    }

    /**
     * Force close of terminal window.
     */
    public void forceWindowClose() {
        server.shutdownServer();
        Stage thisStage = (Stage) basePane.getScene().getWindow();
        thisStage.close();
    }

    public boolean isServerActive() {
        return serverActive;
    }
    
    public Server getServer() {
        return this.server;
    }
}