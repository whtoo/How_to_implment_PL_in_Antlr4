package org.teachfx.antlr4.ep15;

import org.teachfx.antlr4.ep15.gc.*;
import org.teachfx.antlr4.ep15.tro.TailRecursionOptimizer;

/**
 * EP15 — Hello, Compiler!
 *
 * Demonstrates two foundational compiler technologies:
 *   Part 1: Tail Recursion Optimization (TRO) — detect and transform tail calls
 *   Part 2: Garbage Collection (GC) — reference counting with free-list management
 *   Part 3: Benchmark — live timing comparison of naive vs optimized fibonacci
 */
public class Compiler {

    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("  EP15 — Hello, Compiler!  Tail Recursion Optimization + GC");
        System.out.println("================================================================");
        System.out.println();

        // ---- Part 1: Tail Recursion Optimization ----
        part1_TRO();

        // ---- Part 2: Garbage Collection ----
        part2_GC();

        // ---- Part 3: Live Benchmark ----
        part3_Benchmark();

        System.out.println("================================================================");
        System.out.println("  Key Insight: Compilers don't just translate — they TRANSFORM.");
        System.out.println("  TRO turns recursion into loops; GC automates memory lifecycle.");
        System.out.println("================================================================");
    }

    // -----------------------------------------------------------------------
    // Part 1: Tail Recursion Optimization
    // -----------------------------------------------------------------------
    private static void part1_TRO() {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║  Part 1: Tail Recursion Optimization (TRO)                  ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("A 'tail call' is a function call that is the LAST operation");
        System.out.println("before returning. Tail-recursive calls can be replaced with");
        System.out.println("a simple jump (loop) — no new stack frame needed!");
        System.out.println();

        // Run the full TRO demo (analysis + transformation + explanation)
        System.out.println(TailRecursionOptimizer.fullDemo());
        System.out.println();
    }

    // -----------------------------------------------------------------------
    // Part 2: Garbage Collection — Reference Counting Demo
    // -----------------------------------------------------------------------
    private static void part2_GC() {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║  Part 2: Reference Counting Garbage Collector               ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Reference counting tracks how many pointers reference each");
        System.out.println("object. When the count drops to zero, the object is freed.");
        System.out.println();

        // Create a GC with 1024-byte heap
        ReferenceCountingGC gc = new ReferenceCountingGC(1024);
        System.out.println("--- Initial State ---");
        System.out.println("  " + gc.getHeapInfo());
        System.out.println();

        // Allocate some objects
        System.out.println("--- Allocating Objects ---");
        int objA = gc.allocate(64);
        System.out.println("  allocate(64)  -> obj#" + objA + "  [refcount=1]");
        int objB = gc.allocate(128);
        System.out.println("  allocate(128) -> obj#" + objB + "  [refcount=1]");
        int objC = gc.allocate(32);
        System.out.println("  allocate(32)  -> obj#" + objC + "  [refcount=1]");
        System.out.println("  " + gc.getHeapInfo());
        System.out.println();

        // Simulate reference sharing
        System.out.println("--- Reference Sharing ---");
        gc.incrementRef(objA);
        System.out.println("  incrementRef(obj#" + objA + ")  -> refcount=2  (shared reference)");
        gc.incrementRef(objB);
        gc.incrementRef(objB);
        System.out.println("  incrementRef(obj#" + objB + ") x2 -> refcount=3  (multiple refs)");
        System.out.println();

        // Drop references and observe collection
        System.out.println("--- Dropping References ---");

        gc.decrementRef(objA);
        System.out.println("  decrementRef(obj#" + objA + ")  -> refcount=1  (still alive)");
        System.out.println("  obj#" + objA + " alive? " + gc.isObjectAlive(objA));

        gc.decrementRef(objA);
        System.out.println("  decrementRef(obj#" + objA + ")  -> refcount=0  -> COLLECTED!");
        System.out.println("  obj#" + objA + " alive? " + gc.isObjectAlive(objA));
        System.out.println("  " + gc.getHeapInfo());
        System.out.println();

        // Drop objB references
        gc.decrementRef(objB);
        System.out.println("  decrementRef(obj#" + objB + ")  -> refcount=2");
        gc.decrementRef(objB);
        System.out.println("  decrementRef(obj#" + objB + ")  -> refcount=1");
        gc.decrementRef(objB);
        System.out.println("  decrementRef(obj#" + objB + ")  -> refcount=0  -> COLLECTED!");
        System.out.println("  obj#" + objB + " alive? " + gc.isObjectAlive(objB));
        System.out.println();

        // Force collection
        System.out.println("--- Force GC ---");
        gc.forceGC();
        System.out.println("  " + gc.getHeapInfo());
        System.out.println();

        // Allocate after collection (memory reuse)
        System.out.println("--- Allocate After Collection ---");
        int objD = gc.allocate(200);
        System.out.println("  allocate(200) -> obj#" + objD + "  [reuses freed space]");
        System.out.println("  " + gc.getHeapInfo());
        System.out.println();

        // Show stats
        System.out.println("--- GC Statistics ---");
        System.out.println(gc.getStats().getSummary());
    }

    // -----------------------------------------------------------------------
    // Part 3: Live Benchmark
    // -----------------------------------------------------------------------
    private static void part3_Benchmark() {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║  Part 3: Live Benchmark — fib(30)                          ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();

        System.out.println("Running fib(30) three ways and timing each:\n");

        int n = 30;

        // Warmup
        TailRecursionOptimizer.fibNaive(10);
        TailRecursionOptimizer.fibTailRecursive(10, 0, 1);
        TailRecursionOptimizer.fibIterative(10);

        // Naive
        long start1 = System.nanoTime();
        long resultNaive = TailRecursionOptimizer.fibNaive(n);
        long timeNaive = System.nanoTime() - start1;
        System.out.printf("  1. Naive recursive:    fib(%d) = %-12d  [%8.3f ms]%n", n, resultNaive, timeNaive / 1_000_000.0);
        System.out.println("     ^ Makes ~2.7 million calls! O(2^n) exponential time.");

        // Tail-recursive (still allocates stack frames)
        long start2 = System.nanoTime();
        long resultTR = TailRecursionOptimizer.fibTailRecursive(n, 0, 1);
        long timeTR = System.nanoTime() - start2;
        System.out.printf("  2. Tail-recursive:     fib(%d) = %-12d  [%8.3f ms]%n", n, resultTR, timeTR / 1_000_000.0);
        System.out.println("     ^ Only n calls, but still uses O(n) stack space.");

        // Iterative (what TRO compiles to)
        long start3 = System.nanoTime();
        long resultIter = TailRecursionOptimizer.fibIterative(n);
        long timeIter = System.nanoTime() - start3;
        System.out.printf("  3. Iterative (TRO'd):  fib(%d) = %-12d  [%8.3f ms]%n", n, resultIter, timeIter / 1_000_000.0);
        System.out.println("     ^ Only n iterations, O(1) stack — the compiler's output.");
        System.out.println();

        // Speedup
        double speedup = (double) timeNaive / timeIter;
        System.out.printf("  *** Speedup: %.1fx faster after optimization ***%n", speedup);
        System.out.printf("  All results match: %s%n", (resultNaive == resultTR && resultTR == resultIter) ? "YES ✓" : "NO");
        System.out.println();
    }
}
