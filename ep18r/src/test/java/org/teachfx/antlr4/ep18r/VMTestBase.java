package org.teachfx.antlr4.ep18r;

import org.junit.jupiter.api.BeforeEach;
import org.teachfx.antlr4.ep18r.stackvm.CymbolStackVM;
import org.teachfx.antlr4.ep18r.stackvm.VMConfig;

/**
 * VMTestBase - 虚拟机测试基类
 * 提供统一的测试环境和工具
 */
public abstract class VMTestBase {
    protected CymbolStackVM vm;
    protected VMConfig testConfig;
    
    @BeforeEach
    void setUpVMTest() {
        // 创建测试配置
        testConfig = createTestConfig();
        
        // 创建虚拟机实例
        vm = new CymbolStackVM(testConfig);
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
     * 执行字节码并返回结果
     * @param bytecode 字节码
     * @return 执行结果
     * @throws Exception 执行异常
     */
    protected int execute(byte[] bytecode) throws Exception {
        return vm.execute(bytecode);
    }
    
    /**
     * 创建简单的字节码
     * @param instructions 指令数组（操作码和参数交替）
     * @return 字节码数组
     */
    protected byte[] createBytecode(int[] instructions) {
        byte[] bytecode = new byte[instructions.length * 4]; // 每个指令占4字节
        int index = 0;

        for (int instr : instructions) {
            // 将整数转换为字节（大端序）
            bytecode[index++] = (byte) ((instr >> 24) & 0xFF);
            bytecode[index++] = (byte) ((instr >> 16) & 0xFF);
            bytecode[index++] = (byte) ((instr >> 8) & 0xFF);
            bytecode[index++] = (byte) (instr & 0xFF);
        }

        return bytecode;
    }

    /**
     * 编码指令
     * @param opcode 操作码
     * @param param 参数（可选）
     * @return 编码后的指令
     */
    protected int encodeInstruction(int opcode, int param) {
        return (opcode << 24) | (param & 0xFFFFFF);
    }

    /**
     * 编码无参数指令
     * @param opcode 操作码
     * @return 编码后的指令
     */
    protected int encodeInstruction(int opcode) {
        return opcode << 24;
    }
    
    /**
     * 断言执行成功并返回结果
     * @param bytecode 字节码
     * @param expected 期望结果
     * @throws Exception 执行异常
     */
    protected void assertExecutionResult(byte[] bytecode, int expected) throws Exception {
        int result = execute(bytecode);
        if (result != expected) {
            throw new AssertionError(
                String.format("Expected %d but got %d", expected, result)
            );
        }
    }
    
    /**
     * 断言执行抛出异常
     * @param bytecode 字节码
     * @param expectedException 期望的异常类型
     */
    protected void assertExecutionThrows(byte[] bytecode, Class<? extends Exception> expectedException) {
        try {
            execute(bytecode);
            throw new AssertionError(
                String.format("Expected %s but execution succeeded", expectedException.getSimpleName())
            );
        } catch (Exception e) {
            if (!expectedException.isInstance(e)) {
                throw new AssertionError(
                    String.format("Expected %s but got %s", 
                        expectedException.getSimpleName(), e.getClass().getSimpleName()),
                    e
                );
            }
        }
    }
}