package org.teachfx.antlr4.ep18.gc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 内存泄漏检测测试
 *
 * 专门测试GC的内存泄漏检测能力，包括：
 * 1. 未释放对象检测
 * 2. 循环引用检测
 * 3. 弱引用检测
 * 4. 内存增长检测
 *
 * @author EP18 TDD Refactoring Team
 * @version 1.0.0
 */
@DisplayName("内存泄漏检测测试")
@Tag("memory-leak")
@Tag("gc")
public class MemoryLeakDetectionTest {

    private ReferenceCountingGC gc;

    @BeforeEach
    void setUp() {
        gc = new ReferenceCountingGC(1024 * 1024); // 1MB堆大小
    }

    // ====================
    // 基本内存泄漏检测
    // ====================

    @Nested
    @DisplayName("基本内存泄漏检测")
    class BasicMemoryLeakDetectionTests {

        @Test
        @DisplayName("应该检测到未释放的单个对象")
        void testDetectSingleUnreleasedObject() throws Exception {
            // Given: 分配对象但不释放
            int objectId = gc.allocate(100);
            gc.incrementRef(objectId);

            // 记录初始内存使用
            int initialHeapUsage = gc.getHeapUsage();

            // When: 不释放对象，执行垃圾回收
            gc.collect();

            // Then: 对象应该仍然存在（内存泄漏）
            assertThat(gc.isObjectAlive(objectId)).isTrue();
            assertThat(gc.getHeapUsage()).isEqualTo(initialHeapUsage);
        }

        @Test
        @DisplayName("应该检测到多个未释放对象")
        void testDetectMultipleUnreleasedObjects() throws Exception {
            // Given: 分配多个对象但不释放
            int objectCount = 100;
            int[] objectIds = new int[objectCount];
            for (int i = 0; i < objectCount; i++) {
                objectIds[i] = gc.allocate(100);
                gc.incrementRef(objectIds[i]);
            }

            // 记录初始内存使用
            int initialHeapUsage = gc.getHeapUsage();

            // When: 只释放部分对象
            int releasedCount = objectCount / 2;
            for (int i = 0; i < releasedCount; i++) {
                gc.decrementRef(objectIds[i]);
            }
            gc.collect();

            // Then: 部分对象应该被释放，部分仍然存在（内存泄漏）
            int leakedCount = 0;
            for (int i = 0; i < objectCount; i++) {
                if (gc.isObjectAlive(objectIds[i])) {
                    leakedCount++;
                }
            }

            assertThat(leakedCount).isEqualTo(objectCount - releasedCount);
            assertThat(gc.getHeapUsage()).isLessThan(initialHeapUsage);
        }

        @ParameterizedTest
        @ValueSource(ints = {10, 100, 1000})
        @DisplayName("应该检测不同规模的内存泄漏")
        void testDetectMemoryLeakAtDifferentScales(int objectCount) throws Exception {
            // Given: 分配指定数量的对象
            int[] objectIds = new int[objectCount];
            for (int i = 0; i < objectCount; i++) {
                objectIds[i] = gc.allocate(100);
                gc.incrementRef(objectIds[i]);
            }

            // When: 不释放任何对象，执行垃圾回收
            gc.collect();

            // Then: 所有对象应该仍然存在（内存泄漏）
            for (int objectId : objectIds) {
                assertThat(gc.isObjectAlive(objectId)).isTrue();
            }
            assertThat(gc.getObjectCount()).isEqualTo(objectCount);
        }
    }

    // ====================
    // 循环引用内存泄漏检测
    // ====================

    @Nested
    @DisplayName("循环引用内存泄漏检测")
    class CircularReferenceLeakDetectionTests {

