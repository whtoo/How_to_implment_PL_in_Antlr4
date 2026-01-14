package org.teachfx.antlr4.ep18r.vizvmr.event;

/**
 * 寄存器变化事件
 */
public class RegisterChangeEvent extends VMRStateEvent {
    private final int registerNumber;
    private final int oldValue;
    private final int newValue;

    public RegisterChangeEvent(Object source, int stepNumber, int registerNumber, int oldValue, int newValue) {
        super(source, stepNumber);
        this.registerNumber = registerNumber;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public int getRegisterNumber() {
        return registerNumber;
    }

    public int getOldValue() {
        return oldValue;
    }

    public int getNewValue() {
        return newValue;
    }

    public int getChangedValue() {
        return newValue;
    }

    @Override
    public String getDescription() {
        return String.format("r%d: 0x%08X → 0x%08X", registerNumber, oldValue, newValue);
    }
}
