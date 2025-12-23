package org.teachfx.antlr4.ep21.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.expr.addr.OperandSlot;
import org.teachfx.antlr4.ep21.ir.lir.*;
import org.teachfx.antlr4.ep21.symtab.type.OperatorType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LIR指令测试套件
 * 测试所有LIR指令的正确行为
 */
@DisplayName("LIR指令测试套件")
class LIRInstructionTest {

    /**
     * LIRBinaryOp测试
     */
    @Nested
    @DisplayName("LIRBinaryOp二元运算指令测试")
    class LIRBinaryOpTests {

        @Test
        @DisplayName("应该正确创建LIRBinaryOp指令")
        void testLIRBinaryOpCreation() {
            Operand left = OperandSlot.genTemp();
            Operand right = OperandSlot.genTemp();
            Operand result = OperandSlot.genTemp();
            OperatorType.BinaryOpType opType = OperatorType.BinaryOpType.ADD;

            LIRBinaryOp instr = new LIRBinaryOp(opType, left, right, result);

            assertEquals(opType, instr.getOpType());
            assertEquals(left, instr.getLeft());
            assertEquals(right, instr.getRight());
            assertEquals(result, instr.getResult());
        }

        @Test
        @DisplayName("二元运算指令类型应该是ARITHMETIC")
        void testInstructionType() {
            LIRBinaryOp instr = new LIRBinaryOp(
                OperatorType.BinaryOpType.MUL,
                OperandSlot.genTemp(),
                OperandSlot.genTemp(),
                OperandSlot.genTemp()
            );

            assertEquals(LIRNode.InstructionType.ARITHMETIC, instr.getInstructionType());
        }

        @ParameterizedTest
        @EnumSource(OperatorType.BinaryOpType.class)
        @DisplayName("应该正确计算二元运算成本")
        void testCostCalculation(OperatorType.BinaryOpType opType) {
            LIRBinaryOp instr = new LIRBinaryOp(
                opType,
                OperandSlot.genTemp(),
                OperandSlot.genTemp(),
                OperandSlot.genTemp()
            );

            int cost = instr.getCost();
            assertTrue(cost >= 1, "cost should be at least 1, got: " + cost);
            assertTrue(cost <= 3, "cost should be at most 3, got: " + cost);
        }

        @Test
        @DisplayName("应该拒绝null操作数")
        void testRejectNullOperands() {
            assertThrows(NullPointerException.class, () ->
                new LIRBinaryOp(null, OperandSlot.genTemp(), OperandSlot.genTemp(), OperandSlot.genTemp())
            );
            assertThrows(NullPointerException.class, () ->
                new LIRBinaryOp(OperatorType.BinaryOpType.ADD, null, OperandSlot.genTemp(), OperandSlot.genTemp())
            );
            assertThrows(NullPointerException.class, () ->
                new LIRBinaryOp(OperatorType.BinaryOpType.ADD, OperandSlot.genTemp(), null, OperandSlot.genTemp())
            );
            assertThrows(NullPointerException.class, () ->
                new LIRBinaryOp(OperatorType.BinaryOpType.ADD, OperandSlot.genTemp(), OperandSlot.genTemp(), null)
            );
        }

        @Test
        @DisplayName("toString应该返回正确的字符串表示")
        void testToString() {
            Operand left = new TestOperand("x");
            Operand right = new TestOperand("y");
            Operand result = new TestOperand("z");

            LIRBinaryOp instr = new LIRBinaryOp(OperatorType.BinaryOpType.ADD, left, right, result);

            String str = instr.toString();
            assertTrue(str.contains("z"));
            assertTrue(str.contains("x"));
            assertTrue(str.contains("y"));
            assertTrue(str.contains("ADD") || str.contains("+"));
        }
    }

    /**
     * LIRUnaryOp测试
     */
    @Nested
    @DisplayName("LIRUnaryOp一元运算指令测试")
    class LIRUnaryOpTests {

