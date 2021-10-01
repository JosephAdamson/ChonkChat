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
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
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
            
            FXMLLoader fxmlLoader = new FXMLLoader(Objects.requireNonNull(getClass()
                    .getResource("/com/example/views/chat-view.fxml")));

            Parent root = fxmlLoader.load();
            root.getStylesheets().add(String.valueOf(getClass()
                    .getResource("/com/example/styles/chat.css")));
            
            // set up fields and inject them manually and start client listening thread
            ChatController chatController = fxmlLoader.getController();
            Socket socket = new Socket("localhost", Server.PORT);
            Client client = new Client(socket, username, chatController);
            chatController.setClient(client);
            chatController.setUsername(username);
            chatController.getClient().listenForIncomingMessages();
            
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.getIcons().add(new Image(getClass()
                    .getResourceAsStream("/com/example/images/client.png")));
            
            stage.setTitle("client");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
