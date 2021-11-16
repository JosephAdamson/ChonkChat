package com.joe.chonkchat.setup;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        
        FXMLLoader fxmlLoader = 
                new FXMLLoader(getClass().getResource("/com/joe/views/launch-view.fxml"));
        Parent root = fxmlLoader.load();
        
        LauncherController controller = fxmlLoader.getController();
        Image img = new Image(String.valueOf(getClass()
                .getResource("/com/joe/images/avatars/robot_1.png")));
        ImageView defaultAvatar = new ImageView(img);
        defaultAvatar.setFitHeight(80);
        defaultAvatar.setFitWidth(80);
        controller.avatarButton.setGraphic(defaultAvatar);
        controller.avatarImageURL = img.getUrl();
        
        root.getStylesheets().add(String.valueOf(getClass().getResource("/com/joe/styles/launcher.css")));
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}