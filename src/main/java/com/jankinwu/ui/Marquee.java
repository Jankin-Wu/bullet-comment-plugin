package com.jankinwu.ui;

import com.alibaba.fastjson2.JSONObject;
import com.jankinwu.dto.PushMsgDTO;
import com.jankinwu.ws.WebSocketClient;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
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

    private static final double WINDOW_WIDTH = 800;
    private static final double WINDOW_HEIGHT = 60;
    private static final double FONT_SIZE = 40;

    private static final double AVATAR_SIZE = FONT_SIZE * 1.5; // 调整头像大小
    private double xOffset = 0;
    private double yOffset = 0;
    private Text text;

    private static WebSocketClient webSocketClient;

    private TranslateTransition scrollAnimation;

    private static final double SCROLL_SPEED_RATIO = 6; // Adjust this value to change the scroll speed
    private double currentScrollSpeed = WINDOW_WIDTH * SCROLL_SPEED_RATIO / 100;
    private ImageView avatarImageView;

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
        Circle clipCircle = new Circle(AVATAR_SIZE / 2);
        clipCircle.setCenterX(AVATAR_SIZE / 2);
        clipCircle.setCenterY(AVATAR_SIZE / 2);

        // 加载头像图片
        Image avatarImage = new Image("http://oss.jankinwu.com/img/0738ccea15ce36d3623d8d2c3df33a87e950b17a.gif");

        // 创建头像框并设置图片
        avatarImageView = new ImageView(avatarImage);
        avatarImageView.setFitWidth(AVATAR_SIZE);
        avatarImageView.setFitHeight(AVATAR_SIZE);
        avatarImageView.setPreserveRatio(true);
        avatarImageView.setClip(clipCircle);
        avatarImageView.setTranslateX( -AVATAR_SIZE);

        // 将文本节点和头像节点放置在容器内
        StackPane textPane = new StackPane(avatarImageView, text);
        textPane.setAlignment(Pos.CENTER_LEFT);
        textPane.setTranslateX(FONT_SIZE); // 设置文本的偏移量

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
            webSocketClient = new WebSocketClient("ws://localhost:8080/websocket/plugin/1");
            webSocketClient.setTextUpdater(this::updateText);
        } catch (Exception e) {
            updateText(assembleMsg(e.getMessage(), "http://oss.jankinwu.com/img/3cd4202dd42a2834867639175cb5c9ea14cebf63.gif"));
        }
        // 获取文本的宽度
        Text tempText = new Text(text.getText());
        tempText.setFont(text.getFont());
        double textWidth = text.getLayoutBounds().getWidth();
        // 计算滚动动画的持续时间
        double animationDuration = textWidth / currentScrollSpeed;
        // 创建平移动画，使文本节点水平滚动
        scrollAnimation = new TranslateTransition(Duration.seconds(animationDuration), textPane);
//        scrollAnimation.setByX(-textWidth);
        scrollAnimation.setFromX(WINDOW_WIDTH + AVATAR_SIZE);
        scrollAnimation.setToX(-textWidth);
        scrollAnimation.setCycleCount(Animation.INDEFINITE);
        scrollAnimation.setAutoReverse(false);
        scrollAnimation.setOnFinished(event -> {
            // 动画结束后重新开始滚动动画
            scrollAnimation.setFromX(WINDOW_WIDTH + AVATAR_SIZE);
            scrollAnimation.setToX(-textWidth);
            scrollAnimation.play();
        });

        // 开始滚动动画
        scrollAnimation.play();

    }

    @Override
    public void stop() throws Exception {
        // 调用Platform.exit()退出JavaFX应用
        Platform.exit();
        // 使用System.exit(0)退出整个应用程序
        System.exit(0);
    }

    public void updateText(String message) {
        Platform.runLater(() -> {
            JSONObject jsonObject = JSONObject.parseObject(message);

            // Update text content
            String newText = jsonObject.getString("text");
            text.setText(newText);

            // Update text style
            double newFontSize = jsonObject.getDouble("fontSize");
            String newFill = jsonObject.getString("fill");
            String newStroke = jsonObject.getString("stroke");
            String newFontFamily = jsonObject.getString("fontFamily");
            FontWeight newFontWeight = FontWeight.BOLD; // Assuming all text should be bold
            Font newFont = Font.font(newFontFamily, newFontWeight, newFontSize);
            text.setFont(newFont);
            text.setFill(Color.web(newFill));
            text.setStroke(Color.web(newStroke));

            // Update avatar image
            String newAvatarUrl = jsonObject.getString("avatarUrl");
            Image newAvatarImage = new Image(newAvatarUrl);
            avatarImageView.setImage(newAvatarImage);

            // 获取新文本的宽度
            Text tempText = new Text(newText);
            tempText.setFont(newFont);
            double newTextWidth = tempText.getLayoutBounds().getWidth();

            // 停止滚动动画
            scrollAnimation.stop();

            // 计算滚动动画的时间
            double animationDuration = newTextWidth / currentScrollSpeed;
            // 更新滚动动画的起始位置和结束位置
            scrollAnimation.setFromX(WINDOW_WIDTH + AVATAR_SIZE);
            scrollAnimation.setToX(-newTextWidth);
            scrollAnimation.setDuration(Duration.seconds(animationDuration));

            // 开始滚动动画
            scrollAnimation.play();
        });
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

    public static void main(String[] args) {
        launch(Marquee.class);
    }

}
