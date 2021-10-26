package com.joe.chonkchat.setup;

import com.joe.chonkchat.client.ChatController;
import com.joe.chonkchat.client.Client;
import com.joe.chonkchat.server.Server;
import com.joe.chonkchat.server.TerminalController;
import com.joe.chonkchat.util.CustomWindowBaseController;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.file.Path;
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
                        .getResource("/com/joe/views/serverterminal-view.fxml")));

                Parent root = fxmlLoader.load();
                root.getStylesheets().add(String.valueOf(getClass()
                        .getResource("/com/joe/styles/server.css")));

                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.getIcons().add(new Image(getClass()
                        .getResourceAsStream("/com/joe/images/server.png")));
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
                        .getResource("/com/joe/views/chat-view.fxml")));

                Parent root = fxmlLoader.load();
                root.getStylesheets().add(String.valueOf(getClass()
                        .getResource("/com/joe/styles/chat.css")));

                // set up fields, inject them manually and start client listening thread
                ChatController chatController = fxmlLoader.getController();
                Socket socket = new Socket("localhost", Server.PORT);
                Client client = new Client(socket, username, chatController);
                chatController.setClient(client);
                chatController.setUsername(username);
                chatController.getClient().listenForIncomingMessages();

                // set up emoji selector window (hidden) so we only have to initialize it once.
                ScrollPane scrollPane = new ScrollPane();
                
                GridPane emojiSelector = new GridPane();
                emojiSelector.setPrefHeight(100);
                emojiSelector.setPrefWidth(200);
                emojiSelector.getStyleClass().add("emojiBox");

                scrollPane.setContent(emojiSelector);
                
                File emojiFolder = new File(Objects.requireNonNull(getClass()
                        .getResource("/com/joe/images/emojis")).getFile());
                
                File[] emojis = emojiFolder.listFiles();

                // had to hardcode this, not getting a filled-out gridpane.
                double dim = 35;
                
                for (int i = 0;  i < emojis.length; i++) {
                    int row = i / 10;
                    int col = i % 10;
                    
                    String filename = emojis[i].getName();
                    Button btn = new Button();
                    btn.setPrefHeight(dim);
                    btn.setPrefWidth(dim);
                    btn.getStyleClass().add("emojiButton");
                    ImageView emoji = new ImageView(new Image(String.valueOf(getClass()
                            .getResource("/com/joe/images/emojis/" + filename))));
                    emoji.setFitWidth(dim);
                    emoji.setFitHeight(dim);
                    btn.setGraphic(emoji);
                    
                    btn.setOnMouseClicked(new EmojiClicker(chatController.getTextInput(), filename));
                    
                    emojiSelector.add(btn, col, row);
                }
                
                chatController.setEmojiSelector(scrollPane);

                Scene scene = new Scene(root);
                scene.setFill(Color.TRANSPARENT);
                Stage stage = new Stage();
                
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

    /**
     * Manage resources for a emoji button click.
     */
    private class EmojiClicker implements EventHandler<MouseEvent>{
        TextArea output; 
        String filename;
        
        public EmojiClicker(TextArea output, String filename) {
            this.output = output;
            this.filename = filename;
        }

        @Override
        public void handle(MouseEvent mouseEvent) {
            String emoteCode = filename.replace(".png", "");
            output.setText(output.getText() + ":" + emoteCode + ":");
        }
    }
}
