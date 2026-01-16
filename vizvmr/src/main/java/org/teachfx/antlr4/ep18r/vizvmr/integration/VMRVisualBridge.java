package org.teachfx.antlr4.ep18r.vizvmr.integration;

import org.teachfx.antlr4.ep18r.stackvm.RegisterDisAssembler;
import org.teachfx.antlr4.ep18r.stackvm.config.VMConfig;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.stackvm.instructions.model.RegisterBytecodeDefinition;
import org.teachfx.antlr4.ep18r.vizvmr.core.VMRStateModel;
import org.teachfx.antlr4.ep18r.vizvmr.event.*;
import org.teachfx.antlr4.ep18r.visualization.adapter.RegisterVMVisualAdapter;
import org.teachfx.antlr4.common.visualization.IVirtualMachineVisualization;
import org.teachfx.antlr4.common.visualization.ExecutionListener;
import org.teachfx.antlr4.common.visualization.StateChangeListener;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public class VMRVisualBridge implements VMRStateListener, VMRExecutionListener {
    private final RegisterVMInterpreter vm;
    private final VMRStateModel stateModel;
    private final IVirtualMachineVisualization vmAdapter;
    private final AtomicBoolean running;
    private final AtomicBoolean paused;
    private Thread executionThread;

    private RegisterDisAssembler disAssembler;
    private ExecutionCallback executionCallback;

    public VMRVisualBridge(RegisterVMInterpreter vm, VMRStateModel stateModel) {
        this.vm = vm;
        this.stateModel = stateModel;

        VMConfig config = new VMConfig.Builder()
                .setHeapSize(stateModel.getHeap().length)
                .setStackSize(stateModel.getGlobals().length)
                .setMaxStackDepth(stateModel.getCallStack().length)
                .build();

        vmAdapter = new RegisterVMVisualAdapter(vm, config);

        // 注册监听器到两个系统
        registerListeners();

        running = new AtomicBoolean(false);
        paused = new AtomicBoolean(false);

        initializeDisAssembler();
    }

    private void registerListeners() {
        // 注册到 VMRStateModel (vizvmr 事件系统)
        stateModel.addStateListener(this);
        stateModel.addExecutionListener(this);

        // 注册到 RegisterVMVisualAdapter (common 事件系统)
        vmAdapter.addExecutionListener(createCommonExecutionListener());
        vmAdapter.addStateChangeListener(createCommonStateListener());
    }

    private ExecutionListener createCommonExecutionListener() {
        return new ExecutionListener() {
            @Override
            public void afterInstructionExecute(int pc, String instruction, Object result) {
                // 同步PC到状态模型
                stateModel.setProgramCounter(pc);
                
                // 使用VM公共API同步所有寄存器到状态模型
                syncRegistersToStateModel();
                
                if (executionCallback != null) {
                    executionCallback.onPCChanged(-1, pc);
                }
            }

            @Override
            public void executionPaused() {
                paused.set(true);
                stateModel.setVMState(VMStateChangeEvent.State.PAUSED);
                if (executionCallback != null) {
                    executionCallback.onExecutionPaused();
                }
            }

            @Override
            public void executionStopped(String reason) {
                running.set(false);
                stateModel.setVMState(VMStateChangeEvent.State.HALTED);
                // 最终同步所有状态
                syncRegistersToStateModel();
                syncHeapToStateModel();
                if (executionCallback != null) {
                    executionCallback.onExecutionFinished();
                }
            }
        };
    }
    
    /**
     * 使用VM公共API同步寄存器到状态模型
     */
    private void syncRegistersToStateModel() {
        // 同步所有16个寄存器
        for (int i = 0; i < 16; i++) {
            int vmValue = vm.getRegister(i);
            int stateValue = stateModel.getRegister(i);
            if (stateValue != vmValue) {
                stateModel.setRegister(i, vmValue);
            }
        }
    }
    
    /**
     * 使用VM公共API同步堆内存到状态模型
     */
    private void syncHeapToStateModel() {
        int heapSize = Math.min(vm.getHeapSize(), stateModel.getHeap().length);
        for (int i = 0; i < heapSize; i++) {
            int vmValue = vm.readHeap(i);
            int stateValue = stateModel.readHeap(i);
            if (stateValue != vmValue) {
                stateModel.writeHeap(i, vmValue);
            }
        }
    }

    private StateChangeListener createCommonStateListener() {
        return new StateChangeListener() {
            @Override
            public void vmStateChanged(org.teachfx.antlr4.common.visualization.VMState.ExecutionState oldState,
                                      org.teachfx.antlr4.common.visualization.VMState.ExecutionState newState) {
                // 同步状态
                if (executionCallback != null) {
                    executionCallback.onStateChanged(null, convertState(null));
                }
            }
        };
    }

    private VMStateChangeEvent.State convertState(org.teachfx.antlr4.common.visualization.VMState state) {
        if (state == null) {
            return VMStateChangeEvent.State.CREATED;
        }
        return VMStateChangeEvent.State.CREATED; // 简化转换
    }

    private void initializeDisAssembler() {
        byte[] code = vm.getCode();
        int codeSize = vm.getCodeSize();
        Object[] constPool = vm.getConstantPool();
        if (code != null && codeSize > 0) {
            disAssembler = new RegisterDisAssembler(code, codeSize, constPool);
        }
    }

    public boolean loadCode(InputStream input) throws Exception {
        boolean hasErrors = RegisterVMInterpreter.load(vm, input);
        if (!hasErrors) {
            initializeDisAssembler();
            stateModel.setVMState(VMStateChangeEvent.State.LOADED);
        }
        return hasErrors;
    }

    public void start() {
        if (running.get()) {
            return;
        }

        try {
            vmAdapter.run();
            running.set(true);
            paused.set(false);
            stateModel.setVMState(VMStateChangeEvent.State.RUNNING);
        } catch (Exception e) {
            if (executionCallback != null) {
                executionCallback.onError(e);
            }
        }
    }

    public void pause() {
        if (!paused.get()) {
            try {
                vmAdapter.pause();
                paused.set(true);
                stateModel.setVMState(VMStateChangeEvent.State.PAUSED);
            } catch (Exception e) {
                System.err.println("Pause error: " + e.getMessage());
            }
        }
    }

    public void resume() {
        if (paused.get()) {
            paused.set(false);
            vm.setPaused(false); // 同步到VM
            stateModel.setVMState(VMStateChangeEvent.State.RUNNING);
        }
    }

    public void stop() {
        try {
            vmAdapter.stop();
        } catch (Exception e) {
            System.err.println("Stop error: " + e.getMessage());
        }

        running.set(false);
        paused.set(false);

        stateModel.setVMState(VMStateChangeEvent.State.HALTED);
    }

    public void step() {
        if (!running.get() && stateModel.getVMState() != VMStateChangeEvent.State.PAUSED) {
            vmAdapter.getCurrentState();
        }

        stateModel.setVMState(VMStateChangeEvent.State.STEPPING);

        try {
            vmAdapter.step();
        } catch (Exception e) {
            System.err.println("Step error: " + e.getMessage());
            if (executionCallback != null) {
                executionCallback.onError(e);
            }
        }
    }

    public boolean isRunning() {
        return running.get();
    }

    public boolean isPaused() {
        return paused.get() || vm.isPaused();
    }

    public VMRStateModel getStateModel() {
        return stateModel;
    }

    public IVirtualMachineVisualization getVMAdapter() {
        return vmAdapter;
    }

    public RegisterVMInterpreter getVM() {
        return vm;
    }

    public RegisterDisAssembler getDisAssembler() {
        return disAssembler;
    }

    public void setExecutionCallback(ExecutionCallback callback) {
        this.executionCallback = callback;
    }

    public int getCurrentPC() {
        return stateModel.getProgramCounter();
    }

    public int getRegister(int regNum) {
        return stateModel.getRegister(regNum);
    }

    public String getDisassembly() {
        if (disAssembler != null) {
            return disAssembler.disassembleToString();
        }
        return "";
    }

    @Override
    public void registerChanged(RegisterChangeEvent event) {
        if (executionCallback != null) {
            executionCallback.onRegisterChanged(event.getRegisterNumber(),
                event.getOldValue(), event.getNewValue());
        }
    }

    @Override
    public void memoryChanged(MemoryChangeEvent event) {
        if (executionCallback != null) {
            executionCallback.onMemoryChanged(event.getMemoryType(),
                event.getAddress(), event.getOldValue(), event.getNewValue());
        }
    }

    @Override
    public void pcChanged(PCChangeEvent event) {
        if (executionCallback != null) {
            executionCallback.onPCChanged(event.getOldPC(), event.getNewPC());
        }
    }

    @Override
    public void vmStateChanged(VMStateChangeEvent event) {
        if (executionCallback != null) {
            executionCallback.onStateChanged(event.getOldState(), event.getNewState());
        }
    }

    @Override
    public void executionStarted() {
        if (executionCallback != null) {
            executionCallback.onExecutionStarted();
        }
    }

    @Override
    public void executionFinished() {
        if (executionCallback != null) {
            executionCallback.onExecutionFinished();
        }
    }

    @Override
    public void executionPaused() {
        if (executionCallback != null) {
            executionCallback.onExecutionPaused();
        }
    }

    @Override
    public void afterInstructionExecute(InstructionExecutionEvent event) {
        if (executionCallback != null) {
            executionCallback.onInstructionExecuted(event.getPC(),
                event.getOpcode(), event.getMnemonic(), event.getOperands());
        }
    }

    @Override
    public void executionError(Throwable error, int pc) {
        if (executionCallback != null) {
            executionCallback.onError(error);
        }
    }

    public interface ExecutionCallback {
        void onRegisterChanged(int regNum, int oldValue, int newValue);
        void onMemoryChanged(MemoryChangeEvent.MemoryType type, int address, int oldValue, int newValue);
        void onPCChanged(int oldPC, int newPC);
        void onStateChanged(VMStateChangeEvent.State oldState, VMStateChangeEvent.State newState);
        void onInstructionExecuted(int pc, int opcode, String mnemonic, String operands);
        void onExecutionStarted();
        void onExecutionFinished();
        void onExecutionPaused();
        void onError(Throwable error);
    }
}
