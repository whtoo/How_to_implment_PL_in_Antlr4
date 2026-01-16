package org.teachfx.antlr4.ep18r.vizvmr.core;

import org.teachfx.antlr4.ep18r.stackvm.StackFrame;
import org.teachfx.antlr4.ep18r.vizvmr.event.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 虚拟机状态模型
 */
public class VMRStateModel {
    private static final int NUM_REGISTERS = 16;

    private int[] registers;
    private int[] heap;
    private int[] globals;
    private final StackFrame[] callStack;
    private int framePointer;
    private int programCounter;
    private int heapAllocPointer;

    private final boolean[] registerModified;
    private final Set<Integer> modifiedMemoryAddresses;
    private final Set<Integer> modifiedHeapAddresses;

    private final List<VMRStateListener> stateListeners;
    private final List<VMRExecutionListener> executionListeners;

    private volatile VMStateChangeEvent.State vmState;
    private long executionSteps;
    private long startTime;
    private int eventStepNumber;

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

    public int[] getRegisters() {
        return registers;
    }

    public int getRegister(int regNum) {
        if (regNum < 0 || regNum >= NUM_REGISTERS) {
            throw new IllegalArgumentException("Invalid register number: " + regNum);
        }
        return registers[regNum];
    }

    public int[] getHeap() {
        return heap;
    }

    public int readHeap(int address) {
        if (address < 0 || address >= heap.length) {
            throw new IndexOutOfBoundsException("Heap address out of bounds: " + address);
        }
        return heap[address];
    }

    public int readGlobal(int address) {
        if (address < 0 || address >= globals.length) {
            throw new IndexOutOfBoundsException("Global address out of bounds: " + address);
        }
        return globals[address];
    }

    public int[] getGlobals() {
        return globals;
    }

    public int getGlobalsSize() {
        return globals.length;
    }

    public StackFrame[] getCallStack() {
        return callStack;
    }

    public int getCallStackDepth() {
        int depth = 0;
        for (StackFrame frame : callStack) {
            if (frame != null) depth++;
        }
        return depth;
    }

    public int getProgramCounter() {
        return programCounter;
    }

    public VMStateChangeEvent.State getVMState() {
        return vmState;
    }

    public long getExecutionSteps() {
        return executionSteps;
    }

    public int getHeapAllocPointer() {
        return heapAllocPointer;
    }

    public int getFramePointer() {
        return framePointer;
    }

    public void setRegister(int regNum, int value) {
        if (regNum < 0 || regNum >= NUM_REGISTERS) {
            throw new IllegalArgumentException("Invalid register number: " + regNum);
        }
        if (regNum == 0) {
            throw new IllegalArgumentException("Cannot modify register r0 (always returns 0)");
        }
        int oldValue = registers[regNum];
        registers[regNum] = value;
        registerModified[regNum] = true;

        RegisterChangeEvent event = new RegisterChangeEvent(this, eventStepNumber, regNum, oldValue, value);
        for (VMRStateListener listener : stateListeners) {
            listener.registerChanged(event);
        }
    }

    public void writeHeap(int address, int value) {
        if (address < 0 || address >= heap.length) {
            throw new IndexOutOfBoundsException("Heap address out of bounds: " + address);
        }
        int oldValue = heap[address];
        heap[address] = value;
        modifiedHeapAddresses.add(address);

        MemoryChangeEvent event = new MemoryChangeEvent(
            this, eventStepNumber, MemoryChangeEvent.MemoryType.HEAP, address, oldValue, value
        );
        for (VMRStateListener listener : stateListeners) {
            listener.memoryChanged(event);
        }
    }

    public void writeGlobal(int address, int value) {
        if (address < 0 || address >= globals.length) {
            throw new IndexOutOfBoundsException("Global address out of bounds: " + address);
        }
        int oldValue = globals[address];
        globals[address] = value;

        MemoryChangeEvent event = new MemoryChangeEvent(
            this, eventStepNumber, MemoryChangeEvent.MemoryType.GLOBAL, address, oldValue, value
        );
        for (VMRStateListener listener : stateListeners) {
            listener.memoryChanged(event);
        }
    }

    public void setProgramCounter(int pc) {
        int oldPC = programCounter;
        programCounter = pc;

        PCChangeEvent event = new PCChangeEvent(this, eventStepNumber, oldPC, pc);
        for (VMRStateListener listener : stateListeners) {
            listener.pcChanged(event);
        }
    }

