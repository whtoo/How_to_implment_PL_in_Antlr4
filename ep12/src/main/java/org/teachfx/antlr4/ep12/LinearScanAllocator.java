package org.teachfx.antlr4.ep12;

import java.util.*;

/**
 * Simplified linear scan register allocator.
 *
 * <p>This is a self-contained implementation of the classic Poletto &amp; Sarkar
 * linear scan algorithm, adapted from the ep18r LinearScanAllocator but
 * stripped of all EP18R-specific dependencies (no StackOffsets, no ANTLR).</p>
 *
 * <h3>Algorithm summary</h3>
 * <ol>
 *   <li>Sort live intervals by start point</li>
 *   <li>Walk intervals in order, maintaining an <em>active</em> list of
 *       intervals currently holding registers</li>
 *   <li>For each new interval: expire old ones, then either allocate a
 *       free register or spill the farthest-reaching active interval</li>
 * </ol>
 *
 * <p>Physical registers are numbered 0..K-1 and displayed as R1..RK in
 * output for readability.</p>
 */
public class LinearScanAllocator implements IRegisterAllocator {

    // ==================== LiveInterval ====================

    /**
     * A live interval: [start, end] for a virtual register.
     * {@code start} is the first IR instruction where the vreg is defined;
     * {@code end} is the last instruction where it is used.
     */
    public static class LiveInterval {
        public final String name;
        public final int start;
        public final int end;

        public LiveInterval(String name, int start, int end) {
            this.name = name;
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            return String.format("%s[%d,%d]", name, start, end);
        }
    }

    // ==================== internal state ====================

    /** Number of physical registers available for allocation. */
    private final int K;

    /** Current register → vreg mapping (only active intervals). */
    private final String[] regToVreg;

    /** Final vreg → register assignment captured at allocation time. */
    private final Map<String, Integer> finalAssignment;

    /** Vreg → stack slot offset (negative, relative to fp). */
    private final Map<String, Integer> spillSlots;

    /** Next free spill slot index. */
    private int nextSpillSlot;

    /** Active list: intervals currently occupying registers, sorted by end. */
    private final List<ActiveEntry> active;

    /** Free registers available for allocation. */
    private final Deque<Integer> freeRegs;

    // ==================== constructor ====================

    /**
     * @param numRegisters number of physical registers (must be >= 1)
     */
    public LinearScanAllocator(int numRegisters) {
        if (numRegisters < 1) {
            throw new IllegalArgumentException("Need at least 1 register, got " + numRegisters);
        }
        this.K = numRegisters;
        this.regToVreg = new String[K];
        this.finalAssignment = new LinkedHashMap<>();
        this.spillSlots = new LinkedHashMap<>();
        this.nextSpillSlot = 0;
        this.active = new ArrayList<>();
        this.freeRegs = new ArrayDeque<>();
        reset();
    }

    /** Reset all state (useful for re-running with different intervals). */
    public void reset() {
        Arrays.fill(regToVreg, null);
        finalAssignment.clear();
        spillSlots.clear();
        nextSpillSlot = 0;
        active.clear();
        freeRegs.clear();
        for (int i = K - 1; i >= 0; i--) {
            freeRegs.push(i);
        }
    }

    // ==================== core algorithm ====================

    /**
     * Run linear scan on the given intervals.
     *
     * @param intervals live intervals (will be sorted by start internally)
     * @return ordered list of trace entries describing each step
     */
    public List<TraceEntry> allocate(List<LiveInterval> intervals) {
        reset();
        List<TraceEntry> trace = new ArrayList<>();

        // 1. Sort intervals by start point
        List<LiveInterval> sorted = new ArrayList<>(intervals);
        sorted.sort(Comparator.comparingInt(a -> a.start));

        for (LiveInterval cur : sorted) {
            // --- expire old intervals ---
            expireOld(cur.start, trace);

            // build snapshot of active for tracing
            String activeSnapshot = activeToString();

            if (!freeRegs.isEmpty()) {
                // --- allocate ---
                int reg = freeRegs.pop();
                regToVreg[reg] = cur.name;
                finalAssignment.put(cur.name, reg);
                insertActive(cur, reg);
                trace.add(new TraceEntry(cur, activeSnapshot,
                        String.format("alloc %s -> R%d", cur.name, reg + 1), null));
            } else {
                // --- must spill ---
                // Find active interval with farthest end
                ActiveEntry farthest = active.get(active.size() - 1); // last = max end

                if (farthest.interval.end > cur.end) {
                    // Spill the farthest one, give its register to current
                    String spilledName = farthest.interval.name;
                    int spilledReg = farthest.reg;
                    spillOne(spilledName, spilledReg);
                    removeActive(spilledName);

                    regToVreg[spilledReg] = cur.name;
                    finalAssignment.put(cur.name, spilledReg);
                    insertActive(cur, spilledReg);
                    trace.add(new TraceEntry(cur, activeSnapshot,
                            String.format("spill %s -> [fp%d], alloc %s -> R%d",
                                    spilledName, getSpillSlot(spilledName),
                                    cur.name, spilledReg + 1),
                            spilledName));
                } else {
                    // Spill the current one
                    spillOne(cur.name, -1);
                    trace.add(new TraceEntry(cur, activeSnapshot,
                            String.format("spill %s -> [fp%d] (no reg available)",
                                    cur.name, getSpillSlot(cur.name)),
                            cur.name));
                }
            }
        }
        return trace;
    }

