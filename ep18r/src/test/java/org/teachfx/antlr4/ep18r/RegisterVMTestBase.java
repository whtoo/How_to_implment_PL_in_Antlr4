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
        vm = new RegisterVMInterpreter(testConfig);
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
     * 与RegisterVMInterpreter的解码逻辑一致：
     *   - opcode在bits 31-26
     *   - rd在bits 21-25
     *   - rs1在bits 16-20
     *   - rs2在bits 11-15
     *   - bits 0-10未使用
     */
    protected int encodeRType(int opcode, int rd, int rs1, int rs2) {
        // 确保操作码是6位（0-63）
        if (opcode < 0 || opcode > 0x3F) {
            throw new IllegalArgumentException("Opcode must be 6-bit (0-63): " + opcode);
        }
        // 确保寄存器编号是5位（0-31）
        if (rd < 0 || rd > 0x1F || rs1 < 0 || rs1 > 0x1F || rs2 < 0 || rs2 > 0x1F) {
            throw new IllegalArgumentException("Register numbers must be 5-bit (0-31)");
        }
        return (opcode << 26) | (rd << 21) | (rs1 << 16) | (rs2 << 11);
    }

    /**
     * 编码I类型指令：op rd, rs1, imm
     * 32位编码：opcode(6) | rd(5) | rs1(5) | immediate(16)
     * 与RegisterVMInterpreter的解码逻辑一致：
     *   - opcode在bits 31-26
     *   - rd在bits 21-25
     *   - rs1在bits 16-20
     *   - imm在bits 0-15（有符号16位立即数）
     */
    protected int encodeIType(int opcode, int rd, int rs1, int imm) {
        // 确保操作码是6位（0-63）
        if (opcode < 0 || opcode > 0x3F) {
            throw new IllegalArgumentException("Opcode must be 6-bit (0-63): " + opcode);
        }
        // 确保寄存器编号是5位（0-31）
        if (rd < 0 || rd > 0x1F || rs1 < 0 || rs1 > 0x1F) {
            throw new IllegalArgumentException("Register numbers must be 5-bit (0-31)");
        }
        // 立即数截断为16位（有符号）
        imm = imm & 0xFFFF;
        return (opcode << 26) | (rd << 21) | (rs1 << 16) | imm;
    }

    /**
     * 编码J类型指令：op imm
     * 32位编码：opcode(6) | address(26)
     * 与RegisterVMInterpreter的解码逻辑一致：
     *   - opcode在bits 31-26
     *   - address在bits 0-25（有符号26位立即数）
     */
    protected int encodeJType(int opcode, int address) {
        // 确保操作码是6位（0-63）
        if (opcode < 0 || opcode > 0x3F) {
            throw new IllegalArgumentException("Opcode must be 6-bit (0-63): " + opcode);
        }
        // 地址截断为26位（有符号）
        address = address & 0x3FFFFFF;
        return (opcode << 26) | address;
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
        return encodeJType(
            org.teachfx.antlr4.ep18r.stackvm.RegisterBytecodeDefinition.INSTR_HALT,
            0
        );
    }

    /**
     * 创建寄存器字节码
     * @param instructions 指令数组（已编码的32位指令）
     * @return 字节码数组（每条指令4字节：32位指令字）
     */
    protected byte[] createRegisterBytecode(int[] instructions) {
        // RegisterVMInterpreter使用4字节指令字（32位）
        byte[] bytecode = new byte[instructions.length * 4];
        int index = 0;

        for (int instr : instructions) {
            // 将32位指令字转换为4字节（大端序）
            bytecode[index++] = (byte) ((instr >> 24) & 0xFF);
            bytecode[index++] = (byte) ((instr >> 16) & 0xFF);
            bytecode[index++] = (byte) ((instr >> 8) & 0xFF);
            bytecode[index++] = (byte) (instr & 0xFF);
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