package com.example.chonkchat.client;

import com.example.chonkchat.data.Message;
import com.example.chonkchat.server.Server;
import com.example.chonkchat.util.CustomWindowBaseController;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class ChatController extends CustomWindowBaseController {

    private Client client;
    private String username;
    
    @FXML
    private TextArea textInput;

    @FXML
    public ListView<HBox> chatWindow;
    
    // for now both tasks handle text messages only.
    
    class SelfPost extends Task<HBox> {
        
        private final Message message;
        
        public SelfPost(Message message) {
           this.message = message; 
        }
        
        @Override
        protected HBox call() throws Exception {
            Label content = new Label(message.getMessage());
            content.setStyle(
                    "-fx-background-color: #a804d1;" 
                    + "-fx-text-fill: #ffffff;"
                    + "-fx-background-radius: 24px;"
                    + "-fx-border-radius: 24px;"
            );
            content.setWrapText(true);
            
            // create node
            HBox container = new HBox();
            container.getChildren().add(content);
            container.setMaxWidth(500);
            
            return container;
        }
    }
    
    class UserPost extends Task<HBox> {
        
        private Message message;
        
        public UserPost(Message message) {
            this.message = message;
        }

        @Override
        protected HBox call() throws Exception {
            Label content = new Label(message.getMessage());
            content.setStyle(
                    "-fx-background-color: #d3d2d4;"
                    + "-fx-text-fill: #000000;"
                    + "-fx-background-radius: 24px;" 
                    +"-fx-border-radius: 24px;"
            );
            content.setWrapText(true);

            // create node
            HBox container = new HBox();
            container.getChildren().add(content);
            container.setMaxWidth(500);

            return container;
        }
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

    @FXML
    public void onTextInputEnter(KeyEvent keyEvent) {
        
        if (keyEvent.getCode() == KeyCode.ENTER) {
            String text = textInput.getText();
            client.sendMessage(text);
            textInput.clear();
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
