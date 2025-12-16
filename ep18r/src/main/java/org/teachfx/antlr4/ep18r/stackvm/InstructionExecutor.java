package org.teachfx.antlr4.ep18r.stackvm;

/**
 * 指令执行器接口
 * 每个指令类型对应一个实现类
 * 采用策略模式，消除代码重复
 */
@FunctionalInterface
public interface InstructionExecutor {
    /**
     * 执行指令
     * @param operand 指令操作数
     * @param context 执行上下文
     * @throws Exception 执行异常
     */
    void execute(int operand, ExecutionContext context) throws Exception;
}
