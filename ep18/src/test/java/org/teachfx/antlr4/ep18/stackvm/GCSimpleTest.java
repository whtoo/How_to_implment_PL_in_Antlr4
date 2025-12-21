package org.teachfx.antlr4.ep18.stackvm;

import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep18.gc.ReferenceCountingGC;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 简单的GC测试，验证GC基本功能
 */
public class GCSimpleTest {

    @Test
    void testGCAllocation() {
        ReferenceCountingGC gc = new ReferenceCountingGC(1024);

        // 分配内存
        int objectId = gc.allocate(100);
        System.out.println("Allocated object ID: " + objectId);
        System.out.println("Object count: " + gc.getObjectCount());
        System.out.println("Heap usage: " + gc.getHeapUsage());
        System.out.println("Is object alive? " + gc.isObjectAlive(objectId));

        assertThat(objectId).isGreaterThan(0);
        assertThat(gc.getObjectCount()).isEqualTo(1);
        assertThat(gc.getHeapUsage()).isEqualTo(100);
        assertThat(gc.isObjectAlive(objectId)).isTrue();

        // 增加引用计数
        gc.incrementRef(objectId);

        // 减少引用计数（应该还存活）
        gc.decrementRef(objectId);
        assertThat(gc.isObjectAlive(objectId)).isTrue();

        // 再次减少引用计数（应该被回收）
        gc.decrementRef(objectId);
        assertThat(gc.isObjectAlive(objectId)).isFalse();
        assertThat(gc.getObjectCount()).isEqualTo(0);
        assertThat(gc.getHeapUsage()).isEqualTo(0);
    }

    @Test
    void testMultipleAllocations() {
        ReferenceCountingGC gc = new ReferenceCountingGC(1024);

        int obj1 = gc.allocate(100);
        int obj2 = gc.allocate(200);
        int obj3 = gc.allocate(300);

        System.out.println("Object IDs: " + obj1 + ", " + obj2 + ", " + obj3);
        System.out.println("Object count: " + gc.getObjectCount());
        System.out.println("Heap usage: " + gc.getHeapUsage());

        assertThat(gc.getObjectCount()).isEqualTo(3);
        assertThat(gc.getHeapUsage()).isEqualTo(600);

        // 检查所有对象都存活
        assertThat(gc.isObjectAlive(obj1)).isTrue();
        assertThat(gc.isObjectAlive(obj2)).isTrue();
        assertThat(gc.isObjectAlive(obj3)).isTrue();

        // 释放所有对象
        gc.decrementRef(obj1);
        gc.decrementRef(obj2);
        gc.decrementRef(obj3);

        // 手动触发垃圾回收
        gc.forceGC();

        assertThat(gc.getObjectCount()).isEqualTo(0);
        assertThat(gc.getHeapUsage()).isEqualTo(0);
    }
}