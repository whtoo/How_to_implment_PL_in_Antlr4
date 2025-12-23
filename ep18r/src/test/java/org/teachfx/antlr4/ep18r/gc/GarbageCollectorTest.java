package org.teachfx.antlr4.ep18r.gc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * 垃圾回收器单元测试
 * 测试引用计数垃圾回收器的功能
 */
@DisplayName("垃圾回收器测试")
public class GarbageCollectorTest {

    private ReferenceCountingGC gc;

    @BeforeEach
    void setUp() {
        gc = new ReferenceCountingGC(1024 * 1024); // 1MB heap
    }

    @Test
    @DisplayName("应该正确分配对象")
    void testObjectAllocation() throws Exception {
        int objectId = gc.allocate(100);

        assertThat(objectId).isGreaterThan(0);
        assertThat(gc.isObjectAlive(objectId)).isTrue();
        assertThat(gc.getObjectCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("应该正确管理引用计数")
    void testReferenceCounting() throws Exception {
        int objectId = gc.allocate(100);

        // 初始引用计数为1（分配者持有引用）
        assertThat(gc.isObjectAlive(objectId)).isTrue();

        // 增加引用：refCount=1+2=3
        gc.incrementRef(objectId);
        gc.incrementRef(objectId);

        // 减少引用：refCount=3-1=2，仍然存活
        gc.decrementRef(objectId);
        assertThat(gc.isObjectAlive(objectId)).isTrue();

        // 再减少一次：refCount=2-1=1，仍然存活
        gc.decrementRef(objectId);
        assertThat(gc.isObjectAlive(objectId)).isTrue();

        // 再减少一次到0，应该触发回收：refCount=0
        gc.decrementRef(objectId);
        assertThat(gc.isObjectAlive(objectId)).isFalse();
    }

    @Test
    @DisplayName("应该正确执行垃圾回收")
    void testGarbageCollection() throws Exception {
        // 分配多个对象（每个初始refCount=1）
        int[] objectIds = new int[10];
        for (int i = 0; i < 10; i++) {
            objectIds[i] = gc.allocate(100);
            // 增加引用（现在refCount=2）
            gc.incrementRef(objectIds[i]);
        }

        assertThat(gc.getObjectCount()).isEqualTo(10);

        // 减少所有引用：先减少分配时的引用(refCount=1)，再减少测试添加的引用(refCount=0)
        for (int objectId : objectIds) {
            gc.decrementRef(objectId); // refCount: 2 -> 1
        }
        for (int objectId : objectIds) {
            gc.decrementRef(objectId); // refCount: 1 -> 0
        }

        // 手动触发垃圾回收
        gc.collect();

        // 所有对象应该被回收
        assertThat(gc.getObjectCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("应该正确处理内存不足")
    void testOutOfMemory() {
        // 尝试分配超过堆大小的对象
        assertThatThrownBy(() -> gc.allocate(2 * 1024 * 1024))
            .isInstanceOf(OutOfMemoryError.class);
    }

    @Test
    @DisplayName("应该正确记录统计信息")
    void testStatisticsRecording() throws Exception {
        int objectId = gc.allocate(1000);

        // 执行垃圾回收
        gc.collect();

        GCStats stats = gc.getStats();
        assertThat(stats.getTotalAllocatedMemory()).isGreaterThan(0);
        assertThat(stats.getTotalCollections()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("应该正确清理所有对象")
    void testClearAll() throws Exception {
        // 分配多个对象
        for (int i = 0; i < 5; i++) {
            gc.allocate(100);
        }

        assertThat(gc.getObjectCount()).isEqualTo(5);

        // 清理所有对象
        gc.clearAll();

        assertThat(gc.getObjectCount()).isEqualTo(0);
        assertThat(gc.getHeapUsage()).isEqualTo(0);
    }

    @Test
    @DisplayName("应该正确获取堆信息")
    void testHeapInfo() {
        String heapInfo = gc.getHeapInfo();
        assertThat(heapInfo).contains("Heap:");
        assertThat(heapInfo).contains("used");
    }

    @Test
    @DisplayName("应该正确处理负数大小分配")
    void testNegativeSizeAllocation() {
        assertThatThrownBy(() -> gc.allocate(-1))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("应该正确处理零大小分配")
    void testZeroSizeAllocation() {
        assertThatThrownBy(() -> gc.allocate(0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("应该正确处理无效对象ID")
    void testInvalidObjectId() {
        // 测试不存在的对象ID
        assertThat(gc.isObjectAlive(9999)).isFalse();
        assertThatCode(() -> gc.incrementRef(9999)).doesNotThrowAnyException();
        assertThatCode(() -> gc.decrementRef(9999)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("应该正确执行强制垃圾回收")
    void testForceGC() throws Exception {
        int objectId = gc.allocate(100);  // refCount=1
        gc.incrementRef(objectId);         // refCount=2
        gc.decrementRef(objectId);         // refCount=1
        gc.decrementRef(objectId);         // refCount=0，应该被回收

        // 强制垃圾回收
        gc.forceGC();

        assertThat(gc.isObjectAlive(objectId)).isFalse();
    }

    @Test
    @DisplayName("应该正确重置统计信息")
    void testResetStats() throws Exception {
        int objectId = gc.allocate(100);  // refCount=1
        gc.decrementRef(objectId);         // refCount=0
        gc.collect();

        GCStats stats = gc.getStats();
        assertThat(stats.getTotalCollections()).isGreaterThan(0);

        // 重置统计信息
        gc.resetStats();

        stats = gc.getStats();
        assertThat(stats.getTotalCollections()).isEqualTo(0);
        assertThat(stats.getTotalAllocatedMemory()).isEqualTo(0);
    }

    @Test
    @DisplayName("应该正确显示对象数量")
    void testObjectCount() throws Exception {
        assertThat(gc.getObjectCount()).isEqualTo(0);

        int objectId = gc.allocate(100);
        assertThat(gc.getObjectCount()).isEqualTo(1);

        gc.clearAll();
        assertThat(gc.getObjectCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("应该正确获取堆使用情况")
    void testHeapUsage() throws Exception {
        int initialUsage = gc.getHeapUsage();
        assertThat(initialUsage).isEqualTo(0);

        gc.allocate(1000);
        int afterAllocation = gc.getHeapUsage();
        assertThat(afterAllocation).isGreaterThan(initialUsage);
        assertThat(afterAllocation).isEqualTo(1000);
    }

    @Test
    @DisplayName("应该正确获取堆大小")
    void testHeapSize() {
        assertThat(gc.getHeapSize()).isEqualTo(1024 * 1024);
    }

    @Test
    @DisplayName("应该正确显示垃圾回收器信息")
    void testToString() {
        String info = gc.toString();
        assertThat(info).contains("ReferenceCountingGC");
        assertThat(info).contains("heapSize");
        assertThat(info).contains("used");
    }

    @Test
    @DisplayName("应该正确执行多个垃圾回收周期")
    void testMultipleGCCycles() throws Exception {
        // 分配和回收多次
        for (int cycle = 0; cycle < 3; cycle++) {
            int objectId = gc.allocate(100);  // refCount=1
            gc.incrementRef(objectId);         // refCount=2
            gc.decrementRef(objectId);         // refCount=1
            gc.decrementRef(objectId);         // refCount=0
            gc.collect();

            assertThat(gc.getObjectCount()).isEqualTo(0);
        }

        GCStats stats = gc.getStats();
        assertThat(stats.getTotalCollections()).isGreaterThanOrEqualTo(3);
    }
}