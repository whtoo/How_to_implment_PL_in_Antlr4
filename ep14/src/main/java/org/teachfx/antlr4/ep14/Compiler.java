package org.teachfx.antlr4.ep14;

import java.util.*;

/**
 * Classic optimizations on SSA-style three-address code.
 * Self-contained teaching demos for:
 *   1. Dead Code Elimination (DCE)
 *   2. Constant Propagation & Folding
 *   3. Common Subexpression Elimination (CSE)
 *   4. Copy Propagation
 */
public class Compiler {

    public static void main(String[] args) {
        System.out.println("=== EP14 Classic Optimizations ===");
        System.out.println();

        demoDCE();
        demoConstantPropagation();
        demoCSE();
        demoCopyPropagation();
        demoCombined();

        System.out.println("Key insight: On SSA form, these optimizations");
        System.out.println("become algorithmically trivial — each variable");
        System.out.println("has exactly one definition!");
    }

    // ═══ 1. Dead Code Elimination ═══

    static void demoDCE() {
        System.out.println("═══ 1. Dead Code Elimination (DCE) ═══");
        var prog = List.of(
            "x = 10",
            "y = 20",         // dead — y never used
            "z = x + 5",
            "w = y * 2",      // dead — w never used
            "return z"
        );
        System.out.println();
        System.out.println("Before:");
        printProg(prog);
        var result = dce(prog);
        System.out.println("After DCE:");
        printProg(result);
        System.out.printf("  Removed %d dead instructions%n%n", prog.size() - result.size());
    }

    /** DCE: remove instructions whose results are never used */
    static List<String> dce(List<String> prog) {
        // Collect all used variables
        Set<String> used = new LinkedHashSet<>();
        for (String instr : prog) {
            for (String var : extractUses(instr)) {
                used.add(var);
            }
        }

        // Keep instructions that define used variables or have side effects
        List<String> result = new ArrayList<>();
        for (String instr : prog) {
            String def = extractDef(instr);
            if (def == null || used.contains(def) || isSideEffect(instr)) {
                result.add(instr);
            }
        }
        return result;
    }

    // ═══ 2. Constant Propagation & Folding ═══

    static void demoConstantPropagation() {
        System.out.println("═══ 2. Constant Propagation & Folding ═══");
        var prog = List.of(
            "a = 10",
            "b = 5",
            "c = a + b",      // fold: c = 15
            "d = c * 2",      // fold: d = 30
            "e = d - a",      // fold: e = 20
            "return e"
        );
        System.out.println();
        System.out.println("Before:");
        printProg(prog);
        var result = constantProp(prog);
        System.out.println("After constant propagation & folding:");
        printProg(result);
        System.out.println();
    }

    /** Constant propagation + constant folding */
    static List<String> constantProp(List<String> prog) {
        Map<String, Integer> constants = new LinkedHashMap<>();
        List<String> result = new ArrayList<>();

        for (String instr : prog) {
            String def = extractDef(instr);
            String rhs = extractRHS(instr);

            if (def != null && rhs != null) {
                // Try to evaluate: replace known constants
                Integer val = tryEvaluate(rhs, constants);
                if (val != null) {
                    constants.put(def, val);
                    result.add(def + " = " + val);
                    continue;
                }
                // Replace known variables with constants
                String simplified = substituteConstants(rhs, constants);
                result.add(def + " = " + simplified);
            } else {
                // Return or side effect — substitute constants in uses
                String simplified = instr;
                for (var e : constants.entrySet()) {
                    simplified = simplified.replaceAll("\\b" + e.getKey() + "\\b", String.valueOf(e.getValue()));
                }
                result.add(simplified);
            }
        }
        return result;
    }

    /** Try to evaluate an expression to a constant */
    static Integer tryEvaluate(String expr, Map<String, Integer> constants) {
        // Substitute constants
        String sub = substituteConstants(expr, constants);
        // Try to parse as simple arithmetic
        return evalSimple(sub);
    }

    static String substituteConstants(String expr, Map<String, Integer> constants) {
        String result = expr;
        for (var e : constants.entrySet()) {
            result = result.replaceAll("\\b" + e.getKey() + "\\b", String.valueOf(e.getValue()));
        }
        return result;
    }

    /** Evaluate simple arithmetic: "10 + 5", "30 * 2", etc. */
    static Integer evalSimple(String expr) {
        expr = expr.trim();
        // Match: number op number
        var m = java.util.regex.Pattern.compile("^(\\d+)\\s*([+\\-*/%])\\s*(\\d+)$").matcher(expr);
        if (m.matches()) {
            int a = Integer.parseInt(m.group(1));
            String op = m.group(2);
            int b = Integer.parseInt(m.group(3));
            return switch (op) {
                case "+" -> a + b;
                case "-" -> a - b;
                case "*" -> a * b;
                case "/" -> b != 0 ? a / b : null;
                case "%" -> b != 0 ? a % b : null;
                default -> null;
            };
        }
        // Single number
        if (expr.matches("\\d+")) return Integer.parseInt(expr);
        return null;
    }

    // ═══ 3. Common Subexpression Elimination ═══

    static void demoCSE() {
        System.out.println("═══ 3. Common Subexpression Elimination (CSE) ═══");
        var prog = List.of(
            "t1 = a + b",
            "x = t1 * 2",
            "t2 = a + b",     // same as t1!
            "y = t2 * 3",
            "t3 = a + b",     // same as t1!
            "z = t3 + 1"
        );
        System.out.println();
        System.out.println("Before:");
        printProg(prog);
        var result = cse(prog);
        System.out.println("After CSE:");
        printProg(result);
        System.out.printf("  Eliminated %d redundant computations%n%n", prog.size() - result.size());
    }

