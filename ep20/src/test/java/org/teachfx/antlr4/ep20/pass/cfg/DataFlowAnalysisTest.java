package org.teachfx.antlr4.ep20.pass.cfg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep20.ir.IRNode;
import org.teachfx.antlr4.ep20.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep20.ir.expr.val.ConstVal;
import org.teachfx.antlr4.ep20.ir.stmt.Assign;
import org.teachfx.antlr4.ep20.ir.stmt.Label;
import org.teachfx.antlr4.ep20.utils.Kind;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("数据流分析测试")
class DataFlowAnalysisTest {

    private CFG<IRNode> cfg;
    private List<BasicBlock<IRNode>> blocks;
    private List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges;

    @BeforeEach
    void setUp() {
        // 创建基本块列表
        blocks = new ArrayList<>();
        edges = new ArrayList<>();
        
        // 创建第一个基本块 - 赋值指令
        List<IRNode> firstInstructions = new ArrayList<>();
        FrameSlot slot1 = new FrameSlot(1);
        firstInstructions.add(Assign.with(slot1, ConstVal.valueOf(10)));
        
        List<Loc<IRNode>> firstLocList = firstInstructions.stream().map(Loc::new).toList();
        BasicBlock<IRNode> firstBlock = new BasicBlock<>(Kind.CONTINUOUS, firstLocList, new Label("L1", null), 0);
        blocks.add(firstBlock);
        
        // 创建第二个基本块 - 另一个赋值指令
        List<IRNode> secondInstructions = new ArrayList<>();
        FrameSlot slot2 = new FrameSlot(2);
        secondInstructions.add(Assign.with(slot2, ConstVal.valueOf(20)));
        
        List<Loc<IRNode>> secondLocList = secondInstructions.stream().map(Loc::new).toList();
        BasicBlock<IRNode> secondBlock = new BasicBlock<>(Kind.CONTINUOUS, secondLocList, new Label("L2", null), 1);
        blocks.add(secondBlock);
        
        // 创建边 - 第一个块到第二个块
        edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 1, 1));
        
        // 创建CFG
        cfg = new CFG<>(blocks, edges);
    }

    @Test
    @DisplayName("应正确初始化数据流分析结构")
    void testDataFlowAnalysisInitialization() {
        // 验证CFG创建成功
        assertNotNull(cfg);
        assertEquals(2, cfg.nodes.size());
        assertEquals(1, cfg.edges.size());
        
        // 验证基本块属性
        BasicBlock<IRNode> firstBlock = cfg.getBlock(0);
        BasicBlock<IRNode> secondBlock = cfg.getBlock(1);
        
        assertNotNull(firstBlock);
        assertNotNull(secondBlock);
        assertEquals(0, firstBlock.getId());
        assertEquals(1, secondBlock.getId());
        
        // 验证连接关系
        assertEquals(0, cfg.getInDegree(0));  // 第一个块没有前驱
        assertEquals(1, cfg.getOutDegree(0)); // 第一个块有一个后继
        assertEquals(1, cfg.getInDegree(1));  // 第二个块有一个前驱
        assertEquals(0, cfg.getOutDegree(1)); // 第二个块没有后继
        
        // 验证前驱和后继节点
        assertTrue(cfg.getFrontier(0).isEmpty()); // 第一个块没有前驱
        assertTrue(cfg.getSucceed(1).isEmpty());  // 第二个块没有后继
        assertEquals(1, cfg.getFrontier(1).size()); // 第二个块有一个前驱
        assertEquals(1, cfg.getSucceed(0).size());  // 第一个块有一个后继
    }

    @Test
    @DisplayName("应正确获取前驱和后继节点")
    void testGetPredecessorsAndSuccessors() {
        // 验证第一个块的后继
        var successors = cfg.getSucceed(0);
        assertTrue(successors.contains(1));
        assertEquals(1, successors.size());
        
        // 验证第二个块的前驱
        var predecessors = cfg.getFrontier(1);
        assertTrue(predecessors.contains(0));
        assertEquals(1, predecessors.size());
    }

    @Test
    @DisplayName("应正确计算节点的入度和出度")
    void testInAndOutDegreeCalculation() {
        // 验证第一个块（起始块）
        assertEquals(0, cfg.getInDegree(0));  // 没有前驱
        assertEquals(1, cfg.getOutDegree(0)); // 有一个后继
        
        // 验证第二个块（结束块）
        assertEquals(1, cfg.getInDegree(1));  // 有一个前驱
        assertEquals(0, cfg.getOutDegree(1)); // 没有后继
    }

    @Test
    @DisplayName("应正确处理空CFG")
    void testEmptyCFG() {
        // 创建空的CFG
        List<BasicBlock<IRNode>> emptyBlocks = new ArrayList<>();
        List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> emptyEdges = new ArrayList<>();
        
        // 对于空CFG，我们不创建实例，因为构造函数会抛出异常
        assertThrows(Exception.class, () -> {
            CFG<IRNode> emptyCfg = new CFG<>(emptyBlocks, emptyEdges);
        });
    }
}