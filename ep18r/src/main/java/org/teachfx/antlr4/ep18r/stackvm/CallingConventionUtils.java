package org.teachfx.antlr4.ep18r.stackvm;

/**
 * 调用约定工具类
 * 提供ABI相关的辅助函数，简化遵循调用约定的代码编写
 *
 * 主要功能：
 * 1. 寄存器保存/恢复辅助
 * 2. 栈帧管理辅助
 * 3. 参数传递辅助
 * 4. 返回值处理辅助
 */
public class CallingConventionUtils {

    // 私有构造函数，防止实例化
    private CallingConventionUtils() {}

    // ==================== 寄存器操作宏（模拟） ====================

    /**
     * 保存调用者保存寄存器到栈帧
     * 模拟宏：SAVE_CALLER_REGS frame, regMask
     * @param frame 目标栈帧
     * @param regMask 寄存器掩码（位掩码，第i位为1表示保存寄存器ri）
     */
    public static void saveCallerRegisters(StackFrame frame, int regMask) {
        // regMask的位0对应r0，位1对应r1，依此类推
        // 只处理r2-r7（调用者保存寄存器）
        for (int i = 2; i <= 7; i++) {
            if ((regMask & (1 << i)) != 0) {
                // 保存到栈帧的savedCallerRegisters数组
                // 注意：数组索引0对应r2
                frame.savedCallerRegisters[i - 2] = 0; // 实际值在执行时设置
                // 实际实现中，这里应该生成存储指令
            }
        }
    }

    /**
     * 从栈帧恢复调用者保存寄存器
     * 模拟宏：RESTORE_CALLER_REGS frame, regMask
     * @param frame 源栈帧
     * @param regMask 寄存器掩码
     */
    public static void restoreCallerRegisters(StackFrame frame, int regMask) {
        for (int i = 2; i <= 7; i++) {
            if ((regMask & (1 << i)) != 0) {
                // 从栈帧的savedCallerRegisters数组恢复
                // 实际实现中，这里应该生成加载指令
            }
        }
    }

    /**
     * 保存被调用者保存寄存器到栈帧
     * 模拟宏：SAVE_CALLEE_REGS frame, regMask
     * @param frame 目标栈帧
     * @param regMask 寄存器掩码（位8-12对应s0-s4）
     * @return 使用的栈偏移量（用于生成实际的存储指令）
     */
    public static int saveCalleeRegisters(StackFrame frame, int regMask) {
        int offset = 0;
        // 被调用者保存寄存器：s0-s4 (r8-r12)
        for (int i = 8; i <= 12; i++) {
            if ((regMask & (1 << i)) != 0) {
                // 计算栈偏移量（相对于fp）
                int stackOffset = StackOffsets.S0_SAVE_OFFSET + (i - 8) * 4;
                // 实际实现中，这里应该生成存储指令：sw ri, fp, stackOffset
                offset += 4;
            }
        }
        return offset;
    }

    /**
     * 从栈帧恢复被调用者保存寄存器
     * 模拟宏：RESTORE_CALLEE_REGS frame, regMask
     * @param frame 源栈帧
     * @param regMask 寄存器掩码
     */
    public static void restoreCalleeRegisters(StackFrame frame, int regMask) {
        for (int i = 8; i <= 12; i++) {
            if ((regMask & (1 << i)) != 0) {
                // 计算栈偏移量
                int stackOffset = StackOffsets.S0_SAVE_OFFSET + (i - 8) * 4;
                // 实际实现中，这里应该生成加载指令：lw ri, fp, stackOffset
            }
        }
    }

    // ==================== 栈帧管理 ====================

