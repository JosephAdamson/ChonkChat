module com.example.chonkchat {
    requires javafx.controls;
    requires javafx.fxml;


    exports com.joe.chonkchat.setup;
    opens com.joe.chonkchat.setup to javafx.fxml;
    exports com.joe.chonkchat.client;
    opens com.joe.chonkchat.client to javafx.fxml;
    exports com.joe.chonkchat.server;
    opens com.joe.chonkchat.server to javafx.fxml;
    exports com.joe.chonkchat.util;
    opens com.joe.chonkchat.util to javafx.fxml;
    exports com.joe.chonkchat.data;
    opens com.joe.chonkchat.data to javafx.fxml;
}