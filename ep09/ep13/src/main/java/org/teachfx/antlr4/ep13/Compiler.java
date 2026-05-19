package org.teachfx.antlr4.ep13;

/** EP13 — SSA: Single Static Assignment */
public class Compiler {
    public static void main(String[] args) {
        System.out.println("=== EP13 SSA: Single Static Assignment ===");
        System.out.println();
        
        System.out.println("Source:\n  if (x > 0) { y = x; } else { y = -x; }\n  return y;");
        System.out.println();
        
        System.out.println("=== Step 1: IR with Basic Blocks ===");
        System.out.println("  B0:  t0 = x > 0");
        System.out.println("       br t0 → B1, B2");
        System.out.println("  B1:  y1 = x");
        System.out.println("       jmp → B3");
        System.out.println("  B2:  y2 = -x");
        System.out.println("  B3:  y3 = phi(y1, y2)   ← φ inserted at merge point");
        System.out.println("       return y3");
        System.out.println();
        
        System.out.println("=== Step 2: Dominator Tree ===");
        System.out.println("  B0 dominates {B1, B2, B3}");
        System.out.println("  B1 dominates {}");
        System.out.println("  B2 dominates {}");
        System.out.println("  B3 dominates {}");
        System.out.println();
        
        System.out.println("=== Step 3: Dominance Frontiers ===");
        System.out.println("  DF(B0) = {}");
        System.out.println("  DF(B1) = {B3}  ← φ needed here for y");
        System.out.println("  DF(B2) = {B3}  ← φ needed here for y");
        System.out.println("  DF(B3) = {}");
        System.out.println();
        
        System.out.println("=== Step 4: Phi Placement ===");
        System.out.println("  Variable 'y' has definitions in B1 and B2");
        System.out.println("  DF(B1) ∩ DF(B2) = {B3} → place φ(y1,y2) at B3");
        System.out.println();
        
        System.out.println("Key: SSA makes every assignment unique,");
        System.out.println("enabling simple optimization algorithms.");
    }
}
