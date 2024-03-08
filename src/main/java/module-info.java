module com.jankinwu {
    requires javafx.controls;
    requires javafx.fxml;
    requires javax.websocket.api;

    opens com.jankinwu.ui to javafx.graphics;
    exports com.jankinwu.ui;
    exports com.jankinwu.ws;
}