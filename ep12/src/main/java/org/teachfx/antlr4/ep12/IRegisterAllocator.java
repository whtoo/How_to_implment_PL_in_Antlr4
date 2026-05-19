package org.teachfx.antlr4.ep12;

import java.util.List;
import java.util.Map;

/**
 * Simplified register allocator interface for teaching.
 *
 * <p>String-based: virtual registers are named strings (e.g. "t0", "x"),
 * physical registers are numbered 0..K-1 (displayed as R1..RK).</p>
 *
 * <p>This interface captures the two core questions a register allocator answers:</p>
 * <ol>
 *   <li>Which physical register (if any) holds each virtual register?</li>
 *   <li>Which virtual registers must be spilled to the stack?</li>
 * </ol>
 */
public interface IRegisterAllocator {

    /**
     * Run allocation on the given live intervals.
     *
     * @param intervals sorted or unsorted list of live intervals
     * @return ordered trace of allocation decisions
     */
    List<LinearScanAllocator.TraceEntry> allocate(List<LinearScanAllocator.LiveInterval> intervals);

    /**
     * Get the physical register assigned to a virtual register (0-based), or -1 if spilled.
     */
    int getRegister(String vreg);

    /**
     * Check whether a virtual register was spilled to the stack.
     */
    boolean isSpilled(String vreg);

    /**
     * Get the stack slot offset for a spilled vreg, or 0 if not spilled.
     */
    int getSpillSlot(String vreg);

    /**
     * Get the complete allocation map (vreg name -> physical register number).
     */
    Map<String, Integer> getAllocation();

    /**
     * Generate a human-readable report of the allocation result.
     */
    String generateReport();
}
