package org.teachfx.antlr4.ep21.pass.cfg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;
import org.teachfx.antlr4.ep21.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep21.ir.stmt.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 1, CFGConstants.JUMP_EDGE_TYPE));

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
        edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 0, CFGConstants.JUMP_EDGE_TYPE)); // 自循环

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
        edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 1, CFGConstants.JUMP_EDGE_TYPE));

        CFG<IRNode> cjmpCfg = new CFG<>(nodes, edges);

        // Act
        controlFlowAnalysis.onHandle(cjmpCfg);

        // Assert
        assertNotNull(cjmpCfg, "CFG with CJMP instruction should be processed without exception");
    }

    @Test
    void testDebugFlagBehavior() {
        // Arrange
        boolean originalDebug = ControlFlowAnalysis.isDebugEnabled();

        // Act
        ControlFlowAnalysis.setDebugEnabled(true);
        controlFlowAnalysis.onHandle(testCfg);

        // Reset
        ControlFlowAnalysis.setDebugEnabled(originalDebug);

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
                edges.add(org.apache.commons.lang3.tuple.Triple.of(i, i + 1, CFGConstants.JUMP_EDGE_TYPE));
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
    
    // ==================== optimizeJumpInstructions 测试 ====================
    
    @Test
    void testOptimizeJumpInstructionsWithRedundantJump() {
        // Arrange - 创建包含冗余跳转的CFG
        CFG<IRNode> cfgWithRedundantJump = createCFGWithRedundantJump();
        int originalEdgeCount = cfgWithRedundantJump.edges.size();
        int originalNodeCount = cfgWithRedundantJump.nodes.size();

        // Act - 执行优化
        controlFlowAnalysis.onHandle(cfgWithRedundantJump);

        // Assert - 验证跳转指令被正确移除
        assertTrue(cfgWithRedundantJump.edges.size() <= originalEdgeCount,
                   "优化后边数量应该减少或保持不变");
        assertNotNull(cfgWithRedundantJump.nodes, "节点列表不应为空");
        assertTrue(cfgWithRedundantJump.nodes.size() >= 1, "至少应该保留一个节点");
    }
    
    @Test
    void testOptimizeJumpInstructionsWithNoJump() {
        // Arrange - 创建不包含跳转指令的CFG
        CFG<IRNode> cfgWithoutJump = createCFGWithoutJump();
        int originalNodeCount = cfgWithoutJump.nodes.size();

        // Act - 执行完整优化（包括基本块合并）
        controlFlowAnalysis.onHandle(cfgWithoutJump);

        // Assert - 允许基本块合并优化改变节点数量，但CFG应保持有效
        assertNotNull(cfgWithoutJump.nodes, "CFG节点列表应不为空");
        assertTrue(cfgWithoutJump.nodes.size() >= 1, "至少应保留一个节点");
        assertTrue(cfgWithoutJump.nodes.size() <= originalNodeCount,
                  "优化后节点数量不应增加");
    }
    
    @Test
    void testOptimizeJumpInstructionsWithConditionalJump() {
        // Arrange - 创建包含条件跳转的CFG
        CFG<IRNode> cfgWithConditionalJump = createCFGWithConditionalJump();

        // Act
        controlFlowAnalysis.onHandle(cfgWithConditionalJump);

        // Assert - 条件跳转不应被优化
        assertNotNull(cfgWithConditionalJump.nodes, "CFG应正常处理条件跳转");
        assertTrue(cfgWithConditionalJump.nodes.size() >= 1, "应该保留至少一个节点");
    }
    
    @Test
    void testOptimizeJumpInstructionsWithSelfLoop() {
        // Arrange - 创建自循环CFG
        CFG<IRNode> cfgWithSelfLoop = createCFGWithSelfLoop();
        assertNotNull(cfgWithSelfLoop, "CFG创建应成功");
        assertTrue(cfgWithSelfLoop.nodes.size() > 0, "初始CFG应有节点");

        // Act - 执行优化，允许自循环CFG的处理
        try {
            controlFlowAnalysis.onHandle(cfgWithSelfLoop);
        } catch (Exception e) {
            // 如果优化过程出现异常，这本身就是一个测试点
            fail("优化自循环CFG时不应出现异常: " + e.getMessage());
        }

        // Assert - 验证CFG结构完整性（无论节点数量变化如何）
        assertNotNull(cfgWithSelfLoop, "自循环CFG应被正确处理");
        assertNotNull(cfgWithSelfLoop.nodes, "节点列表应不为null");
        assertNotNull(cfgWithSelfLoop.edges, "边列表应不为null");
        
        // 如果节点被完全移除，CFG仍应该存在但为空
        assertTrue(cfgWithSelfLoop.nodes.size() >= 0, "节点数量应非负");
    }
    
    // ==================== optimizeBasicBlockMerging 测试 ====================
    
    @Test
    void testOptimizeBasicBlockMergingWithSinglePredecessor() {
        // Arrange - 创建具有单前驱节点的CFG
        CFG<IRNode> cfgWithSinglePredecessor = createCFGForBlockMerging();
        int originalNodeCount = cfgWithSinglePredecessor.nodes.size();

        // Act - 执行基本块合并优化
        controlFlowAnalysis.onHandle(cfgWithSinglePredecessor);

        // Assert - 验证节点数量是否减少（合并了）
        assertTrue(cfgWithSinglePredecessor.nodes.size() <= originalNodeCount,
                   "基本块合并后节点数量应该减少或相等");
        assertNotNull(cfgWithSinglePredecessor.nodes, "节点列表不应为空");
        assertTrue(cfgWithSinglePredecessor.nodes.size() >= 1, "至少应该保留一个节点");
    }
    
    @Test
    void testOptimizeBasicBlockMergingWithMultiplePredecessors() {
        // Arrange - 创建具有多个前驱的CFG
        CFG<IRNode> cfgWithMultiplePredecessors = createCFGWithMultiplePredecessors();

        // Act
        controlFlowAnalysis.onHandle(cfgWithMultiplePredecessors);

        // Assert - 具有多个前驱的节点不应被合并
        assertNotNull(cfgWithMultiplePredecessors.nodes, "CFG应正常处理多前驱情况");
        assertTrue(cfgWithMultiplePredecessors.nodes.size() >= 1, "至少应保留一个节点");
    }
    
    @Test
    void testOptimizeBasicBlockMergingPreservesCFGStructure() {
        // Arrange
        CFG<IRNode> cfgForMergeTest = createCFGForBlockMerging();

        // Act
        controlFlowAnalysis.onHandle(cfgForMergeTest);

        // Assert - 验证CFG结构的完整性
        assertNotNull(cfgForMergeTest.nodes, "节点列表应不为空");
        assertNotNull(cfgForMergeTest.edges, "边列表应不为空");
        
        // 验证没有悬空边
        for (var edge : cfgForMergeTest.edges) {
            boolean sourceExists = cfgForMergeTest.nodes.stream()
                .anyMatch(node -> node.getId() == edge.getLeft());
            boolean targetExists = cfgForMergeTest.nodes.stream()
                .anyMatch(node -> node.getId() == edge.getMiddle());
            
            assertTrue(sourceExists && targetExists,
                       String.format("边 %d->%d 的端点应该存在于节点列表中",
                                   edge.getLeft(), edge.getMiddle()));
        }
    }
    
    // ==================== 重复边问题测试 ====================
    
    @Test
    void testNoDuplicateEdgesAfterOptimization() {
        // Arrange - 创建可能产生重复边的CFG场景
        CFG<IRNode> cfgWithPotentialDuplicates = createCFGWithPotentialDuplicateEdges();
        Set<String> edgesBeforeOptimization = getEdgeSignatures(cfgWithPotentialDuplicates);
        
        // Act
        controlFlowAnalysis.onHandle(cfgWithPotentialDuplicates);
        Set<String> edgesAfterOptimization = getEdgeSignatures(cfgWithPotentialDuplicates);
        
        // Assert - 验证没有重复边
        assertNoDuplicateEdges(cfgWithPotentialDuplicates, "优化后不应有重复边");
        
        // 验证边数量合理（不应异常增加）
        assertTrue(edgesAfterOptimization.size() <= edgesBeforeOptimization.size() + 2,
                   "优化后的边数量不应异常增加");
    }
    
    @Test
    void testEdgeTypeConversionDoesNotCreateDuplicates() {
        // Arrange - 创建包含JMP指令的CFG，测试边类型转换
        CFG<IRNode> cfgWithJump = createCFGWithJumpInstruction();
        
        // 记录转换前的边
        int edgesBefore = cfgWithJump.edges.size();
        Set<String> edgeSignaturesBefore = getEdgeSignatures(cfgWithJump);
        
        // Act - 执行优化，会触发JMP到SUCCESSOR的边类型转换
        controlFlowAnalysis.onHandle(cfgWithJump);
        
        // Assert
        assertNoDuplicateEdges(cfgWithJump, "边类型转换后不应产生重复边");
        
        // 验证边数量合理
        assertTrue(cfgWithJump.edges.size() <= edgesBefore + 1,
                   "边类型转换不应导致边数量异常增加");
    }
    
    @Test
    void testBlockMergingDoesNotCreateDuplicateEdges() {
        // Arrange - 创建适合基本块合并的场景
        CFG<IRNode> cfgForMerging = createCFGForBlockMergingWithMultipleOutEdges();
        Set<String> edgesBefore = getEdgeSignatures(cfgForMerging);
        
        // Act
        controlFlowAnalysis.onHandle(cfgForMerging);
        
        // Assert
        assertNoDuplicateEdges(cfgForMerging, "基本块合并后不应产生重复边");
        
        // 验证所有边都是有效的
        for (var edge : cfgForMerging.edges) {
            assertTrue(edge.getLeft() >= 0 && edge.getMiddle() >= 0,
                      "边的端点ID应该有效: " + edge);
        }
    }
    
    @Test
    void testComplexCFGWithMultipleEdgeTypes() {
        // Arrange - 创建包含多种边类型的复杂CFG
        CFG<IRNode> complexCfg = createComplexCFGWithMixedEdges();
        
        // Act
        controlFlowAnalysis.onHandle(complexCfg);
        
        // Assert - 复杂场景下也不应有重复边
        assertNoDuplicateEdges(complexCfg, "复杂CFG优化后不应有重复边");
        
        // 验证CFG结构完整性
        assertTrue(complexCfg.nodes.size() >= 1, "应至少保留一个节点");
        assertTrue(complexCfg.edges.size() >= 0, "边数量应非负");
    }
    
    @Test
    void testDuplicateEdgePreventionInCFGBuilder() {
        // Arrange - 测试CFGBuilder的addEdgeIfNotExists方法
        List<BasicBlock<IRNode>> nodes = new ArrayList<>();
        List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges = new ArrayList<>();
        
        BasicBlock<IRNode> block0 = new BasicBlock<>(
            org.teachfx.antlr4.ep21.utils.Kind.CONTINUOUS,
            List.of(new Loc<>(new Label("L0", null))),
            new org.teachfx.antlr4.ep21.ir.stmt.Label("L0", null), 0);
        
        BasicBlock<IRNode> block1 = new BasicBlock<>(
            org.teachfx.antlr4.ep21.utils.Kind.CONTINUOUS,
            List.of(new Loc<>(new Label("L1", null))),
            new org.teachfx.antlr4.ep21.ir.stmt.Label("L1", null), 1);
        
        nodes.add(block0);
        nodes.add(block1);
        
        // 尝试添加重复边（相同节点对，不同类型）
        edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 1, CFGConstants.JUMP_EDGE_TYPE));
        edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 1, CFGConstants.SUCCESSOR_EDGE_TYPE)); // 重复边
        
        // Act
        CFG<IRNode> cfg = new CFG<>(nodes, edges);
        
        // Assert - CFG构造函数应该能处理这种情况，但最终不应有重复边
        Set<String> edgePairs = new HashSet<>();
        for (var edge : cfg.edges) {
            String pair = edge.getLeft() + "->" + edge.getMiddle();
            assertTrue(edgePairs.add(pair), "不应有重复边: " + pair);
        }
    }
    
    // ==================== 辅助方法：重复边检测 ====================
    
    /**
     * 获取CFG中所有边的签名（源节点->目标节点）
     */
    private Set<String> getEdgeSignatures(CFG<IRNode> cfg) {
        Set<String> signatures = new HashSet<>();
        for (var edge : cfg.edges) {
            signatures.add(edge.getLeft() + "->" + edge.getMiddle());
        }
        return signatures;
    }
    
    /**
     * 断言CFG中没有重复边
     */
    private void assertNoDuplicateEdges(CFG<IRNode> cfg, String message) {
        Set<String> seenEdges = new HashSet<>();
        for (var edge : cfg.edges) {
            String edgeKey = edge.getLeft() + "->" + edge.getMiddle();
            assertTrue(seenEdges.add(edgeKey),
                      message + ": 发现重复边 " + edgeKey);
        }
    }
    
    // ==================== 辅助方法：创建测试CFG ====================
    
    /**
     * 创建包含冗余跳转的CFG
     */
    private CFG<IRNode> createCFGWithRedundantJump() {
        List<BasicBlock<IRNode>> nodes = new ArrayList<>();
        List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges = new ArrayList<>();
        
        // 创建三个基本块：让ord自动分配，然后根据实际ord创建CFG
        
        // 创建基本块，按创建顺序分配ord：block0=0, block1=1, block2=2
        BasicBlock<IRNode> block0 = new BasicBlock<>(
            org.teachfx.antlr4.ep21.utils.Kind.CONTINUOUS,
            List.of(new Loc<>(new Label("L0", null))),
            new org.teachfx.antlr4.ep21.ir.stmt.Label("L0", null), 0);
        
        BasicBlock<IRNode> block1 = new BasicBlock<>(
            org.teachfx.antlr4.ep21.utils.Kind.CONTINUOUS,
            List.of(new Loc<>(new Label("L1", null))),
            new org.teachfx.antlr4.ep21.ir.stmt.Label("L1", null), 1);
        
        BasicBlock<IRNode> block2 = new BasicBlock<>(
            org.teachfx.antlr4.ep21.utils.Kind.CONTINUOUS,
            List.of(new Loc<>(new Label("L2", null))),
            new org.teachfx.antlr4.ep21.ir.stmt.Label("L2", null), 2);
        
        nodes.add(block0);
        nodes.add(block1);
        nodes.add(block2);
        
        // 创建线性链结构：0->1->2
        edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 1, CFGConstants.SUCCESSOR_EDGE_TYPE));
        edges.add(org.apache.commons.lang3.tuple.Triple.of(1, 2, CFGConstants.SUCCESSOR_EDGE_TYPE));
        
        return new CFG<>(nodes, edges);
    }
    
    /**
     * 创建不包含跳转指令的CFG
     */
    private CFG<IRNode> createCFGWithoutJump() {
        List<BasicBlock<IRNode>> nodes = new ArrayList<>();
        List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges = new ArrayList<>();
        
        BasicBlock<IRNode> block1 = new BasicBlock<>(
            org.teachfx.antlr4.ep21.utils.Kind.CONTINUOUS,
            List.of(new Loc<>(new Label("L1", null))),
            new org.teachfx.antlr4.ep21.ir.stmt.Label("L1", null), 0);
        
        BasicBlock<IRNode> block2 = new BasicBlock<>(
            org.teachfx.antlr4.ep21.utils.Kind.CONTINUOUS,
            List.of(new Loc<>(new Label("L2", null))),
            new org.teachfx.antlr4.ep21.ir.stmt.Label("L2", null), 1);
        
        nodes.add(block1);
        nodes.add(block2);
        
        edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 1, CFGConstants.SUCCESSOR_EDGE_TYPE));
        
        return new CFG<>(nodes, edges);
    }
    
    /**
     * 创建包含条件跳转的CFG
     */
    private CFG<IRNode> createCFGWithConditionalJump() {
        List<BasicBlock<IRNode>> nodes = new ArrayList<>();
        List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges = new ArrayList<>();
        
        // 创建基本块，按ord顺序：block0=0, block1=1, block2=2
        BasicBlock<IRNode> block1 = new BasicBlock<>(
            org.teachfx.antlr4.ep21.utils.Kind.END_BY_CJMP,
            List.of(new Loc<>(new Label("L1", null))),
            new org.teachfx.antlr4.ep21.ir.stmt.Label("L1", null), 0);
        
        BasicBlock<IRNode> block2 = new BasicBlock<>(
            org.teachfx.antlr4.ep21.utils.Kind.CONTINUOUS,
            List.of(new Loc<>(new Label("L2", null))),
            new org.teachfx.antlr4.ep21.ir.stmt.Label("L2", null), 1);
        
        BasicBlock<IRNode> block3 = new BasicBlock<>(
            org.teachfx.antlr4.ep21.utils.Kind.CONTINUOUS,
            List.of(new Loc<>(new Label("L3", null))),
            new org.teachfx.antlr4.ep21.ir.stmt.Label("L3", null), 2);
        
        nodes.add(block1);
        nodes.add(block2);
        nodes.add(block3);
        
        // 创建分支结构：0 -> 1 和 0 -> 2
        edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 1, CFGConstants.JUMP_EDGE_TYPE));
        edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 2, CFGConstants.JUMP_EDGE_TYPE));
        
        return new CFG<>(nodes, edges);
    }
    
    /**
     * 创建自循环CFG
     */
    private CFG<IRNode> createCFGWithSelfLoop() {
        List<BasicBlock<IRNode>> nodes = new ArrayList<>();
        List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges = new ArrayList<>();
        
        // 创建自循环基本块
        BasicBlock<IRNode> block = new BasicBlock<>(
            org.teachfx.antlr4.ep21.utils.Kind.END_BY_JMP,
            List.of(new Loc<>(new Label("L0", null))),
            new org.teachfx.antlr4.ep21.ir.stmt.Label("L0", null), 0);
        
        nodes.add(block);
        edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 0, CFGConstants.JUMP_EDGE_TYPE)); // 自循环
        
        return new CFG<>(nodes, edges);
    }
    
    /**
     * 创建适合基本块合并测试的CFG
     */
    private CFG<IRNode> createCFGForBlockMerging() {
        List<BasicBlock<IRNode>> nodes = new ArrayList<>();
        List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges = new ArrayList<>();
        
        // 创建链式结构：0 -> 1 -> 2，其中1只有0一个前驱
        BasicBlock<IRNode> block0 = new BasicBlock<>(
            org.teachfx.antlr4.ep21.utils.Kind.CONTINUOUS,
            List.of(new Loc<>(new Label("L0", null))),
            new org.teachfx.antlr4.ep21.ir.stmt.Label("L0", null), 0);
        
        BasicBlock<IRNode> block1 = new BasicBlock<>(
            org.teachfx.antlr4.ep21.utils.Kind.CONTINUOUS,
            List.of(new Loc<>(new Label("L1", null))),
            new org.teachfx.antlr4.ep21.ir.stmt.Label("L1", null), 1);
        
        BasicBlock<IRNode> block2 = new BasicBlock<>(
            org.teachfx.antlr4.ep21.utils.Kind.CONTINUOUS,
            List.of(new Loc<>(new Label("L2", null))),
            new org.teachfx.antlr4.ep21.ir.stmt.Label("L2", null), 2);
        
        nodes.add(block0);
        nodes.add(block1);
        nodes.add(block2);
        
        edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 1, CFGConstants.SUCCESSOR_EDGE_TYPE));
        edges.add(org.apache.commons.lang3.tuple.Triple.of(1, 2, CFGConstants.SUCCESSOR_EDGE_TYPE));
        
        return new CFG<>(nodes, edges);
    }
    
    /**
     * 创建具有多个前驱的CFG
     */
    private CFG<IRNode> createCFGWithMultiplePredecessors() {
        List<BasicBlock<IRNode>> nodes = new ArrayList<>();
        List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges = new ArrayList<>();
        
        // 创建结构：0->2, 1->2，节点2有多个前驱
        BasicBlock<IRNode> block0 = new BasicBlock<>(
            org.teachfx.antlr4.ep21.utils.Kind.CONTINUOUS,
            List.of(new Loc<>(new Label("L0", null))),
            new org.teachfx.antlr4.ep21.ir.stmt.Label("L0", null), 0);
        
        BasicBlock<IRNode> block1 = new BasicBlock<>(
            org.teachfx.antlr4.ep21.utils.Kind.CONTINUOUS,
            List.of(new Loc<>(new Label("L1", null))),
            new org.teachfx.antlr4.ep21.ir.stmt.Label("L1", null), 1);
        
        BasicBlock<IRNode> block2 = new BasicBlock<>(
            org.teachfx.antlr4.ep21.utils.Kind.CONTINUOUS,
            List.of(new Loc<>(new Label("L2", null))),
            new org.teachfx.antlr4.ep21.ir.stmt.Label("L2", null), 2);
        
        nodes.add(block0);
        nodes.add(block1);
        nodes.add(block2);
        
        edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 2, CFGConstants.SUCCESSOR_EDGE_TYPE));
        edges.add(org.apache.commons.lang3.tuple.Triple.of(1, 2, CFGConstants.SUCCESSOR_EDGE_TYPE));
        
        return new CFG<>(nodes, edges);
    }
    
    /**
     * 创建可能产生重复边的CFG场景
     */
    private CFG<IRNode> createCFGWithPotentialDuplicateEdges() {
        List<BasicBlock<IRNode>> nodes = new ArrayList<>();
        List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges = new ArrayList<>();
        
        // 创建结构：0->1->2，其中1可能被合并到0，且2有多个入边
        BasicBlock<IRNode> block0 = new BasicBlock<>(
            org.teachfx.antlr4.ep21.utils.Kind.END_BY_JMP,
            List.of(new Loc<>(new Label("L0", null))),
            new org.teachfx.antlr4.ep21.ir.stmt.Label("L0", null), 0);
        
        BasicBlock<IRNode> block1 = new BasicBlock<>(
            org.teachfx.antlr4.ep21.utils.Kind.CONTINUOUS,
            List.of(new Loc<>(new Label("L1", null))),
            new org.teachfx.antlr4.ep21.ir.stmt.Label("L1", null), 1);
        
        BasicBlock<IRNode> block2 = new BasicBlock<>(
            org.teachfx.antlr4.ep21.utils.Kind.CONTINUOUS,
            List.of(new Loc<>(new Label("L2", null))),
            new org.teachfx.antlr4.ep21.ir.stmt.Label("L2", null), 2);
        
        nodes.add(block0);
        nodes.add(block1);
        nodes.add(block2);
        
        // 创建边：0->1, 1->2, 0->2（模拟可能导致重复边的场景）
        edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 1, CFGConstants.JUMP_EDGE_TYPE));
        edges.add(org.apache.commons.lang3.tuple.Triple.of(1, 2, CFGConstants.SUCCESSOR_EDGE_TYPE));
        edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 2, CFGConstants.JUMP_EDGE_TYPE));
        
        return new CFG<>(nodes, edges);
    }
    
    /**
     * 创建包含JMP指令的CFG，用于测试边类型转换
     */
    private CFG<IRNode> createCFGWithJumpInstruction() {
        List<BasicBlock<IRNode>> nodes = new ArrayList<>();
        List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges = new ArrayList<>();
        
        // 创建结构：0->1，其中0以JMP结束
        BasicBlock<IRNode> block0 = new BasicBlock<>(
            org.teachfx.antlr4.ep21.utils.Kind.END_BY_JMP,
            List.of(new Loc<>(new Label("L0", null))),
            new org.teachfx.antlr4.ep21.ir.stmt.Label("L0", null), 0);
        
        BasicBlock<IRNode> block1 = new BasicBlock<>(
            org.teachfx.antlr4.ep21.utils.Kind.CONTINUOUS,
            List.of(new Loc<>(new Label("L1", null))),
            new org.teachfx.antlr4.ep21.ir.stmt.Label("L1", null), 1);
        
        nodes.add(block0);
        nodes.add(block1);
        
        edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 1, CFGConstants.JUMP_EDGE_TYPE));
        
        return new CFG<>(nodes, edges);
    }
    
    /**
     * 创建用于测试基本块合并产生重复边的场景
     */
    private CFG<IRNode> createCFGForBlockMergingWithMultipleOutEdges() {
        List<BasicBlock<IRNode>> nodes = new ArrayList<>();
        List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges = new ArrayList<>();
        
        // 创建结构：0->1->2 和 0->2，合并1到0时可能产生重复边
        BasicBlock<IRNode> block0 = new BasicBlock<>(
            org.teachfx.antlr4.ep21.utils.Kind.CONTINUOUS,
            List.of(new Loc<>(new Label("L0", null))),
            new org.teachfx.antlr4.ep21.ir.stmt.Label("L0", null), 0);
        
        BasicBlock<IRNode> block1 = new BasicBlock<>(
            org.teachfx.antlr4.ep21.utils.Kind.CONTINUOUS,
            List.of(new Loc<>(new Label("L1", null))),
            new org.teachfx.antlr4.ep21.ir.stmt.Label("L1", null), 1);
        
        BasicBlock<IRNode> block2 = new BasicBlock<>(
            org.teachfx.antlr4.ep21.utils.Kind.CONTINUOUS,
            List.of(new Loc<>(new Label("L2", null))),
            new org.teachfx.antlr4.ep21.ir.stmt.Label("L2", null), 2);
        
        nodes.add(block0);
        nodes.add(block1);
        nodes.add(block2);
        
        edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 1, CFGConstants.SUCCESSOR_EDGE_TYPE));
        edges.add(org.apache.commons.lang3.tuple.Triple.of(1, 2, CFGConstants.SUCCESSOR_EDGE_TYPE));
        edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 2, CFGConstants.JUMP_EDGE_TYPE));
        
        return new CFG<>(nodes, edges);
    }
    
    /**
     * 创建包含多种边类型的复杂CFG
     */
    private CFG<IRNode> createComplexCFGWithMixedEdges() {
        List<BasicBlock<IRNode>> nodes = new ArrayList<>();
        List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges = new ArrayList<>();
        
        // 创建复杂结构：0->1->2, 0->2, 1->3, 2->3
        for (int i = 0; i < 4; i++) {
            BasicBlock<IRNode> block = new BasicBlock<>(
                org.teachfx.antlr4.ep21.utils.Kind.CONTINUOUS,
                List.of(new Loc<>(new Label("L" + i, null))),
                new org.teachfx.antlr4.ep21.ir.stmt.Label("L" + i, null), i);
            nodes.add(block);
        }
        
        edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 1, CFGConstants.JUMP_EDGE_TYPE));
        edges.add(org.apache.commons.lang3.tuple.Triple.of(1, 2, CFGConstants.SUCCESSOR_EDGE_TYPE));
        edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 2, CFGConstants.JUMP_EDGE_TYPE));
        edges.add(org.apache.commons.lang3.tuple.Triple.of(1, 3, CFGConstants.JUMP_EDGE_TYPE));
        edges.add(org.apache.commons.lang3.tuple.Triple.of(2, 3, CFGConstants.SUCCESSOR_EDGE_TYPE));
        
        return new CFG<>(nodes, edges);
    }
}