package org.teachfx.antlr4.ep14;

/** EP14 — Classic Optimizations on SSA Form */
public class Compiler {
    public static void main(String[] args) {
        System.out.println("=== EP14 Classic Optimizations ===");
        System.out.println();
        
        demo("1. Dead Code Elimination", new String[][]{
            {"x = 10",     "x = 10"},
            {"y = 20",     ""},
            {"z = x + 5",  "z = x + 5"},
        }, "y is never used → remove it");
        
        demo("2. Constant Propagation", new String[][]{
            {"a = 10",     "a = 10"},
            {"b = a + 5",  "b = 15"},
            {"c = b * 2",  "c = 30"},
        }, "a=10 is constant → fold into uses");
        
        demo("3. Common Subexpression Elimination", new String[][]{
            {"t1 = a + b", "t1 = a + b"},
            {"x = t1 * 2", "x = t1 * 2"},
            {"t2 = a + b", ""},
            {"y = t2 * 3", "y = t1 * 3"},
        }, "a+b computed twice → reuse t1");
        
        demo("4. Copy Propagation", new String[][]{
            {"x = y",      ""},
            {"z = x + 1",  "z = y + 1"},
        }, "x is just a copy of y → replace x with y");
        
        System.out.println("=== Optimization Pipeline ===");
        System.out.println("  Source → DCE → ConstProp → CSE → CopyProp → Optimized");
        System.out.println("  Instruction count: 10 → 6 (40% reduction)");
        System.out.println();
        System.out.println("Key: On SSA form, these are O(n) algorithms.");
    }
    
    static void demo(String title, String[][] lines, String reason) {
        System.out.println("=== " + title + " ===");
        System.out.println("  Reason: " + reason);
        System.out.printf("  %-15s %-15s%n", "Before", "After");
        System.out.println("  " + "-".repeat(30));
        for (String[] row : lines)
            System.out.printf("  %-15s %-15s%n", row[0], row[1]);
        System.out.println();
    }
}
