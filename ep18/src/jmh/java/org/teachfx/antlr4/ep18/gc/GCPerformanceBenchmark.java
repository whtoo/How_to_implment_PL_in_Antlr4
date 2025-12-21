package org.teachfx.antlr4.ep18.gc;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * GC性能基准测试
 *
 * 使用JMH框架对垃圾回收器进行全面的性能基准测试。
 * 测试包括：对象分配性能、垃圾回收性能、内存使用效率等。
 *
 * @author EP18 TDD Refactoring Team
 * @version 1.0.0
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@Threads(1)
public class GCPerformanceBenchmark {

    // ====================
    // 基准测试状态
    // ====================

    @State(Scope.Thread)
    public static class GCState {
        ReferenceCountingGC gc;
        List<Integer> objectIds;

        @Setup(Level.Iteration)
        public void setup() {
            gc = new ReferenceCountingGC(1024 * 1024 * 10); // 10MB堆大小
            objectIds = new ArrayList<>();
        }

        @TearDown(Level.Iteration)
        public void tearDown() {
            // 清理所有对象
            for (int objectId : objectIds) {
                gc.decrementRef(objectId);
            }
            gc.collect();
            gc.clearAll();
            objectIds.clear();
        }
    }

    @State(Scope.Thread)
    public static class LargeGCState {
        ReferenceCountingGC gc;
        List<Integer> objectIds;

        @Setup(Level.Iteration)
        public void setup() {
            gc = new ReferenceCountingGC(1024 * 1024 * 100); // 100MB堆大小
            objectIds = new ArrayList<>();
        }

        @TearDown(Level.Iteration)
        public void tearDown() {
            for (int objectId : objectIds) {
                gc.decrementRef(objectId);
            }
            gc.collect();
            gc.clearAll();
            objectIds.clear();
        }
    }

