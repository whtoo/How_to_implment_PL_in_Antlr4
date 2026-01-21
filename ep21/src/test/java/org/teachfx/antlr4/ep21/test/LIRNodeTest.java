package org.teachfx.antlr4.ep21.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.lir.LIRAssign;
import org.teachfx.antlr4.ep21.ir.lir.LIRNode;
import org.teachfx.antlr4.ep21.ir.IRVisitor;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LIRNode基类测试
 * 测试LIR节点的通用行为和抽象方法
 */
@DisplayName("LIRNode基类测试套件")
class LIRNodeTest {

    /**
     * 测试LIRNode的抽象方法契约
     */
    @Nested
    @DisplayName("抽象方法契约测试")
    class AbstractMethodContracts {

        @Test
        @DisplayName("所有具体LIRNode子类必须实现getInstructionType")
        void testGetInstructionTypeImplementation() {
            LIRNode node = new LIRNode() {
                @Override
                public InstructionType getInstructionType() {
                    return InstructionType.DATA_TRANSFER;
                }

                @Override
                public int getCost() {
                    return 1;
                }

                @Override
                public <S, E> S accept(IRVisitor<S, E> visitor) {
                    return null;
                }
            };

            assertEquals(LIRNode.InstructionType.DATA_TRANSFER, node.getInstructionType());
        }

        @Test
        @DisplayName("所有具体LIRNode子类必须实现getCost")
        void testGetCostImplementation() {
            LIRNode node = new LIRNode() {
                @Override
                public InstructionType getInstructionType() {
                    return InstructionType.ARITHMETIC;
                }

                @Override
                public int getCost() {
                    return 5;
                }

                @Override
                public <S, E> S accept(IRVisitor<S, E> visitor) {
                    return null;
                }
            };

            assertEquals(5, node.getCost());
        }

        @Test
        @DisplayName("所有具体LIRNode子类必须实现accept方法")
        void testAcceptMethodImplementation() {
            LIRNode node = new LIRNode() {
                @Override
                public InstructionType getInstructionType() {
                    return InstructionType.CONTROL_FLOW;
                }

                @Override
                public int getCost() {
                    return 2;
                }

                @Override
                public <S, E> S accept(IRVisitor<S, E> visitor) {
                    return null;
                }
            };

            assertDoesNotThrow(() -> node.accept(new IRVisitor<Object, Object>() {
                @Override public Object visit(org.teachfx.antlr4.ep21.ir.expr.arith.BinExpr node) { return null; }
                @Override public Object visit(org.teachfx.antlr4.ep21.ir.expr.arith.UnaryExpr node) { return null; }
                @Override public Object visit(org.teachfx.antlr4.ep21.ir.expr.CallFunc node) { return null; }
                @Override public Object visit(org.teachfx.antlr4.ep21.ir.stmt.Label node) { return null; }
                @Override public Object visit(org.teachfx.antlr4.ep21.ir.stmt.JMP node) { return null; }
                @Override public Object visit(org.teachfx.antlr4.ep21.ir.stmt.CJMP node) { return null; }
                @Override public Object visit(org.teachfx.antlr4.ep21.ir.stmt.Assign node) { return null; }
                @Override public Object visit(org.teachfx.antlr4.ep21.ir.stmt.ReturnVal node) { return null; }
                @Override public Object visit(org.teachfx.antlr4.ep21.ir.expr.addr.OperandSlot node) { return null; }
                 @Override public Object visit(org.teachfx.antlr4.ep21.ir.expr.addr.FrameSlot node) { return null; }
                 @Override public <T> Object visit(org.teachfx.antlr4.ep21.ir.expr.val.ConstVal<T> node) { return null; }
                 @Override public Object visit(org.teachfx.antlr4.ep21.ir.expr.ArrayAccess node) { return null; }
                 @Override public Object visit(org.teachfx.antlr4.ep21.ir.stmt.ArrayAssign node) { return null; }
                 @Override public Object visit(org.teachfx.antlr4.ep21.ir.lir.LIRArrayInit node) { return null; }
                 @Override public Object visit(org.teachfx.antlr4.ep21.ir.lir.LIRNewArray node) { return null; }
                 @Override public Object visit(org.teachfx.antlr4.ep21.ir.lir.LIRArrayLoad node) { return null; }
                 @Override public Object visit(org.teachfx.antlr4.ep21.ir.lir.LIRArrayStore node) { return null; }
               }));
        }
    }

