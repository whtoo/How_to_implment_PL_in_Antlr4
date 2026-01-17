package org.teachfx.antlr4.common.visualization.event.events;

import org.teachfx.antlr4.common.visualization.event.EventType;
import org.teachfx.antlr4.common.visualization.event.VMEvent;

public class ExecutionErrorEvent extends VMEvent {
    private final Throwable error;
    
    public ExecutionErrorEvent(Object source, int stepNumber, Throwable error) {
        super(source, EventType.EXECUTION_ERROR, stepNumber);
        this.error = error;
    }
    
    public Throwable getError() {
        return error;
    }
    
    @Override
    public String getDescription() {
        return "虚拟机执行错误: " + (error != null ? error.getMessage() : "未知错误");
    }
    
    @Override
    public String getDetailedDescription() {
        return String.format("步骤 %d - 虚拟机执行错误: %s", 
                          getStepNumber(),
                          error != null ? error.getMessage() : "未知错误");
    }
}