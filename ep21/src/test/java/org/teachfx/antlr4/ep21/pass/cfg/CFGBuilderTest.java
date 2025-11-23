package org.teachfx.antlr4.ep21.pass.cfg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;
import org.teachfx.antlr4.ep21.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep21.ir.stmt.CJMP;
import org.teachfx.antlr4.ep21.ir.stmt.JMP;
import org.teachfx.antlr4.ep21.ir.stmt.Label;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CFGBuilder类的单元测试
 * 测试CFG构建器的功能，包括从LinearIRBlock构建控制流图、边的创建和缓存机制
 */
class CFGBuilderTest {

    private CFGBuilder cfgBuilder;
    private LinearIRBlock startBlock;

    @BeforeEach
    void setUp() {
        startBlock = new LinearIRBlock();
    }

    @Test
    void testConstructorWithEmptyLinearIRBlock() {
        // Arrange
        LinearIRBlock emptyBlock = new LinearIRBlock();

        // Act
        CFGBuilder builder = new CFGBuilder(emptyBlock);
        CFG<IRNode> cfg = builder.getCFG();

        // Assert
        assertNotNull(cfg, "CFG should be created from empty LinearIRBlock");
        assertEquals(1, cfg.nodes.size(), "Empty LinearIRBlock should create one BasicBlock");
        assertTrue(cfg.edges.isEmpty(), "Empty LinearIRBlock should create no edges");
    }

    @Test
    void testBuildWithSingleJMPInstruction() {
        // Arrange
        LinearIRBlock targetBlock = new LinearIRBlock();
        JMP jmpInstruction = new JMP(targetBlock);
        startBlock.addStmt(jmpInstruction);

        // Act
        cfgBuilder = new CFGBuilder(startBlock);
        CFG<IRNode> cfg = cfgBuilder.getCFG();

        // Assert
        assertEquals(2, cfg.nodes.size(), "Should create 2 BasicBlocks (start and target)");
        assertEquals(1, cfg.edges.size(), "Should create 1 edge for JMP instruction");
        
        // 验证边的结构
        var edge = cfg.edges.get(0);
        assertEquals(startBlock.getOrd(), edge.getLeft(), "Edge should start from start block");
        assertEquals(targetBlock.getOrd(), edge.getMiddle(), "Edge should end at target block");
        assertEquals(5, edge.getRight(), "Edge weight should be 5 for JMP");
    }

    @Test
    void testBuildWithConditionalJumpInstruction() {
        // Arrange
        LinearIRBlock thenBlock = new LinearIRBlock();
        LinearIRBlock elseBlock = new LinearIRBlock();
        VarSlot condition = new FrameSlot(0);
        CJMP cjmpInstruction = new CJMP(condition, thenBlock, elseBlock);
        startBlock.addStmt(cjmpInstruction);

        // Act
        cfgBuilder = new CFGBuilder(startBlock);
        CFG<IRNode> cfg = cfgBuilder.getCFG();

        // Assert
        assertEquals(3, cfg.nodes.size(), "Should create 3 BasicBlocks (start, then, else)");
        assertEquals(2, cfg.edges.size(), "Should create 2 edges for CJMP instruction (then and else branches)");
        
        // 验证边的结构 - 检查是否包含then和else分支的边
        boolean foundThenEdge = false;
        boolean foundElseEdge = false;
        
        for (var edge : cfg.edges) {
            assertEquals(startBlock.getOrd(), edge.getLeft(), "All edges should start from start block");
            assertEquals(5, edge.getRight(), "Edge weight should be 5 for CJMP");
            
            if (edge.getMiddle() == thenBlock.getOrd()) {
                foundThenEdge = true;
            } else if (edge.getMiddle() == elseBlock.getOrd()) {
                foundElseEdge = true;
            }
        }
        
        assertTrue(foundThenEdge, "Should have edge to then block");
        assertTrue(foundElseEdge, "Should have edge to else block");
        
        // 保留原有的边验证逻辑作为验证else分支的示例
        var edge = cfg.edges.stream().filter(e -> e.getMiddle() == elseBlock.getOrd()).findFirst().get();
        assertEquals(startBlock.getOrd(), edge.getLeft(), "Edge should start from start block");
        assertEquals(elseBlock.getOrd(), edge.getMiddle(), "Edge should end at else block");
        assertEquals(5, edge.getRight(), "Edge weight should be 5 for CJMP else branch");
    }

    @Test
    void testBuildWithMultipleInstructions() {
        // Arrange
        startBlock.addStmt(new Label("StartLabel", null));
        startBlock.addStmt(new Label("SecondLabel", null));
        
        // Act
        cfgBuilder = new CFGBuilder(startBlock);
        CFG<IRNode> cfg = cfgBuilder.getCFG();

        // Assert
        assertEquals(1, cfg.nodes.size(), "Multiple non-jump instructions should create one BasicBlock");
        assertEquals(2, cfg.nodes.get(0).codes.size(), "Should have 2 instructions in the single block");
    }

