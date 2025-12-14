package org.teachfx.antlr4.ep18r.stackvm;

/**
 * 寄存器文件 - 管理16个通用寄存器
 * 寄存器分配：
 *   r0: 零寄存器（恒为0，只读）
 *   r1-r12: 通用目的寄存器
 *   r13: 栈指针 (SP)
 *   r14: 帧指针 (FP)
 *   r15: 链接寄存器 (LR)
 */
public class RegisterFile {
    private final int[] registers;
    private final int numRegisters;

    /**
     * 创建寄存器文件，初始化所有寄存器为0
     */
    public RegisterFile() {
        this.numRegisters = RegisterBytecodeDefinition.NUM_REGISTERS;
        this.registers = new int[numRegisters];
        reset();
    }

    /**
     * 重置所有寄存器为0
     */
    public void reset() {
        for (int i = 0; i < numRegisters; i++) {
            registers[i] = 0;
        }
    }

    /**
     * 读取寄存器值
     * @param regNum 寄存器编号 (0-15)
     * @return 寄存器值
     * @throws IllegalArgumentException 如果寄存器编号无效
     */
    public int read(int regNum) {
        checkRegisterNumber(regNum);
        if (regNum == RegisterBytecodeDefinition.R0) {
            return 0; // 零寄存器始终返回0
        }
        return registers[regNum];
    }

    /**
     * 写入寄存器值
     * @param regNum 寄存器编号 (0-15)
     * @param value 要写入的值
     * @throws IllegalArgumentException 如果寄存器编号无效或是只读寄存器
     */
    public void write(int regNum, int value) {
        checkRegisterNumber(regNum);
        if (regNum == RegisterBytecodeDefinition.R0) {
            throw new IllegalArgumentException("Cannot write to zero register (r0)");
        }
        registers[regNum] = value;
    }

    /**
     * 批量读取多个寄存器值
     * @param regNums 寄存器编号数组
     * @return 寄存器值数组
     */
    public int[] readMultiple(int[] regNums) {
        int[] values = new int[regNums.length];
        for (int i = 0; i < regNums.length; i++) {
            values[i] = read(regNums[i]);
        }
        return values;
    }

    /**
     * 批量写入多个寄存器值
     * @param regNums 寄存器编号数组
     * @param values 值数组
     */
    public void writeMultiple(int[] regNums, int[] values) {
        if (regNums.length != values.length) {
            throw new IllegalArgumentException("Register numbers and values arrays must have same length");
        }
        for (int i = 0; i < regNums.length; i++) {
            write(regNums[i], values[i]);
        }
    }

    /**
     * 获取栈指针 (r13)
     * @return 栈指针值
     */
    public int getStackPointer() {
        return read(RegisterBytecodeDefinition.R13);
    }

    /**
     * 设置栈指针 (r13)
     * @param value 新的栈指针值
     */
    public void setStackPointer(int value) {
        write(RegisterBytecodeDefinition.R13, value);
    }

    /**
     * 获取帧指针 (r14)
     * @return 帧指针值
     */
    public int getFramePointer() {
        return read(RegisterBytecodeDefinition.R14);
    }

    /**
     * 设置帧指针 (r14)
     * @param value 新的帧指针值
     */
    public void setFramePointer(int value) {
        write(RegisterBytecodeDefinition.R14, value);
    }

    /**
     * 获取链接寄存器 (r15) - 存储返回地址
     * @return 链接寄存器值
     */
    public int getLinkRegister() {
        return read(RegisterBytecodeDefinition.R15);
    }

    /**
     * 设置链接寄存器 (r15)
     * @param value 新的链接寄存器值
     */
    public void setLinkRegister(int value) {
        write(RegisterBytecodeDefinition.R15, value);
    }

    /**
     * 获取所有寄存器的快照（用于调试）
     * @return 寄存器值副本数组
     */
    public int[] snapshot() {
        int[] snapshot = new int[numRegisters];
        System.arraycopy(registers, 0, snapshot, 0, numRegisters);
        // 确保快照中r0为0（即使内部数组可能被修改）
        snapshot[RegisterBytecodeDefinition.R0] = 0;
        return snapshot;
    }

    /**
     * 获取寄存器名称
     * @param regNum 寄存器编号
     * @return 寄存器名称字符串
     */
    public static String getRegisterName(int regNum) {
        switch (regNum) {
            case 0: return "r0 (zero)";
            case 13: return "r13 (SP)";
            case 14: return "r14 (FP)";
            case 15: return "r15 (LR)";
            default: return "r" + regNum;
        }
    }

    /**
     * 检查寄存器编号是否有效
     * @param regNum 寄存器编号
     */
    private void checkRegisterNumber(int regNum) {
        if (regNum < 0 || regNum >= numRegisters) {
            throw new IllegalArgumentException("Invalid register number: " + regNum +
                                               ". Valid range: 0-" + (numRegisters - 1));
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Register File:\n");
        for (int i = 0; i < numRegisters; i++) {
            sb.append(String.format("  %-8s = 0x%08X (%d)",
                                   getRegisterName(i), read(i), read(i)));
            if (i < numRegisters - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}