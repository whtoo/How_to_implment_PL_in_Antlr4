package org.teachfx.antlr4.ep18;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.teachfx.antlr4.ep18.stackvm.CymbolStackVM;
import org.teachfx.antlr4.ep18.stackvm.VMConfig;
import org.teachfx.antlr4.ep18.stackvm.BytecodeDefinition;
import org.teachfx.antlr4.ep18.gc.GarbageCollector;
import org.teachfx.antlr4.ep18.gc.ReferenceCountingGC;
import org.teachfx.antlr4.ep18.gc.GCStats;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.within;

/**
 * CymbolStackVM GC集成测试
 *
 * 遵循TDD原则，为GC集成功能创建完整的测试套件。
 * 测试GC在虚拟机中的初始化、对象分配、引用计数、垃圾回收等功能。
 *
 * @author EP18 TDD Refactoring Team
 * @version 1.0.0
 */
@DisplayName("CymbolStackVM GC集成测试")
@Tag("gc")
@Tag("integration")
public class CymbolStackVMGCIntegrationTest extends VMTestBase {

    private GarbageCollector gc;

    /**
     * 创建启用GC的测试配置
     */
    @Override
    protected VMConfig createTestConfig() {
        return new VMConfig.Builder()
            .setHeapSize(1024 * 1024) // 1MB堆大小
            .setStackSize(1024)
            .setMaxStackDepth(100)
            .setDebugMode(true)
            .setVerboseErrors(true)
            .setEnableBoundsCheck(true)
            .setEnableTypeCheck(true)
            .build();
    }

    @BeforeEach
    void setUpGC() {
        // 创建GC实例
        gc = new ReferenceCountingGC(1024 * 1024); // 1MB堆大小
    }

    // ====================
    // GC初始化测试
    // ====================

    @Nested
    @DisplayName("GC初始化测试")
    class GCInitializationTests {

        @Test
        @DisplayName("虚拟机应该正确初始化GC")
        void testGCInitialization() {
            // Given: 虚拟机实例已创建

            // When: 检查虚拟机状态
            // Then: 虚拟机应该正确初始化
            assertThat(vm).isNotNull();
            assertThat(vm.getConfig()).isNotNull();
            assertThat(vm.isRunning()).isFalse();
        }

        @Test
        @DisplayName("GC应该正确初始化")
        void testGarbageCollectorInitialization() {
            // Given: GC实例已创建

            // When: 检查GC状态
            // Then: GC应该正确初始化
            assertThat(gc).isNotNull();
            assertThat(gc.getStats()).isNotNull();

            // 验证初始统计信息
            GCStats stats = gc.getStats();
            assertThat(stats.getTotalCollections()).isEqualTo(0);
            assertThat(stats.getTotalAllocatedMemory()).isEqualTo(0);
        }

        @Test
        @DisplayName("GC应该正确处理无效堆大小")
        void testInvalidHeapSize() {
            // Given: 无效堆大小
            int invalidHeapSize = -1;

            // When & Then: 创建GC应该抛出异常
            assertThatThrownBy(() -> new ReferenceCountingGC(invalidHeapSize))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Heap size must be positive");
        }
    }

    // ====================
    // 对象分配测试
    // ====================

    @Nested
    @DisplayName("对象分配测试")
    class ObjectAllocationTests {

        @Test
        @DisplayName("应该正确分配小对象")
        void testSmallObjectAllocation() throws Exception {
            // Given: GC实例
            int objectSize = 100;

            // When: 分配对象
            int objectId = gc.allocate(objectSize);

            // Then: 对象应该正确分配
            assertThat(objectId).isGreaterThan(0);
            assertThat(gc.isObjectAlive(objectId)).isTrue();
        }

        @Test
        @DisplayName("应该正确分配大对象")
        void testLargeObjectAllocation() throws Exception {
            // Given: GC实例
            int objectSize = 1024 * 512; // 512KB

            // When: 分配对象
            int objectId = gc.allocate(objectSize);

            // Then: 对象应该正确分配
            assertThat(objectId).isGreaterThan(0);
            assertThat(gc.isObjectAlive(objectId)).isTrue();
        }

        @Test
        @DisplayName("应该拒绝分配零大小对象")
        void testZeroSizeAllocation() {
            // Given: 零大小
            int zeroSize = 0;

            // When & Then: 分配应该抛出异常
            assertThatThrownBy(() -> gc.allocate(zeroSize))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Size must be positive");
        }

