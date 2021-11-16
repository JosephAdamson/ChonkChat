package com.joe.chonkchat.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;

import java.util.Objects;
import java.util.Optional;

/**
 * Wrapper class for custom styled alerts.
 *
 * @author Joseph Adamson
 */
public class AlertWrapper {
    
    private Alert content;                        
    
    public AlertWrapper(AlertWrapper.AlertWrapperType type) {
        switch (type) {                                                       
            case CONNECTION_WARNING: 
                this.content = new Alert(Alert.AlertType.WARNING);
                this.content.setTitle("Connection Warning");
                this.content.setHeaderText("Could not connect to a chonk server");
                this.content.setContentText("To set set up a server locally, click launch and " +
                        "follow the terminal instructors.");
                break;
               
            case DUPLICATE_USERNAME: 
                this.content = new Alert(Alert.AlertType.WARNING);
                this.content.setTitle("Connection rejected");
                this.content.setHeaderText("Duplicate username detected");
                this.content.setContentText("There is already an active user with this username.");
                break;
                
            case BLANK_USERNAME:
                this.content = new Alert(Alert.AlertType.WARNING);
                this.content.setTitle("Username Warning");
                this.content.setHeaderText("Username must not be blank.");
                this.content.setContentText("Please make provide a username before logging in.");
                break;
                
            case LOG_OUT:
                this.content = new Alert(Alert.AlertType.CONFIRMATION);
                this.content.setContentText("Are you sure you want to log out?");
                break;
                
            default:
                System.out.println("Format not found");
        }
        DialogPane dialogPane = content.getDialogPane();

        dialogPane.getStylesheets().add(Objects.requireNonNull(getClass()
                .getResource("/com/joe/styles/customalert.css")).toExternalForm());
        dialogPane.getStyleClass().add("customAlert");
    }
    
    public void show() {
        this.content.show();
    }
    
    public Optional<ButtonType> showAndWait() {
        return this.content.showAndWait();
    }
    
    public void close() {
        this.content.close();
    }
    
    public static enum AlertWrapperType {
        CONNECTION_WARNING,
        DUPLICATE_USERNAME,
        BLANK_USERNAME,
        LOG_OUT;
    }
}