        @Test
        @DisplayName("应该正确创建LIRUnaryOp指令")
        void testLIRUnaryOpCreation() {
            Operand operand = OperandSlot.genTemp();
            Operand result = OperandSlot.genTemp();
            OperatorType.UnaryOpType opType = OperatorType.UnaryOpType.NEG;

            LIRUnaryOp instr = new LIRUnaryOp(opType, operand, result);

            assertEquals(opType, instr.getOpType());
            assertEquals(operand, instr.getOperand());
            assertEquals(result, instr.getResult());
        }

        @Test
        @DisplayName("一元运算指令类型应该是LOGICAL")
        void testInstructionType() {
            LIRUnaryOp instr = new LIRUnaryOp(
                OperatorType.UnaryOpType.NOT,
                OperandSlot.genTemp(),
                OperandSlot.genTemp()
            );

            assertEquals(LIRNode.InstructionType.LOGICAL, instr.getInstructionType());
        }

        @Test
        @DisplayName("一元运算成本应该为1")
        void testCost() {
            LIRUnaryOp instr = new LIRUnaryOp(
                OperatorType.UnaryOpType.NEG,
                OperandSlot.genTemp(),
                OperandSlot.genTemp()
            );

            assertEquals(1, instr.getCost());
        }
    }

    /**
     * LIRCall测试
     */
    @Nested
    @DisplayName("LIRCall函数调用指令测试")
    class LIRCallTests {

        @Test
        @DisplayName("应该正确创建LIRCall指令")
        void testLIRCallCreation() {
            String funcName = "testFunction";
            Operand arg1 = OperandSlot.genTemp();
            Operand arg2 = OperandSlot.genTemp();
            Operand result = OperandSlot.genTemp();

            LIRCall instr = new LIRCall(funcName, List.of(arg1, arg2), result);

            assertEquals(funcName, instr.getFunctionName());
            assertEquals(2, instr.getArguments().size());
            assertEquals(result, instr.getResult());
        }

        @Test
        @DisplayName("void函数调用result可以为null")
        void testVoidFunctionCall() {
            LIRCall instr = new LIRCall("voidFunc", List.of(), null);

            assertEquals("voidFunc", instr.getFunctionName());
            assertTrue(instr.getArguments().isEmpty());
            assertNull(instr.getResult());
        }

        @Test
        @DisplayName("函数调用指令类型应该是FUNCTION_CALL")
        void testInstructionType() {
            LIRCall instr = new LIRCall("test", List.of(OperandSlot.genTemp()), OperandSlot.genTemp());

            assertEquals(LIRNode.InstructionType.FUNCTION_CALL, instr.getInstructionType());
        }

        @Test
        @DisplayName("函数调用成本应该包含基础成本和参数成本")
        void testCost() {
            // 基础成本10 + 参数数量
            LIRCall instr = new LIRCall("test", List.of(
                OperandSlot.genTemp(),
                OperandSlot.genTemp(),
                OperandSlot.genTemp()
            ), OperandSlot.genTemp());

            assertEquals(13, instr.getCost()); // 10 + 3参数
        }

        @Test
        @DisplayName("应该拒绝空函数名")
        void testRejectEmptyFunctionName() {
            assertThrows(IllegalArgumentException.class, () ->
                new LIRCall("", List.of(), OperandSlot.genTemp())
            );
            assertThrows(IllegalArgumentException.class, () ->
                new LIRCall(null, List.of(), OperandSlot.genTemp())
            );
        }
    }

    /**
     * LIRJump测试
     */
    @Nested
    @DisplayName("LIRJump无条件跳转指令测试")
    class LIRJumpTests {

        @Test
        @DisplayName("应该正确创建LIRJump指令")
        void testLIRJumpCreation() {
            String targetLabel = "label_target";

            LIRJump instr = new LIRJump(targetLabel);

            assertEquals(targetLabel, instr.getTargetLabel());
        }

