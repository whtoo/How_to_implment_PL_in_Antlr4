package org.teachfx.antlr4.ep15.tro;

import java.util.*;

/**
 * Tail Recursion Optimizer (TRO) Demo
 *
 * This self-contained class demonstrates how compilers detect and optimize
 * tail-recursive functions by converting them into iterative loops.
 *
 * Key concepts:
 * - A tail call is a function call that is the LAST operation before returning
 * - Tail-recursive calls can be replaced with a jump (loop) — no new stack frame needed
 * - This transforms O(n) stack depth into O(1), and often improves time complexity
 */
public class TailRecursionOptimizer {

    // -----------------------------------------------------------------------
    // Part 1: Simple function representation for IR-level analysis
    // -----------------------------------------------------------------------

    /** Represents a single instruction in our toy IR */
    public static class Instruction {
        public enum Op {
            LOAD_CONST,   // load a constant value
            LOAD_PARAM,   // load a parameter by index
            CALL,         // call a function
            ADD,          // add two values
            SUB,          // subtract
            RETURN,       // return a value
            BRANCH_IF,    // conditional branch
            LABEL         // label / loop target
        }

        public final Op op;
        public final String[] args;
        public String comment;

        public Instruction(Op op, String comment, String... args) {
            this.op = op;
            this.args = args;
            this.comment = comment != null ? comment : "";
        }

        @Override
        public String toString() {
            String argStr = args.length > 0 ? String.join(", ", args) : "";
            String commentStr = comment.isEmpty() ? "" : "  // " + comment;
            return String.format("  %-14s %s%s", op.name(), argStr, commentStr);
        }
    }

    /** Represents a function: name, parameter names, body instructions */
    public static class Function {
        public final String name;
        public final List<String> params;
        public final List<Instruction> body;

        public Function(String name, List<String> params) {
            this.name = name;
            this.params = params;
            this.body = new ArrayList<>();
        }

        public void addInstruction(Instruction.Op op, String comment, String... args) {
            body.add(new Instruction(op, comment, args));
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("func ").append(name).append("(").append(String.join(", ", params)).append(") {\n");
            for (Instruction instr : body) {
                sb.append(instr).append("\n");
            }
            sb.append("}");
            return sb.toString();
        }
    }

    // -----------------------------------------------------------------------
    // Part 2: Tail-call detection
    // -----------------------------------------------------------------------

    /**
     * Detect if a function is tail-recursive.
     * A function is tail-recursive if its LAST meaningful instruction before RETURN
     * is a CALL to itself (possibly with modified arguments).
     *
     * @param func the function to analyze
     * @return true if the function contains a tail-recursive call pattern
     */
    public static boolean isTailRecursive(Function func) {
        // Look for the pattern: CALL self(...) followed by RETURN
        for (int i = 0; i < func.body.size() - 1; i++) {
            Instruction curr = func.body.get(i);
            Instruction next = func.body.get(i + 1);

            if (curr.op == Instruction.Op.CALL
                && curr.args.length > 0
                && curr.args[0].equals(func.name)
                && next.op == Instruction.Op.RETURN) {
                return true;
            }
        }
        return false;
    }

