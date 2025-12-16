package org.teachfx.antlr4.ep18r.stackvm;

/**
 * 控制流指令执行器集合
 * 处理所有跳转、调用和返回指令
 */
public class ControlFlowExecutors {

    /**
     * 函数调用指令执行器
     */
    public static final InstructionExecutor CALL = (operand, context) -> {
        int target = context.extractImm26(operand);
        int returnAddr = context.getProgramCounter() + 4;

        // 验证跳转目标
        if (target < 0 || target >= context.getConfig().getHeapSize()) {
            throw new IllegalArgumentException(
                "Invalid call target: " + target + " at PC=" + context.getProgramCounter());
        }

        // 保存返回地址到链接寄存器
        context.setRegister(RegisterBytecodeDefinition.R15, returnAddr);

        // 设置跳转目标
        context.setJumpTarget(target);
    };

    /**
     * 函数返回指令执行器
     */
    public static final InstructionExecutor RET = (operand, context) -> {
        // 从链接寄存器获取返回地址
        int returnAddr = context.getRegister(RegisterBytecodeDefinition.R15);

        // 设置跳转目标
        context.setJumpTarget(returnAddr);
    };

    /**
     * 无条件跳转指令执行器
     */
    public static final InstructionExecutor J = (operand, context) -> {
        int target = context.extractImm26(operand);

        // 验证跳转目标
        if (target < 0 || target >= context.getConfig().getHeapSize()) {
            throw new IllegalArgumentException(
                "Invalid jump target: " + target + " at PC=" + context.getProgramCounter());
        }

        // 设置跳转目标
        context.setJumpTarget(target);
    };

    /**
     * 条件为真跳转指令执行器
     */
    public static final InstructionExecutor JT = (operand, context) -> {
        int rs1 = context.extractRs1(operand);
        int target = context.extractImm26(operand);

        // 验证跳转目标
        if (target < 0 || target >= context.getConfig().getHeapSize()) {
            throw new IllegalArgumentException(
                "Invalid jump target: " + target + " at PC=" + context.getProgramCounter());
        }

        // 检查条件寄存器
        int condition = context.getRegister(rs1);
        if (condition != 0) {
            // 条件为真，跳转
            context.setJumpTarget(target);
        }
    };

    /**
     * 条件为假跳转指令执行器
     */
    public static final InstructionExecutor JF = (operand, context) -> {
        int rs1 = context.extractRs1(operand);
        int target = context.extractImm26(operand);

        // 验证跳转目标
        if (target < 0 || target >= context.getConfig().getHeapSize()) {
            throw new IllegalArgumentException(
                "Invalid jump target: " + target + " at PC=" + context.getProgramCounter());
        }

        // 检查条件寄存器
        int condition = context.getRegister(rs1);
        if (condition == 0) {
            // 条件为假，跳转
            context.setJumpTarget(target);
        }
    };
}
