package com.jankinwu.ui;

import com.jankinwu.ws.WebSocketClient;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * @author jankinwu
 * @description
 * @date 2024/3/7 12:36
 */
public class Marquee extends Application {

    private static final double WINDOW_WIDTH = 600;
    private static final double WINDOW_HEIGHT = 55;
    private static final double FONT_SIZE = 40;
    private double xOffset = 0;
    private double yOffset = 0;
    private Text text;

    private TranslateTransition scrollAnimation;

    private static final double SCROLL_SPEED_RATIO = 0.2; // Adjust this value to change the scroll speed
    private double currentScrollSpeed = WINDOW_WIDTH * SCROLL_SPEED_RATIO;


    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Marquee Example");

        // 创建文本节点
        text = new Text("正在连接弹幕-按键映射器。。。");
        text.setFill(Color.WHITE);
        text.setStroke(Color.BLACK);
        text.setStrokeWidth(1);
        text.setFont(Font.font("Source Han Sans", FontWeight.BOLD, FONT_SIZE));


        // 创建容器用于裁剪文本内容
        Rectangle clipRect = new Rectangle(WINDOW_WIDTH, WINDOW_HEIGHT);
        clipRect.setArcWidth(10);
        clipRect.setArcHeight(10);

        // 将文本节点放置在裁剪容器内
        StackPane textPane = new StackPane(text);
        textPane.setAlignment(Pos.CENTER);
        textPane.setClip(clipRect);

        // 创建根节点并设置场景
        Group root = new Group();
        root.getChildren().add(textPane);
        root.setStyle("-fx-background-color: transparent;");
        root.getStyleClass().add("root-pane"); // 添加一个样式类
// 创建一个用于显示边框的矩形
        Rectangle border = new Rectangle(WINDOW_WIDTH, WINDOW_HEIGHT);
        border.setStroke(Color.RED); // 设置边框颜色
        border.setFill(Color.TRANSPARENT); // 设置填充颜色为透明
        border.setVisible(false); // 初始时隐藏边框

// 将边框添加到根节点
        root.getChildren().add(border);



        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setAlwaysOnTop(true);

        root.setOnMousePressed(event -> {
            if (event.getY() < WINDOW_HEIGHT) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });

        root.setOnMouseDragged(event -> {
            if (event.getY() < WINDOW_HEIGHT) {
                primaryStage.setX(event.getScreenX() - xOffset);
                primaryStage.setY(event.getScreenY() - yOffset);
            }
        });

        root.setOnMouseEntered(event -> {
            border.setVisible(true); // 鼠标移入时显示边框
        });

        root.setOnMouseExited(event -> {
            border.setVisible(false); // 鼠标移出时隐藏边框
        });


        // icon
        Image icon = new Image(Marquee.class.getResourceAsStream("/img/bullet_4.png"));
        primaryStage.getIcons().add(icon);



        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.setFill(Color.TRANSPARENT);
        primaryStage.setScene(scene);
        primaryStage.show();
        try {
            WebSocketClient webSocketClient = new WebSocketClient("ws://localhost:8080/websocket/plugin/1");
            webSocketClient.setTextUpdater(this::updateText);
        } catch (Exception e) {
            updateText(e.getMessage());
        }

        // 获取文本的宽度
        Text tempText = new Text(text.getText());
        tempText.setFont(text.getFont());
        double textWidth = text.getLayoutBounds().getWidth();
        // 计算滚动动画的持续时间
        double animationDuration = textWidth / currentScrollSpeed;
        // 创建平移动画，使文本节点水平滚动
        scrollAnimation = new TranslateTransition(Duration.seconds(animationDuration), text);
//        scrollAnimation.setByX(-textWidth);
        scrollAnimation.setFromX(WINDOW_WIDTH);
        scrollAnimation.setToX(-textWidth);
        scrollAnimation.setCycleCount(Animation.INDEFINITE);
        scrollAnimation.setAutoReverse(false);
        scrollAnimation.setOnFinished(event -> {
            // 动画结束后重新开始滚动动画
            scrollAnimation.setFromX(WINDOW_WIDTH);
            scrollAnimation.setToX(-textWidth);
            scrollAnimation.play();
        });

        // 开始滚动动画
        scrollAnimation.play();

    }

    public void updateText(String message) {
        Platform.runLater(() -> {
            text.setText(message);
            // 获取新文本的宽度
            Text tempText = new Text(message);
            tempText.setFont(text.getFont());
            double newTextWidth = tempText.getLayoutBounds().getWidth();

            // 停止滚动动画
            scrollAnimation.stop();

            // 计算滚动动画的时间
            double animationDuration = newTextWidth / currentScrollSpeed;
            // 更新滚动动画的起始位置和结束位置
            scrollAnimation.setFromX(WINDOW_WIDTH);
            scrollAnimation.setToX(-newTextWidth);
            scrollAnimation.setDuration(Duration.seconds(animationDuration));

            // 开始滚动动画
            scrollAnimation.play();
        });
    }

}
