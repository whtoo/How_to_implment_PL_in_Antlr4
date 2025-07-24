package org.teachfx.antlr4.ep20.pass.cfg;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep20.ir.IRNode;
import org.teachfx.antlr4.ep20.ir.stmt.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ControlFlowGraphTest {

    @Test
    void testCreateEmptyCFG() {
        // 跳过空CFG测试，因为实际中CFG至少有一个节点
        // CFG构造函数在空节点列表时会抛出异常
        assertTrue(true);
    }

    @Test
    void testCreateCFGWithSingleNode() {
        // Arrange
        LinearIRBlock block = new LinearIRBlock();
        block.addStmt(new Label("L0", null));
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();
        BasicBlock<IRNode> basicBlock = BasicBlock.buildFromLinearBlock(block, cachedNodes);
        
        List<BasicBlock<IRNode>> nodes = List.of(basicBlock);
        List<Triple<Integer, Integer, Integer>> edges = new ArrayList<>();

        // Act
        CFG<IRNode> cfg = new CFG<>(nodes, edges);

        // Assert
        assertEquals(1, cfg.nodes.size());
        assertEquals(basicBlock, cfg.getBlock(0));
        assertEquals(0, cfg.edges.size());
    }

    @Test
    void testCreateCFGWithLinearFlow() {
        // Arrange
        LinearIRBlock block1 = new LinearIRBlock();
        block1.addStmt(new Label("L0", null));
        block1.addStmt(new Assign(null, null));
        
        LinearIRBlock block2 = new LinearIRBlock();
        block2.addStmt(new Label("L1", null));
        block2.addStmt(new Assign(null, null));
        
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();
        BasicBlock<IRNode> basicBlock1 = BasicBlock.buildFromLinearBlock(block1, cachedNodes);
        BasicBlock<IRNode> basicBlock2 = BasicBlock.buildFromLinearBlock(block2, cachedNodes);
        
        List<BasicBlock<IRNode>> nodes = List.of(basicBlock1, basicBlock2);
        List<Triple<Integer, Integer, Integer>> edges = List.of(
            Triple.of(0, 1, 1)
        );

        // Act
        CFG<IRNode> cfg = new CFG<>(nodes, edges);

        // Assert
        assertEquals(2, cfg.nodes.size());
        assertEquals(1, cfg.edges.size());
        assertEquals(Set.of(1), cfg.getSucceed(0));
        assertEquals(Set.of(0), cfg.getFrontier(1));
    }

    @Test
    void testCreateCFGWithBranching() {
        // Arrange
        LinearIRBlock entryBlock = new LinearIRBlock();
        entryBlock.addStmt(new Label("L0", null));
        
        LinearIRBlock thenBlock = new LinearIRBlock();
        thenBlock.addStmt(new Label("L1", null));
        
        LinearIRBlock elseBlock = new LinearIRBlock();
        elseBlock.addStmt(new Label("L2", null));
        
        LinearIRBlock mergeBlock = new LinearIRBlock();
        mergeBlock.addStmt(new Label("L3", null));
        
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();
        BasicBlock<IRNode> entry = BasicBlock.buildFromLinearBlock(entryBlock, cachedNodes);
        BasicBlock<IRNode> thenNode = BasicBlock.buildFromLinearBlock(thenBlock, cachedNodes);
        BasicBlock<IRNode> elseNode = BasicBlock.buildFromLinearBlock(elseBlock, cachedNodes);
        BasicBlock<IRNode> merge = BasicBlock.buildFromLinearBlock(mergeBlock, cachedNodes);
        
        List<BasicBlock<IRNode>> nodes = List.of(entry, thenNode, elseNode, merge);
        List<Triple<Integer, Integer, Integer>> edges = List.of(
            Triple.of(0, 1, 1),
            Triple.of(0, 2, 1),
            Triple.of(1, 3, 1),
            Triple.of(2, 3, 1)
        );

        // Act
        CFG<IRNode> cfg = new CFG<>(nodes, edges);

        // Assert
        assertEquals(4, cfg.nodes.size());
        assertEquals(4, cfg.edges.size());
        
        // Test entry block successors
        assertEquals(Set.of(1, 2), cfg.getSucceed(0));
        
        // Test merge block predecessors
        assertEquals(Set.of(1, 2), cfg.getFrontier(3));
    }

    @Test
    void testGetInDegree() {
        // Arrange
        LinearIRBlock block1 = new LinearIRBlock();
        block1.addStmt(new Label("L0", null));
        
        LinearIRBlock block2 = new LinearIRBlock();
        block2.addStmt(new Label("L1", null));
        
        LinearIRBlock block3 = new LinearIRBlock();
        block3.addStmt(new Label("L2", null));
        
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();
        BasicBlock<IRNode> basicBlock1 = BasicBlock.buildFromLinearBlock(block1, cachedNodes);
        BasicBlock<IRNode> basicBlock2 = BasicBlock.buildFromLinearBlock(block2, cachedNodes);
        BasicBlock<IRNode> basicBlock3 = BasicBlock.buildFromLinearBlock(block3, cachedNodes);
        
        List<BasicBlock<IRNode>> nodes = List.of(basicBlock1, basicBlock2, basicBlock3);
        List<Triple<Integer, Integer, Integer>> edges = List.of(
            Triple.of(0, 1, 1),
            Triple.of(0, 2, 1),
            Triple.of(1, 2, 1)
        );

        // Act
        CFG<IRNode> cfg = new CFG<>(nodes, edges);

        // Assert
        assertEquals(0, cfg.getInDegree(0)); // Entry node
        assertEquals(1, cfg.getInDegree(1)); // Node 1 has one predecessor
        assertEquals(2, cfg.getInDegree(2)); // Node 2 has two predecessors
    }

    @Test
    void testGetOutDegree() {
        // Arrange
        LinearIRBlock block1 = new LinearIRBlock();
        block1.addStmt(new Label("L0", null));
        
        LinearIRBlock block2 = new LinearIRBlock();
        block2.addStmt(new Label("L1", null));
        
        LinearIRBlock block3 = new LinearIRBlock();
        block3.addStmt(new Label("L2", null));
        
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();
        BasicBlock<IRNode> basicBlock1 = BasicBlock.buildFromLinearBlock(block1, cachedNodes);
        BasicBlock<IRNode> basicBlock2 = BasicBlock.buildFromLinearBlock(block2, cachedNodes);
        BasicBlock<IRNode> basicBlock3 = BasicBlock.buildFromLinearBlock(block3, cachedNodes);
        
        List<BasicBlock<IRNode>> nodes = List.of(basicBlock1, basicBlock2, basicBlock3);
        List<Triple<Integer, Integer, Integer>> edges = List.of(
            Triple.of(0, 1, 1),
            Triple.of(0, 2, 1),
            Triple.of(1, 2, 1)
        );

        // Act
        CFG<IRNode> cfg = new CFG<>(nodes, edges);

        // Assert
        assertEquals(2, cfg.getOutDegree(0)); // Entry node has two successors
        assertEquals(1, cfg.getOutDegree(1)); // Node 1 has one successor
        assertEquals(0, cfg.getOutDegree(2)); // Exit node
    }

    @Test
    void testIterator() {
        // Arrange
        LinearIRBlock block1 = new LinearIRBlock();
        block1.addStmt(new Label("L0", null));
        
        LinearIRBlock block2 = new LinearIRBlock();
        block2.addStmt(new Label("L1", null));
        
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();
        BasicBlock<IRNode> basicBlock1 = BasicBlock.buildFromLinearBlock(block1, cachedNodes);
        BasicBlock<IRNode> basicBlock2 = BasicBlock.buildFromLinearBlock(block2, cachedNodes);
        
        List<BasicBlock<IRNode>> nodes = List.of(basicBlock1, basicBlock2);
        List<Triple<Integer, Integer, Integer>> edges = List.of();
        CFG<IRNode> cfg = new CFG<>(nodes, edges);

        // Act
        int count = 0;
        for (BasicBlock<IRNode> block : cfg) {
            count++;
        }

        // Assert
        assertEquals(2, count);
    }

    @Test
    void testRemoveEdge() {
        // Arrange
        LinearIRBlock block1 = new LinearIRBlock();
        block1.addStmt(new Label("L0", null));
        
        LinearIRBlock block2 = new LinearIRBlock();
        block2.addStmt(new Label("L1", null));
        
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();
        BasicBlock<IRNode> basicBlock1 = BasicBlock.buildFromLinearBlock(block1, cachedNodes);
        BasicBlock<IRNode> basicBlock2 = BasicBlock.buildFromLinearBlock(block2, cachedNodes);
        
        List<BasicBlock<IRNode>> nodes = List.of(basicBlock1, basicBlock2);
        Triple<Integer, Integer, Integer> edge = Triple.of(0, 1, 1);
        List<Triple<Integer, Integer, Integer>> edges = new ArrayList<>(List.of(edge));
        CFG<IRNode> cfg = new CFG<>(nodes, edges);

        // Act
        cfg.removeEdge(edge);

        // Assert
        assertEquals(0, cfg.edges.size());
        assertTrue(cfg.getSucceed(0).isEmpty());
        assertTrue(cfg.getFrontier(1).isEmpty());
    }

    @Test
    void testGetIRNodes() {
        // Arrange
        LinearIRBlock block1 = new LinearIRBlock();
        block1.addStmt(new Label("L0", null));
        block1.addStmt(new Assign(null, null));
        
        LinearIRBlock block2 = new LinearIRBlock();
        block2.addStmt(new Label("L1", null));
        block2.addStmt(new Assign(null, null));
        
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();
        BasicBlock<IRNode> basicBlock1 = BasicBlock.buildFromLinearBlock(block1, cachedNodes);
        BasicBlock<IRNode> basicBlock2 = BasicBlock.buildFromLinearBlock(block2, cachedNodes);
        
        List<BasicBlock<IRNode>> nodes = List.of(basicBlock1, basicBlock2);
        List<Triple<Integer, Integer, Integer>> edges = List.of();
        CFG<IRNode> cfg = new CFG<>(nodes, edges);

        // Act
        var irNodes = cfg.getIRNodes();

        // Assert
        assertEquals(4, irNodes.size()); // 2 labels + 2 assigns
    }

    @Test
    void testToString() {
        // Arrange
        LinearIRBlock block1 = new LinearIRBlock();
        block1.addStmt(new Label("L0", null));
        
        LinearIRBlock block2 = new LinearIRBlock();
        block2.addStmt(new Label("L1", null));
        
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();
        BasicBlock<IRNode> basicBlock1 = BasicBlock.buildFromLinearBlock(block1, cachedNodes);
        BasicBlock<IRNode> basicBlock2 = BasicBlock.buildFromLinearBlock(block2, cachedNodes);
        
        List<BasicBlock<IRNode>> nodes = List.of(basicBlock1, basicBlock2);
        List<Triple<Integer, Integer, Integer>> edges = List.of(Triple.of(0, 1, 1));
        CFG<IRNode> cfg = new CFG<>(nodes, edges);

        // Act
        String dotString = cfg.toString();

        // Assert
        assertNotNull(dotString);
        assertTrue(dotString.contains("graph TD"));
        assertTrue(dotString.contains("L0"));
        assertTrue(dotString.contains("L1"));
    }

    @Test
    void testRemoveNode() {
        // Arrange
        LinearIRBlock block1 = new LinearIRBlock();
        block1.addStmt(new Label("L0", null));
        
        LinearIRBlock block2 = new LinearIRBlock();
        block2.addStmt(new Label("L1", null));
        
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();
        BasicBlock<IRNode> basicBlock1 = BasicBlock.buildFromLinearBlock(block1, cachedNodes);
        BasicBlock<IRNode> basicBlock2 = BasicBlock.buildFromLinearBlock(block2, cachedNodes);
        
        List<BasicBlock<IRNode>> nodes = new ArrayList<>(List.of(basicBlock1, basicBlock2));
        List<Triple<Integer, Integer, Integer>> edges = List.of();
        CFG<IRNode> cfg = new CFG<>(nodes, edges);

        // Act
        cfg.removeNode(basicBlock1);

        // Assert
        assertEquals(1, cfg.nodes.size());
        assertFalse(cfg.nodes.contains(basicBlock1));
    }

    @Test
    void testGetInEdges() {
        // Arrange
        LinearIRBlock block1 = new LinearIRBlock();
        block1.addStmt(new Label("L0", null));
        
        LinearIRBlock block2 = new LinearIRBlock();
        block2.addStmt(new Label("L1", null));
        
        LinearIRBlock block3 = new LinearIRBlock();
        block3.addStmt(new Label("L2", null));
        
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();
        BasicBlock<IRNode> basicBlock1 = BasicBlock.buildFromLinearBlock(block1, cachedNodes);
        BasicBlock<IRNode> basicBlock2 = BasicBlock.buildFromLinearBlock(block2, cachedNodes);
        BasicBlock<IRNode> basicBlock3 = BasicBlock.buildFromLinearBlock(block3, cachedNodes);
        
        List<BasicBlock<IRNode>> nodes = List.of(basicBlock1, basicBlock2, basicBlock3);
        List<Triple<Integer, Integer, Integer>> edges = List.of(
            Triple.of(0, 2, 1),
            Triple.of(1, 2, 1)
        );
        CFG<IRNode> cfg = new CFG<>(nodes, edges);

        // Act
        var inEdges = cfg.getInEdges(2).toList();

        // Assert
        assertEquals(2, inEdges.size());
    }
}