package org.teachfx.antlr4.common.visualization.event.events;

import org.teachfx.antlr4.common.visualization.VMState;
import org.teachfx.antlr4.common.visualization.event.EventType;
import org.teachfx.antlr4.common.visualization.event.VMEvent;

public class ExecutionStateChangedEvent extends VMEvent {
    private final VMState.ExecutionState oldState;
    private final VMState.ExecutionState newState;
    
    public ExecutionStateChangedEvent(Object source, int stepNumber, 
                                      VMState.ExecutionState oldState, 
                                      VMState.ExecutionState newState) {
        super(source, EventType.EXECUTION_STATE_CHANGED, stepNumber);
        this.oldState = oldState;
        this.newState = newState;
    }
    
    public VMState.ExecutionState getOldState() {
        return oldState;
    }
    
    public VMState.ExecutionState getNewState() {
        return newState;
    }
    
    @Override
    public String getDescription() {
        return String.format("执行状态: %s → %s", 
                          oldState != null ? oldState.name() : "null",
                          newState != null ? newState.name() : "null");
    }
    
    @Override
    public String getDetailedDescription() {
        return String.format("步骤 %d - 执行状态变化: %s → %s", 
                          getStepNumber(),
                          oldState != null ? oldState.name() : "null",
                          newState != null ? newState.name() : "null");
    }
}