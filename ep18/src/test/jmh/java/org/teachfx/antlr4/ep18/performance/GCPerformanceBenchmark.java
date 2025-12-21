package org.teachfx.antlr4.ep18.performance;

import org.openjdk.jmh.annotations.*;
import org.teachfx.antlr4.ep18.stackvm.CymbolStackVM;
import org.teachfx.antlr4.ep18.stackvm.VMConfig;
import org.teachfx.antlr4.ep18.stackvm.BytecodeDefinition;

import java.util.concurrent.TimeUnit;

/**
 * GC性能基准测试
 *
 * 测量垃圾回收器集成对虚拟机性能的影响
 * 包括GC开销、内存分配性能、垃圾回收暂停时间等
 *
 * @author GC Integration Team
 * @version 1.0.0
 */
@BenchmarkMode({Mode.AverageTime, Mode.Throughput})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(2)
public class GCPerformanceBenchmark {

    /** 启用GC的虚拟机实例 */
    private CymbolStackVM vmWithGC;

    /** 禁用GC的虚拟机实例（用于对比） */
    private CymbolStackVM vmWithoutGC;

    /** 小结构体分配字节码 */
    private byte[] smallStructAllocation;

    /** 中结构体分配字节码 */
    private byte[] mediumStructAllocation;

    /** 大结构体分配字节码 */
    private byte[] largeStructAllocation;

    /** 循环分配字节码 */
    private byte[] loopAllocation;

    /** 引用计数操作字节码 */
    private byte[] refCountingOperations;

