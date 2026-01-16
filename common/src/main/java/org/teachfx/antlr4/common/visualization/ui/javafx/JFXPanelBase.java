package org.teachfx.antlr4.common.visualization.ui.javafx;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;

/**
 * JavaFX面板基类
 * 提供统一的生命周期管理和线程安全机制
 */
public abstract class JFXPanelBase extends BorderPane {
    
    protected final String panelId;
    protected volatile boolean initialized;
    protected volatile boolean updating;
    
    // 主题相关属性
    private final SimpleObjectProperty<javafx.scene.paint.Color> backgroundColor = 
        new SimpleObjectProperty<>(javafx.scene.paint.Color.web("#F5F5F5"));
    private final StringProperty title = new SimpleStringProperty("Panel");
    
    public JFXPanelBase(String panelId) {
        this.panelId = panelId != null ? panelId : this.getClass().getSimpleName();
        this.initialized = false;
        this.updating = false;

        // 注意：不在构造函数中调用 initializePanel()，避免子类字段未初始化问题
        // 子类应在构造函数中调用 buildUI() 来完成初始化
    }

    /**
     * 构建UI组件 - 在对象完全构造后调用
     * 子类必须在构造函数中调用此方法
     */
    protected void buildUI() {
        initializePanel();
    }

    /**
     * 初始化面板（内部方法）
     */
    private void initializePanel() {
        setId(panelId);
        setPrefSize(getPreferredWidth(), getPreferredHeight());

        // 应用默认样式
        getStyleClass().add("jfx-panel");

        // 初始化组件
        initializeComponents();

        // 应用主题
        applyTheme();

        initialized = true;
    }
    
    /**
     * 初始化组件（子类实现）
     */
    protected abstract void initializeComponents();
    
    /**
     * 获取首选宽度（默认400）
     */
    protected double getPreferredWidth() {
        return 400;
    }
    
    /**
     * 获取首选高度（默认300）
     */
    protected double getPreferredHeight() {
        return 300;
    }
    
    /**
     * 线程安全的UI更新
     */
    protected final void safeUpdateUI(Runnable updateAction) {
        if (Platform.isFxApplicationThread()) {
            updateAction.run();
        } else {
            Platform.runLater(updateAction);
        }
    }
    
    /**
     * 批量更新UI
     */
    protected final void batchUpdate(Runnable... updates) {
        if (updating) {
            return;
        }
        
        updating = true;
        try {
            safeUpdateUI(() -> {
                for (Runnable update : updates) {
                    update.run();
                }
            });
        } finally {
            updating = false;
        }
    }
    
    /**
     * 应用主题
     */
    private void applyTheme() {
        String css = getClass().getResource("/css/themes/light.css").toExternalForm();
        if (css != null) {
            getStylesheets().add(css);
        }
    }
    
    /**
     * 刷新面板
     */
    public void refresh() {
        safeUpdateUI(this::requestLayout);
    }
    
    /**
     * 重置面板状态
     */
    public void reset() {
        safeUpdateUI(() -> {
            getChildren().clear();
            initializeComponents();
            requestLayout();
        });
    }
    
    /**
     * 设置面板标题
     */
    public void setTitle(String newTitle) {
        title.set(newTitle);
    }
    
    public String getTitle() {
        return title.get();
    }
    
    public StringProperty titleProperty() {
        return title;
    }
    
    /**
     * 设置背景颜色
     */
    public void setBackgroundColor(javafx.scene.paint.Color color) {
        backgroundColor.set(color);
        setStyle(String.format("-fx-background-color: %s;", toHexString(color)));
    }
    
    private String toHexString(javafx.scene.paint.Color color) {
        return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255)
        );
    }
    
    // Getters
    public String getPanelId() {
        return panelId;
    }
    
    public boolean isInitialized() {
        return initialized;
    }
}
