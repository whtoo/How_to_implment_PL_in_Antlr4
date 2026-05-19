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
}
