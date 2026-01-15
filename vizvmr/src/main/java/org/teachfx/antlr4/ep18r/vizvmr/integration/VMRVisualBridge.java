package org.teachfx.antlr4.ep18r.vizvmr.integration;

import org.teachfx.antlr4.ep18r.stackvm.RegisterDisAssembler;
import org.teachfx.antlr4.ep18r.stackvm.config.VMConfig;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.stackvm.instructions.model.RegisterBytecodeDefinition;
import org.teachfx.antlr4.ep18r.vizvmr.core.VMRStateModel;
import org.teachfx.antlr4.ep18r.vizvmr.event.*;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 虚拟机可视化桥接器
 * 连接虚拟机和可视化界面，提供运行控制接口
 */
public class VMRVisualBridge implements VMRStateListener, VMRExecutionListener {
    private final RegisterVMInterpreter vm;
    private final VMRStateModel stateModel;
    private final VMRInstrumentation instrumentation;
    private final AtomicBoolean running;
    private final AtomicBoolean paused;
    private Thread executionThread;

    // 反汇编器
    private RegisterDisAssembler disAssembler;

    // 回调接口
    private ExecutionCallback executionCallback;

    public VMRVisualBridge(RegisterVMInterpreter vm, VMRStateModel stateModel) {
        this.vm = vm;
        this.stateModel = stateModel;
        this.instrumentation = new VMRInstrumentation(vm, stateModel);
        this.running = new AtomicBoolean(false);
        this.paused = new AtomicBoolean(false);

        // 注册为监听器
        stateModel.addStateListener(this);
        stateModel.addExecutionListener(this);

        // 初始化反汇编器
        initializeDisAssembler();
    }

    private void initializeDisAssembler() {
        byte[] code = vm.getCode();
        int codeSize = vm.getCodeSize();
        Object[] constPool = getConstantPoolFromVM();
        if (code != null && codeSize > 0) {
            disAssembler = new RegisterDisAssembler(code, codeSize, constPool);
        }
    }

    private Object[] getConstantPoolFromVM() {
        try {
            java.lang.reflect.Field constPoolField = RegisterVMInterpreter.class.getDeclaredField("constPool");
            constPoolField.setAccessible(true);
            return (Object[]) constPoolField.get(vm);
        } catch (Exception e) {
            return new Object[0];
        }
    }

    /**
     * 加载字节码
     */
    public boolean loadCode(InputStream input) throws Exception {
        boolean hasErrors = RegisterVMInterpreter.load(vm, input);
        if (!hasErrors) {
            initializeDisAssembler();
            instrumentation.instrument();
            stateModel.setVMState(VMStateChangeEvent.State.LOADED);
        }
        return hasErrors;
    }

    /**
     * 开始执行
     */
    public void start() {
        if (running.get()) {
            return;
        }

        running.set(true);
        paused.set(false);
        stateModel.setVMState(VMStateChangeEvent.State.RUNNING);

        executionThread = new Thread(() -> {
            try {
                instrumentation.syncState();
                vm.exec();
            } catch (Exception e) {
                if (running.get()) {
                    System.err.println("Execution error: " + e.getMessage());
                    e.printStackTrace();
                    if (executionCallback != null) {
                        executionCallback.onError(e);
                    }
                }
            } finally {
                running.set(false);
                stateModel.setVMState(VMStateChangeEvent.State.HALTED);
            }
        }, "VM-Execution-Thread");

        executionThread.start();
    }

    /**
     * 暂停执行
     */
    public void pause() {
        if (running.get() && !paused.get()) {
            paused.set(true);
            stateModel.setVMState(VMStateChangeEvent.State.PAUSED);
        }
    }

    /**
     * 继续执行
     */
    public void resume() {
        if (paused.get()) {
            paused.set(false);
            stateModel.setVMState(VMStateChangeEvent.State.RUNNING);
            // 注意：这里需要恢复执行，但简单实现中我们不支持真正暂停
            // 实际实现需要在虚拟机执行循环中添加暂停点
        }
    }

    /**
     * 停止执行
     */
    public void stop() {
        running.set(false);
        paused.set(false);

        if (executionThread != null && executionThread.isAlive()) {
            executionThread.interrupt();
        }

        stateModel.setVMState(VMStateChangeEvent.State.PAUSED);
    }

    /**
     * 单步执行
     */
    public void step() {
        if (!running.get() && stateModel.getVMState() != VMStateChangeEvent.State.PAUSED) {
            // 如果没有运行，先加载状态
            instrumentation.syncState();
        }

        stateModel.setVMState(VMStateChangeEvent.State.STEPPING);

        try {
            // 执行单步
            int pc = instrumentation.getProgramCounter();
            instrumentation.beforeInstructionExecute(pc, readOpcode(pc));

            // 模拟执行一条指令（简化实现）
            executeSingleStep(pc);

            // 同步状态
            instrumentation.syncState();

        } catch (Exception e) {
            System.err.println("Step error: " + e.getMessage());
            if (executionCallback != null) {
                executionCallback.onError(e);
            }
        }
    }

    private int readOpcode(int pc) {
        byte[] code = vm.getCode();
        if (code != null && pc >= 0 && pc < code.length) {
            return code[pc] & 0xFF;
        }
        return 0;
    }

    private void executeSingleStep(int pc) {
        // 简化实现：只更新PC
        // 实际实现需要完整执行指令
        stateModel.setProgramCounter(pc + 4);
    }

    /**
     * 检查是否正在运行
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * 检查是否已暂停
     */
    public boolean isPaused() {
        return paused.get();
    }

    /**
     * 获取状态模型
     */
    public VMRStateModel getStateModel() {
        return stateModel;
    }

    /**
     * 获取插桩适配器
     */
    public VMRInstrumentation getInstrumentation() {
        return instrumentation;
    }

    /**
     * 获取反汇编器
     */
    public RegisterDisAssembler getDisAssembler() {
        return disAssembler;
    }

    /**
     * 获取代码面板（用于断点管理）
     * 注意：此方法需要在UI初始化后调用
     */
    public org.teachfx.antlr4.ep18r.vizvmr.ui.panel.CodePanel getCodePanel() {
        // 返回断点管理器的代码面板引用
        // 实际使用时需要在外部设置
        return null;
    }

    /**
     * 设置代码面板引用
     */
    public void setCodePanel(org.teachfx.antlr4.ep18r.vizvmr.ui.panel.CodePanel codePanel) {
        // 存储代码面板引用用于断点管理
    }

    /**
     * 设置执行回调
     */
    public void setExecutionCallback(ExecutionCallback callback) {
        this.executionCallback = callback;
    }

    /**
     * 获取当前PC
     */
    public int getCurrentPC() {
        return instrumentation.getProgramCounter();
    }

    /**
     * 获取寄存器值
     */
    public int getRegister(int regNum) {
        return instrumentation.getRegister(regNum);
    }

    /**
     * 获取反汇编代码
     */
    public String getDisassembly() {
        if (disAssembler != null) {
            return disAssembler.disassembleToString();
        }
        return "";
    }

    // ==================== VMRStateListener 实现 ====================

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

    // ==================== VMRExecutionListener 实现 ====================

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

    // ==================== 内部接口：执行回调 ====================

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
