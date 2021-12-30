package com.joe.chonkchat.client;

import com.joe.chonkchat.data.*;
import com.joe.chonkchat.util.AlertWrapper;
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
import javafx.scene.paint.Paint;
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
    private final DownloaderService downloaderService = new DownloaderService();
    private ScrollPane emojiSelector;
    @FXML private ToggleButton micButton;
    @FXML private Button fileButton;
    @FXML private Button emojiButton;
    private boolean micToggled;
    @FXML public BorderPane basePane;
    @FXML private BorderPane chatBox;
    @FXML public ComboBox<String> statusBar;
    @FXML private TextArea textInput;
    @FXML public HBox textInputConsole;
    @FXML private ListView<HBox> chatWindow;
    @FXML private ListView<HBox> onlineUsers;
    /**
     * Method is called by client lister thread repeatedly during conversation.
     * Must be synchronized as each post thread shares the same data structure (chatWindow, 
     * observable list) on execution.
     *
     * @param message Message object kicked up from clientThread.
     */
    public synchronized void updateChatWindow(Message message) {

        Post post = new Post(message);

        post.setOnSucceeded(event -> {
            chatWindow.getItems().add(post.getValue());
        });

        Thread postUpdate = new Thread(post);
        postUpdate.setDaemon(true);
        postUpdate.start();
    }

    /**
     * Represents a single post to the chat, carried out on its own thread
     * away from the main GUI thread.
     */
    class Post extends Task<HBox> {

        private final Message message;

        Post(Message message) {
            this.message = message;
        }

        @Override
        protected HBox call() {
            HBox container = new HBox();

            if (message.getSender().getUsername().equals(client.getUsername())) {
                container.setAlignment(Pos.BASELINE_RIGHT);
            } else {
                container.setAlignment(Pos.CENTER_LEFT);
            }
            return processMessage(container, message);
        }
    }

    /**
     * Passes a given message on to the helper method designed to format it 
     * for display, according to its MessageType.
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
                VBox fileBox = formatFilePost(message);
                container.getChildren().add(fileBox);
                break;
                
            case AUDIO:
                HBox audioBox = formatAudioPost(message);
                container.getChildren().add(audioBox);
                break;

            default:
                System.out.println("Message type not found.");
        }
        return container;
    }

    /**
     * Set colour attributes for basic text posts (user and self)
     *
     * @param message: message.
     * @return formatted and styled text in a TextFlow.
     */
    public TextFlow formatBasicPost(Message message) {
        
        TextFlow content = emojiParser(message.getTextData());
        content.setMaxWidth(340);

        Text time = formatTime(message);
        
        TextFlow flow = new TextFlow();
        flow.setMaxWidth(350);
        
        if (!message.getSender().getUsername().equals(client.getUsername())) {

            Text sender = new Text(message.getSender().getUsername() + "\n");
            sender.setStyle("-fx-fill: " + message.getSender().getColourTag() + ";" 
                    + "-fx-font-size: 15");
            flow.getStyleClass().add("senderBubble");
            flow.getChildren().addAll(sender, content, new Text("\n"), time);

        } else {
            flow.getStyleClass().add("selfBubble");
            flow.getChildren().addAll(content, new Text("\n"), time);
        }
        flow.setLineSpacing(2);
        
        return flow;
    }

    /**
     * Refresh active users displayed to the left of the chat window.
     *
     * @param message containing current active users.
     */
    public void refreshOnlineUserList(Message message) {
        List<User> activeUsers = message.getActiveUsers();

        ArrayList<HBox> users = new ArrayList<>();
        for (User user: activeUsers) {
            Label username = new Label(user.getUsername());
            username.setStyle("-fx-text-fill: " + user.getColourTag() + ";"
                    + "-fx-font-size: 16;");
            Label status = new Label(user.getStatus().toString());
            status.setStyle("-fx-text-fill: #949392;" +
                    "-fx-font-size: 12;");
            ImageView avatar = new ImageView(new Image(user.getAvatar()));
            avatar.setFitWidth(30);
            avatar.setFitHeight(30);
            HBox container = new HBox();
            container.setPrefHeight(35);
            container.setSpacing(6);
            container.getChildren().addAll(avatar, username, status);
            container.setAlignment(Pos.CENTER);
            container.setStyle("-fx-background-color: #151a1c;" +
                    "-fx-border-color: #151a1c #151a1c #484a4a #151a1c");
            users.add(container);
        }
        onlineUsers.getItems().clear();
        onlineUsers.getItems().addAll(users);
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
        Button downloadButton = new Button();
        StackPane loadPane = new StackPane();

        try {
            ImageView fileImg = new ImageView(String.valueOf(getClass()
                    .getResource("/com/joe/images/file.png")));
            fileImg.setFitWidth(30);
            fileImg.setFitHeight(30);

            // load pane will contain download image, the progress indicator and
            // the updated (on success image)
            loadPane.setPrefWidth(30);
            loadPane.setPrefHeight(30);
            
            downloadButton.setPrefWidth(30);
            downloadButton.setPrefHeight(30);
            ImageView downloadImg = new ImageView(String.valueOf(getClass()
                    .getResource("/com/joe/images/download.png")));
            downloadImg.setFitWidth(30);
            downloadImg.setFitHeight(30);
            downloadButton.setGraphic(downloadImg);
            
            downloadButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    ProgressIndicator progressIndicator = new ProgressIndicator();
                    downloaderService.setFileTDownload(message.getFile());
                    progressIndicator.progressProperty()
                            .bind(downloaderService.progressProperty());
                    
                    loadPane.getChildren().add(progressIndicator);
                    downloaderService.restart();
                    downloadButton.toFront();
                    ImageView checkedImg = new ImageView(String.valueOf(getClass()
                            .getResource("/com/joe/images/checked.png")));
                    checkedImg.setFitHeight(30);
                    checkedImg.setFitWidth(30);
                    downloadButton.setGraphic(checkedImg);
                }
            });

            downloadView.getChildren().add(fileImg);
            Label file = new Label(filename);
            file.setStyle("-fx-text-fill: #ffffff");
            downloadView.getChildren().add(file);
            downloadView.setSpacing(5);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        if (!message.getSender().getUsername().equals(client.getUsername())) {
            bubble.getStyleClass().add("senderBubble");
            downloadView.setStyle("-fx-background-color: #3b3d3d;");
            downloadView.getChildren().get(1).setStyle("-fx-text-fill: #ffffff");
            
            Label sender = new Label(message.getSender().getUsername());
            sender.setStyle("-fx-background-color: #3b3d3d;"
                    +"-fx-text-fill:" + message.getSender().getColourTag() + ";");
            bubble.getChildren().add(sender);
        } else {
            bubble.getStyleClass().add("selfBubble");
            downloadView.setStyle("-fx-background-color: #007EA7;");
            downloadButton.setStyle("-fx-background-color: #007EA7;");
        }
        bubble.setStyle("-fx-text-fill: #ffffff;");
        loadPane.getChildren().add(downloadButton);
        downloadView.getChildren().add(loadPane);
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
        
        Text time = formatTime(message);
        
        flow.getChildren().addAll(size, new Text("\n"), time);
        bubble.getChildren().add(flow);
        bubble.setSpacing(5);
        
        return bubble;
    }

    /**
     * Play an audio message.
     * 
     * @param message audio message sent to the chat room
     * @return formatted post containing audio message.
     */
    public HBox formatAudioPost(Message message) {
        // get audio data
        byte[] audioFile = message.getAudioFile();

        // create bubble
        HBox bubble = new HBox();
        bubble.setAlignment(Pos.BASELINE_CENTER);
        Button mediaButton = new Button();
        mediaButton.setPrefHeight(30);
        mediaButton.setPrefWidth(30);
        
        final ImageView playIcon = new ImageView(String.valueOf(
                getClass().getResource("/com/joe/images/speaker.png"))
        );
        playIcon.setFitWidth(30);
        playIcon.setFitHeight(30);
        
        mediaButton.setGraphic(playIcon);

        mediaButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            private boolean mediaButtonClicked;
            
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (!mediaButtonClicked) {
                    mediaButtonClicked = true;
                    AudioPlayback.playback(audioFile);
                } else {
                    mediaButtonClicked = false;
                    AudioPlayback.stopPlayback();
                }
            }
        });

        TextFlow content = new TextFlow();
        Text sender = new Text();
        if (!message.getSender().getUsername().equals(client.getUsername())) {
            bubble.getStyleClass().add("senderBubble");
            sender = new Text(message.getSender().getUsername());
            sender.setFill(Paint.valueOf(message.getSender().getColourTag()));
        } else {
            bubble.getStyleClass().add("selfBubble");
            sender = new Text("You");
            sender.setFill(Color.WHITE);
        }
        sender.setFont(Font.font("Veranda", FontWeight.NORMAL, 15));
        Text trail = new Text(" posted an audio message");
        trail.setFill(Color.WHITE);
        trail.setFont(Font.font("Veranda", FontWeight.NORMAL, 15));
        content.getChildren().addAll(sender, trail);
        Text time = formatTime(message);
        bubble.getChildren().addAll(content, mediaButton, time);
        return bubble;
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

    /**
     * Toggle microphone button to record audio.
     */
    public void recordAudio() {
        if (!micToggled) {
            micToggled = true;
            textInput.setDisable(true);
            emojiButton.setDisable(true);
            fileButton.setDisable(true);
            AudioUtil.setIsRecording(true);
            AudioRecorder.record(client);
        } else {
            micToggled = false;
            textInput.setDisable(false);
            emojiButton.setDisable(false);
            fileButton.setDisable(false);
            AudioUtil.setIsRecording(false);
        }
    }
    
    @FXML
    public void emojiChooser() {
        
        // if the cancel button is present (emoji button already clicked)
        if (textInputConsole.getChildren().size() == 4) {
            
            // We will need to resize the textInputContainer dynamically to accommodate cancel button.
            HBox textInputContainer = (HBox) textInputConsole.getChildren().get(2);
            final double originalTextBoxWidth = textInputContainer.getWidth();
            
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
                            .setPrefWidth(originalTextBoxWidth);
                    
                    chatBox.getChildren().remove(chatBox.getTop());
                    chatBox.setPrefHeight(chatBox.getPrefHeight() - 100);
                }
            });
            ((HBox) textInputConsole.getChildren().get(2))
                    .setPrefWidth(originalTextBoxWidth - 45);
            
            textInputConsole.getChildren().add(0, cancel);
            chatBox.setPrefHeight(chatBox.getPrefHeight() + 100);
            chatBox.setTop(emojiSelector);
        }
    }

    /**
     * Splits text content to identify Strings that represent emojis.
     * Turns string representations into corresponding emoji.pngs. They are 
     * then added, along with text content, to the resulting TextFlow.
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
     * Enables user to change availability displayed to all
     * other users.
     */
    @FXML
    public void changeAvailability() {
        Status update = Status.valueOf(statusBar.getValue().toUpperCase());
        client.updateStatus(update);
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
     * Custom close window method for chat window.
     */
    @FXML
    public void logoutOnWindowClose() {
        AlertWrapper logoutAlert = new AlertWrapper(AlertWrapper.AlertWrapperType.LOG_OUT);

        Optional<ButtonType> confirmation = logoutAlert.showAndWait();
        if (confirmation.isPresent() && confirmation.get() == ButtonType.OK) {
            client.disconnect();
            Stage thisStage = (Stage) basePane.getScene().getWindow();
            thisStage.close();

        } else {
            logoutAlert.close();
        }
    }

    /**
     * Close window on server shutdown.
     */
    public void forceClose() {
        Stage thisStage = (Stage) basePane.getScene().getWindow();
        thisStage.close();
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Client getClient() {
        return client;
    }
    
    public void setEmojiSelector(ScrollPane emojiSelector) {
        this.emojiSelector = emojiSelector;
    }
    
    public TextArea getTextInput() {return textInput;}

    public ComboBox<String> getStatusBar() {
        return statusBar;
    }

    /**
     * Format timestamps for messages.
     * @param message message content
     * @return formatted text object
     */
    public Text formatTime(Message message) {
        Text time = new Text(message.getTimeSent());
        time.setFont(Font.font("Veranda", FontWeight.NORMAL, 10));
        time.setFill(Color.valueOf("#d0d2d6"));
        return time;
    }
}
