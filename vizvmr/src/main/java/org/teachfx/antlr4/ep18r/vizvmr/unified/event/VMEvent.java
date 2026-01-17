package org.teachfx.antlr4.ep18r.vizvmr.unified.event;

import org.teachfx.antlr4.ep18r.vizvmr.unified.core.VMTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 统一虚拟机事件基类
 *
 * <p>所有VM事件都继承自此类，提供：</p>
 * <ul>
 *   <li>唯一事件ID</li>
 *   <li>时间戳</li>
 *   <li>事件类型</li>
 *   <li>额外数据</li>
 * </ul>
 */
public abstract class VMEvent {

    private final String eventId;
    private final Instant timestamp;
    private final VMEventType eventType;
    private final Map<String, Object> metadata;

    protected VMEvent(VMEventType eventType) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.eventType = eventType;
        this.metadata = new HashMap<>();
    }

    protected VMEvent(VMEventType eventType, Map<String, Object> metadata) {
        this(eventType);
        if (metadata != null) {
            this.metadata.putAll(metadata);
        }
    }

    public String getEventId() {
        return eventId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public VMEventType getEventType() {
        return eventType;
    }

    public Map<String, Object> getMetadata() {
        return new HashMap<>(metadata);
    }

    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    public Object getMetadata(String key) {
        return metadata.get(key);
    }

    /**
     * 事件类型枚举
     */
    public enum VMEventType {
        // 状态事件
        STATE_CHANGED,
        PC_CHANGED,
        REGISTER_CHANGED,
        MEMORY_CHANGED,
        STACK_CHANGED,

        // 执行事件
        INSTRUCTION_FETCHED,
        INSTRUCTION_EXECUTED,
        EXECUTION_STARTED,
        EXECUTION_PAUSED,
        EXECUTION_RESUMED,
        EXECUTION_STOPPED,
        EXECUTION_ERROR,

        // 断点事件
        BREAKPOINT_SET,
        BREAKPOINT_CLEARED,
        BREAKPOINT_HIT,

        // 命令事件
        COMMAND_EXECUTED,
        COMMAND_FAILED
    }

    /**
     * 状态变化事件
     */
    public static class StateChangedEvent extends VMEvent {
        private final VMTypes.VMState oldState;
        private final VMTypes.VMState newState;

        public StateChangedEvent(VMTypes.VMState oldState,
                                VMTypes.VMState newState) {
            super(VMEventType.STATE_CHANGED);
            this.oldState = oldState;
            this.newState = newState;
            addMetadata("oldState", oldState);
            addMetadata("newState", newState);
        }

        public VMTypes.VMState getOldState() { return oldState; }
        public VMTypes.VMState getNewState() { return newState; }
    }

    /**
     * PC变化事件
     */
    public static class PCChangedEvent extends VMEvent {
        private final int oldPC;
        private final int newPC;

        public PCChangedEvent(int oldPC, int newPC) {
            super(VMEventType.PC_CHANGED);
            this.oldPC = oldPC;
            this.newPC = newPC;
            addMetadata("oldPC", oldPC);
            addMetadata("newPC", newPC);
        }

        public int getOldPC() { return oldPC; }
        public int getNewPC() { return newPC; }
    }

    /**
     * 寄存器变化事件
     */
    public static class RegisterChangedEvent extends VMEvent {
        private final int regNum;
        private final int oldValue;
        private final int newValue;

        public RegisterChangedEvent(int regNum, int oldValue, int newValue) {
            super(VMEventType.REGISTER_CHANGED);
            this.regNum = regNum;
            this.oldValue = oldValue;
            this.newValue = newValue;
            addMetadata("regNum", regNum);
            addMetadata("oldValue", oldValue);
            addMetadata("newValue", newValue);
        }

        public int getRegNum() { return regNum; }
        public int getOldValue() { return oldValue; }
        public int getNewValue() { return newValue; }
    }

    /**
     * 内存变化事件
     */
    public static class MemoryChangedEvent extends VMEvent {
        public enum MemoryType {
            HEAP,
            GLOBAL
        }

        private final MemoryType memoryType;
        private final int address;
        private final int oldValue;
        private final int newValue;

        public MemoryChangedEvent(MemoryType memoryType, int address,
                                int oldValue, int newValue) {
            super(VMEventType.MEMORY_CHANGED);
            this.memoryType = memoryType;
            this.address = address;
            this.oldValue = oldValue;
            this.newValue = newValue;
            addMetadata("memoryType", memoryType);
            addMetadata("address", address);
            addMetadata("oldValue", oldValue);
            addMetadata("newValue", newValue);
        }

        public MemoryType getMemoryType() { return memoryType; }
        public int getAddress() { return address; }
        public int getOldValue() { return oldValue; }
        public int getNewValue() { return newValue; }
    }

    /**
     * 指令执行事件
     */
    public static class InstructionExecutedEvent extends VMEvent {
        private final int pc;
        private final int opcode;
        private final String mnemonic;
        private final String operands;

        public InstructionExecutedEvent(int pc, int opcode, String mnemonic, String operands) {
            super(VMEventType.INSTRUCTION_EXECUTED);
            this.pc = pc;
            this.opcode = opcode;
            this.mnemonic = mnemonic;
            this.operands = operands;
            addMetadata("pc", pc);
            addMetadata("opcode", opcode);
            addMetadata("mnemonic", mnemonic);
            addMetadata("operands", operands);
        }

        public int getPC() { return pc; }
        public int getOpcode() { return opcode; }
        public String getMnemonic() { return mnemonic; }
        public String getOperands() { return operands; }
    }

    /**
     * 断点命中事件
     */
    public static class BreakpointHitEvent extends VMEvent {
        private final int pc;

        public BreakpointHitEvent(int pc) {
            super(VMEventType.BREAKPOINT_HIT);
            this.pc = pc;
            addMetadata("pc", pc);
        }

        public int getPC() { return pc; }
    }
}
