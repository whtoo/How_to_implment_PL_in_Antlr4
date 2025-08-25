package org.teachfx.antlr4.ep20.pass.cfg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep20.ir.IRNode;
import org.teachfx.antlr4.ep20.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep20.ir.expr.arith.BinExpr;
import org.teachfx.antlr4.ep20.ir.expr.arith.UnaryExpr;
import org.teachfx.antlr4.ep20.ir.expr.val.ConstVal;
import org.teachfx.antlr4.ep20.ir.stmt.Assign;
import org.teachfx.antlr4.ep20.ir.stmt.Label;
import org.teachfx.antlr4.ep20.ir.expr.Operand;
import org.teachfx.antlr4.ep20.symtab.type.OperatorType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("活性分析测试")
class LivenessAnalysisTest {

    private LivenessAnalysis livenessAnalysis;
    private List<IRNode> testInstructions;

    @BeforeEach
    void setUp() {
        livenessAnalysis = new LivenessAnalysis();
        testInstructions = new ArrayList<>();
    }

    @Test
    @DisplayName("应正确访问标签节点")
    void testVisitLabel() {
        // 创建标签节点
        Label label = new Label("test_label", null);
        
        // 执行访问
        Void result = livenessAnalysis.visit(label);
        
        // 验证结果
        assertNull(result); // visit方法应返回null
        
        // 验证use和def集合都为空
        Set<Operand> useSet = livenessAnalysis.getCurrentUse();
        assertTrue(useSet.isEmpty());
        
        Set<Operand> defSet = livenessAnalysis.getCurrentDef();
        assertTrue(defSet.isEmpty());
    }

    @Test
    @DisplayName("应正确访问帧槽节点")
    void testVisitFrameSlot() {
        // 创建帧槽节点
        FrameSlot frameSlot = new FrameSlot(5);
        
        // 执行访问
        Void result = livenessAnalysis.visit(frameSlot);
        
        // 验证结果
        assertNull(result); // visit方法应返回null
        
        // 验证use集合包含该帧槽
        Set<Operand> useSet = livenessAnalysis.getCurrentUse();
        assertEquals(1, useSet.size());
        assertTrue(useSet.contains(frameSlot));
        
        // 验证def集合为空
        Set<Operand> defSet = livenessAnalysis.getCurrentDef();
        assertTrue(defSet.isEmpty());
    }

    @Test
    @DisplayName("应正确访问常量值节点")
    void testVisitConstVal() {
        // 创建常量值节点
        ConstVal<Integer> constVal = ConstVal.valueOf(42);
        
        // 执行访问
        Void result = livenessAnalysis.visit(constVal);
        
        // 验证结果
        assertNull(result); // visit方法应返回null
        
        // 验证use和def集合都为空
        Set<Operand> useSet = livenessAnalysis.getCurrentUse();
        assertTrue(useSet.isEmpty());
        
        Set<Operand> defSet = livenessAnalysis.getCurrentDef();
        assertTrue(defSet.isEmpty());
    }

    @Test
    @DisplayName("应正确访问赋值语句节点")
    void testVisitAssign() {
        // 创建帧槽作为左值，常量作为右值
        FrameSlot slot1 = new FrameSlot(1);
        ConstVal<Integer> constVal = ConstVal.valueOf(100);
        
        // 创建赋值语句：slot1 = 100
        Assign assign = Assign.with(slot1, constVal);
        
        // 执行访问
        Void result = livenessAnalysis.visit(assign);
        
        // 验证结果
        assertNull(result);
        
        // 验证use集合包含右值（常量，但常量不计入use）
        Set<Operand> useSet = livenessAnalysis.getCurrentUse();
        assertTrue(useSet.isEmpty()); // 常量不计入use
        
        // 验证def集合包含左值
        Set<Operand> defSet = livenessAnalysis.getCurrentDef();
        assertEquals(1, defSet.size());
        assertTrue(defSet.contains(slot1));
    }

    @Test
    @DisplayName("应正确访问二元表达式节点")
    void testVisitBinExpr() {
        // 创建两个帧槽作为操作数
        FrameSlot slot1 = new FrameSlot(1);
        FrameSlot slot2 = new FrameSlot(2);
        
        // 创建二元表达式：slot1 + slot2
        BinExpr binExpr = new BinExpr(OperatorType.BinaryOpType.ADD, slot1, slot2);
        
        // 执行访问
        Void result = livenessAnalysis.visit(binExpr);
        
        // 验证结果
        assertNull(result);
        
        // 验证use集合包含两个操作数
        Set<Operand> useSet = livenessAnalysis.getCurrentUse();
        assertEquals(2, useSet.size());
        assertTrue(useSet.contains(slot1));
        assertTrue(useSet.contains(slot2));
        
        // 验证def集合为空
        Set<Operand> defSet = livenessAnalysis.getCurrentDef();
        assertTrue(defSet.isEmpty());
    }

    @Test
    @DisplayName("应正确访问一元表达式节点")
    void testVisitUnaryExpr() {
        // 创建帧槽作为操作数
        FrameSlot slot1 = new FrameSlot(1);
        
        // 创建一元表达式：-slot1
        UnaryExpr unaryExpr = new UnaryExpr(OperatorType.UnaryOpType.NEG, slot1);
        
        // 执行访问
        Void result = livenessAnalysis.visit(unaryExpr);
        
        // 验证结果
        assertNull(result);
        
        // 验证use集合包含操作数
        Set<Operand> useSet = livenessAnalysis.getCurrentUse();
        assertEquals(1, useSet.size());
        assertTrue(useSet.contains(slot1));
        
        // 验证def集合为空
        Set<Operand> defSet = livenessAnalysis.getCurrentDef();
        assertTrue(defSet.isEmpty());
    }

    @Test
    @DisplayName("应正确处理空指令序列")
    void testEmptyInstructions() {
        // 验证活性分析器可以处理空指令序列
        assertNotNull(livenessAnalysis);
    }
}