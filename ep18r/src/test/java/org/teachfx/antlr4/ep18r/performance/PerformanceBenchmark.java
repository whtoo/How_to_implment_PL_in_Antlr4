package org.teachfx.antlr4.ep18r.performance;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.teachfx.antlr4.ep18r.VMInterpreter;
import org.teachfx.antlr4.ep18r.stackvm.CymbolStackVM;
import org.teachfx.antlr4.ep18r.stackvm.VMConfig;
import org.teachfx.antlr4.ep18r.stackvm.BytecodeDefinition;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 虚拟机性能基准测试
 * 测试虚拟机的执行速度和内存使用效率
 */
@DisplayName("虚拟机性能基准测试")
public class PerformanceBenchmark {

    private static final int WARMUP_ITERATIONS = 10;
    private static final int BENCHMARK_ITERATIONS = 100;

    private CymbolStackVM vm;
    private VMConfig performanceConfig;

    @BeforeEach
    void setUp() {
        performanceConfig = new VMConfig.Builder()
            .setHeapSize(1024 * 1024)
            .setStackSize(1024)
            .setMaxStackDepth(1000)
            .setDebugMode(false)
            .setVerboseErrors(false)
            .setEnableBoundsCheck(false)
            .setEnableTypeCheck(false)
            .build();
        vm = new CymbolStackVM(performanceConfig);
    }

