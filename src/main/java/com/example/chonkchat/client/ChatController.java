package com.example.chonkchat.client;

import com.example.chonkchat.util.CustomWindowBaseController;
import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;

public class ChatController extends CustomWindowBaseController implements Initializable {
    
    private Thread clientThread;
    private String username;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
