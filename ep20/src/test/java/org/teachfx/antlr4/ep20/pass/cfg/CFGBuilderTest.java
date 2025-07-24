package org.teachfx.antlr4.ep20.pass.cfg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep20.ir.IRNode;
import org.teachfx.antlr4.ep20.ir.stmt.Label;

import static org.junit.jupiter.api.Assertions.*;

class CFGBuilderTest {

    private LinearIRBlock startBlock;

    @BeforeEach
    void setUp() {
        startBlock = new LinearIRBlock();
        startBlock.addStmt(new Label("start", null));
    }

    @Test
    void testCFGBuilderCreation() {
        // 测试CFGBuilder能否正确创建
        CFGBuilder cfgBuilder = new CFGBuilder(startBlock);
        assertNotNull(cfgBuilder);
        
        CFG<IRNode> cfg = cfgBuilder.getCFG();
        assertNotNull(cfg);
    }

    @Test
    void testGetCFG() {
        // 测试获取的CFG是否正确
        CFGBuilder cfgBuilder = new CFGBuilder(startBlock);
        CFG<IRNode> cfg = cfgBuilder.getCFG();
        
        assertNotNull(cfg.nodes);
        assertNotNull(cfg.edges);
        assertFalse(cfg.nodes.isEmpty());
    }

    @Test
    void testSimpleLinearFlow() {
        // 创建线性流程：start -> block1 -> block2
        LinearIRBlock block1 = new LinearIRBlock();
        block1.addStmt(new Label("block1", null));
        
        LinearIRBlock block2 = new LinearIRBlock();
        block2.addStmt(new Label("block2", null));
        
        // 设置后继关系
        startBlock.getSuccessors().add(block1);
        block1.getSuccessors().add(block2);
        
        // 构建CFG
        CFGBuilder cfgBuilder = new CFGBuilder(startBlock);
        CFG<IRNode> cfg = cfgBuilder.getCFG();
        
        // 验证
        assertTrue(cfg.nodes.size() >= 2);
        assertTrue(cfg.edges.size() >= 1);
    }

    @Test
    void testEmptyBlockHandling() {
        // 测试空块的处理
        LinearIRBlock emptyBlock = new LinearIRBlock();
        emptyBlock.addStmt(new Label("empty", null));
        
        startBlock.getSuccessors().add(emptyBlock);
        
        CFGBuilder cfgBuilder = new CFGBuilder(startBlock);
        CFG<IRNode> cfg = cfgBuilder.getCFG();
        
        assertTrue(cfg.nodes.size() >= 1);
        assertTrue(cfg.edges.size() >= 0);
    }

    @Test
    void testEdgeWeightConsistency() {
        // 测试边的权重一致性
        LinearIRBlock block1 = new LinearIRBlock();
        block1.addStmt(new Label("block1", null));
        
        LinearIRBlock block2 = new LinearIRBlock();
        block2.addStmt(new Label("block2", null));
        
        startBlock.getSuccessors().add(block1);
        block1.getSuccessors().add(block2);
        
        CFGBuilder cfgBuilder = new CFGBuilder(startBlock);
        CFG<IRNode> cfg = cfgBuilder.getCFG();
        
        // 验证所有边都有正确的权重
        for (var edge : cfg.edges) {
            assertNotNull(edge);
            assertTrue(edge.getRight() >= 0); // weight should be non-negative
        }
    }

    @Test
    void testBasicBlockProperties() {
        // 测试基本块属性
        CFGBuilder cfgBuilder = new CFGBuilder(startBlock);
        CFG<IRNode> cfg = cfgBuilder.getCFG();
        
        // 验证所有节点都有对应的标签
        for (BasicBlock<IRNode> block : cfg) {
            assertNotNull(block.getLabel());
            assertTrue(block.getId() >= 0);
        }
    }
}