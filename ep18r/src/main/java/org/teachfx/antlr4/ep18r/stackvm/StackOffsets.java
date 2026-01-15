package org.teachfx.antlr4.ep18r.stackvm;

/**
 * 栈帧偏移量定义（基于EP18R ABI规范）
 * 定义标准栈帧布局中各个部分的偏移量
 * 所有偏移量相对于帧指针（fp, r14）计算
 *
 * 栈帧布局（向下增长）：
 *
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
 * +-------------------+
 * 低地址               ← sp (栈指针)
 */
public class StackOffsets {

    private static final Logger logger = Logger.getLogger(StackOffsets.class);

    // 私有构造函数，防止实例化
    private StackOffsets() {}

    // ==================== 固定偏移量 ====================

    /** 返回地址在调用栈中的位置（不存储在栈内存中） */
    public static final int RETURN_ADDRESS_IN_CALLSTACK = 12;

    /** 旧帧指针保存位置（相对于fp） */
    public static final int FP_SAVE_OFFSET = 8;

    /** 保存寄存器s4（r12）保存位置（相对于fp） */
    public static final int S4_SAVE_OFFSET = 4;

    /** 保存寄存器s3（r11）保存位置（相对于fp） */
    public static final int S3_SAVE_OFFSET = 0;

    /** 保存寄存器s2（r10）保存位置（相对于fp） */
    public static final int S2_SAVE_OFFSET = -4;

    /** 保存寄存器s1（r9）保存位置（相对于fp） */
    public static final int S1_SAVE_OFFSET = -8;

    /** 保存寄存器s0（r8）保存位置（相对于fp） */
    public static final int S0_SAVE_OFFSET = -12;

    /** 第一个局部变量的偏移量（相对于fp） */
    public static final int FIRST_LOCAL_OFFSET = -16;

    /** 参数区域的起始偏移量（相对于fp），用于第7个及以后的参数 */
    public static final int ARG_AREA_START_OFFSET = 16;

    // ==================== 计算方法 ====================

    /**
     * 计算局部变量的偏移量
     * @param index 局部变量索引（0表示第一个局部变量）
     * @return 相对于fp的偏移量
     */
    public static int localVarOffset(int index) {
        return FIRST_LOCAL_OFFSET - 4 * index;
    }

    /**
     * 计算参数的偏移量（用于第7个及以后的参数）
     * @param argIndex 参数索引（从0开始计数）
     * @return 相对于fp的偏移量
     */
    public static int argOffset(int argIndex) {
        // argIndex 0-5: 通过寄存器a0-a5传递
        // argIndex 6+: 通过栈传递
        if (argIndex < 6) {
            throw new IllegalArgumentException(
                "Arguments 0-5 are passed in registers, not on stack. argIndex: " + argIndex);
        }
        return ARG_AREA_START_OFFSET + 4 * (argIndex - 6);
    }

    /**
     * 计算栈帧大小
     * @param numSavedRegs 需要保存的被调用者保存寄存器数量（0-5）
     * @param numLocals 局部变量数量
     * @param numStackArgs 通过栈传递的参数数量（第7个及以后）
     * @return 栈帧总大小（字节）
     */
    public static int calculateFrameSize(int numSavedRegs, int numLocals, int numStackArgs) {
        // 基本固定部分：保存寄存器区域（已包含在局部变量区域前）
        // 局部变量区域：每个局部变量4字节
        // 栈参数区域：每个栈参数4字节
        // 对齐填充：确保8字节对齐

        int size = 0;

        // 1. 被调用者保存寄存器区域（s0-s4）：0-5个寄存器，每个4字节
        size += numSavedRegs * 4;

        // 2. 局部变量区域
        size += numLocals * 4;

        // 3. 栈参数区域（由调用者分配，但计算总大小）
        size += numStackArgs * 4;

        // 4. 固定开销：旧fp保存位置（4字节）
        size += 4;

        // 5. 对齐到8字节
        size = alignTo8Bytes(size);

        return size;
    }

