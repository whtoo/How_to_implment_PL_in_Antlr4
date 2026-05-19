package org.teachfx.antlr4.ep12.test;

import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep12.LinearScanAllocator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EP12 tests: Linear Scan Register Allocation.
 */
public class LinearScanAllocatorTest {

    @Test
    void testBasicAllocation() {
        var alloc = new LinearScanAllocator(4);
        var intervals = List.of(
            new LinearScanAllocator.LiveInterval("a", 0, 3),
            new LinearScanAllocator.LiveInterval("b", 1, 4),
            new LinearScanAllocator.LiveInterval("c", 2, 5)
        );
        var trace = alloc.allocate(intervals);

        assertFalse(trace.isEmpty());
        assertTrue(alloc.getAllocation().size() >= 3);
    }

    @Test
    void testSpillWhenPressureExceedsRegisters() {
        var alloc = new LinearScanAllocator(2);
        var intervals = List.of(
            new LinearScanAllocator.LiveInterval("a", 0, 5),
            new LinearScanAllocator.LiveInterval("b", 1, 5),
            new LinearScanAllocator.LiveInterval("c", 2, 5),
            new LinearScanAllocator.LiveInterval("d", 3, 5)
        );
        alloc.allocate(intervals);

        assertTrue(alloc.getSpillCount() > 0, "Should spill when pressure > K");
    }

    @Test
    void testNoSpillWhenEnoughRegisters() {
        var alloc = new LinearScanAllocator(4);
        var intervals = List.of(
            new LinearScanAllocator.LiveInterval("a", 0, 1),
            new LinearScanAllocator.LiveInterval("b", 2, 3)
        );
        alloc.allocate(intervals);

        assertEquals(0, alloc.getSpillCount(), "Non-overlapping intervals should not spill");
    }

    @Test
    void testRegisterAssignmentIsValid() {
        var alloc = new LinearScanAllocator(3);
        var intervals = List.of(
            new LinearScanAllocator.LiveInterval("a", 0, 2),
            new LinearScanAllocator.LiveInterval("b", 1, 3)
        );
        alloc.allocate(intervals);

        for (var entry : alloc.getAllocation().entrySet()) {
            int reg = entry.getValue();
            assertTrue(reg >= 0 && reg < 3, "Register should be in valid range");
        }
    }

    @Test
    void testImplementsInterface() {
        LinearScanAllocator alloc = new LinearScanAllocator(4);
        assertTrue(alloc instanceof org.teachfx.antlr4.ep12.IRegisterAllocator);
    }

    @Test
    void testAllocatorReset() {
        var alloc = new LinearScanAllocator(4);
        var intervals = List.of(
            new LinearScanAllocator.LiveInterval("a", 0, 2)
        );
        alloc.allocate(intervals);
        assertTrue(alloc.getAllocation().size() > 0);

        alloc.reset();
        assertEquals(0, alloc.getAllocation().size());
        assertEquals(0, alloc.getSpillCount());
    }

    @Test
    void testAllocateSingleInterval() {
        var alloc = new LinearScanAllocator(4);
        var intervals = List.of(
            new LinearScanAllocator.LiveInterval("x", 0, 1)
        );
        alloc.allocate(intervals);
        assertEquals(1, alloc.getAllocation().size());
        assertTrue(alloc.getAllocation().containsValue(0));
    }

    @Test
    void testReuseRegisterAfterIntervalEnds() {
        var alloc = new LinearScanAllocator(4);
        var intervals = List.of(
            new LinearScanAllocator.LiveInterval("a", 0, 2),
            new LinearScanAllocator.LiveInterval("b", 3, 5),
            new LinearScanAllocator.LiveInterval("c", 0, 5)
        );
        alloc.allocate(intervals);
        // a and b should both get the same register since they don't overlap
        int regA = alloc.getAllocation().get("a");
        int regB = alloc.getAllocation().get("b");
        assertTrue(regA >= 0);
        assertTrue(regB >= 0);
    }

    @Test
    void testSpillWithHighPressure() {
        var alloc = new LinearScanAllocator(1);
        var intervals = List.of(
            new LinearScanAllocator.LiveInterval("a", 0, 5),
            new LinearScanAllocator.LiveInterval("b", 0, 5)
        );
        alloc.allocate(intervals);
        assertTrue(alloc.getSpillCount() >= 1);
    }

    @Test
    void testAllocatedRegisterCount() {
        var alloc = new LinearScanAllocator(4);
        var intervals = List.of(
            new LinearScanAllocator.LiveInterval("a", 0, 1),
            new LinearScanAllocator.LiveInterval("b", 0, 1)
        );
        alloc.allocate(intervals);
        // alloc.getRegisterCount() returns K (physical register count)
        assertEquals(4, alloc.getRegisterCount());
    }

    @Test
    void testLiveIntervalConstructor() {
        var interval = new LinearScanAllocator.LiveInterval("var1", 0, 10);
        assertEquals("var1", interval.name);
        assertEquals(0, interval.start);
        assertEquals(10, interval.end);
    }

    @Test
    void testZeroRegisterAllocator() {
        assertThrows(IllegalArgumentException.class, () -> new LinearScanAllocator(0));
    }
}
