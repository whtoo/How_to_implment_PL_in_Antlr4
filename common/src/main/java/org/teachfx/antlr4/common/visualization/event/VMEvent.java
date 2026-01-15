package org.teachfx.antlr4.common.visualization.event;

import java.util.EventObject;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 统一事件基类
 * 所有可视化框架事件的基础类
 */
public abstract class VMEvent extends EventObject {
    private static final AtomicLong EVENT_ID_GENERATOR = new AtomicLong(0);
    
    private final long eventId;
    private final EventType eventType;
    private final long timestamp;
    private final int stepNumber;
    private final String sourceId;
    
    protected VMEvent(Object source, EventType eventType, int stepNumber, String sourceId) {
        super(source);
        this.eventId = EVENT_ID_GENERATOR.incrementAndGet();
        this.eventType = eventType;
        this.timestamp = System.currentTimeMillis();
        this.stepNumber = stepNumber;
        this.sourceId = sourceId != null ? sourceId : source.getClass().getSimpleName();
    }
    
    protected VMEvent(Object source, EventType eventType, int stepNumber) {
        this(source, eventType, stepNumber, null);
    }
    
    public long getEventId() {
        return eventId;
    }
    
    public EventType getEventType() {
        return eventType;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public int getStepNumber() {
        return stepNumber;
    }
    
    public String getSourceId() {
        return sourceId;
    }
    
    /**
     * 获取事件的简要描述
     */
    public abstract String getDescription();
    
    /**
     * 获取事件的详细信息
     */
    public String getDetailedDescription() {
        return getDescription();
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s - %s", 
            eventType, 
            sourceId, 
            getDescription());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        VMEvent vmEvent = (VMEvent) obj;
        return eventId == vmEvent.eventId;
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(eventId);
    }
}