package org.teachfx.antlr4.ep18r.vizvmr.core;

import org.teachfx.antlr4.ep18r.stackvm.StackFrame;
import org.teachfx.antlr4.ep18r.vizvmr.event.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 虚拟机状态模型
 * 管理寄存器、内存、调用栈等状态，并提供事件通知机制
 */
public class VMRStateModel {
    // 常量定义
    private static final int NUM_REGISTERS = 16;

    // 内部状态
    private final int[] registers;
    private final int[] heap;
    private final int[] globals;
    private final StackFrame[] callStack;
    private int framePointer;
    private int programCounter;
    private int heapAllocPointer;

    // 修改追踪
    private final boolean[] registerModified;
    private final Set<Integer> modifiedMemoryAddresses;
    private final Set<Integer> modifiedHeapAddresses;

    // 监听器
    private final List<VMRStateListener> stateListeners;
    private final List<VMRExecutionListener> executionListeners;

    // 执行状态
    private volatile VMStateChangeEvent.State vmState;
    private long executionSteps;
    private long startTime;

    // 事件步数计数器
    private int eventStepNumber;

    // 构造函数
    public VMRStateModel(int heapSize, int globalsSize, int maxCallStackDepth) {
        this.registers = new int[NUM_REGISTERS];
        this.heap = new int[heapSize];
        this.globals = new int[globalsSize];
        this.callStack = new StackFrame[maxCallStackDepth];
        this.framePointer = -1;
        this.programCounter = 0;
        this.heapAllocPointer = 0;

        this.registerModified = new boolean[NUM_REGISTERS];
        this.modifiedMemoryAddresses = new HashSet<>();
        this.modifiedHeapAddresses = new HashSet<>();

        this.stateListeners = new CopyOnWriteArrayList<>();
        this.executionListeners = new CopyOnWriteArrayList<>();

        this.vmState = VMStateChangeEvent.State.CREATED;
        this.executionSteps = 0;
        this.eventStepNumber = 0;
    }

    // ==================== 监听器管理 ====================

    public void addStateListener(VMRStateListener listener) {
        if (listener != null && !stateListeners.contains(listener)) {
            stateListeners.add(listener);
        }
    }

    public void removeStateListener(VMRStateListener listener) {
        stateListeners.remove(listener);
    }

    public void addExecutionListener(VMRExecutionListener listener) {
        if (listener != null && !executionListeners.contains(listener)) {
            executionListeners.add(listener);
        }
    }

    public void removeExecutionListener(VMRExecutionListener listener) {
        executionListeners.remove(listener);
    }

    // ==================== 状态更新方法 ====================

    public void setRegister(int regNum, int value) {
        if (regNum < 0 || regNum >= NUM_REGISTERS) {
            throw new IllegalArgumentException("Invalid register number: " + regNum);
        }

        // r0 是零寄存器，忽略写入
        if (regNum == 0) {
            return;
        }

        int oldValue = registers[regNum];
        if (oldValue != value) {
            registers[regNum] = value;
            registerModified[regNum] = true;

            // 触发事件
            RegisterChangeEvent event = new RegisterChangeEvent(this, eventStepNumber++, regNum, oldValue, value);
            notifyRegisterChanged(event);
        }
    }

    public int getRegister(int regNum) {
        if (regNum < 0 || regNum >= NUM_REGISTERS) {
            throw new IllegalArgumentException("Invalid register number: " + regNum);
        }
        return registers[regNum];
    }

    public void writeHeap(int address, int value) {
        if (address < 0 || address >= heap.length) {
            throw new IndexOutOfBoundsException("Heap address out of bounds: " + address);
        }

        int oldValue = heap[address];
        if (oldValue != value) {
            heap[address] = value;
            modifiedHeapAddresses.add(address);

            // 触发事件
            MemoryChangeEvent event = new MemoryChangeEvent(
                this, eventStepNumber++,
                MemoryChangeEvent.MemoryType.HEAP,
                address, oldValue, value
            );
            notifyMemoryChanged(event);
        }
    }

    public int readHeap(int address) {
        if (address < 0 || address >= heap.length) {
            throw new IndexOutOfBoundsException("Heap address out of bounds: " + address);
        }
        return heap[address];
    }

