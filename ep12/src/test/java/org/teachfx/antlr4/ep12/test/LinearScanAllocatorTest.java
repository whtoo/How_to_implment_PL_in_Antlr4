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
}