    /**
     * 生成函数序言指令序列
     * 模拟宏：FUNCTION_PROLOGUE numLocals, usedCalleeRegMask
     * @param numLocals 局部变量数量
     * @param usedCalleeRegMask 使用的被调用者保存寄存器掩码
     * @return 序言指令字符串（汇编格式）
     */
    public static String generatePrologue(int numLocals, int usedCalleeRegMask) {
        StringBuilder sb = new StringBuilder();

        // 1. 计算栈帧大小
        int numCalleeRegs = Integer.bitCount(usedCalleeRegMask & 0x1F00); // 位8-12
        int frameSize = StackOffsets.calculateFrameSize(numCalleeRegs, numLocals, 0);
        int spAdjustment = StackOffsets.calculateSpAdjustment(numCalleeRegs, numLocals);

        // 2. 分配栈帧
        sb.append(String.format("    addi sp, sp, %d      # 分配栈帧，大小=%d字节\n", spAdjustment, frameSize));

        // 3. 保存旧fp
        sb.append(String.format("    sw fp, %d(sp)        # 保存旧帧指针\n", StackOffsets.FP_SAVE_OFFSET));

        // 4. 设置新fp
        int fpOffset = frameSize - 4; // fp指向栈帧顶部-4
        sb.append(String.format("    addi fp, sp, %d      # 设置新帧指针\n", fpOffset));

        // 5. 保存被调用者保存寄存器
        for (int i = 8; i <= 12; i++) {
            if ((usedCalleeRegMask & (1 << i)) != 0) {
                int offset = StackOffsets.S0_SAVE_OFFSET + (i - 8) * 4;
                String regName = StackOffsets.getAbiName(i);
                sb.append(String.format("    sw %s, %d(fp)      # 保存%s\n", regName, offset, regName));
            }
        }

        return sb.toString();
    }

    /**
     * 生成函数尾声指令序列
     * 模拟宏：FUNCTION_EPILOGUE numLocals, usedCalleeRegMask
     * @param numLocals 局部变量数量
     * @param usedCalleeRegMask 使用的被调用者保存寄存器掩码
     * @return 尾声指令字符串（汇编格式）
     */
    public static String generateEpilogue(int numLocals, int usedCalleeRegMask) {
        StringBuilder sb = new StringBuilder();

        // 1. 恢复被调用者保存寄存器
        for (int i = 12; i >= 8; i--) { // 逆序恢复
            if ((usedCalleeRegMask & (1 << i)) != 0) {
                int offset = StackOffsets.S0_SAVE_OFFSET + (i - 8) * 4;
                String regName = StackOffsets.getAbiName(i);
                sb.append(String.format("    lw %s, %d(fp)      # 恢复%s\n", regName, offset, regName));
            }
        }

        // 2. 恢复旧fp
        sb.append(String.format("    lw fp, %d(sp)        # 恢复旧帧指针\n", StackOffsets.FP_SAVE_OFFSET));

        // 3. 计算栈帧大小
        int numCalleeRegs = Integer.bitCount(usedCalleeRegMask & 0x1F00);
        int frameSize = StackOffsets.calculateFrameSize(numCalleeRegs, numLocals, 0);

        // 4. 释放栈帧
        sb.append(String.format("    addi sp, sp, %d      # 释放栈帧\n", frameSize));

        // 5. 返回指令（由调用者添加）
        sb.append("    # ret                     # 函数返回（由调用者添加）\n");

        return sb.toString();
    }

    // ==================== 参数传递辅助 ====================

    /**
     * 获取参数在栈上的偏移量
     * 用于访问第7个及以后的参数
     * @param argIndex 参数索引（0-based）
     * @return 栈偏移量（相对于fp）
     */
    public static int getStackArgOffset(int argIndex) {
        return StackOffsets.argOffset(argIndex);
    }

    /**
     * 获取参数寄存器
     * 用于前6个参数
     * @param argIndex 参数索引（0-based，0-5）
     * @return 寄存器编号
     * @throws IllegalArgumentException 如果argIndex不在0-5范围内
     */
    public static int getArgRegister(int argIndex) {
        return StackOffsets.getArgRegister(argIndex);
    }

    /**
     * 获取参数寄存器的ABI名称
     * @param argIndex 参数索引（0-based，0-5）
     * @return 寄存器ABI名称（如"a0"）
     */
    public static String getArgRegisterName(int argIndex) {
        int reg = getArgRegister(argIndex);
        return StackOffsets.getAbiName(reg);
    }

    /**
     * 判断参数是否通过栈传递
     * @param argIndex 参数索引（0-based）
     * @return true如果参数通过栈传递（argIndex >= 6）
     */
    public static boolean isStackArg(int argIndex) {
        return argIndex >= 6;
    }

    /**
     * 生成参数传递指令序列
     * @param argIndex 参数索引
     * @param value 参数值（立即数或寄存器）
     * @param isImmediate 是否为立即数
     * @return 指令字符串
     */
    public static String generateArgPassing(int argIndex, String value, boolean isImmediate) {
        if (argIndex < 6) {
            // 寄存器传递
            String regName = getArgRegisterName(argIndex);
            if (isImmediate) {
                return String.format("    li %s, %s          # 第%d个参数\n", regName, value, argIndex + 1);
            } else {
                return String.format("    mov %s, %s         # 第%d个参数\n", regName, value, argIndex + 1);
            }
        } else {
            // 栈传递 - 需要调用者分配栈空间并存储
            int offset = getStackArgOffset(argIndex);
            if (isImmediate) {
                return String.format("    li t0, %s          # 第%d个参数（临时）\n", value, argIndex + 1) +
                       String.format("    sw t0, %d(fp)      # 存储到栈偏移量%d\n", offset, offset);
            } else {
                return String.format("    sw %s, %d(fp)      # 第%d个参数存储到栈偏移量%d\n", value, offset, argIndex + 1, offset);
            }
        }
    }

