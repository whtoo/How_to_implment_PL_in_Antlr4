package org.teachfx.antlr4.ep20.pass.cfg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep20.ir.IRNode;
import org.teachfx.antlr4.ep20.ir.expr.val.ConstVal;
import org.teachfx.antlr4.ep20.ir.stmt.Assign;
import org.teachfx.antlr4.ep20.ir.stmt.Label;
import org.teachfx.antlr4.ep20.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep20.utils.Kind;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BasicBlock优化测试")
class BasicBlockOptimizationTest {

    private BasicBlock<IRNode> basicBlock;
    private List<IRNode> testInstructions;

    @BeforeEach
    void setUp() {
        // 创建测试指令序列
        testInstructions = new ArrayList<>();
        FrameSlot slot1 = new FrameSlot(1);
        FrameSlot slot2 = new FrameSlot(2);
        testInstructions.add(Assign.with(slot1, ConstVal.valueOf(1))); // 简单赋值指令
        testInstructions.add(Assign.with(slot2, ConstVal.valueOf(2))); // 简单赋值指令
        
        // 创建基本块
        List<Loc<IRNode>> locList = testInstructions.stream().map(Loc::new).toList();
        basicBlock = new BasicBlock<>(Kind.CONTINUOUS, locList, new Label("test", null), 0);
    }

    @Test
    @DisplayName("应正确合并相邻基本块")
    void testMergeNearBlock() {
        // 创建第一个基本块 - 两个赋值指令
        List<IRNode> firstInstructions = new ArrayList<>();
        FrameSlot slot1 = new FrameSlot(1);
        FrameSlot slot2 = new FrameSlot(2);
        firstInstructions.add(Assign.with(slot1, ConstVal.valueOf(1)));
        firstInstructions.add(Assign.with(slot2, ConstVal.valueOf(2)));
        
        List<Loc<IRNode>> firstLocList = firstInstructions.stream().map(Loc::new).toList();
        BasicBlock<IRNode> firstBlock = new BasicBlock<>(Kind.CONTINUOUS, firstLocList, new Label("L1", null), 0);
        
        // 创建第二个基本块 - 一个赋值指令
        List<IRNode> secondInstructions = new ArrayList<>();
        FrameSlot slot3 = new FrameSlot(3);
        secondInstructions.add(Assign.with(slot3, ConstVal.valueOf(3)));
        
        List<Loc<IRNode>> secondLocList = secondInstructions.stream().map(Loc::new).toList();
        BasicBlock<IRNode> secondBlock = new BasicBlock<>(Kind.CONTINUOUS, secondLocList, new Label("L2", null), 1);
        
        // 执行合并操作
        int originalSize = firstBlock.codes.size();
        firstBlock.mergeNearBlock(secondBlock);
        
        // 验证合并结果 - 应该合并为3个指令
        assertEquals(originalSize + 1, firstBlock.codes.size());
        assertEquals(Kind.CONTINUOUS, firstBlock.kind);
    }

    @Test
    @DisplayName("应正确移除最后一个指令")
    void testRemoveLastInstr() {
        // 验证初始状态
        assertEquals(2, basicBlock.codes.size());
        
        // 移除最后一个指令
        basicBlock.removeLastInstr();
        
        // 验证移除结果
        assertEquals(1, basicBlock.codes.size());
        assertTrue(basicBlock.codes.get(0).instr instanceof Assign);
        assertEquals(Kind.CONTINUOUS, basicBlock.kind);
    }

    @Test
    @DisplayName("应正确处理空基本块")
    void testEmptyBasicBlock() {
        // 创建空基本块
        List<Loc<IRNode>> emptyLocList = new ArrayList<>();
        BasicBlock<IRNode> emptyBlock = new BasicBlock<>(Kind.CONTINUOUS, emptyLocList, new Label("empty", null), 0);
        
        // 验证空基本块属性
        assertTrue(emptyBlock.isEmpty());
        assertEquals(0, emptyBlock.codes.size());
    }

    @Test
    @DisplayName("应正确获取基本块属性")
    void testBasicBlockProperties() {
        // 验证基本块属性
        assertEquals(0, basicBlock.getId());
        assertNotNull(basicBlock.getLabel());
        assertEquals("L0", basicBlock.getOrdLabel());
        assertFalse(basicBlock.isEmpty());
        assertEquals(2, basicBlock.codes.size());
    }
}