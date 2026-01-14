package org.teachfx.antlr4.ep18r.vizvmr.event;

/**
 * 内存变化事件
 */
public class MemoryChangeEvent extends VMRStateEvent {
    public enum MemoryType {
        HEAP, GLOBAL, STACK
    }

    private final MemoryType memoryType;
    private final int address;
    private final int oldValue;
    private final int newValue;

    public MemoryChangeEvent(Object source, int stepNumber, MemoryType memoryType, int address, int oldValue, int newValue) {
        super(source, stepNumber);
        this.memoryType = memoryType;
        this.address = address;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public MemoryType getMemoryType() {
        return memoryType;
    }

    public int getAddress() {
        return address;
    }

    public int getOldValue() {
        return oldValue;
    }

    public int getNewValue() {
        return newValue;
    }

    @Override
    public String getDescription() {
        return String.format("[%s @ 0x%04X] 0x%08X → 0x%08X", memoryType, address, oldValue, newValue);
    }
}
