package org.teachfx.antlr4.ep12;

import java.util.*;

/**
 * EP12 — Register Allocation: Linear Scan Algorithm Demo
 *
 * <p>This is a self-contained teaching demo that walks through the classic
 * Poletto &amp; Sarkar linear scan register allocator step by step.</p>
 *
 * <h3>What you will see</h3>
 * <ol>
 *   <li>Part 1: A simple program with 6 variables and 4 registers (no spills)</li>
 *   <li>Part 2: The same program with only 2 registers (forced spills)</li>
 *   <li>Part 3: A complex program with overlapping live ranges (spills + eviction)</li>
 * </ol>
 */
public class Compiler {

    // ==================== helpers ====================

    /** Build a LiveInterval list from a textual program description. */
    private static List<LinearScanAllocator.LiveInterval> buildIntervals(String[][] program) {
        // program[i] = { def, use1, use2, ... } or { null, use1 } for a use-only line
        Map<String, Integer> start = new LinkedHashMap<>();
        Map<String, Integer> end = new LinkedHashMap<>();

        for (int i = 0; i < program.length; i++) {
            String def = program[i][0];
            if (def != null) {
                start.putIfAbsent(def, i);
                end.put(def, i);
            }
            for (int j = 1; j < program[i].length; j++) {
                String use = program[i][j];
                if (use.startsWith("t") || use.equals("x") || use.equals("y") || use.equals("z") || use.equals("w")) {
                    start.putIfAbsent(use, i);
                    end.put(use, i);
                }
            }
        }

        List<LinearScanAllocator.LiveInterval> intervals = new ArrayList<>();
        for (String name : start.keySet()) {
            intervals.add(new LinearScanAllocator.LiveInterval(name, start.get(name), end.get(name)));
        }
        return intervals;
    }

    /** Print the program listing. */
    private static void printProgram(String[][] program) {
        System.out.println("  IR listing:");
        for (int i = 0; i < program.length; i++) {
            String def = program[i][0];
            String uses = String.join(", ", Arrays.copyOfRange(program[i], 1, program[i].length));
            if (def != null) {
                System.out.printf("    %2d: %s = use(%s)%n", i, def, uses);
            } else {
                System.out.printf("    %2d: ret %s%n", i, uses);
            }
        }
    }

    /** Print live intervals in a table. */
    private static void printIntervals(List<LinearScanAllocator.LiveInterval> intervals) {
        System.out.println("  Live intervals:");
        // Header
        int maxEnd = intervals.stream().mapToInt(i -> i.end).max().orElse(0);
        System.out.print("    Var      Start  End   ");
        for (int i = 0; i <= maxEnd; i++) System.out.print(i % 10);
        System.out.println();
        System.out.print("    -------- ----- ----- ");
        for (int i = 0; i <= maxEnd; i++) System.out.print("-");
        System.out.println();

        for (var iv : intervals) {
            System.out.printf("    %-8s %5d %5d  ", iv.name, iv.start, iv.end);
            for (int i = 0; i <= maxEnd; i++) {
                System.out.print(i >= iv.start && i <= iv.end ? "#" : ".");
            }
            System.out.println();
        }
    }

    /** Print the step-by-step trace. */
    private static void printTrace(List<LinearScanAllocator.TraceEntry> trace) {
        System.out.println("  Step-by-step trace:");
        int step = 1;
        for (var entry : trace) {
            System.out.printf("    Step %d: processing %s%n", step++, entry.interval);
            System.out.printf("      Active before: %s%n", entry.activeBefore);
            System.out.printf("      Action: %s%n", entry.action);
            if (entry.spilledVar != null) {
                System.out.printf("      ** SPILL: %s evicted **%n", entry.spilledVar);
            }
        }
    }

    /** Print the final allocation report. */
    private static void printReport(LinearScanAllocator alloc) {
        System.out.println("  " + alloc.generateReport().replace("\n", "\n  "));
    }

    // ==================== demo programs ====================

    /**
     * Part 1: Simple sequential program.
     * 6 temporaries, mostly non-overlapping, 4 registers should suffice.
     */
    private static final String[][] PROGRAM_SEQUENTIAL = {
        {"t0", "x", "y"},      // 0: t0 = x + y
        {"t1", "t0", "z"},     // 1: t1 = t0 + z
        {"t2", "t1", "y"},     // 2: t2 = t1 + y
        {"t3", "t2", "x"},     // 3: t3 = t2 + x
        {"t4", "t3", "t1"},    // 4: t4 = t3 + t1
        {"t5", "t4", "t0"},    // 5: t5 = t4 + t0
        {null, "t5"},          // 6: ret t5
    };

