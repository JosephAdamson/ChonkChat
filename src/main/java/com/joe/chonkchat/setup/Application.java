package com.joe.chonkchat.setup;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

import java.io.IOException;

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
        controller.getAvatarButton().setGraphic(defaultAvatar);
        controller.setAvatarImageURL(img.getUrl());
        
        controller.getUsernameFont().getItems()
                        .addAll(Color.WHITE,
                                Color.DEEPSKYBLUE,
                                Color.AQUA,
                                Color.BLUEVIOLET,
                                Color.RED,
                                Color.CRIMSON,
                                Color.YELLOW,
                                Color.LAWNGREEN,
                                Color.YELLOWGREEN,
                                Color.HOTPINK
                        );
        
        // Set-up for a simplified color picker
        controller.getUsernameFont().setCellFactory(new Callback<ListView<Color>, ListCell<Color>>() {
            @Override public ListCell<Color> call(ListView<Color> p) {
                return new ListCell<Color>() {
                    private final Rectangle rectangle;
                    {
                        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                        rectangle = new Rectangle(15, 15);
                    }

                    @Override protected void updateItem(Color item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            rectangle.setFill(item);
                            setGraphic(rectangle);
                        }
                    }
                };
            }
        });
        controller.getUsernameFont().setButtonCell(new ComboButtonSetter());
        controller.getUsernameFont().getSelectionModel().selectFirst();
        
        root.getStylesheets().add(String.valueOf(getClass().getResource("/com/joe/styles/launcher.css")));
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setResizable(false);
        stage.show();
    }
    
    class ComboButtonSetter extends ListCell<Color> {

        @Override
        protected void updateItem(Color color, boolean b) {
            super.updateItem(color, b);
            if (color != null) {
                setGraphic(new Rectangle(15, 15, color));
            }
        }
    }

    public static void main(String[] args) {
        launch();
    }
}