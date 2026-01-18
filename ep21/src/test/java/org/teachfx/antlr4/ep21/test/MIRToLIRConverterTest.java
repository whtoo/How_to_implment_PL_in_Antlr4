package org.teachfx.antlr4.ep21.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep21.ir.lir.*;
import org.teachfx.antlr4.ep21.ir.mir.*;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MIR到LIR转换器测试
 * 测试MIR节点正确转换为LIR指令
 */
@DisplayName("MIR到LIR转换器测试套件")
class MIRToLIRConverterTest {

    private MIRToLIRConverter converter;

    @BeforeEach
    void setUp() {
        converter = new MIRToLIRConverter();
    }

    /**
     * 基本转换测试
     */
    @Nested
    @DisplayName("基本转换测试")
    class BasicConversionTests {

        @Test
        @DisplayName("应该转换简单的MIR赋值语句")
        void testConvertSimpleAssignment() {
            // Given: MIR赋值语句 x = y
            MIRAssignStmt mirStmt = new MIRAssignStmt("x", new TestMIRExpr(Set.of("y")));

            // When: 转换为LIR
            LIRNode lirNode = converter.convertNode(mirStmt);

            // Then: 应该生成LIRAssign指令
            assertNotNull(lirNode);
            assertInstanceOf(LIRAssign.class, lirNode);

            LIRAssign lirAssign = (LIRAssign) lirNode;
            assertNotNull(lirAssign.getTarget());
            assertNotNull(lirAssign.getSource());
        }

        @Test
        @DisplayName("应该拒绝null输入")
        void testRejectNullInput() {
            assertThrows(IllegalArgumentException.class, () -> converter.convert(null));
            assertThrows(IllegalArgumentException.class, () -> converter.convertNode(null));
        }

        @Test
        @DisplayName("应该转换MIRFunction为LIR指令序列")
        void testConvertMIRFunction() {
            // Given: 包含多个语句的MIR函数
            MIRFunction mirFunction = new MIRFunction("testFunc");
            mirFunction.addStatement(new MIRAssignStmt("a", new TestMIRExpr(Set.of("x"))));
            mirFunction.addStatement(new MIRAssignStmt("b", new TestMIRExpr(Set.of("y"))));

            // When: 转换为LIR
            List<LIRNode> lirInstructions = converter.convert(mirFunction);

            // Then: 应该生成多条LIR指令
            assertNotNull(lirInstructions);
            assertEquals(2, lirInstructions.size());
            assertTrue(lirInstructions.get(0) instanceof LIRAssign);
            assertTrue(lirInstructions.get(1) instanceof LIRAssign);
        }
    }

    /**
     * 表达式转换测试
     */
    @Nested
    @DisplayName("表达式转换测试")
    class ExpressionConversionTests {

        @Test
        @DisplayName("应该正确处理单变量引用表达式")
        void testSingleVariableReference() {
            // Given: MIR赋值语句包含单变量引用
            MIRAssignStmt mirStmt = new MIRAssignStmt("x", new TestMIRExpr(Set.of("y")));

            // When: 转换为LIR
            LIRNode lirNode = converter.convertNode(mirStmt);

            // Then: 应该生成LIRAssign指令
            assertNotNull(lirNode);
            assertInstanceOf(LIRAssign.class, lirNode);
        }

        @Test
        @DisplayName("应该正确处理多变量引用的复杂表达式")
        void testComplexExpression() {
            // Given: MIR赋值语句包含多变量引用
            MIRAssignStmt mirStmt = new MIRAssignStmt("result", new TestMIRExpr(Set.of("x", "y", "z")));

            // When: 转换为LIR
            LIRNode lirNode = converter.convertNode(mirStmt);

            // Then: 应该生成LIR节点
            assertNotNull(lirNode);
            assertInstanceOf(LIRAssign.class, lirNode);
        }
    }

    /**
     * LIR指令类型验证测试
     */
    @Nested
    @DisplayName("LIR指令类型验证测试")
    class LIRInstructionTypeTests {

        @Test
        @DisplayName("生成的LIRAssign应该有正确的指令类型")
        void testLIRAssignInstructionType() {
            // Given
            MIRAssignStmt mirStmt = new MIRAssignStmt("result", new TestMIRExpr(Set.of("input")));

            // When
            LIRNode lirNode = converter.convertNode(mirStmt);

            // Then
            if (lirNode instanceof LIRAssign assign) {
                assertEquals(LIRNode.InstructionType.DATA_TRANSFER, assign.getInstructionType());
            }
        }
    }

    /**
     * 错误处理测试
     */
    @Nested
    @DisplayName("错误处理测试")
    class ErrorHandlingTests {

        @Test
        @DisplayName("应该处理空函数")
        void testEmptyFunction() {
            // Given: 没有语句的MIR函数
            MIRFunction emptyFunction = new MIRFunction("empty");

            // When: 转换为LIR
            List<LIRNode> lirInstructions = converter.convert(emptyFunction);

            // Then: 应该生成空列表
            assertNotNull(lirInstructions);
            assertTrue(lirInstructions.isEmpty());
        }

        @Test
        @DisplayName("应该处理不支持的MIR节点类型")
        void testUnsupportedMIRNodeType() {
            // Given: 不支持的自定义MIR节点
            MIRNode unsupportedNode = new MIRNode() {
                @Override
                public int getComplexityLevel() {
                    return 2;
                }

                @Override
                public Set<String> getUsedVariables() {
                    return Set.of();
                }

                @Override
                public Set<String> getDefinedVariables() {
                    return Set.of();
                }

                @Override
                public void accept(MIRVisitor<?> visitor) {
                    visitor.visit(this);
                }
            };

            // When & Then: 应该抛出异常
            assertThrows(UnsupportedOperationException.class, () -> converter.convertNode(unsupportedNode));
        }
    }

