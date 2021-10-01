package com.example.chonkchat.client;

import com.example.chonkchat.data.Message;
import com.example.chonkchat.util.CustomWindowBaseController;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

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

            Text content = new Text(message.getMessage());
            content.setFont(Font.font("Veranda", FontWeight.NORMAL, 15));
            content.setFill(Color.WHITE);

            Text time = new Text(message.getTimeSent());
            time.setFont(Font.font("Veranda", FontWeight.NORMAL, 10));
            time.setFill(Color.valueOf("#d0d2d6"));

            TextFlow flow = new TextFlow();
            flow.setStyle(
                    "-fx-background-color: #007EA7;"
                            + "-fx-background-radius: 24px;"
                            +"-fx-border-radius: 24px;"
                            + "-fx-padding: 10;"
            );
            flow.getChildren().addAll(content, time);
            
            // create node
            HBox container = new HBox();
            container.getChildren().add(flow);
            container.setMaxWidth(700);
            container.setAlignment(Pos.BASELINE_RIGHT);
            
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
            
            Text sender = new Text(message.getSender() + "\n");
            sender.setFont(Font.font("Veranda", FontWeight.BOLD, 15));
            sender.setFill(Color.WHITE);
            
            Text content = new Text(message.getMessage());
            content.setFont(Font.font("Veranda", FontWeight.NORMAL, 15));
            content.setFill(Color.WHITE);

            Text time = new Text(message.getTimeSent());
            time.setFont(Font.font("Veranda", FontWeight.NORMAL, 10));
            time.setFill(Color.valueOf("#d0d2d6"));

            TextFlow flow = new TextFlow();
            flow.setStyle(
                    "-fx-background-color: #3b3d3d;" 
                            + "-fx-background-radius: 24px;"
                            +"-fx-border-radius: 24px;"
                            + "-fx-padding: 10;"
            );
            flow.getChildren().addAll(sender, content, time);

            // create node
            HBox container = new HBox();
            container.setMaxWidth(700);
            container.getChildren().add(flow);
            container.setAlignment(Pos.CENTER_LEFT);
            
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
