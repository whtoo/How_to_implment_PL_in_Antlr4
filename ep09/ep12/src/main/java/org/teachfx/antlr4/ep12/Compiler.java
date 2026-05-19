package org.teachfx.antlr4.ep12;

import org.teachfx.antlr4.ep12.alloc.LinearScanAllocator;
import java.util.*;

/** EP12 — Register Allocation with Linear Scan */
public class Compiler {
    public static void main(String[] args) {
        System.out.println("=== EP12 Register Allocation: Linear Scan ===");
        int K = 4;
        System.out.printf("Physical registers: R0-R%d%n%n", K-1);
        
        // Build program: t0=x+y; t1=t0*z; t2=t1-y; t3=t2+x; t4=t3*t1; return t4
        var program = List.of(
            new LinearScanAllocator.Instruction("t0", "x", "y"),
            new LinearScanAllocator.Instruction("t1", "t0", "z"),
            new LinearScanAllocator.Instruction("t2", "t1", "y"),
            new LinearScanAllocator.Instruction("t3", "t2", "x"),
            new LinearScanAllocator.Instruction("t4", "t3", "t1"),
            new LinearScanAllocator.Instruction(null, "t4")
        );
        
        System.out.println("=== Program (6 vregs) ===");
        for (int i = 0; i < program.size(); i++) {
            var inst = program.get(i);
            String code = inst.def() != null ? inst.def() + " = " + String.join(" ", inst.uses()) : "ret " + inst.uses()[0];
            System.out.printf("  %2d: %s%n", i, code);
        }
        
        var alloc = new LinearScanAllocator(K);
        var result = alloc.allocate(program);
        
        System.out.println("\n=== Live Intervals (sorted by start) ===");
        System.out.printf("  %-6s %6s %6s%n", "VReg", "Start", "End");
        System.out.println("  ------ ------ ------");
        // Show allocation result as intervals
        for (var e : result.mapping().entrySet())
            System.out.printf("  %-6s R%-5d %s%n", e.getKey(), e.getValue(), "");
        for (var s : result.spilled())
            System.out.printf("  %-6s SPILLED%n", s);
        
        System.out.println("\n=== Allocation ===");
        for (var e : result.mapping().entrySet())
            System.out.printf("  %s → R%d%n", e.getKey(), e.getValue());
        for (var s : result.spilled())
            System.out.printf("  %s → stack[spill]%n", s);
        
        System.out.printf("\nResults: %d allocated, %d spilled (%.0f%% success)%n",
            result.mapping().size(), result.spilled().size(),
            100.0 * result.mapping().size() / (result.mapping().size() + result.spilled().size()));
    }
}