    // ====================
    // 对象分配性能基准测试
    // ====================

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public void benchmarkObjectAllocationSmall(GCState state, Blackhole blackhole) {
        // 测试小对象分配性能
        int objectId = state.gc.allocate(100);
        state.gc.incrementRef(objectId);
        state.objectIds.add(objectId);
        blackhole.consume(objectId);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public void benchmarkObjectAllocationMedium(GCState state, Blackhole blackhole) {
        // 测试中等对象分配性能
        int objectId = state.gc.allocate(1024); // 1KB
        state.gc.incrementRef(objectId);
        state.objectIds.add(objectId);
        blackhole.consume(objectId);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public void benchmarkObjectAllocationLarge(GCState state, Blackhole blackhole) {
        // 测试大对象分配性能
        int objectId = state.gc.allocate(1024 * 10); // 10KB
        state.gc.incrementRef(objectId);
        state.objectIds.add(objectId);
        blackhole.consume(objectId);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public void benchmarkBulkObjectAllocation(GCState state, Blackhole blackhole) {
        // 测试批量对象分配性能
        int batchSize = 100;
        for (int i = 0; i < batchSize; i++) {
            int objectId = state.gc.allocate(100);
            state.gc.incrementRef(objectId);
            state.objectIds.add(objectId);
            blackhole.consume(objectId);
        }
    }

    // ====================
    // 引用计数操作性能基准测试
    // ====================

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public void benchmarkReferenceIncrement(GCState state, Blackhole blackhole) {
        // 测试引用增加性能
        if (state.objectIds.isEmpty()) {
            int objectId = state.gc.allocate(100);
            state.objectIds.add(objectId);
        }

        int objectId = state.objectIds.get(0);
        state.gc.incrementRef(objectId);
        blackhole.consume(objectId);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public void benchmarkReferenceDecrement(GCState state, Blackhole blackhole) {
        // 测试引用减少性能
        if (state.objectIds.isEmpty()) {
            int objectId = state.gc.allocate(100);
            state.gc.incrementRef(objectId); // 先增加引用
            state.objectIds.add(objectId);
        }

        int objectId = state.objectIds.get(0);
        state.gc.decrementRef(objectId);
        blackhole.consume(objectId);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public void benchmarkReferenceCycle(GCState state, Blackhole blackhole) {
        // 测试完整的引用周期（分配->增加->减少）
        int objectId = state.gc.allocate(100);
        state.gc.incrementRef(objectId);
        state.gc.decrementRef(objectId);
        blackhole.consume(objectId);
    }

    // ====================
    // 垃圾回收性能基准测试
    // ====================

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 0) // SingleShotTime不需要预热
    @Measurement(iterations = 10)
    public void benchmarkGarbageCollectionEmpty(LargeGCState state) {
        // 测试空堆的垃圾回收性能
        state.gc.collect();
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 0)
    @Measurement(iterations = 10)
    public void benchmarkGarbageCollectionWithObjects(LargeGCState state) {
        // 测试有对象的垃圾回收性能
        // 创建一些对象
        int objectCount = 1000;
        for (int i = 0; i < objectCount; i++) {
            int objectId = state.gc.allocate(100);
            state.gc.incrementRef(objectId);
            state.objectIds.add(objectId);
        }

        // 释放所有对象
        for (int objectId : state.objectIds) {
            state.gc.decrementRef(objectId);
        }

        // 执行垃圾回收
        state.gc.collect();

        // 清理状态
        state.objectIds.clear();
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 0)
    @Measurement(iterations = 10)
    public void benchmarkGarbageCollectionPartial(LargeGCState state) {
        // 测试部分对象回收的性能
        int objectCount = 2000;
        List<Integer> objectsToKeep = new ArrayList<>();

        // 创建对象，部分保留引用
        for (int i = 0; i < objectCount; i++) {
            int objectId = state.gc.allocate(100);
            state.gc.incrementRef(objectId);
            state.objectIds.add(objectId);

            // 一半对象保留引用
            if (i % 2 == 0) {
                objectsToKeep.add(objectId);
            }
        }

        // 释放一半对象
        for (int i = 0; i < objectCount; i++) {
            if (i % 2 != 0) {
                state.gc.decrementRef(state.objectIds.get(i));
            }
        }

        // 执行垃圾回收
        state.gc.collect();

        // 清理：释放保留的对象
        for (int objectId : objectsToKeep) {
            state.gc.decrementRef(objectId);
        }
        state.gc.collect();
        state.objectIds.clear();
    }

    // ====================
    // 内存使用效率基准测试
    // ====================

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public void benchmarkMemoryEfficiencySmallObjects(GCState state, Blackhole blackhole) {
        // 测试小对象的内存使用效率
        int objectCount = 100;
        int totalAllocated = 0;

        for (int i = 0; i < objectCount; i++) {
            int objectId = state.gc.allocate(10); // 10字节小对象
            state.gc.incrementRef(objectId);
            state.objectIds.add(objectId);
            totalAllocated += 10;
            blackhole.consume(objectId);
        }

        // 计算内存使用效率
        int heapUsage = state.gc.getHeapUsage();
        double efficiency = (double) heapUsage / totalAllocated * 100;
        blackhole.consume(efficiency);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public void benchmarkMemoryEfficiencyLargeObjects(GCState state, Blackhole blackhole) {
        // 测试大对象的内存使用效率
        int objectCount = 10;
        int totalAllocated = 0;

        for (int i = 0; i < objectCount; i++) {
            int objectId = state.gc.allocate(1024 * 10); // 10KB大对象
            state.gc.incrementRef(objectId);
            state.objectIds.add(objectId);
            totalAllocated += 1024 * 10;
            blackhole.consume(objectId);
        }

        // 计算内存使用效率
        int heapUsage = state.gc.getHeapUsage();
        double efficiency = (double) heapUsage / totalAllocated * 100;
        blackhole.consume(efficiency);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public void benchmarkMemoryFragmentation(GCState state, Blackhole blackhole) {
        // 测试内存碎片化影响
        // 分配和释放不同大小的对象，模拟碎片化

        // 分配不同大小的对象
        int[] sizes = {10, 100, 1000, 10000};
        List<Integer> allocatedObjects = new ArrayList<>();

        for (int size : sizes) {
            for (int i = 0; i < 10; i++) {
                int objectId = state.gc.allocate(size);
                state.gc.incrementRef(objectId);
                allocatedObjects.add(objectId);
                blackhole.consume(objectId);
            }
        }

        // 释放一半对象（交替释放）
        for (int i = 0; i < allocatedObjects.size(); i++) {
            if (i % 2 == 0) {
                state.gc.decrementRef(allocatedObjects.get(i));
            }
        }

        // 执行垃圾回收
        state.gc.collect();

        // 分配新对象（可能遇到碎片）
        int newObjectId = state.gc.allocate(5000);
        state.gc.incrementRef(newObjectId);
        state.objectIds.add(newObjectId);
        blackhole.consume(newObjectId);

        // 清理
        for (int i = 0; i < allocatedObjects.size(); i++) {
            if (i % 2 != 0) {
                state.gc.decrementRef(allocatedObjects.get(i));
            }
        }
        state.gc.decrementRef(newObjectId);
        state.gc.collect();
    }

    // ====================
    // 并发性能基准测试
    // ====================

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    @Threads(4) // 多线程测试
    public void benchmarkConcurrentAllocation(LargeGCState state, Blackhole blackhole) {
        // 测试并发分配性能
        int objectId = state.gc.allocate(100);
        state.gc.incrementRef(objectId);
        state.objectIds.add(objectId);
        blackhole.consume(objectId);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    @Threads(4)
    public void benchmarkConcurrentReferenceOperations(LargeGCState state, Blackhole blackhole) {
        // 测试并发引用操作性能
        if (state.objectIds.isEmpty()) {
            int objectId = state.gc.allocate(100);
            state.gc.incrementRef(objectId);
            state.objectIds.add(objectId);
        }

        int objectId = state.objectIds.get(0);
        // 交替执行增加和减少引用
        if (Thread.currentThread().getId() % 2 == 0) {
            state.gc.incrementRef(objectId);
        } else {
            state.gc.decrementRef(objectId);
        }
        blackhole.consume(objectId);
    }

    // ====================
    // 辅助方法
    // ====================

    /**
     * 主方法：运行所有基准测试
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(GCPerformanceBenchmark.class.getSimpleName())
            .shouldFailOnError(true)
            .shouldDoGC(true)
            .build();

        new Runner(opt).run();
    }

    /**
     * 运行特定基准测试组
     */
    public static void runAllocationBenchmarks() throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(GCPerformanceBenchmark.class.getSimpleName() + ".benchmarkObjectAllocation.*")
            .shouldFailOnError(true)
            .shouldDoGC(true)
            .build();

        new Runner(opt).run();
    }

    /**
     * 运行垃圾回收基准测试
     */
    public static void runGCBenchmarks() throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(GCPerformanceBenchmark.class.getSimpleName() + ".benchmarkGarbageCollection.*")
            .shouldFailOnError(true)
            .shouldDoGC(true)
            .build();

        new Runner(opt).run();
    }

    /**
     * 运行内存效率基准测试
     */
    public static void runMemoryEfficiencyBenchmarks() throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(GCPerformanceBenchmark.class.getSimpleName() + ".benchmarkMemoryEfficiency.*")
            .shouldFailOnError(true)
            .shouldDoGC(true)
            .build();

        new Runner(opt).run();
    }
}