    /**
     * 计算栈指针调整量（负数，表示需要减少sp的值）
     * @param numSavedRegs 需要保存的被调用者保存寄存器数量（0-5）
     * @param numLocals 局部变量数量
     * @return sp需要减少的字节数（负数）
     */
    public static int calculateSpAdjustment(int numSavedRegs, int numLocals) {
        // 栈帧中由被调用者分配的部分：
        // - 保存寄存器区域（如果被调用者使用这些寄存器）
        // - 局部变量区域
        // - 旧fp保存位置
        // 注意：栈参数区域由调用者分配

        int size = 0;

        // 1. 被调用者保存寄存器区域
        size += numSavedRegs * 4;

        // 2. 局部变量区域
        size += numLocals * 4;

        // 3. 旧fp保存位置
        size += 4;

        // 4. 对齐到8字节
        size = alignTo8Bytes(size);

        return -size; // 负值表示减少sp
    }

    /**
     * 将大小对齐到8字节
     */
    public static int alignTo8Bytes(int size) {
        return (size + 7) & ~7;
    }

    /**
     * 获取寄存器对应的ABI名称
     * @param regNum 寄存器编号（0-15）
     * @return ABI名称
     */
    public static String getAbiName(int regNum) {
        switch (regNum) {
            case 0: return "zero";
            case 1: return "ra";
            case 2: return "a0";
            case 3: return "a1";
            case 4: return "a2";
            case 5: return "a3";
            case 6: return "a4";
            case 7: return "a5";
            case 8: return "s0";
            case 9: return "s1";
            case 10: return "s2";
            case 11: return "s3";
            case 12: return "s4";
            case 13: return "sp";
            case 14: return "fp";
            case 15: return "lr";
            default: return "r" + regNum;
        }
    }

    /**
     * 检查寄存器是否为调用者保存寄存器
     * @param regNum 寄存器编号（0-15）
     * @return true如果是调用者保存寄存器
     */
    public static boolean isCallerSaved(int regNum) {
        // 调用者保存寄存器：ra (r1), a0-a5 (r2-r7), lr (r15)
        return regNum == 1 ||
               (regNum >= 2 && regNum <= 7) ||
               regNum == 15;
    }

    /**
     * 检查寄存器是否为被调用者保存寄存器
     * @param regNum 寄存器编号（0-15）
     * @return true如果是被调用者保存寄存器
     */
    public static boolean isCalleeSaved(int regNum) {
        // 被调用者保存寄存器：s0-s4 (r8-r12), sp (r13), fp (r14)
        return (regNum >= 8 && regNum <= 12) ||
               regNum == 13 ||
               regNum == 14;
    }

    /**
     * 检查寄存器是否为参数寄存器（a0-a5）
     * @param regNum 寄存器编号（0-15）
     * @return true如果是参数寄存器
     */
    public static boolean isArgRegister(int regNum) {
        // 参数寄存器：a0-a5 (r2-r7)
        return regNum >= 2 && regNum <= 7;
    }

    /**
     * 获取参数寄存器对应的参数索引
     * @param regNum 寄存器编号（必须是2-7）
     * @return 参数索引（0表示第一个参数）
     */
    public static int getArgIndex(int regNum) {
        if (!isArgRegister(regNum)) {
            throw new IllegalArgumentException("Register " + regNum + " is not an argument register (a0-a5)");
        }
        return regNum - 2;
    }

    /**
     * 根据参数索引获取对应的参数寄存器
     * @param argIndex 参数索引（0-5）
     * @return 寄存器编号
     */
    public static int getArgRegister(int argIndex) {
        if (argIndex < 0 || argIndex > 5) {
            throw new IllegalArgumentException("Argument index must be 0-5, got: " + argIndex);
        }
        return argIndex + 2; // a0 = r2
    }

    /**
     * 打印局部变量偏移信息（调试用）
     * @param functionName 函数名
     * @param numArgs 参数数量
     * @param numLocals 局部变量数量
     */
    public static void printLocalVarOffsets(String functionName, int numArgs, int numLocals) {
        logger.debug("函数 %s: 参数数量=%d, 局部变量数量=%d", functionName, numArgs, numLocals);
        // 参数偏移（栈传递的参数，索引>=6）
        for (int i = 6; i < numArgs; i++) {
            int offset = argOffset(i);
            logger.debug("  参数%d (栈传递): fp%+d", i, offset);
        }
        // 局部变量偏移
        for (int i = 0; i < numLocals; i++) {
            int offset = localVarOffset(i);
            logger.debug("  局部变量%d: fp%+d", i, offset);
        }
        // 计算栈帧大小
        int numStackArgs = Math.max(0, numArgs - 6);
        int frameSize = calculateFrameSize(0, numLocals, numStackArgs); // 假设没有保存寄存器
        logger.debug("  栈帧大小: %d 字节 (对齐后)", frameSize);
    }
}