    @Test
    @DisplayName("整数加法性能基准")
    void testIntegerAdditionPerformance() throws Exception {
        // 准备测试字节码
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 5),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 3),
            encodeInstruction(BytecodeDefinition.INSTR_IADD),
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        // 预热
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            vm.execute(bytecode);
        }

        // 基准测试
        List<Long> executionTimes = new ArrayList<>();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            long startTime = System.nanoTime();
            vm.execute(bytecode);
            long endTime = System.nanoTime();
            executionTimes.add(endTime - startTime);
        }

        // 计算统计信息
        long avgTime = executionTimes.stream()
            .mapToLong(Long::longValue)
            .sum() / executionTimes.size();
        long minTime = executionTimes.stream()
            .mapToLong(Long::longValue)
            .min()
            .orElse(0);
        long maxTime = executionTimes.stream()
            .mapToLong(Long::longValue)
            .max()
            .orElse(0);

        System.out.println("整数加法性能统计:");
        System.out.println("平均执行时间: " + (avgTime / 1_000_000.0) + " ms");
        System.out.println("最小执行时间: " + (minTime / 1_000_000.0) + " ms");
        System.out.println("最大执行时间: " + (maxTime / 1_000_000.0) + " ms");

        // 性能断言（应该很快完成）
        assertThat(avgTime).isLessThan(1_000_000); // 小于1ms
    }

    @Test
    @DisplayName("复杂表达式性能基准")
    void testComplexExpressionPerformance() throws Exception {
        // 准备复杂表达式：(10 + 5) * 2 - 8 / 4
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 10),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 5),
            encodeInstruction(BytecodeDefinition.INSTR_IADD),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 2),
            encodeInstruction(BytecodeDefinition.INSTR_IMUL),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 8),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 4),
            encodeInstruction(BytecodeDefinition.INSTR_IDIV),
            encodeInstruction(BytecodeDefinition.INSTR_ISUB),
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        // 预热
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            vm.execute(bytecode);
        }

        // 基准测试
        List<Long> executionTimes = new ArrayList<>();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            long startTime = System.nanoTime();
            vm.execute(bytecode);
            long endTime = System.nanoTime();
            executionTimes.add(endTime - startTime);
        }

        long avgTime = executionTimes.stream()
            .mapToLong(Long::longValue)
            .sum() / executionTimes.size();

        System.out.println("复杂表达式性能统计:");
        System.out.println("平均执行时间: " + (avgTime / 1_000_000.0) + " ms");

        assertThat(avgTime).isLessThan(2_000_000); // 小于2ms
    }

    @Test
    @DisplayName("VMInterpreter性能基准")
    void testVMInterpreterPerformance() throws Exception {
        String program = """
            .def main: args=0, locals=0
                iconst 0
                iconst 0
                iconst 0
                iconst 0
                iconst 0
                iadd
                iadd
                iadd
                iadd
                halt
            """;

        // 预热
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            VMInterpreter interpreter = new VMInterpreter();
            InputStream input = new ByteArrayInputStream(program.getBytes());
            VMInterpreter.load(interpreter, input);
            interpreter.exec();
        }

        // 基准测试
        List<Long> executionTimes = new ArrayList<>();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            VMInterpreter interpreter = new VMInterpreter();
            InputStream input = new ByteArrayInputStream(program.getBytes());

            long startTime = System.nanoTime();
            VMInterpreter.load(interpreter, input);
            interpreter.exec();
            long endTime = System.nanoTime();

            executionTimes.add(endTime - startTime);
        }

        long avgTime = executionTimes.stream()
            .mapToLong(Long::longValue)
            .sum() / executionTimes.size();

        System.out.println("VMInterpreter性能统计:");
        System.out.println("平均执行时间: " + (avgTime / 1_000_000.0) + " ms");

        assertThat(avgTime).isLessThan(10_000_000); // 小于10ms
    }

    @Test
    @DisplayName("内存使用基准测试")
    void testMemoryUsage() throws Exception {
        Runtime runtime = Runtime.getRuntime();
        long heapSizeBefore = runtime.totalMemory() - runtime.freeMemory();

        // 执行大量内存分配
        for (int i = 0; i < 1000; i++) {
            byte[] bytecode = createBytecode(new int[]{
                encodeInstruction(BytecodeDefinition.INSTR_ICONST, i),
                encodeInstruction(BytecodeDefinition.INSTR_HALT)
            });
            vm.execute(bytecode);
        }

        // 强制GC
        System.gc();
        Thread.sleep(100);

        long heapSizeAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = heapSizeAfter - heapSizeBefore;

        System.out.println("内存使用统计:");
        System.out.println("使用的内存: " + (memoryUsed / 1024.0) + " KB");

        // 内存使用应该在合理范围内
        assertThat(memoryUsed).isLessThan(10 * 1024 * 1024); // 小于10MB
    }

    @Test
    @DisplayName("栈操作性能测试")
    void testStackOperationsPerformance() throws Exception {
        // 创建深度嵌套的栈操作
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 1),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 1),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 1),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 1),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 1),
            encodeInstruction(BytecodeDefinition.INSTR_IADD),
            encodeInstruction(BytecodeDefinition.INSTR_IADD),
            encodeInstruction(BytecodeDefinition.INSTR_IADD),
            encodeInstruction(BytecodeDefinition.INSTR_IADD),
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        // 预热
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            vm.execute(bytecode);
        }

        // 基准测试
        List<Long> executionTimes = new ArrayList<>();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            long startTime = System.nanoTime();
            vm.execute(bytecode);
            long endTime = System.nanoTime();
            executionTimes.add(endTime - startTime);
        }

        long avgTime = executionTimes.stream()
            .mapToLong(Long::longValue)
            .sum() / executionTimes.size();

        System.out.println("栈操作性能统计:");
        System.out.println("平均执行时间: " + (avgTime / 1_000_000.0) + " ms");

        assertThat(avgTime).isLessThan(1_000_000); // 小于1ms
    }

    @Test
    @DisplayName("比较操作性能测试")
    void testComparisonPerformance() throws Exception {
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 100),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 50),
            encodeInstruction(BytecodeDefinition.INSTR_IGT),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 100),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 150),
            encodeInstruction(BytecodeDefinition.INSTR_ILT),
            encodeInstruction(BytecodeDefinition.INSTR_IADD),
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        // 预热
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            vm.execute(bytecode);
        }

        // 基准测试
        List<Long> executionTimes = new ArrayList<>();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            long startTime = System.nanoTime();
            vm.execute(bytecode);
            long endTime = System.nanoTime();
            executionTimes.add(endTime - startTime);
        }

        long avgTime = executionTimes.stream()
            .mapToLong(Long::longValue)
            .sum() / executionTimes.size();

        System.out.println("比较操作性能统计:");
        System.out.println("平均执行时间: " + (avgTime / 1_000_000.0) + " ms");

        assertThat(avgTime).isLessThan(1_500_000); // 小于1.5ms
    }

    /**
     * 编码指令
     * @param opcode 操作码
     * @param param 参数（可选）
     * @return 编码后的指令
     */
    private int encodeInstruction(int opcode, int param) {
        return (opcode << 24) | (param & 0xFFFFFF);
    }

    /**
     * 编码无参数指令
     * @param opcode 操作码
     * @return 编码后的指令
     */
    private int encodeInstruction(int opcode) {
        return opcode << 24;
    }

    /**
     * 创建字节码
     */
    private byte[] createBytecode(int[] instructions) {
        byte[] bytecode = new byte[instructions.length * 4];
        int index = 0;

        for (int instr : instructions) {
            bytecode[index++] = (byte) ((instr >> 24) & 0xFF);
            bytecode[index++] = (byte) ((instr >> 16) & 0xFF);
            bytecode[index++] = (byte) ((instr >> 8) & 0xFF);
            bytecode[index++] = (byte) (instr & 0xFF);
        }

        return bytecode;
    }
}