    /**
     * 上下文管理测试
     */
    @Nested
    @DisplayName("转换上下文测试")
    class ContextTests {

        @Test
        @DisplayName("多次转换应该独立工作")
        void testMultipleIndependentConversions() {
            // Given: 第一次转换
            MIRFunction func1 = new MIRFunction("func1");
            func1.addStatement(new MIRAssignStmt("a", new TestMIRExpr(Set.of("x"))));
            List<LIRNode> result1 = converter.convert(func1);

            // Given: 第二次转换
            MIRFunction func2 = new MIRFunction("func2");
            func2.addStatement(new MIRAssignStmt("b", new TestMIRExpr(Set.of("y"))));
            List<LIRNode> result2 = converter.convert(func2);

            // Then: 两次转换应该独立
            assertEquals(1, result1.size());
            assertEquals(1, result2.size());
            assertNotSame(result1.get(0), result2.get(0));
        }
    }

    /**
     * 测试辅助类
     */
    private static class TestMIRExpr extends MIRExpr {
        private final Set<String> usedVars;

        TestMIRExpr(Set<String> usedVars) {
            this.usedVars = usedVars != null ? usedVars : Set.of();
        }

        @Override
        public Set<String> getUsedVariables() {
            return usedVars;
        }

        @Override
        public void accept(MIRVisitor<?> visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return "TestMIRExpr" + usedVars;
        }
    }

    /**
     * MIRFunction变量信息缓存测试
     * 验证缓存机制的正确性和失效逻辑
     */
    @Nested
    @DisplayName("MIRFunction缓存机制测试")
    class MIRFunctionCacheTests {

        @Test
        @DisplayName("初始调用应该计算变量信息")
        void testFirstCallComputesVariables() {
            MIRFunction func = new MIRFunction("test");
            func.addStatement(new MIRAssignStmt("a", createMockMIRExpr(Set.of("x"))));
            func.addStatement(new MIRAssignStmt("b", createMockMIRExpr(Set.of("y"))));

            Set<String> used = func.getUsedVariables();
            Set<String> defined = func.getDefinedVariables();

            assertTrue(used.contains("x"));
            assertTrue(used.contains("y"));
            assertTrue(defined.contains("a"));
            assertTrue(defined.contains("b"));
        }

        @Test
        @DisplayName("后续调用应该使用缓存")
        void testSubsequentCallsUseCache() {
            MIRFunction func = new MIRFunction("test");
            func.addStatement(new MIRAssignStmt("a", createMockMIRExpr(Set.of("x"))));

            Set<String> firstCall = func.getUsedVariables();
            Set<String> secondCall = func.getUsedVariables();

            assertSame(firstCall, secondCall, "Subsequent calls should return cached set");
        }

        @Test
        @DisplayName("添加语句后应该使缓存失效")
        void testAddingStatementInvalidatesCache() {
            MIRFunction func = new MIRFunction("test");
            func.addStatement(new MIRAssignStmt("a", createMockMIRExpr(Set.of("x"))));

            Set<String> cachedSet = func.getUsedVariables();
            assertSame(cachedSet, func.getUsedVariables());

            func.addStatement(new MIRAssignStmt("b", createMockMIRExpr(Set.of("y"))));

            Set<String> newSet = func.getUsedVariables();
            assertNotSame(cachedSet, newSet, "Cache should be invalidated after adding statement");
            assertEquals(2, newSet.size());
        }

        @Test
        @DisplayName("添加参数后应该使缓存失效")
        void testAddingParameterInvalidatesCache() {
            MIRFunction func = new MIRFunction("test");
            func.addStatement(new MIRAssignStmt("a", createMockMIRExpr(Set.of("x"))));

            Set<String> cachedSet = func.getUsedVariables();

            func.addParameter("p1");

            Set<String> newSet = func.getUsedVariables();
            assertNotSame(cachedSet, newSet);
            assertTrue(newSet.contains("p1"));
        }

        @Test
        @DisplayName("多次修改后缓存应该保持一致性")
        void testCacheConsistencyAfterMultipleModifications() {
            MIRFunction func = new MIRFunction("test");

            Set<String> result1 = func.getUsedVariables();
            Set<String> defined1 = func.getDefinedVariables();

            func.addStatement(new MIRAssignStmt("a", createMockMIRExpr(Set.of("x"))));

            Set<String> result2 = func.getUsedVariables();
            Set<String> defined2 = func.getDefinedVariables();

            func.addLocalVariable("local1");

            Set<String> result3 = func.getUsedVariables();
            Set<String> defined3 = func.getDefinedVariables();

            assertNotSame(result1, result2);
            assertNotSame(result2, result3);
            assertEquals(1, result2.size());
            assertEquals(2, defined3.size()); // a + local1
        }
    }

    /**
     * 辅助方法：创建模拟MIRExpr
     */
    private MIRExpr createMockMIRExpr(Set<String> usedVars) {
        return new MIRExpr() {
            @Override
            public Set<String> getUsedVariables() {
                return usedVars != null ? usedVars : Set.of();
            }

            @Override
            public void accept(MIRVisitor<?> visitor) {
                visitor.visit(this);
            }
        };
    }
}
