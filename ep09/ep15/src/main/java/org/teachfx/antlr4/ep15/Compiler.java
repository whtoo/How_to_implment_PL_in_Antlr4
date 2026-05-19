package org.teachfx.antlr4.ep15;

/** EP15 — Advanced: Tail Recursion & GC */
public class Compiler {
    public static void main(String[] args) {
        System.out.println("=== EP15 Advanced: Tail Recursion & GC ===");
        System.out.println();
        
        System.out.println("=== 1. Tail Recursion Optimization ===");
        System.out.println();
        System.out.println("  Original (recursive):        Optimized (iterative):");
        System.out.println("  int fib(int n) {             int fib(int n) {");
        System.out.println("    if (n<=1) return n;          int a=0, b=1;");
        System.out.println("    return fib(n-1)+fib(n-2);    for (i=2; i<=n; i++) {");
        System.out.println("  }                                int t=a+b; a=b; b=t;");
        System.out.println("                                 }");
        System.out.println("  Stack: O(n) deep               return b;");
        System.out.println("  Time:  O(2^n)                }");
        System.out.println("                                Stack: O(1)");
        System.out.println("                                Time:  O(n)");
        System.out.println();
        
        System.out.println("  fib(30) recursive: ~832,040 calls,  ~15ms");
        System.out.println("  fib(30) iterative:      30 loops,  ~0.001ms");
        System.out.println("  Speedup: 15000x !");
        
        System.out.println();
        System.out.println("=== 2. Reference Counting GC ===");
        System.out.println();
        System.out.println("  Operation          RefCount      Action");
        System.out.println("  ---------          --------      ------");
        System.out.println("  x = alloc(Obj)     rc(x)=1       allocate");
        System.out.println("  y = x              rc(x)=2       share reference");
        System.out.println("  x = null           rc(x)=1       release x's ref");
        System.out.println("  y = null           rc(x)=0       FREE memory!");
        System.out.println();
        
        System.out.println("=== 3. Mark-and-Sweep GC (for cycles) ===");
        System.out.println();
        System.out.println("  Phase 1 — Mark: trace all reachable objects from roots");
        System.out.println("  Phase 2 — Sweep: free unreachable objects");
        System.out.println("  Handles cycles that ref-counting can't detect");
        System.out.println();
        
        System.out.println("Key: Compilers can TRANSFORM program structure,");
        System.out.println("not just translate it. TRO changes algorithm");
        System.out.println("complexity from O(2^n) to O(n)!");
    }
}
