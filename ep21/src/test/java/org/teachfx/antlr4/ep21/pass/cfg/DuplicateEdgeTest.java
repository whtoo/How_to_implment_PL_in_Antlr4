package org.teachfx.antlr4.ep21.pass.cfg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.stmt.Label;
import org.teachfx.antlr4.ep21.pass.cfg.Loc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 重复边问题单元测试
 * 专门测试CFG构建和优化过程中的重复边预防机制
 */
class DuplicateEdgeTest {

    private ControlFlowAnalysis<IRNode> controlFlowAnalysis;

    @BeforeEach
    void setUp() {
        controlFlowAnalysis = new ControlFlowAnalysis<>();
    }

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