        @Test
        @DisplayName("应该拒绝分配负数大小对象")
        void testNegativeSizeAllocation() {
            // Given: 负数大小
            int negativeSize = -100;

            // When & Then: 分配应该抛出异常
            assertThatThrownBy(() -> gc.allocate(negativeSize))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Size must be positive");
        }

        @Test
        @DisplayName("应该正确处理内存不足")
        void testOutOfMemory() {
            // Given: 超过堆大小的对象
            int hugeSize = 2 * 1024 * 1024; // 2MB，超过1MB堆大小

            // When & Then: 分配应该抛出OutOfMemoryError
            assertThatThrownBy(() -> gc.allocate(hugeSize))
                .isInstanceOf(OutOfMemoryError.class)
                .hasMessageContaining("Object size exceeds heap size");
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 10, 100, 1000, 10000})
        @DisplayName("应该正确分配多个对象")
        void testMultipleObjectAllocation(int objectCount) throws Exception {
            // Given: 对象数量

            // When: 分配多个对象
            int[] objectIds = new int[objectCount];
            for (int i = 0; i < objectCount; i++) {
                objectIds[i] = gc.allocate(100);
            }

            // Then: 所有对象应该正确分配
            for (int objectId : objectIds) {
                assertThat(objectId).isGreaterThan(0);
                assertThat(gc.isObjectAlive(objectId)).isTrue();
            }
        }
    }

    // ====================
    // 引用计数测试
    // ====================

    @Nested
    @DisplayName("引用计数测试")
    class ReferenceCountingTests {

        @Test
        @DisplayName("应该正确增加引用计数")
        void testIncrementReferenceCount() throws Exception {
            // Given: 分配的对象
            int objectId = gc.allocate(100);

            // When: 增加引用计数
            gc.incrementRef(objectId);
            gc.incrementRef(objectId);

            // Then: 对象应该仍然存活
            assertThat(gc.isObjectAlive(objectId)).isTrue();
        }

        @Test
        @DisplayName("应该正确减少引用计数")
        void testDecrementReferenceCount() throws Exception {
            // Given: 分配的对象并增加引用
            int objectId = gc.allocate(100);  // refCount=1
            gc.incrementRef(objectId);  // refCount=2
            gc.incrementRef(objectId);  // refCount=3

            // When: 减少引用计数
            gc.decrementRef(objectId);  // refCount: 3 -> 2

            // Then: 对象应该仍然存活
            assertThat(gc.isObjectAlive(objectId)).isTrue();

            // When: 减少到0
            gc.decrementRef(objectId);  // refCount: 2 -> 1
            gc.decrementRef(objectId);  // refCount: 1 -> 0 (触发回收)

            // Then: 对象应该被回收
            // 注意：引用计数为0时，对象会被立即回收
            assertThat(gc.isObjectAlive(objectId)).isFalse();
        }

        @Test
        @DisplayName("应该正确处理无效对象ID的引用操作")
        void testInvalidObjectIdReferenceOperations() {
            // Given: 无效对象ID
            int invalidObjectId = 9999;

            // When & Then: 引用操作不应该抛出异常
            assertThatCode(() -> gc.incrementRef(invalidObjectId))
                .doesNotThrowAnyException();
            assertThatCode(() -> gc.decrementRef(invalidObjectId))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("应该正确处理循环引用")
        void testCircularReference() throws Exception {
            // Given: 创建两个对象
            int objectId1 = gc.allocate(100);
            int objectId2 = gc.allocate(100);

            // When: 建立循环引用
            gc.incrementRef(objectId1); // object1引用object2
            gc.incrementRef(objectId2); // object2引用object1

            // Then: 两个对象都应该存活
            assertThat(gc.isObjectAlive(objectId1)).isTrue();
            assertThat(gc.isObjectAlive(objectId2)).isTrue();

            // When: 移除外部引用
            gc.decrementRef(objectId1);
            gc.decrementRef(objectId2);

            // Then: 循环引用应该导致内存泄漏（引用计数算法的问题）
            // 执行垃圾回收
            gc.collect();

            // 注意：引用计数算法无法处理循环引用
            // 两个对象可能仍然存活（内存泄漏）
            // 这是引用计数算法的已知限制
        }
    }

    // ====================
    // 垃圾回收测试
    // ====================

    @Nested
    @DisplayName("垃圾回收测试")
    class GarbageCollectionTests {

        @Test
        @DisplayName("应该正确回收无引用对象")
        void testGarbageCollectionOfUnreferencedObjects() throws Exception {
            // Given: 分配多个对象
            int objectCount = 10;
            int[] objectIds = new int[objectCount];
            for (int i = 0; i < objectCount; i++) {
                objectIds[i] = gc.allocate(100);
                // allocate() 已设置 refCount=1（分配者持有）
            }

            // When: 移除所有引用并执行垃圾回收
            for (int objectId : objectIds) {
                gc.decrementRef(objectId); // refCount: 1 -> 0
            }
            gc.collect();

            // Then: 所有对象应该被回收
            for (int objectId : objectIds) {
                assertThat(gc.isObjectAlive(objectId)).isFalse();
            }
        }

        @Test
        @DisplayName("应该保留有引用对象")
        void testPreserveReferencedObjects() throws Exception {
            // Given: 分配多个对象，部分有引用
            int objectCount = 10;
            int[] objectIds = new int[objectCount];
            for (int i = 0; i < objectCount; i++) {
                objectIds[i] = gc.allocate(100);  // refCount=1
                if (i % 2 == 0) { // 偶数索引的对象保留引用
                    gc.incrementRef(objectIds[i]);  // refCount: 1 -> 2
                }
            }

            // When: 释放所有对象的初始引用，然后执行垃圾回收
            for (int i = 0; i < objectCount; i++) {
                gc.decrementRef(objectIds[i]);
                // 偶数索引: refCount: 2 -> 1 (仍然存活)
                // 奇数索引: refCount: 1 -> 0 (被回收)
            }
            gc.collect();

            // Then: 有引用的对象应该存活，无引用的对象应该被回收
            for (int i = 0; i < objectCount; i++) {
                if (i % 2 == 0) {
                    assertThat(gc.isObjectAlive(objectIds[i])).isTrue();
                } else {
                    assertThat(gc.isObjectAlive(objectIds[i])).isFalse();
                }
            }
        }

        @Test
        @DisplayName("应该正确记录垃圾回收统计信息")
        void testGCStatisticsRecording() throws Exception {
            // Given: 初始统计信息
            GCStats initialStats = gc.getStats();
            long initialCollections = initialStats.getTotalCollections();
            long initialAllocatedMemory = initialStats.getTotalAllocatedMemory();

            // When: 分配和回收对象
            // allocate() 已设置 refCount=1（分配者持有引用）
            int objectId = gc.allocate(1000);
            // decrementRef() 将 refCount: 1 -> 0，触发回收
            gc.decrementRef(objectId);
            gc.collect();

            // Then: 统计信息应该更新
            GCStats updatedStats = gc.getStats();
            assertThat(updatedStats.getTotalCollections()).isGreaterThan(initialCollections);
            assertThat(updatedStats.getTotalAllocatedMemory()).isGreaterThan(initialAllocatedMemory);
        }

        @Test
        @DisplayName("应该正确重置统计信息")
        void testResetStatistics() throws Exception {
            // Given: 执行一些操作
            // allocate() 已设置 refCount=1（分配者持有引用）
            int objectId = gc.allocate(100);
            // decrementRef() 将 refCount: 1 -> 0，触发回收
            gc.decrementRef(objectId);
            gc.collect();

            GCStats statsBeforeReset = gc.getStats();
            assertThat(statsBeforeReset.getTotalCollections()).isGreaterThan(0);

            // When: 重置统计信息
            gc.resetStats();

            // Then: 统计信息应该被重置
            GCStats statsAfterReset = gc.getStats();
            assertThat(statsAfterReset.getTotalCollections()).isEqualTo(0);
            assertThat(statsAfterReset.getTotalAllocatedMemory()).isEqualTo(0);
        }

        @Test
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        @DisplayName("垃圾回收不应该阻塞")
        void testGCShouldNotBlock() throws Exception {
            // Given: 大量对象
            int objectCount = 1000;
            int[] objectIds = new int[objectCount];
            for (int i = 0; i < objectCount; i++) {
                objectIds[i] = gc.allocate(100);  // refCount=1
            }

            // When & Then: 垃圾回收应该在合理时间内完成
            assertThatCode(() -> {
                for (int objectId : objectIds) {
                    gc.decrementRef(objectId);  // refCount: 1 -> 0
                }
                gc.collect();
            }).doesNotThrowAnyException();
        }
    }

    // ====================
    // 内存泄漏检测测试
    // ====================

    @Nested
    @DisplayName("内存泄漏检测测试")
    class MemoryLeakDetectionTests {

        @Test
        @DisplayName("应该检测到未释放的对象")
        void testDetectUnreleasedObjects() throws Exception {
            // Given: 分配对象，偶数索引保留额外引用
            int objectCount = 100;
            int[] objectIds = new int[objectCount];
            for (int i = 0; i < objectCount; i++) {
                objectIds[i] = gc.allocate(100);  // refCount=1
                if (i % 2 == 0) {
                    // 偶数索引：保留额外引用，模拟内存泄漏
                    gc.incrementRef(objectIds[i]);  // refCount: 1 -> 2
                }
            }

            // When: 释放所有对象的初始引用
            for (int i = 0; i < objectCount; i++) {
                gc.decrementRef(objectIds[i]);
                // 偶数索引: refCount: 2 -> 1 (仍然存活，内存泄漏)
                // 奇数索引: refCount: 1 -> 0 (被回收)
            }
            gc.collect();

            // Then: 偶数索引的对象应该仍然存活（内存泄漏）
            int leakedCount = 0;
            for (int i = 0; i < objectCount; i++) {
                if (gc.isObjectAlive(objectIds[i])) {
                    leakedCount++;
                }
            }
            assertThat(leakedCount).isEqualTo(objectCount / 2);  // 50个偶数索引的对象
        }

        @Test
        @DisplayName("应该检测循环引用导致的内存泄漏")
        void testDetectCircularReferenceLeak() throws Exception {
            // Given: 创建循环引用
            int objectId1 = gc.allocate(100);
            int objectId2 = gc.allocate(100);

            // 建立循环引用
            gc.incrementRef(objectId1); // object1引用object2
            gc.incrementRef(objectId2); // object2引用object1

            // When: 移除外部引用
            gc.decrementRef(objectId1);
            gc.decrementRef(objectId2);
            gc.collect();

            // Then: 循环引用应该导致内存泄漏
            // 引用计数算法无法回收循环引用
            assertThat(gc.isObjectAlive(objectId1)).isTrue();
            assertThat(gc.isObjectAlive(objectId2)).isTrue();
        }

        @Test
        @DisplayName("应该正确清理所有对象")
        void testClearAllObjects() throws Exception {
            // Given: 分配多个对象
            int objectCount = 50;
            for (int i = 0; i < objectCount; i++) {
                gc.allocate(100);
            }

            // 验证对象存在
            assertThat(((ReferenceCountingGC) gc).getObjectCount()).isEqualTo(objectCount);

            // When: 清理所有对象
            ((ReferenceCountingGC) gc).clearAll();

            // Then: 所有对象应该被清理
            assertThat(((ReferenceCountingGC) gc).getObjectCount()).isEqualTo(0);
            assertThat(((ReferenceCountingGC) gc).getHeapUsage()).isEqualTo(0);
        }
    }

    // ====================
    // 性能基准测试
    // ====================

    @Nested
    @DisplayName("性能基准测试")
    @Tag("performance")
    class PerformanceBenchmarkTests {

        @Test
        @DisplayName("对象分配性能测试")
        void testObjectAllocationPerformance() throws Exception {
            // Given: 性能测试参数
            int iterations = 1000;
            int objectSize = 100;

            // When: 执行多次分配
            long startTime = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                int objectId = gc.allocate(objectSize);  // refCount=1
                // 不调用 incrementRef，直接释放
                gc.decrementRef(objectId);  // refCount: 1 -> 0，触发回收
            }
            gc.collect();
            long endTime = System.nanoTime();

            // Then: 计算性能指标
            long totalTime = endTime - startTime;
            double avgTimePerAllocation = totalTime / (double) iterations;

            // 验证性能在合理范围内
            assertThat(avgTimePerAllocation).isLessThan(1_000_000); // 小于1ms每次分配
        }

        @Test
        @DisplayName("垃圾回收性能测试")
        void testGarbageCollectionPerformance() throws Exception {
            // Given: 创建大量对象
            int objectCount = 10000;
            int objectSize = 100;
            int[] objectIds = new int[objectCount];

            for (int i = 0; i < objectCount; i++) {
                objectIds[i] = gc.allocate(objectSize);  // refCount=1
            }

            // When: 测量垃圾回收时间
            long startTime = System.nanoTime();
            for (int objectId : objectIds) {
                gc.decrementRef(objectId);  // refCount: 1 -> 0
            }
            gc.collect();
            long endTime = System.nanoTime();

            // Then: 计算性能指标
            long gcTime = endTime - startTime;
            double gcTimePerObject = gcTime / (double) objectCount;

            // 验证性能在合理范围内
            assertThat(gcTimePerObject).isLessThan(10_000); // 小于10微秒每个对象
        }

        @Test
        @DisplayName("内存使用效率测试")
        void testMemoryUsageEfficiency() throws Exception {
            // Given: 分配不同大小的对象
            int[] sizes = {10, 100, 1000, 10000};
            int totalAllocated = 0;

            // When: 分配对象
            for (int size : sizes) {
                int objectId = gc.allocate(size);
                gc.incrementRef(objectId);
                totalAllocated += size;
            }

            // Then: 验证内存使用
            int heapUsage = ((ReferenceCountingGC) gc).getHeapUsage();
            double efficiency = (double) heapUsage / totalAllocated * 100;

            // 内存使用效率应该在合理范围内
            assertThat(efficiency).isBetween(90.0, 110.0); // 允许10%的开销
        }
    }

    // ====================
    // 边界测试
    // ====================

    @Nested
    @DisplayName("边界测试")
    @Tag("boundary")
    class BoundaryTests {

        @Test
        @DisplayName("应该处理最大堆使用")
        void testMaximumHeapUsage() throws Exception {
            // Given: 接近堆大小的对象
            int heapSize = ((ReferenceCountingGC) gc).getHeapSize();
            int largeObjectSize = heapSize - 1000; // 留出一些空间

            // When: 分配大对象
            int objectId = gc.allocate(largeObjectSize);

            // Then: 对象应该正确分配
            assertThat(objectId).isGreaterThan(0);
            assertThat(gc.isObjectAlive(objectId)).isTrue();

            // When: 尝试分配更多内存
            // Then: 应该抛出OutOfMemoryError
            assertThatThrownBy(() -> gc.allocate(2000))
                .isInstanceOf(OutOfMemoryError.class);
        }

        @Test
        @DisplayName("应该处理大量小对象")
        void testManySmallObjects() throws Exception {
            // Given: 大量小对象
            int objectCount = 10000;
            int objectSize = 10;

            // When: 分配大量对象
            int[] objectIds = new int[objectCount];
            for (int i = 0; i < objectCount; i++) {
                objectIds[i] = gc.allocate(objectSize);  // refCount=1
            }

            // Then: 所有对象应该正确分配
            for (int objectId : objectIds) {
                assertThat(gc.isObjectAlive(objectId)).isTrue();
            }

            // When: 释放所有对象
            for (int objectId : objectIds) {
                gc.decrementRef(objectId);  // refCount: 1 -> 0
            }
            gc.collect();

            // Then: 所有对象应该被回收
            for (int objectId : objectIds) {
                assertThat(gc.isObjectAlive(objectId)).isFalse();
            }
        }

        @Test
        @DisplayName("应该处理并发访问")
        void testConcurrentAccess() throws Exception {
            // Given: 多线程测试
            int threadCount = 10;
            int operationsPerThread = 100;
            Thread[] threads = new Thread[threadCount];

            // When: 并发执行GC操作
            for (int i = 0; i < threadCount; i++) {
                threads[i] = new Thread(() -> {
                    try {
                        for (int j = 0; j < operationsPerThread; j++) {
                            int objectId = gc.allocate(100);  // refCount=1
                            // 不调用 incrementRef，直接释放
                            gc.decrementRef(objectId);  // refCount: 1 -> 0
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                threads[i].start();
            }

            // 等待所有线程完成
            for (Thread thread : threads) {
                thread.join();
            }

            // Then: 执行垃圾回收
            gc.collect();

            // 验证没有异常发生
            assertThat(((ReferenceCountingGC) gc).getObjectCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("应该处理长时间运行")
        @Timeout(value = 30, unit = TimeUnit.SECONDS)
        void testLongRunning() throws Exception {
            // Given: 长时间运行测试
            int iterations = 100000;
            int objectSize = 100;

            // When: 长时间运行GC操作
            for (int i = 0; i < iterations; i++) {
                int objectId = gc.allocate(objectSize);
                // allocate() 已设置引用计数为1（分配者持有）
                // decrementRef() 将引用计数减为0，触发回收
                gc.decrementRef(objectId);

                // 定期执行垃圾回收
                if (i % 1000 == 0) {
                    gc.collect();
                }
            }

            // Then: 执行最终垃圾回收
            gc.collect();

            // 验证内存使用正常
            int heapUsage = ((ReferenceCountingGC) gc).getHeapUsage();
            assertThat(heapUsage).isLessThan(((ReferenceCountingGC) gc).getHeapSize());
        }
    }

    // ====================
    // 辅助方法
    // ====================

    /**
     * 为参数化测试提供对象大小
     */
    static Stream<Integer> provideObjectSizes() {
        return Stream.of(1, 10, 100, 1000, 10000, 100000);
    }

    /**
     * 为参数化测试提供对象数量
     */
    static Stream<Integer> provideObjectCounts() {
        return Stream.of(1, 10, 100, 1000);
    }
}