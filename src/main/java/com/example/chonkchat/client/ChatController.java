package com.example.chonkchat.client;

import com.example.chonkchat.data.Message;
import com.example.chonkchat.util.CustomWindowBaseController;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller for main chat interface.
 * 
 * @author Joseph Adamson.
 */
public class ChatController extends CustomWindowBaseController {
    
    private Client client;
    private String username;

    @FXML
    public BorderPane basePane;
    
    @FXML
    private TextArea textInput;

    @FXML
    public ListView<HBox> chatWindow;

    @FXML
    public ListView<HBox> onlineUsers;
    
    /**
     * Background UI task that updates chat window when the user
     * posts a message.
     */
    class SelfPost extends Task<HBox> {
        
        private final Message message;
        
        public SelfPost(Message message) {
           this.message = message; 
        }
        
        @Override
        protected HBox call() throws Exception {
            
            TextFlow flow = setBasicPost(message);
            
            // create node
            HBox container = new HBox();
            container.getChildren().add(flow);
            container.setMaxWidth(700);
            container.setAlignment(Pos.BASELINE_RIGHT);
            
            return container;
        }
    }

    /**
     * Background UI task that updates chat window as soon as another
     * user posts a message.
     */
    class UserPost extends Task<HBox> {
        
        private final Message message;
        
        public UserPost(Message message) {
            this.message = message;
        }

        @Override
        protected HBox call() throws Exception {
            
            TextFlow flow = setBasicPost(message);
            
            HBox container = new HBox();
            container.setMaxWidth(700);
            container.getChildren().add(flow);
            container.setAlignment(Pos.CENTER_LEFT);
            
            return container;
        }
    }

    /**
     * Set colour attributes for basic text posts (user and self)
     * 
     * @param message: message.
     * @return formatted and styled text for a post.
     */
    public TextFlow setBasicPost(Message message) {

        Text content = new Text(message.getTextData());
        content.setFont(Font.font("Veranda", FontWeight.NORMAL, 15));
        content.setFill(Color.WHITE);

        Text time = new Text(message.getTimeSent());
        time.setFont(Font.font("Veranda", FontWeight.NORMAL, 10));
        time.setFill(Color.valueOf("#d0d2d6"));
        
        TextFlow flow = new TextFlow();
        
        if (!message.getSender().equals(username)) {

            Text sender = new Text(message.getSender() + "\n");
            sender.setFont(Font.font("Veranda", FontWeight.BOLD, 15));
            sender.setFill(Color.WHITE);

            flow.setStyle(
                    "-fx-background-color: #3b3d3d;"
                            + "-fx-background-radius: 24px;"
                            +"-fx-border-radius: 24px;"
                            + "-fx-padding: 10;"
            );
            
            flow.getChildren().addAll(sender, content, time);
            
        } else {
            flow.setStyle(
                    "-fx-background-color: #007EA7;"
                            + "-fx-background-radius: 24px;"
                            +"-fx-border-radius: 24px;"
                            + "-fx-padding: 10;"
            );
            
            flow.getChildren().addAll(content, time);
        }
        
        return flow;
    }

    /**
     * Method is called by client lister thread repeatedly during conversation.
     * Must be synchronized as method calls share same data structure (chatWindow).
     * 
     * @param message Message object kicked up from clientThread.
     */
    public synchronized void updateChatWindow(Message message) {
        
        if (!message.getSender().equals(username)) {
            UserPost userPost = new UserPost(message);
            
            userPost.setOnSucceeded(event -> {
                chatWindow.getItems().add(userPost.getValue());
            });
            
            Thread userPostUpdate = new Thread(userPost);
            userPostUpdate.setDaemon(true);
            userPostUpdate.start();
        } else {
            SelfPost selfPost = new SelfPost(message);
            
            selfPost.setOnSucceeded(event -> {
                chatWindow.getItems().add(selfPost.getValue());
            });
            
            Thread selfPostUpdate = new Thread(selfPost);
            selfPostUpdate.setDaemon(true);
            selfPostUpdate.start();
        }
    }

    /**
     * Refresh active users displayed to the left of the chat window.
     * 
     * @param message containing current active users.
     */
    public void refreshOnlineUserList(Message message) {
        List<String> activeUsers = message.getActiveUsers();
        
        ArrayList<HBox> users = new ArrayList<>();
        for (String user: activeUsers) {
            Label label = new Label(user);
            label.setStyle("-fx-text-fill: #ffffff");
            HBox container = new HBox();
            container.getChildren().add(label);
            container.setAlignment(Pos.CENTER);
            container.setStyle("-fx-background-color: #151a1c;" +
                    "-fx-border-color: #151a1c #151a1c #484a4a #151a1c");
            users.add(container);
        }
        
        onlineUsers.getItems().clear();
        onlineUsers.getItems().addAll(users);
    }

    /**
     * Keyboard input for text input in chat window.
     * 
     * @param keyEvent enter 
     */
    @FXML
    public void onTextInputEnter(KeyEvent keyEvent) {
        
        if (keyEvent.getCode() == KeyCode.ENTER) {
            String text = textInput.getText();
            client.sendMessage(text);
            textInput.clear();
        }
    }

    /**
     * Custom close window method for chat window.
     */
    @FXML
    public void logoutOnWindowClose() {
        
        Alert logoutAlert = new Alert(Alert.AlertType.CONFIRMATION);
        logoutAlert.setContentText("Are you sure you want to log out?");

        Optional<ButtonType> confirmation = logoutAlert.showAndWait();
        if (confirmation.get() == ButtonType.OK) {
            client.disconnect();
            Stage thisStage = (Stage) basePane.getScene().getWindow();
            thisStage.close();
            
        } else {
            logoutAlert.close();
        }
    }
    
    @FXML
    public void uploadFile(MouseEvent event) {

        // Retrieve the window.
        Node node = (Node) event.getSource();
        Stage thisStage = (Stage) node.getScene().getWindow();
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters()
                .addAll(
                        new FileChooser.ExtensionFilter("All files must have", "*.*"),
                        new FileChooser.ExtensionFilter("Document", List.of(".txt", ".pdf")), 
                        new FileChooser.ExtensionFilter( "Image", List.of(".png", ".jpg", "gif"))
                );
        
        File selectedFile = fileChooser.showOpenDialog(thisStage);
        if (selectedFile != null) {
            client.sendFile(selectedFile);
        }
        
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Client getClient() {
        return client;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
