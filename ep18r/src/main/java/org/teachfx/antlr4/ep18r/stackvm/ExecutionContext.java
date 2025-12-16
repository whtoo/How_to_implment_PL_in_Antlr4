package org.teachfx.antlr4.ep18r.stackvm;

/**
 * 执行上下文
 * 封装寄存器、内存等执行环境
 * 提供统一的访问接口，消除重复代码
 */
public class ExecutionContext {
    private final RegisterVMInterpreter vm;
    private final int[] registers;
    private final int programCounter;

    public ExecutionContext(RegisterVMInterpreter vm, int programCounter) {
        this.vm = vm;
        this.registers = vm.getRegisters();
        this.programCounter = programCounter;
    }

    // ==================== 寄存器访问 ====================

    /**
     * 获取寄存器值
     */
    public int getRegister(int regNum) {
        validateRegister(regNum);
        return registers[regNum];
    }

    /**
     * 设置寄存器值
     */
    public void setRegister(int regNum, int value) {
        validateRegister(regNum);
        if (regNum != 0) { // r0是只读的零寄存器
            registers[regNum] = value;
        }
    }

    /**
     * 验证寄存器编号
     */
    private void validateRegister(int regNum) {
        if (regNum < 0 || regNum >= RegisterBytecodeDefinition.NUM_REGISTERS) {
            throw new IllegalArgumentException(
                "Invalid register number: " + regNum + ", must be 0-" +
                (RegisterBytecodeDefinition.NUM_REGISTERS - 1));
        }
    }

    // ==================== 操作数提取 ====================

    /**
     * 从操作数中提取rd字段（目标寄存器）
     */
    public int extractRd(int operand) {
        return (operand >> 21) & 0x1F;
    }

    /**
     * 从操作数中提取rs1字段（源寄存器1）
     */
    public int extractRs1(int operand) {
        return (operand >> 16) & 0x1F;
    }

    /**
     * 从操作数中提取rs2字段（源寄存器2）
     */
    public int extractRs2(int operand) {
        return (operand >> 11) & 0x1F;
    }

    /**
     * 从操作数中提取16位立即数（符号扩展）
     */
    public int extractImm16(int operand) {
        int imm = operand & 0xFFFF;
        if ((imm & 0x8000) != 0) {
            imm |= 0xFFFF0000;
        }
        return imm;
    }

    /**
     * 从操作数中提取26位立即数（符号扩展）
     */
    public int extractImm26(int operand) {
        int imm = operand & 0x3FFFFFF;
        if ((imm & 0x2000000) != 0) {
            imm |= 0xFC000000;
        }
        return imm;
    }

    // ==================== 内存访问 ====================

    /**
     * 读取内存
     */
    public int readMemory(int address) {
        return vm.readMemory(address);
    }

    /**
     * 写入内存
     */
    public void writeMemory(int address, int value) {
        vm.writeMemory(address, value);
    }

    /**
     * 读取堆内存
     */
    public int readHeap(int address) {
        return vm.readHeap(address);
    }

    /**
     * 写入堆内存
     */
    public void writeHeap(int address, int value) {
        vm.writeHeap(address, value);
    }

    // ==================== 跳转控制 ====================

    /**
     * 设置跳转目标
     */
    public void setJumpTarget(int target) {
        vm.setJumpTarget(target);
    }

    /**
     * 检查是否发生了跳转
     */
    public boolean didJump() {
        return vm.didJump();
    }

    // ==================== 异常处理 ====================

    /**
     * 抛出异常
     */
    public void throwException(Exception e) throws Exception {
        throw e;
    }

    // ==================== Getters ====================

    /**
     * 获取虚拟机配置
     */
    public VMConfig getConfig() {
        return vm.getConfig();
    }

    /**
     * 获取程序计数器
     */
    public int getProgramCounter() {
        return programCounter;
    }

    /**
     * 获取寄存器数组（内部使用）
     */
    public int[] getRegisters() {
        return registers;
    }
}