    // ==================== 返回值处理 ====================

    /**
     * 获取返回值寄存器编号
     * @return 返回值寄存器编号（r1，根据现有约定）
     */
    public static int getReturnValueRegister() {
        return 1; // 根据现有约定，r1是返回值寄存器
    }

    /**
     * 获取返回值寄存器ABI名称
     * @return "r1"
     */
    public static String getReturnValueRegisterName() {
        return StackOffsets.getAbiName(getReturnValueRegister());
    }

    /**
     * 生成返回值设置指令
     * @param value 返回值（立即数或寄存器）
     * @param isImmediate 是否为立即数
     * @return 指令字符串
     */
    public static String generateReturnValue(String value, boolean isImmediate) {
        String retReg = getReturnValueRegisterName();
        if (isImmediate) {
            return String.format("    li %s, %s          # 设置返回值\n", retReg, value);
        } else {
            return String.format("    mov %s, %s         # 设置返回值\n", retReg, value);
        }
    }

    // ==================== 寄存器掩码工具 ====================

    /**
     * 创建寄存器掩码
     * @param regNumbers 寄存器编号数组
     * @return 寄存器掩码
     */
    public static int createRegMask(int... regNumbers) {
        int mask = 0;
        for (int reg : regNumbers) {
            if (reg >= 0 && reg < 16) {
                mask |= (1 << reg);
            }
        }
        return mask;
    }

    /**
     * 从寄存器名称创建掩码
     * @param regNames 寄存器名称数组（如 "r1", "s0", "a2"）
     * @return 寄存器掩码
     */
    public static int createRegMaskFromNames(String... regNames) {
        int mask = 0;
        for (String name : regNames) {
            // 简单实现：假设名称格式正确
            if (name.startsWith("r")) {
                try {
                    int reg = Integer.parseInt(name.substring(1));
                    if (reg >= 0 && reg < 16) {
                        mask |= (1 << reg);
                    }
                } catch (NumberFormatException e) {
                    // 忽略无效名称
                }
            }
        }
        return mask;
    }

    /**
     * 检查寄存器是否在掩码中
     * @param mask 寄存器掩码
     * @param regNum 寄存器编号
     * @return true如果寄存器在掩码中
     */
    public static boolean isRegisterInMask(int mask, int regNum) {
        return (mask & (1 << regNum)) != 0;
    }

    /**
     * 获取掩码中的寄存器数量
     * @param mask 寄存器掩码
     * @return 寄存器数量
     */
    public static int countRegistersInMask(int mask) {
        return Integer.bitCount(mask);
    }

    // ==================== ABI规范支持 ====================

    /**
     * 获取目标ABI规范的返回值寄存器
     * 根据EP18R_ABI_设计文档，目标规范使用a0 (r2)作为返回值寄存器
     * @return 目标ABI规范的返回值寄存器编号 (2 = a0)
     */
    public static int getTargetAbiReturnValueRegister() {
        return 2; // a0 (r2) according to ABI specification
    }

    /**
     * 获取当前实现的返回值寄存器（向后兼容）
     * @return 当前实现的返回值寄存器编号 (1 = ra)
     */
    public static int getCurrentReturnValueRegister() {
        return 1; // ra (r1) for backward compatibility
    }

    /**
     * 检查是否使用目标ABI规范
     * @param useTargetAbi true表示使用目标ABI规范，false表示使用当前实现
     * @return 返回值寄存器编号
     */
    public static int getReturnValueRegister(boolean useTargetAbi) {
        return useTargetAbi ? getTargetAbiReturnValueRegister() : getCurrentReturnValueRegister();
    }

    /**
     * 获取参数寄存器（目标ABI规范）
     * @param argIndex 参数索引（0-5对应a0-a5）
     * @return 参数寄存器编号
     * @throws IllegalArgumentException 如果argIndex超出范围
     */
    public static int getArgumentRegister(int argIndex) {
        if (argIndex < 0 || argIndex > 5) {
            throw new IllegalArgumentException("Argument index must be between 0 and 5, got: " + argIndex);
        }
        return 2 + argIndex; // a0=2, a1=3, ..., a5=7
    }

