package org.teachfx.antlr4.common.visualization.event.events;

import org.teachfx.antlr4.common.visualization.event.EventType;
import org.teachfx.antlr4.common.visualization.event.VMEvent;

public class ExecutionPausedEvent extends VMEvent {
    public ExecutionPausedEvent(Object source, int stepNumber) {
        super(source, EventType.EXECUTION_PAUSED, stepNumber);
    }
    
    @Override
    public String getDescription() {
        return "虚拟机执行暂停";
    }
    
    @Override
    public String getDetailedDescription() {
        return String.format("步骤 %d - 虚拟机执行暂停", getStepNumber());
    }
}