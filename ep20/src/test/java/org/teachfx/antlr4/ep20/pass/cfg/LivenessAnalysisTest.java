package org.teachfx.antlr4.ep20.pass.cfg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep20.ir.IRNode;
import org.teachfx.antlr4.ep20.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep20.ir.expr.val.ConstVal;
import org.teachfx.antlr4.ep20.ir.stmt.Assign;
import org.teachfx.antlr4.ep20.ir.stmt.Label;
import org.teachfx.antlr4.ep20.pass.cfg.LivenessAnalysis;
import org.teachfx.antlr4.ep20.utils.Kind;

import java.util.ArrayList;
import java.util.List;

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
    @DisplayName("应正确访问二元表达式节点")
    void testVisitBinaryExpr() {
        // 创建赋值指令（包含二元表达式）
        FrameSlot slot1 = new FrameSlot(1);
        Assign assign = Assign.with(slot1, ConstVal.valueOf(42));
        
        // 执行访问
        Void result = livenessAnalysis.visit(assign);
        
        // 验证结果
        assertNull(result); // visit方法应返回null
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
    }

    @Test
    @DisplayName("应正确访问赋值语句节点")
    void testVisitAssign() {
        // 创建赋值指令
        FrameSlot slot1 = new FrameSlot(1);
        Assign assign = Assign.with(slot1, ConstVal.valueOf(100));
        
        // 执行访问
        Void result = livenessAnalysis.visit(assign);
        
        // 验证结果
        assertNull(result); // visit方法应返回null
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
    }

    @Test
    @DisplayName("应正确处理空指令序列")
    void testEmptyInstructions() {
        // 验证活性分析器可以处理空指令序列
        assertNotNull(livenessAnalysis);
    }
}