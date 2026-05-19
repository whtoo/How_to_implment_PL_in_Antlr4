package org.teachfx.antlr4.ep13;

import java.util.*;

/**
 * EP13 — SSA: Static Single Assignment
 *
 * Demonstrates SSA construction from a simple CFG:
 * 1. Build control-flow graph with if/else branches
 * 2. Compute dominator tree and dominance frontiers
 * 3. Insert phi functions at dominance frontiers
 * 4. Rename variables to SSA form
 *
 * All code is self-contained — no ANTLR or external IR types.
 */
public class Compiler {

    public static void main(String[] args) {
        System.out.println("=== EP13 SSA: Static Single Assignment ===");
        System.out.println();

        // ── Part 1: if/else merge (classic phi example) ──
        System.out.println("═══ Part 1: if/else merge ═══");
        demoIfElse();

        // ── Part 2: loop (back-edge creates dominance frontier) ──
        System.out.println("═══ Part 2: loop with back-edge ═══");
        demoLoop();

        // ── Part 3: nested branches (multiple phi functions) ──
        System.out.println("═══ Part 3: nested branches ═══");
        demoNested();

        System.out.println("Key insight: SSA makes every definition unique,");
        System.out.println("enabling simple, powerful optimizations!");
    }

    // ── Demo 1: if/else merge ──

    static void demoIfElse() {
        var cfg = new SimpleCFG();

        // B0: entry
        cfg.addBlock(0, "entry")
            .add("x = 1")
            .add("cond = x > 0")
            .add("if cond goto B1 else B2");

        // B1: then
        cfg.addBlock(1, "then")
            .add("x = 10");

        // B2: else
        cfg.addBlock(2, "else")
            .add("x = 20");

        // B3: merge
        cfg.addBlock(3, "merge")
            .add("y = x + 1")
            .add("return y");

        cfg.addEdge(0, 1);
        cfg.addEdge(0, 2);
        cfg.addEdge(1, 3);
        cfg.addEdge(2, 3);

        runDemo(cfg, """
            if (x > 0) { x = 10; } else { x = 20; }
            y = x + 1;""");
    }

    // ── Demo 2: loop ──

    static void demoLoop() {
        var cfg = new SimpleCFG();

        cfg.addBlock(0, "entry")
            .add("i = 0")
            .add("sum = 0")
            .add("goto loop_header");

        cfg.addBlock(1, "loop_header")
            .add("cond = i < 10")
            .add("if cond goto loop_body else exit");

        cfg.addBlock(2, "loop_body")
            .add("sum = sum + i")
            .add("i = i + 1")
            .add("goto loop_header");

        cfg.addBlock(3, "exit")
            .add("return sum");

        cfg.addEdge(0, 1);
        cfg.addEdge(1, 2);
        cfg.addEdge(1, 3);
        cfg.addEdge(2, 1); // back-edge

        runDemo(cfg, """
            i=0; sum=0;
            while (i<10) { sum+=i; i++; }
            return sum;""");
    }

    // ── Demo 3: nested branches ──

    static void demoNested() {
        var cfg = new SimpleCFG();

        cfg.addBlock(0, "entry")
            .add("a = 1")
            .add("b = 2")
            .add("if a goto left else right");

        cfg.addBlock(1, "left")
            .add("a = 10")
            .add("b = 20");

        cfg.addBlock(2, "right")
            .add("a = 30");

        cfg.addBlock(3, "merge1")
            .add("if b goto m2 else m3");

        cfg.addBlock(4, "m2")
            .add("a = a + b");

        cfg.addBlock(5, "m3")
            .add("c = a + b")
            .add("return c");

        cfg.addEdge(0, 1);
        cfg.addEdge(0, 2);
        cfg.addEdge(1, 3);
        cfg.addEdge(2, 3);
        cfg.addEdge(3, 4);
        cfg.addEdge(3, 5);
        cfg.addEdge(4, 5);

        runDemo(cfg, """
            if (a) { a=10; b=20; } else { a=30; }
            if (b) { a=a+b; }
            c=a+b; return c;""");
    }

    // ── Demo runner ──

    static void runDemo(SimpleCFG cfg, String sourceDesc) {
        System.out.println();
        System.out.println("Source: " + sourceDesc.strip().replace("\n", " "));
        System.out.println();

        // Show original CFG
        System.out.println("── Original CFG ──");
        for (var block : cfg.blocks) {
            System.out.printf("  %s:%n", block.label);
            for (String instr : block.instructions) {
                System.out.printf("    %s%n", instr);
            }
        }
        System.out.println();

        // Run dominator analysis
        var dom = new DominatorAnalysis(cfg);
        dom.analyze();

        // Show dominator tree
        System.out.println("── Dominator Tree ──");
        for (var block : cfg.blocks) {
            Integer idom = dom.getImmediateDominators().get(block.id);
            String parent = idom != null ? "B" + idom : "(entry)";
            System.out.printf("  B%d (%s) ← idom = %s%n", block.id, block.label, parent);
        }
        System.out.println();

        // Show dominance frontiers
        System.out.println("── Dominance Frontiers ──");
        for (var block : cfg.blocks) {
            Set<Integer> frontier = dom.getDominanceFrontier(block.id);
            if (!frontier.isEmpty()) {
                System.out.printf("  DF(B%d) = %s%n", block.id, formatBlockSet(frontier));
            }
        }
        System.out.println();

        // Run SSA transformation
        var ssa = new SSATransformer(cfg, dom);
        ssa.transform();

        // Show SSA form
        System.out.println("── SSA Form ──");
        ssa.printSSA();

        // Show phi functions summary
        var phis = ssa.getPhiFunctions();
        if (phis.isEmpty()) {
            System.out.println("  (no phi functions needed)");
        } else {
            System.out.println("  Phi functions inserted at:");
            for (var entry : phis.entrySet()) {
                System.out.printf("    B%d: %s%n", entry.getKey(), entry.getValue());
            }
        }
        System.out.println();
    }

    static String formatBlockSet(Set<Integer> ids) {
        var list = ids.stream().map(id -> "B" + id).toList();
        return list.toString();
    }
}
