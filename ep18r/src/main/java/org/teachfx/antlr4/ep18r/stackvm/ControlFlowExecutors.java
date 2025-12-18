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
     * 按照ABI调用约定（第3.3节）：
     * - caller-saved寄存器：ra(r1), a1-a5(r3-r7), lr(r15)由调用者保存（共7个，不包括a0/r2）
     * - 为被调用函数分配locals空间，更新FP指向新栈帧（第4节）
     * - callee-saved寄存器（s0-s4, sp, fp）由被调用函数负责保存/恢复
     */
    public static final InstructionExecutor CALL = (operand, context) -> {
        int target = context.extractImm26(operand);

        // 调试跟踪输出
        System.out.printf("[CALL] PC=%d, target=%d (0x%x), returnAddr=%d, fp=%d%n",
            context.getProgramCounter(), target, target,
            context.getProgramCounter() + 4, context.getRegister(RegisterBytecodeDefinition.R14));

        // 验证跳转目标（26位跳转需要4字节对齐）
        context.validateJumpTarget26(target);

        // 保存返回地址
        int returnAddr = context.getProgramCounter() + 4;

        // 检查调用栈溢出
        context.checkStackOverflow();

        // 查找被调用函数符号，获取nlocals
        FunctionSymbol func = context.getFunctionSymbol(target);
        int nlocals = (func != null) ? func.nlocals : 0;

        // 为被调用函数分配栈帧空间（按照ABI规范第4节）
        // 栈帧包括：保存寄存器区域 + locals区域
        // 注意：参数区域由调用者分配
        // 简化：假设被调用者使用所有s0-s4寄存器，保存寄存器区域固定为5*4=20字节
        // 实际应该根据函数是否使用s0-s4动态计算
        final int CALLEE_SAVE_AREA_SIZE = 5 * 4; // s0-s4: 5个寄存器 * 4字节
        int localsSize = nlocals * 4;
        int currentHeapPointer = context.getHeapAllocPointer();

        // 计算总栈帧大小（8字节对齐）
        int frameSize = CALLEE_SAVE_AREA_SIZE + localsSize;
        frameSize = (frameSize + 7) & ~7; // 对齐到8字节

        // 检查heap空间是否足够
        if (currentHeapPointer + frameSize > context.getConfig().getHeapSize()) {
            throw new OutOfMemoryError("Not enough heap space for frame: need " + frameSize + " bytes");
        }

        // locals区域从 当前heap指针 + 保存寄存器区域 开始
        int localsBase = currentHeapPointer + CALLEE_SAVE_AREA_SIZE;

        // FP指向旧FP保存位置（在locals区域上方16字节处）
        // 16 = 返回地址(4) + 旧FP(4) + 参数区偏移(8)
        int newFP = localsBase + 16;

        // 保存旧的FP值到新栈帧的FP位置（ABI第4.3节）
        int oldFP = context.getRegister(RegisterBytecodeDefinition.R14);
        context.writeMemory(newFP / 4, oldFP);

        // 创建新的栈帧并压入调用栈
        // frameBasePointer指向locals区域开始
        StackFrame newFrame = new StackFrame(func, returnAddr, localsBase);

        // 保存caller-saved寄存器（ABI第3.3节）
        // 数组索引映射：0:a1(r3), 1:a2(r4), 2:a3(r5), 3:a4(r6), 4:a5(r7), 5:lr(r15), 6:ra(r1)
        // 注意：a0(r2)不保存，因为它是返回值寄存器，callee会直接修改它
        // 如果调用者需要在调用后保留a0的值，调用者应该在call前手动保存
        newFrame.savedCallerRegisters[0] = context.getRegister(3); // a1
        newFrame.savedCallerRegisters[1] = context.getRegister(4); // a2
        newFrame.savedCallerRegisters[2] = context.getRegister(5); // a3
        newFrame.savedCallerRegisters[3] = context.getRegister(6); // a4
        newFrame.savedCallerRegisters[4] = context.getRegister(7); // a5
        newFrame.savedCallerRegisters[5] = context.getRegister(15); // lr
        newFrame.savedCallerRegisters[6] = context.getRegister(1); // ra

        int newFramePointer = context.getFramePointer() + 1;
        context.getCallStack()[newFramePointer] = newFrame;
        context.setFramePointer(newFramePointer);

        // 更新FP寄存器
        context.setRegister(RegisterBytecodeDefinition.R14, newFP);

        // 更新heap分配指针（跳过locals区域）
        context.setHeapAllocPointer(localsBase + localsSize);

        // 设置lr(r15)为返回地址
        context.setRegister(RegisterBytecodeDefinition.R15, returnAddr);

        // 调试跟踪输出
        System.out.printf("[CALL] localsBase=%d, newFP=%d, heapPtr=%d, savedRegs=[a1=%d,a2=%d,a3=%d,a4=%d,a5=%d,lr=%d,ra=%d]%n",
            localsBase, newFP, localsBase + localsSize,
            newFrame.savedCallerRegisters[0], newFrame.savedCallerRegisters[1],
            newFrame.savedCallerRegisters[2], newFrame.savedCallerRegisters[3],
            newFrame.savedCallerRegisters[4], newFrame.savedCallerRegisters[5],
            newFrame.savedCallerRegisters[6]);
        System.out.printf("[CALL EXIT] 从PC=%d跳转到PC=%d\n\n", context.getProgramCounter(), target);

        // 跳转
        context.setJumpTarget(target);
    };

    /**
     * 函数返回指令执行器 (RET)
     * ret: 从调用栈恢复返回地址
     *
     * 按照ABI调用约定（第3.3节）：
     * - 恢复caller-saved寄存器（ra, a1-a5, lr）
     * - 恢复帧指针（FP）指向上一个栈帧基址
     * - 从栈帧获取返回地址
     * - callee-saved寄存器（s0-s4, sp, fp）由被调用函数负责保存/恢复
     */
    public static final InstructionExecutor RET = (operand, context) -> {
        int returnAddr;
        int currentFramePointer = context.getFramePointer();

        // 调试跟踪输出 - 进入RET
        System.out.printf("\n[RET ENTRY] PC=%d, 当前fp=%d%n",
            context.getProgramCounter(), currentFramePointer);
        System.out.printf("[RET ENTRY] 跳转前寄存器状态: a0=%d, a1=%d, a2=%d, s0=%d, s1=%d, s2=%d%n",
            context.getRegister(2), context.getRegister(3), context.getRegister(4),
            context.getRegister(8), context.getRegister(9), context.getRegister(10));

        if (currentFramePointer < 0) {
            // 没有调用栈帧，尝试使用 r15（兼容旧代码）
            returnAddr = context.getRegister(RegisterBytecodeDefinition.R15);
            System.out.printf("[RET] 无栈帧，使用lr=%d作为返回地址%n", returnAddr);
        } else {
            // 从调用栈弹出返回地址
            StackFrame frame = context.getCallStack()[currentFramePointer];
            returnAddr = frame.returnAddress;

            System.out.printf("[RET] 从栈帧获取: returnAddr=%d, frameBase=%d, savedA1=%d%n",
                returnAddr, frame.frameBasePointer, frame.savedCallerRegisters[0]);

            // 恢复caller-saved寄存器（ABI第3.3节）
            // 数组索引映射：0:a1(r3), 1:a2(r4), 2:a3(r5), 3:a4(r6), 4:a5(r7), 5:lr(r15), 6:ra(r1)
            // 注意：不恢复a0(r2)，因为它是返回值寄存器
            System.out.printf("[RET] 恢复寄存器: a1=%d->%d, a2=%d->%d, a3=%d->%d, a4=%d->%d, a5=%d->%d, lr=%d->%d, ra=%d->%d%n",
                context.getRegister(3), frame.savedCallerRegisters[0],
                context.getRegister(4), frame.savedCallerRegisters[1],
                context.getRegister(5), frame.savedCallerRegisters[2],
                context.getRegister(6), frame.savedCallerRegisters[3],
                context.getRegister(7), frame.savedCallerRegisters[4],
                context.getRegister(15), frame.savedCallerRegisters[5],
                context.getRegister(1), frame.savedCallerRegisters[6]);

            context.setRegister(3, frame.savedCallerRegisters[0]);  // a1
            context.setRegister(4, frame.savedCallerRegisters[1]);  // a2
            context.setRegister(5, frame.savedCallerRegisters[2]);  // a3
            context.setRegister(6, frame.savedCallerRegisters[3]);  // a4
            context.setRegister(7, frame.savedCallerRegisters[4]); // a5
            context.setRegister(15, frame.savedCallerRegisters[5]); // lr
            context.setRegister(1, frame.savedCallerRegisters[6]);  // ra

            // 恢复FP（指向调用者的旧FP位置）
            // 调用者的FP = frameBasePointer + 16
            int prevLocalsBase = frame.frameBasePointer;
            int prevFP = prevLocalsBase + 16;
            context.setRegister(RegisterBytecodeDefinition.R14, prevFP);

            System.out.printf("[RET] 恢复FP: %d->%d, 设置framePointer: %d->%d%n",
                context.getRegister(RegisterBytecodeDefinition.R14), prevFP,
                currentFramePointer, currentFramePointer - 1);

            context.setFramePointer(currentFramePointer - 1);
        }

        // 验证返回地址（26位跳转需要4字节对齐）
        context.validateJumpTarget26(returnAddr);

        // 调试跟踪输出
        System.out.printf("[RET EXIT] 跳转: PC->%d (0x%x)\n\n", returnAddr, returnAddr);

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

        context.setJumpTarget(target);
    };

    /**
     * 条件为真跳转指令执行器
     */
    public static final InstructionExecutor JT = (operand, context) -> {
        int rs = context.extractRs1(operand);
        int target = context.extractImm16(operand);

        int value = context.getRegister(rs);
        if (value != 0) {
            context.setJumpTarget(target);
        }
    };

    /**
     * 条件为假跳转指令执行器
     */
    public static final InstructionExecutor JF = (operand, context) -> {
        int rs = context.extractRs1(operand);
        int target = context.extractImm16(operand);

        int value = context.getRegister(rs);
        if (value == 0) {
            context.setJumpTarget(target);
        }
    };
}
