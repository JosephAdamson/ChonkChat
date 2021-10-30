package com.joe.chonkchat.client;

import com.joe.chonkchat.data.FileTransfer;
import com.joe.chonkchat.data.Message;
import com.joe.chonkchat.util.CustomWindowBaseController;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
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
    private final DownloaderService downloaderService = new DownloaderService();
    
    // set this so we only have to initialize it once.
    private ScrollPane emojiSelector;

    @FXML
    public BorderPane basePane;

    /**
     * Allows for further manipulation of text input area
     */
    @FXML
    private BorderPane consoleBox;
    
    @FXML
    private TextArea textInput;

    @FXML
    public HBox textInputConsole;
    
    @FXML
    private ListView<HBox> chatWindow;

    @FXML
    private ListView<HBox> onlineUsers;

    /**
     * Set colour attributes for basic text posts (user and self)
     *
     * @param message: message.
     * @return formatted and styled text in a TextFlow.
     */
    public TextFlow formatBasicPost(Message message) {
        
        TextFlow content = emojiParser(message.getTextData());
        content.setMaxWidth(340);

        Text time = new Text(message.getTimeSent());
        
        time = new Text(time.getText());
        time.setFont(Font.font("Veranda", FontWeight.NORMAL, 10));
        time.setFill(Color.valueOf("#d0d2d6"));
        
        TextFlow flow = new TextFlow();
        flow.setMaxWidth(350);
        
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

            flow.getChildren().addAll(sender, content, new Text("\n"), time);

        } else {
            flow.setStyle(
                    "-fx-background-color: #007EA7;"
                            + "-fx-background-radius: 24px;"
                            +"-fx-border-radius: 24px;"
                            + "-fx-padding: 10;"
            );

            flow.getChildren().addAll(content, new Text("\n"), time);
        }
        flow.setLineSpacing(2);
        
        return flow;
    }

    /**
     * Set attributes for a file post (user and self)
     * 
     * @param message: message
     * @return formatted post in a HBox
     */
    public VBox formatFilePost(Message message) {
        FileTransfer fileTransfer = message.getFile();
        String filename = fileTransfer.getName() + fileTransfer.getExtension();
        
        VBox bubble = new VBox();
        
        // middle element of the bubble; displays file pic, filename and download clickable.
        HBox downloadView = new HBox();

        try {
            ImageView fileImg = new ImageView(String.valueOf(getClass()
                    .getResource("/com/joe/images/file.png")));
            fileImg.setFitWidth(30);
            fileImg.setFitHeight(30);

            // load pane will contain download image, the progress indicator and
            // the updated (on success image)
            StackPane loadPane = new StackPane();
            loadPane.setPrefWidth(30);
            loadPane.setPrefHeight(30);

            ImageView downloadImg = new ImageView(String.valueOf(getClass()
                    .getResource("/com/joe/images/download.png")));
            downloadImg.setFitWidth(30);
            downloadImg.setFitHeight(30);
            
            loadPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    
                    // insert a progress indicator here?
                    ProgressIndicator progressIndicator = new ProgressIndicator();
                    downloaderService.setFileTDownload(message.getFile());
                    progressIndicator.progressProperty()
                            .bind(downloaderService.progressProperty());
                    
                    loadPane.getChildren().add(progressIndicator);
                    
                    downloaderService.restart();
                    
                    downloadImg.toFront();
                    downloadImg.setImage(new Image(String.valueOf(getClass()
                            .getResource("/com/joe/images/checked.png"))));
                }
            });
            loadPane.getChildren().add(downloadImg);

            downloadView.getChildren().add(fileImg);
            Label file = new Label(filename);
            file.setStyle("-fx-text-fill: #ffffff");
            downloadView.getChildren().add(file);
            downloadView.getChildren().add(loadPane);
            downloadView.setSpacing(5);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        if (!message.getSender().equals(username)) {
            bubble.setStyle(
                    "-fx-background-color: #3b3d3d;"
                            +"-fx-text-fill: #ffffff;"
                            + "-fx-background-radius: 24px;"
                            +"-fx-border-radius: 24px;"
                            + "-fx-padding: 10;"
            );
            
            downloadView.setStyle("-fx-background-color: #3b3d3d;");
            downloadView.getChildren().get(1).setStyle("-fx-text-fill: #ffffff");
            
            Label sender = new Label(message.getSender());
            sender.setStyle("-fx-background-color: #3b3d3d;"
                    +"-fx-text-fill: #ffffff;");
            bubble.getChildren().add(sender);
        } else {
            bubble.setStyle(
                    "-fx-background-color: #007EA7;"
                            +"-fx-text-fill: #ffffff;"
                            + "-fx-background-radius: 24px;"
                            +"-fx-border-radius: 24px;"
                            + "-fx-padding: 10;"
            );
            
            downloadView.setStyle("-fx-background-color: #007EA7;");
        }
        bubble.getChildren().add(downloadView);
        
        TextFlow flow = new TextFlow();
        Text size = new Text();
        
        // covert file bytes to a human-readable format
        long byteVal = message.getFile().getContent().length;
        long kilobytes = byteVal / 1024;
        if (kilobytes > 1024) {
            long megabytes = kilobytes / 1024;
            size.setText(String.format("%,d MB", megabytes));
        } else {
            size.setText(String.format("%,d KB", kilobytes));
        }
        size.setFont(Font.font("Veranda", FontWeight.NORMAL, 10));
        size.setFill(Color.BLACK);
        
        Text time = new Text(message.getTimeSent());
        time.setFont(Font.font("Veranda", FontWeight.NORMAL, 10));
        time.setFill(Color.valueOf("#d0d2d6"));
        
        flow.getChildren().addAll(size, new Text("\n"), time);
        bubble.getChildren().add(flow);
        bubble.setSpacing(5);
        
        return bubble;
    }

    /**
     * Looks at a given message and passes it on to the helper method
     * designed to format it for display according to its MessageType.
     *  
     * @param container: underlying HBox that forms the base of the list cell
     *                  displayed when a post is updated to the chat window
     * @param message: message
     * @return container
     */
    public HBox processMessage(HBox container, Message message) {
        switch (message.getMessageType()) {

            case TEXT:
                TextFlow flow = formatBasicPost(message);
                container.getChildren().add(flow);
                break;

            case FILE:
                VBox box = formatFilePost(message);
                container.getChildren().add(box);
                break;

            default:
                System.out.println("Whaaaat?");
        }

        return container;
    }
    
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
        protected HBox call() {

            HBox container = new HBox();
            container.setMaxWidth(700);
            container.setAlignment(Pos.BASELINE_RIGHT);
            
            return processMessage(container, message);
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
        protected HBox call() {

            HBox container = new HBox();
            container.setMaxWidth(700);
            container.setAlignment(Pos.CENTER_LEFT);
            
            return processMessage(container, message);
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
            label.setStyle("-fx-text-fill: #ffffff;" 
                    + "-fx-font-size: 16;");
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
        
        if (keyEvent.getCode() == KeyCode.ENTER && 
                !textInput.getText().isEmpty()) {
            String text = textInput.getText();
            client.sendMessage(text);
            textInput.clear();
        }
    }

    /**
     * Upload file via the chat window
     * 
     * @param event click of the 'file' button.
     */
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
    
    @FXML
    public void emojiChooser() {
        
        // if the cancel button is present (emoji button already clicked)
        // we do nothing, this might have to be changed in the future if I 
        // add further buttons.
        if (textInputConsole.getChildren().size() == 3) {
            
            // We will need to resize the textInputContainer and its container dynamically
            // to accommodate cancel button.
            HBox textInputContainer = (HBox) textInputConsole.getChildren().get(2);
            double originalContainerWidth = textInputContainer.getWidth();
            
            // add cancellation button to left of emoji button
            Button cancel = new Button();
            cancel.prefHeight(45);
            cancel.prefWidth(45);
            cancel.setTranslateX(10);
            cancel.setTranslateY(30);
            
            cancel.setStyle("-fx-text-fill: #949392;" 
                    + "-fx-font-size: 20px;"
                    + "-fx-font-family: System;"
            );
            cancel.setText("X");

            cancel.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    textInputConsole.getChildren().remove(0);
                    ((HBox) textInputConsole.getChildren().get(2))
                            .setPrefWidth(originalContainerWidth);
                    
                    consoleBox.getChildren().remove(consoleBox.getTop());
                    consoleBox.setPrefHeight(consoleBox.getPrefHeight() - 100);
                }
            });
            
            ((HBox) textInputConsole.getChildren().get(2))
                    .setPrefWidth(originalContainerWidth - 45);
            
            textInputConsole.getChildren().add(0, cancel);
            
            
            consoleBox.setPrefHeight(consoleBox.getPrefHeight() + 100);
            
            consoleBox.setTop(emojiSelector);
        }
    }

    /**
     * Splits text content to identify Strings that represent emojis (byte codes).
     * Turns string byte codes into corresponding emojis where they are added,
     * along with text content, to the resulting TextFlow.
     * 
     * @param message text message content
     * @return the formatted text content now containing inline emojis
     */
    public TextFlow emojiParser(String message) {
        
        TextFlow flow = new TextFlow();
        
        // split on colon-casing to determine if any strings represent emojis
        String[] tokens = message.split(":");
        
        for (String token : tokens) {
            
            URL tokenPath = getClass().getResource("/com/joe/images/emojis/" + token + ".png");
            
            if (tokenPath != null) {
                
                ImageView emoji = new ImageView(new Image(String.valueOf(tokenPath)));
                emoji.setFitHeight(22);
                emoji.setFitWidth(22);
                flow.getChildren().add(emoji);
                
            } else {
                Text textSegment = new Text(token);
                textSegment.setFont(Font.font("OpenSansEmoji", FontWeight.NORMAL, 15));
                textSegment.setFill(Color.WHITE);
                flow.getChildren().add(textSegment);
            }
        }
        return flow;
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

    public void setClient(Client client) {
        this.client = client;
    }

    public Client getClient() {
        return client;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public void setEmojiSelector(ScrollPane emojiSelector) {
        this.emojiSelector = emojiSelector;
    }
    
    public TextArea getTextInput() {return textInput;}
}
