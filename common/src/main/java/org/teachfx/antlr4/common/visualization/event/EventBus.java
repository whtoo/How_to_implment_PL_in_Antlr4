package org.teachfx.antlr4.common.visualization.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 线程安全的事件总线实现
 * 支持事件发布-订阅模式和事件过滤
 */
public class EventBus {
    private final List<EventSubscriber<?>> subscribers;
    private final EventHistory history;
    private volatile boolean running;
    
    public EventBus() {
        this.subscribers = new CopyOnWriteArrayList<>();
        this.history = new EventHistory(1000);
        this.running = false;
    }
    
    /**
     * 启动事件总线
     */
    public synchronized void start() {
        if (running) {
            throw new IllegalStateException("EventBus is already running");
        }
        running = true;
    }
    
    /**
     * 停止事件总线
     */
    public synchronized void stop() {
        if (!running) {
            throw new IllegalStateException("EventBus is not running");
        }
        running = false;
        subscribers.clear();
    }
    
    /**
     * 检查事件总线是否运行中
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * 发布事件
     */
    @SuppressWarnings("unchecked")
    public <T extends VMEvent> void publish(T event) {
        if (!running) {
            return;
        }
        
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        
        try {
            history.addEvent(event);
            
            for (EventSubscriber<?> subscriber : subscribers) {
                if (subscriber.getSubscribedEventType().isInstance(event)) {
                    try {
                        ((EventSubscriber<T>) subscriber).onEvent(event);
                    } catch (Exception e) {
                        System.err.println("Error in event subscriber: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error publishing event: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 订阅事件
     */
    public <T extends VMEvent> void subscribe(EventSubscriber<T> subscriber) {
        if (subscriber == null) {
            throw new IllegalArgumentException("Subscriber cannot be null");
        }
        
        subscribers.add(subscriber);
    }
    
    /**
     * 取消订阅
     */
    public <T extends VMEvent> void unsubscribe(EventSubscriber<T> subscriber) {
        if (subscriber != null) {
            subscribers.remove(subscriber);
        }
    }
    
    /**
     * 取消指定源的所有订阅
     */
    public void unsubscribeBySource(String sourceId) {
        subscribers.removeIf(subscriber -> sourceId.equals(subscriber.getSourceId()));
    }
    
    /**
     * 获取指定类型的订阅者数量
     */
    public int getSubscriberCount(EventType eventType) {
        return (int) subscribers.stream()
            .filter(subscriber -> subscriber.getSubscribedEventType().getName().equals(eventType.name()))
            .count();
    }
    
    /**
     * 获取事件历史
     */
    public EventHistory getHistory() {
        return history;
    }
    
    /**
     * 清空事件历史
     */
    public void clearHistory() {
        history.clear();
    }
    
    /**
     * 获取统计信息
     */
    public EventBusStats getStats() {
        return new EventBusStats(
            subscribers.size(),
            history.getEventCount(),
            history.getEventCountByType(),
            running
        );
    }
}