    /**
     * 测试LIRNode的默认方法实现
     */
    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("默认hasMemoryAccess应该返回false")
        void testHasMemoryAccessDefault() {
            LIRNode node = new LIRNode() {
                @Override
                public InstructionType getInstructionType() {
                    return InstructionType.DATA_TRANSFER;
                }

                @Override
                public int getCost() {
                    return 1;
                }

                @Override
                public <S, E> S accept(IRVisitor<S, E> visitor) {
                    return null;
                }
            };

            assertFalse(node.hasMemoryAccess());
        }

        @Test
        @DisplayName("默认hasRegisterOperation应该返回false")
        void testHasRegisterOperationDefault() {
            LIRNode node = new LIRNode() {
                @Override
                public InstructionType getInstructionType() {
                    return InstructionType.ARITHMETIC;
                }

                @Override
                public int getCost() {
                    return 3;
                }

                @Override
                public <S, E> S accept(IRVisitor<S, E> visitor) {
                    return null;
                }
            };

            assertFalse(node.hasRegisterOperation());
        }

        @Test
        @DisplayName("具体子类可以覆盖默认方法")
        void testOverrideDefaultMethods() {
            // LIRAssign覆盖了这些方法
            Operand mockTarget = new Operand() {
                @Override
                public <S, E> E accept(IRVisitor<S, E> visitor) {
                    return null;
                }

                @Override
                public String toString() {
                    return "t0";
                }
            };

            Operand mockSource = new Operand() {
                @Override
                public <S, E> E accept(IRVisitor<S, E> visitor) {
                    return null;
                }

                @Override
                public String toString() {
                    return "1";
                }
            };

            LIRAssign registerAssign = new LIRAssign(mockTarget, mockSource, LIRAssign.RegisterType.REGISTER);
            assertTrue(registerAssign.hasRegisterOperation());
            assertFalse(registerAssign.hasMemoryAccess());

            LIRAssign memoryAssign = new LIRAssign(mockTarget, mockSource, LIRAssign.RegisterType.MEMORY);
            assertTrue(memoryAssign.hasMemoryAccess());
            assertFalse(memoryAssign.hasRegisterOperation());

            LIRAssign immediateAssign = new LIRAssign(mockTarget, mockSource, LIRAssign.RegisterType.IMMEDIATE);
            assertFalse(immediateAssign.hasMemoryAccess());
            assertFalse(immediateAssign.hasRegisterOperation());
        }
    }

    /**
     * 测试LIRNode.InstructionType枚举
     */
    @Nested
    @DisplayName("指令类型枚举测试")
    class InstructionTypeEnumTests {

        @Test
        @DisplayName("指令类型枚举应该包含所有预期值")
        void testEnumValues() {
            LIRNode.InstructionType[] values = LIRNode.InstructionType.values();

            assertEquals(7, values.length);
            assertArrayEquals(new LIRNode.InstructionType[]{
                LIRNode.InstructionType.DATA_TRANSFER,
                LIRNode.InstructionType.ARITHMETIC,
                LIRNode.InstructionType.LOGICAL,
                LIRNode.InstructionType.CONTROL_FLOW,
                LIRNode.InstructionType.FUNCTION_CALL,
                LIRNode.InstructionType.MEMORY_ACCESS,
                LIRNode.InstructionType.COMPARE
            }, values);
        }

        @ParameterizedTest
        @EnumSource(LIRNode.InstructionType.class)
        @DisplayName("所有指令类型都应该有合理的字符串表示")
        void testEnumToString(LIRNode.InstructionType type) {
            assertNotNull(type.name());
            assertFalse(type.name().isEmpty());
        }

        @Test
        @DisplayName("枚举值应该可以通过名称查找")
        void testEnumValueOf() {
            for (LIRNode.InstructionType type : LIRNode.InstructionType.values()) {
                assertEquals(type, LIRNode.InstructionType.valueOf(type.name()));
            }
        }
    }

    /**
     * 测试LIRNode的成本模型
     */
    @Nested
    @DisplayName("成本模型测试")
    class CostModelTests {

        @Test
        @DisplayName("成本应该是非负整数")
        void testCostNonNegative() {
            LIRNode node = new LIRNode() {
                @Override
                public InstructionType getInstructionType() {
                    return InstructionType.DATA_TRANSFER;
                }

                @Override
                public int getCost() {
                    return 0; // 最小成本
                }

                @Override
                public <S, E> S accept(IRVisitor<S, E> visitor) {
                    return null;
                }
            };

            assertTrue(node.getCost() >= 0);
        }

        @Test
        @DisplayName("不同类型指令应该有不同成本")
        void testDifferentCostsForDifferentTypes() {
            // 创建不同成本的节点
            LIRNode lowCostNode = new LIRNode() {
                @Override
                public InstructionType getInstructionType() {
                    return InstructionType.DATA_TRANSFER;
                }

                @Override
                public int getCost() {
                    return 1;
                }

                @Override
                public <S, E> S accept(IRVisitor<S, E> visitor) {
                    return null;
                }
            };

            LIRNode highCostNode = new LIRNode() {
                @Override
                public InstructionType getInstructionType() {
                    return InstructionType.MEMORY_ACCESS;
                }

                @Override
                public int getCost() {
                    return 5;
                }

                @Override
                public <S, E> S accept(IRVisitor<S, E> visitor) {
                    return null;
                }
            };

            assertTrue(highCostNode.getCost() > lowCostNode.getCost());
        }
    }

    /**
     * 测试LIRNode的继承关系
     */
    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("LIRNode应该继承IRNode")
        void testExtendsIRNode() {
            LIRNode node = new LIRNode() {
                @Override
                public InstructionType getInstructionType() {
                    return InstructionType.LOGICAL;
                }

                @Override
                public int getCost() {
                    return 2;
                }

                @Override
                public <S, E> S accept(IRVisitor<S, E> visitor) {
                    return null;
                }
            };

            assertTrue(node instanceof org.teachfx.antlr4.ep21.ir.IRNode);
        }

        @Test
        @DisplayName("LIRAssign应该继承LIRNode")
        void testLIRAssignInheritance() {
            Operand mockTarget = new Operand() {
                @Override
                public <S, E> E accept(IRVisitor<S, E> visitor) {
                    return null;
                }

                @Override
                public String toString() {
                    return "t0";
                }
            };

            Operand mockSource = new Operand() {
                @Override
                public <S, E> E accept(IRVisitor<S, E> visitor) {
                    return null;
                }

                @Override
                public String toString() {
                    return "1";
                }
            };

            LIRAssign assign = new LIRAssign(mockTarget, mockSource, LIRAssign.RegisterType.REGISTER);
            assertTrue(assign instanceof LIRNode);
        }
    }

    /**
     * 测试LIRNode的边界条件
     */
    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("成本为0的指令应该被允许")
        void testZeroCost() {
            LIRNode node = new LIRNode() {
                @Override
                public InstructionType getInstructionType() {
                    return InstructionType.DATA_TRANSFER;
                }

                @Override
                public int getCost() {
                    return 0;
                }

                @Override
                public <S, E> S accept(IRVisitor<S, E> visitor) {
                    return null;
                }
            };

            assertEquals(0, node.getCost());
        }

        @Test
        @DisplayName("高成本指令应该被允许")
        void testHighCost() {
            LIRNode node = new LIRNode() {
                @Override
                public InstructionType getInstructionType() {
                    return InstructionType.FUNCTION_CALL;
                }

                @Override
                public int getCost() {
                    return Integer.MAX_VALUE;
                }

                @Override
                public <S, E> S accept(IRVisitor<S, E> visitor) {
                    return null;
                }
            };

            assertEquals(Integer.MAX_VALUE, node.getCost());
        }
    }
}