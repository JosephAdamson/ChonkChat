module com.example.chonkchat {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.chonkchat to javafx.fxml;
    exports com.example.chonkchat;
    exports com.example.chonkchat.client;
    opens com.example.chonkchat.client to javafx.fxml;
    exports com.example.chonkchat.server;
    opens com.example.chonkchat.server to javafx.fxml;
    exports com.example.chonkchat.util;
    opens com.example.chonkchat.util to javafx.fxml;
}