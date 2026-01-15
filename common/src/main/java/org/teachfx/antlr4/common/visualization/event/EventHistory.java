package org.teachfx.antlr4.common.visualization.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 事件历史记录器
 * 支持事件回放和分析
 */
public class EventHistory {
    private int maxEvents;
    private final List<VMEvent> events;
    private final Map<EventType, Integer> eventTypeCounts;
    
    public EventHistory(int maxEvents) {
        this.maxEvents = maxEvents > 0 ? maxEvents : 1000;
        this.events = new CopyOnWriteArrayList<>();
        this.eventTypeCounts = new ConcurrentHashMap<>();
    }
    
    /**
     * 添加事件到历史记录
     */
    public synchronized void addEvent(VMEvent event) {
        if (event == null) {
            return;
        }
        
        events.add(event);
        eventTypeCounts.merge(event.getEventType(), 1, Integer::sum);
        
        while (events.size() > maxEvents) {
            VMEvent removed = events.remove(0);
            eventTypeCounts.computeIfPresent(removed.getEventType(), 
                (type, count) -> count > 1 ? count - 1 : null);
        }
    }
    
    /**
     * 获取所有事件
     */
    public List<VMEvent> getAllEvents() {
        return new CopyOnWriteArrayList<>(events);
    }
    
    /**
     * 获取指定类型的事件
     */
    public List<VMEvent> getEventsByType(EventType eventType) {
        return events.stream()
            .filter(event -> event.getEventType() == eventType)
            .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
    }
    
    /**
     * 获取指定源的事件
     */
    public List<VMEvent> getEventsBySource(String sourceId) {
        return events.stream()
            .filter(event -> sourceId.equals(event.getSourceId()))
            .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
    }
    
    /**
     * 获取指定步骤范围的事件
     */
    public List<VMEvent> getEventsByStepRange(int startStep, int endStep) {
        return events.stream()
            .filter(event -> event.getStepNumber() >= startStep && 
                              event.getStepNumber() <= endStep)
            .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
    }
    
    /**
     * 获取最近的事件
     */
    public List<VMEvent> getRecentEvents(int count) {
        int size = events.size();
        int fromIndex = Math.max(0, size - count);
        return new CopyOnWriteArrayList<>(events.subList(fromIndex, size));
    }
    
    /**
     * 获取事件总数
     */
    public int getEventCount() {
        return events.size();
    }
    
    /**
     * 获取指定类型的事件数量
     */
    public int getEventCount(EventType eventType) {
        return eventTypeCounts.getOrDefault(eventType, 0);
    }
    
    /**
     * 获取所有类型的事件数量
     */
    public Map<EventType, Integer> getEventCountByType() {
        return new ConcurrentHashMap<>(eventTypeCounts);
    }
    
    /**
     * 清空历史记录
     */
    public synchronized void clear() {
        events.clear();
        eventTypeCounts.clear();
    }
    
    /**
     * 设置最大事件数量
     */
    public synchronized void setMaxEvents(int maxEvents) {
        while (events.size() > maxEvents) {
            VMEvent removed = events.remove(0);
            eventTypeCounts.computeIfPresent(removed.getEventType(), 
                (type, count) -> count > 1 ? count - 1 : null);
        }
        this.maxEvents = maxEvents;
    }
    
    /**
     * 获取最大事件数量
     */
    public int getMaxEvents() {
        return maxEvents;
    }
}