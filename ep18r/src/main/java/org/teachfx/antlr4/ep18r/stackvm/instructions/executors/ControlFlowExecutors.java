package org.teachfx.antlr4.ep18r.stackvm.instructions;

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
     * - 为被调用函数分配栈帧空间，更新SP/FP指向新栈帧（第4节）
     * - callee-saved寄存器（s0-s4）由被调用函数负责保存/恢复
     *
     * 栈帧布局（根据StackOffsets定义）：
     * 高地址
     * +-------------------+ ← 调用者栈帧结束
     * |   参数7+          |   fp + 16 + 4*(n-7)
     * |   ...             |
     * |   参数8           |   fp + 20
     * |   参数7           |   fp + 16
     * +-------------------+
     * |   返回地址         |   fp + 12  (存储在调用栈中)
     * +-------------------+
     * |   旧帧指针(fp)     |   fp + 8   (fp旧值)
     * +-------------------+
     * |   保存寄存器s4     |   fp + 4   (r12)
     * |   保存寄存器s3     |   fp + 0   (r11)
     * |   保存寄存器s2     |   fp - 4   (r10)
     * |   保存寄存器s1     |   fp - 8   (r9)
     * |   保存寄存器s0     |   fp - 12  (r8)
     * +-------------------+
     * |   局部变量n       |   fp - 16 - 4*(n-1)
     * |   ...             |
     * |   局部变量2       |   fp - 20
     * |   局部变量1       |   fp - 16
     * +-------------------+
     * |   临时空间         |   （用于表达式求值等）
     * +-------------------+ ← sp (栈指针，低地址)
     */
    public static final InstructionExecutor CALL = (operand, context) -> {
        int target = context.extractImm26(operand);

        // 调试跟踪输出
        System.out.printf("[CALL] PC=%d, target=%d (0x%x), returnAddr=%d, fp=%d, sp=%d%n",
            context.getProgramCounter(), target, target,
            context.getProgramCounter() + 4,
            context.getRegister(RegisterBytecodeDefinition.R14),
            context.getRegister(RegisterBytecodeDefinition.R13));

        // 验证跳转目标（26位跳转需要4字节对齐）
        context.validateJumpTarget26(target);

        // 保存返回地址
        int returnAddr = context.getProgramCounter() + 4;

        // 检查调用栈溢出
        context.checkStackOverflow();

        // 查找被调用函数符号，获取参数数量和局部变量数量
        FunctionSymbol func = context.getFunctionSymbol(target);
        int nargs = (func != null) ? func.nargs : 0;
        int nlocals = (func != null) ? func.nlocals : 0;

        // 使用StackOffsets计算标准栈帧布局
        // 假设被调用者使用所有s0-s4寄存器（5个）
        final int NUM_CALLEE_SAVED_REGS = 5; // s0-s4
        int numStackArgs = Math.max(0, nargs - 6); // 第7+个参数数量

        // 计算栈帧大小（字节）和字数（8字节对齐，所以是2字的倍数）
        int frameSize = StackOffsets.calculateFrameSize(NUM_CALLEE_SAVED_REGS, nlocals, numStackArgs);
        int frameSizeWords = frameSize / 4; // 转换为字索引（堆是int数组）
        int spAdjustment = StackOffsets.calculateSpAdjustment(NUM_CALLEE_SAVED_REGS, nlocals);

        // 获取当前SP和堆分配指针
        int currentSP = context.getRegister(RegisterBytecodeDefinition.R13);
        int currentHeapPointer = context.getHeapAllocPointer();

        // 确保SP和堆分配指针同步（使用堆作为栈空间）
        // newSP指向新栈帧的开始（低地址），堆分配指针是字索引
        int newSP = currentHeapPointer; // 字索引
        // fp指向栈帧顶部-4字节：fp = sp + frameSize - 4 字节
        // 转换为字索引：newFP = newSP + frameSizeWords - 1
        int newFP = newSP + frameSizeWords - 1; // 字索引

        // 检查堆空间是否足够（newSP和frameSizeWords都是字索引）
        if (newSP + frameSizeWords > context.getConfig().getHeapSize()) {
            throw new OutOfMemoryError("Not enough heap space for frame: need " + frameSize + " bytes (" + frameSizeWords + " words)");
        }

        // 保存旧的FP值到标准位置（fp+8字节）
        int oldFP = context.getRegister(RegisterBytecodeDefinition.R14);
        // StackOffsets.FP_SAVE_OFFSET=8字节，除以4得2字
        int fpSaveAddress = newFP + StackOffsets.FP_SAVE_OFFSET / 4; // 字索引
        context.writeMemory(fpSaveAddress, oldFP);

        // 创建新的栈帧并压入调用栈
        // frameBasePointer指向局部变量区域开始（fp-16字节）
        // StackOffsets.FIRST_LOCAL_OFFSET = -16字节，除以4得-4字
        int localsBase = newFP + StackOffsets.FIRST_LOCAL_OFFSET / 4;
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

        // 更新寄存器
        // SP寄存器（r13）指向新栈帧开始
        context.setRegister(RegisterBytecodeDefinition.R13, newSP);
        // FP寄存器（r14）指向新栈帧顶部-4
        context.setRegister(RegisterBytecodeDefinition.R14, newFP);
        // LR寄存器（r15）设置为返回地址
        context.setRegister(RegisterBytecodeDefinition.R15, returnAddr);

        // 更新堆分配指针（跳过整个栈帧，字数）
        context.setHeapAllocPointer(newSP + frameSizeWords);

        // 复制栈参数（如果有）从调用者栈帧到被调用者栈帧参数区域
        if (numStackArgs > 0) {
            // 源地址：调用者栈帧中的栈参数区域（假设调用者将栈参数存储在 sp + ARG_AREA_START_OFFSET/4 开始的位置）
            int srcBase = currentSP + StackOffsets.ARG_AREA_START_OFFSET / 4;
            // 目标地址：被调用者栈帧参数区域（fp + ARG_AREA_START_OFFSET/4）
            int dstBase = newFP + StackOffsets.ARG_AREA_START_OFFSET / 4;
            System.out.printf("[CALL] 复制栈参数: 数量=%d, 从 src=%d字 到 dst=%d字%n", numStackArgs, srcBase, dstBase);
            for (int i = 0; i < numStackArgs; i++) {
                int argValue = context.readMemory(srcBase + i);
                context.writeMemory(dstBase + i, argValue);
                System.out.printf("[CALL]   栈参数 %d: 值=%d (src=%d, dst=%d)%n", i, argValue, srcBase + i, dstBase + i);
            }
        }

        // 调试跟踪输出
        System.out.printf("[CALL] 栈帧布局: frameSize=%d字节(%d字), newSP=%d字, newFP=%d字, localsBase=%d字, heapPtr=%d字%n",
            frameSize, frameSizeWords, newSP, newFP, localsBase, newSP + frameSizeWords);
        System.out.printf("[CALL] 参数: nargs=%d (栈参数=%d), nlocals=%d%n", nargs, numStackArgs, nlocals);
        System.out.printf("[CALL] 保存寄存器位置: s0@fp%d, s1@fp%d, s2@fp%d, s3@fp%d, s4@fp%d%n",
            StackOffsets.S0_SAVE_OFFSET, StackOffsets.S1_SAVE_OFFSET,
            StackOffsets.S2_SAVE_OFFSET, StackOffsets.S3_SAVE_OFFSET,
            StackOffsets.S4_SAVE_OFFSET);
        System.out.printf("[CALL] 局部变量区域: 起始@fp%d, 结束@fp%d%n",
            StackOffsets.FIRST_LOCAL_OFFSET, StackOffsets.FIRST_LOCAL_OFFSET - 4 * (nlocals - 1));
        System.out.printf("[CALL] 保存caller寄存器: [a1=%d,a2=%d,a3=%d,a4=%d,a5=%d,lr=%d,ra=%d]%n",
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
     * - 恢复帧指针（FP）从栈帧内存读取旧值（fp+8）
     * - 恢复栈指针（SP）：sp = fp + 4
     * - 从栈帧获取返回地址
     * - callee-saved寄存器（s0-s4）由被调用函数在尾声恢复
     */
    public static final InstructionExecutor RET = (operand, context) -> {
        int returnAddr;
        int currentFramePointer = context.getFramePointer();

        // 调试跟踪输出 - 进入RET
        System.out.printf("\n[RET ENTRY] PC=%d, 当前framePointer=%d, 寄存器fp=%d, sp=%d%n",
            context.getProgramCounter(), currentFramePointer,
            context.getRegister(RegisterBytecodeDefinition.R14),
            context.getRegister(RegisterBytecodeDefinition.R13));
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

            // 恢复FP：从当前栈帧内存读取旧FP值（存储在fp+8字节）
            int currentFP = context.getRegister(RegisterBytecodeDefinition.R14); // 字索引
            // StackOffsets.FP_SAVE_OFFSET=8字节，除以4得2字
            int fpSaveAddress = currentFP + StackOffsets.FP_SAVE_OFFSET / 4; // 字索引
            int prevFP = context.readMemory(fpSaveAddress);
            context.setRegister(RegisterBytecodeDefinition.R14, prevFP);

            // 恢复SP：sp = fp + 1 - frameSizeWords（因为fp = sp + frameSizeWords - 1）
            // 需要计算当前栈帧的大小
            int frameSizeWords = 0;
            if (frame.symbol != null) {
                int nargs = frame.symbol.nargs;
                int nlocals = frame.symbol.nlocals;
                int numStackArgs = Math.max(0, nargs - 6);
                final int NUM_CALLEE_SAVED_REGS = 5; // 假设使用所有s0-s4
                int frameSize = StackOffsets.calculateFrameSize(NUM_CALLEE_SAVED_REGS, nlocals, numStackArgs);
                frameSizeWords = frameSize / 4;
            } else {
                // 未知函数，估计帧大小（至少包含保存寄存器区域）
                // 假设没有局部变量，只有s0-s4寄存器
                frameSizeWords = 5; // s0-s4
            }
            int newSP = currentFP + 1 - frameSizeWords;
            context.setRegister(RegisterBytecodeDefinition.R13, newSP);

            System.out.printf("[RET] 恢复FP: 从fp+8字节(字地址%d)读取旧FP=%d字, 设置SP=%d字 (fp+1-frameSizeWords, frameSizeWords=%d)%n",
                fpSaveAddress, prevFP, newSP, frameSizeWords);
            System.out.printf("[RET] 设置framePointer: %d->%d%n",
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
