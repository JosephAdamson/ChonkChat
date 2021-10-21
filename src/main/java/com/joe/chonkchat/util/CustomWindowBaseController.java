package com.joe.chonkchat.util;

import javafx.application.Platform;
import javafx.scene.Node;;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;

/**
 * Controller methods for views that use undecorated/transparent
 * window styles.
 * 
 * NOTE: must have javafx version 15.0.1 or greater as there is an issue with how the 
 * 'setIconify' method works with current iterations of macOS (I'm using Big Sur as I write this. 
 * The issue was fixed for javafx version 15).
 * 
 * @author Joseph Adamson
 */
public class CustomWindowBaseController {
    
    private double xOffSet = 0;
    private double yOffSet = 0;

    /**
     * Get the x and y offset for the window.
     */
    public void pressed(MouseEvent event) {
        xOffSet = event.getSceneX();
        yOffSet = event.getSceneY();
    }

    /**
     * Recompute offsets on drag event
     */
    public void drag(MouseEvent event) {
        
        // retrieve the stage (main window) from the event.
        Node node  = (Node) event.getSource();
        Stage thisStage = (Stage) node.getScene().getWindow();
        
        // register new position of the window
        thisStage.setX(event.getScreenX() - xOffSet);
        thisStage.setY(event.getScreenY() - yOffSet);
    }

    /**
     * Minimise the window.
     */
    public void minimize(MouseEvent event) {
        Node node  = (Node) event.getSource();
        Stage thisStage = (Stage) node.getScene().getWindow();
        
        thisStage.setIconified(true);
    }

    /**
     * Close button application fully
     */
    public void close() {
        Platform.exit();
        System.exit(0);
    }
}
