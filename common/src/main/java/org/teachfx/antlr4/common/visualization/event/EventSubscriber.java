package org.teachfx.antlr4.common.visualization.event;

/**
 * 事件订阅者接口
 * 定义事件处理的基本契约
 */
public interface EventSubscriber<T extends VMEvent> {
    
    /**
     * 处理事件
     */
    void onEvent(T event);
    
    /**
     * 获取订阅的事件类型
     */
    Class<T> getSubscribedEventType();
    
    /**
     * 获取订阅者ID
     */
    String getSubscriberId();
    
    /**
     * 获取源ID过滤条件
     * 返回null表示不过滤源
     */
    String getSourceId();
    
    /**
     * 检查是否应该处理该事件
     */
    default boolean shouldHandle(VMEvent event) {
        if (getSourceId() != null && !getSourceId().equals(event.getSourceId())) {
            return false;
        }
        return getSubscribedEventType().isInstance(event);
    }
    
    /**
     * 事件处理优先级
     * 数值越小优先级越高
     */
    default int getPriority() {
        return 100;
    }
    
    /**
     * 是否异步处理事件
     */
    default boolean isAsync() {
        return false;
    }
}