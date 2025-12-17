package org.teachfx.antlr4.ep18r.stackvm;

/**
 * 控制流指令执行器集合
 * 处理所有跳转、调用和返回指令
 */
public class ControlFlowExecutors {

    /**
     * 函数调用指令执行器 (CALL)
     * call target: 保存返回地址到调用栈，跳转到目标地址
     *
     * 按照调用约定，保存caller-saved寄存器：r1-r7
     * callee-saved寄存器：r8-r14 由被调用函数负责保存
     */
    public static final InstructionExecutor CALL = (operand, context) -> {
        int target = context.extractImm26(operand);

        // 验证跳转目标（26位跳转需要4字节对齐）
        context.validateJumpTarget26(target);

        // 保存返回地址
        int returnAddr = context.getProgramCounter() + 4;

        // 检查调用栈溢出
        context.checkStackOverflow();

        // 创建新的栈帧并压入调用栈
        StackFrame newFrame = new StackFrame(null, returnAddr);
        // 保存caller-saved寄存器（r2-r7）到新栈帧的savedCallerRegisters数组
        // r1是返回值寄存器，不被保存
        for (int i = 2; i <= 7; i++) {
            newFrame.savedCallerRegisters[i - 2] = context.getRegister(i); // i-2因为数组索引0对应r2
        }
        int newFramePointer = context.getFramePointer() + 1;
        context.getCallStack()[newFramePointer] = newFrame;
        context.setFramePointer(newFramePointer);

        // 同时保存到 LR(r15) 以保持兼容性
        context.setRegister(RegisterBytecodeDefinition.R15, returnAddr);

        // 跳转
        context.setJumpTarget(target);
    };

    /**
     * 函数返回指令执行器 (RET)
     * ret: 从调用栈恢复返回地址和寄存器
     *
     * 按照调用约定，恢复caller-saved寄存器：r1-r7
     */
    public static final InstructionExecutor RET = (operand, context) -> {
        int returnAddr;
        int currentFramePointer = context.getFramePointer();

        if (currentFramePointer < 0) {
            // 没有调用栈帧，尝试使用 r15（兼容旧代码）
            returnAddr = context.getRegister(RegisterBytecodeDefinition.R15);
        } else {
            // 从调用栈弹出返回地址
            StackFrame frame = context.getCallStack()[currentFramePointer];
            returnAddr = frame.returnAddress;

            // 恢复caller-saved寄存器r2-r7
            for (int i = 2; i <= 7; i++) {
                context.setRegister(i, frame.savedCallerRegisters[i - 2]); // i-2因为数组索引0对应r2
            }

            context.setFramePointer(currentFramePointer - 1);
        }

        // 验证返回地址（26位跳转需要4字节对齐）
        context.validateJumpTarget26(returnAddr);

        // 跳转
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
