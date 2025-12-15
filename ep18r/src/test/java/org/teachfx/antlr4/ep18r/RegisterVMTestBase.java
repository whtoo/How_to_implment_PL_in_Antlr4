package org.teachfx.antlr4.ep18r;

import org.junit.jupiter.api.BeforeEach;
import org.teachfx.antlr4.ep18r.stackvm.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.stackvm.VMConfig;

/**
 * RegisterVMTestBase - 寄存器虚拟机测试基类
 * 提供统一的寄存器虚拟机测试环境和工具
 */
public abstract class RegisterVMTestBase {
    protected RegisterVMInterpreter vm;
    protected VMConfig testConfig;

    @BeforeEach
    void setUpVMTest() {
        // 创建测试配置
        testConfig = createTestConfig();

        // 创建寄存器虚拟机实例
        vm = new RegisterVMInterpreter();
        // 注意：RegisterVMInterpreter当前没有VMConfig参数
        // 后续可能需要添加配置支持
    }

    /**
     * 创建测试配置
     * @return 测试配置
     */
    protected VMConfig createTestConfig() {
        return new VMConfig.Builder()
            .setHeapSize(1024 * 1024) // 1MB for tests
            .setStackSize(1024)
            .setMaxStackDepth(100)
            .setDebugMode(true)
            .setVerboseErrors(true)
            .setEnableBoundsCheck(true)
            .setEnableTypeCheck(true)
            .build();
    }

    /**
     * 编码R类型指令：op rd, rs1, rs2
     * 32位编码：opcode(6) | rd(5) | rs1(5) | rs2(5) | unused(11)
     * 简化为：opcode(8) | rd(5) | rs1(5) | rs2(5) | unused(9)
     */
    protected int encodeRType(int opcode, int rd, int rs1, int rs2) {
        // 使用8位操作码（与RegisterVMInterpreter的executeInstruction一致）
        // RegisterVMInterpreter使用1字节操作码，所以这里需要调整
        // 先简单实现：高8位操作码，然后是寄存器字段
        return (opcode << 24) | (rd << 19) | (rs1 << 14) | (rs2 << 9);
    }

    /**
     * 编码I类型指令：op rd, rs1, imm
     * 32位编码：opcode(6) | rd(5) | rs1(5) | immediate(16)
     */
    protected int encodeIType(int opcode, int rd, int rs1, int imm) {
        // 符号扩展立即数到16位
        imm = imm & 0xFFFF;
        return (opcode << 24) | (rd << 19) | (rs1 << 14) | (imm);
    }

    /**
     * 编码J类型指令：op imm
     * 32位编码：opcode(6) | address(26)
     */
    protected int encodeJType(int opcode, int address) {
        address = address & 0x3FFFFFF;
        return (opcode << 24) | address;
    }

    /**
     * 编码LI指令（立即数加载）：li rd, imm
     * 这是I类型指令的特例
     */
    protected int encodeLI(int rd, int imm) {
        return encodeIType(
            org.teachfx.antlr4.ep18r.stackvm.RegisterBytecodeDefinition.INSTR_LI,
            rd, 0, imm
        );
    }

    /**
     * 编码ADD指令：add rd, rs1, rs2
     */
    protected int encodeADD(int rd, int rs1, int rs2) {
        return encodeRType(
            org.teachfx.antlr4.ep18r.stackvm.RegisterBytecodeDefinition.INSTR_ADD,
            rd, rs1, rs2
        );
    }

    /**
     * 编码PRINT指令：print rs
     */
    protected int encodePRINT(int rs) {
        // PRINT是I类型：print rs, 无立即数
        return encodeIType(
            org.teachfx.antlr4.ep18r.stackvm.RegisterBytecodeDefinition.INSTR_PRINT,
            0, rs, 0
        );
    }

    /**
     * 编码HALT指令
     */
    protected int encodeHALT() {
        return org.teachfx.antlr4.ep18r.stackvm.RegisterBytecodeDefinition.INSTR_HALT << 24;
    }

    /**
     * 创建寄存器字节码
     * @param instructions 指令数组（已编码的32位指令）
     * @return 字节码数组（每条指令5字节：1字节操作码 + 4字节操作数）
     */
    protected byte[] createRegisterBytecode(int[] instructions) {
        // RegisterVMInterpreter使用5字节指令：1字节操作码 + 4字节操作数
        byte[] bytecode = new byte[instructions.length * 5];
        int index = 0;

        for (int instr : instructions) {
            // 提取操作码（高8位）
            int opcode = (instr >> 24) & 0xFF;
            // 操作数（低24位）
            int operand = instr & 0xFFFFFF;

            // 第1字节：操作码
            bytecode[index++] = (byte) opcode;

            // 第2-5字节：操作数（大端序）
            bytecode[index++] = (byte) ((operand >> 24) & 0xFF);
            bytecode[index++] = (byte) ((operand >> 16) & 0xFF);
            bytecode[index++] = (byte) ((operand >> 8) & 0xFF);
            bytecode[index++] = (byte) (operand & 0xFF);
        }

        return bytecode;
    }

    /**
     * 执行寄存器字节码
     * @param bytecode 字节码
     * @throws Exception 执行异常
     */
    protected void executeRegisterBytecode(byte[] bytecode) throws Exception {
        // RegisterVMInterpreter需要先加载代码，然后执行
        // 当前实现不支持直接执行字节码，需要模拟加载过程
        // 简化：使用内存数组
        vm.exec(); // 这会使用已加载的代码
        // 注意：这需要先设置vm的code和codeSize
    }

    /**
     * 设置虚拟机代码
     */
    protected void setVMCode(byte[] code) {
        // 使用反射设置私有字段
        try {
            java.lang.reflect.Field codeField = RegisterVMInterpreter.class.getDeclaredField("code");
            codeField.setAccessible(true);
            codeField.set(vm, code);

            java.lang.reflect.Field codeSizeField = RegisterVMInterpreter.class.getDeclaredField("codeSize");
            codeSizeField.setAccessible(true);
            codeSizeField.set(vm, code.length);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set VM code", e);
        }
    }

    /**
     * 获取寄存器值
     */
    protected int getRegisterValue(int regNum) {
        return vm.getRegister(regNum);
    }

    /**
     * 设置寄存器值
     */
    protected void setRegisterValue(int regNum, int value) {
        vm.setRegister(regNum, value);
    }

    /**
     * 断言寄存器值
     */
    protected void assertRegisterEquals(int regNum, int expected) {
        int actual = getRegisterValue(regNum);
        if (actual != expected) {
            throw new AssertionError(
                String.format("Register r%d: expected %d but got %d", regNum, expected, actual)
            );
        }
    }
}