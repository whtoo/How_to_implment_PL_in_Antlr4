package org.teachfx.antlr4.common.visualization.event.events;

import org.teachfx.antlr4.common.visualization.event.EventType;
import org.teachfx.antlr4.common.visualization.event.VMEvent;

public class ExecutionStartedEvent extends VMEvent {
    public ExecutionStartedEvent(Object source, int stepNumber) {
        super(source, EventType.EXECUTION_STARTED, stepNumber);
    }
    
    @Override
    public String getDescription() {
        return "虚拟机执行开始";
    }
    
    @Override
    public String getDetailedDescription() {
        return String.format("步骤 %d - 虚拟机执行开始", getStepNumber());
    }
}