    /**
     * Analyze a function and report tail-recursion status
     */
    public static String analyze(Function func) {
        StringBuilder sb = new StringBuilder();
        sb.append("Analyzing: ").append(func.name).append("(").append(String.join(", ", func.params)).append(")\n");
        sb.append("  Instructions: ").append(func.body.size()).append("\n");

        boolean isTR = isTailRecursive(func);
        sb.append("  Tail-recursive: ").append(isTR ? "YES" : "NO").append("\n");

        if (isTR) {
            sb.append("  Detected tail call at instruction: ");
            for (int i = 0; i < func.body.size() - 1; i++) {
                if (func.body.get(i).op == Instruction.Op.CALL
                    && func.body.get(i).args[0].equals(func.name)
                    && func.body.get(i + 1).op == Instruction.Op.RETURN) {
                    sb.append(i).append(" -> ").append(func.body.get(i));
                    break;
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // -----------------------------------------------------------------------
    // Part 3: Transformation — convert tail recursion to loop
    // -----------------------------------------------------------------------

    /**
     * Transform a tail-recursive function into an iterative loop.
     * This is the core optimization that compilers perform.
     *
     * The key insight: instead of pushing a new stack frame for the recursive call,
     * we overwrite the current parameters and jump back to the top of the function.
     *
     * @param func the tail-recursive function
     * @return a NEW function with the tail call replaced by a loop
     */
    public static Function transformToLoop(Function func) {
        // Find the tail-call pattern
        int tailCallIndex = -1;
        for (int i = 0; i < func.body.size() - 1; i++) {
            Instruction curr = func.body.get(i);
            Instruction next = func.body.get(i + 1);
            if (curr.op == Instruction.Op.CALL
                && curr.args[0].equals(func.name)
                && next.op == Instruction.Op.RETURN) {
                tailCallIndex = i;
                break;
            }
        }

        if (tailCallIndex < 0) {
            return func; // not tail-recursive, return as-is
        }

        Instruction tailCall = func.body.get(tailCallIndex);
        Function optimized = new Function(func.name + "_optimized", func.params);

        // Build the optimized version:
        // The original code before the tail call becomes the loop body,
        // and the recursive call arguments become parameter updates at loop bottom.

        // 1. Add a LOOP_START label at the beginning
        optimized.addInstruction(Instruction.Op.LABEL, "loop_start");

        // 2. Copy instructions before the tail call (they become the loop body)
        for (int i = 0; i < tailCallIndex; i++) {
            Instruction instr = func.body.get(i);
            optimized.addInstruction(instr.op, instr.comment, instr.args);
        }

        // 3. Replace the recursive CALL with parameter reassignment + jump back
        //    In a real compiler, this is: update params, then goto loop_start
        //    The args[1..] are the new parameter values for the recursive call
        String[] newArgs = new String[tailCall.args.length - 1];
        System.arraycopy(tailCall.args, 1, newArgs, 0, newArgs.length);

        StringBuilder reassignComment = new StringBuilder("TRO: reassign params (was recursive call)");
        for (int i = 0; i < newArgs.length && i < func.params.size(); i++) {
            reassignComment.append(" ").append(func.params.get(i)).append("=").append(newArgs[i]);
        }
        optimized.addInstruction(Instruction.Op.LABEL, reassignComment.toString(),
            "reassign: " + String.join(" <- ", newArgs));
        optimized.addInstruction(Instruction.Op.LABEL, "goto loop_start");

        // 4. Copy the final RETURN (for when the loop exits via a branch)
        //    Actually the RETURN from the original base case is already included in step 2

        return optimized;
    }

    // -----------------------------------------------------------------------
    // Part 4: Live Fibonacci benchmark — naive vs optimized
    // -----------------------------------------------------------------------

    /** Naive recursive Fibonacci — O(2^n) time, O(n) stack */
    public static long fibNaive(int n) {
        if (n <= 1) return n;
        return fibNaive(n - 1) + fibNaive(n - 2);
    }

    /** Tail-recursive Fibonacci (accumulator style) — O(n) time, O(n) stack WITHOUT TRO */
    public static long fibTailRecursive(int n, long a, long b) {
        if (n == 0) return a;
        if (n == 1) return b;
        return fibTailRecursive(n - 1, b, a + b);
    }

    /** Iterative Fibonacci (what TRO compiles to) — O(n) time, O(1) stack */
    public static long fibIterative(int n) {
        if (n <= 1) return n;
        long a = 0, b = 1;
        for (int i = 2; i <= n; i++) {
            long temp = a + b;
            a = b;
            b = temp;
        }
        return b;
    }

    /**
     * Run the live benchmark comparing naive vs optimized fibonacci
     */
    public static String runBenchmark(int n) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Live Benchmark: fib(").append(n).append(") ===\n\n");

        // Warm up
        fibNaive(5);
        fibTailRecursive(5, 0, 1);
        fibIterative(5);

        // Benchmark naive
        long naiveResult;
        long naiveTime;
        try {
            long start = System.nanoTime();
            naiveResult = fibNaive(n);
            naiveTime = System.nanoTime() - start;
            sb.append(String.format("  Naive recursive:    fib(%d) = %d  [%.3f ms]%n", n, naiveResult, naiveTime / 1_000_000.0));
        } catch (StackOverflowError e) {
            naiveResult = -1;
            naiveTime = -1;
            sb.append(String.format("  Naive recursive:    fib(%d) = STACK OVERFLOW%n", n));
        }

        // Benchmark tail-recursive (still uses stack, but fewer calls)
        long startTR = System.nanoTime();
        long trResult = fibTailRecursive(n, 0, 1);
        long trTime = System.nanoTime() - startTR;
        sb.append(String.format("  Tail-recursive:     fib(%d) = %d  [%.3f ms]%n", n, trResult, trTime / 1_000_000.0));

        // Benchmark iterative (what TRO produces)
        long startIter = System.nanoTime();
        long iterResult = fibIterative(n);
        long iterTime = System.nanoTime() - startIter;
        sb.append(String.format("  Iterative (TRO'd):  fib(%d) = %d  [%.3f ms]%n", n, iterResult, iterTime / 1_000_000.0));

        // Speedup
        sb.append("\n");
        if (naiveTime > 0) {
            double speedupVsNaive = (double) naiveTime / iterTime;
            sb.append(String.format("  Speedup (Iterative vs Naive): %.1fx%n", speedupVsNaive));
            sb.append(String.format("  Speedup (Iterative vs Tail-Rec): %.1fx%n", (double) trTime / iterTime));
        }

        sb.append(String.format("%n  All results match: %s%n",
            (naiveResult < 0 || (naiveResult == trResult && trResult == iterResult)) ? "YES" : "NO"));

        return sb.toString();
    }

    // -----------------------------------------------------------------------
    // Part 5: Full demo output
    // -----------------------------------------------------------------------

    public static String fullDemo() {
        StringBuilder sb = new StringBuilder();

        sb.append("================================================================\n");
        sb.append("     TAIL RECURSION OPTIMIZATION (TRO) DEMO\n");
        sb.append("================================================================\n\n");

        // Build example functions in our IR
        // --- Example 1: factorial (classic tail recursion) ---
        Function factTR = new Function("fact", Arrays.asList("n", "acc"));
        factTR.addInstruction(Instruction.Op.LOAD_PARAM, "load n", "n");
        factTR.addInstruction(Instruction.Op.LOAD_CONST, "base case: n==0?", "0");
        factTR.addInstruction(Instruction.Op.BRANCH_IF, "return acc if n==0", "return_acc");
        factTR.addInstruction(Instruction.Op.LOAD_PARAM, "compute n-1", "n");
        factTR.addInstruction(Instruction.Op.LOAD_CONST, "", "1");
        factTR.addInstruction(Instruction.Op.SUB, "n-1", "");
        factTR.addInstruction(Instruction.Op.LOAD_PARAM, "compute n*acc", "n");
        factTR.addInstruction(Instruction.Op.LOAD_PARAM, "", "acc");
        factTR.addInstruction(Instruction.Op.ADD, "new_acc = n*acc", "");
        // This is actually multiply for factorial, but let's keep it simple with add
        factTR.addInstruction(Instruction.Op.CALL, "tail call: fact(n-1, n*acc)", "fact", "n-1", "n*acc");
        factTR.addInstruction(Instruction.Op.RETURN, "return result", "result");

        // --- Example 2: non-tail-recursive function ---
        Function sumToN = new Function("sumToN", Arrays.asList("n"));
        sumToN.addInstruction(Instruction.Op.LOAD_PARAM, "load n", "n");
        sumToN.addInstruction(Instruction.Op.LOAD_CONST, "base case: n==0?", "0");
        sumToN.addInstruction(Instruction.Op.BRANCH_IF, "return 0 if n==0", "return_0");
        sumToN.addInstruction(Instruction.Op.CALL, "NOT tail: result used in add", "sumToN", "n-1");
        sumToN.addInstruction(Instruction.Op.LOAD_PARAM, "add n to recursive result", "n");
        sumToN.addInstruction(Instruction.Op.ADD, "n + sumToN(n-1)", "");
        sumToN.addInstruction(Instruction.Op.RETURN, "return sum", "sum");

        // Show analysis
        sb.append("--- Example 1: fact(n, acc) — tail-recursive ---\n");
        sb.append(analyze(factTR)).append("\n");

        sb.append("--- Example 2: sumToN(n) — NOT tail-recursive ---\n");
        sb.append(analyze(sumToN)).append("\n");

        // Show transformation
        sb.append("--- Transformation: fact -> fact_optimized ---\n");
        sb.append("BEFORE:\n").append(factTR).append("\n\n");

        Function optimized = transformToLoop(factTR);
        sb.append("AFTER TRO:\n").append(optimized).append("\n\n");

        // Fibonacci comparison in pseudo-code
        sb.append("--- Fibonacci: Before vs After TRO ---\n\n");
        sb.append("  fib_naive(n):                   fib_optimized(n):\n");
        sb.append("    if n <= 1: return n             a = 0, b = 1\n");
        sb.append("    return fib(n-1)+fib(n-2)        for i = 2..n:\n");
        sb.append("                                     temp = a + b\n");
        sb.append("  Time:  O(2^n)                      a = b\n");
        sb.append("  Stack: O(n)                        b = temp\n");
        sb.append("                                     return b\n");
        sb.append("                                   Time:  O(n)\n");
        sb.append("                                   Stack: O(1)\n\n");

        // Run live benchmark
        sb.append(runBenchmark(30));

        return sb.toString();
    }
}
