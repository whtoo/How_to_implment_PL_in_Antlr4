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

    @Test
    void testBasicBlockWithInstructions() {
        List<IRNode> instrs = new ArrayList<>();
        BasicBlock block = new BasicBlock(7, instrs);
        assertEquals(7, block.id);
        assertEquals(0, block.instructions.size());
        assertNotNull(block.predecessors);
        assertNotNull(block.successors);
    }

    @Test
    void testLinearBlockChain() {
        BasicBlock b0 = new BasicBlock(0, new ArrayList<>());
        BasicBlock b1 = new BasicBlock(1, new ArrayList<>());
        BasicBlock b2 = new BasicBlock(2, new ArrayList<>());

        b0.successors.add(b1);
        b1.predecessors.add(b0);
        b1.successors.add(b2);
        b2.predecessors.add(b1);

        assertEquals(b1, b0.successors.get(0));
        assertEquals(b2, b1.successors.get(0));
        assertEquals(b0, b1.predecessors.get(0));
        assertEquals(b1, b2.predecessors.get(0));
    }

    @Test
    void testIfElseCFGShape() {
        CFG cfg = CFGBuilder.demoIfElse();
        // Should have at least 4 blocks: entry, then, else, merge
        assertTrue(cfg.blocks.size() >= 4, "If-else CFG should have at least 4 blocks");
    }

    @Test
    void testCFGWithMultipleSuccessors() {
        // Entry block should branch to two successors (then/else)
        BasicBlock entry = new BasicBlock(0, new ArrayList<>());
        BasicBlock thenBlock = new BasicBlock(1, new ArrayList<>());
        BasicBlock elseBlock = new BasicBlock(2, new ArrayList<>());
        BasicBlock merge = new BasicBlock(3, new ArrayList<>());

        entry.successors.add(thenBlock);
        entry.successors.add(elseBlock);
        thenBlock.predecessors.add(entry);
        elseBlock.predecessors.add(entry);
        thenBlock.successors.add(merge);
        elseBlock.successors.add(merge);
        merge.predecessors.add(thenBlock);
        merge.predecessors.add(elseBlock);

        // Entry has two successors
        assertEquals(2, entry.successors.size());
        // Merge has two predecessors
        assertEquals(2, merge.predecessors.size());
    }

    @Test
    void testEmptyCFG() {
        CFG cfg = new CFG(new ArrayList<>());
        assertEquals(0, cfg.blocks.size());
        assertTrue(cfg.entryLabel.isEmpty());
    }

    @Test
    void testCFGBlocksAreAccessible() {
        CFG cfg = CFGBuilder.demoIfElse();
        assertNotNull(cfg.blocks);
        for (var block : cfg.blocks) {
            assertNotNull(block);
            assertTrue(block.id >= 0);
        }
    }

    @Test
    void testCFGBuilderDoesNotReturnNull() {
        assertNotNull(CFGBuilder.demoIfElse());
    }

    @Test
    void testLoopCFGWithBackEdge() {
        // Simulate: while (cond) { body } exit
        BasicBlock header = new BasicBlock(0, new ArrayList<>());
        BasicBlock body = new BasicBlock(1, new ArrayList<>());
        BasicBlock exit = new BasicBlock(2, new ArrayList<>());

        header.successors.add(body);
        header.successors.add(exit);
        body.predecessors.add(header);
        body.successors.add(header); // back-edge
        exit.predecessors.add(header);

        // Header has two successors: body and exit
        assertEquals(2, header.successors.size());
        // Body has a back-edge to header
        assertTrue(body.successors.contains(header));
        // Exit has header as predecessor
        assertTrue(exit.predecessors.contains(header));
    }

    @Test
    void testNestedIfElseCFG() {
        // Simulate: if (a) { if (b) { T1 } else { E1 } } else { E2 } M
        BasicBlock b0 = new BasicBlock(0, new ArrayList<>()); // if a
        BasicBlock b1 = new BasicBlock(1, new ArrayList<>()); // if b (inner)
        BasicBlock b2 = new BasicBlock(2, new ArrayList<>()); // T1
        BasicBlock b3 = new BasicBlock(3, new ArrayList<>()); // E1
        BasicBlock b4 = new BasicBlock(4, new ArrayList<>()); // E2
        BasicBlock b5 = new BasicBlock(5, new ArrayList<>()); // M

        b0.successors.add(b1); b0.successors.add(b4);
        b1.predecessors.add(b0); b4.predecessors.add(b0);
        b1.successors.add(b2); b1.successors.add(b3);
        b2.predecessors.add(b1); b3.predecessors.add(b1);
        b2.successors.add(b5); b3.successors.add(b5); b4.successors.add(b5);
        b5.predecessors.add(b2); b5.predecessors.add(b3); b5.predecessors.add(b4);

        // Merge has 3 predecessors (T1, E1, E2)
        assertEquals(3, b5.predecessors.size());
    }
}
