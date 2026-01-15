package org.teachfx.antlr4.common.visualization.event;

/**
 * 事件发布者接口
 * 定义事件发布的基本契约
 */
public interface EventPublisher {
    
    /**
     * 发布事件
     */
    <T extends VMEvent> void publish(T event);
    
    /**
     * 获取发布者ID
     */
    String getPublisherId();
    
    /**
     * 检查是否启用发布
     */
    default boolean isEnabled() {
        return true;
    }
    
    /**
     * 获取事件总线
     */
    EventBus getEventBus();
    
    /**
     * 设置事件总线
     */
    void setEventBus(EventBus eventBus);
}