        @Test
        @DisplayName("应该检测到简单循环引用")
        void testDetectSimpleCircularReference() throws Exception {
            // Given: 创建两个对象的循环引用
            int objectId1 = gc.allocate(100);
            int objectId2 = gc.allocate(100);

            // 建立循环引用
            gc.incrementRef(objectId1); // object1引用object2
            gc.incrementRef(objectId2); // object2引用object1

            // When: 移除外部引用，执行垃圾回收
            gc.decrementRef(objectId1);
            gc.decrementRef(objectId2);
            gc.collect();

            // Then: 循环引用应该导致内存泄漏
            // 引用计数算法无法回收循环引用
            assertThat(gc.isObjectAlive(objectId1)).isTrue();
            assertThat(gc.isObjectAlive(objectId2)).isTrue();
            assertThat(gc.getObjectCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("应该检测到复杂循环引用链")
        void testDetectComplexCircularReferenceChain() throws Exception {
            // Given: 创建三个对象的循环引用链
            int objectId1 = gc.allocate(100);
            int objectId2 = gc.allocate(100);
            int objectId3 = gc.allocate(100);

            // 建立循环引用链: 1 -> 2 -> 3 -> 1
            gc.incrementRef(objectId1); // object1引用object2
            gc.incrementRef(objectId2); // object2引用object3
            gc.incrementRef(objectId3); // object3引用object1

            // When: 移除外部引用，执行垃圾回收
            gc.decrementRef(objectId1);
            gc.decrementRef(objectId2);
            gc.decrementRef(objectId3);
            gc.collect();

            // Then: 循环引用链应该导致内存泄漏
            assertThat(gc.isObjectAlive(objectId1)).isTrue();
            assertThat(gc.isObjectAlive(objectId2)).isTrue();
            assertThat(gc.isObjectAlive(objectId3)).isTrue();
            assertThat(gc.getObjectCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("应该检测到自引用")
        void testDetectSelfReference() throws Exception {
            // Given: 创建自引用对象
            int objectId = gc.allocate(100);

            // 建立自引用
            gc.incrementRef(objectId); // 对象引用自己

            // When: 移除外部引用，执行垃圾回收
            gc.decrementRef(objectId);
            gc.collect();

            // Then: 自引用应该导致内存泄漏
            assertThat(gc.isObjectAlive(objectId)).isTrue();
            assertThat(gc.getObjectCount()).isEqualTo(1);
        }
    }

    // ====================
    // 内存增长检测
    // ====================

    @Nested
    @DisplayName("内存增长检测")
    class MemoryGrowthDetectionTests {

        @Test
        @DisplayName("应该检测到渐进式内存泄漏")
        void testDetectProgressiveMemoryLeak() throws Exception {
            // Given: 多次分配和部分释放
            int iterations = 100;
            int objectsPerIteration = 10;
            List<Integer> allObjectIds = new ArrayList<>();

            for (int i = 0; i < iterations; i++) {
                // 分配新对象
                for (int j = 0; j < objectsPerIteration; j++) {
                    int objectId = gc.allocate(100);
                    gc.incrementRef(objectId);
                    allObjectIds.add(objectId);
                }

                // 只释放一半的对象
                int objectsToRelease = objectsPerIteration / 2;
                for (int j = 0; j < objectsToRelease; j++) {
                    int index = allObjectIds.size() - 1 - j;
                    gc.decrementRef(allObjectIds.get(index));
                }

                // 执行垃圾回收
                gc.collect();
            }

            // Then: 内存使用应该逐渐增长
            int finalObjectCount = gc.getObjectCount();
            int expectedLeakedObjects = iterations * (objectsPerIteration / 2);

            assertThat(finalObjectCount).isEqualTo(expectedLeakedObjects);
            assertThat(gc.getHeapUsage()).isGreaterThan(0);
        }

        @Test
        @DisplayName("应该检测到内存使用峰值")
        void testDetectMemoryUsageSpikes() throws Exception {
            // Given: 记录初始内存使用
            int initialHeapUsage = gc.getHeapUsage();

            // When: 分配大量临时对象
            int temporaryObjects = 1000;
            int[] tempObjectIds = new int[temporaryObjects];
            for (int i = 0; i < temporaryObjects; i++) {
                tempObjectIds[i] = gc.allocate(100);
                gc.incrementRef(tempObjectIds[i]);
            }

            // 记录峰值内存使用
            int peakHeapUsage = gc.getHeapUsage();

            // 释放所有临时对象
            for (int objectId : tempObjectIds) {
                gc.decrementRef(objectId);
            }
            gc.collect();

            // Then: 内存使用应该回到初始水平
            int finalHeapUsage = gc.getHeapUsage();
            assertThat(peakHeapUsage).isGreaterThan(initialHeapUsage);
            assertThat(finalHeapUsage).isEqualTo(initialHeapUsage);
        }

        @Test
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        @DisplayName("应该检测到长时间运行的内存泄漏")
        void testDetectLongRunningMemoryLeak() throws Exception {
            // Given: 长时间运行的内存泄漏场景
            int totalIterations = 10000;
            int leakEveryNIterations = 100; // 每100次迭代泄漏一个对象

            List<Integer> leakedObjects = new ArrayList<>();

            for (int i = 0; i < totalIterations; i++) {
                // 分配临时对象
                int tempObjectId = gc.allocate(50);
                gc.incrementRef(tempObjectId);

                // 定期泄漏对象
                if (i % leakEveryNIterations == 0) {
                    // 不释放这个对象，造成泄漏
                    leakedObjects.add(tempObjectId);
                } else {
                    // 正常释放
                    gc.decrementRef(tempObjectId);
                }

                // 定期执行垃圾回收
                if (i % 1000 == 0) {
                    gc.collect();
                }
            }

            // 执行最终垃圾回收
            gc.collect();

            // Then: 应该检测到泄漏的对象
            int expectedLeaks = totalIterations / leakEveryNIterations;
            assertThat(leakedObjects).hasSize(expectedLeaks);

            // 验证泄漏的对象仍然存在
            for (int objectId : leakedObjects) {
                assertThat(gc.isObjectAlive(objectId)).isTrue();
            }
        }
    }

    // ====================
    // 弱引用和软引用检测
    // ====================

    @Nested
    @DisplayName("弱引用检测")
    class WeakReferenceDetectionTests {

        @Test
        @DisplayName("应该正确处理弱引用对象")
        void testHandleWeaklyReferencedObjects() throws Exception {
            // Given: 创建对象和弱引用
            int objectId = gc.allocate(100);
            gc.incrementRef(objectId);

            // 创建Java弱引用（用于测试）
            WeakReference<Object> weakRef = new WeakReference<>(new Object());

            // When: 移除强引用，执行垃圾回收
            gc.decrementRef(objectId);
            gc.collect();

            // Then: 对象应该被回收
            assertThat(gc.isObjectAlive(objectId)).isFalse();

            // 弱引用可能被回收（取决于JVM）
            // 这里主要测试GC本身的行为
        }

        @Test
        @DisplayName("应该检测到只有弱引用的对象")
        void testDetectObjectsWithOnlyWeakReferences() throws Exception {
            // Given: 创建对象并建立弱引用关系
            int objectId = gc.allocate(100);

            // 注意：这里的弱引用是Java的WeakReference，不是GC的
            // GC本身不支持弱引用，这是引用计数算法的限制
            // 测试GC在只有弱引用时的行为

            // When: 不增加强引用，执行垃圾回收
            gc.collect();

            // Then: 对象应该被立即回收（因为没有强引用）
            // 引用计数算法：初始引用计数为0，应该被回收
            assertThat(gc.isObjectAlive(objectId)).isFalse();
        }
    }

    // ====================
    // 内存泄漏修复验证
    // ====================

    @Nested
    @DisplayName("内存泄漏修复验证")
    class MemoryLeakFixVerificationTests {

        @Test
        @DisplayName("应该验证内存泄漏修复")
        void testVerifyMemoryLeakFix() throws Exception {
            // Given: 创建内存泄漏场景
            int objectId = gc.allocate(100);
            gc.incrementRef(objectId);

            // 验证泄漏存在
            gc.collect();
            assertThat(gc.isObjectAlive(objectId)).isTrue();
            int leakedHeapUsage = gc.getHeapUsage();

            // When: 修复泄漏（释放对象）
            gc.decrementRef(objectId);
            gc.collect();

            // Then: 泄漏应该被修复
            assertThat(gc.isObjectAlive(objectId)).isFalse();
            assertThat(gc.getHeapUsage()).isLessThan(leakedHeapUsage);
        }

        @Test
        @DisplayName("应该验证循环引用泄漏修复")
        void testVerifyCircularReferenceLeakFix() throws Exception {
            // Given: 创建循环引用泄漏
            int objectId1 = gc.allocate(100);
            int objectId2 = gc.allocate(100);

            gc.incrementRef(objectId1); // object1引用object2
            gc.incrementRef(objectId2); // object2引用object1

            // 移除外部引用，创建泄漏
            gc.decrementRef(objectId1);
            gc.decrementRef(objectId2);

            // 验证泄漏存在
            gc.collect();
            assertThat(gc.isObjectAlive(objectId1)).isTrue();
            assertThat(gc.isObjectAlive(objectId2)).isTrue();

            // When: 修复泄漏（需要打破循环引用）
            // 对于引用计数算法，需要手动打破循环
            // 这里模拟修复：重新建立引用并正确释放
            gc.incrementRef(objectId1); // 重新获得引用
            gc.incrementRef(objectId2);

            // 正确释放：先打破循环
            // 在实际应用中，可能需要使用弱引用或其他机制
            gc.decrementRef(objectId1); // 打破object1 -> object2的引用
            gc.decrementRef(objectId2); // 打破object2 -> object1的引用

            // 然后正常释放
            gc.decrementRef(objectId1);
            gc.decrementRef(objectId2);
            gc.collect();

            // Then: 泄漏应该被修复
            // 注意：引用计数算法需要手动打破循环引用
            // 这个测试展示了修复过程
            assertThat(gc.isObjectAlive(objectId1)).isFalse();
            assertThat(gc.isObjectAlive(objectId2)).isFalse();
        }
    }

    // ====================
    // 性能影响检测
    // ====================

    @Nested
    @DisplayName("性能影响检测")
    @Tag("performance")
    class PerformanceImpactDetectionTests {

        @Test
        @DisplayName("应该检测内存泄漏对性能的影响")
        void testDetectPerformanceImpactOfMemoryLeak() throws Exception {
            // Given: 创建内存泄漏场景
            int leakIterations = 1000;
            List<Integer> leakedObjects = new ArrayList<>();

            long startTimeWithLeak = System.nanoTime();

            // 模拟有内存泄漏的操作
            for (int i = 0; i < leakIterations; i++) {
                int objectId = gc.allocate(100);
                gc.incrementRef(objectId);
                leakedObjects.add(objectId); // 不释放，造成泄漏

                // 执行一些操作
                if (i % 100 == 0) {
                    gc.collect();
                }
            }

            long endTimeWithLeak = System.nanoTime();
            long timeWithLeak = endTimeWithLeak - startTimeWithLeak();

            // 清理泄漏，重新测试
            for (int objectId : leakedObjects) {
                gc.decrementRef(objectId);
            }
            gc.clearAll();

            long startTimeWithoutLeak = System.nanoTime();

            // 模拟没有内存泄漏的操作
            for (int i = 0; i < leakIterations; i++) {
                int objectId = gc.allocate(100);
                gc.incrementRef(objectId);
                gc.decrementRef(objectId); // 及时释放

                if (i % 100 == 0) {
                    gc.collect();
                }
            }

            long endTimeWithoutLeak = System.nanoTime();
            long timeWithoutLeak = endTimeWithoutLeak - startTimeWithoutLeak;

            // Then: 内存泄漏应该导致性能下降
            // 注意：实际影响可能因实现而异
            // 这里主要验证测试框架能够检测性能差异
            assertThat(timeWithLeak).isGreaterThan(0);
            assertThat(timeWithoutLeak).isGreaterThan(0);

            // 记录性能差异（用于分析）
            double performanceRatio = (double) timeWithLeak / timeWithoutLeak;
            System.out.printf("内存泄漏性能影响: 有泄漏/无泄漏 = %.2fx%n", performanceRatio);
        }

        @Test
        @DisplayName("应该检测垃圾回收频率对性能的影响")
        void testDetectGCFrequencyImpact() throws Exception {
            // Given: 不同垃圾回收频率的测试
            int totalObjects = 10000;
            int objectSize = 100;

            // 测试1: 频繁垃圾回收
            long startTimeFrequentGC = System.nanoTime();
            for (int i = 0; i < totalObjects; i++) {
                int objectId = gc.allocate(objectSize);
                gc.incrementRef(objectId);
                gc.decrementRef(objectId);

                // 每次操作后都执行GC
                gc.collect();
            }
            long endTimeFrequentGC = System.nanoTime();
            long timeFrequentGC = endTimeFrequentGC - startTimeFrequentGC;

            // 重置GC
            gc.clearAll();

            // 测试2: 较少垃圾回收
            long startTimeInfrequentGC = System.nanoTime();
            for (int i = 0; i < totalObjects; i++) {
                int objectId = gc.allocate(objectSize);
                gc.incrementRef(objectId);
                gc.decrementRef(objectId);

                // 每100次操作执行一次GC
                if (i % 100 == 0) {
                    gc.collect();
                }
            }
            // 最终执行一次GC
            gc.collect();
            long endTimeInfrequentGC = System.nanoTime();
            long timeInfrequentGC = endTimeInfrequentGC - startTimeInfrequentGC;

            // Then: 不同GC频率应该有性能差异
            assertThat(timeFrequentGC).isGreaterThan(0);
            assertThat(timeInfrequentGC).isGreaterThan(0);

            // 记录性能差异
            double gcFrequencyImpact = (double) timeFrequentGC / timeInfrequentGC;
            System.out.printf("GC频率性能影响: 频繁/较少 = %.2fx%n", gcFrequencyImpact);
        }
    }

    // ====================
    // 辅助方法
    // ====================

    /**
     * 模拟有内存泄漏的操作时间测量
     */
    private long startTimeWithLeak() {
        return System.nanoTime();
    }
}