    /**
     * Part 2: High register pressure program.
     * 6 temporaries all live simultaneously → forces spills with K=2.
     */
    private static final String[][] PROGRAM_PRESSURE = {
        {"t0", "a"},           // 0: t0 = a
        {"t1", "b"},           // 1: t1 = b
        {"t2", "c"},           // 2: t2 = c
        {"t3", "d"},           // 3: t3 = d
        {"t4", "e"},           // 4: t4 = e
        {"t5", "f"},           // 5: t5 = f
        {"t6", "t0", "t1", "t2", "t3", "t4", "t5"}, // 6: t6 = big expr
        {null, "t6"},          // 7: ret t6
    };

    /**
     * Part 3: Mixed program with some overlap and long live ranges.
     * Triggers eviction (spill farthest-end active interval).
     */
    private static final String[][] PROGRAM_EVICT = {
        {"t0", "x"},           // 0: t0 = x       (long-lived)
        {"t1", "y"},           // 1: t1 = y       (long-lived)
        {"t2", "z"},           // 2: t2 = z       (medium)
        {"t3", "w"},           // 3: t3 = w       (medium)
        {"t4", "t0", "t1"},    // 4: t4 = t0 + t1 (uses long-lived)
        {"t5", "t2", "t3"},    // 5: t5 = t2 + t3 (uses medium)
        {"t6", "t4", "t5"},    // 6: t6 = t4 + t5 (combines)
        {null, "t6"},          // 7: ret t6
    };

    // ==================== main ====================

    public static void main(String[] args) {
        System.out.println("=======================================================");
        System.out.println("  EP12 — Linear Scan Register Allocation Demo");
        System.out.println("  Algorithm: Poletto & Sarkar (1999)");
        System.out.println("=======================================================");
        System.out.println();

        // ---- Part 1: Simple case, 4 registers ----
        System.out.println("=== Part 1: Sequential Program, K=4 registers ===");
        System.out.println();
        printProgram(PROGRAM_SEQUENTIAL);
        System.out.println();

        List<LinearScanAllocator.LiveInterval> intervals1 = buildIntervals(PROGRAM_SEQUENTIAL);
        printIntervals(intervals1);
        System.out.println();

        LinearScanAllocator alloc1 = new LinearScanAllocator(4);
        List<LinearScanAllocator.TraceEntry> trace1 = alloc1.allocate(intervals1);
        printTrace(trace1);
        System.out.println();
        printReport(alloc1);

        // ---- Part 2: Spill scenario, 2 registers ----
        System.out.println();
        System.out.println("=== Part 2: High Pressure, K=2 registers (forced spills) ===");
        System.out.println();
        printProgram(PROGRAM_PRESSURE);
        System.out.println();

        List<LinearScanAllocator.LiveInterval> intervals2 = buildIntervals(PROGRAM_PRESSURE);
        printIntervals(intervals2);
        System.out.println();

        LinearScanAllocator alloc2 = new LinearScanAllocator(2);
        List<LinearScanAllocator.TraceEntry> trace2 = alloc2.allocate(intervals2);
        printTrace(trace2);
        System.out.println();
        printReport(alloc2);

        // ---- Part 3: Eviction scenario, 3 registers ----
        System.out.println();
        System.out.println("=== Part 3: Eviction Scenario, K=3 registers ===");
        System.out.println();
        printProgram(PROGRAM_EVICT);
        System.out.println();

        List<LinearScanAllocator.LiveInterval> intervals3 = buildIntervals(PROGRAM_EVICT);
        printIntervals(intervals3);
        System.out.println();

        LinearScanAllocator alloc3 = new LinearScanAllocator(3);
        List<LinearScanAllocator.TraceEntry> trace3 = alloc3.allocate(intervals3);
        printTrace(trace3);
        System.out.println();
        printReport(alloc3);

        // ---- Summary ----
        System.out.println();
        System.out.println("=== Summary ===");
        System.out.printf("  Part 1 (K=4): %d intervals, %d allocated, %d spilled%n",
                intervals1.size(), alloc1.getAllocation().size(), alloc1.getSpillCount());
        System.out.printf("  Part 2 (K=2): %d intervals, %d allocated, %d spilled%n",
                intervals2.size(), alloc2.getAllocation().size(), alloc2.getSpillCount());
        System.out.printf("  Part 3 (K=3): %d intervals, %d allocated, %d spilled%n",
                intervals3.size(), alloc3.getAllocation().size(), alloc3.getSpillCount());
        System.out.println();
        System.out.println("  Key insight: fewer registers = more spills = more memory traffic.");
        System.out.println("  A stack VM does ~2-3x more memory ops than a register VM.");
        System.out.println("=======================================================");
    }
}
