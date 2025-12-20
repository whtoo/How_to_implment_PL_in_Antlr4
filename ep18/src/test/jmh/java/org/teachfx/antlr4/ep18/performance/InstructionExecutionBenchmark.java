package org.teachfx.antlr4.ep18.performance;

import org.openjdk.jmh.annotations.*;
import org.teachfx.antlr4.ep18.stackvm.CymbolStackVM;
import org.teachfx.antlr4.ep18.stackvm.VMConfig;

import java.util.concurrent.TimeUnit;

/**
 * Instruction Execution Performance Benchmarks for EP18
 *
 * This benchmark suite measures the performance of individual VM instructions
 * and instruction execution patterns to establish performance baselines.
 *
 * Benchmark results help identify:
 * - Hot spots in instruction execution
 * - Performance regressions
 * - Optimization opportunities
 *
 * @author EP18 TDD Refactoring Team
 * @version 1.0.0
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class InstructionExecutionBenchmark {

    /** VM instance for benchmarking */
    private CymbolStackVM vm;

    /** Test VM configuration */
    private VMConfig config;

    /** Sample bytecode for simple arithmetic */
    private byte[] addBytecode;

    /** Sample bytecode for complex computation */
    private byte[] computeBytecode;

    /** Sample bytecode for function call */
    private byte[] callBytecode;

    /**
     * Set up the benchmark environment
     */
    @Setup
    public void setup() {
        // Create test configuration
        config = new VMConfig.Builder()
            .setHeapSize(1024 * 1024)
            .setStackSize(1024)
            .setMaxStackDepth(100)
            .setDebugMode(false) // Disable debug mode for accurate benchmarking
            .setVerboseErrors(false)
            .setEnableBoundsCheck(true)
            .setEnableTypeCheck(true)
            .build();

        // Create VM instance
        vm = new CymbolStackVM(config);

        // Create simple addition bytecode: iconst 10, iconst 20, iadd, halt
        addBytecode = createBytecode(new int[]{
            encodeInstruction(1, 10),  // iconst 10
            encodeInstruction(1, 20),  // iconst 20
            encodeInstruction(2),      // iadd
            encodeInstruction(0)       // halt
        });

        // Create complex computation bytecode
        computeBytecode = createBytecode(new int[]{
            encodeInstruction(1, 10),  // iconst 10
            encodeInstruction(1, 20),  // iconst 20
            encodeInstruction(1, 5),   // iconst 5
            encodeInstruction(1, 3),   // iconst 3
            encodeInstruction(2),      // iadd (10 + 20)
            encodeInstruction(3),      // imul ((10+20) * 5)
            encodeInstruction(4),      // idiv ((10+20)*5 / 3)
            encodeInstruction(0)       // halt
        });

        // Create function call bytecode (simplified)
        callBytecode = createBytecode(new int[]{
            encodeInstruction(1, 5),   // iconst 5
            encodeInstruction(1, 3),   // iconst 3
            encodeInstruction(50),     // call
            encodeInstruction(0)       // halt
        });
    }

    /**
     * Benchmark: Simple addition instruction
     * Tests: iconst, iadd, halt
     */
    @Benchmark
    public int benchmarkSimpleAdd() throws Exception {
        return vm.execute(addBytecode);
    }

    /**
     * Benchmark: Complex arithmetic computation
     * Tests: Multiple arithmetic operations
     */
    @Benchmark
    public int benchmarkComplexComputation() throws Exception {
        return vm.execute(computeBytecode);
    }

    /**
     * Benchmark: Function call overhead
     * Tests: call instruction performance
     */
    @Benchmark
    public int benchmarkFunctionCall() throws Exception {
        return vm.execute(callBytecode);
    }

    /**
     * Benchmark: Stack push/pop operations
     * Tests: Stack manipulation overhead
     */
    @Benchmark
    public int benchmarkStackOperations() throws Exception {
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(1, 1),   // iconst 1
            encodeInstruction(1, 2),   // iconst 2
            encodeInstruction(1, 3),   // iconst 3
            encodeInstruction(1, 4),   // iconst 4
            encodeInstruction(1, 5),   // iconst 5
            encodeInstruction(0)       // halt
        });
        return vm.execute(bytecode);
    }

    /**
     * Benchmark: Comparison operations
     * Tests: ilt, igt, ieq performance
     */
    @Benchmark
    public int benchmarkComparisons() throws Exception {
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(1, 10),  // iconst 10
            encodeInstruction(1, 20),  // iconst 20
            encodeInstruction(10),     // ilt
            encodeInstruction(1, 30),  // iconst 30
            encodeInstruction(1, 30),  // iconst 30
            encodeInstruction(12),     // ieq
            encodeInstruction(0)       // halt
        });
        return vm.execute(bytecode);
    }

    /**
     * Benchmark: Load/Store operations
     * Tests: Local variable access performance
     */
    @Benchmark
    public int benchmarkLoadStore() throws Exception {
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(1, 42),  // iconst 42
            encodeInstruction(30, 0),  // istore 0
            encodeInstruction(30, 1),  // istore 1
            encodeInstruction(31, 0),  // load 0
            encodeInstruction(31, 1),  // load 1
            encodeInstruction(2),      // iadd
            encodeInstruction(0)       // halt
        });
        return vm.execute(bytecode);
    }

    /**
     * Benchmark: Branch operations
     * Tests: Conditional branch performance
     */
    @Benchmark
    public int benchmarkBranches() throws Exception {
        // Simplified branch bytecode (actual implementation would need labels)
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(1, 10),  // iconst 10
            encodeInstruction(1, 5),   // iconst 5
            encodeInstruction(10),     // ilt
            encodeInstruction(40, 8),  // brf (jump 8 instructions)
            encodeInstruction(1, 1),   // iconst 1
            encodeInstruction(0),      // halt
            encodeInstruction(1, 0),   // iconst 0
            encodeInstruction(0)       // halt
        });
        return vm.execute(bytecode);
    }

    /**
     * Benchmark: Array operations
     * Tests: Memory access performance
     */
    @Benchmark
    public int benchmarkArrayAccess() throws Exception {
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(1, 5),   // iconst 5
            encodeInstruction(20),     // anewarray
            encodeInstruction(1, 0),   // iconst 0
            encodeInstruction(1, 100), // iconst 100
            encodeInstruction(21),     // iastore
            encodeInstruction(22, 0),  // iaload
            encodeInstruction(0)       // halt
        });
        return vm.execute(bytecode);
    }

    /**
     * Benchmark: Loop execution
     * Tests: Control flow overhead
     */
    @Benchmark
    public int benchmarkLoop() throws Exception {
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(1, 0),   // iconst 0
            encodeInstruction(30, 0),  // istore 0
            // Loop start (simplified - would need proper label handling)
            encodeInstruction(31, 0),  // load 0
            encodeInstruction(1, 10),  // iconst 10
            encodeInstruction(11),     // igt
            encodeInstruction(40, 16), // brf to end (jump 16 instructions)
            encodeInstruction(31, 0),  // load 0
            encodeInstruction(1, 1),   // iconst 1
            encodeInstruction(2),      // iadd
            encodeInstruction(30, 0),  // istore 0
            encodeInstruction(41, 0),  // goto loop start
            // Loop end
            encodeInstruction(1, 1),   // iconst 1
            encodeInstruction(0)       // halt
        });
        return vm.execute(bytecode);
    }

    // ====================
    // Utility Methods
    // ====================

    /**
     * Create bytecode from instruction array
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
     * Encode instruction with parameter
     */
    private int encodeInstruction(int opcode, int param) {
        return (opcode << 24) | (param & 0xFFFFFF);
    }

    /**
     * Encode instruction without parameter
     */
    private int encodeInstruction(int opcode) {
        return opcode << 24;
    }
}
