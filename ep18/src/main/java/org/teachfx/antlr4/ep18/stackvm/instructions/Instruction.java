package org.teachfx.antlr4.ep18.stackvm.instructions;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;

/**
 * 指令接口 - 定义所有虚拟机指令的通用执行接口
 * 采用策略模式，将每个指令的实现解耦到独立的类中
 */
public interface Instruction {
    /**
     * 执行指令
     * @param context 虚拟机执行上下文，包含栈、寄存器、程序计数器等状态
     * @param operand 指令操作数（如果无操作数则为0）
     * @throws Exception 执行过程中的异常
     */
    void execute(VMExecutionContext context, int operand) throws Exception;

    /**
     * 获取指令名称
     * @return 指令名称字符串
     */
    String getName();

    /**
     * 获取指令操作码
     * @return 操作码值
     */
    int getOpcode();

    /**
     * 检查指令是否有操作数
     * @return true如果指令需要操作数，false否则
     */
    boolean hasOperand();
}
