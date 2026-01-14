package org.teachfx.antlr4.ep18r.vizvmr.event;

/**
 * PC（程序计数器）变化事件
 */
public class PCChangeEvent extends VMRStateEvent {
    private final int oldPC;
    private final int newPC;

    public PCChangeEvent(Object source, int stepNumber, int oldPC, int newPC) {
        super(source, stepNumber);
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
        return String.format("PC: 0x%04X → 0x%04X", oldPC, newPC);
    }
}