        @Test
        @DisplayName("跳转指令类型应该是CONTROL_FLOW")
        void testInstructionType() {
            LIRJump instr = new LIRJump("target");

            assertEquals(LIRNode.InstructionType.CONTROL_FLOW, instr.getInstructionType());
        }

        @Test
        @DisplayName("跳转指令成本应该为1")
        void testCost() {
            LIRJump instr = new LIRJump("target");

            assertEquals(1, instr.getCost());
        }

        @Test
        @DisplayName("toString应该包含jmp关键字")
        void testToString() {
            LIRJump instr = new LIRJump("loop_start");

            String str = instr.toString();
            assertTrue(str.contains("jmp"));
            assertTrue(str.contains("loop_start"));
        }
    }

    /**
     * LIRCondJump测试
     */
    @Nested
    @DisplayName("LIRCondJump条件跳转指令测试")
    class LIRCondJumpTests {

        @Test
        @DisplayName("应该正确创建LIRCondJump指令")
        void testLIRCondJumpCreation() {
            Operand condition = OperandSlot.genTemp();
            String trueLabel = "then_block";
            String falseLabel = "else_block";

            LIRCondJump instr = new LIRCondJump(condition, trueLabel, falseLabel);

            assertEquals(condition, instr.getCondition());
            assertEquals(trueLabel, instr.getTrueLabel());
            assertEquals(falseLabel, instr.getFalseLabel());
        }

        @Test
        @DisplayName("条件跳转指令类型应该是CONTROL_FLOW")
        void testInstructionType() {
            LIRCondJump instr = new LIRCondJump(
                OperandSlot.genTemp(),
                "then",
                "else"
            );

            assertEquals(LIRNode.InstructionType.CONTROL_FLOW, instr.getInstructionType());
        }

        @Test
        @DisplayName("条件跳转指令成本应该为2")
        void testCost() {
            LIRCondJump instr = new LIRCondJump(
                OperandSlot.genTemp(),
                "then",
                "else"
            );

            assertEquals(2, instr.getCost());
        }

        @Test
        @DisplayName("应该拒绝null条件")
        void testRejectNullCondition() {
            assertThrows(NullPointerException.class, () ->
                new LIRCondJump(null, "then", "else")
            );
        }
    }

    /**
     * LIRReturn测试
     */
    @Nested
    @DisplayName("LIRReturn返回指令测试")
    class LIRReturnTests {

        @Test
        @DisplayName("应该正确创建带返回值的LIRReturn指令")
        void testLIRReturnWithValue() {
            Operand returnValue = OperandSlot.genTemp();

            LIRReturn instr = new LIRReturn(returnValue);

            assertEquals(returnValue, instr.getReturnValue());
        }

        @Test
        @DisplayName("应该正确创建void返回的LIRReturn指令")
        void testLIRReturnVoid() {
            LIRReturn instr = new LIRReturn(null);

            assertNull(instr.getReturnValue());
        }

        @Test
        @DisplayName("返回指令类型应该是CONTROL_FLOW")
        void testInstructionType() {
            LIRReturn instr = new LIRReturn(OperandSlot.genTemp());

            assertEquals(LIRNode.InstructionType.CONTROL_FLOW, instr.getInstructionType());
        }

        @Test
        @DisplayName("返回指令成本应该为5")
        void testCost() {
            LIRReturn instr = new LIRReturn(OperandSlot.genTemp());

            assertEquals(5, instr.getCost());
        }

        @Test
        @DisplayName("toString应该包含return关键字")
        void testToString() {
            Operand retValue = new TestOperand("result");
            LIRReturn instr = new LIRReturn(retValue);

            String str = instr.toString();
            assertTrue(str.contains("return"));
            assertTrue(str.contains("result"));
        }
    }

    /**
     * 测试辅助类
     */
    private static class TestOperand extends Operand {
        private final String name;

        TestOperand(String name) {
            this.name = name;
        }

        @Override
        public <S, E> E accept(org.teachfx.antlr4.ep21.ir.IRVisitor<S, E> visitor) {
            return null;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
