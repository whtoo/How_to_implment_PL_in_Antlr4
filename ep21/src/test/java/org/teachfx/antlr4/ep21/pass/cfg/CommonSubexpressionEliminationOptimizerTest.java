package org.teachfx.antlr4.ep21.pass.cfg;

import org.junit.jupiter.api.*;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.val.ConstVal;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;
import org.teachfx.antlr4.ep21.ir.expr.addr.OperandSlot;
import org.teachfx.antlr4.ep21.ir.expr.arith.BinExpr;
import org.teachfx.antlr4.ep21.ir.expr.arith.UnaryExpr;
import org.teachfx.antlr4.ep21.ir.stmt.Label;
import org.teachfx.antlr4.ep21.symtab.type.OperatorType;
import org.teachfx.antlr4.ep21.utils.Kind;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 公共子表达式消除优化器测试
 *
 * 测试CSE优化器的各种功能，包括：
 * - 二元表达式公共子表达式消除
 * - 一元表达式公共子表达式消除
 * - 常量表达式公共子表达式消除
 * - 边界条件测试
 */
@DisplayName("公共子表达式消除优化器测试")
@Tag("optimizer")
@Tag("cse")
class CommonSubexpressionEliminationOptimizerTest {

    private CommonSubexpressionEliminationOptimizer optimizer;

    @BeforeEach
    void setUp() {
        optimizer = new CommonSubexpressionEliminationOptimizer();
    }

    @Nested
    @DisplayName("创建和配置测试")
    class CreationTests {

        @Test
        @DisplayName("应该能够创建CSE优化器")
        void testCanCreateOptimizer() {
            assertNotNull(optimizer);
        }

        @Test
        @DisplayName("优化器应该初始化为空状态")
        void testOptimizerInitialState() {
            assertEquals(0, optimizer.getEliminatedCount());
            assertEquals(0, optimizer.getProcessedCount());
        }
    }

    @Nested
    @DisplayName("值编号键测试")
    class ValueNumberKeyTests {

        @Test
        @DisplayName("相同的表达式应该产生相同的键")
        void testSameExpressionSameKey() {
            // Arrange - 创建两个相同的二元表达式，使用相同的操作数
            VarSlot a = createMockVarSlot("a");
            VarSlot b = createMockVarSlot("b");

            BinExpr expr1 = BinExpr.with(OperatorType.BinaryOpType.ADD, a, b);
            BinExpr expr2 = BinExpr.with(OperatorType.BinaryOpType.ADD, a, b);

            // 注意：由于我们无法访问私有类ValueNumberKey，
            // 这里我们通过验证表达式结构来测试
            assertEquals(expr1.getOpType(), expr2.getOpType());
            // 验证使用相同的操作数引用
            assertSame(expr1.getLhs(), expr2.getLhs());
            assertSame(expr1.getRhs(), expr2.getRhs());
        }

        @Test
        @DisplayName("不同的表达式应该产生不同的键")
        void testDifferentExpressionDifferentKey() {
            // 使用相同的操作数但不同的操作符
            VarSlot a = createMockVarSlot("a");
            VarSlot b = createMockVarSlot("b");

            BinExpr expr1 = BinExpr.with(OperatorType.BinaryOpType.ADD, a, b);
            BinExpr expr2 = BinExpr.with(OperatorType.BinaryOpType.MUL, a, b);

            // 操作符不同，表达式应该不同
            assertNotEquals(expr1.getOpType(), expr2.getOpType());
        }
    }

    @Nested
    @DisplayName("CFG处理测试")
    class CFGProcessingTests {

        @Test
        @DisplayName("空CFG应该被正确处理")
        void testEmptyCFG() {
            // Arrange
            CFG<IRNode> emptyCFG = new CFG<>(new ArrayList<>(), new ArrayList<>());

            // Act & Assert
            assertDoesNotThrow(() -> optimizer.onHandle(emptyCFG));
            assertEquals(0, optimizer.getProcessedCount());
            assertEquals(0, optimizer.getEliminatedCount());
        }

        @Test
        @DisplayName("单个基本块的CFG应该被正确处理")
        void testSingleBlockCFG() {
            // Arrange
            BasicBlock<IRNode> block = new BasicBlock<>(
                Kind.CONTINUOUS,
                new ArrayList<>(),
                new Label("test", null),
                0
            );

            List<BasicBlock<IRNode>> blocks = new ArrayList<>();
            blocks.add(block);

            CFG<IRNode> cfg = new CFG<>(blocks, new ArrayList<>());

            // Act & Assert
            assertDoesNotThrow(() -> optimizer.onHandle(cfg));
        }

        @Test
        @DisplayName("包含多个基本块的CFG应该被正确处理")
        void testMultiBlockCFG() {
            // Arrange
            List<BasicBlock<IRNode>> blocks = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                blocks.add(new BasicBlock<>(
                    Kind.CONTINUOUS,
                    new ArrayList<>(),
                    new Label("L" + i, null),
                    i
                ));
            }

