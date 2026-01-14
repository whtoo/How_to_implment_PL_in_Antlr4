package org.teachfx.antlr4.ep18r.vizvmr.controller;

import org.teachfx.antlr4.ep18r.vizvmr.core.VMRStateModel;
import org.teachfx.antlr4.ep18r.vizvmr.event.VMStateChangeEvent;
import org.teachfx.antlr4.ep18r.vizvmr.integration.VMRInstrumentation;
import org.teachfx.antlr4.ep18r.vizvmr.integration.VMRVisualBridge;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 单步执行控制器
 * 支持单步、步过、步出、运行到断点等模式
 */
public class VMRStepController {
    public enum StepMode {
        STEP_INTO,    // 单步进入（执行一条指令）
        STEP_OVER,    // 步过（不进入函数调用）
        STEP_OUT,     // 步出（运行到当前函数返回）
        RUN_TO_LINE,  // 运行到指定行
        CONTINUE      // 继续运行
    }

    private final VMRVisualBridge visualBridge;
    private final VMRStateModel stateModel;
    private final VMRInstrumentation instrumentation;
    private final VMRBreakpointManager breakpointManager;
    private final AtomicBoolean stepping;
    private StepMode currentMode;
    private int targetPC;
    private int returnAddress;

    public VMRStepController(VMRVisualBridge visualBridge) {
        this.visualBridge = visualBridge;
        this.stateModel = visualBridge.getStateModel();
        this.instrumentation = visualBridge.getInstrumentation();
        this.breakpointManager = new VMRBreakpointManager();
        this.stepping = new AtomicBoolean(false);
        this.currentMode = StepMode.CONTINUE;
    }

    /**
     * 单步进入
     */
    public void stepInto() {
        if (stateModel.getVMState() == VMStateChangeEvent.State.HALTED) {
            return;
        }

        currentMode = StepMode.STEP_INTO;
        stepping.set(true);

        // 执行单条指令
        executeStep();
    }

    /**
     * 步过
     */
    public void stepOver() {
        if (stateModel.getVMState() == VMStateChangeEvent.State.HALTED) {
            return;
        }

        currentMode = StepMode.STEP_OVER;
        stepping.set(true);

        int pc = instrumentation.getProgramCounter();
        int opcode = readOpcode(pc);

        // 如果是 CALL 指令，记录返回地址
        if (opcode == 23) { // CALL
            int operand = readOperand(pc);
            targetPC = operand; // 目标地址
        } else {
            targetPC = pc + 4;
        }

        executeStep();
    }

    /**
     * 步出
     */
    public void stepOut() {
        if (stateModel.getVMState() == VMStateChangeEvent.State.HALTED) {
            return;
        }

        currentMode = StepMode.STEP_OUT;
        stepping.set(true);

        // 获取当前函数的返回地址
        var frame = instrumentation.getCurrentFrame();
        if (frame != null) {
            returnAddress = frame.returnAddress;
        } else {
            returnAddress = -1;
        }

        continueExecution();
    }

    /**
     * 运行到指定 PC
     */
    public void runToPC(int pc) {
        if (stateModel.getVMState() == VMStateChangeEvent.State.HALTED) {
            return;
        }

        currentMode = StepMode.RUN_TO_LINE;
        stepping.set(true);
        targetPC = pc;

        continueExecution();
    }

    /**
     * 继续执行直到断点
     */
    public void continueExecution() {
        if (stateModel.getVMState() == VMStateChangeEvent.State.HALTED) {
            return;
        }

        currentMode = StepMode.CONTINUE;
        stepping.set(false);

        visualBridge.start();
    }

    /**
     * 执行单步
     */
    private void executeStep() {
        visualBridge.step();
    }

    /**
     * 检查是否应该暂停
     */
    public boolean shouldPause(int pc) {
        if (!stepping.get()) {
            // 检查断点
            return breakpointManager.shouldPause(pc);
        }

        switch (currentMode) {
            case STEP_INTO:
                stepping.set(false);
                return true;

            case STEP_OVER:
                if (pc >= targetPC) {
                    stepping.set(false);
                    return true;
                }
                return breakpointManager.shouldPause(pc);

            case STEP_OUT:
                if (pc == returnAddress || pc > returnAddress) {
                    stepping.set(false);
                    return true;
                }
                return breakpointManager.shouldPause(pc);

            case RUN_TO_LINE:
                if (pc == targetPC) {
                    stepping.set(false);
                    return true;
                }
                return breakpointManager.shouldPause(pc);

            case CONTINUE:
                return breakpointManager.shouldPause(pc);

            default:
                return false;
        }
    }

    /**
     * 设置断点
     */
    public void setBreakpoint(int pc) {
        breakpointManager.setBreakpoint(pc);
    }

    /**
     * 清除断点
     */
    public void clearBreakpoint(int pc) {
        breakpointManager.clearBreakpoint(pc);
    }

    /**
     * 切换断点
     */
    public void toggleBreakpoint(int pc) {
        breakpointManager.toggleBreakpoint(pc);
    }

    /**
     * 清除所有断点
     */
    public void clearAllBreakpoints() {
        breakpointManager.clearAllBreakpoints();
    }

    /**
     * 获取断点管理器
     */
    public VMRBreakpointManager getBreakpointManager() {
        return breakpointManager;
    }

    /**
     * 获取当前模式
     */
    public StepMode getCurrentMode() {
        return currentMode;
    }

    /**
     * 是否正在单步执行
     */
    public boolean isStepping() {
        return stepping.get();
    }

    private int readOpcode(int pc) {
        byte[] code = instrumentation.getCode();
        if (code != null && pc >= 0 && pc < code.length) {
            return code[pc] & 0xFF;
        }
        return 0;
    }

    private int readOperand(int pc) {
        byte[] code = instrumentation.getCode();
        if (code != null && pc + 4 <= code.length) {
            return ((code[pc + 1] & 0xFF) << 16) |
                   ((code[pc + 2] & 0xFF) << 8) |
                   (code[pc + 3] & 0xFF);
        }
        return 0;
    }
}
