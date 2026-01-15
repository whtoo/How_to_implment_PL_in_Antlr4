package org.teachfx.antlr4.common.visualization.event;

import java.util.Map;

public record EventBusStats(
    int subscriberCount,
    int totalEvents,
    Map<EventType, Integer> eventCountsByType,
    boolean isRunning
) {
    
    public int getEventCount(EventType eventType) {
        return eventCountsByType.getOrDefault(eventType, 0);
    }
    
    public boolean hasEvents() {
        return totalEvents > 0;
    }
    
    public boolean hasSubscribers() {
        return subscriberCount > 0;
    }
    
    @Override
    public String toString() {
        return String.format(
            "EventBusStats{subscribers=%d, events=%d, running=%s, types=%s}",
            subscriberCount, totalEvents, isRunning, eventCountsByType.keySet()
        );
    }
}