    // ==================== active list helpers ====================

    /** Active entry: interval + which physical register it holds. */
    private static class ActiveEntry {
        final LiveInterval interval;
        final int reg;
        ActiveEntry(LiveInterval interval, int reg) {
            this.interval = interval;
            this.reg = reg;
        }
    }

    /** Insert into active list, maintaining sort by end point. */
    private void insertActive(LiveInterval li, int reg) {
        ActiveEntry entry = new ActiveEntry(li, reg);
        int pos = 0;
        while (pos < active.size() && active.get(pos).interval.end <= li.end) {
            pos++;
        }
        active.add(pos, entry);
    }

    /** Remove an interval from the active list by name. */
    private void removeActive(String name) {
        active.removeIf(e -> e.interval.name.equals(name));
    }

    /** Expire intervals whose end &lt; currentPos. */
    private void expireOld(int currentPos, List<TraceEntry> trace) {
        Iterator<ActiveEntry> it = active.iterator();
        while (it.hasNext()) {
            ActiveEntry e = it.next();
            if (e.interval.end < currentPos) {
                regToVreg[e.reg] = null;
                freeRegs.push(e.reg);
                it.remove();
            }
        }
    }

    private String activeToString() {
        if (active.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < active.size(); i++) {
            if (i > 0) sb.append(", ");
            ActiveEntry e = active.get(i);
            sb.append(e.interval.name).append("(").append(e.interval.end).append(")");
        }
        sb.append("]");
        return sb.toString();
    }

    // ==================== spill helpers ====================

    private void spillOne(String name, int reg) {
        if (reg >= 0) {
            regToVreg[reg] = null;
            freeRegs.push(reg);
        }
        int slot = -4 - nextSpillSlot * 4; // fp-4, fp-8, fp-12, ...
        spillSlots.put(name, slot);
        nextSpillSlot++;
    }

    // ==================== query API ====================

    /** Get the physical register assigned to a vreg (0-based), or -1 if spilled. */
    public int getRegister(String vreg) {
        return finalAssignment.getOrDefault(vreg, -1);
    }

    /** Get the stack slot offset for a spilled vreg, or 0 if not spilled. */
    public int getSpillSlot(String vreg) {
        return spillSlots.getOrDefault(vreg, 0);
    }

    /** Check if a vreg was spilled. */
    public boolean isSpilled(String vreg) {
        return spillSlots.containsKey(vreg);
    }

    /** Get the final allocation map (vreg → register number, 0-based). */
    public Map<String, Integer> getAllocation() {
        return Collections.unmodifiableMap(finalAssignment);
    }

    /** Get the spill slot map (vreg → offset). */
    public Map<String, Integer> getSpillSlots() {
        return Collections.unmodifiableMap(spillSlots);
    }

    /** Number of physical registers. */
    public int getRegisterCount() {
        return K;
    }

    /** Number of spilled vregs. */
    public int getSpillCount() {
        return spillSlots.size();
    }

    // ==================== trace entry ====================

    /**
     * One step of the linear scan trace, for demo output.
     */
    public static class TraceEntry {
        public final LiveInterval interval;
        public final String activeBefore;
        public final String action;
        public final String spilledVar; // null if no spill this step

        TraceEntry(LiveInterval interval, String activeBefore,
                   String action, String spilledVar) {
            this.interval = interval;
            this.activeBefore = activeBefore;
            this.action = action;
            this.spilledVar = spilledVar;
        }
    }

    // ==================== report ====================

    /** Generate a human-readable allocation report. */
    public String generateReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("Register assignments:\n");
        for (Map.Entry<String, Integer> e : finalAssignment.entrySet()) {
            sb.append(String.format("  %s -> R%d\n", e.getKey(), e.getValue() + 1));
        }
        if (!spillSlots.isEmpty()) {
            sb.append("Spills:\n");
            for (Map.Entry<String, Integer> e : spillSlots.entrySet()) {
                sb.append(String.format("  %s -> [fp%d]\n", e.getKey(), e.getValue()));
            }
        } else {
            sb.append("Spills: none\n");
        }
        sb.append(String.format("Register pressure: %d/%d used, %d spilled\n",
                finalAssignment.size(), K, spillSlots.size()));
        return sb.toString();
    }

    @Override
    public String toString() {
        return generateReport();
    }
}