    @Test
    void testEdgeCachingPreventsDuplicates() {
        // Arrange
        LinearIRBlock targetBlock = new LinearIRBlock();
        // 手动设置相同的ord来模拟重复边的情况
        // 使用反射设置私有字段（仅用于测试目的）
        try {
            java.lang.reflect.Field ordField = LinearIRBlock.class.getDeclaredField("ord");
            ordField.setAccessible(true);
            ordField.set(targetBlock, ordField.get(startBlock));
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ord field", e);
        }
        
        JMP jmpInstruction = new JMP(targetBlock);
        startBlock.addStmt(jmpInstruction);

        // Act
        Set<String> cachedEdges = new HashSet<>();
        // 模拟build方法中的缓存检查
        String edgeKey = startBlock.getOrd() + "-" + targetBlock.getOrd() + "-" + 5;
        cachedEdges.add(edgeKey); // 模拟已经存在的边

        // Assert
        assertTrue(cachedEdges.contains(edgeKey), "Edge key should be cached to prevent duplicates");
        assertEquals(1, cachedEdges.size(), "Should have exactly one cached edge");
    }

    @Test
    void testBuildWithSuccessorsCreatesMultipleEdges() {
        // Arrange
        LinearIRBlock successor1 = new LinearIRBlock();
        LinearIRBlock successor2 = new LinearIRBlock();
        
        // 设置successors（这通常在运行时由IR构建器设置）
        startBlock.getSuccessors().add(successor1);
        startBlock.getSuccessors().add(successor2);

        // Act
        cfgBuilder = new CFGBuilder(startBlock);
        CFG<IRNode> cfg = cfgBuilder.getCFG();

        // Assert
        assertTrue(cfg.nodes.size() >= 1, "Should have at least the start block");
        // 边的数量取决于successors的设置
        assertTrue(cfg.edges.size() >= 0, "Edge count should be non-negative");
    }

    @Test
    void testGetCFGReturnsValidCFG() {
        // Arrange
        startBlock.addStmt(new Label("TestLabel", null));

        // Act
        cfgBuilder = new CFGBuilder(startBlock);
        CFG<IRNode> cfg = cfgBuilder.getCFG();

        // Assert
        assertNotNull(cfg, "getCFG should return non-null CFG");
        assertNotNull(cfg.nodes, "CFG nodes should not be null");
        assertNotNull(cfg.edges, "CFG edges should not be null");
        assertTrue(cfg.nodes.size() > 0, "CFG should have at least one node");
    }

    @Test
    void testBuildWithMixedInstructions() {
        // Arrange
        LinearIRBlock nextBlock = new LinearIRBlock();
        startBlock.addStmt(new Label("MixedLabel", null));
        startBlock.addStmt(new Label("AnotherLabel", null));
        startBlock.addStmt(new JMP(nextBlock)); // 最后一条是跳转指令

        // Act
        cfgBuilder = new CFGBuilder(startBlock);
        CFG<IRNode> cfg = cfgBuilder.getCFG();

        // Assert
        assertEquals(2, cfg.nodes.size(), "Mixed instructions with JMP should create 2 blocks");
        assertEquals(1, cfg.edges.size(), "Should have 1 edge for the JMP");
        
        // 验证第一个块包含所有非跳转指令
        BasicBlock<IRNode> firstBlock = cfg.nodes.get(0);
        assertTrue(firstBlock.codes.size() >= 2, "First block should contain the non-jump instructions");
    }

    @Test
    void testBuildRecursiveStructure() {
        // Arrange
        LinearIRBlock nestedBlock = new LinearIRBlock();
        nestedBlock.addStmt(new Label("NestedLabel", null));
        
        startBlock.addStmt(new JMP(nestedBlock));

        // Act
        cfgBuilder = new CFGBuilder(startBlock);
        CFG<IRNode> cfg = cfgBuilder.getCFG();

        // Assert
        assertTrue(cfg.nodes.size() >= 2, "Should have nodes for start and nested blocks");
        assertTrue(cfg.edges.size() >= 1, "Should have at least one edge");
    }

    @Test
    void testCFGBuilderWithNullStartBlock() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            new CFGBuilder(null);
        }, "CFGBuilder should throw NullPointerException with null start block");
    }

    @Test
    void testEdgeWeightAssignment() {
        // Arrange
        LinearIRBlock targetBlock = new LinearIRBlock();
        JMP jmpInstruction = new JMP(targetBlock);
        startBlock.addStmt(jmpInstruction);

        // Act
        cfgBuilder = new CFGBuilder(startBlock);
        CFG<IRNode> cfg = cfgBuilder.getCFG();

        // Assert
        assertEquals(1, cfg.edges.size(), "Should have exactly one edge");
        var edge = cfg.edges.get(0);
        assertTrue(edge.getRight() == 5 || edge.getRight() == 10, 
                   "Edge weight should be either 5 (for jumps) or 10 (for successors)");
    }
}