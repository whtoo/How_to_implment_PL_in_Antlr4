package org.teachfx.antlr4.ep18.performance;

import org.openjdk.jmh.annotations.*;
import org.teachfx.antlr4.ep18.stackvm.CymbolStackVM;
import org.teachfx.antlr4.ep18.stackvm.VMConfig;

import java.util.concurrent.TimeUnit;

/**
 * Memory Access Performance Benchmarks for EP18
 *
 * This benchmark suite specifically focuses on memory access patterns:
 * - Stack push/pop operations
 * - Heap allocation
 * - Array access
 * - Local variable access
 *
 * These benchmarks help establish performance baselines for memory-intensive operations
 * and identify optimization opportunities in memory access patterns.
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
public class MemoryAccessBenchmark {

    /** VM instance for benchmarking */
    private CymbolStackVM vm;

    /** Test VM configuration */
    private VMConfig config;

    /**
     * Set up the benchmark environment
     */
    @Setup
    public void setup() {
        config = new VMConfig.Builder()
            .setHeapSize(1024 * 1024)
            .setStackSize(1024)
            .setMaxStackDepth(100)
            .setDebugMode(false)
            .setVerboseErrors(false)
            .setEnableBoundsCheck(true)
            .setEnableTypeCheck(true)
            .build();

        vm = new CymbolStackVM(config);
    }

    /**
     * Benchmark: Stack push operation
     * Tests: iconst instruction (push to stack)
     */
    @Benchmark
    public int benchmarkStackPush() throws Exception {
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(1, 42),  // iconst 42
            encodeInstruction(0)       // halt
        });
        return vm.execute(bytecode);
    }

    /**
     * Benchmark: Stack pop operation
     * Tests: Multiple pushes followed by halt (implicit pop)
     */
    @Benchmark
    public int benchmarkStackPop() throws Exception {
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(1, 1),   // iconst 1
            encodeInstruction(1, 2),   // iconst 2
            encodeInstruction(1, 3),   // iconst 3
            encodeInstruction(0)       // halt
        });
        return vm.execute(bytecode);
    }

    /**
     * Benchmark: Stack depth operations
     * Tests: Deep stack operations
     */
    @Benchmark
    public int benchmarkStackDepth() throws Exception {
        // Create bytecode with 50 stack pushes
        int[] instructions = new int[52];
        instructions[0] = encodeInstruction(1, 0);  // iconst 0
        for (int i = 1; i < 51; i++) {
            instructions[i] = encodeInstruction(1, i);  // iconst i
        }
        instructions[51] = encodeInstruction(0);  // halt

        byte[] bytecode = createBytecode(instructions);
        return vm.execute(bytecode);
    }

    /**
     * Benchmark: Heap allocation
     * Tests: anewarray instruction
     */
    @Benchmark
    public int benchmarkHeapAllocation() throws Exception {
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(1, 100), // iconst 100
            encodeInstruction(20),     // anewarray
            encodeInstruction(0)       // halt
        });
        return vm.execute(bytecode);
    }

    /**
     * Benchmark: Array write operation
     * Tests: iastore instruction
     */
    @Benchmark
    public int benchmarkArrayWrite() throws Exception {
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(1, 10),  // iconst 10
            encodeInstruction(20),     // anewarray
            encodeInstruction(1, 0),   // iconst 0
            encodeInstruction(1, 999), // iconst 999
            encodeInstruction(21),     // iastore
            encodeInstruction(0)       // halt
        });
        return vm.execute(bytecode);
    }

    /**
     * Benchmark: Array read operation
     * Tests: iaload instruction
     */
    @Benchmark
    public int benchmarkArrayRead() throws Exception {
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(1, 10),  // iconst 10
            encodeInstruction(20),     // anewarray
            encodeInstruction(1, 0),   // iconst 0
            encodeInstruction(1, 42),  // iconst 42
            encodeInstruction(21),     // iastore
            encodeInstruction(22, 0),  // iaload
            encodeInstruction(0)       // halt
        });
        return vm.execute(bytecode);
    }

    /**
     * Benchmark: Sequential array access
     * Tests: Pattern of array writes and reads
     */
    @Benchmark
    public int benchmarkSequentialArrayAccess() throws Exception {
        // Write 5 values, then read them back
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(1, 5),   // iconst 5
            encodeInstruction(20),     // anewarray

            // Write 0: 10
            encodeInstruction(1, 0),   // iconst 0
            encodeInstruction(1, 10),  // iconst 10
            encodeInstruction(21),     // iastore

            // Write 1: 20
            encodeInstruction(1, 1),   // iconst 1
            encodeInstruction(1, 20),  // iconst 20
            encodeInstruction(21),     // iastore

            // Write 2: 30
            encodeInstruction(1, 2),   // iconst 2
            encodeInstruction(1, 30),  // iconst 30
            encodeInstruction(21),     // iastore

            // Write 3: 40
            encodeInstruction(1, 3),   // iconst 3
            encodeInstruction(1, 40),  // iconst 40
            encodeInstruction(21),     // iastore

            // Write 4: 50
            encodeInstruction(1, 4),   // iconst 4
            encodeInstruction(1, 50),  // iconst 50
            encodeInstruction(21),     // iastore

            // Read back
            encodeInstruction(22, 0),  // iaload
            encodeInstruction(22, 1),  // iaload
            encodeInstruction(22, 2),  // iaload
            encodeInstruction(22, 3),  // iaload
            encodeInstruction(22, 4),  // iaload

            encodeInstruction(0)       // halt
        });
        return vm.execute(bytecode);
    }

    /**
     * Benchmark: Local variable store
     * Tests: istore instruction
     */
    @Benchmark
    public int benchmarkLocalVariableStore() throws Exception {
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(1, 100), // iconst 100
            encodeInstruction(30, 0),  // istore 0
            encodeInstruction(1, 200), // iconst 200
            encodeInstruction(30, 1),  // istore 1
            encodeInstruction(1, 300), // iconst 300
            encodeInstruction(30, 2),  // istore 2
            encodeInstruction(0)       // halt
        });
        return vm.execute(bytecode);
    }

    /**
     * Benchmark: Local variable load
     * Tests: load instruction
     */
    @Benchmark
    public int benchmarkLocalVariableLoad() throws Exception {
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(1, 100), // iconst 100
            encodeInstruction(30, 0),  // istore 0

            encodeInstruction(31, 0),  // load 0
            encodeInstruction(31, 0),  // load 0
            encodeInstruction(2),      // iadd

            encodeInstruction(31, 0),  // load 0
            encodeInstruction(3),      // imul

            encodeInstruction(0)       // halt
        });
        return vm.execute(bytecode);
    }

    /**
     * Benchmark: Mixed load/store operations
     * Tests: Pattern of stores and loads
     */
    @Benchmark
    public int benchmarkMixedLoadStore() throws Exception {
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(1, 10),  // iconst 10
            encodeInstruction(30, 0),  // istore 0

            encodeInstruction(1, 20),  // iconst 20
            encodeInstruction(30, 1),  // istore 1

            encodeInstruction(1, 30),  // iconst 30
            encodeInstruction(30, 2),  // istore 2

            // Load and compute
            encodeInstruction(31, 0),  // load 0
            encodeInstruction(31, 1),  // load 1
            encodeInstruction(2),      // iadd

            encodeInstruction(31, 2),  // load 2
            encodeInstruction(3),      // imul

            encodeInstruction(0)       // halt
        });
        return vm.execute(bytecode);
    }

    /**
     * Benchmark: Memory pattern - write-heavy
     * Tests: Multiple store operations
     */
    @Benchmark
    public int benchmarkWriteHeavyPattern() throws Exception {
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(1, 1),   // iconst 1
            encodeInstruction(30, 0),  // istore 0
            encodeInstruction(1, 2),   // iconst 2
            encodeInstruction(30, 1),  // istore 1
            encodeInstruction(1, 3),   // iconst 3
            encodeInstruction(30, 2),  // istore 2
            encodeInstruction(1, 4),   // iconst 4
            encodeInstruction(30, 3),  // istore 3
            encodeInstruction(1, 5),   // iconst 5
            encodeInstruction(30, 4),  // istore 4
            encodeInstruction(0)       // halt
        });
        return vm.execute(bytecode);
    }

    /**
     * Benchmark: Memory pattern - read-heavy
     * Tests: Multiple load operations
     */
    @Benchmark
    public int benchmarkReadHeavyPattern() throws Exception {
        // Pre-populate variables
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(1, 10),  // iconst 10
            encodeInstruction(30, 0),  // istore 0
            encodeInstruction(1, 20),  // iconst 20
            encodeInstruction(30, 1),  // istore 1
            encodeInstruction(1, 30),  // iconst 30
            encodeInstruction(30, 2),  // istore 2
            encodeInstruction(1, 40),  // iconst 40
            encodeInstruction(30, 3),  // istore 3
            encodeInstruction(1, 50),  // iconst 50
            encodeInstruction(30, 4),  // istore 4

            // Read-heavy computation
            encodeInstruction(31, 0),  // load 0
            encodeInstruction(31, 1),  // load 1
            encodeInstruction(31, 2),  // load 2
            encodeInstruction(31, 3),  // load 3
            encodeInstruction(31, 4),  // load 4
            encodeInstruction(2),      // iadd (0+1)
            encodeInstruction(2),      // iadd (result+2)
            encodeInstruction(2),      // iadd (result+3)
            encodeInstruction(2),      // iadd (result+4)

            encodeInstruction(0)       // halt
        });
        return vm.execute(bytecode);
    }

    // ====================
    // Utility Methods
    // ====================

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

    private int encodeInstruction(int opcode, int param) {
        return (opcode << 24) | (param & 0xFFFFFF);
    }

    private int encodeInstruction(int opcode) {
        return opcode << 24;
    }
}
