package org.teachfx.antlr4.ep09.test;

import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep09.ir.IRNode;
import org.teachfx.antlr4.ep09.pass.cfg.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EP09 tests: Control Flow Graph construction.
 */
public class CFGTest {

    @Test
    void testBasicBlockCreation() {
        List<IRNode> instrs = new ArrayList<>();
        BasicBlock block = new BasicBlock(0, instrs);

        assertEquals(0, block.id);
        assertEquals(0, block.instructions.size());
    }

    @Test
    void testCFGCreation() {
        List<BasicBlock> blocks = new ArrayList<>();
        blocks.add(new BasicBlock(0, new ArrayList<>()));
        blocks.add(new BasicBlock(1, new ArrayList<>()));
        CFG cfg = new CFG(blocks);

        assertEquals(2, cfg.blocks.size());
    }

    @Test
    void testDemoIfElseCFG() {
        CFG cfg = CFGBuilder.demoIfElse();
        assertNotNull(cfg);
        assertFalse(cfg.blocks.isEmpty());
        assertNotNull(cfg.entryLabel);
    }

    @Test
    void testBlockConnections() {
        BasicBlock b0 = new BasicBlock(0, new ArrayList<>());
        BasicBlock b1 = new BasicBlock(1, new ArrayList<>());
        BasicBlock b2 = new BasicBlock(2, new ArrayList<>());

        b0.successors.add(b1);
        b0.successors.add(b2);
        b1.predecessors.add(b0);
        b2.predecessors.add(b0);

        assertEquals(2, b0.successors.size());
        assertEquals(1, b1.predecessors.size());
        assertEquals(1, b2.predecessors.size());
    }

    @Test
    void testEntryLabelSet() {
        CFG cfg = CFGBuilder.demoIfElse();
        assertFalse(cfg.entryLabel.isEmpty());
    }
}
