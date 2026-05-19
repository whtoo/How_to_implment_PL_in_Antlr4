package org.teachfx.antlr4.ep15.test;

import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep15.gc.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EP15 tests: Reference Counting Garbage Collector.
 */
public class GCTest {

    @Test
    void testAllocateCreatesObject() {
        ReferenceCountingGC gc = new ReferenceCountingGC(1024);
        int addr = gc.allocate(100);
        assertTrue(addr >= 0, "Allocation should succeed");
    }

    @Test
    void testRefCountIncrementAndDecrement() {
        ReferenceCountingGC gc = new ReferenceCountingGC(1024);
        int addr = gc.allocate(50);
        gc.incrementRef(addr);
        gc.incrementRef(addr);
        gc.decrementRef(addr);
        gc.decrementRef(addr);
        gc.decrementRef(addr); // should free

        assertFalse(gc.isObjectAlive(addr), "Object should be freed after refcount reaches 0");
    }

    @Test
    void testCollectFreesUnreachableObjects() {
        ReferenceCountingGC gc = new ReferenceCountingGC(1024);
        int addr = gc.allocate(100);
        assertTrue(gc.isObjectAlive(addr));
        gc.decrementRef(addr); // make refcount 0
        gc.collect(); // force collection
        assertFalse(gc.isObjectAlive(addr), "Unreachable object should be collected");
    }

    @Test
    void testStatsTrackAllocations() {
        ReferenceCountingGC gc = new ReferenceCountingGC(1024);
        gc.allocate(10);
        gc.allocate(20);
        GCStats stats = gc.getStats();
        assertTrue(stats.getTotalAllocatedMemory() >= 30, "Should track allocations");
    }

    @Test
    void testAllocateFailsWhenOutOfMemory() {
        ReferenceCountingGC gc = new ReferenceCountingGC(64);
        assertThrows(OutOfMemoryError.class, () -> gc.allocate(128), "Should throw when requesting more than available");
    }

    @Test
    void testCollectAfterMultipleAllocations() {
        ReferenceCountingGC gc = new ReferenceCountingGC(1024);
        int a = gc.allocate(100);
        int b = gc.allocate(200);
        int c = gc.allocate(300);

        gc.decrementRef(a);
        gc.decrementRef(b);
        gc.collect();

        assertFalse(gc.isObjectAlive(a));
        assertFalse(gc.isObjectAlive(b));
        assertTrue(gc.isObjectAlive(c));
    }

    @Test
    void testIncrementRefOnMultipleObjects() {
        ReferenceCountingGC gc = new ReferenceCountingGC(1024);
        int a = gc.allocate(100);
        int b = gc.allocate(100);

        gc.incrementRef(a);
        gc.incrementRef(a);
        gc.incrementRef(b);

        gc.decrementRef(a);
        assertTrue(gc.isObjectAlive(a)); // refcount still > 0

        gc.decrementRef(a);
        gc.decrementRef(a); // should free a

        assertFalse(gc.isObjectAlive(a));
        assertTrue(gc.isObjectAlive(b)); // still has refcount = 2
    }

    @Test
    void testAllocateNegativeSizeThrows() {
        ReferenceCountingGC gc = new ReferenceCountingGC(1024);
        assertThrows(IllegalArgumentException.class, () -> gc.allocate(-1));
        assertThrows(IllegalArgumentException.class, () -> gc.allocate(0));
    }

    @Test
    void testNegativeHeapSizeThrows() {
        assertThrows(IllegalArgumentException.class, () -> new ReferenceCountingGC(-1));
        assertThrows(IllegalArgumentException.class, () -> new ReferenceCountingGC(0));
    }

    @Test
    void testStatsTracksCollection() {
        ReferenceCountingGC gc = new ReferenceCountingGC(1024);
        int addr = gc.allocate(100);
        gc.decrementRef(addr);
        gc.collect();

        GCStats stats = gc.getStats();
        assertNotNull(stats);
    }

    @Test
    void testManySmallAllocations() {
        ReferenceCountingGC gc = new ReferenceCountingGC(1024);
        for (int i = 0; i < 10; i++) {
            int addr = gc.allocate(50);
            assertTrue(addr >= 0);
        }
    }

    @Test
    void testFreeMemoryReuse() {
        ReferenceCountingGC gc = new ReferenceCountingGC(512);
        int a = gc.allocate(200);
        gc.decrementRef(a);
        gc.collect();

        // A new allocation should be able to reuse freed space
        int b = gc.allocate(200);
        assertTrue(b >= 0);
        assertTrue(gc.isObjectAlive(b));
    }
}