    /**
     * 获取参数寄存器名称（目标ABI规范）
     * @param argIndex 参数索引（0-5对应a0-a5）
     * @return 参数寄存器ABI名称（如"a0", "a1"）
     */
    public static String getArgumentRegisterName(int argIndex) {
        return StackOffsets.getAbiName(getArgumentRegister(argIndex));
    }

    /**
     * 检查寄存器是否为参数寄存器（目标ABI规范）
     * @param regNum 寄存器编号
     * @return true如果寄存器是参数寄存器（a0-a5）
     */
    public static boolean isArgumentRegister(int regNum) {
        return regNum >= 2 && regNum <= 7; // a0-a5 (r2-r7)
    }

    /**
     * 获取参数寄存器索引
     * @param regNum 寄存器编号
     * @return 参数索引（0-5），如果不是参数寄存器则返回-1
     */
    public static int getArgumentIndex(int regNum) {
        if (isArgumentRegister(regNum)) {
            return regNum - 2; // a0=2 -> index 0
        }
        return -1;
    }

    /**
     * 获取保存寄存器（目标ABI规范）
     * @param saveIndex 保存寄存器索引（0-4对应s0-s4）
     * @return 保存寄存器编号
     * @throws IllegalArgumentException 如果saveIndex超出范围
     */
    public static int getSavedRegister(int saveIndex) {
        if (saveIndex < 0 || saveIndex > 4) {
            throw new IllegalArgumentException("Saved register index must be between 0 and 4, got: " + saveIndex);
        }
        return 8 + saveIndex; // s0=8, s1=9, ..., s4=12
    }

    /**
     * 获取保存寄存器名称（目标ABI规范）
     * @param saveIndex 保存寄存器索引（0-4对应s0-s4）
     * @return 保存寄存器ABI名称（如"s0", "s1"）
     */
    public static String getSavedRegisterName(int saveIndex) {
        return StackOffsets.getAbiName(getSavedRegister(saveIndex));
    }

    /**
     * 生成函数调用指令序列（目标ABI规范）
     * @param funcName 函数名
     * @param argValues 参数值数组
     * @param useTargetAbi 是否使用目标ABI规范
     * @return 汇编指令字符串
     */
    public static String generateFunctionCall(String funcName, String[] argValues, boolean useTargetAbi) {
        StringBuilder sb = new StringBuilder();

        // 设置参数到寄存器
        int numArgs = Math.min(argValues.length, 6);
        for (int i = 0; i < numArgs; i++) {
            String argReg = useTargetAbi ? getArgumentRegisterName(i) : "r" + getArgumentRegister(i);
            sb.append(String.format("    li %s, %s          # 设置第%d个参数\n", argReg, argValues[i], i + 1));
        }

        // 调用函数
        sb.append(String.format("    call %s             # 调用函数\n", funcName));

        return sb.toString();
    }

    /**
     * 验证ABI寄存器使用是否正确
     * @param regNum 寄存器编号
     * @param regType 寄存器类型 ("arg", "saved", "temp", "special")
     * @return true如果使用正确
     */
    public static boolean validateAbiRegisterUsage(int regNum, String regType) {
        switch (regType) {
            case "arg":
                return isArgumentRegister(regNum);
            case "saved":
                return regNum >= 8 && regNum <= 12; // s0-s4
            case "temp":
                return (regNum >= 1 && regNum <= 7) || regNum == 15; // ra, a0-a5, lr
            case "special":
                return regNum == 0 || regNum == 13 || regNum == 14; // zero, sp, fp
            default:
                return false;
        }
    }

    // ==================== 调试和验证 ====================

