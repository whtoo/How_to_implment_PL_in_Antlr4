package org.teachfx.antlr4.ep13.test;

import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep13.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EP13 tests: SSA construction (dominators, phi insertion, variable renaming).
 */
public class SSATest {

    @Test
    void testDominatorAnalysisOnSimpleIfElse() {
        var cfg = buildIfElseCFG();
        var dom = new DominatorAnalysis(cfg);
        dom.analyze();

        // Entry (B0) dominates all
        assertTrue(dom.dominates(0, 1));
        assertTrue(dom.dominates(0, 2));
        assertTrue(dom.dominates(0, 3));

        // B1 and B2 don't dominate each other
        assertFalse(dom.dominates(1, 2));
        assertFalse(dom.dominates(2, 1));
    }

    @Test
    void testDominanceFrontierForIfElse() {
        var cfg = buildIfElseCFG();
        var dom = new DominatorAnalysis(cfg);
        dom.analyze();

        // B1 and B2 should have B3 in their DF
        assertTrue(dom.getDominanceFrontier(1).contains(3));
        assertTrue(dom.getDominanceFrontier(2).contains(3));
    }

    @Test
    void testPhiInsertionAtMergePoint() {
        var cfg = buildIfElseCFG();
        var dom = new DominatorAnalysis(cfg);
        dom.analyze();
        var ssa = new SSATransformer(cfg, dom);
        ssa.transform();

        // B3 (merge) should have a phi for x
        var phis = ssa.getPhiFunctions();
        assertTrue(phis.containsKey(3), "Phi should be inserted at merge block");
    }

    @Test
    void testVariableRenaming() {
        var cfg = buildIfElseCFG();
        var dom = new DominatorAnalysis(cfg);
        dom.analyze();
        var ssa = new SSATransformer(cfg, dom);
        ssa.transform();

        var ssaInstrs = ssa.getSSAInstructions();
        // Entry block: x should be renamed to x_1
        var entryInstrs = ssaInstrs.get(0);
        assertNotNull(entryInstrs);
        assertTrue(entryInstrs.get(0).contains("x_1"), "First x definition should be x_1");
    }

    @Test
    void testLoopBackEdgeCreatesPhi() {
        var cfg = new SimpleCFG();
        cfg.addBlock(0, "entry").add("i = 0").add("goto loop");
        cfg.addBlock(1, "loop").add("cond = i < 10").add("if cond goto body else exit");
        cfg.addBlock(2, "body").add("i = i + 1").add("goto loop");
        cfg.addBlock(3, "exit").add("return i");
        cfg.addEdge(0, 1);
        cfg.addEdge(1, 2);
        cfg.addEdge(1, 3);
        cfg.addEdge(2, 1); // back-edge

        var dom = new DominatorAnalysis(cfg);
        dom.analyze();
        var ssa = new SSATransformer(cfg, dom);
        ssa.transform();

        // Loop header (B1) should have phi for i
        var phis = ssa.getPhiFunctions();
        assertTrue(phis.containsKey(1), "Loop header should have phi functions");
    }

    // Helper: build if/else CFG
    private SimpleCFG buildIfElseCFG() {
        var cfg = new SimpleCFG();
        cfg.addBlock(0, "entry").add("x = 1").add("if cond goto then else else");
        cfg.addBlock(1, "then").add("x = 10");
        cfg.addBlock(2, "else").add("x = 20");
        cfg.addBlock(3, "merge").add("y = x + 1").add("return y");
        cfg.addEdge(0, 1);
        cfg.addEdge(0, 2);
        cfg.addEdge(1, 3);
        cfg.addEdge(2, 3);
        return cfg;
    }

    @Test
    void testDominationSelfDominates() {
        var cfg = buildIfElseCFG();
        var dom = new DominatorAnalysis(cfg);
        dom.analyze();

        // Every block dominates itself
        assertTrue(dom.dominates(0, 0));
        assertTrue(dom.dominates(1, 1));
        assertTrue(dom.dominates(2, 2));
        assertTrue(dom.dominates(3, 3));
    }

    @Test
    void testImmediateDominatorForIfElse() {
        var cfg = buildIfElseCFG();
        var dom = new DominatorAnalysis(cfg);
        dom.analyze();

        // idom of B1 (then) and B2 (else) should be B0 (entry)
        assertEquals(0, dom.getImmediateDominators().get(1));
        assertEquals(0, dom.getImmediateDominators().get(2));
    }

    @Test
    void testSSATransformPreservesBlockCount() {
        var cfg = buildIfElseCFG();
        var dom = new DominatorAnalysis(cfg);
        dom.analyze();
        var ssa = new SSATransformer(cfg, dom);
        ssa.transform();

        var ssaInstrs = ssa.getSSAInstructions();
        assertEquals(4, ssaInstrs.size(), "SSA should preserve block count");
    }

    @Test
    void testStraightLineCFGNoPhi() {
        var cfg = new SimpleCFG();
        cfg.addBlock(0, "entry").add("x = 1").add("y = 2").add("z = x + y").add("return z");
        // single block, no edges needed

        var dom = new DominatorAnalysis(cfg);
        dom.analyze();
        var ssa = new SSATransformer(cfg, dom);
        ssa.transform();

        // Straight-line code should have no phi nodes
        var phis = ssa.getPhiFunctions();
        assertTrue(phis.isEmpty() || !phis.containsKey(0),
            "Straight-line CFG should have no phi nodes");
    }

    @Test
    void testDominatorTreeOnIfElse() {
        var cfg = buildIfElseCFG();
        var dom = new DominatorAnalysis(cfg);
        dom.analyze();

        // B0 (entry) dominates B3 (merge)
        assertTrue(dom.dominates(0, 3));
        // B1 (then) does NOT dominate B3 (merge)
        assertFalse(dom.dominates(1, 3));
    }

    @Test
    void testDominanceFrontierForSingleBlock() {
        var cfg = new SimpleCFG();
        cfg.addBlock(0, "entry").add("x = 1").add("return x");

        var dom = new DominatorAnalysis(cfg);
        dom.analyze();

        assertNotNull(dom.getDominanceFrontier(0));
    }

    @Test
    void testDiamondCFGSsaPhiInMerge() {
        // Diamond if-else: a define in both branches → phi in merge
        var cfg = buildIfElseCFG();
        var dom = new DominatorAnalysis(cfg);
        dom.analyze();
        var ssa = new SSATransformer(cfg, dom);
        ssa.transform();

        var phis = ssa.getPhiFunctions();
        // Merge block should have phi for x (defined in both branches)
        assertTrue(phis.containsKey(3), "Merge block (B3) should have phi");
    }
}
