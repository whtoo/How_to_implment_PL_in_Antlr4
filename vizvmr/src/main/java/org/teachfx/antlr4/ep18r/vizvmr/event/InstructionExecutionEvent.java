package org.teachfx.antlr4.ep18r.vizvmr.event;

/**
 * 指令执行事件
 */
public class InstructionExecutionEvent extends VMRStateEvent {
    private final int pc;
    private final int opcode;
    private final String mnemonic;
    private final String operands;

    public InstructionExecutionEvent(Object source, int stepNumber, int pc, int opcode, String mnemonic, String operands) {
        super(source, stepNumber);
        this.pc = pc;
        this.opcode = opcode;
        this.mnemonic = mnemonic;
        this.operands = operands;
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

    public String getFullInstruction() {
        if (operands != null && !operands.isEmpty()) {
            return String.format("%s %s", mnemonic, operands);
        }
        return mnemonic;
    }

    @Override
    public String getDescription() {
        return String.format("[0x%04X] %s", pc, getFullInstruction());
    }
}
