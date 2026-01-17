package org.teachfx.antlr4.common.visualization.event.events;

import org.teachfx.antlr4.common.visualization.event.EventType;
import org.teachfx.antlr4.common.visualization.event.VMEvent;

public class BreakpointHitEvent extends VMEvent {
    private final int pc;
    private final String instruction;
    
    public BreakpointHitEvent(Object source, int stepNumber, int pc, String instruction) {
        super(source, EventType.BREAKPOINT_HIT, stepNumber);
        this.pc = pc;
        this.instruction = instruction;
    }
    
    public int getPC() {
        return pc;
    }
    
    public String getInstruction() {
        return instruction;
    }
    
    @Override
    public String getDescription() {
        return String.format("断点命中: PC=0x%04X, 指令=%s", pc, instruction);
    }
    
    @Override
    public String getDetailedDescription() {
        return String.format("步骤 %d - 断点命中: PC=0x%04X, 指令=%s", 
                          getStepNumber(), pc, instruction);
    }
}