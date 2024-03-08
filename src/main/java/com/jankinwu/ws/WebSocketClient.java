package com.jankinwu.ws;

import javafx.application.Platform;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

/**
 * @author jankinwu
 * @description
 * @date 2024/3/7 12:33
 */
@ClientEndpoint
public class WebSocketClient{

    private Session session;

    private Consumer<String> textUpdater;

    public WebSocketClient(String serverUrl) {
        try {
            URI uri = new URI(serverUrl);
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, uri);
        } catch (URISyntaxException | DeploymentException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException("与弹幕-按键映射器连接失败！");
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connected to WebSocket server.");
        this.session = session;
        sendMessage("Hello, Server!");
        Platform.runLater(() -> {
            if (textUpdater != null) {
                textUpdater.accept("已成功连接至弹幕-按键映射器！");
            }
        });
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("Received message from server: " + message);
        Platform.runLater(() -> {
            if (textUpdater != null) {
                textUpdater.accept(message);
            }
        });
    }

    @OnClose
    public void onClose(CloseReason reason) {
        System.out.println("WebSocket connection closed: " + reason);
        Platform.runLater(() -> {
            if (textUpdater != null) {
                textUpdater.accept("与弹幕-按键映射器连接中断！");
            }
        });
    }

    @OnError
    public void onError(Throwable error) {
        System.err.println("WebSocket error: " + error.getMessage());
    }

    public void sendMessage(String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setTextUpdater(Consumer<String> textUpdater) {
        this.textUpdater = textUpdater;
    }
}
