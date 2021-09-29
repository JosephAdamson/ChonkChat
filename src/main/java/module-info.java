module com.example.chonkchat {
    requires javafx.controls;
    requires javafx.fxml;


    exports com.example.chonkchat.setup;
    opens com.example.chonkchat.setup to javafx.fxml;
    exports com.example.chonkchat.client;
    opens com.example.chonkchat.client to javafx.fxml;
    exports com.example.chonkchat.server;
    opens com.example.chonkchat.server to javafx.fxml;
    exports com.example.chonkchat.util;
    opens com.example.chonkchat.util to javafx.fxml;
}