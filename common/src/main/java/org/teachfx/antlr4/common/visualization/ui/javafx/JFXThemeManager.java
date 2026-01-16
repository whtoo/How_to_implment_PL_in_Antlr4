package org.teachfx.antlr4.common.visualization.ui.javafx;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.event.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;

import java.util.*;
import java.util.function.*;

/**
 * JavaFX主题管理器
 * 提供主题管理和样式应用功能
 */
public class JFXThemeManager {
    
    /**
     * 预定义主题
     */
    public enum Theme {
        LIGHT("Light", "/css/themes/light.css"),
        DARK("Dark", "/css/themes/dark.css"),
        EDUCATIONAL("Educational", "/css/themes/educational.css"),
        HIGH_CONTRAST("High Contrast", "/css/themes/high-contrast.css");
        
        private final String displayName;
        private final String cssPath;
        
        Theme(String displayName, String cssPath) {
            this.displayName = displayName;
            this.cssPath = cssPath;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getCssPath() {
            return cssPath;
        }
    }
    
    // 主题颜色定义
    public static final Color COLOR_LIGHT_BG = Color.web("#FFFFFF");
    public static final Color COLOR_DARK_BG = Color.web("#2D2D2D");
    public static final Color COLOR_LIGHT_TEXT = Color.web("#000000");
    public static final Color COLOR_DARK_TEXT = Color.web("#FFFFFF");
    public static final Color COLOR_ACCENT = Color.web("#3498DB");
    public static final Color COLOR_SUCCESS = Color.web("#27AE60");
    public static final Color COLOR_WARNING = Color.web("#F39C12");
    public static final Color COLOR_ERROR = Color.web("#E74C3C");
    public static final Color COLOR_ZERO_REG = Color.web("#90EE90");
    public static final Color COLOR_MODIFIED_REG = Color.web("#FFB6C1");
    public static final Color COLOR_SPECIAL_REG = Color.web("#ADD8E6");
    public static final Color COLOR_NORMAL_REG = Color.web("#DCDCDC");
    
    private static Theme currentTheme = Theme.LIGHT;
    private static final Set<String> loadedStylesheets = new HashSet<>();
    
    /**
     * 应用主题到场景
     */
    public static void applyTheme(javafx.scene.Scene scene, Theme theme) {
        if (scene == null || theme == null) {
            return;
        }
        
        currentTheme = theme;
        
        // 加载主题CSS
        String cssPath = theme.getCssPath();
        if (!loadedStylesheets.contains(cssPath)) {
            java.net.URL cssUrl = JFXThemeManager.class.getResource(cssPath);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                loadedStylesheets.add(cssPath);
            }
        }
    }
    
    /**
     * 应用主题到节点
     */
    public static void applyTheme(Node node, Theme theme) {
        if (node == null || theme == null) {
            return;
        }
        
        currentTheme = theme;
        
        String cssPath = theme.getCssPath();
        if (!loadedStylesheets.contains(cssPath)) {
            java.net.URL cssUrl = JFXThemeManager.class.getResource(cssPath);
            if (cssUrl != null) {
                // 尝试获取场景的样式表
                javafx.scene.Scene scene = node.getScene();
                if (scene != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                    loadedStylesheets.add(cssPath);
                }
            }
        }
    }
    
    /**
     * 切换主题
     */
    public static void switchTheme(javafx.scene.Scene scene, Theme newTheme) {
        // 移除旧主题
        scene.getStylesheets().clear();
        loadedStylesheets.clear();
        
        // 应用新主题
        applyTheme(scene, newTheme);
    }
    
    /**
     * 获取当前主题
     */
    public static Theme getCurrentTheme() {
        return currentTheme;
    }
    
    /**
     * 获取背景颜色（根据当前主题）
     */
    public static Color getBackgroundColor() {
        return currentTheme == Theme.DARK ? COLOR_DARK_BG : COLOR_LIGHT_BG;
    }
    
    /**
     * 获取文字颜色（根据当前主题）
     */
    public static Color getTextColor() {
        return currentTheme == Theme.DARK ? COLOR_DARK_TEXT : COLOR_LIGHT_TEXT;
    }
    
    /**
     * 应用颜色编码到寄存器面板
     */
    public static void applyRegisterColor(Node cell, int regNum, boolean modified) {
        String color;
        
        if (regNum == 0) {
            color = toHexString(COLOR_ZERO_REG);
        } else if (regNum >= 13) {
            color = toHexString(COLOR_SPECIAL_REG);
        } else if (modified) {
            color = toHexString(COLOR_MODIFIED_REG);
        } else {
            color = toHexString(COLOR_NORMAL_REG);
        }
        
        cell.setStyle(String.format("-fx-background-color: %s;", color));
    }
    
    /**
     * 高亮教育元素
     */
    public static void highlightEducational(Node node, Color highlightColor, int durationMs) {
        String originalStyle = node.getStyle();
        String highlight = String.format(
            "-fx-background-color: %s; -fx-border-color: %s; -fx-border-width: 2px;",
            toHexString(highlightColor),
            toHexString(highlightColor)
        );
        
        node.setStyle(highlight);
        
        // 定时移除高亮
        new Thread(() -> {
            try {
                Thread.sleep(durationMs);
                Platform.runLater(() -> node.setStyle(originalStyle));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    /**
     * 创建脉冲动画
     */
    public static void pulse(Node node) {
        javafx.animation.ScaleTransition transition = 
            new javafx.animation.ScaleTransition(
                javafx.util.Duration.millis(300), node
            );
        transition.setToX(1.1);
        transition.setToY(1.1);
        transition.setAutoReverse(true);
        transition.setCycleCount(2);
        transition.play();
    }
    
    /**
     * 颜色转十六进制字符串
     */
    private static String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255)
        );
    }
    
    /**
     * 创建分隔线
     */
    public static Separator createSeparator() {
        Separator separator = new Separator();
        separator.getStyleClass().add("theme-separator");
        return separator;
    }
    
    /**
     * 创建工具栏按钮
     */
    public static Button createToolBarButton(String text, String tooltip, EventHandler<ActionEvent> handler) {
        Button button = new Button(text);
        button.setTooltip(new Tooltip(tooltip));
        button.setOnAction(handler);
        button.getStyleClass().add("toolbar-button");
        return button;
    }
    
    /**
     * 创建状态标签
     */
    public static Label createStatusLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("status-label");
        return label;
    }
}
