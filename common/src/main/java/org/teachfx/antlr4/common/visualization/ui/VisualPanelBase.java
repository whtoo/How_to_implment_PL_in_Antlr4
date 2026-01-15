package org.teachfx.antlr4.common.visualization.ui;

import org.teachfx.antlr4.common.visualization.event.EventBus;
import org.teachfx.antlr4.common.visualization.event.EventSubscriber;
import org.teachfx.antlr4.common.visualization.event.VMEvent;

import javax.swing.*;
import java.awt.*;

/**
 * 可视化面板基类
 * 所有可视化面板的基础类
 */
public abstract class VisualPanelBase extends JPanel {
    private final String panelId;
    private final EventBus eventBus;
    private final DataBinding dataBinding;
    private final ThemeManager themeManager;
    private volatile boolean initialized;
    private volatile boolean updating;
    
    protected VisualPanelBase(String panelId, EventBus eventBus) {
        super(new BorderLayout());
        this.panelId = panelId != null ? panelId : this.getClass().getSimpleName();
        this.eventBus = eventBus;
        this.dataBinding = new DataBinding(this);
        this.themeManager = new ThemeManager();
        this.initialized = false;
        this.updating = false;
        
        initializePanel();
    }
    
    /**
     * 初始化面板
     */
    private void initializePanel() {
        setBorder(BorderFactory.createTitledBorder(getPanelTitle()));
        setPreferredSize(getPreferredPanelSize());
        setBackground(getThemeManager().getBackgroundColor());
        
        setupEventSubscriptions();
        initializeComponents();
        setupDataBindings();
        
        initialized = true;
    }
    
    /**
     * 设置事件订阅
     */
    protected abstract void setupEventSubscriptions();
    
    /**
     * 初始化组件
     */
    protected abstract void initializeComponents();
    
    /**
     * 设置数据绑定
     */
    protected abstract void setupDataBindings();
    
    /**
     * 获取面板标题
     */
    protected abstract String getPanelTitle();
    
    /**
     * 获取面板首选尺寸
     */
    protected Dimension getPreferredPanelSize() {
        return new Dimension(400, 300);
    }
    
    /**
     * 安全更新UI
     */
    protected final void safeUpdateUI(Runnable updateAction) {
        if (SwingUtilities.isEventDispatchThread()) {
            updateAction.run();
        } else {
            SwingUtilities.invokeLater(updateAction);
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
     * 添加事件订阅者
     */
    protected final <T extends VMEvent> void addEventSubscriber(EventSubscriber<T> subscriber) {
        eventBus.subscribe(subscriber);
    }
    
    /**
     * 移除事件订阅者
     */
    protected final <T extends VMEvent> void removeEventSubscriber(EventSubscriber<T> subscriber) {
        eventBus.unsubscribe(subscriber);
    }
    
    /**
     * 发布事件
     */
    protected final <T extends VMEvent> void publishEvent(T event) {
        eventBus.publish(event);
    }
    
    /**
     * 高亮教育元素
     */
    public final void highlightEducationalElement(String elementId, Color highlightColor) {
        safeUpdateUI(() -> {
            JComponent component = findEducationalComponent(elementId);
            if (component != null) {
                component.setBackground(highlightColor);
                component.setBorder(BorderFactory.createLineBorder(highlightColor, 2));
                
                Timer timer = new Timer(2000, e -> {
                    component.setBackground(getThemeManager().getBackgroundColor());
                    component.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
                });
                timer.setRepeats(false);
                timer.start();
            }
        });
    }
    
    /**
     * 查找教育组件
     */
    protected abstract JComponent findEducationalComponent(String elementId);
    
    /**
     * 刷新面板
     */
    public void refresh() {
        safeUpdateUI(this::repaint);
    }
    
    /**
     * 重置面板状态
     */
    public void reset() {
        safeUpdateUI(() -> {
            removeAll();
            initializeComponents();
            setupDataBindings();
            revalidate();
            repaint();
        });
    }
    
    /**
     * 清理资源
     */
    public void cleanup() {
        if (eventBus != null) {
            eventBus.unsubscribeBySource(panelId);
        }
        dataBinding.cleanup();
    }
    
    public String getPanelId() {
        return panelId;
    }
    
    public EventBus getEventBus() {
        return eventBus;
    }
    
    public DataBinding getDataBinding() {
        return dataBinding;
    }
    
    public ThemeManager getThemeManager() {
        return themeManager;
    }
    
    public boolean isInitialized() {
        return initialized;
    }
    
    public boolean isUpdating() {
        return updating;
    }
}