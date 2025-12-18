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
     * 按照ABI调用约定，保存所有caller-saved寄存器：ra(r1), a0-a5(r2-r7), lr(r15)
     * callee-saved寄存器：s0-s4(r8-r12), sp(r13), fp(r14) 由被调用函数负责保存
     * 注意：a0(r2)是返回值寄存器，但也是调用者保存寄存器，调用者如需保留其值应保存
     */
    public static final InstructionExecutor CALL = (operand, context) -> {
        int target = context.extractImm26(operand);

        // 调试跟踪输出
        System.out.printf("[CALL] PC=%d, target=%d (0x%x), returnAddr=%d, fp=%d%n",
            context.getProgramCounter(), target, target,
            context.getProgramCounter() + 4, context.getFramePointer());

        // 验证跳转目标（26位跳转需要4字节对齐）
        context.validateJumpTarget26(target);

        // 保存返回地址
        int returnAddr = context.getProgramCounter() + 4;

        // 检查调用栈溢出
        context.checkStackOverflow();

        // 创建新的栈帧并压入调用栈
        StackFrame newFrame = new StackFrame(null, returnAddr);
        // 保存所有caller-saved寄存器（ra(r1), a0-a5(r2-r7), lr(r15)）到新栈帧的savedCallerRegisters数组
        // 数组索引映射：0:ra(r1), 1:a0(r2), 2:a1(r3), 3:a2(r4), 4:a3(r5), 5:a4(r6), 6:a5(r7), 7:lr(r15)
        // 首先保存r1, r2-r7, r15的原始值
        // 首先保存r1, r3-r7的原始值（跳过a0/r2，因为它是返回值寄存器）
        for (int i = 1; i <= 7; i++) {
            if (i != 2) { // 跳过a0(r2)
                newFrame.savedCallerRegisters[i - 1] = context.getRegister(i);
            }
        }
        // 保存r15(lr)的原始值到索引7
        newFrame.savedCallerRegisters[7] = context.getRegister(RegisterBytecodeDefinition.R15);

        int newFramePointer = context.getFramePointer() + 1;
        context.getCallStack()[newFramePointer] = newFrame;
        context.setFramePointer(newFramePointer);

        // 调试跟踪输出
        System.out.printf("[CALL] 新栈帧创建: fp=%d, 返回地址=%d, 保存寄存器 ra(r1),a0-a5(r2-r7),lr(r15): [%d, %d, %d, %d, %d, %d, %d, %d]%n",
            newFramePointer, returnAddr,
            newFrame.savedCallerRegisters[0], newFrame.savedCallerRegisters[1],
            newFrame.savedCallerRegisters[2], newFrame.savedCallerRegisters[3],
            newFrame.savedCallerRegisters[4], newFrame.savedCallerRegisters[5],
            newFrame.savedCallerRegisters[6], newFrame.savedCallerRegisters[7]);

        // 设置lr(r15)为返回地址以保持兼容性（调用者保存寄存器lr被调用者可以修改）
        context.setRegister(RegisterBytecodeDefinition.R15, returnAddr);

        // 跳转
        context.setJumpTarget(target);
    };

    /**
     * 函数返回指令执行器 (RET)
     * ret: 从调用栈恢复返回地址和寄存器
     *
     * 按照ABI调用约定，恢复所有caller-saved寄存器：ra(r1), a0-a5(r2-r7), lr(r15)
     * 注意：lr(r15)恢复为调用前的原始值，返回地址从栈帧的returnAddress获取
     */
    public static final InstructionExecutor RET = (operand, context) -> {
        int returnAddr;
        int currentFramePointer = context.getFramePointer();

        // 调试跟踪输出
        System.out.printf("[RET] PC=%d, 当前fp=%d%n",
            context.getProgramCounter(), currentFramePointer);

        if (currentFramePointer < 0) {
            // 没有调用栈帧，尝试使用 r15（兼容旧代码）
            returnAddr = context.getRegister(RegisterBytecodeDefinition.R15);
        } else {
            // 从调用栈弹出返回地址
            StackFrame frame = context.getCallStack()[currentFramePointer];
            returnAddr = frame.returnAddress;

            // 恢复所有caller-saved寄存器ra(r1), a0-a5(r2-r7), lr(r15)
            // 数组索引映射：0:ra(r1), 1:a0(r2), 2:a1(r3), 3:a2(r4), 4:a3(r5), 5:a4(r6), 6:a5(r7), 7:lr(r15)
            // 恢复所有caller-saved寄存器，但跳过a0(r2)因为它是返回值寄存器
            // 恢复ra(r1), a1-a5(r3-r7)
            for (int i = 1; i <= 7; i++) {
                if (i != 2) { // 跳过a0(r2)
                    context.setRegister(i, frame.savedCallerRegisters[i - 1]);
                }
            }
            // 恢复lr(r15)的原始值
            context.setRegister(RegisterBytecodeDefinition.R15, frame.savedCallerRegisters[7]);

            // 调试跟踪输出
            System.out.printf("[RET] 恢复寄存器 ra(r1),a0-a5(r2-r7),lr(r15): [%d, %d, %d, %d, %d, %d, %d, %d], 返回地址=%d, 新fp=%d%n",
                frame.savedCallerRegisters[0], frame.savedCallerRegisters[1],
                frame.savedCallerRegisters[2], frame.savedCallerRegisters[3],
                frame.savedCallerRegisters[4], frame.savedCallerRegisters[5],
                frame.savedCallerRegisters[6], frame.savedCallerRegisters[7],
                returnAddr, currentFramePointer - 1);

            context.setFramePointer(currentFramePointer - 1);
        }

        // 验证返回地址（26位跳转需要4字节对齐）
        context.validateJumpTarget26(returnAddr);

        // 调试跟踪输出
        System.out.printf("[RET] 跳转到返回地址=%d (0x%x)%n", returnAddr, returnAddr);

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
