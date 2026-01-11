package org.teachfx.antlr4.ep18r.stackvm.instructions.factory;

import org.teachfx.antlr4.ep18r.stackvm.instructions.InstructionExecutor;

/**
 * 指令工厂接口
 * 定义了指令执行器的注册和管理接口
 */
public interface IInstructionFactory {

    /**
     * 获取指定操作码的指令执行器
     * @param opcode 操作码（0-63）
     * @return 指令执行器，如果操作码无效返回null或默认处理器
     * @throws IllegalArgumentException 如果操作码超出有效范围
     */
    InstructionExecutor getExecutor(int opcode);

    /**
     * 注册新的指令执行器
     * @param opcode 操作码（0-63）
     * @param executor 指令执行器
     * @throws IllegalArgumentException 如果操作码或执行器无效
     */
    void registerExecutor(int opcode, InstructionExecutor executor);

    /**
     * 注销指定操作码的指令执行器
     * @param opcode 操作码（0-63）
     * @return 被注销的指令执行器，如果不存在返回null
     */
    InstructionExecutor unregisterExecutor(int opcode);

    /**
     * 清空所有指令执行器
     */
    void clear();

    /**
     * 重置为默认指令集
     */
    void resetToDefaults();
}