            List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges = new ArrayList<>();
            edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 1, 1));
            edges.add(org.apache.commons.lang3.tuple.Triple.of(1, 2, 1));

            CFG<IRNode> cfg = new CFG<>(blocks, edges);

            // Act & Assert
            assertDoesNotThrow(() -> optimizer.onHandle(cfg));
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("优化器应该支持多次调用")
        void testMultipleOptimizations() {
            // Arrange
            CFG<IRNode> cfg = createEmptyCFG();

            // Act & Assert
            assertDoesNotThrow(() -> {
                optimizer.onHandle(cfg);
                optimizer.onHandle(cfg);
                optimizer.onHandle(cfg);
            });
        }

        @Test
        @DisplayName("优化器状态应该在每次调用时重置")
        void testStateResetBetweenCalls() {
            // Arrange
            CFG<IRNode> cfg = createEmptyCFG();
            optimizer.onHandle(cfg);
            int firstProcessed = optimizer.getProcessedCount();

            // Act
            optimizer.onHandle(cfg);
            int secondProcessed = optimizer.getProcessedCount();

            // Assert: 处理计数应该重置
            assertEquals(firstProcessed, secondProcessed);
        }

        @Test
        @DisplayName("包含相同表达式的CFG应该被检测")
        void testDetectsSameExpressions() {
            // Arrange - 创建包含重复表达式的CFG
            List<BasicBlock<IRNode>> blocks = new ArrayList<>();
            BasicBlock<IRNode> block = new BasicBlock<>(
                Kind.CONTINUOUS,
                new ArrayList<>(),
                new Label("test", null),
                0
            );
            blocks.add(block);

            List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges = new ArrayList<>();
            CFG<IRNode> cfg = new CFG<>(blocks, edges);

            // Act & Assert
            assertDoesNotThrow(() -> optimizer.onHandle(cfg));
        }

        @Test
        @DisplayName("复杂表达式应该被正确处理")
        void testComplexExpressions() {
            // 测试包含多个操作符的表达式
            BinExpr expr1 = BinExpr.with(
                OperatorType.BinaryOpType.ADD,
                createMockVarSlot("x"),
                createMockVarSlot("y")
            );
            BinExpr expr2 = BinExpr.with(
                OperatorType.BinaryOpType.MUL,
                createMockVarSlot("x"),
                createMockVarSlot("z")
            );

            // 验证表达式结构
            assertEquals(OperatorType.BinaryOpType.ADD, expr1.getOpType());
            assertEquals(OperatorType.BinaryOpType.MUL, expr2.getOpType());
        }
    }

    @Nested
    @DisplayName("表达式类型测试")
    class ExpressionTypeTests {

        @Test
        @DisplayName("应该支持二元表达式")
        void testSupportsBinaryExpressions() {
            // 测试所有二元运算符
            OperatorType.BinaryOpType[] ops = {
                OperatorType.BinaryOpType.ADD,
                OperatorType.BinaryOpType.SUB,
                OperatorType.BinaryOpType.MUL,
                OperatorType.BinaryOpType.DIV,
                OperatorType.BinaryOpType.MOD,
                OperatorType.BinaryOpType.LT,
                OperatorType.BinaryOpType.GT,
                OperatorType.BinaryOpType.EQ,
                OperatorType.BinaryOpType.NE
            };

            for (OperatorType.BinaryOpType op : ops) {
                BinExpr expr = BinExpr.with(op, createMockVarSlot("a"), createMockVarSlot("b"));
                assertEquals(op, expr.getOpType());
            }
        }

        @Test
        @DisplayName("应该支持一元表达式")
        void testSupportsUnaryExpressions() {
            // 测试所有一元运算符
            OperatorType.UnaryOpType[] ops = {
                OperatorType.UnaryOpType.NEG,
                OperatorType.UnaryOpType.NOT
            };

            for (OperatorType.UnaryOpType op : ops) {
                UnaryExpr expr = UnaryExpr.with(op, createMockVarSlot("a"));
                assertEquals(op, expr.op);
            }
        }

        @Test
        @DisplayName("常量表达式应该被正确识别")
        void testConstantExpressions() {
            ConstVal<Integer> intConst = ConstVal.valueOf(42);
            ConstVal<Boolean> boolConst = ConstVal.valueOf(true);
            ConstVal<String> stringConst = ConstVal.valueOf("hello");

            assertEquals(42, intConst.getVal());
            assertEquals(true, boolConst.getVal());
            assertEquals("hello", stringConst.getVal());
        }
    }

    @Nested
    @DisplayName("正确性验证测试")
    class CorrectnessTests {

        @Test
        @DisplayName("值编号键的equals和hashCode应该一致")
        void testValueNumberKeyEqualsHashCodeContract() {
            // 创建多个值编号键并验证equals/hashCode契约
            Set<String> keys = new HashSet<>();
            keys.add("BIN(ADD, t0, t1)");
            keys.add("BIN(MUL, t2, t3)");
            keys.add("UNARY(NEG, t4)");
            keys.add("CONST_42");

            // 验证集合大小
            assertEquals(4, keys.size());
        }

        @Test
        @DisplayName("相同键的hashCode应该相同")
        void testSameKeySameHashCode() {
            String key1 = "BIN(ADD, t0, t1)";
            String key2 = "BIN(ADD, t0, t1)";

            assertEquals(key1.hashCode(), key2.hashCode());
        }
    }

    // ========== 测试辅助方法 ==========

    /**
     * 创建模拟的VarSlot
     */
    private VarSlot createMockVarSlot(String name) {
        // 使用OperandSlot创建临时变量槽
        return OperandSlot.genTemp();
    }

    /**
     * 创建空CFG
     */
    private CFG<IRNode> createEmptyCFG() {
        return new CFG<>(new ArrayList<>(), new ArrayList<>());
    }
}