    public void writeGlobal(int address, int value) {
        if (address < 0 || address >= globals.length) {
            throw new IndexOutOfBoundsException("Global address out of bounds: " + address);
        }

        int oldValue = globals[address];
        if (oldValue != value) {
            globals[address] = value;
            modifiedMemoryAddresses.add(address);

            // 触发事件
            MemoryChangeEvent event = new MemoryChangeEvent(
                this, eventStepNumber++,
                MemoryChangeEvent.MemoryType.GLOBAL,
                address, oldValue, value
            );
            notifyMemoryChanged(event);
        }
    }

    public int readGlobal(int address) {
        if (address < 0 || address >= globals.length) {
            throw new IndexOutOfBoundsException("Global address out of bounds: " + address);
        }
        return globals[address];
    }

    public void setProgramCounter(int pc) {
        int oldPC = this.programCounter;
        if (oldPC != pc) {
            this.programCounter = pc;

            // 触发事件
            PCChangeEvent event = new PCChangeEvent(this, eventStepNumber++, oldPC, pc);
            notifyPCChanged(event);
        }
    }

    public int getProgramCounter() {
        return programCounter;
    }

    public void incrementExecutionStep() {
        executionSteps++;
    }

    public long getExecutionSteps() {
        return executionSteps;
    }

    // ==================== 栈帧管理 ====================

    public void pushStackFrame(StackFrame frame) {
        if (framePointer >= callStack.length - 1) {
            throw new RuntimeException("Call stack overflow");
        }
        callStack[++framePointer] = frame;
    }

    public StackFrame popStackFrame() {
        if (framePointer < 0) {
            return null;
        }
        return callStack[framePointer--];
    }

    public StackFrame getCurrentFrame() {
        if (framePointer < 0) {
            return null;
        }
        return callStack[framePointer];
    }

    public int getFramePointer() {
        return framePointer;
    }

    public StackFrame[] getCallStack() {
        return callStack.clone();
    }

    public int getCallStackDepth() {
        return framePointer + 1;
    }

    // ==================== 堆管理 ====================

    public int allocateHeap(int size) {
        if (heapAllocPointer + size > heap.length) {
            throw new OutOfMemoryError("Heap overflow");
        }
        int address = heapAllocPointer;
        heapAllocPointer += size;
        return address;
    }

    public int getHeapAllocPointer() {
        return heapAllocPointer;
    }

    // ==================== 状态查询 ====================

    public int[] getRegisters() {
        return registers.clone();
    }

    public int[] getHeap() {
        return heap.clone();
    }

    public int[] getGlobals() {
        return globals.clone();
    }

    public boolean isRegisterModified(int regNum) {
        if (regNum < 0 || regNum >= NUM_REGISTERS) {
            return false;
        }
        return registerModified[regNum];
    }

    public Set<Integer> getModifiedMemoryAddresses() {
        return new HashSet<>(modifiedMemoryAddresses);
    }

    public Set<Integer> getModifiedHeapAddresses() {
        return new HashSet<>(modifiedHeapAddresses);
    }

    public void clearModifiedFlags() {
        Arrays.fill(registerModified, false);
        modifiedMemoryAddresses.clear();
        modifiedHeapAddresses.clear();
    }

    // ==================== 虚拟机状态管理 ====================

    public void setVMState(VMStateChangeEvent.State state) {
        VMStateChangeEvent.State oldState = this.vmState;
        this.vmState = state;

        VMStateChangeEvent event = new VMStateChangeEvent(this, eventStepNumber++, oldState, state);
        notifyVMStateChanged(event);

        if (state == VMStateChangeEvent.State.RUNNING && oldState != VMStateChangeEvent.State.RUNNING) {
            startTime = System.currentTimeMillis();
            executionSteps = 0;
            notifyExecutionStarted();
        } else if (state == VMStateChangeEvent.State.HALTED) {
            notifyExecutionFinished();
        } else if (state == VMStateChangeEvent.State.PAUSED) {
            notifyExecutionPaused();
        }
    }

    public VMStateChangeEvent.State getVMState() {
        return vmState;
    }

    public long getExecutionTime() {
        if (startTime == 0) {
            return 0;
        }
        return System.currentTimeMillis() - startTime;
    }

