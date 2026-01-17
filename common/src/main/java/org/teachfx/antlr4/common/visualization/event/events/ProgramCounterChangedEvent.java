package org.teachfx.antlr4.common.visualization.event.events;

import org.teachfx.antlr4.common.visualization.event.EventType;
import org.teachfx.antlr4.common.visualization.event.VMEvent;

public class ProgramCounterChangedEvent extends VMEvent {
    private final int oldPC;
    private final int newPC;
    
    public ProgramCounterChangedEvent(Object source, int stepNumber, int oldPC, int newPC) {
        super(source, EventType.PROGRAM_COUNTER_CHANGED, stepNumber);
        this.oldPC = oldPC;
        this.newPC = newPC;
    }
    
    public int getOldPC() {
        return oldPC;
    }
    
    public int getNewPC() {
        return newPC;
    }
    
    @Override
    public String getDescription() {
        return String.format("程序计数器: 0x%04X → 0x%04X", oldPC, newPC);
    }
    
    @Override
    public String getDetailedDescription() {
        return String.format("步骤 %d - 程序计数器变化: 0x%04X → 0x%04X", 
                          getStepNumber(), oldPC, newPC);
    }
}