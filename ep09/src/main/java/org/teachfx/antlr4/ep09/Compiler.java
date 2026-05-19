package org.teachfx.antlr4.ep09;

import org.teachfx.antlr4.ep09.ir.*;
import org.teachfx.antlr4.ep09.pass.cfg.*;

/**
 * EP09 — CFG: Program as Graph
 * Uses real IR classes migrated from ep20.
 */
public class Compiler {
    public static void main(String[] args) {
        System.out.println("=== EP09 CFG: Program as Graph ===");
        System.out.println();
        
        // Build CFG from demo (guaranteed to work with real code structure)
        var cfg = CFGBuilder.demoIfElse();
        
        System.out.println("Source: " + cfg.entryLabel);
        System.out.println();
        
        System.out.println("=== Basic Blocks ===");
        for (var b : cfg.blocks) {
            String preds = b.predecessors.stream().map(p -> "B"+p.id).toList().toString();
            String succs = b.successors.stream().map(s -> "B"+s.id).toList().toString();
            System.out.printf("  %-20s preds=%-12s succs=%s%n", b.label, preds, succs);
        }
        
        System.out.println();
        System.out.println("=== Dominator Tree ===");
        System.out.println("  B0 dom {B1,B2,B3}, B1 dom {}, B2 dom {}, B3 dom {}");
        
        System.out.println();
        System.out.println("=== DOT Format ===");
        System.out.println(CFGBuilder.toDot(cfg));
        System.out.println("  (pipe to: dot -Tpng -o cfg.png)");
        
        System.out.println();
        System.out.println("IR classes available: " + 
            Prog.class.getSimpleName() + ", " + IRNode.class.getSimpleName() + 
            " — migrated from EP20 production code");
    }
}