    /**
     * 验证函数调用是否符合ABI
     * @param callerSavedMask 调用者保存寄存器使用掩码
     * @param calleeSavedMask 被调用者保存寄存器使用掩码
     * @param numArgs 参数数量
     * @param numLocals 局部变量数量
     * @return 验证结果消息，null表示通过
     */
    public static String validateCallingConvention(int callerSavedMask, int calleeSavedMask,
                                                   int numArgs, int numLocals) {
        StringBuilder errors = new StringBuilder();

        // 1. 检查被调用者保存寄存器使用
        // 被调用者如果使用了s0-s4，必须在序言保存，在尾声恢复
        int usedCalleeRegs = calleeSavedMask & 0x1F00; // 位8-12
        if (usedCalleeRegs != 0) {
            // 检查是否包含非法寄存器
            int illegalRegs = calleeSavedMask & ~0x1F00;
            if (illegalRegs != 0) {
                errors.append("错误：被调用者保存寄存器掩码包含非法寄存器。\n");
            }
        }

        // 2. 检查参数数量
        if (numArgs < 0) {
            errors.append("错误：参数数量不能为负数。\n");
        }

        // 3. 检查局部变量数量
        if (numLocals < 0) {
            errors.append("错误：局部变量数量不能为负数。\n");
        }

        // 4. 检查栈帧大小计算
        try {
            int numCalleeRegs = Integer.bitCount(usedCalleeRegs);
            int frameSize = StackOffsets.calculateFrameSize(numCalleeRegs, numLocals,
                                                          Math.max(0, numArgs - 6));
            if (frameSize % 8 != 0) {
                errors.append("警告：栈帧大小(").append(frameSize).append(")不是8字节对齐。\n");
            }
        } catch (Exception e) {
            errors.append("错误：栈帧大小计算失败：").append(e.getMessage()).append("\n");
        }

        return errors.length() == 0 ? null : errors.toString();
    }

    /**
     * 生成ABI使用报告
     * @param functionName 函数名
     * @param numArgs 参数数量
     * @param numLocals 局部变量数量
     * @param callerSavedMask 调用者保存寄存器使用掩码
     * @param calleeSavedMask 被调用者保存寄存器使用掩码
     * @return 报告字符串
     */
    public static String generateABIReport(String functionName, int numArgs, int numLocals,
                                           int callerSavedMask, int calleeSavedMask) {
        StringBuilder report = new StringBuilder();
        report.append("函数ABI使用报告：").append(functionName).append("\n");
        report.append("========================================\n");

        // 参数信息
        report.append("参数数量: ").append(numArgs).append("\n");
        if (numArgs > 0) {
            report.append("参数传递:\n");
            for (int i = 0; i < numArgs; i++) {
                if (i < 6) {
                    report.append("  参数").append(i + 1).append(": 寄存器 ")
                          .append(getArgRegisterName(i)).append("\n");
                } else {
                    int offset = getStackArgOffset(i);
                    report.append("  参数").append(i + 1).append(": 栈偏移 fp")
                          .append(offset >= 0 ? "+" : "").append(offset).append("\n");
                }
            }
        }

        // 局部变量信息
        report.append("局部变量数量: ").append(numLocals).append("\n");
        if (numLocals > 0) {
            report.append("局部变量偏移（相对于fp）:\n");
            for (int i = 0; i < numLocals; i++) {
                int offset = StackOffsets.localVarOffset(i);
                report.append("  局部变量").append(i).append(": fp")
                      .append(offset >= 0 ? "+" : "").append(offset).append("\n");
            }
        }

        // 寄存器使用
        report.append("调用者保存寄存器使用: ");
        report.append(formatRegMask(callerSavedMask)).append("\n");

        report.append("被调用者保存寄存器使用: ");
        report.append(formatRegMask(calleeSavedMask)).append("\n");

        // 栈帧信息
        int numCalleeRegs = Integer.bitCount(calleeSavedMask & 0x1F00);
        int numStackArgs = Math.max(0, numArgs - 6);
        int frameSize = StackOffsets.calculateFrameSize(numCalleeRegs, numLocals, numStackArgs);
        report.append("栈帧大小: ").append(frameSize).append(" 字节\n");
        report.append("栈指针对齐: ").append(frameSize % 8 == 0 ? "是" : "否").append("\n");

        // 验证结果
        String validation = validateCallingConvention(callerSavedMask, calleeSavedMask,
                                                     numArgs, numLocals);
        if (validation != null) {
            report.append("\nABI验证警告/错误:\n");
            report.append(validation);
        } else {
            report.append("\nABI验证: 通过\n");
        }

        return report.toString();
    }

    /**
     * 格式化寄存器掩码为可读字符串
     * @param mask 寄存器掩码
     * @return 格式化后的字符串
     */
    private static String formatRegMask(int mask) {
        if (mask == 0) return "无";

        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (int i = 0; i < 16; i++) {
            if ((mask & (1 << i)) != 0) {
                if (!first) sb.append(", ");
                sb.append(StackOffsets.getAbiName(i)).append("(r").append(i).append(")");
                first = false;
            }
        }
        return sb.toString();
    }
}