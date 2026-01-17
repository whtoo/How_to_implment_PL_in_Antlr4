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
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public class VMRVisualBridge implements VMRStateListener, VMRExecutionListener {
    private static final Logger logger = LogManager.getLogger(VMRVisualBridge.class);
    
    private final RegisterVMInterpreter vm;
    private final VMRStateModel stateModel;
    private final IVirtualMachineVisualization vmAdapter;
    private final AtomicBoolean running;
    private final AtomicBoolean paused;
    private Thread executionThread;

    private RegisterDisAssembler disAssembler;
    private ExecutionCallback executionCallback;

    public VMRVisualBridge(RegisterVMInterpreter vm, VMRStateModel stateModel) {
        logger.info("创建VMRVisualBridge，VM: {}, 状态模型: {}", vm, stateModel);
        this.vm = vm;
        this.stateModel = stateModel;

        VMConfig config = new VMConfig.Builder()
                .setHeapSize(stateModel.getHeap().length)
                .setStackSize(stateModel.getGlobals().length)
                .setMaxStackDepth(stateModel.getCallStack().length)
                .build();

        vmAdapter = new RegisterVMVisualAdapter(vm, config);
        logger.debug("创建VM适配器，配置: 堆大小={}, 栈大小={}, 最大栈深度={}", 
            stateModel.getHeap().length, stateModel.getGlobals().length, stateModel.getCallStack().length);

        // 注册监听器到两个系统
        registerListeners();

        running = new AtomicBoolean(false);
        paused = new AtomicBoolean(false);

        initializeDisAssembler();
        logger.info("VMRVisualBridge初始化完成");
    }

    private void registerListeners() {
        logger.debug("注册事件监听器");
        // 注册到 VMRStateModel (vizvmr 事件系统)
        stateModel.addStateListener(this);
        stateModel.addExecutionListener(this);
        logger.debug("已注册到VMRStateModel状态和事件监听器");

        // 注册到 RegisterVMVisualAdapter (common 事件系统)
        vmAdapter.addExecutionListener(createCommonExecutionListener());
        vmAdapter.addStateChangeListener(createCommonStateListener());
        logger.debug("已注册到VM适配器执行和状态变化监听器");
    }

    private ExecutionListener createCommonExecutionListener() {
        return new ExecutionListener() {
            @Override
            public void afterInstructionExecute(int pc, String instruction, Object result) {
                logger.debug("指令执行完成: PC={}, 指令={}, 结果={}", pc, instruction, result);
                stateModel.setProgramCounter(pc);
                syncRegistersToStateModel();
                stateModel.notifyInstructionExecuted(pc, 0, instruction, "");

                if (executionCallback != null) {
                    executionCallback.onPCChanged(-1, pc);
                }
            }

            @Override
            public void executionPaused() {
                logger.info("执行暂停");
                paused.set(true);
                stateModel.setVMState(VMStateChangeEvent.State.PAUSED);
                if (executionCallback != null) {
                    executionCallback.onExecutionPaused();
                }
            }

            @Override
            public void executionStopped(String reason) {
                logger.info("执行停止: {}", reason);
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
            logger.warn("尝试启动但已经在运行中");
            return;
        }

        logger.info("启动VM执行，running={}, paused={}", running.get(), paused.get());
        try {
            vmAdapter.run();
            running.set(true);
            paused.set(false);
            stateModel.setVMState(VMStateChangeEvent.State.RUNNING);
            logger.info("VM执行已启动，状态: RUNNING，vmAdapter运行状态: {}", vmAdapter.isRunning());
        } catch (Exception e) {
            logger.error("启动VM执行失败", e);
            if (executionCallback != null) {
                executionCallback.onError(e);
            }
        }
    }

    public void pause() {
        if (!paused.get()) {
            logger.info("暂停VM执行");
            try {
                vmAdapter.pause();
                paused.set(true);
                stateModel.setVMState(VMStateChangeEvent.State.PAUSED);
                logger.info("VM执行已暂停");
            } catch (Exception e) {
                logger.error("暂停VM执行失败", e);
                System.err.println("Pause error: " + e.getMessage());
            }
        } else {
            logger.debug("VM已经是暂停状态");
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
        logger.info("停止VM执行");
        try {
            vmAdapter.stop();
            logger.debug("VM适配器已停止");
        } catch (Exception e) {
            logger.error("停止VM执行失败", e);
            System.err.println("Stop error: " + e.getMessage());
        }

        running.set(false);
        paused.set(false);

        stateModel.setVMState(VMStateChangeEvent.State.HALTED);
        logger.info("VM执行已停止，状态: HALTED");
    }

    public void step() {
        logger.info("单步执行VM指令");
        if (!running.get() && stateModel.getVMState() != VMStateChangeEvent.State.PAUSED) {
            logger.debug("获取当前状态，状态: {}", stateModel.getVMState());
            vmAdapter.getCurrentState();
        }

        stateModel.setVMState(VMStateChangeEvent.State.STEPPING);
        logger.debug("设置VM状态为: STEPPING");

        try {
            vmAdapter.step();
            logger.debug("单步执行完成");
        } catch (Exception e) {
            logger.error("单步执行失败", e);
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
        logger.info("设置执行回调，callback: {}", callback);
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

    public void setAutoStepMode(boolean autoStepMode) {
        vmAdapter.setAutoStepMode(autoStepMode);
    }

    public void setAutoStepDelay(int delayMs) {
        vmAdapter.setAutoStepDelay(delayMs);
    }

    public boolean isAutoStepMode() {
        return vmAdapter.isAutoStepMode();
    }

    public int getAutoStepDelay() {
        return vmAdapter.getAutoStepDelay();
    }

    @Override
    public void registerChanged(RegisterChangeEvent event) {
        logger.debug("寄存器变化事件: 寄存器={}, 旧值={}, 新值={}", 
            event.getRegisterNumber(), event.getOldValue(), event.getNewValue());
        if (executionCallback != null) {
            executionCallback.onRegisterChanged(event.getRegisterNumber(),
                event.getOldValue(), event.getNewValue());
        } else {
            logger.warn("执行回调未设置，无法传递寄存器变化事件");
        }
    }

    @Override
    public void memoryChanged(MemoryChangeEvent event) {
        logger.debug("内存变化事件: 类型={}, 地址={}, 旧值={}, 新值={}", 
            event.getMemoryType(), event.getAddress(), event.getOldValue(), event.getNewValue());
        if (executionCallback != null) {
            executionCallback.onMemoryChanged(event.getMemoryType(),
                event.getAddress(), event.getOldValue(), event.getNewValue());
        } else {
            logger.warn("执行回调未设置，无法传递内存变化事件");
        }
    }

    @Override
    public void pcChanged(PCChangeEvent event) {
        logger.debug("程序计数器变化事件: 旧PC={}, 新PC={}", 
            event.getOldPC(), event.getNewPC());
        if (executionCallback != null) {
            executionCallback.onPCChanged(event.getOldPC(), event.getNewPC());
        } else {
            logger.warn("执行回调未设置，无法传递PC变化事件");
        }
    }

    @Override
    public void vmStateChanged(VMStateChangeEvent event) {
        logger.info("VM状态变化事件: 旧状态={}, 新状态={}", 
            event.getOldState(), event.getNewState());
        if (executionCallback != null) {
            executionCallback.onStateChanged(event.getOldState(), event.getNewState());
        } else {
            logger.warn("执行回调未设置，无法传递VM状态变化事件");
        }
    }

    @Override
    public void executionStarted() {
        logger.info("执行开始事件");
        if (executionCallback != null) {
            executionCallback.onExecutionStarted();
        } else {
            logger.warn("执行回调未设置，无法传递执行开始事件");
        }
    }

    @Override
    public void executionFinished() {
        logger.info("执行完成事件");
        if (executionCallback != null) {
            executionCallback.onExecutionFinished();
        } else {
            logger.warn("执行回调未设置，无法传递执行完成事件");
        }
    }

    @Override
    public void executionPaused() {
        logger.info("执行暂停事件");
        if (executionCallback != null) {
            executionCallback.onExecutionPaused();
        } else {
            logger.warn("执行回调未设置，无法传递执行暂停事件");
        }
    }

    @Override
    public void afterInstructionExecute(InstructionExecutionEvent event) {
        logger.debug("指令执行后事件: PC={}, 操作码={}, 助记符={}, 操作数={}",
            event.getPC(), event.getOpcode(), event.getMnemonic(), event.getOperands());
        if (executionCallback != null) {
            executionCallback.onInstructionExecuted(event.getPC(),
                event.getOpcode(), event.getMnemonic(), event.getOperands());
        } else {
            logger.warn("执行回调未设置，无法传递指令执行事件");
        }
    }

    @Override
    public void executionError(Throwable error, int pc) {
        logger.error("执行错误事件: PC={}, 错误={}", pc, error.getMessage(), error);
        if (executionCallback != null) {
            executionCallback.onError(error);
        } else {
            logger.warn("执行回调未设置，无法传递执行错误事件");
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