    public void setVMState(VMStateChangeEvent.State newState) {
        if (vmState != newState) {
            VMStateChangeEvent.State oldState = vmState;
            vmState = newState;

            VMStateChangeEvent event = new VMStateChangeEvent(this, eventStepNumber, oldState, newState);
            for (VMRStateListener listener : stateListeners) {
                listener.vmStateChanged(event);
            }
        }
    }

    public void incrementExecutionSteps() {
        executionSteps++;
        eventStepNumber++;
    }

    /**
     * Alias for incrementExecutionSteps() - provides singular form for API compatibility.
     */
    public void incrementExecutionStep() {
        incrementExecutionSteps();
    }

    public void notifyInstructionExecuted(int pc, int opcode, String mnemonic, String operands) {
        InstructionExecutionEvent event = new InstructionExecutionEvent(
            this, eventStepNumber, pc, opcode, mnemonic, operands
        );
        for (VMRExecutionListener listener : executionListeners) {
            listener.afterInstructionExecute(event);
        }
    }

    public void notifyExecutionStarted() {
        for (VMRExecutionListener listener : executionListeners) {
            listener.executionStarted();
        }
    }

    public void notifyExecutionFinished() {
        for (VMRExecutionListener listener : executionListeners) {
            listener.executionFinished();
        }
    }

    public void notifyExecutionPaused() {
        for (VMRExecutionListener listener : executionListeners) {
            listener.executionPaused();
        }
    }

    public void notifyExecutionError(Throwable error, int pc) {
        for (VMRExecutionListener listener : executionListeners) {
            listener.executionError(error, pc);
        }
    }

    public void pushStackFrame(StackFrame frame) {
        for (int i = 0; i < callStack.length; i++) {
            if (callStack[i] == null) {
                callStack[i] = frame;
                framePointer = i;
                return;
            }
        }
    }

    public StackFrame popStackFrame() {
        for (int i = callStack.length - 1; i >= 0; i--) {
            if (callStack[i] != null) {
                StackFrame frame = callStack[i];
                callStack[i] = null;
                framePointer = i - 1;
                return frame;
            }
        }
        return null;
    }

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
        if (snapshot == null) return;

        this.registers = snapshot.getRegisters().clone();
        this.heap = snapshot.getHeap().clone();
        this.globals = snapshot.getGlobals().clone();
        this.programCounter = snapshot.getProgramCounter();
        this.framePointer = snapshot.getFramePointer();
        this.heapAllocPointer = snapshot.getHeapAllocPointer();
        this.executionSteps = snapshot.getExecutionSteps();
        this.vmState = snapshot.getVmState();
    }

    public void restoreFromSnapshot(VMRStateSnapshot snapshot) {
        restoreSnapshot(snapshot);
    }

    public void reset() {
        this.registers = new int[NUM_REGISTERS];
        Arrays.fill(heap, 0);
        Arrays.fill(globals, 0);
        Arrays.fill(callStack, null);
        this.framePointer = -1;
        this.programCounter = 0;
        this.heapAllocPointer = 0;

        Arrays.fill(registerModified, false);
        modifiedMemoryAddresses.clear();
        modifiedHeapAddresses.clear();

        this.vmState = VMStateChangeEvent.State.CREATED;
        this.executionSteps = 0;
        this.eventStepNumber = 0;
    }

    public boolean isRegisterModified(int regNum) {
        if (regNum >= 0 && regNum < NUM_REGISTERS) {
            return registerModified[regNum];
        }
        return false;
    }

    public void clearModifiedFlags() {
        Arrays.fill(registerModified, false);
        modifiedMemoryAddresses.clear();
        modifiedHeapAddresses.clear();
    }

    public List<Integer> getModifiedRegisters() {
        List<Integer> modified = new ArrayList<>();
        for (int i = 0; i < registerModified.length; i++) {
            if (registerModified[i]) {
                modified.add(i);
            }
        }
        return modified;
    }

    public Set<Integer> getModifiedHeapAddresses() {
        return new HashSet<>(modifiedHeapAddresses);
    }

    public static class VMRStateSnapshot {
        private final int[] registers;
        private final int[] heap;
        private final int[] globals;
        private final int programCounter;
        private final int framePointer;
        private final int heapAllocPointer;
        private final long executionSteps;
        private final VMStateChangeEvent.State vmState;

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
        }

        public int[] getRegisters() { return registers; }
        public int[] getHeap() { return heap; }
        public int[] getGlobals() { return globals; }
        public int getProgramCounter() { return programCounter; }
        public int getFramePointer() { return framePointer; }
        public int getHeapAllocPointer() { return heapAllocPointer; }
        public long getExecutionSteps() { return executionSteps; }
        public VMStateChangeEvent.State getVmState() { return vmState; }
    }
}
