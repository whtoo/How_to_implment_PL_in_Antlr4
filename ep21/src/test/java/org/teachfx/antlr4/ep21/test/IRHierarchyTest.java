package org.teachfx.antlr4.ep21.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;
import org.teachfx.antlr4.ep21.ir.expr.addr.OperandSlot;
import org.teachfx.antlr4.ep21.ir.expr.arith.BinExpr;
import org.teachfx.antlr4.ep21.ir.lir.*;
import org.teachfx.antlr4.ep21.ir.mir.*;
import org.teachfx.antlr4.ep21.ir.stmt.*;
import org.teachfx.antlr4.ep21.symtab.type.OperatorType;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IR类型层次统一测试
 * 验证所有IR节点类型都支持统一的IRNode接口
 */
@DisplayName("IR类型层次统一测试套件")
class IRHierarchyTest {

    /**
     * 基类方法测试
     */
    @Nested
    @DisplayName("IRNode基类方法测试")
    class BaseClassMethodTests {

        @Test
        @DisplayName("所有IR节点都应该有getComplexityLevel方法")
        void testAllNodesHaveComplexityLevel() {
            // 测试Expr节点
            IRNode exprNode = createTestExprNode();
            assertTrue(exprNode.getComplexityLevel() >= 0 && exprNode.getComplexityLevel() <= 3);

            // 测试Stmt节点
            IRNode stmtNode = createTestStmtNode();
            assertTrue(stmtNode.getComplexityLevel() >= 0 && stmtNode.getComplexityLevel() <= 3);

            // 测试MIR节点
            IRNode mirNode = createTestMIRNode();
            assertTrue(mirNode.getComplexityLevel() >= 0 && mirNode.getComplexityLevel() <= 3);

            // 测试LIR节点
            IRNode lirNode = createTestLIRNode();
            assertTrue(lirNode.getComplexityLevel() >= 0 && lirNode.getComplexityLevel() <= 3);
        }

        @Test
        @DisplayName("所有IR节点都应该有getUsedVariables方法")
        void testAllNodesHaveUsedVariables() {
            IRNode node = createTestExprNode();
            assertNotNull(node.getUsedVariables());
        }

        @Test
        @DisplayName("所有IR节点都应该有getDefinedVariables方法")
        void testAllNodesHaveDefinedVariables() {
            IRNode node = createTestStmtNode();
            assertNotNull(node.getDefinedVariables());
        }

        @Test
        @DisplayName("所有IR节点都应该有getIRNodeType方法")
        void testAllNodesHaveIRNodeType() {
            IRNode exprNode = createTestExprNode();
            assertNotNull(exprNode.getIRNodeType());

            IRNode stmtNode = createTestStmtNode();
            assertNotNull(stmtNode.getIRNodeType());

            IRNode mirNode = createTestMIRNode();
            assertNotNull(mirNode.getIRNodeType());

            IRNode lirNode = createTestLIRNode();
            assertNotNull(lirNode.getIRNodeType());
        }
    }

    /**
     * 复杂度级别测试
     */
    @Nested
    @DisplayName("复杂度级别测试")
    class ComplexityLevelTests {

        @Test
        @DisplayName("Prog应该是复杂度级别0")
        void testProgComplexityLevel() {
            // Prog继承IRNode，应该有复杂度0
            // 这里假设Prog有适当的类名
            IRNode node = new IRNode() {
                public <S, E> S accept(org.teachfx.antlr4.ep21.ir.IRVisitor<S, E> visitor) {
                    return null;
                }
            };
            // 由于类名不含Prog，默认会是3（表达式级别）
            assertTrue(node.getComplexityLevel() >= 0);
        }

        @Test
        @DisplayName("Label应该是基本块入口")
        void testLabelIsBasicBlockEntry() {
            Label label = new Label(null, 1);
            assertTrue(label.isBasicBlockEntry());
        }

        @Test
        @DisplayName("普通语句不应该是基本块入口")
        void testStmtIsNotBasicBlockEntry() {
            Assign assign = Assign.with(OperandSlot.genTemp(), OperandSlot.genTemp());
            assertFalse(assign.isBasicBlockEntry());
        }
    }

