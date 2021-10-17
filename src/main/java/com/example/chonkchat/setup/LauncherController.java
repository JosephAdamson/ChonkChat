package com.example.chonkchat.setup;

import com.example.chonkchat.client.ChatController;
import com.example.chonkchat.client.Client;
import com.example.chonkchat.server.Server;
import com.example.chonkchat.server.TerminalController;
import com.example.chonkchat.util.CustomWindowBaseController;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Objects;

public class LauncherController extends CustomWindowBaseController {
    
    private TerminalController terminalController;

    private boolean terminalOpen;

    @FXML
    public TextField usernameInput;

    @FXML
    private ToggleButton launchServerButton;
    
    @FXML
    public void launchServer() {
        
        if (!terminalOpen) {
            
            terminalOpen = true;
            
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(Objects.requireNonNull(getClass()
                        .getResource("/com/example/views/serverterminal-view.fxml")));

                Parent root = fxmlLoader.load();
                root.getStylesheets().add(String.valueOf(getClass()
                        .getResource("/com/example/styles/server.css")));

                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.getIcons().add(new Image(getClass()
                        .getResourceAsStream("/com/example/images/server.png")));
                stage.setTitle("chonkchat server terminal");
                
                stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent windowEvent) {
                        launchServerButton.setSelected(false);
                    }
                });

                // Enable manipulation of terminal window from launcher.
                terminalController = fxmlLoader.getController();
                
                stage.setResizable(false);
                stage.setScene(scene);

                stage.show();
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            
            terminalOpen = false;
            
            Platform.runLater(
                    () -> terminalController.forceWindowClose()
            );
        }
    }
    
    @FXML
    public void launchClient() {
        
        try {

            String username = usernameInput.getText();

            if (username != null && !username.isBlank()) {

                FXMLLoader fxmlLoader = new FXMLLoader(Objects.requireNonNull(getClass()
                        .getResource("/com/example/views/chat-view.fxml")));

                Parent root = fxmlLoader.load();
                root.getStylesheets().add(String.valueOf(getClass()
                        .getResource("/com/example/styles/chat.css")));

                // set up fields, inject them manually and start client listening thread
                ChatController chatController = fxmlLoader.getController();
                Socket socket = new Socket("localhost", Server.PORT);
                Client client = new Client(socket, username, chatController);
                chatController.setClient(client);
                chatController.setUsername(username);
                chatController.getClient().listenForIncomingMessages();

                Scene scene = new Scene(root);
                scene.setFill(Color.TRANSPARENT);
                Stage stage = new Stage();
                stage.getIcons().add(new Image(getClass()
                        .getResourceAsStream("/com/example/images/client.png")));

                stage.setTitle("client");
                stage.setScene(scene);
                stage.initStyle(StageStyle.TRANSPARENT);
                stage.show();
            } else {

                Alert usernameAlert = new Alert(Alert.AlertType.WARNING);
                usernameAlert.setTitle("Username Warning");
                usernameAlert.setHeaderText("Username must not be blank.");
                usernameAlert.setContentText("Please make provide a username before logging in.");
                usernameAlert.show();
            }

        } catch (ConnectException e) {
            
            Alert connectionAlert = new Alert(Alert.AlertType.WARNING);
            connectionAlert.setTitle("Connection Warning");
            connectionAlert.setHeaderText("Could not connect to a chonk server");
            connectionAlert.setContentText("To set set up a server locally, click launch and " +
                    "follow the terminal instructors.");
            connectionAlert.show();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
