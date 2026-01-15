package org.teachfx.antlr4.common.visualization.event.events;

import org.teachfx.antlr4.common.visualization.event.EventType;
import org.teachfx.antlr4.common.visualization.event.VMEvent;

/**
 * 指令执行事件
 */
public class InstructionExecutedEvent extends VMEvent {
    private final int pc;
    private final int opcode;
    private final String mnemonic;
    private final String operands;
    private final int executionTime;
    
    public InstructionExecutedEvent(Object source, int stepNumber, int pc, 
                               int opcode, String mnemonic, String operands, int executionTime) {
        super(source, EventType.INSTRUCTION_EXECUTED, stepNumber);
        this.pc = pc;
        this.opcode = opcode;
        this.mnemonic = mnemonic;
        this.operands = operands;
        this.executionTime = executionTime;
    }
    
    public InstructionExecutedEvent(Object source, int stepNumber, int pc, 
                               int opcode, String mnemonic, String operands) {
        this(source, stepNumber, pc, opcode, mnemonic, operands, 0);
    }
    
    public int getPC() {
        return pc;
    }
    
    public int getOpcode() {
        return opcode;
    }
    
    public String getMnemonic() {
        return mnemonic;
    }
    
    public String getOperands() {
        return operands;
    }
    
    public int getExecutionTime() {
        return executionTime;
    }
    
    @Override
    public String getDescription() {
        return String.format("执行指令: %s %s (PC: %d, Opcode: 0x%02X)", 
                          mnemonic, operands, pc, opcode);
    }
    
    @Override
    public String getDetailedDescription() {
        return String.format("步骤 %d - PC: %d, 指令: %s %s, 操作码: 0x%02X, 执行时间: %dμs", 
                          getStepNumber(), pc, mnemonic, operands, opcode, executionTime);
    }
}