    /** CSE: replace repeated computations with previous results */
    static List<String> cse(List<String> prog) {
        Map<String, String> exprToVar = new LinkedHashMap<>(); // "a + b" -> "t1"
        List<String> result = new ArrayList<>();

        for (String instr : prog) {
            String def = extractDef(instr);
            String rhs = extractRHS(instr);

            if (def != null && rhs != null) {
                // Normalize expression
                String normalized = normalizeExpr(rhs);
                if (exprToVar.containsKey(normalized)) {
                    // Replace with previous result
                    String prevVar = exprToVar.get(normalized);
                    result.add(def + " = " + prevVar + "  ; CSE: was " + rhs);
                } else {
                    exprToVar.put(normalized, def);
                    result.add(instr);
                }
            } else {
                result.add(instr);
            }
        }
        return result;
    }

    /** Normalize expression: sort commutative operands */
    static String normalizeExpr(String expr) {
        var m = java.util.regex.Pattern.compile("(.+)\\s*([+*])\\s*(.+)").matcher(expr.trim());
        if (m.matches()) {
            String a = m.group(1).trim();
            String op = m.group(2);
            String b = m.group(3).trim();
            // Sort for commutativity
            if (a.compareTo(b) > 0) return b + " " + op + " " + a;
            return a + " " + op + " " + b;
        }
        return expr.trim();
    }

    // ═══ 4. Copy Propagation ═══

    static void demoCopyPropagation() {
        System.out.println("═══ 4. Copy Propagation ═══");
        var prog = List.of(
            "x = y",
            "z = x + 1",     // x → y
            "w = x * 2"      // x → y
        );
        System.out.println();
        System.out.println("Before:");
        printProg(prog);
        var result = copyProp(prog);
        System.out.println("After copy propagation:");
        printProg(result);
        System.out.println();
    }

    /** Copy propagation: replace x with y where x = y */
    static List<String> copyProp(List<String> prog) {
        Map<String, String> copies = new LinkedHashMap<>(); // x → y
        List<String> result = new ArrayList<>();

        for (String instr : prog) {
            String def = extractDef(instr);
            String rhs = extractRHS(instr);

            // Detect copy: x = y (simple variable)
            if (def != null && rhs != null && rhs.matches("[a-zA-Z_]\\w*")) {
                copies.put(def, rhs);
            }

            // Substitute known copies in uses
            String substituted = instr;
            for (var e : copies.entrySet()) {
                // Don't substitute in the definition itself
                if (def != null && substituted.startsWith(def + " =")) {
                    String lhs = def + " = ";
                    String rest = substituted.substring(lhs.length());
                    substituted = lhs + replaceVar(rest, e.getKey(), e.getValue());
                } else {
                    substituted = replaceVar(substituted, e.getKey(), e.getValue());
                }
            }
            result.add(substituted);
        }
        return result;
    }

    // ═══ 5. Combined Optimization Pipeline ═══

    static void demoCombined() {
        System.out.println("═══ 5. Combined Pipeline (copy prop → CSE → const fold → DCE) ═══");
        var prog = List.of(
            "a = 5",
            "b = 10",
            "c = a",           // copy
            "d = c + b",       // c→a, then a+b=15
            "e = a + b",       // same as d
            "f = d * 2",       // 15*2=30
            "g = e * 2",       // same as f via CSE
            "h = 99",          // dead
            "return f"
        );
        System.out.println();
        System.out.println("Before (9 instructions):");
        printProg(prog);

        var step1 = copyProp(prog);
        var step2 = cse(step1);
        var step3 = constantProp(step2);
        var step4 = dce(step3);

        System.out.println("After pipeline:");
        printProg(step4);
        System.out.printf("  %d instructions → %d instructions%n", prog.size(), step4.size());
        System.out.println();
    }

    // ═══ Helpers ═══

    static String extractDef(String instr) {
        instr = instr.trim();
        int eq = instr.indexOf('=');
        if (eq > 0) {
            String lhs = instr.substring(0, eq).trim();
            if (lhs.matches("[a-zA-Z_]\\w*")) return lhs;
        }
        return null;
    }

    static String extractRHS(String instr) {
        instr = instr.trim();
        int eq = instr.indexOf('=');
        if (eq > 0) {
            String rhs = instr.substring(eq + 1).trim();
            // Strip comments
            int comment = rhs.indexOf(';');
            if (comment > 0) rhs = rhs.substring(0, comment).trim();
            return rhs;
        }
        return null;
    }

    static List<String> extractUses(String instr) {
        List<String> uses = new ArrayList<>();
        String rhs = extractRHS(instr);
        if (rhs == null) {
            // Return statement
            rhs = instr.replace("return", "").trim();
        }
        if (rhs == null) return uses;
        var m = java.util.regex.Pattern.compile("[a-zA-Z_]\\w*").matcher(rhs);
        while (m.find()) {
            String tok = m.group();
            if (!isKeyword(tok)) uses.add(tok);
        }
        return uses;
    }

    static boolean isSideEffect(String instr) {
        return instr.trim().startsWith("return") || instr.trim().startsWith("print") || instr.trim().startsWith("call");
    }

    static boolean isKeyword(String s) {
        return Set.of("return", "if", "else", "goto", "print", "call").contains(s);
    }

    static String replaceVar(String expr, String from, String to) {
        return expr.replaceAll("\\b" + from + "\\b", to);
    }

    static void printProg(List<String> prog) {
        for (String instr : prog) {
            System.out.println("    " + instr);
        }
    }
}