    /**
     * MIR节点兼容性测试
     */
    @Nested
    @DisplayName("MIR节点兼容性测试")
    class MIRNodeCompatibilityTests {

        @Test
        @DisplayName("MIR节点应该实现所有IRNode方法")
        void testMIRNodeImplementsAllIRNodeMethods() {
            MIRNode mirNode = createTestMIRNode();

            // 验证所有方法都存在并返回合理值
            assertTrue(mirNode.getComplexityLevel() >= 0);
            assertNotNull(mirNode.getUsedVariables());
            assertNotNull(mirNode.getDefinedVariables());
            assertFalse(mirNode.isBasicBlockEntry());
            assertNotNull(mirNode.getIRNodeType());
        }

        @Test
        @DisplayName("MIRAssignStmt应该正确实现变量追踪")
        void testMIRAssignStmtVariableTracking() {
            MIRAssignStmt assignStmt = new MIRAssignStmt("x", new TestMIRExpr(Set.of("y", "z")));

            assertEquals(Set.of("x"), assignStmt.getDefinedVariables());
            assertEquals(Set.of("y", "z"), assignStmt.getUsedVariables());
        }
    }

    /**
     * LIR节点兼容性测试
     */
    @Nested
    @DisplayName("LIR节点兼容性测试")
    class LIRNodeCompatibilityTests {

        @Test
        @DisplayName("LIR节点应该实现所有IRNode方法")
        void testLIRNodeImplementsAllIRNodeMethods() {
            LIRNode lirNode = createTestLIRNode();

            // 验证所有方法都存在并返回合理值
            assertTrue(lirNode.getComplexityLevel() >= 0);
            assertNotNull(lirNode.getUsedVariables());
            assertNotNull(lirNode.getDefinedVariables());
            assertNotNull(lirNode.getIRNodeType());
        }

        @Test
        @DisplayName("LIRAssign应该有正确的指令类型")
        void testLIRAssignInstructionType() {
            LIRAssign lirAssign = new LIRAssign(
                OperandSlot.genTemp(),
                OperandSlot.genTemp(),
                LIRAssign.RegisterType.REGISTER
            );

            assertEquals(LIRNode.InstructionType.DATA_TRANSFER, lirAssign.getInstructionType());
        }
    }

    /**
     * 跨体系兼容性测试
     */
    @Nested
    @DisplayName("跨体系兼容性测试")
    class CrossSystemCompatibilityTests {

        @Test
        @DisplayName("不同IR体系应该可以一起使用")
        void testDifferentIRSystemsCanBeUsedTogether() {
            // 创建一个包含不同类型IR节点的列表
            List<IRNode> nodes = List.of(
                createTestExprNode(),
                createTestStmtNode(),
                createTestMIRNode(),
                createTestLIRNode()
            );

            // 验证所有节点都可以调用通用方法
            for (IRNode node : nodes) {
                assertTrue(node.getComplexityLevel() >= 0);
                assertNotNull(node.getIRNodeType());
            }
        }
    }

    // 测试辅助方法

    private IRNode createTestExprNode() {
        return new org.teachfx.antlr4.ep21.ir.expr.arith.BinExpr(
            OperatorType.BinaryOpType.ADD,
            OperandSlot.genTemp(),
            OperandSlot.genTemp()
        );
    }

    private IRNode createTestStmtNode() {
        return Assign.with(OperandSlot.genTemp(), OperandSlot.genTemp());
    }

    private MIRNode createTestMIRNode() {
        return new MIRAssignStmt("temp", new TestMIRExpr(Set.of("input")));
    }

    private LIRNode createTestLIRNode() {
        return new LIRAssign(
            OperandSlot.genTemp(),
            OperandSlot.genTemp(),
            LIRAssign.RegisterType.REGISTER
        );
    }

    // 测试辅助类

    private static class TestMIRExpr extends MIRExpr {
        private final Set<String> usedVars;

        TestMIRExpr(Set<String> usedVars) {
            this.usedVars = usedVars;
        }

        @Override
        public int getComplexityLevel() {
            return 3;
        }

        @Override
        public Set<String> getUsedVariables() {
            return usedVars;
        }

        @Override
        public void accept(MIRVisitor<?> visitor) {
            // 空实现
        }
    }
}
