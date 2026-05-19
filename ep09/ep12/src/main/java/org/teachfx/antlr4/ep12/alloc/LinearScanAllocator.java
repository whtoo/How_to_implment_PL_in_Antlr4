package org.teachfx.antlr4.ep12.alloc;

import java.util.*;

/**
 * Simplified Linear-Scan Register Allocator.
 * Input: instruction stream with virtual registers (t0,t1,...)
 * Output: mapping virtual → physical register (or spilled)
 */
public class LinearScanAllocator {
    private final int K;
    
    public LinearScanAllocator(int numPhysicalRegisters) { this.K = numPhysicalRegisters; }
    
    public record Result(Map<String,Integer> mapping, List<String> spilled) {}
    
    public Result allocate(List<Instruction> program) {
        // Step 1: collect virtual registers
        Set<String> vregs = new LinkedHashSet<>();
        for (Instruction inst : program) {
            if (inst.def != null) vregs.add(inst.def);
            for (String u : inst.uses) if (u.startsWith("t")) vregs.add(u);
        }
        
        // Step 2: compute live intervals
        Map<String,Interval> intervals = new LinkedHashMap<>();
        for (String v : vregs) intervals.put(v, new Interval(v, Integer.MAX_VALUE, -1));
        for (int i = 0; i < program.size(); i++) {
            Instruction inst = program.get(i);
            if (inst.def != null && intervals.containsKey(inst.def)) {
                Interval iv = intervals.get(inst.def);
                if (i < iv.start) iv.start = i;
            }
            for (String u : inst.uses) {
                if (intervals.containsKey(u)) {
                    Interval iv = intervals.get(u);
                    if (i < iv.start) iv.start = i;
                    if (i > iv.end) iv.end = i;
                }
            }
        }
        
        // Step 3: linear scan
        List<Interval> sorted = new ArrayList<>(intervals.values());
        sorted.sort(Comparator.comparingInt(a -> a.start));
        Map<String,Integer> mapping = new LinkedHashMap<>();
        List<String> spilled = new ArrayList<>();
        List<Interval> active = new ArrayList<>();
        
        for (Interval cur : sorted) {
            // Expire dead intervals
            active.removeIf(old -> old.end < cur.start);
            if (active.size() < K) {
                int r = findFree(active);
                mapping.put(cur.name, r);
                cur.reg = r;
                active.add(cur);
            } else {
                // Spill the farthest-end interval
                Interval farthest = active.get(0);
                for (Interval a : active) if (a.end > farthest.end) farthest = a;
                if (farthest.end > cur.end) {
                    mapping.remove(farthest.name);
                    spilled.add(farthest.name);
                    int r = farthest.reg;
                    mapping.put(cur.name, r);
                    cur.reg = r;
                    active.remove(farthest);
                    active.add(cur);
                } else {
                    spilled.add(cur.name);
                }
            }
        }
        return new Result(mapping, spilled);
    }
    
    private int findFree(List<Interval> active) {
        boolean[] used = new boolean[K];
        for (Interval a : active) if (a.reg >= 0) used[a.reg] = true;
        for (int i = 0; i < K; i++) if (!used[i]) return i;
        return 0;
    }
    
    public static class Interval {
        String name; int start, end, reg = -1;
        Interval(String n, int s, int e) { name=n; start=s; end=e; }
    }
    
    public record Instruction(String def, String... uses) {}
}