    // ==================== 事件通知 ====================

    private void notifyRegisterChanged(RegisterChangeEvent event) {
        for (VMRStateListener listener : stateListeners) {
            listener.registerChanged(event);
        }
    }

    private void notifyMemoryChanged(MemoryChangeEvent event) {
        for (VMRStateListener listener : stateListeners) {
            listener.memoryChanged(event);
        }
    }

    private void notifyPCChanged(PCChangeEvent event) {
        for (VMRStateListener listener : stateListeners) {
            listener.pcChanged(event);
        }
    }

    private void notifyVMStateChanged(VMStateChangeEvent event) {
        for (VMRExecutionListener listener : executionListeners) {
            listener.vmStateChanged(event);
        }
    }

    private void notifyExecutionStarted() {
        for (VMRExecutionListener listener : executionListeners) {
            listener.executionStarted();
        }
    }

    private void notifyExecutionFinished() {
        for (VMRExecutionListener listener : executionListeners) {
            listener.executionFinished();
        }
    }

    private void notifyExecutionPaused() {
        for (VMRExecutionListener listener : executionListeners) {
            listener.executionPaused();
        }
    }

    // ==================== 批量更新 ====================

    public void notifyInstructionExecuted(int pc, int opcode, String mnemonic, String operands) {
        InstructionExecutionEvent event = new InstructionExecutionEvent(
            this, eventStepNumber++, pc, opcode, mnemonic, operands
        );

        for (VMRExecutionListener listener : executionListeners) {
            listener.afterInstructionExecute(event);
        }
    }

    public void notifyBreakpointHit(int pc) {
        setVMState(VMStateChangeEvent.State.PAUSED);
        System.out.println("Breakpoint hit at PC=0x" + Integer.toHexString(pc));
    }

    // ==================== 快照功能 ====================

    public VMRStateSnapshot createSnapshot() {
        return new VMRStateSnapshot(
            registers.clone(),
            heap.clone(),
            globals.clone(),
            programCounter,
            framePointer,
            heapAllocPointer,
            executionSteps,
            vmState
        );
    }

    public void restoreSnapshot(VMRStateSnapshot snapshot) {
        System.arraycopy(snapshot.getRegisters(), 0, registers, 0, registers.length);
        System.arraycopy(snapshot.getHeap(), 0, heap, 0, heap.length);
        System.arraycopy(snapshot.getGlobals(), 0, globals, 0, globals.length);
        programCounter = snapshot.getProgramCounter();
        framePointer = snapshot.getFramePointer();
        heapAllocPointer = snapshot.getHeapAllocPointer();
        executionSteps = snapshot.getExecutionSteps();
        vmState = snapshot.getVMState();

        // 触发完整更新事件
        clearModifiedFlags();
    }

    // ==================== 内部类：状态快照 ====================

    public static class VMRStateSnapshot {
        private final int[] registers;
        private final int[] heap;
        private final int[] globals;
        private final int programCounter;
        private final int framePointer;
        private final int heapAllocPointer;
        private final long executionSteps;
        private final VMStateChangeEvent.State vmState;
        private final long timestamp;

        public VMRStateSnapshot(int[] registers, int[] heap, int[] globals,
                                int programCounter, int framePointer, int heapAllocPointer,
                                long executionSteps, VMStateChangeEvent.State vmState) {
            this.registers = registers;
            this.heap = heap;
            this.globals = globals;
            this.programCounter = programCounter;
            this.framePointer = framePointer;
            this.heapAllocPointer = heapAllocPointer;
            this.executionSteps = executionSteps;
            this.vmState = vmState;
            this.timestamp = System.currentTimeMillis();
        }

        public int[] getRegisters() {
            return registers;
        }

        public int[] getHeap() {
            return heap;
        }

        public int[] getGlobals() {
            return globals;
        }

        public int getProgramCounter() {
            return programCounter;
        }

        public int getFramePointer() {
            return framePointer;
        }

        public int getHeapAllocPointer() {
            return heapAllocPointer;
        }

        public long getExecutionSteps() {
            return executionSteps;
        }

        public VMStateChangeEvent.State getVMState() {
            return vmState;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
