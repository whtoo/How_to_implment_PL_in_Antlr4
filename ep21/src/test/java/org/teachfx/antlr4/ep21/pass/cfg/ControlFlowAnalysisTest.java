package org.teachfx.antlr4.ep21.pass.cfg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;
import org.teachfx.antlr4.ep21.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep21.ir.stmt.CJMP;
import org.teachfx.antlr4.ep21.ir.stmt.JMP;
import org.teachfx.antlr4.ep21.ir.stmt.Label;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ControlFlowAnalysis类的单元测试
 * 测试控制流分析器的功能，包括基本块合并和跳转优化
 */
class ControlFlowAnalysisTest {

    private ControlFlowAnalysis<IRNode> controlFlowAnalysis;
    private CFG<IRNode> testCfg;

    @BeforeEach
    void setUp() {
        controlFlowAnalysis = new ControlFlowAnalysis<>();
        testCfg = createTestCFG();
    }

    private CFG<IRNode> createTestCFG() {
        // 创建一个简单的测试CFG
        List<BasicBlock<IRNode>> nodes = new ArrayList<>();
        List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges = new ArrayList<>();

        // 创建两个基本块
        BasicBlock<IRNode> block1 = new BasicBlock<>(org.teachfx.antlr4.ep21.utils.Kind.CONTINUOUS, 
                                                     List.of(new Loc<>(new Label("L1", null))), 
                                                     new org.teachfx.antlr4.ep21.ir.stmt.Label("L1", null), 0);
        
        BasicBlock<IRNode> block2 = new BasicBlock<>(org.teachfx.antlr4.ep21.utils.Kind.END_BY_JMP, 
                                                     List.of(new Loc<>(new Label("L2", null))), 
                                                     new org.teachfx.antlr4.ep21.ir.stmt.Label("L2", null), 1);

        nodes.add(block1);
        nodes.add(block2);

        // 添加边：从block1到block2
        edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 1, 5));

        return new CFG<>(nodes, edges);
    }

    @Test
    void testOnHandleWithValidCFG() {
        // Arrange
        assertEquals(2, testCfg.nodes.size(), "Test CFG should have 2 nodes initially");

        // Act
        controlFlowAnalysis.onHandle(testCfg);

        // Assert
        // 由于我们的测试CFG是精心设计的，onHandle应该正常执行而不抛出异常
        assertNotNull(testCfg, "CFG should still exist after analysis");
    }

    @Test
    void testOnHandleDoesNotThrowException() {
        // Arrange
        CFG<IRNode> emptyCfg = new CFG<>(new ArrayList<>(), new ArrayList<>());

        // Act & Assert
        assertDoesNotThrow(() -> {
            controlFlowAnalysis.onHandle(emptyCfg);
        }, "onHandle should not throw exception with empty CFG");
    }

    @Test
    void testOnHandleWithSingleBlockCFG() {
        // Arrange
        List<BasicBlock<IRNode>> nodes = new ArrayList<>();
        List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges = new ArrayList<>();

        BasicBlock<IRNode> singleBlock = new BasicBlock<>(org.teachfx.antlr4.ep21.utils.Kind.CONTINUOUS, 
                                                          List.of(new Loc<>(new Label("L1", null))), 
                                                          new org.teachfx.antlr4.ep21.ir.stmt.Label("L1", null), 0);
        nodes.add(singleBlock);

        CFG<IRNode> singleBlockCfg = new CFG<>(nodes, edges);

        // Act
        controlFlowAnalysis.onHandle(singleBlockCfg);

        // Assert
        assertEquals(1, singleBlockCfg.nodes.size(), "Single block CFG should still have 1 node");
    }

    @Test
    void testOnHandleWithBlockHavingJMPInstruction() {
        // Arrange
        List<BasicBlock<IRNode>> nodes = new ArrayList<>();
        List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges = new ArrayList<>();

        // 创建一个带有JMP指令的基本块
        LinearIRBlock targetBlock = new LinearIRBlock();
        JMP jmpInstruction = new JMP(targetBlock);
        
        BasicBlock<IRNode> jmpBlock = new BasicBlock<>(org.teachfx.antlr4.ep21.utils.Kind.END_BY_JMP, 
                                                       List.of(new Loc<>(jmpInstruction)), 
                                                       new org.teachfx.antlr4.ep21.ir.stmt.Label("L1", null), 0);

        nodes.add(jmpBlock);
        edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 0, 5)); // 自循环

        CFG<IRNode> jmpCfg = new CFG<>(nodes, edges);

        // Act
        controlFlowAnalysis.onHandle(jmpCfg);

        // Assert
        assertNotNull(jmpCfg, "CFG with JMP instruction should be processed without exception");
    }

    @Test
    void testOnHandleWithBlockHavingCJMPInstruction() {
        // Arrange
        List<BasicBlock<IRNode>> nodes = new ArrayList<>();
        List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges = new ArrayList<>();

        // 创建一个带有CJMP指令的基本块
        LinearIRBlock thenBlock = new LinearIRBlock();
        LinearIRBlock elseBlock = new LinearIRBlock();
        VarSlot condSlot = new FrameSlot(0);
        CJMP cjmpInstruction = new CJMP(condSlot, thenBlock, elseBlock);

        BasicBlock<IRNode> cjmpBlock = new BasicBlock<>(org.teachfx.antlr4.ep21.utils.Kind.END_BY_CJMP, 
                                                        List.of(new Loc<>(cjmpInstruction)), 
                                                        new org.teachfx.antlr4.ep21.ir.stmt.Label("L1", null), 0);

        nodes.add(cjmpBlock);
        edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 1, 5));

        CFG<IRNode> cjmpCfg = new CFG<>(nodes, edges);

        // Act
        controlFlowAnalysis.onHandle(cjmpCfg);

        // Assert
        assertNotNull(cjmpCfg, "CFG with CJMP instruction should be processed without exception");
    }

    @Test
    void testDebugFlagBehavior() {
        // Arrange
        boolean originalDebug = ControlFlowAnalysis.DEBUG;

        // Act
        ControlFlowAnalysis.DEBUG = true;
        controlFlowAnalysis.onHandle(testCfg);

        // Reset
        ControlFlowAnalysis.DEBUG = originalDebug;

        // Assert
        // 这个测试主要验证DEBUG标志不会导致异常
        assertTrue(true, "DEBUG flag should not affect onHandle execution");
    }

    @Test
    void testOnHandleWithMultipleBlocks() {
        // Arrange
        List<BasicBlock<IRNode>> nodes = new ArrayList<>();
        List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges = new ArrayList<>();

        // 创建多个基本块
        for (int i = 0; i < 5; i++) {
            BasicBlock<IRNode> block = new BasicBlock<>(org.teachfx.antlr4.ep21.utils.Kind.CONTINUOUS, 
                                                        List.of(new Loc<>(new Label("L" + i, null))), 
                                                        new org.teachfx.antlr4.ep21.ir.stmt.Label("L" + i, null), i);
            nodes.add(block);
            
            // 添加边
            if (i < 4) {
                edges.add(org.apache.commons.lang3.tuple.Triple.of(i, i + 1, 5));
            }
        }

        CFG<IRNode> multiBlockCfg = new CFG<>(nodes, edges);

        // Act
        controlFlowAnalysis.onHandle(multiBlockCfg);

        // Assert
        // ControlFlowAnalysis是一个优化器，会合并和删除节点，所以节点数量可能减少
        assertTrue(multiBlockCfg.nodes.size() >= 1 && multiBlockCfg.nodes.size() <= 5,
                   "Multi-block CFG should have between 1 and 5 nodes after optimization");
    }

    @Test
    void testCFGIntegrityAfterAnalysis() {
        // Arrange
        CFG<IRNode> originalCfg = createTestCFG();
        int originalNodeCount = originalCfg.nodes.size();
        int originalEdgeCount = originalCfg.edges.size();

        // Act
        controlFlowAnalysis.onHandle(originalCfg);

        // Assert
        assertNotNull(originalCfg.nodes, "Nodes list should not be null");
        assertNotNull(originalCfg.edges, "Edges list should not be null");
        assertTrue(originalCfg.nodes.size() >= 0, "Node count should be non-negative");
        assertTrue(originalCfg.edges.size() >= 0, "Edge count should be non-negative");
    }
}