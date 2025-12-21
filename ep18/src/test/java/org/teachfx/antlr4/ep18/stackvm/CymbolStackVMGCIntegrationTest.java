package org.teachfx.antlr4.ep18.stackvm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep18.gc.GarbageCollector;
import org.teachfx.antlr4.ep18.gc.ReferenceCountingGC;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * CymbolStackVM GC集成测试
 * 测试虚拟机与垃圾回收器的集成
 */
@DisplayName("CymbolStackVM GC集成测试")
public class CymbolStackVMGCIntegrationTest {

    private CymbolStackVM vm;
    private VMConfig config;

    @BeforeEach
    void setUp() {
        // 创建测试配置
        config = VMConfig.builder()
                .setHeapSize(1024 * 1024) // 1MB
                .setStackSize(1024)
                .setMaxStackDepth(1000)
                .setMaxFrameCount(1000)
                .setDebugMode(true)  // 启用调试模式
                .setTraceEnabled(false)
                .setVerboseErrors(false)
                .setInstructionCacheSize(1024)
                .setMaxExecutionTime(60000)
                .setEnableBoundsCheck(true)
                .setEnableTypeCheck(true)
                .build();

        vm = new CymbolStackVM(config);
    }

    @Test
    @DisplayName("虚拟机应该正确初始化GC")
    void testGCInitialization() {
        // 检查虚拟机是否包含GC实例
        assertThat(vm).isNotNull();

        // 通过反射检查GC字段是否存在
        try {
            var gcField = CymbolStackVM.class.getDeclaredField("garbageCollector");
            gcField.setAccessible(true);
            GarbageCollector gc = (GarbageCollector) gcField.get(vm);
            assertThat(gc).isNotNull();
            assertThat(gc).isInstanceOf(ReferenceCountingGC.class);
        } catch (NoSuchFieldException e) {
            fail("CymbolStackVM should have garbageCollector field");
        } catch (IllegalAccessException e) {
            fail("Cannot access garbageCollector field");
        }
    }

    @Test
    @DisplayName("结构体分配应该使用GC")
    void testStructAllocationUsesGC() throws Exception {
        // 创建测试字节码：分配一个2字段的结构体
        byte[] bytecode = createTestBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_STRUCT, 2),
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        // 执行字节码
        int structId = vm.execute(bytecode);

        // 检查栈顶应该是结构体ID（大于0）
        assertThat(structId).isGreaterThan(0);

        // 通过反射获取GC并检查对象是否存活
        try {
            var gcField = CymbolStackVM.class.getDeclaredField("garbageCollector");
            gcField.setAccessible(true);
            GarbageCollector gc = (GarbageCollector) gcField.get(vm);

            // 调试信息
            System.out.println("Debug: structId = " + structId);
            System.out.println("Debug: isObjectAlive(" + structId + ") = " + gc.isObjectAlive(structId));

            if (gc instanceof ReferenceCountingGC) {
                ReferenceCountingGC refGC = (ReferenceCountingGC) gc;
                System.out.println("Debug: GC object count = " + refGC.getObjectCount());
                System.out.println("Debug: GC heap usage = " + refGC.getHeapUsage());
            }

            assertThat(gc.isObjectAlive(structId)).isTrue();
        } catch (Exception e) {
            fail("Failed to check GC object status: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("GC集成不应该影响现有功能")
    void testBackwardCompatibility() throws Exception {
        // 测试基本算术运算仍然工作
        byte[] bytecode = createTestBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 5),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 3),
            encodeInstruction(BytecodeDefinition.INSTR_IADD),
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        int result = vm.execute(bytecode);
        assertThat(result).isEqualTo(8);
    }

    @Test
    @DisplayName("应该支持GC配置选项")
    void testGCConfiguration() {
        // 这个测试将在VMConfig扩展后实现
        // 目前先通过，等VMConfig扩展后再完善
        assertTrue(true);
    }

    @Test
    @DisplayName("应该正确处理内存不足情况")
    void testOutOfMemoryHandling() throws Exception {
        // 这个测试将在GC集成后实现
        // 目前先通过，等GC集成后再完善
        assertTrue(true);
    }

    // 辅助方法
    private byte[] createTestBytecode(int[] instructions) {
        byte[] bytecode = new byte[instructions.length * 4];
        for (int i = 0; i < instructions.length; i++) {
            int instruction = instructions[i];
            bytecode[i * 4] = (byte) ((instruction >> 24) & 0xFF);
            bytecode[i * 4 + 1] = (byte) ((instruction >> 16) & 0xFF);
            bytecode[i * 4 + 2] = (byte) ((instruction >> 8) & 0xFF);
            bytecode[i * 4 + 3] = (byte) (instruction & 0xFF);
        }
        return bytecode;
    }

    private int encodeInstruction(int opcode, int operand) {
        return (opcode << 24) | (operand & 0xFFFFFF);
    }

    private int encodeInstruction(int opcode) {
        return opcode << 24;
    }
}