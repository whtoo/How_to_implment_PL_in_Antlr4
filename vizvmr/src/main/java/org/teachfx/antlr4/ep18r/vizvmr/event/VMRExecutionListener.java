package org.teachfx.antlr4.ep18r.vizvmr.event;

import java.util.EventListener;

/**
 * 虚拟机执行监听器接口
 * 监听指令取指、解码、执行等执行流程事件
 */
public interface VMRExecutionListener extends EventListener {

    /**
     * 指令取指时调用
     */
    default void instructionFetched(InstructionExecutionEvent event) {}

    /**
     * 指令解码前调用
     */
    default void beforeInstructionDecode(InstructionExecutionEvent event) {}

    /**
     * 指令执行前调用
     */
    default void beforeInstructionExecute(InstructionExecutionEvent event) {}

    /**
     * 指令执行后调用
     */
    default void afterInstructionExecute(InstructionExecutionEvent event) {}

    /**
     * 虚拟机状态变化时调用
     */
    void vmStateChanged(VMStateChangeEvent event);

    /**
     * 执行异常时调用
     */
    default void executionError(Throwable error, int pc) {
        System.err.println("Execution error at PC=" + pc + ": " + error.getMessage());
    }

    /**
     * 执行开始时调用
     */
    default void executionStarted() {}

    /**
     * 执行结束时调用
     */
    default void executionFinished() {}

    /**
     * 执行暂停时调用
     */
    default void executionPaused() {}
}
