package org.teachfx.antlr4.common.visualization.event.events;

import org.teachfx.antlr4.common.visualization.event.EventType;
import org.teachfx.antlr4.common.visualization.event.VMEvent;

/**
 * 内存变化事件
 */
public class MemoryChangedEvent extends VMEvent {
    public enum MemoryType {
        HEAP, GLOBAL, STACK, CODE
    }
    
    private final MemoryType memoryType;
    private final int address;
    private final Object oldValue;
    private final Object newValue;
    
    public MemoryChangedEvent(Object source, int stepNumber, MemoryType memoryType, 
                              int address, Object oldValue, Object newValue) {
        super(source, EventType.MEMORY_CHANGED, stepNumber);
        this.memoryType = memoryType;
        this.address = address;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
    
    public MemoryChangedEvent(Object source, int stepNumber, MemoryType memoryType, 
                              int address, int oldValue, int newValue) {
        this(source, stepNumber, memoryType, address, Integer.valueOf(oldValue), Integer.valueOf(newValue));
    }
    
    public MemoryType getMemoryType() {
        return memoryType;
    }
    
    public int getAddress() {
        return address;
    }
    
    public Object getOldValue() {
        return oldValue;
    }
    
    public Object getNewValue() {
        return newValue;
    }
    
    public int getOldValueAsInt() {
        return oldValue instanceof Number ? ((Number) oldValue).intValue() : 0;
    }
    
    public int getNewValueAsInt() {
        return newValue instanceof Number ? ((Number) newValue).intValue() : 0;
    }
    
    @Override
    public String getDescription() {
        return String.format("[%s @ 0x%04X] %s → %s", 
                          memoryType, address,
                          formatValue(oldValue),
                          formatValue(newValue));
    }
    
    @Override
    public String getDetailedDescription() {
        return String.format("步骤 %d - %s内存地址 0x%04X: %s → %s", 
                          getStepNumber(),
                          memoryType,
                          address,
                          formatValue(oldValue),
                          formatValue(newValue));
    }
    
    private String formatValue(Object value) {
        if (value instanceof Integer) {
            return String.format("0x%08X (%d)", (Integer) value, (Integer) value);
        } else if (value instanceof Long) {
            return String.format("0x%016X (%d)", (Long) value, (Long) value);
        } else if (value instanceof Float) {
            return String.format("%.6f", (Float) value);
        } else if (value instanceof Double) {
            return String.format("%.12f", (Double) value);
        } else if (value instanceof byte[]) {
            byte[] bytes = (byte[]) value;
            return String.format("[%d bytes]", bytes.length);
        } else if (value instanceof String) {
            return "\"" + value + "\"";
        } else {
            return String.valueOf(value);
        }
    }
}