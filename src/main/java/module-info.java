module com.jankinwu {
    requires javafx.controls;
    requires javafx.fxml;
    requires javax.websocket.api;
    requires com.alibaba.fastjson2;
    requires lombok;

    opens com.jankinwu.ui to javafx.graphics;
    opens com.jankinwu to javafx.fxml;
    exports com.jankinwu.ui;
    exports com.jankinwu.ws;
    exports com.jankinwu;
    exports com.jankinwu.dto;
    requires javafx.graphics;
}