    /**
     * 设置基准测试环境
     */
    @Setup
    public void setup() {
        // 创建启用GC的配置
        VMConfig configWithGC = VMConfig.builder()
                .setHeapSize(16 * 1024 * 1024) // 16MB堆
                .setStackSize(4096)
                .setMaxStackDepth(1000)
                .setDebugMode(false)
                .setEnableBoundsCheck(true)
                .setEnableTypeCheck(true)
                .setEnableGC(true)
                .build();

        // 创建禁用GC的配置
        VMConfig configWithoutGC = VMConfig.builder()
                .setHeapSize(16 * 1024 * 1024) // 16MB堆
                .setStackSize(4096)
                .setMaxStackDepth(1000)
                .setDebugMode(false)
                .setEnableBoundsCheck(true)
                .setEnableTypeCheck(true)
                .setEnableGC(false)
                .build();

        vmWithGC = new CymbolStackVM(configWithGC);
        vmWithoutGC = new CymbolStackVM(configWithoutGC);

        // 创建测试字节码
        smallStructAllocation = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_STRUCT, 2),  // 2字段结构体
            encodeInstruction(BytecodeDefinition.INSTR_POP),        // 弹出，放弃引用
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        mediumStructAllocation = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_STRUCT, 8),  // 8字段结构体
            encodeInstruction(BytecodeDefinition.INSTR_POP),
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        largeStructAllocation = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_STRUCT, 32), // 32字段结构体
            encodeInstruction(BytecodeDefinition.INSTR_POP),
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        // 循环分配：分配10个小结构体
        loopAllocation = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 0),  // i = 0
            encodeInstruction(BytecodeDefinition.INSTR_STORE, 0),   // store i
            // 循环开始
            encodeInstruction(BytecodeDefinition.INSTR_LOAD, 0),    // load i
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 10), // 10
            encodeInstruction(BytecodeDefinition.INSTR_ILT),        // i < 10
            encodeInstruction(BytecodeDefinition.INSTR_BRF, 12),    // 条件跳转
            encodeInstruction(BytecodeDefinition.INSTR_STRUCT, 2),  // 分配结构体
            encodeInstruction(BytecodeDefinition.INSTR_POP),        // 弹出
            encodeInstruction(BytecodeDefinition.INSTR_LOAD, 0),    // load i
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 1),  // 1
            encodeInstruction(BytecodeDefinition.INSTR_IADD),       // i++
            encodeInstruction(BytecodeDefinition.INSTR_STORE, 0),   // store i
            encodeInstruction(BytecodeDefinition.INSTR_BR, -16),    // 跳回循环开始
            // 循环结束
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 0),  // 返回0
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        // 引用计数操作：分配、增加引用、减少引用
        refCountingOperations = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_STRUCT, 4),  // 分配结构体
            encodeInstruction(BytecodeDefinition.INSTR_STORE, 0),   // 存储到局部变量0
            encodeInstruction(BytecodeDefinition.INSTR_LOAD, 0),    // 加载（增加引用）
            encodeInstruction(BytecodeDefinition.INSTR_STORE, 1),   // 存储到局部变量1（增加引用）
            encodeInstruction(BytecodeDefinition.INSTR_LOAD, 0),    // 加载（增加引用）
            encodeInstruction(BytecodeDefinition.INSTR_STORE, 2),   // 存储到局部变量2（增加引用）
            encodeInstruction(BytecodeDefinition.INSTR_NULL),       // 设置null（减少引用）
            encodeInstruction(BytecodeDefinition.INSTR_STORE, 1),   // 存储null到变量1（减少引用）
            encodeInstruction(BytecodeDefinition.INSTR_NULL),       // 设置null（减少引用）
            encodeInstruction(BytecodeDefinition.INSTR_STORE, 2),   // 存储null到变量2（减少引用）
            encodeInstruction(BytecodeDefinition.INSTR_NULL),       // 设置null（减少引用）
            encodeInstruction(BytecodeDefinition.INSTR_STORE, 0),   // 存储null到变量0（减少引用）
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 0),  // 返回0
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });
    }

    /**
     * 基准测试：小结构体分配性能（启用GC）
     */
    @Benchmark
    public int benchmarkSmallStructAllocationWithGC() throws Exception {
        return vmWithGC.execute(smallStructAllocation);
    }

    /**
     * 基准测试：小结构体分配性能（禁用GC）
     */
    @Benchmark
    public int benchmarkSmallStructAllocationWithoutGC() throws Exception {
        return vmWithoutGC.execute(smallStructAllocation);
    }

    /**
     * 基准测试：中结构体分配性能（启用GC）
     */
    @Benchmark
    public int benchmarkMediumStructAllocationWithGC() throws Exception {
        return vmWithGC.execute(mediumStructAllocation);
    }

    /**
     * 基准测试：中结构体分配性能（禁用GC）
     */
    @Benchmark
    public int benchmarkMediumStructAllocationWithoutGC() throws Exception {
        return vmWithoutGC.execute(mediumStructAllocation);
    }

    /**
     * 基准测试：大结构体分配性能（启用GC）
     */
    @Benchmark
    public int benchmarkLargeStructAllocationWithGC() throws Exception {
        return vmWithGC.execute(largeStructAllocation);
    }

    /**
     * 基准测试：大结构体分配性能（禁用GC）
     */
    @Benchmark
    public int benchmarkLargeStructAllocationWithoutGC() throws Exception {
        return vmWithoutGC.execute(largeStructAllocation);
    }

    /**
     * 基准测试：循环分配性能（启用GC）
     * 测量GC在连续分配场景下的表现
     */
    @Benchmark
    public int benchmarkLoopAllocationWithGC() throws Exception {
        return vmWithGC.execute(loopAllocation);
    }

    /**
     * 基准测试：循环分配性能（禁用GC）
     */
    @Benchmark
    public int benchmarkLoopAllocationWithoutGC() throws Exception {
        return vmWithoutGC.execute(loopAllocation);
    }

    /**
     * 基准测试：引用计数操作性能
     * 测量引用计数管理的开销
     */
    @Benchmark
    public int benchmarkRefCountingOperations() throws Exception {
        return vmWithGC.execute(refCountingOperations);
    }

    /**
     * 基准测试：GC开销对比
     * 执行相同操作，比较启用和禁用GC的性能差异
     */
    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void benchmarkGCOverheadComparison() throws Exception {
        // 执行1000次小结构体分配
        for (int i = 0; i < 1000; i++) {
            vmWithGC.execute(smallStructAllocation);
        }
    }

    /**
     * 基准测试：内存压力测试
     * 在内存压力下测试GC性能
     */
    @Benchmark
    public int benchmarkMemoryPressureTest() throws Exception {
        // 分配大量小对象，模拟内存压力
        byte[] pressureTest = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 0),  // i = 0
            encodeInstruction(BytecodeDefinition.INSTR_STORE, 0),
            // 循环开始
            encodeInstruction(BytecodeDefinition.INSTR_LOAD, 0),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 100), // 分配100个对象
            encodeInstruction(BytecodeDefinition.INSTR_ILT),
            encodeInstruction(BytecodeDefinition.INSTR_BRF, 10),
            encodeInstruction(BytecodeDefinition.INSTR_STRUCT, 1),  // 1字段结构体
            encodeInstruction(BytecodeDefinition.INSTR_POP),
            encodeInstruction(BytecodeDefinition.INSTR_LOAD, 0),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 1),
            encodeInstruction(BytecodeDefinition.INSTR_IADD),
            encodeInstruction(BytecodeDefinition.INSTR_STORE, 0),
            encodeInstruction(BytecodeDefinition.INSTR_BR, -16),
            // 循环结束
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 0),
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        return vmWithGC.execute(pressureTest);
    }

    /**
     * 基准测试：GC暂停时间
     * 测量垃圾回收导致的停顿时间
     */
    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public long benchmarkGCPauseTime() throws Exception {
        // 首先分配大量对象
        byte[] allocateMany = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 0),
            encodeInstruction(BytecodeDefinition.INSTR_STORE, 0),
            encodeInstruction(BytecodeDefinition.INSTR_LOAD, 0),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 500),
            encodeInstruction(BytecodeDefinition.INSTR_ILT),
            encodeInstruction(BytecodeDefinition.INSTR_BRF, 8),
            encodeInstruction(BytecodeDefinition.INSTR_STRUCT, 2),
            encodeInstruction(BytecodeDefinition.INSTR_POP),
            encodeInstruction(BytecodeDefinition.INSTR_LOAD, 0),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 1),
            encodeInstruction(BytecodeDefinition.INSTR_IADD),
            encodeInstruction(BytecodeDefinition.INSTR_STORE, 0),
            encodeInstruction(BytecodeDefinition.INSTR_BR, -16),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 0),
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        vmWithGC.execute(allocateMany);

        // 手动触发GC并测量时间
        long startTime = System.nanoTime();
        // 注意：这里需要调用GC的collect方法，但需要通过反射或其他方式访问
        // 暂时返回0，实际实现时需要修改
        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    // ====================
    // 工具方法
    // ====================

    /**
     * 从指令数组创建字节码
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

    /**
     * 编码带参数的指令
     */
    private int encodeInstruction(int opcode, int param) {
        return (opcode << 24) | (param & 0xFFFFFF);
    }

    /**
     * 编码无参数的指令
     */
    private int encodeInstruction(int opcode) {
        return opcode << 24;
    }
}