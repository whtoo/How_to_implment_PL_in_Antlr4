package org.teachfx.antlr4.common.visualization.event.events;

import org.teachfx.antlr4.common.visualization.event.EventType;
import org.teachfx.antlr4.common.visualization.event.VMEvent;

public class ExecutionFinishedEvent extends VMEvent {
    private final String reason;
    
    public ExecutionFinishedEvent(Object source, int stepNumber, String reason) {
        super(source, EventType.EXECUTION_FINISHED, stepNumber);
        this.reason = reason;
    }
    
    public String getReason() {
        return reason;
    }
    
    @Override
    public String getDescription() {
        return "虚拟机执行完成" + (reason != null ? ": " + reason : "");
    }
    
    @Override
    public String getDetailedDescription() {
        return String.format("步骤 %d - 虚拟机执行完成%s", 
                          getStepNumber(),
                          reason != null ? ": " + reason : "");
    }
}