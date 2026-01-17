package org.teachfx.antlr4.common.visualization.event.events;

import org.teachfx.antlr4.common.visualization.event.EventType;
import org.teachfx.antlr4.common.visualization.event.VMEvent;

/**
 * 寄存器变化事件
 */
public class RegisterChangedEvent extends VMEvent {
    private final int registerNumber;
    private final Object oldValue;
    private final Object newValue;
    
    public RegisterChangedEvent(Object source, int stepNumber, int registerNumber, 
                                Object oldValue, Object newValue) {
        super(source, EventType.REGISTER_CHANGED, stepNumber);
        this.registerNumber = registerNumber;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
    
    public RegisterChangedEvent(Object source, int stepNumber, int registerNumber, 
                                int oldValue, int newValue) {
        this(source, stepNumber, registerNumber, Integer.valueOf(oldValue), Integer.valueOf(newValue));
    }
    
    public int getRegisterNumber() {
        return registerNumber;
    }
    
    public Object getOldValue() {
        return oldValue;
    }
    
    public Object getNewValue() {
        return newValue;
    }
    
    public int getRegisterNumberAsInt() {
        return registerNumber;
    }
    
    public int getOldValueAsInt() {
        return oldValue instanceof Number ? ((Number) oldValue).intValue() : 0;
    }
    
    public int getNewValueAsInt() {
        return newValue instanceof Number ? ((Number) newValue).intValue() : 0;
    }
    
    @Override
    public String getDescription() {
        return String.format("寄存器 r%d: %s → %s", 
                          registerNumber, 
                          formatValue(oldValue),
                          formatValue(newValue));
    }
    
    @Override
    public String getDetailedDescription() {
        return String.format("步骤 %d - 寄存器 r%d 变化: %s → %s", 
                          getStepNumber(),
                          registerNumber,
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
        } else {
            return String.valueOf(value);
        }
    }
}