package com.jankinwu.ws;

import com.alibaba.fastjson2.JSONObject;
import com.jankinwu.dto.PushMsgDTO;
import javafx.application.Platform;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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

    private ScheduledExecutorService reconnectScheduler;

    private String serverUrl;
    private static final long RECONNECT_DELAY = 8; // 重新连接延迟时间（单位：秒）

    public WebSocketClient(String serverUrl) {
        try {
            URI uri = new URI(serverUrl);
            this.serverUrl = serverUrl;
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, uri);
        } catch (URISyntaxException | DeploymentException | IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                if (textUpdater != null) {
                    textUpdater.accept(assembleMsg("与弹幕-按键映射器连接失败！即将尝试重新连接。。。", "http://oss.jankinwu.com/img/f34e7c310a55b319b1238d6944a98226cefc1764.gif"));
                }
            });
            scheduleReconnect();
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connected to WebSocket server.");
        this.session = session;
        sendMessage("Hello, Server!");
        Platform.runLater(() -> {
            if (textUpdater != null) {
                textUpdater.accept(assembleMsg("已成功连接至弹幕-按键映射器！", "http://oss.jankinwu.com/img/af53b18f8c5494ee023701052af5e0fe99257e26.gif"));
            }
        });
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("Received message from server: " + message);
        Platform.runLater(() -> {
            if (textUpdater != null) {
                if ("Hello, Client!".equals(message)) {
                    textUpdater.accept(assembleMsg("已成功连接至弹幕-按键映射器！", "http://oss.jankinwu.com/img/af53b18f8c5494ee023701052af5e0fe99257e26.gif"));
                } else {
                    textUpdater.accept(message);
                }
            }
        });
    }

    @OnClose
    public void onClose(CloseReason reason) {
        System.out.println("WebSocket connection closed: " + reason);
        Platform.runLater(() -> {
            if (textUpdater != null) {
                textUpdater.accept(assembleMsg("与弹幕-按键映射器连接中断！即将尝试重新连接。。。", "http://oss.jankinwu.com/img/3cd4202dd42a2834867639175cb5c9ea14cebf63.gif"));
            }
        });
        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        scheduleReconnect();
    }

    @OnError
    public void onError(Throwable error) {
        error.printStackTrace();
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

    private String assembleMsg(String msg, String avatarUrl) {
        PushMsgDTO dto = new PushMsgDTO();
        dto.setFill("#ffffff");
        dto.setFontFamily("Source Han Sans");
        dto.setStroke("#000000");
        dto.setFontSize("40");
        dto.setText(msg);
        dto.setType("shadow");
        dto.setAvatarUrl(avatarUrl);
        return JSONObject.toJSONString(dto);
    }

    private void scheduleReconnect() {
        if (reconnectScheduler == null || reconnectScheduler.isShutdown()) {
            reconnectScheduler = Executors.newSingleThreadScheduledExecutor();
            reconnectScheduler.scheduleWithFixedDelay(this::reconnect, 0, RECONNECT_DELAY, TimeUnit.SECONDS);
        }
    }

    private void reconnect() {
        // 进行重新连接的操作
        try {
            URI uri = new URI(serverUrl);
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, uri);
            // 连接成功后取消重连任务
            cancelReconnect();
        } catch (URISyntaxException | DeploymentException | IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                if (textUpdater != null) {
                    textUpdater.accept(assembleMsg("与弹幕-按键映射器重新尝试连接失败！", "http://oss.jankinwu.com/img/f34e7c310a55b319b1238d6944a98226cefc1764.gif"));
                }
            });
        }
    }

    private void cancelReconnect() {
        if (reconnectScheduler != null && !reconnectScheduler.isShutdown()) {
            reconnectScheduler.shutdown();
        }
    }
}
